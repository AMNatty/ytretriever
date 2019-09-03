package cz.tefek.youtubetoolkit.parser.fmt;

import java.util.ArrayList;
import java.util.List;

import cz.tefek.youtubetoolkit.descrambler.Descrambler;
import cz.tefek.youtubetoolkit.descrambler.DescramblerHelper;
import cz.tefek.youtubetoolkit.multimedia.Multimedia;
import cz.tefek.youtubetoolkit.multimedia.YouTubeMultimedia;

public class OldLegacyFMTParser
{
    public static List<YouTubeMultimedia> getMedia(Descrambler descrambler, String data)
    {
        var media = new ArrayList<YouTubeMultimedia>();

        if (data == null)
        {
            return media;
        }

        for (String fmt : data.split(","))
        {
            String url = null;
            String signature = null;
            String quality = null;
            String mediaType = null;
            String codecs = null;
            int itag = Integer.MIN_VALUE;

            for (String vidParam : fmt.split("&"))
            {
                String parameter = DescramblerHelper.urlDecode(vidParam);

                if (parameter.startsWith("url="))
                {
                    url = parameter.substring("url=".length());
                }

                if (parameter.startsWith("s="))
                {
                    signature = descrambler.descramble(parameter.substring("s=".length()));
                }

                if (parameter.startsWith("quality="))
                {
                    quality = parameter.substring("quality=".length()).trim();
                }

                if (parameter.startsWith("itag="))
                {
                    itag = Integer.parseInt(parameter.substring("itag=".length()).trim());
                }

                if (parameter.startsWith("type="))
                {
                    mediaType = parameter.split(";")[0].trim().substring("type=".length());

                    codecs = parameter.split(";")[1].trim().split("\"")[1].trim();
                }
            }

            if (signature != null)
            {
                url = url + "&sig=" + signature;
            }

            Multimedia mediaData = new Multimedia(false, quality, Multimedia.UNKNOWN_BITRATE, Multimedia.UNDEFINED_SIZE, Multimedia.UNDEFINED_SIZE, itag, Multimedia.UNKNOWN_FPS, mediaType, codecs);
            YouTubeMultimedia multimedia = new YouTubeMultimedia(url, mediaData);

            media.add(multimedia);
        }

        return media;
    }
}
