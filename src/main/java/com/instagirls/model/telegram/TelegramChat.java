package com.instagirls.model.telegram;

import com.pengrad.telegrambot.model.Chat;
import lombok.Data;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table
@Data
public class TelegramChat {

    @Id
    @GeneratedValue
    private UUID uuid;

    @Column(nullable = false)
    private Integer telegramChatId;

    @Column(nullable = false)
    private Chat.Type telegramChatType;

    @Column
    private String title;
}
