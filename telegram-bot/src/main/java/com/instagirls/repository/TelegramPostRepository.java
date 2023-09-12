package com.instagirls.repository;

import com.instagirls.model.TelegramPost;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TelegramPostRepository extends CrudRepository<TelegramPost, UUID> {

    Optional<TelegramPost> findTopByOrderByCreatedAtDesc();

}
