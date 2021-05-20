package com.instagirls.model.telegram;

import lombok.Data;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table
@Data
public class TelegramUser {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private Integer telegramId;

    @Column
    private String firstName;

    @Column
    private String lastName;

    @Column
    private String username;

}
