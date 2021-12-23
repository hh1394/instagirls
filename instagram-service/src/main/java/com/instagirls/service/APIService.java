package com.instagirls.service;

import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.exceptions.IGLoginException;
import com.github.instagram4j.instagram4j.requests.IGRequest;
import com.github.instagram4j.instagram4j.responses.IGResponse;
import com.instagirls.exception.LoginFailedException;
import com.instagirls.model.InstagramAccount;
import com.instagirls.util.ThreadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class APIService {

    private static final Logger LOGGER = LoggerFactory.getLogger(APIService.class);

    private static IGClient igClient;
    private static int loginRetryCounter = 0;

    private void login() {
        if (igClient == null || !igClient.isLoggedIn()) {
            LOGGER.info("IG Client not logged in!");
            performLogin();
        } else {
            LOGGER.info("IG Client logged in!");
        }
    }

    public Long loadPk(final InstagramAccount instagramAccount) {
        login();
        try {
            return igClient.actions().users()
                    .findByUsername(instagramAccount.getUsername()).get().getUser().getPk();
        } catch (InterruptedException e) {
            throw new LoginFailedException("interrupted", e);
        } catch (ExecutionException e) {
            throw new LoginFailedException("execution", e);
        }
    }

    public <T extends IGResponse> T sendRequest(final IGRequest<T> request) {
        login();
        try {
            return igClient.sendRequest(request).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            ThreadUtil.sleep(1);
            LOGGER.info("Retyring..");
            return sendRequest(request);
        }
    }

    private void performLogin() {
        LOGGER.info("Performing login.. ");
        try {
            loginToInstagram();
        } catch (final IGLoginException e) {
            if (loginRetryCounter < 3) {
                retryLogin();
            } else {
                throw new LoginFailedException("Login failed after 3 retries!", e);
            }
        }
        LOGGER.info("Login performed.");
    }

    private void retryLogin() {
        ++loginRetryCounter;
        LOGGER.info("Login failed. Retrying in 1 minute..");
        ThreadUtil.sleep(1);
        performLogin();
    }

    private void loginToInstagram() throws IGLoginException {
        igClient = IGClient.builder()
                .username(System.getenv("instagram_username"))
                .password(System.getenv("instagram_password"))
                .login();
    }


}
