package com.instagirls.model.telegram;


import com.instagirls.model.instagram.InstagramPost;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table
@Data
public class TelegramPost {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(optional = false)
    private TelegramChat telegramChat;

    @OneToOne(optional = false)
    private InstagramPost instagramPost;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public TelegramPost(final InstagramPost instagramPost) {
        this.instagramPost = instagramPost;
    }

    public TelegramPost() {

    }
}
