package com.instagirls.model.instagram;

import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table
@Data
@Builder
public class InstagramMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String id;

    @Column(nullable = false, unique = true)
    private String url;

    @ManyToOne
    private InstagramPost instagramPost;

    @CreatedDate
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
