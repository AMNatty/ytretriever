package cz.tefek.youtubetoolkit.player.response;

import java.util.List;

import cz.tefek.youtubetoolkit.metadata.YouTubeMetadata;
import cz.tefek.youtubetoolkit.multimedia.YouTubeMultimedia;

public class PlayerResponse
{
    private YouTubeMetadata metadata;
    private List<YouTubeMultimedia> legacyFmts;
    private List<YouTubeMultimedia> adaptiveFmts;

    public PlayerResponse(YouTubeMetadata metadata, List<YouTubeMultimedia> legacyFmts, List<YouTubeMultimedia> adaptiveFmts)
    {
        this.metadata = metadata;
        this.legacyFmts = legacyFmts;
        this.adaptiveFmts = adaptiveFmts;
    }

    public List<YouTubeMultimedia> getAdaptiveFmts()
    {
        return this.adaptiveFmts;
    }

    public List<YouTubeMultimedia> getLegacyFmts()
    {
        return this.legacyFmts;
    }

    public YouTubeMetadata getMetadata()
    {
        return this.metadata;
    }
}
