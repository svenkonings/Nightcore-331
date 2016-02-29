package net.ddns.meteoorkip.nightcore_331.async.youtube.parser;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;

public class YoutubeMap extends HashMap<String, String> {

    public YoutubeMap(String src) {
        super();
        if (src == null) {
            return;
        }
        String[] map = src.split("&");
        for (String entry : map) {
            String[] pair = entry.split("=", 2);
            try {
                put(pair[0], URLDecoder.decode(pair[1], "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    public YoutubeMap getMap(Object key) {
        return new YoutubeMap(get(key));
    }

    public YoutubeList getList(Object key) {
        return new YoutubeList(get(key));
    }
}
