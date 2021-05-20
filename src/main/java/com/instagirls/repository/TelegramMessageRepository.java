package com.instagirls.repository;

import com.instagirls.model.telegram.TelegramMessage;
import com.instagirls.model.telegram.TelegramUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TelegramMessageRepository extends CrudRepository<TelegramMessage, UUID> {

    TelegramMessage findTopTelegramMessageByTelegramUserOrderByIdDesc(final TelegramUser telegramUser);

}
