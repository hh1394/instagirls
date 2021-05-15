package com.instagirls.util;

import com.instagirls.model.instagram.InstagramPost;
import com.instagirls.model.telegram.TelegramPost;
import com.instagirls.model.telegram.TelegramVote;
import com.pengrad.telegrambot.model.Update;

public class PostMapper {

//    public static TelegramPost mapToTelegram(final InstagramPost instagramPost) {
//
//        final TelegramPost telegramPost = new TelegramPost();
//        telegramPost.set
//
//    }

    public static TelegramVote mapToTelegramVote(final Update update) {
        final TelegramVote telegramVote = new TelegramVote();
        telegramVote.setTelegramUserId(update.callbackQuery().from().id());
        return telegramVote;
    }
}
