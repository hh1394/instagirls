package com.instagirls.telegram;

import com.instagirls.instagram.InstagramService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.InputMedia;
import com.pengrad.telegrambot.model.request.InputMediaPhoto;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SendMediaGroup;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import lombok.SneakyThrows;
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

import static com.instagirls.PropertiesUtil.CHATS_FILE_URL;

public class TelegramService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramService.class);
    private static TelegramBot bot;
    private static Set<Long> chatIds;

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
        final List<InputMedia<?>> medias = mapMedias(telegramPost);
        final SendMediaGroup request = new SendMediaGroup(chatId, medias.toArray(new InputMedia[0]));
        bot.execute(request);
    }

    private List<InputMedia<?>> mapMedias(final TelegramPost telegramPost) {
        List<InputMedia<?>> inputMedia = new ArrayList<>();
        for (Media instagramPostMedia : telegramPost.getInstagramPostMedias()) {
            InputMediaPhoto photo = new InputMediaPhoto(instagramPostMedia.getUrl());
            photo.caption(telegramPost.getGirlAccountURL());
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
