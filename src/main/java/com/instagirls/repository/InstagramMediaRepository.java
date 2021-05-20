package com.instagirls.repository;

import com.instagirls.model.instagram.InstagramMedia;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InstagramMediaRepository extends CrudRepository<InstagramMedia, UUID> {
}
