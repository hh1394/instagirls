package com.instagirls.repository;

import com.instagirls.model.instagram.InstagramPost;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InstagramPostRepository extends CrudRepository<InstagramPost, UUID> {

}
