package cz.tefek.youtubetoolkit.player.config;

import org.json.JSONObject;

public record PlayerConfig(String youtubePlayerJSUrl,
                           JSONObject youtubePlayerConfig,
                           JSONObject youtubeVideoDetails,
                           JSONObject youtubeStreamingData)
{

}
