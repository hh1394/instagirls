package com.instagirls.service.telegram;

import com.instagirls.dto.InstagramPostDTO;
import com.instagirls.model.instagram.InstagramMedia;
import com.instagirls.model.telegram.TelegramPost;
import com.instagirls.model.telegram.TelegramVote;
import com.instagirls.repository.TelegramPostRepository;
import com.instagirls.repository.TelegramVoteRepository;
import com.instagirls.service.instagram.InstagramService;
import com.instagirls.util.PostMapper;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.InputMedia;
import com.pengrad.telegrambot.model.request.InputMediaPhoto;
import com.pengrad.telegrambot.request.EditMessageReplyMarkup;
import com.pengrad.telegrambot.request.SendMediaGroup;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Service
public class TelegramService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramService.class);
    private static final String UPDATE_MESSAGE = "send_new_girl";
    private static final String CHAT_ID = System.getenv("chat_id");
    private TelegramBot bot;

    @Autowired
    private TelegramVoteRepository telegramVoteRepository;

    @Autowired
    private TelegramPostRepository telegramPostRepository;

    @Autowired
    private InstagramService instagramService;

    @PostConstruct
    private void initBot() {
        LOGGER.info("Initializing bot..");
        if (bot == null) {
            bot = new TelegramBot(System.getenv("bot_token"));
            bot.setUpdatesListener(this::processUpdates);
            LOGGER.info("Created new bot.");
        } else {
            LOGGER.info("Bot already initialized.");
        }
        sendNewPostToTelegram();
    }

    @Scheduled(cron = "@daily")
    public void sendNewPostToTelegram() {
        telegramVoteRepository.deleteAll();
        final InstagramPostDTO instagramPostDTO = instagramService.getNewMostLikedPostFromRandomAccount();
        TelegramPost telegramPost = new TelegramPost(instagramPostDTO.getInstagramPost());
        telegramPost = telegramPostRepository.save(telegramPost);
        sendContentToChat(telegramPost, instagramPostDTO.getInstagramAccountURL());

        instagramService.setPosted(telegramPost.getInstagramPost());
    }

    private int processUpdates(final List<Update> updates) {
        LOGGER.info(String.format("Got %s updates!", updates.size()));
        updates.forEach(this::processUpdate);
        return updates.get(updates.size() - 1).updateId();
    }

    private void processUpdate(final Update update) {
        if (updateContainsValidVote(update)) {
            final TelegramVote telegramVote = PostMapper.mapToTelegramVote(update);
            telegramVote.setTelegramPost(telegramPostRepository.findTopByOrderByIdDesc());
            telegramVoteRepository.save(telegramVote);
            LOGGER.info("Got request for a new girl!");
            LOGGER.info("User: " + update.callbackQuery().from());
            checkVote(update);
        }
    }

    private void checkVote(final Update update) {
        if (isEnoughVotes()) {
            removeVoteFromMessageReplyMarkup(update);
            sendNewPostToTelegram();
        }else{
            incrementMessageReplyMarkup(update);
        }
    }

    private void removeVoteFromMessageReplyMarkup(final Update update) {
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

    private void sendContentToChat(final TelegramPost telegramPost, final String instagramAccountURL) {
        sendMedia(telegramPost);
        sendCaptionWithReplyKeyboardMarkup(telegramPost, instagramAccountURL);
    }

    private void sendCaptionWithReplyKeyboardMarkup(final TelegramPost telegramPost, final String instagramAccountURL) {
        final SendResponse response = sendCaption(telegramPost);
        addReplyKeyboardMarkup(response, instagramAccountURL);
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
    private InlineKeyboardButton buildVoteKeyboardButton() {
        final int newTelegramPostCounter = telegramVoteRepository.findByTelegramPost(telegramPostRepository.findTopByOrderByIdDesc()).size();
        final InlineKeyboardButton voteKeyboardButton = new InlineKeyboardButton(String.format("Send New Girl! (%s\\4)", newTelegramPostCounter));
        voteKeyboardButton.callbackData(UPDATE_MESSAGE);
        return voteKeyboardButton;
    }

    @NotNull
    private InlineKeyboardButton buildInstagramAccountLinkKeyboardButton(final String instagramAccountURL) {
        final InlineKeyboardButton girlAccountUrlKeyboardButton = new InlineKeyboardButton("Girl Instagram");
        girlAccountUrlKeyboardButton.url(instagramAccountURL);
        return girlAccountUrlKeyboardButton;
    }

    private SendResponse sendCaption(final TelegramPost telegramPost) {
        final SendMessage sendMessage = new SendMessage(CHAT_ID, telegramPost.getInstagramPost().getCaption());
        return bot.execute(sendMessage);
    }

    private void sendMedia(final TelegramPost telegramPost) {
        final List<InputMedia<?>> medias = mapMedias(telegramPost);
        final SendMediaGroup request = new SendMediaGroup(CHAT_ID, medias.toArray(new InputMedia[0]));
        bot.execute(request);
    }

    private List<InputMedia<?>> mapMedias(final TelegramPost telegramPost) {
        final List<InputMedia<?>> inputMedia = new ArrayList<>();
        for (InstagramMedia instagramPostMedia : telegramPost.getInstagramPost().getInstagramMedia()) {
            InputMediaPhoto photo = new InputMediaPhoto(instagramPostMedia.getUrl());
            inputMedia.add(photo);
        }
        return inputMedia;
    }


    private boolean isEnoughVotes() {
        final TelegramPost currentPost = telegramPostRepository.findTopByOrderByIdDesc();
        List<TelegramVote> votes = telegramVoteRepository.findByTelegramPost(currentPost);
        return votes.size() > 3;
    }

    private boolean updateContainsValidVote(final Update update) {
        if (update.callbackQuery() == null) {
            return false;
        }
        final boolean isUpdateGirl = update.callbackQuery().data().equals(UPDATE_MESSAGE);
        final boolean isNewUserVote =
                telegramVoteRepository.findByTelegramUserIdAndTelegramPost(update.callbackQuery().from().id(),
                        telegramPostRepository.findTopByOrderById()) == null;
        return isUpdateGirl && isNewUserVote;
    }

}
