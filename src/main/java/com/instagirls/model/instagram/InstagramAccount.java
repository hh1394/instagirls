package com.instagirls.model.instagram;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table
@Data
public class InstagramAccount {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(unique = true)
    private Long instagramPk;

    @OneToMany(fetch = FetchType.EAGER)
    private List<InstagramPost> instagramPosts;

    @Column(nullable = false)
    private boolean isActive = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public InstagramAccount(final String username) {
        this.username = username;
    }

    public InstagramAccount() {

    }
}
