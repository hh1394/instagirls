package com.instagirls.repository;

import com.instagirls.model.instagram.InstagramAccount;
import com.instagirls.model.instagram.InstagramPost;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstagramPostRepository extends CrudRepository<InstagramPost, String> {

    InstagramPost findTopByInstagramAccountAndPostedFalseOrderByLikesDesc(final InstagramAccount instagramAccount);

}
