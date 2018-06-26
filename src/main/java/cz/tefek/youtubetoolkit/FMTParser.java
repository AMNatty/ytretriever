package cz.tefek.youtubetoolkit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FMTParser
{
    public static List<YouTubeMultimedia> getMedia(PlayerInfo info) throws IOException
    {
        Descrambler descrambler = new Descrambler(DescramblerHelper.update(info.getPlayerJSURL()));

        List<YouTubeMultimedia> media = new ArrayList<>();

        int i = 0;

        if (info.getAdaptiveFmts() != null)
            for (String b : info.getAdaptiveFmts().split(","))
            {
                System.out.println("╔══>> Video " + i);

                String url = null;
                String signature = null;
                String quality = null;
                String mediaType = null;
                String codecs = null;
                int bitrate = Multimedia.UNKNOWN_BITRATE;
                int width = Multimedia.UNDEFINED_SIZE;
                int height = Multimedia.UNDEFINED_SIZE;
                int itag = Integer.MIN_VALUE;

                for (String vidParam : b.split("&"))
                {
                    String parameter = DescramblerHelper.urlDecode(vidParam);

                    System.out.println("‖ " + parameter);

                    if (parameter.startsWith("url="))
                    {
                        url = parameter.substring("url=".length());
                    }

                    if (parameter.startsWith("s="))
                    {
                        signature = descrambler.descramble(parameter.substring("s=".length()));
                    }

                    if (parameter.startsWith("quality_label="))
                    {
                        quality = parameter.substring("quality_label=".length()).trim();
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
                }

                if (signature != null)
                {
                    url = url + "&signature=" + signature;
                }

                Multimedia mediaData = new Multimedia(true, quality, bitrate, width, height, itag, mediaType, codecs);
                YouTubeMultimedia multimedia = new YouTubeMultimedia(url, mediaData);

                media.add(multimedia);

                i++;
            }

        for (String b : info.getUrlEncodedFmtMap().split(","))
        {
            System.out.println("╔══>> Video " + i);

            String url = null;
            String signature = null;
            String quality = null;
            String mediaType = null;
            String codecs = null;
            int itag = Integer.MIN_VALUE;

            for (String vidParam : b.split("&"))
            {
                String parameter = DescramblerHelper.urlDecode(vidParam);

                System.out.println("‖ " + parameter);

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
                url = url + "&signature=" + signature;
            }

            Multimedia mediaData = new Multimedia(false, quality, Multimedia.UNKNOWN_BITRATE, Multimedia.UNDEFINED_SIZE, Multimedia.UNDEFINED_SIZE, itag, mediaType, codecs);
            YouTubeMultimedia multimedia = new YouTubeMultimedia(url, mediaData);

            media.add(multimedia);

            i++;
        }

        return media;
    }

    // Prefer Opus?
    // Maybe
    public static YouTubeMultimedia getBestAudio(List<YouTubeMultimedia> allMedia)
    {
        YouTubeMultimedia audio = null;

        for (YouTubeMultimedia media : allMedia)
        {
            if (media.getMediaData().getType() == MultimediaType.AUDIO)
            {
                if (audio == null)
                {
                    audio = media;
                }
                else
                {
                    if (audio.getMediaData().getBitrate() < media.getMediaData().getBitrate())
                    {
                        audio = media;
                    }
                }
            }
        }

        return audio;
    }

    public static YouTubeMultimedia getBestVideo(List<YouTubeMultimedia> allMedia)
    {
        YouTubeMultimedia video = null;

        for (YouTubeMultimedia media : allMedia)
        {
            if (media.getMediaData().getType() == MultimediaType.VIDEO)
            {
                if (video == null)
                {
                    video = media;
                }
                else
                {
                    if (video.getMediaData().getBitrate() < media.getMediaData().getBitrate())
                    {
                        video = media;
                    }
                }
            }
        }

        return video;
    }

    public static List<YouTubeMultimedia> getVideosA(List<YouTubeMultimedia> allMedia)
    {
        List<YouTubeMultimedia> videos = new ArrayList<>();

        videos = allMedia.stream().filter(input -> input.getMediaData().getType() == MultimediaType.VIDEO).collect(Collectors.toList());

        return videos;
    }

    public static List<YouTubeMultimedia> getAudiosA(List<YouTubeMultimedia> allMedia)
    {
        List<YouTubeMultimedia> videos = new ArrayList<>();

        videos = allMedia.stream().filter(input -> input.getMediaData().getType() == MultimediaType.AUDIO).collect(Collectors.toList());

        return videos;
    }

    public static List<YouTubeMultimedia> getLegacy(List<YouTubeMultimedia> allMedia)
    {
        List<YouTubeMultimedia> videos = new ArrayList<>();

        videos = allMedia.stream().filter(input -> input.getMediaData().getType() == MultimediaType.BOTH).collect(Collectors.toList());

        return videos;
    }
}
