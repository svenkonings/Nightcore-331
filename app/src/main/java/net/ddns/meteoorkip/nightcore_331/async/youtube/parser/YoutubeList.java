package net.ddns.meteoorkip.nightcore_331.async.youtube.parser;

import java.util.ArrayList;

public class YoutubeList extends ArrayList<String> {

    public YoutubeList(String src) {
        super();
        if (src == null) {
            return;
        }
        String[] list = src.split(",");
        for (String entry : list) {
            add(entry);
        }
    }

    public YoutubeMap getMap(int index) {
        return new YoutubeMap(get(index));
    }

    public YoutubeList getList(int index) {
        return new YoutubeList(get(index));
    }
}
