package com.instagirls.repository;

import com.instagirls.model.telegram.TelegramChat;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TelegramChatRepository extends CrudRepository<Long, TelegramChat> {
}
