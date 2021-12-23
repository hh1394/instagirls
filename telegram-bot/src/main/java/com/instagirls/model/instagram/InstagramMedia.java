package com.instagirls.model.instagram;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table
@Data
public class InstagramMedia {

    @Id
    @GeneratedValue
    @Type(type = "uuid-char")
    private UUID uuid;

    @Column(nullable = false, columnDefinition = "text", length = 512)
    private String url;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public InstagramMedia() {

    }

    public InstagramMedia(final String url) {
        this.url = url;
    }
}
