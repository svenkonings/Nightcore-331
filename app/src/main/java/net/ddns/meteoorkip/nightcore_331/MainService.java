package net.ddns.meteoorkip.nightcore_331;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.audio.AudioCapabilitiesReceiver;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;

import net.ddns.meteoorkip.nightcore_331.async.BitmapTask;
import net.ddns.meteoorkip.nightcore_331.async.TaskResult;
import net.ddns.meteoorkip.nightcore_331.async.youtube.YoutubeTask;
import net.ddns.meteoorkip.nightcore_331.socket.Song;
import net.ddns.meteoorkip.nightcore_331.socket.Type;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.List;
import java.util.Map;

public class MainService extends Service implements YoutubeTask.Listener, BitmapTask.Listener, AudioCapabilitiesReceiver.Listener {

    public static final String ACTION_PLAY = "com.signature.action_play";
    public static final String ACTION_PAUSE = "com.signature.action_pause";
    public static final String ACTION_STOP = "com.signature.action_stop";
    public static final String ACTION_MESSAGE = "com.signature.action_message";

    public static final int START = 0;
    public static final int STOP = 1;

    private static final String TAG = "Service";
    private static final String URL = "https://alpha.nightcore-331.net:8080/socket/websocket";
    private static final int NOTIFY_ID = 1;

    private static final CookieManager defaultCookieManager;

    static {
        defaultCookieManager = new CookieManager();
        defaultCookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    private static Player player;

    private PowerManager.WakeLock lock;
    private AudioCapabilitiesReceiver audioCapabilitiesReceiver;
    private Handler handler;
    private NotificationManagerCompat notificationManager;
    private WebSocket webSocket;
    private boolean registered;
    private boolean disconnecting;

    private String title;
    private String userName;
    private Bitmap bitmap;

    private YoutubeTask youtubeTask;
    private BitmapTask bitmapTask;

    public static Player getPlayer() {
        return player;
    }

    @Override
    public void onCreate() {
        lock = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getString(R.string.app_name));
        lock.acquire();
        CookieHandler currentHandler = CookieHandler.getDefault();
        if (currentHandler != defaultCookieManager) {
            CookieHandler.setDefault(defaultCookieManager);
        }
        audioCapabilitiesReceiver = new AudioCapabilitiesReceiver(this, this);
        audioCapabilitiesReceiver.register();

        handler = new Handler(getMainLooper());
        player = new Player(this, handler);
        startListening();
        sendMessage(START);

        notificationManager = NotificationManagerCompat.from(this);
        startForeground(NOTIFY_ID, generateNotification());

        Log.d(TAG, "Service started");
    }

    private void startListening() {
        if (disconnecting) {
            return;
        }
        try {
            webSocket = new WebSocketFactory().createSocket(URL);
        } catch (IOException e) {
            e.printStackTrace();
            stopListening();
            return;
        }
        webSocket.addListener(socketListener);
        webSocket.connectAsynchronously();
        Log.d(TAG, "Socket created");
    }

    private void stopListening() {
        closeSocket();
        if (disconnecting) {
            return;
        }
        registerReceiver(connectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        registered = true;
        Log.d(TAG, "Connectivity receiver started");
    }

    private void closeSocket() {
        if (webSocket == null) {
            return;
        }
        if (webSocket.isOpen()) {
            webSocket.disconnect();
        }
        webSocket = null;
        Log.d(TAG, "Socket closed");
    }

    private void updateNotification() {
        notificationManager.notify(NOTIFY_ID, generateNotification());
    }

    private Notification generateNotification() {
        String contentTitle = title == null ? getString(R.string.app_name) : title;

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, NOTIFY_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setSmallIcon(R.drawable.ic_volume_up)
                .setContentTitle(contentTitle)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.MediaStyle().setShowActionsInCompactView(0, 1));

        if (userName != null) {
            builder.setContentText(userName);
        }
        if (bitmap != null) {
            builder.setLargeIcon(bitmap);
        }
        if (player.getPlayWhenReady()) {
            builder.addAction(R.drawable.ic_pause, "Pause", generateAction(ACTION_PAUSE));
        } else {
            builder.addAction(R.drawable.ic_play_arrow, "Play", generateAction(ACTION_PLAY));
        }
        builder.addAction(R.drawable.ic_stop, "Stop", generateAction(ACTION_STOP));

        return builder.build();
    }

