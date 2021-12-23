package com.instagirls.model;


import com.instagirls.dto.InstagramPostDTO;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
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

    @Column(nullable = false)
    private String instagramPostCode;

    @Column(nullable = false)
    private String instagramUsername;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public TelegramPost(final InstagramPostDTO instagramPostDTO) {
        this.instagramPostCode = instagramPostDTO.getPostCode();
        this.instagramUsername = instagramPostDTO.getAccount();
    }

    public TelegramPost() {

    }
}
