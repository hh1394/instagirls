package com.instagirls.model.telegram;

import com.pengrad.telegrambot.model.Chat;
import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table
@Data
public class TelegramChat {

    @Id
    @GeneratedValue
    @Type(type = "uuid-char")
    private UUID uuid;

    @Column(nullable = false)
    private Integer telegramChatId;

    @Column(nullable = false)
    private Chat.Type telegramChatType;

    @Column
    private String title;
}
