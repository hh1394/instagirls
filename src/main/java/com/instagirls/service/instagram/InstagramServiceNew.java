package com.instagirls.service.instagram;

import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.exceptions.IGLoginException;
import com.github.instagram4j.instagram4j.models.media.timeline.TimelineMedia;
import com.github.instagram4j.instagram4j.requests.feed.FeedUserRequest;
import com.github.instagram4j.instagram4j.responses.feed.FeedUserResponse;
import com.instagirls.exception.LoginFailedException;
import com.instagirls.exception.SleepFailedException;
import com.instagirls.model.instagram.InstagramAccount;
import com.instagirls.model.instagram.InstagramMedia;
import com.instagirls.model.instagram.InstagramPost;
import com.instagirls.repository.InstagramAccountRepository;
import com.instagirls.repository.InstagramMediaRepository;
import com.instagirls.repository.InstagramPostRepository;
import com.instagirls.util.InstagramMediaExtractor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.instagirls.model.instagram.InstagramPost.DEFAULT_CAPTION;

@Service
public class InstagramServiceNew {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstagramServiceNew.class);
    private static final Random random = new Random();
    private static IGClient igClient;
    private static int loginRetryCounter = 0;


    @Autowired
    private InstagramAccountRepository instagramAccountRepository;

    @Autowired
    private InstagramPostRepository instagramPostRepository;

    @Autowired
    private InstagramMediaRepository instagramMediaRepository;

    public void test() {
        long count = instagramAccountRepository.findById(1L).get().getInstagramPosts().stream().mapToLong(ip -> ip.getInstagramMedia().size()).sum();
        LOGGER.info("COUNT: " + count);
    }

    @PostConstruct
    public void loadPostsForAccounts() {
        loadAllAccounts();
        login();
        LOGGER.info("Loading all posts..");
        final List<InstagramAccount> allAccounts = (List<InstagramAccount>) instagramAccountRepository.findAll();
        LOGGER.info("Accounts: " + allAccounts.size());
        allAccounts.forEach(this::loadPostsForAccount);
    }

    private void loadAllAccounts() {
        LOGGER.info("Loading all accounts..");

        List<String> accounts = new ArrayList<>();
        accounts.add("lanarhoades");
        accounts.add("natalee.007");
        accounts.add("oabramovich");
        accounts.add("alexisren");
        accounts.add("Gabbywestbrook");
        accounts.add("mathildtantot");
        accounts.add("cayleecowan");
        accounts.add("sophiemudd");
        accounts.add("demirose");
//        accounts.add("daisykeech");
//        accounts.add("rachelc00k");
//        accounts.add("magui_ansuz");
//        accounts.add("you___fit");
//        accounts.add("elena.berlato");
//        accounts.add("sophie_xdt");
//        accounts.add("daria.plane");
//        accounts.add("ale.valerya");

        accounts.stream()
                .map(InstagramAccount::new)
                .forEach(instagramAccountRepository::save);
        LOGGER.info("Done loading all accounts!");

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
        instagramPost.setCaption(timelineMedia.getCaption() != null ? timelineMedia.getCaption().getText() : DEFAULT_CAPTION);
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

}