    private PendingIntent generateAction(String action) {
        Intent intent = new Intent(this, MainService.class);
        intent.setAction(action);
        return PendingIntent.getService(this, NOTIFY_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIntent(intent);
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        disconnecting = true;
        if (registered) {
            unregisterReceiver(connectivityReceiver);
        }
        closeSocket();
        if (youtubeTask != null) {
            youtubeTask.cancel(true);
        }
        if (bitmapTask != null) {
            bitmapTask.cancel(true);
        }
        audioCapabilitiesReceiver.unregister();
        player.release(true);
        player = null;
        sendMessage(STOP);
        notificationManager.cancelAll();
        lock.release();
        Log.d(TAG, "Service destroyed");
    }

    private void handleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        if (action == null) {
            return;
        }
        switch (action) {
            case ACTION_PLAY:
                player.setPlayWhenReady(true);
                updateNotification();
                break;
            case ACTION_PAUSE:
                player.setPlayWhenReady(false);
                updateNotification();
                break;
            case ACTION_STOP:
                stopSelf();
                break;
        }
    }

    private void sendMessage(int message) {
        Intent intent = new Intent(ACTION_MESSAGE);
        intent.putExtra(ACTION_MESSAGE, message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.d(TAG, "Sent message: " + message);
    }

    @Override
    public void onYoutubeTaskCompleted(TaskResult<String> result, Long seekTo) {
        if (result.hasValue()) {
            player.release(true);
            if (seekTo != null) {
                player.seekTo(seekTo);
            }
            player.prepare(result.getValue());
            Log.d(TAG, "Received video: " + result.getValue());
        }
        if (result.hasException()) {
            result.getException().printStackTrace();
        }
        youtubeTask = null;
    }

    @Override
    public void onBitmapTaskCompleted(String title, String userName, TaskResult<Bitmap> result) {
        this.title = title;
        this.userName = userName;
        if (result.hasValue()) {
            bitmap = result.getValue();
            Log.d(TAG, "Received bitmap");
        } else {
            bitmap = null;
        }
        updateNotification();
        if (result.hasException()) {
            result.getException().printStackTrace();
        }
        bitmapTask = null;
    }

    @Override
    public void onAudioCapabilitiesChanged(AudioCapabilities audioCapabilities) {
        player.resetPlayer(false);
        Log.d(TAG, "Audio capabilities changed");
    }

    private final WebSocketAdapter socketListener = new WebSocketAdapter() {
        private static final String TAG = "Socket";
        private String currentVideo;

        @Override
        public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    player.resetPlayer(true);
                }
            });
            Log.d(TAG, "Connected");
        }

        @Override
        public void onConnectError(WebSocket websocket, WebSocketException exception) throws Exception {
            stopListening();
            Log.d(TAG, "Connect error");
        }

        @Override
        public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
            stopListening();
            Log.d(TAG, "Disconnected");
        }

        @Override
        public void onTextMessage(WebSocket websocket, String text) throws Exception {
            Log.d(TAG, "Received String: " + text);
            try {
                JSONObject jsonObject = new JSONObject(text);
                String type = jsonObject.getString(Type.TYPE);
                switch (type) {
                    case Type.SONG:
                        Song song = new Song(jsonObject);
                        Song.Data data = song.getData();
                        String videoId = data.getCid();
                        if (videoId.equals(currentVideo)) {
                            Log.d(TAG, "Same video: " + videoId);
                            break;
                        }
                        currentVideo = videoId;

                        if (youtubeTask != null) {
                            youtubeTask.cancel(true);
                        }
                        Long seekTo = null;
                        if (data.hasRem()) {
                            seekTo = data.getDur() - data.optRem();
                        }
                        youtubeTask = new YoutubeTask(MainService.this, currentVideo, seekTo);
                        youtubeTask.execute();

                        if (bitmapTask != null) {
                            bitmapTask.cancel(true);
                        }
                        bitmapTask = new BitmapTask(MainService.this, data.getTitle(), song.getUser().getUserName(), data.getThumbnail());
                        bitmapTask.execute();

                        Log.d(TAG, "Preparing video: " + data.getCid());
                        break;
                    case Type.WAITLIST_UPDATE:
                    case Type.WOOTUPDATE:
                    case Type.HISTORYUPDATE:
                    case Type.USER_ADD:
                    case Type.USER_REMOVE:
                    case Type.MSG:
                    case Type.ANNOUNCEMENT:
                    case Type.DELETECHAT:
                    case Type.LOG:
                        // TODO: implement more types
                        break;
                    default:
                        Log.e(TAG, "Unknown type: " + type);
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
            cause.printStackTrace();
        }

        @Override
        public void handleCallbackError(WebSocket websocket, Throwable cause) throws Exception {
            cause.printStackTrace();
        }
    };

    private final BroadcastReceiver connectivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork != null && activeNetwork.isConnected()) {
                startListening();
                unregisterReceiver(this);
                registered = false;
            }
        }
    };
}
