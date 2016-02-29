package net.ddns.meteoorkip.nightcore_331;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;

import com.google.android.exoplayer.AspectRatioFrameLayout;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Activity";

    private AspectRatioFrameLayout frameLayout;
    private SurfaceView surfaceView;
    private boolean active;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getIntExtra(MainService.ACTION_MESSAGE, -1)) {
                case MainService.START:
                    if (active) {
                        resumeVideo();
                    }
                    break;
                case MainService.STOP:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        finishAndRemoveTask();
                    } else {
                        finish();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        frameLayout = (AspectRatioFrameLayout) findViewById(R.id.video_frame);
        surfaceView = (SurfaceView) findViewById(R.id.surface_view);
    }

    @Override
    protected void onResume() {
        super.onResume();
        active = true;
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(MainService.ACTION_MESSAGE));
        resumeVideo();
    }

    @Override
    protected void onPause() {
        super.onPause();
        active = false;
        pauseVideo();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    private void resumeVideo() {
        Player player = MainService.getPlayer();
        if (player != null) {
            player.setView(frameLayout, surfaceView.getHolder());
            Log.d(TAG, "View set");
        } else {
            startService(new Intent(this, MainService.class));
            Log.d(TAG, "Service started");
        }
    }

    private void pauseVideo() {
        Player player = MainService.getPlayer();
        if (player != null) {
            player.removeView();
            Log.d(TAG, "View removed");
        }
    }
}
