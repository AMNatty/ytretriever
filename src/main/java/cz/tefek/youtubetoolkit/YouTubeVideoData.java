package cz.tefek.youtubetoolkit;

import java.util.Comparator;
import java.util.List;

import cz.tefek.youtubetoolkit.metadata.YouTubeMetadata;
import cz.tefek.youtubetoolkit.multimedia.MultimediaType;
import cz.tefek.youtubetoolkit.multimedia.YouTubeMultimedia;

public class YouTubeVideoData
{
    private YouTubeMetadata metadata;
    private List<YouTubeMultimedia> adaptiveMedia;
    private List<YouTubeMultimedia> legacyMedia;

    public YouTubeVideoData(YouTubeMetadata youTubeMetadata, List<YouTubeMultimedia> adaptiveMedia, List<YouTubeMultimedia> legacyMedia, List<YouTubeMultimedia> oldAdaptiveMedia, List<YouTubeMultimedia> oldLegacyMedia)
    {
        this.metadata = youTubeMetadata;

        if (adaptiveMedia == null || adaptiveMedia.isEmpty())
        {
            this.adaptiveMedia = oldAdaptiveMedia;
        }
        else
        {
            this.adaptiveMedia = adaptiveMedia;
        }

        if (legacyMedia == null || legacyMedia.isEmpty())
        {
            this.legacyMedia = oldLegacyMedia;
        }
        else
        {
            this.legacyMedia = legacyMedia;
        }
    }

    public List<YouTubeMultimedia> getAdaptiveMedia()
    {
        return this.adaptiveMedia;
    }

    public List<YouTubeMultimedia> getLegacyMedia()
    {
        return this.legacyMedia;
    }

    public YouTubeMetadata getMetadata()
    {
        return this.metadata;
    }

    public YouTubeMultimedia getBestAudio()
    {
        return this.adaptiveMedia.stream().filter(media -> media.getMediaData().getType() == MultimediaType.AUDIO).max(Comparator.comparing(mm -> mm.getMediaData().getBitrate())).orElse(null);
    }

    public YouTubeMultimedia getBestVideo()
    {
        return this.adaptiveMedia.stream().filter(media -> media.getMediaData().getType() == MultimediaType.VIDEO).max(Comparator.comparing(mm -> mm.getMediaData().getBitrate())).orElse(null);
    }
}
