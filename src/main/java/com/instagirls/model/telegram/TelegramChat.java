package com.instagirls.model.telegram;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table
@Data
public class TelegramChat {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

    @Column(nullable = false)
    private Integer telegramChatId;

    @Column(nullable = false)
    private TelegramChatType telegramChatType;

    @Column
    private String title;
}
