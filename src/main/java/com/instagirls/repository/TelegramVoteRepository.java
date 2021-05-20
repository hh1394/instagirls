package com.instagirls.repository;

import com.instagirls.model.telegram.TelegramPost;
import com.instagirls.model.telegram.TelegramVote;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TelegramVoteRepository extends CrudRepository<TelegramVote, UUID> {

    List<TelegramVote> findByTelegramPost(final TelegramPost currentPost);

    TelegramVote findByTelegramUserIdAndTelegramPost(final Integer telegramUserId, final TelegramPost telegramPost);
}
