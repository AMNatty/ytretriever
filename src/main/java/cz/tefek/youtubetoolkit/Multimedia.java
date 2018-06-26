package cz.tefek.youtubetoolkit;

public class Multimedia
{
    public static final int ITAG_3GPP_176x144 = 17; // 176x144 3GPP MP4V MP4A
    public static final int ITAG_3GPP_320x240 = 36; // 320x240 3GPP MP4V MP4A
    public static final int ITAG_MP4_480x360 = 18; // 480x360 MP4 AVC1 MP4A
    public static final int ITAG_WEBM_480x360 = 43; // 480x360 WEBM VP8 VORBIS
    public static final int ITAG_MP4_1280x720 = 22; // 1280x720 MP4 AVC1 MP4A

    public static final int UNKNOWN_BITRATE = -2;
    public static final int UNDEFINED_SIZE = -2;
    public static final int NOT_VIDEO = -3;

    private MultimediaType type;
    private String qualityName;
    private int bitrate;
    private int width;
    private int height;
    private String videoCodec;
    private String audioCodec;
    private String container;
    private int itag;

    public Multimedia(boolean adaptive, String qualityName, int bitrate, int width, int height, int itag, String mediaType, String codecs)
    {
        this.qualityName = qualityName;
        this.bitrate = bitrate;
        this.width = width;
        this.height = height;
        this.itag = itag;

        if (adaptive)
        {
            this.bitrate = bitrate / 1000;
            this.type = mediaType.contains("video") ? MultimediaType.VIDEO : MultimediaType.AUDIO;
            this.container = mediaType.substring(mediaType.indexOf("/") + 1);

            if (this.type == MultimediaType.VIDEO)
            {
                this.qualityName = qualityName;
                this.videoCodec = codecs.trim();
            }
            else
            {
                this.audioCodec = codecs.trim();
                this.qualityName = "Audio " + this.bitrate + "kbps " + this.audioCodec;
                this.width = NOT_VIDEO;
                this.height = NOT_VIDEO;
            }
        }
        else
        {
            this.qualityName = qualityName;

            this.width = UNDEFINED_SIZE;
            this.height = UNDEFINED_SIZE;

            this.bitrate = UNKNOWN_BITRATE;
            this.type = MultimediaType.BOTH;

            this.videoCodec = codecs.split(",")[0].trim();
            this.audioCodec = codecs.split(",")[1].trim();

            if (itag == ITAG_3GPP_176x144)
            {
                this.width = 176;
                this.height = 144;
                this.container = "3gpp";
                this.qualityName = "Legacy 3GPP Small 144p";
            }
            else if (itag == ITAG_3GPP_320x240)
            {
                this.width = 320;
                this.height = 240;
                this.container = "3gpp";
                this.qualityName = "Legacy 3GPP Small 240p";
            }
            else if (itag == ITAG_MP4_1280x720)
            {
                this.width = 1280;
                this.height = 720;
                this.container = "mp4";
                this.qualityName = "Legacy MP4 720p";
            }
            else if (itag == ITAG_MP4_480x360)
            {
                this.width = 480;
                this.height = 360;
                this.container = "mp4";
                this.qualityName = "Legacy MP4 360p";
            }
            else if (itag == ITAG_WEBM_480x360)
            {
                this.width = 480;
                this.height = 360;
                this.container = "webm";
                this.qualityName = "Legacy WEBM 360p";
            }
        }
    }

    public MultimediaType getType()
    {
        return type;
    }

    public int getBitrate()
    {
        return bitrate;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public String getQualityName()
    {
        return qualityName;
    }

    public String getVideoCodec()
    {
        return videoCodec;
    }

    public String getAudioCodec()
    {
        return audioCodec;
    }

    public String getContainer()
    {
        return container;
    }

    public int getITag()
    {
        return itag;
    }

    public void printData()
    {
        System.out.println("------------------");
        System.out.println(this.getContainer() + " " + this.getType().toString());

        switch (this.getType())
        {
            case AUDIO:
                System.out.println(" Audio codec: " + this.getAudioCodec());
                break;
            case VIDEO:
                System.out.println(" Video codec: " + this.getVideoCodec());
                break;
            case BOTH:
                System.out.println(" Video codec: " + this.getVideoCodec());
                System.out.println(" Audio codec: " + this.getAudioCodec());
                break;
        }

        if (this.getBitrate() > 0)
        {
            System.out.println(" Bitrate: " + this.getBitrate() + "kbps");
        }

        if (this.getBitrate() > 0)
        {
            System.out.println(" Quality: " + this.getQualityName());
        }

        System.out.println(" Itag: " + this.getITag());

        if ((this.getType() == MultimediaType.VIDEO || this.getType() == MultimediaType.BOTH) && this.getWidth() > 0 && this.getHeight() > 0)
        {
            System.out.println(" Standard dimensions: " + this.getWidth() + "x" + this.getHeight());
        }
    }
}
