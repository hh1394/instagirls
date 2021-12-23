package com.instagirls.model.instagram;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table
@Data
public class InstagramPost {

    @Id
    @GeneratedValue
    @Type(type = "uuid-char")
    private UUID uuid;

    @Column(nullable = false, unique = true)
    private String instagramPostId;

    @Column(nullable = false, unique = true)
    private String instagramPostCode;

    @OneToMany(fetch = FetchType.EAGER)
    private List<InstagramMedia> instagramMedia;

    @Column(nullable = false)
    private Integer likes = 0;

    @Column(nullable = false)
    private boolean posted = false;

    @Column(nullable = false)
    private long takenAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public InstagramPost() {
    }
}
