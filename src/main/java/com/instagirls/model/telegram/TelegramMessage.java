package com.instagirls.model.telegram;

import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.UUID;

@Table
@Entity
@Data
public class TelegramMessage {

    @Id
    @GeneratedValue
    @Type(type = "uuid-char")
    private UUID uuid;

    @Column(nullable = false)
    private Integer telegramMessageId;

    @ManyToOne(optional = false)
    private TelegramUser telegramUser;

    @Column(nullable = false)
    private String text;

    public TelegramMessage() {
    }

    public TelegramMessage(final Integer telegramMessageId, final TelegramUser telegramUser, final String text) {
        this.telegramMessageId = telegramMessageId;
        this.telegramUser = telegramUser;
        this.text = text;
    }
}
