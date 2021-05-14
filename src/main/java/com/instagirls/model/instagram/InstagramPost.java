package com.instagirls.model.instagram;

import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table
@Data
public class InstagramPost {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String id;

    @Column(nullable = false, unique = true)
    private String instagramPostId;

    @OneToMany
    private List<InstagramMedia> instagramMedia;

    @ManyToOne
    private InstagramAccount instagramAccount;

    @Column(nullable = false)
    private Integer likes = 0;

    @Column(nullable = false)
    private boolean posted = false;

    @Column
    private String caption = "(NO CAPTION)";

    @CreatedDate
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
