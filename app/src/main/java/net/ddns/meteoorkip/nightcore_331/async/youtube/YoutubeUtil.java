package net.ddns.meteoorkip.nightcore_331.async.youtube;

import net.ddns.meteoorkip.nightcore_331.async.AsyncUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class YoutubeUtil {
    private static final String INFO_URL = "http://youtube.com/get_video_info?video_id=%s&el=vevo&ps=default&eurl=&gl=US&hl=en";
    private static final String SIG_URL = "http://meteoorkip.ddns.net:8080/youtube/%s";
    private static final String SIG_LOCAL_URL = "http://192.168.0.34:8080/youtube/%s";
    private static final boolean LOCAL = false;

    public static String getVideoInfo(String videoId) throws IOException {
        return readUrl(String.format(INFO_URL, videoId));
    }

    public static String getDashMpd(String videoId) throws IOException {
        if (LOCAL) {
            try {
                return readUrl(String.format(SIG_URL, videoId));
            } catch (IOException e) {
                return readUrl(String.format(SIG_LOCAL_URL, videoId));
            }
        } else {
            return readUrl(String.format(SIG_URL, videoId));
        }
    }

    private static String readUrl(String src) throws IOException {
        URL url = new URL(src);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            return reader.readLine();
        } finally {
            AsyncUtil.close(reader);
        }
    }
}
