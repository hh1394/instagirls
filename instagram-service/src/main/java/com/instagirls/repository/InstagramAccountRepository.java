package com.instagirls.repository;

import com.instagirls.model.InstagramAccount;
import com.instagirls.model.InstagramPost;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InstagramAccountRepository extends CrudRepository<InstagramAccount, UUID> {

    InstagramAccount findByInstagramPostsContaining(final InstagramPost instagramPost);

    InstagramAccount findByUsername(final String username);

    List<InstagramAccount> findByActiveTrue();

    @Modifying
    @Query("update InstagramAccount acc set acc.active = false where acc.username = :username")
    void setActiveFalse(@Param("username") String username);
}
