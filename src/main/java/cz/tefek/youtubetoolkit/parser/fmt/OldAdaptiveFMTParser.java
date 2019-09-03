package cz.tefek.youtubetoolkit.parser.fmt;

import java.util.ArrayList;
import java.util.List;

import cz.tefek.youtubetoolkit.descrambler.Descrambler;
import cz.tefek.youtubetoolkit.descrambler.DescramblerHelper;
import cz.tefek.youtubetoolkit.multimedia.Multimedia;
import cz.tefek.youtubetoolkit.multimedia.YouTubeMultimedia;

public class OldAdaptiveFMTParser
{
    public static List<YouTubeMultimedia> getMedia(Descrambler descrambler, String data)
    {
        var media = new ArrayList<YouTubeMultimedia>();

        if (data == null)
        {
            return media;
        }

        for (String b : data.split(","))
        {
            String url = null;
            String signature = null;
            String quality = null;
            String mediaType = null;
            String codecs = null;
            int bitrate = Multimedia.UNKNOWN_BITRATE;
            int width = Multimedia.UNDEFINED_SIZE;
            int height = Multimedia.UNDEFINED_SIZE;
            int itag = Integer.MIN_VALUE;
            int fps = Multimedia.UNKNOWN_FPS;

            for (String vidParam : b.split("&"))
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

                if (parameter.startsWith("itag="))
                {
                    itag = Integer.parseInt(parameter.substring("itag=".length()).trim());
                }

                if (parameter.startsWith("bitrate="))
                {
                    bitrate = Integer.parseInt(parameter.substring("bitrate=".length()).trim());
                }

                if (parameter.startsWith("size="))
                {
                    String sizeStr = parameter.substring("size=".length());
                    width = Integer.parseInt(sizeStr.split("x")[0].trim());
                    height = Integer.parseInt(sizeStr.split("x")[1].trim());
                }

                if (parameter.startsWith("type="))
                {
                    mediaType = parameter.split(";")[0].trim().substring("type=".length());

                    codecs = parameter.split(";")[1].trim().split("\"")[1].trim();
                }

                if (parameter.startsWith("fps="))
                {
                    String fpsStr = parameter.substring("fps=".length());
                    fps = Integer.parseInt(fpsStr);
                }
            }

            if (signature != null)
            {
                url = url + "&sig=" + signature;
            }

            Multimedia mediaData = new Multimedia(true, quality, bitrate, width, height, itag, fps, mediaType, codecs);
            YouTubeMultimedia multimedia = new YouTubeMultimedia(url, mediaData);

            media.add(multimedia);
        }

        return media;
    }
}
