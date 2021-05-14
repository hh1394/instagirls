package com.instagirls.service.instagram;

import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.models.media.timeline.TimelineMedia;
import com.github.instagram4j.instagram4j.requests.feed.FeedUserRequest;
import com.github.instagram4j.instagram4j.responses.feed.FeedUserResponse;
import com.instagirls.model.instagram.InstagramAccount;
import com.instagirls.model.instagram.InstagramPost;
import com.instagirls.repository.InstagramAccountRepository;
import com.instagirls.repository.InstagramPostRepository;
import com.instagirls.service.telegram.entity.TelegramPost;
import com.instagirls.util.InstagramMediaExtractor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class InstagramServiceNew {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstagramServiceNew.class);
    private static final Random random = new Random();
    private static IGClient igClient;

    @Value("${instagram.username}")
    private String instagramUsername;

    @Value("${instagram.password}")
    private String instagramPassword;

    @Autowired
    private InstagramAccountRepository instagramAccountRepository;

    @Autowired
    private InstagramPostRepository instagramPostRepository;

    @PostConstruct
    public void loadPostsForAccounts() {
        login();
        final List<InstagramAccount> allAccounts = (List<InstagramAccount>) instagramAccountRepository.findAll();
        allAccounts.forEach(this::loadPostsForAccount);
    }

    @SneakyThrows
    // TODO load only new
    private void loadPostsForAccount(final InstagramAccount instagramAccount) {
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

        List<InstagramPost> instagramPosts = items.stream().map(i -> mapToInstagramPost(i, instagramAccount)).collect(Collectors.toList());
        instagramPostRepository.saveAll(instagramPosts);
    }

    private InstagramPost mapToInstagramPost(final TimelineMedia timelineMedia, final InstagramAccount instagramAccount) {
        InstagramPost instagramPost = new InstagramPost();
        instagramPost.setInstagramMedia(InstagramMediaExtractor.extractMedia(timelineMedia));
        instagramPost.setInstagramPostId(timelineMedia.getId());
        instagramPost.setInstagramAccount(instagramAccount);
        instagramPost.setPosted(false);
        instagramPost.setCaption(timelineMedia.getCaption().getText());
        instagramPost.setLikes(timelineMedia.getLike_count());
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

//    public TelegramPost generatePost() {
//        login();
//        final InstagramAccount girlUsername = getRandomAccount();
//        return getNewMostLikedPostMediaUrls(girlUsername);
//    }

    private InstagramAccount getRandomAccount() {
        final List<InstagramAccount> allAccounts = (List<InstagramAccount>) instagramAccountRepository.findAll();
        return allAccounts.stream().findAny()
                .orElseThrow(() -> new IllegalStateException("No instagram accounts in DB!"));
//        return instagramPostRepository.findTopByInstagramAccountAndPostedFalseOrderByLikesDesc(instagramAccount);
    }

    private void login() {
        if (igClient == null || !igClient.isLoggedIn()) {
            LOGGER.info("IG Client not logged in!");
            performLogin();
        } else {
            LOGGER.info("IG Client logged in!");
        }
    }

    @SneakyThrows
    private void performLogin() {
        LOGGER.info("Performing login.. " + instagramUsername);
        igClient = IGClient.builder()
                .username(instagramUsername)
                .password(instagramPassword)
                .login();
        LOGGER.info("Login performed.");
    }

}
