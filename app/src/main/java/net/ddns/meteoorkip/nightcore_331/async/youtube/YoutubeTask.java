package net.ddns.meteoorkip.nightcore_331.async.youtube;

import android.os.AsyncTask;
import android.util.Log;

import net.ddns.meteoorkip.nightcore_331.async.TaskResult;
import net.ddns.meteoorkip.nightcore_331.async.youtube.parser.YoutubeMap;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class YoutubeTask extends AsyncTask<String, Void, TaskResult<String>> {

    private static final String TAG = "Youtube";
    private static final String DASH_MPD = "dashmpd";
    private static final String CIPHER = "use_cipher_signature";

    private final WeakReference<Listener> callback;
    private final String videoId;
    private final Long seekTo;

    public YoutubeTask(Listener listener, String videoId, Long seekTo) {
        this.callback = new WeakReference<>(listener);
        this.videoId = videoId;
        this.seekTo = seekTo;
    }

    public static String getDashMpd(String videoId) throws IOException {
        // Collection is updated in constructor
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        YoutubeMap videoInfo = new YoutubeMap(YoutubeUtil.getVideoInfo(videoId));
        if (videoInfo.containsKey(DASH_MPD) && !"True".equals(videoInfo.get(CIPHER))) {
            Log.d(TAG, "Using unencrypted url");
            return videoInfo.get(DASH_MPD);
        } else {
            Log.d(TAG, "Using decrypted url");
            return YoutubeUtil.getDashMpd(videoId);
        }
    }

    @Override
    protected TaskResult<String> doInBackground(String... params) {
        try {
            return new TaskResult<>(getDashMpd(videoId));
        } catch (IOException e) {
            return new TaskResult<>(e);
        }
    }

    @Override
    protected void onPostExecute(TaskResult<String> result) {
        Listener listener = callback.get();
        if (listener != null) {
            listener.onYoutubeTaskCompleted(result, seekTo);
        }
    }

    public interface Listener {
        void onYoutubeTaskCompleted(TaskResult<String> result, Long seekTo);
    }
}
