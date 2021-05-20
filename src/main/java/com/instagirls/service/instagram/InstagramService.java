package com.instagirls.service.instagram;

import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.exceptions.IGLoginException;
import com.github.instagram4j.instagram4j.exceptions.IGResponseException;
import com.github.instagram4j.instagram4j.models.media.timeline.TimelineMedia;
import com.github.instagram4j.instagram4j.requests.feed.FeedUserRequest;
import com.github.instagram4j.instagram4j.requests.users.UsersUsernameInfoRequest;
import com.github.instagram4j.instagram4j.responses.feed.FeedUserResponse;
import com.github.instagram4j.instagram4j.responses.users.UserResponse;
import com.instagirls.dto.InstagramPostDTO;
import com.instagirls.exception.LoginFailedException;
import com.instagirls.exception.SleepFailedException;
import com.instagirls.exception.UnexpectedInstagramException;
import com.instagirls.model.instagram.InstagramAccount;
import com.instagirls.model.instagram.InstagramMedia;
import com.instagirls.model.instagram.InstagramPost;
import com.instagirls.repository.InstagramAccountRepository;
import com.instagirls.repository.InstagramMediaRepository;
import com.instagirls.repository.InstagramPostRepository;
import com.instagirls.util.InstagramMediaExtractor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
public class InstagramService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstagramService.class);
    private static IGClient igClient;
    private static int loginRetryCounter = 0;

    @Autowired
    private InstagramAccountRepository instagramAccountRepository;

    @Autowired
    private InstagramPostRepository instagramPostRepository;

    @Autowired
    private InstagramMediaRepository instagramMediaRepository;

    @PostConstruct
    private void login() {
        if (igClient == null || !igClient.isLoggedIn()) {
            LOGGER.info("IG Client not logged in!");
            performLogin();
        } else {
            LOGGER.info("IG Client logged in!");
        }
    }

    @NotNull
    public InstagramPostDTO getNewMostLikedPostFromRandomAccount() {
        final InstagramAccount instagramAccount = getRandomAccount();
        final Optional<InstagramPost> instagramPost = instagramAccount.getInstagramPosts().stream()
                .sorted((ip1, ip2) -> Long.compare(ip2.getLikes(), ip1.getLikes()))
                .filter(ip -> !ip.isPosted())
                .findFirst();
        if (instagramPost.isPresent()) {
            final InstagramPost post = instagramPost.get();
            LOGGER.info("New most liked post ID: " + post.getUuid());
            return InstagramPostDTO.builder()
                    .instagramPost(post)
                    .instagramAccountURL("https://www.instagram.com/" + instagramAccount.getUsername() + "/")
                    .build();
        } else {
            disableAccount(instagramAccount);
            return getNewMostLikedPostFromRandomAccount();
        }
    }

    public boolean accountExists(final String username) {
        boolean result = false;
        try {
            final UsersUsernameInfoRequest usersUsernameInfoRequest = new UsersUsernameInfoRequest(username);
            final UserResponse userResponse = igClient.sendRequest(usersUsernameInfoRequest).get();
            result = userResponse.getUser() != null && !userResponse.getUser().is_private();
        } catch (final ExecutionException exception) {
            if (exception.getCause() instanceof IGResponseException &&
                    "User not found".equals(((IGResponseException) exception.getCause()).getResponse().getMessage())) {
                return false;
            }
        } catch (final InterruptedException e) {
            throw new UnexpectedInstagramException(e);
        }
        return result;

    }

    public void loadNewAccount(final String username) {
        LOGGER.info("Loading new account: " + username);
        InstagramAccount instagramAccount = new InstagramAccount(username);
        instagramAccount = instagramAccountRepository.save(instagramAccount);
        loadAllPostsForAccount(instagramAccount);
    }


    @SneakyThrows
    // TODO load only new
    private void loadAllPostsForAccount(final InstagramAccount instagramAccount) {
        final Set<TimelineMedia> items = new HashSet<>();
        FeedUserResponse feedUserResponse;

        final Long pk = getUserPK(instagramAccount);
        final FeedUserRequest request = new FeedUserRequest(pk);
        feedUserResponse = igClient.sendRequest(request).get();
        items.addAll(feedUserResponse.getItems());

        while (feedUserResponse.isMore_available()) {
            request.setMax_id(feedUserResponse.getNext_max_id());
            feedUserResponse = igClient.sendRequest(request).get();
            items.addAll(feedUserResponse.getItems());
            LOGGER.info("Batch: " + feedUserResponse.getItems().size());
            LOGGER.info("Overall: " + items.size());
            LOGGER.info("Loading more posts!");
        }
        LOGGER.info("Done loading posts!");

        List<InstagramPost> instagramPosts = items.stream().map(this::saveAsInstagramPostWithMedia).collect(Collectors.toList());
        instagramAccount.setInstagramPosts(instagramPosts);
        instagramAccountRepository.save(instagramAccount);
    }

    private InstagramPost saveAsInstagramPostWithMedia(final TimelineMedia timelineMedia) {

        List<InstagramMedia> instagramMedia = InstagramMediaExtractor.extractMedia(timelineMedia);
        instagramMediaRepository.saveAll(instagramMedia);

        InstagramPost instagramPost = new InstagramPost();

        instagramPost.setInstagramPostId(timelineMedia.getId());
        instagramPost.setPosted(false);
        instagramPost.setLikes(timelineMedia.getLike_count());
        instagramPost.setInstagramMedia(instagramMedia);
        instagramPostRepository.save(instagramPost);

        return instagramPost;
    }

    @SneakyThrows
    private Long getUserPK(final InstagramAccount instagramAccount) {

        if (instagramAccount.getInstagramPk() != null) {
            return instagramAccount.getInstagramPk();
        }

        return loadUserPk(instagramAccount);
    }

    private Long loadUserPk(final InstagramAccount instagramAccount) throws InterruptedException, ExecutionException {
        Long pk = igClient.actions().users()
                .findByUsername(instagramAccount.getUsername()).get().getUser().getPk();
        instagramAccount.setInstagramPk(pk);
        instagramAccountRepository.save(instagramAccount);
        return pk;
    }

    private void disableAccount(final InstagramAccount instagramAccount) {
        LOGGER.info("No posts for " + instagramAccount.getUsername());
        LOGGER.info("Switching off!");
        instagramAccount.setActive(false);
        instagramAccountRepository.save(instagramAccount);
    }

    private InstagramAccount getRandomAccount() {
        final List<InstagramAccount> allAccounts = (List<InstagramAccount>) instagramAccountRepository.findAll();
        LOGGER.info("All accounts: " + allAccounts.size());
        final Random rand = new Random();
        final InstagramAccount instagramAccount = allAccounts.get(rand.nextInt(allAccounts.size()));
        LOGGER.info("Random instagram account: " + instagramAccount.getUsername());
        return instagramAccount;
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
        sleep(1);
        performLogin();
    }

    private void loginToInstagram() throws IGLoginException {
        igClient = IGClient.builder()
                .username(System.getenv("instagram_username"))
                .password(System.getenv("instagram_password"))
                .login();
    }

    private void sleep(final int minutes) {
        try {
            Thread.sleep(TimeUnit.MINUTES.toMillis(minutes));
        } catch (final InterruptedException interruptedException) {
            throw new SleepFailedException("Login sleep failed.", interruptedException);
        }
    }

    public void setPosted(final InstagramPost instagramPost) {
        instagramPost.setPosted(true);
        instagramPostRepository.save(instagramPost);
    }
}
