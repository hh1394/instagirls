package com.instagirls.model.telegram;

public enum TelegramChatType {

    PRIVATE_CHAT("Private"), GROUP_CHAT("group");

    private String type;

    TelegramChatType(final String type) {
    }
}
