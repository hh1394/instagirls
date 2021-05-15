package com.instagirls.util;

import com.github.instagram4j.instagram4j.models.media.timeline.*;
import com.instagirls.model.instagram.InstagramMedia;
import com.instagirls.service.telegram.entity.MediaType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InstagramMediaExtractor {

    public static List<InstagramMedia> extractMedia(final TimelineMedia timelineMedia) {
        final List<InstagramMedia> medias = new ArrayList<>();
        if (timelineMedia instanceof TimelineImageMedia) {
            final String mediaURL = getMediaURL(timelineMedia, MediaType.PHOTO);
            medias.add(new InstagramMedia(mediaURL));
        } else if (timelineMedia instanceof TimelineVideoMedia) {
            final String mediaURL = getMediaURL(timelineMedia, MediaType.VIDEO);
            medias.add(new InstagramMedia(mediaURL));
        } else if (timelineMedia instanceof TimelineCarouselMedia) {
            List<String> carouselMedias = getCarouselMedia((TimelineCarouselMedia) timelineMedia);
            medias.addAll(carouselMedias.stream().map(InstagramMedia::new).collect(Collectors.toList()));
        }
        return medias;
    }

    @NotNull
    private static String getMediaURL(TimelineMedia timelineMedia, final MediaType mediaType) {

        return mediaType == MediaType.PHOTO ?
                getImageMediaUrl((TimelineImageMedia) timelineMedia)
                : getVideoUrl((TimelineVideoMedia) timelineMedia);

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
    private static List<String> getCarouselMedia(final TimelineCarouselMedia timelineMedia) {
        return timelineMedia
                .getCarousel_media()
                .stream()
                .map(InstagramMediaExtractor::getCarouselItemMediaURL)
                .collect(Collectors.toList());
    }

    private static String getCarouselItemMediaURL(CaraouselItem caraouselItem) {
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
