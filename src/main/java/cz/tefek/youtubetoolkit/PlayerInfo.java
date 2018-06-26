package cz.tefek.youtubetoolkit;

public class PlayerInfo
{
    private String thumbnail;
    private String author;
    private String name;
    private String adaptiveFmts;
    private String urlEncodedFmtMap;
    private String playerJS;
    private long views;
    private double loudness;

    private boolean valid;

    public PlayerInfo(String thumbnail, long views, double loudness, String author, String name, String adaptiveFmts, String urlEncodedFmtMap, String playerJS)
    {
        this.thumbnail = thumbnail;
        this.adaptiveFmts = adaptiveFmts;
        this.author = author;
        this.name = name;
        this.urlEncodedFmtMap = urlEncodedFmtMap;
        this.playerJS = playerJS;
        this.loudness = loudness;
        this.views = views;

        if ((this.adaptiveFmts != null || this.urlEncodedFmtMap != null) && this.author != null && this.thumbnail != null && this.playerJS != null && this.name != null)
        {
            valid = true;
        }
    }

    public boolean isValid()
    {
        return valid;
    }

    public String getThumbnail()
    {
        return thumbnail;
    }

    public String getAuthor()
    {
        return author;
    }

    public String getName()
    {
        return name;
    }

    public String getUrlEncodedFmtMap()
    {
        return urlEncodedFmtMap;
    }

    public String getAdaptiveFmts()
    {
        return adaptiveFmts;
    }

    public String getPlayerJSURL()
    {
        return playerJS;
    }

    public double getLoudness()
    {
        return loudness;
    }

    public long getViews()
    {
        return views;
    }
}
