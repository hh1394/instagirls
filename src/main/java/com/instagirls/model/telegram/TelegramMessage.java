package com.instagirls.model.telegram;

import lombok.Data;

import javax.persistence.*;

@Table
@Entity
@Data
public class TelegramMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

    @Column(nullable = false)
    private Integer telegramMessageId;

    @ManyToOne(optional = false)
    private TelegramUser telegramUser;

    @ManyToOne(optional = false)
    private TelegramChat telegramChat;

    @Column(nullable = false)
    private String text;

}
