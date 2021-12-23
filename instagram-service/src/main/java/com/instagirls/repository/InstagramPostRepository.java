package com.instagirls.repository;

import com.instagirls.model.InstagramPost;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InstagramPostRepository extends CrudRepository<InstagramPost, UUID> {

    @Query(nativeQuery = true,
            value = "SELECT * FROM instagram_post AS post WHERE post.uuid IN \n" +
                    "(SELECT instagram_posts_uuid FROM instagram_account_instagram_posts WHERE instagram_account_uuid = ?1) \n" +
                    "order by post.taken_at desc\n" +
                    "limit 1")
    Optional<InstagramPost> findTopByInstagramAccountOrderByTakenAtDesc(final String instagramAccountUUID);

    InstagramPost findByInstagramPostCode(String postCode);

    @Modifying
    @Query("update InstagramPost post set post.posted = true where post.instagramPostCode = :postCode")
    void setPosted(@Param("phone") String postCode);

}
