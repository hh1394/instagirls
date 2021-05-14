package com.instagirls.repository;

import com.instagirls.model.instagram.InstagramAccount;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstagramAccountRepository extends CrudRepository<InstagramAccount, String> {
}
