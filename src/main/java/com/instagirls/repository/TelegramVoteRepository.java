package com.instagirls.repository;

import com.instagirls.model.telegram.TelegramPost;
import com.instagirls.model.telegram.TelegramVote;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TelegramVoteRepository extends CrudRepository<TelegramVote, Long> {

    List<TelegramVote> findByTelegramPost(final TelegramPost currentPost);

    List<TelegramVote> findByTelegramUserIdAndTelegramPost(final Integer userId, final TelegramPost topByOrderById);
}
