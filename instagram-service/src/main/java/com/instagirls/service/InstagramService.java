package com.instagirls.service;

import com.github.instagram4j.instagram4j.models.media.timeline.TimelineMedia;
import com.github.instagram4j.instagram4j.requests.feed.FeedUserRequest;
import com.github.instagram4j.instagram4j.requests.users.UsersUsernameInfoRequest;
import com.github.instagram4j.instagram4j.responses.feed.FeedUserResponse;
import com.github.instagram4j.instagram4j.responses.users.UserResponse;
import com.instagirls.dto.InstagramPostDTO;
import com.instagirls.exception.InstagramAccountExistsException;
import com.instagirls.exception.InstagramAccountNotFoundException;
import com.instagirls.exception.NoActiveAccountException;
import com.instagirls.exception.UncheckedInterruptedException;
import com.instagirls.model.InstagramAccount;
import com.instagirls.model.InstagramPost;
import com.instagirls.repository.InstagramAccountRepository;
import com.instagirls.repository.InstagramPostRepository;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
public class InstagramService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstagramService.class);
    private final static Random rand = new Random();

    @Autowired
    private APIService apiService;

    @Autowired
    private InstagramAccountRepository instagramAccountRepository;

    @Autowired
    private InstagramPostRepository instagramPostRepository;


    public InstagramPostDTO getNewMostLikedPostFromAccount(final String username) {
        LOGGER.info("Getting new most liked post for account: " + username);
        final InstagramAccount instagramAccount = instagramAccountRepository.findByUsername(username);
        return buildInstagramPostDTO(instagramAccount);
    }

    public InstagramPostDTO getNewMostLikedPostFromRandomAccountExcept(String instagramAccountName) {
        final InstagramAccount instagramAccount = getRandomAccountExcept(instagramAccountName);
        return buildInstagramPostDTO(instagramAccount);
    }

    @NotNull
    public InstagramPostDTO getNewMostLikedPostFromRandomAccount() {
        final InstagramAccount instagramAccount = getRandomAccount();
        return buildInstagramPostDTO(instagramAccount);
    }

    public void updateGirls() {
        instagramAccountRepository.findAll().forEach(this::loadPostsForAccount);
    }

    private InstagramPostDTO buildInstagramPostDTO(final InstagramAccount instagramAccount) {
        final Optional<InstagramPost> instagramPost = instagramAccount.getInstagramPosts().stream()
                .sorted((ip1, ip2) -> Long.compare(ip2.getLikes(), ip1.getLikes()))
                .filter(ip -> !ip.isPosted())
                .findFirst();
        if (instagramPost.isPresent()) {
            final InstagramPost post = instagramPost.get();
            LOGGER.info("New most liked post ID: " + post.getUuid());
            List<String> mediaURLs = apiService.getMediaURLsByPostId(post.getInstagramPostId());
            return InstagramPostDTO.builder()
                    .account(instagramAccount.getUsername())
                    .postCode(post.getInstagramPostCode())
                    .mediaURLs(mediaURLs)
                    .build();
        } else {
            LOGGER.info("No posts for " + instagramAccount.getUsername());
            disableAccount(instagramAccount.getUsername());
            return getNewMostLikedPostFromRandomAccount();
        }
    }

    public void loadNewAccount(final String username) {
        LOGGER.info("Loading new account: " + username);
        if (!accountExists(username)) {
            throw new InstagramAccountNotFoundException(username);
        }
        InstagramAccount instagramAccount = new InstagramAccount(username);
        try {
            instagramAccount = instagramAccountRepository.save(instagramAccount);
        } catch (DataIntegrityViolationException exception) {
            LOGGER.info(String.format("Account %s already exists!", username));
            throw new InstagramAccountExistsException(username);
        }
        loadPostsForAccount(instagramAccount);
        LOGGER.info(String.format("Account %s loaded!", username));
    }

    private boolean accountExists(final String username) {
        final UsersUsernameInfoRequest usersUsernameInfoRequest = new UsersUsernameInfoRequest(username);
        final UserResponse userResponse = apiService.sendRequest(usersUsernameInfoRequest);
        return userResponse.getUser() != null && !userResponse.getUser().is_private();
    }

    private void loadPostsForAccount(final InstagramAccount instagramAccount) {
        Optional<InstagramPost> firstOrderByTakenAtDesc = instagramPostRepository.findTopByInstagramAccountOrderByTakenAtDesc(instagramAccount.getUuid().toString());
        if (firstOrderByTakenAtDesc.isPresent()) {
            LOGGER.info("Loading only new posts!");
            loadPostsNewerThan(firstOrderByTakenAtDesc.get().getTakenAt(), instagramAccount);
        } else {
            LOGGER.info("Loading all posts!");
            loadAllPostsForAccount(instagramAccount);
        }
    }

    // TODO refactor
    private void loadPostsNewerThan(final Long takenAt, final InstagramAccount instagramAccount) {
        final Long pk = getUserPK(instagramAccount);
        final FeedUserRequest request = new FeedUserRequest(pk);
        FeedUserResponse feedUserResponse = apiService.sendRequest(request);
        int batchSize = feedUserResponse.getNum_results();
        final Set<TimelineMedia> items = feedUserResponse.getItems()
                .stream()
                .filter(i -> i.getTaken_at() > takenAt)
                .collect(Collectors.toSet());

        if (batchSize > items.size()) {
            LOGGER.info("Found border.");
        } else {
            boolean borderReached = false;
            while (feedUserResponse.isMore_available() && !borderReached) {
                request.setMax_id(feedUserResponse.getNext_max_id());
                feedUserResponse = apiService.sendRequest(request);

                batchSize = feedUserResponse.getNum_results();
                final Set<TimelineMedia> responseItems = feedUserResponse.getItems()
                        .stream()
                        .filter(i -> i.getTaken_at() > takenAt)
                        .collect(Collectors.toSet());

                if (batchSize > responseItems.size()) {
                    LOGGER.info("Found border.");
                    borderReached = true;
                }

                items.addAll(responseItems);
                LOGGER.info("Batch: " + feedUserResponse.getItems().size());
                LOGGER.info("Overall: " + items.size());
                LOGGER.info("Loading more posts!");
            }
        }
        LOGGER.info("Done loading posts!");

        List<InstagramPost> instagramPosts = items.parallelStream().map(i -> saveAsInstagramPost(i, instagramAccount)).collect(Collectors.toList());
        instagramAccount.setInstagramPosts(instagramPosts);
        instagramAccountRepository.save(instagramAccount);
        LOGGER.info("Done saving posts!");

    }

    //TODO refactor
    private void loadAllPostsForAccount(final InstagramAccount instagramAccount) {
        FeedUserResponse feedUserResponse;

        final Long pk = getUserPK(instagramAccount);
        final FeedUserRequest request = new FeedUserRequest(pk);
        feedUserResponse = apiService.sendRequest(request);

        List<InstagramPost> posts = feedUserResponse.getItems().parallelStream()
                .map(i -> saveAsInstagramPost(i, instagramAccount))
                .collect(Collectors.toList());
        instagramAccount.addInstagramPosts(posts);

        ExecutorService es = Executors.newCachedThreadPool();
        while (feedUserResponse.isMore_available()) {
            request.setMax_id(feedUserResponse.getNext_max_id());
            feedUserResponse = apiService.sendRequest(request);

            final List<TimelineMedia> itemsChunk = feedUserResponse.getItems();
            Runnable task = () -> {
                LOGGER.debug("Started new thread");
                List<InstagramPost> instagramPosts = itemsChunk.parallelStream()
                        .map(i -> saveAsInstagramPost(i, instagramAccount))
                        .collect(Collectors.toList());
                instagramAccount.addInstagramPosts(instagramPosts);
            };

            es.execute(task);


            LOGGER.info("Batch: " + feedUserResponse.getItems().size());
            LOGGER.info("Loading more posts!");
        }

        es.shutdown();
        try {
            boolean finished = es.awaitTermination(5, TimeUnit.HOURS);
            if (finished) {
                instagramAccountRepository.save(instagramAccount);
                LOGGER.info("Done loading posts!");
            } else {
                instagramAccountRepository.delete(instagramAccount);
                LOGGER.info("Error while loading posts!");
                LOGGER.info("Deleting account! " + instagramAccount);
            }
        } catch (InterruptedException e) {
            throw new UncheckedInterruptedException(e);
        }
    }

    private InstagramPost saveAsInstagramPost(final TimelineMedia timelineMedia, final InstagramAccount instagramAccount) {

        final InstagramPost instagramPost = new InstagramPost();

        instagramPost.setInstagramPostId(timelineMedia.getId());
        instagramPost.setInstagramPostCode(timelineMedia.getCode());
        instagramPost.setLikes(timelineMedia.getLike_count());
        instagramPost.setTakenAt(timelineMedia.getTaken_at());
        instagramPost.setInstagramAccount(instagramAccount);
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
        Long pk = apiService.loadPk(instagramAccount);
        instagramAccount.setInstagramPk(pk);
        instagramAccountRepository.save(instagramAccount);
        return pk;
    }

    @Transactional
    public void disableAccount(final String username) {
        LOGGER.info(String.format("Switching account %s off!", username));
        instagramAccountRepository.setActiveFalse(username);
    }

    private InstagramAccount getRandomAccount() {
        final List<InstagramAccount> allActiveAccounts = instagramAccountRepository.findByActiveTrue();
        LOGGER.info("All accounts: " + allActiveAccounts.size());
        if(allActiveAccounts.isEmpty()){
            throw new NoActiveAccountException();
        }
        final InstagramAccount instagramAccount = allActiveAccounts.get(rand.nextInt(allActiveAccounts.size()));
        LOGGER.info("Random instagram account: " + instagramAccount.getUsername());
        return instagramAccount;
    }

    private InstagramAccount getRandomAccountExcept(String instagramAccountName) {
        final List<InstagramAccount> allActiveAccounts = instagramAccountRepository.findByActiveTrue();
        LOGGER.info("All accounts: " + allActiveAccounts.size());
        if(allActiveAccounts.isEmpty()){
            throw new NoActiveAccountException();
        }
        final InstagramAccount instagramAccount = allActiveAccounts.stream()
                .filter(a -> !a.getUsername().equals(instagramAccountName))
                .toList()
                .get(rand.nextInt(allActiveAccounts.size()));
        LOGGER.info("Random instagram account: " + instagramAccount.getUsername());
        return instagramAccount;
    }

    @Transactional
    public void setPosted(final String postCode) {
        instagramPostRepository.setPosted(postCode);
        LOGGER.info(String.format("Post %s is set to POSTED!", postCode));
    }

    public void deleteAccount(final String username) {
        LOGGER.info("Deleting " + username + "...");
        final InstagramAccount account = instagramAccountRepository.findByUsername(username);
        instagramAccountRepository.delete(account);
        LOGGER.info("Deleted " + username + "!");
    }
}
