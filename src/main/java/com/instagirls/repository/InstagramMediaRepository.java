package com.instagirls.repository;

import com.instagirls.model.instagram.InstagramAccount;
import com.instagirls.model.instagram.InstagramMedia;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstagramMediaRepository extends CrudRepository<InstagramMedia, String> {
}
