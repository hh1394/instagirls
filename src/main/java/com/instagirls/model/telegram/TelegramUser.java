package com.instagirls.model.telegram;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table
@Data
public class TelegramUser {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

    @Column(nullable = false)
    private Integer telegramId;

    @Column
    private String firstName;

    @Column
    private String lastName;

    @Column
    private String username;

}
