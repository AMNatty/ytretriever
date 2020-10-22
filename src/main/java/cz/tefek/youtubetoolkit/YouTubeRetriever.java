package cz.tefek.youtubetoolkit;

import cz.tefek.youtubetoolkit.descrambler.Descrambler;
import cz.tefek.youtubetoolkit.descrambler.DescramblerHelper;
import cz.tefek.youtubetoolkit.parser.PlayerResponseParser;
import cz.tefek.youtubetoolkit.player.config.PlayerConfigRetriever;

public class YouTubeRetriever
{
    public static YouTubeVideoData retrieveVideoData(String videoID) throws Exception
    {
        System.out.println("------------------");
        System.out.println("Retrieving video: " + videoID);
        System.out.println("------------------");

        var info = PlayerConfigRetriever.get(videoID);
        var descrambleSteps = DescramblerHelper.update(info.getYouTubePlayerJSUrl());
        Descrambler descrambler = new Descrambler(descrambleSteps);
        var response = PlayerResponseParser.parse(descrambler, info.getYouTubePlayerResponse());

        return new YouTubeVideoData(videoID, response.getMetadata(), response.getAdaptiveFmts(), response.getLegacyFmts());
    }
}
