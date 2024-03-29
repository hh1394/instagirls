package com.instagirls.model;

import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table
@Data
public class TelegramUser {

    @Id
    @GeneratedValue
    @Type(type = "uuid-char")
    private UUID uuid;

    @Column(nullable = false, unique = true)
    private Long telegramId;

    @Column
    private String firstName;

    @Column
    private String lastName;

    @Column
    private String username;

}
