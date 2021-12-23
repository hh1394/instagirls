package com.instagirls.repository;

import com.instagirls.model.telegram.TelegramPost;
import com.instagirls.model.telegram.TelegramVote;
import com.instagirls.model.telegram.TelegramVoteType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TelegramVoteRepository extends CrudRepository<TelegramVote, UUID> {

    List<TelegramVote> findByTelegramPostAndTelegramVoteType(final TelegramPost currentPost, final TelegramVoteType telegramVoteType);

    TelegramVote findByTelegramUserIdAndTelegramPostAndTelegramVoteType(final Integer telegramUserId, final TelegramPost telegramPost, final TelegramVoteType telegramVoteType);
}
