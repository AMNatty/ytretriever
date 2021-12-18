package cz.tefek.youtubetoolkit;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cz.tefek.youtubetoolkit.metadata.YouTubeMetadata;
import cz.tefek.youtubetoolkit.multimedia.MultimediaType;
import cz.tefek.youtubetoolkit.multimedia.YouTubeMultimedia;

public class YouTubeVideoData
{
    private final String videoID;
    private final YouTubeMetadata metadata;
    private final List<YouTubeMultimedia> adaptiveMedia;
    private final List<YouTubeMultimedia> legacyMedia;

    public YouTubeVideoData(String videoID, YouTubeMetadata youTubeMetadata, List<YouTubeMultimedia> adaptiveMedia, List<YouTubeMultimedia> legacyMedia)
    {
        this.videoID = videoID;
        this.metadata = youTubeMetadata;

        this.adaptiveMedia = adaptiveMedia;
        this.legacyMedia = legacyMedia;
    }

    public String getVideoID()
    {
        return this.videoID;
    }

    public List<YouTubeMultimedia> getAdaptiveMedia()
    {
        return Collections.unmodifiableList(this.adaptiveMedia);
    }

    public List<YouTubeMultimedia> getLegacyMedia()
    {
        return Collections.unmodifiableList(this.legacyMedia);
    }

    public YouTubeMetadata getMetadata()
    {
        return this.metadata;
    }

    public YouTubeMultimedia getBestAudio()
    {
        return this.adaptiveMedia.stream()
            .filter(media -> media.getMediaData().getType() == MultimediaType.AUDIO)
            .max(Comparator.comparing(mm -> mm.getMediaData().getBitrate()))
            .orElse(null);
    }

    public YouTubeMultimedia getBestVideo()
    {
        return this.adaptiveMedia.stream()
            .filter(media -> media.getMediaData().getType() == MultimediaType.VIDEO)
            .max(Comparator.comparing(mm -> mm.getMediaData().getBitrate()))
            .orElse(null);
    }
}
