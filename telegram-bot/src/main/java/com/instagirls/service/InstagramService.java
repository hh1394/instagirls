package com.instagirls.service;

import com.instagirls.dto.InstagramPostDTO;
import com.instagirls.exception.InstagramAccountDoesntExistException;
import org.springframework.stereotype.Service;

@Service
public class InstagramService {
    public InstagramPostDTO getNewMostLikedPostFromAccount(final String instagramUsername) {

    }

    public InstagramPostDTO getNewMostLikedPostFromRandomAccount() {
    }

    public void setPosted(final String postCode) {
    }

    public void loadNewAccount(final String accountUsername) throws InstagramAccountDoesntExistException {

    }

    public String disableAccount(final String instagramUsername) {

    }
}
