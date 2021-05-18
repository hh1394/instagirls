package com.instagirls.repository;

import com.instagirls.model.telegram.TelegramMessage;
import com.instagirls.model.telegram.TelegramUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TelegramMessageRepository extends CrudRepository<TelegramMessage, Long> {

    TelegramMessage findTopTelegramMessageByTelegramUserOrderByIdDesc(final TelegramUser telegramUser);

}
