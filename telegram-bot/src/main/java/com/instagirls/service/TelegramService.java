package com.instagirls.service;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.instagirls.dto.InstagramPostDTO;
import com.instagirls.exception.CurrentPostNotFoundException;
import com.instagirls.exception.EmptyTelegramMessageException;
import com.instagirls.exception.InternalRequestFailedException;
import com.instagirls.exception.UnsupportedMediaFormatException;
import com.instagirls.model.*;
import com.instagirls.repository.TelegramMessageRepository;
import com.instagirls.repository.TelegramPostRepository;
import com.instagirls.repository.TelegramUserRepository;
import com.instagirls.repository.TelegramVoteRepository;
import com.instagirls.util.PostMapper;
import com.instagirls.util.ThreadUtil;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.EditMessageReplyMarkup;
import com.pengrad.telegrambot.request.SendMediaGroup;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.instagirls.model.TelegramVoteType.*;
import static com.instagirls.util.Util.generateEndearment;
import static com.instagirls.util.Util.generateInsult;

@Service
public class TelegramService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramService.class);
    private static final Long CREATOR_ACCOUNT_ID = 441477123L;
    private static final String COMMAND_START = "/start";
    private static final String COMMAND_ADD_GIRL = "/addgirl";
    private static final String COMMAND_SEND_GIRL = "/sendgirl";
    private static final String CHAT_ID = System.getenv("chat_id");
    private final ObjectMapper mapper = new ObjectMapper();
    private TelegramBot bot;

    @Autowired
    private TelegramVoteRepository telegramVoteRepository;
    @Autowired
    private TelegramPostRepository telegramPostRepository;
    @Autowired
    private TelegramMessageRepository telegramMessageRepository;
    @Autowired
    private TelegramUserRepository telegramUserRepository;
    @Autowired
    private InstagramAccessService instagramAccessService;
    @Autowired
    private CaptionService captionService;

    private static String extractAccountFromURL(final String url) {
        final String domain = "instagram.com/";
        int beginIndex = url.indexOf(domain) + domain.length();
        String substring = url.substring(beginIndex);
        if (substring.contains("/")) {
            int endIndex = substring.indexOf("/") + beginIndex;
            return url.substring(beginIndex, endIndex);
        } else if (substring.contains("?")) {
            int endIndex = substring.indexOf("?") + beginIndex;
            return url.substring(beginIndex, endIndex);
        } else {
            return url.substring(beginIndex);
        }
    }

    @PostConstruct
    private void initService() {
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        initBot();
    }

    private void initBot() {
        LOGGER.info("Initializing bot..");
        if (bot == null) {
            bot = new TelegramBot(System.getenv("bot_token"));
            bot.setUpdatesListener(this::processUpdates);
            LOGGER.info("Created new bot.");
        } else {
            LOGGER.info("Bot already initialized.");
        }
    }

    public void sendDailyGirl() {
        sendNewPostToTelegram(false);
    }

    private void sendNewPostToTelegram(final boolean sameGirl) {
        telegramVoteRepository.deleteAll();
        final InstagramPostDTO instagramPostDTO;
        Optional<TelegramPost> currentPost = getCurrentPost();


        if (currentPost.isPresent()) {
            String instagramUsername = currentPost.get().getInstagramUsername();
            if (sameGirl) {
                instagramPostDTO = instagramAccessService.getNewMostLikedPostFromAccount(instagramUsername);
            } else {
                instagramPostDTO = instagramAccessService.getNewMostLikedPostFromRandomAccountExcept(instagramUsername);
            }
        } else {
            instagramPostDTO = instagramAccessService.getNewMostLikedPostFromRandomAccount();
        }
        TelegramPost telegramPost = new TelegramPost(instagramPostDTO);
        telegramPostRepository.save(telegramPost);
        sendContentToChat(instagramPostDTO);

        instagramAccessService.setPosted(instagramPostDTO.getPostCode());
    }

    private int processUpdates(final List<Update> updates) {
        LOGGER.info(String.format("Got %s updates!", updates.size()));
        try {
            updates.forEach(this::processUpdate);
        } catch (UncheckedIOException exception) {
            LOGGER.info("Update failed.");
        }
        return updates.get(updates.size() - 1).updateId();
    }

    @SneakyThrows
    private void processUpdate(final Update update) {
        LOGGER.debug(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(update));
        if (update.message() != null) {
            addUserIfNeeded(update.message().from());
        }
        if (update.callbackQuery() != null && update.callbackQuery().data() != null && isNewVote(update)) {
            final TelegramVote telegramVote = PostMapper.mapToTelegramVote(update);
            telegramVote.setTelegramPost(getCurrentPost().orElseThrow(CurrentPostNotFoundException::new));
            telegramVoteRepository.save(telegramVote);
            LOGGER.info(String.format("Got %s request!", update.callbackQuery().data()));
            LOGGER.info("User: " + update.callbackQuery().from());
            checkVote(update);
        } else if (updateContainsValidCommand(update)) {
            final String command = update.message().text();
            switch (command) {
                case COMMAND_START:
                    processStartCommand(update);
                    break;
                case COMMAND_ADD_GIRL:
                    processAddGirlCommand(update);
                    break;
                case COMMAND_SEND_GIRL:
                    processSendGirlCommand(update);
                    break;
                default:
                    processMessageFromUser(update);
                    break;
            }
        }
    }

    private void processSendGirlCommand(final Update update) {
        if (update.message().from().id().equals(CREATOR_ACCOUNT_ID)) {
            sendNewPostToTelegram(false);
        } else {
            sendMessage(update.message().chat().id().toString(), "You're not my daddy!");
        }
    }

    private void addUserIfNeeded(final User user) {
        if (telegramUserRepository.findByTelegramId(user.id()) == null) {
            LOGGER.info("Adding new user!");
            LOGGER.info(user.toString());

            final TelegramUser telegramUser = mapToTelegramUser(user);
            telegramUserRepository.save(telegramUser);
        }
    }

    private TelegramUser mapToTelegramUser(final User user) {
        final TelegramUser telegramUser = new TelegramUser();
        telegramUser.setTelegramId(user.id());
        telegramUser.setUsername(user.username());
        telegramUser.setFirstName(user.firstName());
        telegramUser.setLastName(user.lastName());
        return telegramUser;
    }

    private void processMessageFromUser(final Update update) {
        if (update.message().chat() != null) {
            final Chat.Type type = update.message().chat().type();
            if (type == Chat.Type.Private) {
                processPrivateMessage(update);
            } else {
                LOGGER.info("Unsupported chat type: " + type);
//                sendMessage(update.message().chat().id().toString(), String.format("%s, %s, call me directly!", update.message().from().firstName(), generateEndearment()));
            }
        }
    }

    private void processPrivateMessage(final Update update) {
        final TelegramMessage previousUserCommandMessage = getLastUserMessage(update.message().from().id());
        if (previousUserCommandMessage == null) {
            return;
        }
        if (COMMAND_ADD_GIRL.equals(previousUserCommandMessage.getText())) {
            final List<String> accountUsernames = extractInstagramAccount(update.message().text()).lines().collect(Collectors.toList());
            loadGirl(update.message().chat().id().toString(), accountUsernames);
        } else {
            sendMessage(update.message().chat().id().toString(), String.format("Won't do, %s!", generateEndearment()));
        }
        telegramMessageRepository.delete(previousUserCommandMessage);
    }

    private void loadGirl(final String chatId, final List<String> accountUsernames) {
        sendMessage(chatId, String.format("Loading %s... Anything else?", accountUsernames));

        accountUsernames.forEach(u -> instagramAccessService.loadNewAccount(u).handle((res, ex) -> {
            if (ex == null) {
                sendMessage(
                        chatId,
                        String.format("Loaded %s for you, %s!", u, generateEndearment()));
            } else {
                processCompletionExceptionHttpStatusCode(chatId, u, ex);
            }
            return null;
        }));
    }

    private void processCompletionExceptionHttpStatusCode(final String chatId, final String accountUsername, final Throwable ex) {
        if (ex.getCause() instanceof InternalRequestFailedException) {
            final Integer statusCode = ((InternalRequestFailedException) ex.getCause()).getStatusCode();
            switch (statusCode) {
                case 409:
                    sendMessage(chatId, String.format("We already have %s, %s!", accountUsername, generateEndearment()));
                    break;
                case 404:
                    sendMessage(chatId, String.format("%s doesn't exists or is private! %s, provide an actual and public account..", accountUsername, generateEndearment()));
                    break;
                default:
                    LOGGER.error("Unexpected status code: " + statusCode);
            }
        }
    }

    private String extractInstagramAccount(final String text) {
        if (text != null) {
            if (text.contains("/")) {
                return extractAccountFromURL(text);
            }
            return sanitizeAccount(text);
        }
        throw new EmptyTelegramMessageException();
    }

    private String sanitizeAccount(final String text) {
        return text.replace("/", "");
    }

    private TelegramMessage getLastUserMessage(final Long telegramUserId) {
        final TelegramUser telegramUser = telegramUserRepository.findByTelegramId(telegramUserId);
        return telegramMessageRepository.findTopTelegramMessageByTelegramUser(telegramUser);
    }

    private void processStartCommand(final Update update) {
        sendMessage(update.message().from().id().toString(),
                "You can add a girl by sending /addgirl command.");
    }

    private void processAddGirlCommand(final Update update) {
        LOGGER.info("Got request for a new girl account!");
        final Long telegramUserId = update.message().from().id();
        final TelegramUser telegramUser = telegramUserRepository.findByTelegramId(telegramUserId);
        telegramMessageRepository.save(new TelegramMessage(update.message().messageId(), telegramUser, update.message().text()));
        sendMessage(telegramUserId.toString(),
                String.format("Send me girl account you want to add, %s..", generateEndearment()));
    }

    private boolean updateContainsValidCommand(final Update update) {
        if (update.message() != null && update.message().text() != null) {
            final String command = update.message().text();
            LOGGER.info("Got command: " + command);
            return true;
        }
        return false;
    }

    private void checkVote(final Update update) {
        final TelegramVoteType telegramVoteType = valueOf(update.callbackQuery().data());
        if (isEnoughVotes(telegramVoteType)) {
            removeVotesFromMessageReplyMarkup(update);
            switch (telegramVoteType) {
                case BAN_GIRL:
                    banCurrentGirl().thenAccept(username -> {
                        sendMessage(update.callbackQuery().message().chat().id().toString(), String.format("Banned that %s '%s' for you!", generateInsult(), username));
                        sendNewPostToTelegram(false);
                    });
                    break;
                case SEND_SAME_GIRL:
                    sendNewPostToTelegram(true);
                    break;
                case SEND_NEW_GIRL:
                    sendNewPostToTelegram(false);
                    break;
            }
        } else {
            incrementMessageReplyMarkup(update);
        }
    }

    private CompletableFuture<String> banCurrentGirl() {
        String instagramUsername = getCurrentPost().orElseThrow(CurrentPostNotFoundException::new).getInstagramUsername();
        return instagramAccessService.disableAccount(instagramUsername).thenApply(v -> instagramUsername);
    }

    private void removeVotesFromMessageReplyMarkup(final Update update) {
        final EditMessageReplyMarkup editMessageReplyMarkup =
                new EditMessageReplyMarkup(update.callbackQuery().message().chat().id(),
                        update.callbackQuery().message().messageId());

        final InlineKeyboardMarkup replyKeyboardMarkup = new InlineKeyboardMarkup();

        final InlineKeyboardButton girlAccountUrlKeyboardButton = buildInstagramAccountLinkKeyboardButton(update.callbackQuery().message().replyMarkup().inlineKeyboard()[0][0].url());
        replyKeyboardMarkup.addRow(girlAccountUrlKeyboardButton);
        editMessageReplyMarkup.replyMarkup(replyKeyboardMarkup);
        bot.execute(editMessageReplyMarkup);
    }

    private void incrementMessageReplyMarkup(final Update update) {
        final EditMessageReplyMarkup editMessageReplyMarkup =
                new EditMessageReplyMarkup(update.callbackQuery().message().chat().id(),
                        update.callbackQuery().message().messageId());

        final InlineKeyboardMarkup replyKeyboardMarkup =
                getReplyInlineKeyboardMarkup(
                        update.callbackQuery().message().replyMarkup().inlineKeyboard()[0][0].url());

        editMessageReplyMarkup.replyMarkup(replyKeyboardMarkup);
        bot.execute(editMessageReplyMarkup);
    }

    private void sendContentToChat(final InstagramPostDTO instagramPostDTO) {
        sendMedia(instagramPostDTO);
        sendCaptionWithReplyKeyboardMarkup(instagramPostDTO.getAccountURL());
    }

    private void sendCaptionWithReplyKeyboardMarkup(final String instagramAccountURL) {
        LOGGER.info("Sending caption with reply keyboard markup..");
        final SendResponse response = sendMessage(CHAT_ID, captionService.getCaption());
        if (!response.isOk() && response.description().contains("retry")) {
            LOGGER.info("Could not send reply keyboard markup. Retrying..");
            ThreadUtil.sleepMinutes(1);
            sendCaptionWithReplyKeyboardMarkup(instagramAccountURL);
        } else {
            LOGGER.info("Adding reply keyboard markup..");
            addReplyKeyboardMarkup(response, instagramAccountURL);
        }
        LOGGER.info("Done!");
    }

    private void addReplyKeyboardMarkup(final SendResponse response, final String instagramAccountURL) {
        final InlineKeyboardMarkup replyKeyboardMarkup = getReplyInlineKeyboardMarkup(instagramAccountURL);
        sendReplyKeyboardMarkup(response, replyKeyboardMarkup);
    }

    private void sendReplyKeyboardMarkup(final SendResponse response, final InlineKeyboardMarkup replyKeyboardMarkup) {
        final Integer messageId = response.message().messageId();
        final EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup(CHAT_ID, messageId);
        editMessageReplyMarkup.replyMarkup(replyKeyboardMarkup);
        bot.execute(editMessageReplyMarkup);
    }

    @NotNull
    private InlineKeyboardMarkup getReplyInlineKeyboardMarkup(final String instagramAccountURL) {
        final InlineKeyboardButton girlAccountUrlKeyboardButton = buildInstagramAccountLinkKeyboardButton(instagramAccountURL);
        final InlineKeyboardButton newGirlVoteKeyboardButton = buildNewGirlVoteKeyboardButton();
        final InlineKeyboardButton sameGirlVoteKeyboardButton = buildSameGirlVoteKeyboardButton();
        final InlineKeyboardButton banGirlVoteKeyboardButton = buildBanGirlVoteKeyboardButton();
        return buildKeyboardMarkup(girlAccountUrlKeyboardButton, newGirlVoteKeyboardButton, sameGirlVoteKeyboardButton, banGirlVoteKeyboardButton);
    }

    @NotNull
    private InlineKeyboardMarkup buildKeyboardMarkup(final InlineKeyboardButton... inlineKeyboardButtons) {
        final InlineKeyboardMarkup replyKeyboardMarkup = new InlineKeyboardMarkup();
        Arrays.stream(inlineKeyboardButtons).forEach(replyKeyboardMarkup::addRow);
        return replyKeyboardMarkup;
    }

    @NotNull
    private InlineKeyboardButton buildSameGirlVoteKeyboardButton() {
        final int newTelegramPostCounter = getNewTelegramVoteCounter(SEND_SAME_GIRL);
        final InlineKeyboardButton voteKeyboardButton = new InlineKeyboardButton(String.format("Send Same Girl! (%s\\4)", newTelegramPostCounter));
        voteKeyboardButton.callbackData(SEND_SAME_GIRL.name());
        return voteKeyboardButton;
    }

    @NotNull
    private InlineKeyboardButton buildBanGirlVoteKeyboardButton() {
        final int newTelegramPostCounter = getNewTelegramVoteCounter(BAN_GIRL);
        final InlineKeyboardButton voteKeyboardButton = new InlineKeyboardButton(String.format("Ban Girl! (%s\\8)", newTelegramPostCounter));
        voteKeyboardButton.callbackData(BAN_GIRL.name());
        return voteKeyboardButton;
    }

    @NotNull
    private InlineKeyboardButton buildNewGirlVoteKeyboardButton() {
        final int newTelegramPostCounter = getNewTelegramVoteCounter(SEND_NEW_GIRL);
        final InlineKeyboardButton voteKeyboardButton = new InlineKeyboardButton(String.format("Send New Girl! (%s\\4)", newTelegramPostCounter));
        voteKeyboardButton.callbackData(SEND_NEW_GIRL.name());
        return voteKeyboardButton;
    }

    private int getNewTelegramVoteCounter(final TelegramVoteType telegramVoteType) {
        final TelegramPost telegramPost = getCurrentPost().orElseThrow(CurrentPostNotFoundException::new);
        return telegramVoteRepository.findByTelegramPostAndTelegramVoteType(telegramPost, telegramVoteType).size();
    }

    private Optional<TelegramPost> getCurrentPost() {
        return telegramPostRepository.findTopByOrderByCreatedAtDesc();
    }

    @NotNull
    private InlineKeyboardButton buildInstagramAccountLinkKeyboardButton(final String instagramAccountURL) {
        final InlineKeyboardButton girlAccountUrlKeyboardButton = new InlineKeyboardButton("Girl Instagram");
        girlAccountUrlKeyboardButton.url(instagramAccountURL);
        return girlAccountUrlKeyboardButton;
    }

    public SendResponse sendMessage(final String chatId, final String message) {
        final SendMessage sendMessage = new SendMessage(chatId, message);
        return bot.execute(sendMessage);
    }

    private void sendMedia(final InstagramPostDTO instagramPostDTO) {
        final List<InputMedia<?>> medias = mapMedias(instagramPostDTO);
        LOGGER.info("Sending media to chat_id " + CHAT_ID);
        final SendMediaGroup request = new SendMediaGroup(CHAT_ID, medias.toArray(new InputMedia[0]));
        bot.execute(request);
    }

    private List<InputMedia<?>> mapMedias(final InstagramPostDTO instagramPostDTO) {
        final List<InputMedia<?>> inputMedia = new ArrayList<>();
        for (String mediaURL : instagramPostDTO.getMediaURLs()) {
            if (mediaURL.contains(".mp4?")) {
                InputMediaVideo video = new InputMediaVideo(mediaURL);
                inputMedia.add(video);
            } else if (mediaURL.contains(".jpg?")) {
                InputMediaPhoto photo = new InputMediaPhoto(mediaURL);
                inputMedia.add(photo);
            } else {
                throw new UnsupportedMediaFormatException("Unknown file format: " + mediaURL);
            }
        }
        return inputMedia;
    }


    private boolean isEnoughVotes(final TelegramVoteType telegramVoteType) {
        final TelegramPost currentPost = getCurrentPost().orElseThrow(CurrentPostNotFoundException::new);
        List<TelegramVote> votes = telegramVoteRepository.findByTelegramPostAndTelegramVoteType(currentPost, telegramVoteType);
        if (SEND_NEW_GIRL.equals(telegramVoteType) || SEND_SAME_GIRL.equals(telegramVoteType)) {
            return votes.size() > 3;
        }
        return votes.size() > 7;
    }

    private boolean isNewVote(final Update update) {
        if (update.callbackQuery().from().id().equals(CREATOR_ACCOUNT_ID)) {
            return true;
        }
        if (update.callbackQuery() == null) {
            return false;
        }
        final TelegramVoteType telegramVoteType = valueOf(update.callbackQuery().data());
        return telegramVoteRepository.findByTelegramUserIdAndTelegramPostAndTelegramVoteType(update.callbackQuery().from().id(),
                getCurrentPost().orElseThrow(CurrentPostNotFoundException::new), telegramVoteType) == null;
    }


}
