package com.instagirls.repository;

import com.instagirls.model.telegram.TelegramPost;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TelegramPostRepository extends CrudRepository<TelegramPost, UUID> {

    TelegramPost findTopByOrderByCreatedAtDesc();

}
