package com.instagirls.model.telegram;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Table
@Entity
@Data
public class TelegramVote {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

    @Column(nullable = false)
    private Integer telegramUserId;

    @OneToOne
    private TelegramPost telegramPost;

    @CreationTimestamp
    private LocalDateTime createdAt;

}
