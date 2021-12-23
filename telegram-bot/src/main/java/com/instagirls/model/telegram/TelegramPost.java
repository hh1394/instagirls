package com.instagirls.model.telegram;


import com.instagirls.model.instagram.InstagramPost;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table
@Data
public class TelegramPost {

    @Id
    @GeneratedValue
    @Type(type = "uuid-char")
    private UUID uuid;

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
