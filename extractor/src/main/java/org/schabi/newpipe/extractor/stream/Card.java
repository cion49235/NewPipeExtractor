package org.schabi.newpipe.extractor.stream;

import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;

public abstract class Card implements Serializable {

    public static final String SIMPLE_TEXT = "simpleText";

    public static final int INVALID = -1;
    public static final int POLL = 1;
    public static final int VIDEO = 2;
    public static final int PLAYLIST = 3;
    public static final int LINK = 4;
    public static final int CHANNEL = 5;

    public static String typeToString(int type) {
        switch (type) {
            case POLL:
                return "poll";
            case VIDEO:
                return "video";
            case PLAYLIST:
                return "playlist";
            case LINK:
                return "link";
            case CHANNEL:
                return "channel";
            case INVALID:
            default:
                return "invalid";
        }
    }

    public abstract String getTeaserMessage();

    /*
    ====================
    GETTERS
    ====================
     */

    public abstract String getTitle();

    public abstract int getType();

    /**
     * The start time of the card
     *
     * @return when the card should be displayed (in milliseconds)
     */
    public abstract long getStartTime();

    /**
     * The url that NewPipe should browse on card click
     *
     * @return the url to browse on card click, or null.
     */
    public abstract String getUrl();

    @Nullable
    public abstract List<Choice> getChoices();

    public String toString() {
        String str = "Card{" +
                "type=" + typeToString(getType()) + "," +
                "startTime=" + getStartTime() + "," +
                "title=\"" + getTitle() + "\"";
        switch (getType()) {
            case (VIDEO):
                str += ",thumbnailUrl=\"" + getThumbnailUrl() + "\"," +
                        "url=" + "\"" + getUrl() + "\"," +
                        "length=" + getLength() + "," +
                        "views=" + getCount();
                break;
            case (POLL):
                str += ",totalVotes=" + getCount() + ",choices=" + getChoices();
                break;
            case (PLAYLIST):
                str += ",thumbnailUrl=\"" + getThumbnailUrl() + "\"," +
                        "url=" + "\"" + getUrl() + "\"," +
                        "videosNumber=" + getCount();
                break;
            case (LINK):
                str += ",thumbnailUrl=\"" + getThumbnailUrl() + "\"," +
                        "url=" + "\"" + getUrl() + "\"";
                break;
            case (CHANNEL):
                str += ",thumbnailUrl=\"" + getThumbnailUrl() + "\"," +
                        "url=" + "\"" + getUrl() + "\"," +
                        "subCount=" + getCount() + "," +
                        "channel=" + getChannel();
            default:
                break;
        }
        return str + '}';
    }

    public abstract String getChannel();

    public abstract String getThumbnailUrl();

    public abstract long getLength();

    /**
     * Count: depends of the card.
     * It can be for example videos number for playlists, total votes for polls, total views for videos…
     *
     * @return the count of the card
     */
    public abstract long getCount();

    public abstract String getHost() throws ParsingException;

    public abstract String getAdditionalMessage();

    public abstract int getServiceId();

    public class Choice implements Serializable {
        private JsonObject object;

        public Choice(JsonObject object) {
            this.object = object;
        }

        public long getNumVotes() {
            return Long.parseLong(object.getString("numVotes"));
        }

        public String getText() {
            return object.getObject("text").getString("simpleText");
        }

        public String toString() {
            return "{text=" + getText() + ",numVotes=" + getNumVotes() + ",percentage=" + (double) Math.round(getPercentage() * 10) / 10 + "%}";
        }

        public double getPercentage() {
            return (double) getNumVotes() / getCount();
        }
    }
}