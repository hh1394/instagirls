package com.instagirls.instagram;

import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.exceptions.IGLoginException;
import com.github.instagram4j.instagram4j.models.media.timeline.*;
import com.github.instagram4j.instagram4j.requests.feed.FeedUserRequest;
import com.github.instagram4j.instagram4j.responses.feed.FeedUserResponse;
import com.instagirls.telegram.Media;
import com.instagirls.telegram.MediaType;
import com.instagirls.telegram.TelegramPost;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.instagirls.PropertiesUtil.GIRLS_FILE_URL;
import static com.instagirls.PropertiesUtil.POSTED_FILE_URL;

public class InstagramService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstagramService.class);
    private static final Random random = new Random();
    private IGClient igClient;

    public TelegramPost generatePost() {
        login();
        final String girlUsername = getRandomUsername();
        return getNewMostLikedPostMediaUrls(girlUsername);
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
        LOGGER.info("Performing login..");
        try {
            igClient = IGClient.builder()
                    .login();
            LOGGER.info("Login performed.");
        } catch (IGLoginException e) {
            throw new RuntimeException("Could not login to com.instagirls.instagram!");
        }
    }

    private TelegramPost getNewMostLikedPostMediaUrls(final String girlUsername) {
        final Set<TimelineMedia> items = new HashSet<>();
        FeedUserResponse feedUserResponse;

        try {
            final Long pk = getUserPK(girlUsername);
            final FeedUserRequest request = new FeedUserRequest(pk);
            feedUserResponse = igClient.sendRequest(request).get();
            items.addAll(feedUserResponse.getItems());

            while (feedUserResponse.isMore_available()) {
                request.setMax_id(feedUserResponse.getNext_max_id());
                feedUserResponse = igClient.sendRequest(request).get();
                items.addAll(feedUserResponse.getItems());
                LOGGER.info("Loading more posts!");
            }


        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("FeedUserRequest problems");
        }
        final TelegramPost mostLiked = getMostLiked(items);
        mostLiked.setGirlAccount(girlUsername);
        return mostLiked;
    }

    private Long getUserPK(final String girlUsername) throws InterruptedException, ExecutionException {
        return igClient.actions().users()
                .findByUsername(girlUsername).get().getUser().getPk();
    }

    private TelegramPost getMostLiked(final Set<TimelineMedia> items) {
        final TimelineMedia timelineMedia = getTimelineMedia(items);
        return getTelegramPost(timelineMedia);
    }

    private TimelineMedia getTimelineMedia(final Set<TimelineMedia> items) {
        return items.stream()
                .filter(this::isNotPosted)
                .max(Comparator.comparingInt(TimelineMedia::getLike_count))
                .orElseThrow(RuntimeException::new);
    }

    @NotNull
    private TelegramPost getTelegramPost(final TimelineMedia timelineMedia) {
        final Set<Media> medias = new HashSet<>();
        addMedia(medias, timelineMedia);
        return buildTelegramPost(timelineMedia, medias);
    }

    @NotNull
    private TelegramPost buildTelegramPost(final TimelineMedia timelineMedia, final Set<Media> medias) {
        final TelegramPost telegramPost = new TelegramPost();
        telegramPost.setInstagramPostId(timelineMedia.getId());
        telegramPost.setInstagramPostMedias(medias);
        return telegramPost;
    }

    private void addMedia(Set<Media> medias, TimelineMedia timelineMedia) {
        if (timelineMedia instanceof TimelineImageMedia) {
            Media media = getMedia(timelineMedia, MediaType.PHOTO);
            medias.add(media);
        } else if (timelineMedia instanceof TimelineVideoMedia) {
            Media media = getMedia(timelineMedia, MediaType.VIDEO);
            medias.add(media);
        } else if (timelineMedia instanceof TimelineCarouselMedia) {
            List<Media> carouselMedias = getCarouselMedia((TimelineCarouselMedia) timelineMedia);
            medias.addAll(carouselMedias);
        }
    }

    @NotNull
    private List<Media> getCarouselMedia(final TimelineCarouselMedia timelineMedia) {
        return timelineMedia
                .getCarousel_media()
                .stream()
                .map(this::getCarouselItemMediaURL)
                .collect(Collectors.toList());
    }

    @NotNull
    private Media getMedia(TimelineMedia timelineMedia, final MediaType mediaType) {

        String url = mediaType == MediaType.PHOTO ?
                getImageMediaUrl((TimelineImageMedia) timelineMedia)
                : getVideoUrl((TimelineVideoMedia) timelineMedia);

        return new Media(url, mediaType);
    }

    private String getVideoUrl(final TimelineVideoMedia timelineMedia) {
        return timelineMedia.getImage_versions2()
                .getCandidates()
                .get(0)
                .getUrl();
    }

    private String getImageMediaUrl(final TimelineImageMedia timelineMedia) {
        return timelineMedia.getImage_versions2()
                .getCandidates()
                .get(0)
                .getUrl();
    }

    private Media getCarouselItemMediaURL(CaraouselItem caraouselItem) {
        if (caraouselItem instanceof ImageCaraouselItem) {
            return getCarouselImageMedia((ImageCaraouselItem) caraouselItem);
        } else if (caraouselItem instanceof VideoCaraouselItem) {
            return getCarouselVideoMedia((VideoCaraouselItem) caraouselItem);
        }
        throw new RuntimeException("Unknown carousel item type!");
    }

    @NotNull
    private Media getCarouselVideoMedia(final VideoCaraouselItem carouselItem) {
        return new Media(carouselItem
                .getImage_versions2()
                .getCandidates()
                .get(0)
                .getUrl(), MediaType.VIDEO);
    }

    @NotNull
    private Media getCarouselImageMedia(final ImageCaraouselItem carouselItem) {
        return new Media(carouselItem
                .getImage_versions2()
                .getCandidates()
                .get(0)
                .getUrl(), MediaType.PHOTO);
    }

    @SneakyThrows
    private boolean isNotPosted(TimelineMedia timelineMedia) {
        return Files.lines(Paths.get(System.getenv(POSTED_FILE_URL)))
                .anyMatch(s -> s.equals(timelineMedia.getId()));
    }

    @SneakyThrows
    private String getRandomUsername() {
        List<String> strings = Files.lines(
                Paths.get(System.getenv(GIRLS_FILE_URL))).collect(Collectors.toList());
        int randomIndex = random.nextInt(strings.size());
        String girlUsername = strings.get(randomIndex);
        LOGGER.info(String.format("Girls: %s", girlUsername));
        return girlUsername;
    }


}
