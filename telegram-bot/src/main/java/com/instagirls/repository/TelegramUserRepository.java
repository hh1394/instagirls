package com.instagirls.repository;

import com.instagirls.model.TelegramUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TelegramUserRepository extends CrudRepository<TelegramUser, UUID> {

    TelegramUser findByTelegramId(final Integer telegramId);

}
