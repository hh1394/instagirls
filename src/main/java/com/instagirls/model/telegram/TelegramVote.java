package com.instagirls.model.telegram;

import lombok.Data;

import javax.persistence.*;

@Table
@Entity
@Data
public class TelegramVote {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private Integer telegramUserId;

    @OneToOne
    private TelegramPost telegramPost;
}
