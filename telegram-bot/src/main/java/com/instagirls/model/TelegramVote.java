package com.instagirls.model;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Table
@Entity
@Data
public class TelegramVote {

    @Id
    @GeneratedValue
    @Type(type = "uuid-char")
    private UUID uuid;

    @Column(nullable = false)
    private Integer telegramUserId;

    @OneToOne
    private TelegramPost telegramPost;

    @Column
    private TelegramVoteType telegramVoteType;

    @CreationTimestamp
    private LocalDateTime createdAt;

}
