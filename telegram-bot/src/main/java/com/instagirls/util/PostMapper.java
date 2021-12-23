package com.instagirls.util;

import com.instagirls.model.TelegramVote;
import com.instagirls.model.TelegramVoteType;
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
        telegramVote.setTelegramVoteType(TelegramVoteType.valueOf(update.callbackQuery().data()));
        return telegramVote;
    }
}
