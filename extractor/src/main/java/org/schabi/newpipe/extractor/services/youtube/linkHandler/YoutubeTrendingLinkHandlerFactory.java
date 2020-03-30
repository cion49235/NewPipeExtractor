package org.schabi.newpipe.extractor.services.youtube.linkHandler;

/*
 * Created by Christian Schabesberger on 12.08.17.
 *
 * Copyright (C) Christian Schabesberger 2018 <chris.schabesberger@mailbox.org>
 * YoutubeTrendingLinkHandlerFactory.java is part of NewPipe.
 *
 * NewPipe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe.  If not, see <http://www.gnu.org/licenses/>.
 */

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.Utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YoutubeTrendingLinkHandlerFactory extends ListLinkHandlerFactory {

    public static final String TRENDING = "Trending";
    public static final String MUSIC = "Music";
    public static final String GAMING = "Gaming";
    public static final String NEWS = "News";
    public static final String MOVIES = "Movies";
    public static final String BASE_URL = "https://www.youtube.com/feed/";

    public static final Map<String, String> MAP;
    public static final Map<String, String> REVERSE_MAP;

    public static final YoutubeTrendingLinkHandlerFactory instance = new YoutubeTrendingLinkHandlerFactory();

    static {
        Map<String, String> map = new HashMap<>();
        map.put(TRENDING, "trending");
        map.put(MUSIC, "trending&bp=4gIuCggvbS8wNHJsZhIiUExGZ3F1TG5MNTlhbVhvZGtOeGV2aXM2V2laX3AwZXdGOA%3D%3D");
        map.put(GAMING, "trending&bp=4gIvCgkvbS8wYnp2bTISIlBMaUN2Vkp6QnVwS25pMzlocXlsc3lmdlVra0tTTlk4Z2c%3D");
        map.put(NEWS, "trending?bp=4gIuCggvbS8wNWpoZxIiUEwzWlE1Q3BOdWxRbnpKclg5c0EzZnR0ZjhqRTlNc1p2Yw%3D%3D");
        map.put(MOVIES, "trending&bp=4gIuCggvbS8wMnZ4bhIiUEx6akZiYUZ6c21NVEQtZ0F1VEZTSzVta1VwUzFiTEJENg%3D%3D");
        MAP = Collections.unmodifiableMap(map);

        Map<String, String> reverseMap = new HashMap<>();
        for (Map.Entry<String, String> entry : MAP.entrySet()) {
            reverseMap.put(entry.getValue(), entry.getKey());
        }
        REVERSE_MAP = Collections.unmodifiableMap(reverseMap);
    }

    public String getUrl(String id, List<String> contentFilters, String sortFilter) throws ParsingException {
        String parameter = MAP.get(id);
        if (parameter == null) {
            throw new ParsingException("Could not get url from id \"" + id + "\"");
        }
        return BASE_URL + parameter;
    }

    @Override
    public String getId(String url) throws ParsingException {
        String urlWithoutBase = url.replace(BASE_URL, "");
        if (REVERSE_MAP.containsKey(urlWithoutBase)) {
            return REVERSE_MAP.get(urlWithoutBase);
        } else {
            throw new ParsingException("Could not get id");
        }
    }

    @Override
    public boolean onAcceptUrl(final String url) {
        URL urlObj;
        try {
            urlObj = Utils.stringToURL(url);
        } catch (MalformedURLException e) {
            return false;
        }

        String urlPath = urlObj.getPath();
        return Utils.isHTTP(urlObj) && (YoutubeParsingHelper.isYoutubeURL(urlObj) || YoutubeParsingHelper.isInvidioURL(urlObj)) && urlPath.startsWith("/feed/trending");
    }

    public YoutubeTrendingLinkHandlerFactory getInstance() {
        return instance;
    }
}
