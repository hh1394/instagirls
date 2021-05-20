package com.instagirls.model.telegram;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Table
@Entity
@Data
public class TelegramVote {

    @Id
    @GeneratedValue
    private UUID uuid;

    @Column(nullable = false)
    private Integer telegramUserId;

    @OneToOne
    private TelegramPost telegramPost;

    @CreationTimestamp
    private LocalDateTime createdAt;

}
