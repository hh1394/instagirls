package com.instagirls.model.telegram;


import com.instagirls.model.instagram.InstagramPost;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table
@Data
public class TelegramPost {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String id;

    @ManyToOne(optional = false)
    private TelegramChat telegramChat;

    @OneToOne(optional = false)
    private InstagramPost instagramPost;

    @Column
    private Integer votesForNewPost;

    @CreatedDate
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
