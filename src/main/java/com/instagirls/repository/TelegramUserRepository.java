package com.instagirls.repository;

import com.instagirls.model.telegram.TelegramUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TelegramUserRepository extends CrudRepository<TelegramUser, Long> {

    TelegramUser findByTelegramId(final Integer telegramId);

}
