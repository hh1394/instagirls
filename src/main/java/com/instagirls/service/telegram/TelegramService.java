package com.instagirls.service.telegram;

import com.instagirls.service.instagram.InstagramService;
import com.instagirls.service.telegram.entity.Media;
import com.instagirls.service.telegram.entity.TelegramPost;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.instagirls.util.PropertiesUtil.CHATS_FILE_URL;

public class TelegramService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramService.class);
    private static final String UPDATE_MESSAGE = "send_new_girl";
    private int newGirlCounter = 0;
    private TelegramBot bot;
    private Set<Long> chatIds;
    private Set<Integer> votedUsersIds = new HashSet<>();

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

    private int processUpdates(final List<Update> updates) {
        LOGGER.info(String.format("Got %s updates!", updates.size()));
        updates.forEach(this::processUpdate);
        return updates.get(updates.size() - 1).updateId();
    }

    private void processUpdate(final Update update) {
        if (updateContainsValidVote(update)) {
            ++newGirlCounter;
            votedUsersIds.add(update.callbackQuery().from().id());
            LOGGER.info("Got request for a new girl!");
            LOGGER.info("Current counter: " + newGirlCounter);
            LOGGER.info("User: " + update.callbackQuery().from());

            if (newGirlCounter < 4) {
                incrementMessageReplyMarkup(update);
            } else {
                newGirlCounter = 0;
                votedUsersIds = new HashSet<>();
                removeVoteFromMessageReplyMarkup(update);
                final TelegramPost telegramPost = new InstagramService().generatePost();
                this.postToTelegram(telegramPost);
            }
        }
    }

    private void removeVoteFromMessageReplyMarkup(final Update update) {
        final EditMessageReplyMarkup editMessageReplyMarkup =
                new EditMessageReplyMarkup(update.callbackQuery().message().chat().id(),
                        update.callbackQuery().message().messageId());

        final InlineKeyboardMarkup replyKeyboardMarkup = new InlineKeyboardMarkup();

        final InlineKeyboardButton girlAccountUrlKeyboardButton = buildGirlLinkKeyboardButton(update.callbackQuery().message().replyMarkup().inlineKeyboard()[0][0].url());
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

    private boolean updateContainsValidVote(final Update update) {
        final boolean isUpdateGirl = update.callbackQuery().data().equals(UPDATE_MESSAGE);
        final boolean isNewUserVote = !votedUsersIds.contains(update.callbackQuery().from().id());
        return isUpdateGirl && isNewUserVote;
    }

    public void postToTelegram(TelegramPost telegramPost) {
        initBot();
        loadChatIds();
        sendContentToChats(telegramPost);
        InstagramService.setPosted(telegramPost.getInstagramPostId());
    }

    private void sendContentToChats(final TelegramPost telegramPost) {
        for (Long chatId : chatIds) {
            sendContentToChat(telegramPost, chatId);
        }
    }

    private void sendContentToChat(final TelegramPost telegramPost, final Long chatId) {
        sendMedia(telegramPost, chatId);
        sendCaptionWithReplyKeyboardMarkup(telegramPost, chatId);
    }

    private void sendMedia(final TelegramPost telegramPost, final Long chatId) {
        final List<InputMedia<?>> medias = mapMedias(telegramPost);
        final SendMediaGroup request = new SendMediaGroup(chatId, medias.toArray(new InputMedia[0]));
        bot.execute(request);
    }

    private void sendCaptionWithReplyKeyboardMarkup(final TelegramPost telegramPost, final Long chatId) {
        final SendResponse response = sendCaption(telegramPost, chatId);
        addReplyKeyboardMarkup(telegramPost, chatId, response);
    }

    private void addReplyKeyboardMarkup(final TelegramPost telegramPost, final Long chatId, final SendResponse response) {

        final InlineKeyboardMarkup replyKeyboardMarkup = getReplyInlineKeyboardMarkup(telegramPost.getGirlAccountURL());
        sendReplyKeyboardMarkup(chatId, response, replyKeyboardMarkup);
    }

    private void sendReplyKeyboardMarkup(final Long chatId, final SendResponse response, final InlineKeyboardMarkup replyKeyboardMarkup) {
        final Integer messageId = response.message().messageId();
        final EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup(chatId, messageId);
        editMessageReplyMarkup.replyMarkup(replyKeyboardMarkup);
        bot.execute(editMessageReplyMarkup);
    }

    @NotNull
    private InlineKeyboardMarkup getReplyInlineKeyboardMarkup(final String girlAccountURL) {
        final InlineKeyboardButton girlAccountUrlKeyboardButton = buildGirlLinkKeyboardButton(girlAccountURL);
        final InlineKeyboardButton voteKeyboardButton = buildVoteKeyboardButton();
        return buildKeyboardMarkup(girlAccountUrlKeyboardButton, voteKeyboardButton);
    }

    @NotNull
    private InlineKeyboardMarkup buildKeyboardMarkup(final InlineKeyboardButton girlAccountUrlKeyboardButton, final InlineKeyboardButton voteKeyboardButton) {
        final InlineKeyboardMarkup replyKeyboardMarkup = new InlineKeyboardMarkup();
        replyKeyboardMarkup.addRow(girlAccountUrlKeyboardButton);
        replyKeyboardMarkup.addRow(voteKeyboardButton);
        return replyKeyboardMarkup;
    }

    @NotNull
    private InlineKeyboardButton buildGirlLinkKeyboardButton(final String girlAccountURL) {
        final InlineKeyboardButton girlAccountUrlKeyboardButton = new InlineKeyboardButton("Girl Instagram");
        girlAccountUrlKeyboardButton.url(girlAccountURL);
        return girlAccountUrlKeyboardButton;
    }

    @NotNull
    private InlineKeyboardButton buildVoteKeyboardButton() {
        final InlineKeyboardButton voteKeyboardButton = new InlineKeyboardButton(String.format("Send New Girl! (%s\\4)", newGirlCounter));
        voteKeyboardButton.callbackData(UPDATE_MESSAGE);
        return voteKeyboardButton;
    }

    private SendResponse sendCaption(final TelegramPost telegramPost, final Long chatId) {
        final SendMessage sendMessage = new SendMessage(chatId, telegramPost.getCaption() != null ? telegramPost.getCaption() : "(NO CAPTION)");
        return bot.execute(sendMessage);
    }

    private List<InputMedia<?>> mapMedias(final TelegramPost telegramPost) {
        final List<InputMedia<?>> inputMedia = new ArrayList<>();
        for (Media instagramPostMedia : telegramPost.getInstagramPostMedias()) {
            InputMediaPhoto photo = new InputMediaPhoto(instagramPostMedia.getUrl());
            inputMedia.add(photo);
        }
        return inputMedia;
    }

    @SneakyThrows
    private void loadChatIds() {
        if (chatIds == null) {
            chatIds = Files.lines(Paths.get(System.getenv(CHATS_FILE_URL)))
                    .map(Long::new)
                    .collect(Collectors.toSet());
            LOGGER.info("Loaded chats!");
        } else {
            LOGGER.info("No need to load chats!");

        }
    }

    public void updateChats() throws IOException {
        loadChatIds();
        initBot();
        final Set<Long> chatsWithUpdates = getChatsWithUpdates();
        Set<Long> newChatIds = chatsWithUpdates.stream().filter(chatId -> !chatIds.contains(chatId)).collect(Collectors.toSet());
        LOGGER.info("Detected " + newChatIds.size() + " new chats");
        chatIds.addAll(chatsWithUpdates);
        saveChatIds(newChatIds);
    }

    //TODO optimize
    private void saveChatIds(final Set<Long> newChatIds) throws IOException {
        if (newChatIds != null && !newChatIds.isEmpty()) {
            final Path path = Paths.get(System.getenv(CHATS_FILE_URL));
            for (final Long chatId : newChatIds) {
                Files.write(path,
                        (chatId + "\n").getBytes(),
                        StandardOpenOption.APPEND);
            }
        }
    }

    private Set<Long> getChatsWithUpdates() {
        final GetUpdatesResponse updates = bot.execute(new GetUpdates());
        final Set<Long> chatIds = new HashSet<>();
        updates.updates()
                .forEach(update -> {
                    if (update.myChatMember() != null) {
                        chatIds.add(update.myChatMember().chat().id());
                    }
                });

        LOGGER.info(String.format("Chats with updates: %s", chatIds));
        return chatIds;
    }
}
