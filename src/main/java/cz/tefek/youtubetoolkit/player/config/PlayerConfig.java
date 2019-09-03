package cz.tefek.youtubetoolkit.player.config;

public class PlayerConfig
{
    private String oldLegacyFmts;
    private String oldAdaptiveFmts;

    private String youtubePlayerResponse;

    private String youtubePlayerJSUrl;

    public PlayerConfig(String youtubePlayerJSUrl, String oldLegacyFmts, String oldAdaptiveFmts, String youtubePlayerResponse)
    {
        this.oldLegacyFmts = oldLegacyFmts;
        this.oldAdaptiveFmts = oldAdaptiveFmts;
        this.youtubePlayerResponse = youtubePlayerResponse;
        this.youtubePlayerJSUrl = youtubePlayerJSUrl;
    }

    public String getOldAdaptiveFmts()
    {
        return this.oldAdaptiveFmts;
    }

    public String getOldLegacyFmts()
    {
        return this.oldLegacyFmts;
    }

    public String getYouTubePlayerResponse()
    {
        return this.youtubePlayerResponse;
    }

    public String getYouTubePlayerJSUrl()
    {
        return this.youtubePlayerJSUrl;
    }
}
