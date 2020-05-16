package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.TimeAgoParser;

import javax.annotation.Nullable;

public class YoutubeStreamPlaylistInfoItemExtractor extends YoutubeStreamInfoItemExtractor {

    private final JsonObject videoInfo;

    /**
     * Creates an extractor of StreamInfoItems from a YouTube playlist page.
     *
     * @param videoInfoItem The JSON page element
     * @param timeAgoParser A parser of the textual dates or {@code null}.
     */
    public YoutubeStreamPlaylistInfoItemExtractor(JsonObject videoInfoItem, @Nullable TimeAgoParser timeAgoParser) {
        super(videoInfoItem, timeAgoParser);
        this.videoInfo = videoInfoItem;
    }

    @Override
    public long getViewCount() throws ParsingException {
        return -1;
    }

    @Override
    public boolean isAd() throws ParsingException {
        return super.isAd() || !videoInfo.has("isPlayable");
    }
}
