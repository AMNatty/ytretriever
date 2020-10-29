package cz.tefek.youtubetoolkit.player.config;

import org.json.JSONObject;

public class PlayerConfig
{
    private final String youtubePlayerJSUrl;

    private final JSONObject youtubePlayerConfig;
    private final JSONObject youtubeVideoDetails;
    private final JSONObject youtubeStreamingData;

    public PlayerConfig(String youtubePlayerJSUrl, JSONObject playerConfig, JSONObject videoDetails, JSONObject streamingData)
    {
        this.youtubePlayerJSUrl = youtubePlayerJSUrl;

        this.youtubePlayerConfig = playerConfig;
        this.youtubeVideoDetails = videoDetails;
        this.youtubeStreamingData = streamingData;
    }

    public String getYouTubePlayerJSUrl()
    {
        return this.youtubePlayerJSUrl;
    }

    public JSONObject getYoutubePlayerConfig()
    {
        return this.youtubePlayerConfig;
    }

    public JSONObject getYoutubeStreamingData()
    {
        return this.youtubeStreamingData;
    }

    public JSONObject getYoutubeVideoDetails()
    {
        return this.youtubeVideoDetails;
    }
}
