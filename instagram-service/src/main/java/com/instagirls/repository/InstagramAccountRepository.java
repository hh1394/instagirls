package com.instagirls.repository;

import com.instagirls.model.InstagramAccount;
import com.instagirls.model.InstagramPost;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InstagramAccountRepository extends CrudRepository<InstagramAccount, UUID> {

    InstagramAccount findByInstagramPostsContaining(final InstagramPost instagramPost);

    InstagramAccount findByUsername(final String username);

    List<InstagramAccount> findByActiveTrue();
}
