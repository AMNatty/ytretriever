package cz.tefek.youtubetoolkit.metadata;

public class YouTubeMetadata
{
    private String title;

    private String author;

    private long views;

    private long length;

    private boolean allowRatings;
    private double rating;

    private boolean useCipher;

    private double loudness;

    public YouTubeMetadata(String title, String author, long views, long length, boolean allowRatings, double rating, double loudness, boolean useCipher)
    {
        this.title = title;
        this.author = author;
        this.useCipher = useCipher;
        this.length = length;
        this.allowRatings = allowRatings;
        this.rating = rating;
        this.views = views;
        this.loudness = loudness;
    }

    public String getTitle()
    {
        return this.title;
    }

    public String getAuthor()
    {
        return this.author;
    }

    public long getLength()
    {
        return this.length;
    }

    public boolean doesUseCipher()
    {
        return this.useCipher;
    }

    public boolean isRatingAllowed()
    {
        return this.allowRatings;
    }

    public double getRating()
    {
        return this.rating;
    }

    public long getViews()
    {
        return this.views;
    }

    public double getLoudness()
    {
        return this.loudness;
    }
}
