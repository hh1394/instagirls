package com.instagirls.model.telegram;

import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table
@Data
public class TelegramChat {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private String id;

    @Column(nullable = false, unique = true)
    private String telegramChatId;

    @Column(nullable = false)
    private boolean isActive = true;

    @CreatedDate
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
