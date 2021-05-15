package com.instagirls.repository;

import com.instagirls.model.telegram.TelegramPost;
import com.instagirls.model.telegram.TelegramVote;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TelegramVoteRepository extends CrudRepository<TelegramVote, Long> {
    TelegramVote findByTelegramUserIdAndTelegramPost(final Integer id, final TelegramPost byCurrentTrue);

    List<TelegramVote> findByTelegramPost(final TelegramPost currentPost);
}
