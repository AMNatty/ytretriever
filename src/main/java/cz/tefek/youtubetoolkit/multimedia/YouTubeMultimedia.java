package cz.tefek.youtubetoolkit.multimedia;

public class YouTubeMultimedia
{
    private final String url;
    private final Multimedia media;

    public YouTubeMultimedia(String url, Multimedia mediaData)
    {
        this.url = url;
        this.media = mediaData;
    }

    public String getUrl()
    {
        return this.url;
    }

    public Multimedia getMediaData()
    {
        return this.media;
    }
}
