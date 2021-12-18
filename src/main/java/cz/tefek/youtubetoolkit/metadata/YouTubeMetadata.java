package cz.tefek.youtubetoolkit.metadata;

public record YouTubeMetadata(String title,
                              String author,
                              long views,
                              long length,
                              boolean allowRatings,
                              double loudness,
                              boolean useCipher)
{

}
