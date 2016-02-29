package net.ddns.meteoorkip.nightcore_331.async;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;

public class BitmapTask extends AsyncTask<String, Void, TaskResult<Bitmap>> {

    private final WeakReference<Listener> callback;
    private final String title;
    private final String userName;
    private final String src;

    public BitmapTask(Listener listener, String title, String userName, String src) {
        this.callback = new WeakReference<>(listener);
        this.title = title;
        this.userName = userName;
        this.src = src;
    }

    public static Bitmap getBitmap(String src) throws IOException {
        URL url = new URL(src);
        InputStream input = null;
        try {
            input = url.openStream();
            return BitmapFactory.decodeStream(input);
        } finally {
            AsyncUtil.close(input);
        }
    }

    @Override
    protected TaskResult<Bitmap> doInBackground(String... params) {
        try {
            return new TaskResult<>(getBitmap(src.replace("default", "mqdefault")));
        } catch (IOException e1) {
            try {
                return new TaskResult<>(getBitmap(src), e1);
            } catch (IOException e2) {
                return new TaskResult<>(e2);
            }
        }
    }

    @Override
    protected void onPostExecute(TaskResult<Bitmap> result) {
        Listener listener = callback.get();
        if (listener != null) {
            listener.onBitmapTaskCompleted(title, userName, result);
        }
    }

    public interface Listener {
        void onBitmapTaskCompleted(String title, String userName, TaskResult<Bitmap> result);
    }
}
