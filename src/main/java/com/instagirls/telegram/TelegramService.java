package com.instagirls.telegram;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.InputMedia;
import com.pengrad.telegrambot.model.request.InputMediaPhoto;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SendMediaGroup;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.instagirls.PropertiesUtil.POSTED_FILE_URL;

public class TelegramService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramService.class);
    private static TelegramBot bot;

    private static void initBot() {
        LOGGER.info("Initializing bot..");
        if (bot == null) {
            bot = new TelegramBot(System.getenv("bot_token"));
            LOGGER.info("Created new bot.");
        } else {
            LOGGER.info("Bot already initialized.");
        }

    }

    public void postToTelegram(TelegramPost telegramPost) {
        initBot();
        final Set<Long> chatIds = getChatIds();
        sendContentToChats(chatIds, telegramPost);
        setPosted(telegramPost.getInstagramPostId());
    }

    private void setPosted(final String instagramPostId) {
        try {
            Files.write(Paths.get(System.getenv(POSTED_FILE_URL)),
                    instagramPostId.getBytes(),
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            LOGGER.error("Could not append to posted file!", e);
        }
        LOGGER.info("Posted!");
    }

    private void sendContentToChats(final Set<Long> chatIds, final TelegramPost telegramPost) {
        for (Long chatId : chatIds) {
            sendContentToChat(telegramPost, chatId);
        }
    }

    private void sendContentToChat(final TelegramPost telegramPost, final Long chatId) {
        final List<InputMedia<?>> medias = mapMedias(telegramPost);
        final SendMediaGroup request = new SendMediaGroup(chatId, medias.toArray(new InputMedia[0]));
        bot.execute(request);
    }

//    private List<InputMedia<?>> mapMedias(final TelegramPost telegramPost) {
//        List<InputMedia<?>> inputMedia = new ArrayList<>();
//        for (Media instagramPostMedia : telegramPost.getInstagramPostMedias()) {
//            if (instagramPostMedia.getType() == PHOTO) {
//                InputMediaPhoto photo = new InputMediaPhoto(instagramPostMedia.getUrl());
//                photo.caption(telegramPost.getGirlAccountURL());
//                inputMedia.add(photo);
//            } else {
//                InputMediaVideo video = new InputMediaVideo(instagramPostMedia.getUrl());
//                video.caption(telegramPost.getGirlAccountURL());
//                inputMedia.add(video);
//            }
//        }
//        return inputMedia;
//    }

    private List<InputMedia<?>> mapMedias(final TelegramPost telegramPost) {
        List<InputMedia<?>> inputMedia = new ArrayList<>();
        for (Media instagramPostMedia : telegramPost.getInstagramPostMedias()) {
            InputMediaPhoto photo = new InputMediaPhoto(instagramPostMedia.getUrl());
            photo.caption(telegramPost.getGirlAccountURL());
            inputMedia.add(photo);
        }
        return inputMedia;
    }

    private Set<Long> getChatIds() {
        final GetUpdatesResponse updates = bot.execute(new GetUpdates());
        final Set<Long> chatIds = new HashSet<>();
        updates.updates()
                .forEach(update -> {
                    if (update.myChatMember() != null) {
                        chatIds.add(update.myChatMember().chat().id());
                    }
                });

        LOGGER.info(String.format("Chats for posting: %s", chatIds));
        return chatIds;
    }

}
