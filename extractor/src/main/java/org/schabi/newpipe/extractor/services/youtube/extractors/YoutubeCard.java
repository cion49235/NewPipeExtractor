package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeStreamLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.Card;
import org.schabi.newpipe.extractor.utils.Utils;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import static org.schabi.newpipe.extractor.ServiceList.YouTube;
import static org.schabi.newpipe.extractor.utils.Utils.getLengthFromString;
import static org.schabi.newpipe.extractor.utils.Utils.mixedNumberWordToLong;

public class YoutubeCard extends Card {

    private JsonObject cardRenderer;
    private JsonObject content;
    private int type;
    private String title;

    public YoutubeCard(JsonObject cardRenderer) throws ParsingException {
        this.cardRenderer = cardRenderer;
        this.content = cardRenderer.getObject("content");
        fetchTypeAndTitle();
    }

    private static String getThumbnail(String path, JsonObject info) {
        JsonArray thumbnails = info.getObject(path).getArray("thumbnails");
        return thumbnails.getObject(thumbnails.size() - 1).getString("url"); //last, higher quality
    }

    @Override
    public String getTeaserMessage() {
        return cardRenderer.getObject("teaser").getObject("simpleCardTeaserRenderer")
                .getObject("message").getString(SIMPLE_TEXT);
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public int getType() {
        return this.type;
    }

    @Override
    public long getStartTime() {
        return Long.parseLong(cardRenderer.getArray("cueRanges").getObject(0).getString("startCardActiveMs"));
    }

    @Override
    public String getUrl() {
        switch (type) {
            case VIDEO:
                return YoutubeStreamLinkHandlerFactory.getInstance().getUrl(content.getObject("videoInfoCardContentRenderer")
                        .getObject("action").getObject("watchEndpoint").getString("videoId"));
            case PLAYLIST:
                JsonObject endpoint = content.getObject("playlistInfoCardContentRenderer").getObject("action").getObject("watchEndpoint");
                return "https://www.youtube.com/watch?v=" + endpoint.getString("videoId") + "&list=" + endpoint.getString("playlistId");
            case LINK:
                return Utils.removeYoutubeRedirectLink(content.getObject("simpleCardContentRenderer")
                        .getObject("command").getObject("urlEndpoint").getString("url"));
            case CHANNEL:
                return "https://www.youtube.com/channel/" + content.getObject("collaboratorInfoCardContentRenderer").getObject("endpoint").getObject("browseEndpoint").getString("browseId");
            default:
                return null;
        }
    }

    @Override
    public List<Choice> getChoices() {
        if (type != POLL) return null;

        JsonArray options = content.getObject("pollRenderer").getArray("choices");
        List<Choice> choices = new ArrayList<>();
        for (Object c : options) {
            if (c instanceof JsonObject) {
                final JsonObject item = (JsonObject) c;
                choices.add(new Choice(item));
            }
        }
        return choices;
    }

    @Override
    public String getChannel() {
        switch (type) {
            case VIDEO:
                return content.getObject("videoInfoCardContentRenderer").getObject("channelName").getString(SIMPLE_TEXT).substring(3); //it's "by ChannelName", so we remove "by "
            case PLAYLIST:
                return content.getObject("playlistInfoCardContentRenderer").getObject("channelName").getString(SIMPLE_TEXT).substring(3);
            case CHANNEL:
                return content.getObject("collaboratorInfoCardContentRenderer").getObject("channelName").getString(SIMPLE_TEXT);
            default:
                return null;
        }
    }

    @Override
    public String getThumbnailUrl() {
        switch (type) {
            case VIDEO:
                return getThumbnail("videoThumbnail", content.getObject("videoInfoCardContentRenderer")); //todo: get lower res, because the higher quality for videos is 1080p, and we don't need that much)
            case LINK:
                return getThumbnail("image", content.getObject("simpleCardContentRenderer"));
            case PLAYLIST:
                return getThumbnail("playlistThumbnail", content.getObject("playlistInfoCardContentRenderer"));
            case CHANNEL:
                return getThumbnail("channelAvatar", content.getObject("collaboratorInfoCardContentRenderer"));
            default:
                return null;
        }
    }

    @Override
    public long getLength() {
        if (type == VIDEO) {
            return getLengthFromString(content.getObject("videoInfoCardContentRenderer").getObject("lengthString").getString(SIMPLE_TEXT));
        } else {
            return -1;
        }
    }

    @Override
    public long getCount() {
        switch (type) {
            case VIDEO:
                return Long.parseLong(Utils.removeNonDigitCharacters(content.getObject("videoInfoCardContentRenderer")
                        .getObject("viewCountText").getString(SIMPLE_TEXT)));
            case PLAYLIST:
                return Long.parseLong(content.getObject("playlistInfoCardContentRenderer").getObject("playlistVideoCount").getString(SIMPLE_TEXT));
            case CHANNEL:
                try {
                    return mixedNumberWordToLong(content.getObject("collaboratorInfoCardContentRenderer").getObject("subscriberCountText").getString(SIMPLE_TEXT));
                } catch (ParsingException e) {
                    return -1;
                }
            case POLL:
                long totalVotes = 0;
                for (Choice c : getChoices()) {
                    totalVotes += c.getNumVotes();
                }
                return totalVotes;
            default:
                return 0;
        }
    }

    @Override
    public String getHost() throws ParsingException {
        String host = null;
        if (type == LINK) {
            try {
                host = content.getObject("simpleCardContentRenderer").getObject("displayDomain").getString(SIMPLE_TEXT);
            } catch (Exception e) {
                try {
                    host = Utils.stringToURL(getUrl()).getHost();
                } catch (MalformedURLException ignored) {
                    throw new ParsingException("Could not get host name", e);
                }
            }
        }
        return host;
    }

    @Override
    public String getAdditionalMessage() {
        switch (type) {
            case LINK:
                return content.getObject("simpleCardContentRenderer").getObject("callToAction").getString(SIMPLE_TEXT);
            case CHANNEL:
                return content.getObject("collaboratorInfoCardContentRenderer").getObject("customText").getString(SIMPLE_TEXT);
            default:
                return null;
        }
    }

    @Override
    public int getServiceId() {
        return YouTube.getServiceId();
    }

    private void fetchTypeAndTitle() {
        //fetch both title & type. If we have a NPE, it means the object doesn't exist & we move onto the next type.
        try {
            JsonObject pollInfo = content.getObject("pollRenderer").getObject("question");
            this.type = POLL;
            this.title = pollInfo.getString(SIMPLE_TEXT);
            return;
        } catch (NullPointerException ignored) {
        }
        try {
            JsonObject videoInfo = content.getObject("videoInfoCardContentRenderer").getObject("videoTitle");
            type = VIDEO;
            this.title = videoInfo.getString(SIMPLE_TEXT);
            return;
        } catch (NullPointerException ignored) {
        }
        try {
            JsonObject playlistInfo = content.getObject("playlistInfoCardContentRenderer").getObject("playlistTitle");
            type = PLAYLIST;
            this.title = playlistInfo.getString(SIMPLE_TEXT);
            return;
        } catch (NullPointerException ignored) {
        }
        try {
            JsonObject linkInfo = content.getObject("simpleCardContentRenderer").getObject("title");
            type = LINK;
            this.title = linkInfo.getString(SIMPLE_TEXT);
            return;
        } catch (NullPointerException ignored) {
        }
        try {
            JsonObject channelInfo = content.getObject("collaboratorInfoCardContentRenderer").getObject("customText");
            type = CHANNEL;
            this.title = channelInfo.getString(SIMPLE_TEXT);
            return;
        } catch (NullPointerException ignored) {
        }

        type = INVALID;
    }
}
