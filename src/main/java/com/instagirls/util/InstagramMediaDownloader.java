package com.instagirls.util;

import com.github.instagram4j.instagram4j.models.media.timeline.*;
import com.instagirls.model.instagram.InstagramMedia;
import com.instagirls.service.telegram.entity.MediaType;
import net.bytebuddy.utility.RandomString;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InstagramMediaDownloader {

    private static final int CONNECT_TIMEOUT = 100_000;
    private static final int READ_TIMEOUT = 100_000;
    private static final Logger LOGGER = LoggerFactory.getLogger(InstagramMediaDownloader.class);

    public static List<InstagramMedia> downloadMedia(final TimelineMedia timelineMedia) {
        final List<InstagramMedia> medias = new ArrayList<>();
        if (timelineMedia instanceof TimelineImageMedia) {
            final String mediaURL = getDownloadedMediaURL(timelineMedia, MediaType.PHOTO);
            medias.add(new InstagramMedia(mediaURL));
        } else if (timelineMedia instanceof TimelineVideoMedia) {
            final String mediaURL = getDownloadedMediaURL(timelineMedia, MediaType.VIDEO);
            medias.add(new InstagramMedia(mediaURL));
        } else if (timelineMedia instanceof TimelineCarouselMedia) {
            List<String> carouselMedias = getDownloadedCarouselMediaURLs((TimelineCarouselMedia) timelineMedia);
            medias.addAll(carouselMedias.stream().map(InstagramMedia::new).collect(Collectors.toList()));
        }
        LOGGER.info("Done loading " + medias.size() + " medias.");
        return medias;
    }

    @NotNull
    private static String getDownloadedMediaURL(TimelineMedia timelineMedia, final MediaType mediaType) {

        final String webURL = mediaType == MediaType.PHOTO ?
                getImageMediaUrl((TimelineImageMedia) timelineMedia)
                : getVideoUrl((TimelineVideoMedia) timelineMedia);
        return downloadMedia(timelineMedia.getUser().getUsername(), timelineMedia.getCode(), webURL, mediaType);
    }

    private static String downloadMedia(final String username, final String postCode, final String url, final MediaType mediaType) {
        String pathname = "images" + File.separator +
                username + File.separator +
                postCode + File.separator +
                RandomString.make(6) + (mediaType == MediaType.PHOTO ? ".jpg" : ".mp4");
        try {
            FileUtils.copyURLToFile(
                    new URL(url),
                    new File(pathname),
                    CONNECT_TIMEOUT,
                    READ_TIMEOUT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return pathname;
    }

    private static String getVideoUrl(final TimelineVideoMedia timelineMedia) {
        return timelineMedia.getImage_versions2()
                .getCandidates()
                .get(0)
                .getUrl();
    }

    private static String getImageMediaUrl(final TimelineImageMedia timelineMedia) {
        return timelineMedia.getImage_versions2()
                .getCandidates()
                .get(0)
                .getUrl();
    }

    @NotNull
    private static List<String> getDownloadedCarouselMediaURLs(final TimelineCarouselMedia timelineMedia) {
        return timelineMedia
                .getCarousel_media()
                .stream()
                .map(m -> getCarouselItemMediaURL(m, timelineMedia))
                .collect(Collectors.toList());
    }

    private static String getCarouselItemMediaURL(final CaraouselItem caraouselItem, final TimelineCarouselMedia timelineMedia) {
        if (caraouselItem instanceof ImageCaraouselItem) {
            String webURL = getCarouselImageMedia((ImageCaraouselItem) caraouselItem);
            return downloadMedia(timelineMedia.getUser().getUsername(), timelineMedia.getCode(), webURL, MediaType.PHOTO);
        } else if (caraouselItem instanceof VideoCaraouselItem) {
            String webURL = getCarouselVideoMedia((VideoCaraouselItem) caraouselItem);
            return downloadMedia(timelineMedia.getUser().getUsername(), timelineMedia.getCode(), webURL, MediaType.VIDEO);
        }
        throw new RuntimeException("Unknown carousel item type!");
    }

    @NotNull
    private static String getCarouselVideoMedia(final VideoCaraouselItem carouselItem) {
        return carouselItem
                .getImage_versions2()
                .getCandidates()
                .get(0)
                .getUrl();
    }

    @NotNull
    private static String getCarouselImageMedia(final ImageCaraouselItem carouselItem) {
        return carouselItem
                .getImage_versions2()
                .getCandidates()
                .get(0)
                .getUrl();
    }

}
