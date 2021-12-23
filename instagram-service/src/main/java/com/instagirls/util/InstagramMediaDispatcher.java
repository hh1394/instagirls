package com.instagirls.util;

import com.github.instagram4j.instagram4j.models.media.timeline.*;
import com.instagirls.service.MediaType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InstagramMediaDispatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstagramMediaDispatcher.class);

    public static List<String> getMediaURLs(final TimelineMedia timelineMedia) {
        final List<String> medias = new ArrayList<>();
        if (timelineMedia instanceof TimelineImageMedia) {
            final String mediaURL = getMediaURLs(timelineMedia, MediaType.PHOTO);
            medias.add(mediaURL);
        } else if (timelineMedia instanceof TimelineVideoMedia) {
            final String mediaURL = getMediaURLs(timelineMedia, MediaType.VIDEO);
            medias.add(mediaURL);
        } else if (timelineMedia instanceof TimelineCarouselMedia) {
            List<String> carouselMedias = getDownloadedCarouselMediaURLs((TimelineCarouselMedia) timelineMedia);
            medias.addAll(carouselMedias);
        }
        LOGGER.info("Done loading " + medias.size() + " medias.");
        return medias;
    }

    @NotNull
    private static String getMediaURLs(TimelineMedia timelineMedia, final MediaType mediaType) {
        return mediaType == MediaType.PHOTO ?
                getImageMediaUrl((TimelineImageMedia) timelineMedia)
                : getVideoUrl((TimelineVideoMedia) timelineMedia);
    }

    private static String getVideoUrl(final TimelineVideoMedia timelineMedia) {
        return timelineMedia.getVideo_versions()
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
            return getCarouselImageMedia((ImageCaraouselItem) caraouselItem);
        } else if (caraouselItem instanceof VideoCaraouselItem) {
            return getCarouselVideoMedia((VideoCaraouselItem) caraouselItem);
        }
        throw new RuntimeException("Unknown carousel item type!");
    }

    @NotNull
    private static String getCarouselVideoMedia(final VideoCaraouselItem carouselItem) {
        return carouselItem
                .getVideo_versions()
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
