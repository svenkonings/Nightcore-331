package net.ddns.meteoorkip.nightcore_331;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaCodec;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.google.android.exoplayer.AspectRatioFrameLayout;
import com.google.android.exoplayer.DefaultLoadControl;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.LoadControl;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecSelector;
import com.google.android.exoplayer.MediaCodecTrackRenderer;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.audio.AudioTrack;
import com.google.android.exoplayer.chunk.ChunkSampleSource;
import com.google.android.exoplayer.chunk.ChunkSource;
import com.google.android.exoplayer.chunk.FormatEvaluator;
import com.google.android.exoplayer.dash.DashChunkSource;
import com.google.android.exoplayer.dash.DefaultDashTrackSelector;
import com.google.android.exoplayer.dash.mpd.MediaPresentationDescription;
import com.google.android.exoplayer.dash.mpd.MediaPresentationDescriptionParser;
import com.google.android.exoplayer.upstream.BandwidthMeter;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;
import com.google.android.exoplayer.util.ManifestFetcher;
import com.google.android.exoplayer.util.Util;

import java.io.IOException;

public class Player implements ManifestFetcher.ManifestCallback<MediaPresentationDescription>, SurfaceHolder.Callback, MediaCodecVideoTrackRenderer.EventListener, MediaCodecAudioTrackRenderer.EventListener, ExoPlayer.Listener {
    private static final int RENDERER_COUNT = 2;
    private static final int TYPE_VIDEO = 0;
    private static final int TYPE_AUDIO = 1;
    private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
    private static final int VIDEO_BUFFER_SEGMENTS = 200;
    private static final int AUDIO_BUFFER_SEGMENTS = 54;

    private static final int MAX_DELAY = 3000;
    private static final int DELAY_COMPENSATION = 1000;
    private static final float DEFAULT_ASPECT_RATIO = 1.7777778f;

    private static final String TAG = "Player";

    private final Context context;
    private final Handler handler;
    private final String userAgent;

    private boolean playWhenReady;

    private ManifestFetcher<MediaPresentationDescription> manifestFetcher;
    private MediaPresentationDescription manifest;
    private Long positionMs;
    private Long startTime;

    private ExoPlayer player;
    private TrackRenderer[] renderers;
    private float aspectRatio;
    private boolean error;

    private AspectRatioFrameLayout frameLayout;
    private SurfaceHolder surfaceHolder;
    private Surface surface;

    public Player(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
        userAgent = Util.getUserAgent(context, "ExoPlayer");
        setDefaults();
    }

    private void setDefaults() {
        playWhenReady = true;

        manifestFetcher = null;
        manifest = null;
        positionMs = null;
        startTime = null;

        player = null;
        renderers = null;
        aspectRatio = DEFAULT_ASPECT_RATIO;
        error = false;

        frameLayout = null;
        surfaceHolder = null;
        surface = null;
    }

    public void prepare(String url) {
        manifestFetcher = new ManifestFetcher<>(url, new DefaultUriDataSource(context, userAgent), new MediaPresentationDescriptionParser());
        manifestFetcher.singleLoad(handler.getLooper(), this);
    }

    private void preparePlayer() {
        if (manifest == null) {
            return;
        }
        buildRenderers();
        player = ExoPlayer.Factory.newInstance(RENDERER_COUNT);
        player.addListener(this);
        player.prepare(renderers);

        updateVideo();
        seekTo();
        setPlayWhenReady();
        Log.d(TAG, "Prepared player");
    }

    public void release(boolean cancelPrepare) {
        if (cancelPrepare) {
            manifestFetcher = null;
        }
        manifest = null;
        positionMs = null;
        startTime = null;
        releasePlayer();
    }

    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
        renderers = null;
        aspectRatio = DEFAULT_ASPECT_RATIO;
        error = false;
        Log.d(TAG, "Released player");
    }

    public void resetPlayer(boolean errorOnly) {
        if (errorOnly && !error) {
            return;
        }
        releasePlayer();
        preparePlayer();
    }

    public void setPlayWhenReady(boolean playWhenReady) {
        this.playWhenReady = playWhenReady;
        setPlayWhenReady();
    }

    private void setPlayWhenReady() {
        if (player != null) {
            player.setPlayWhenReady(playWhenReady);
        }
    }

    public boolean getPlayWhenReady() {
        return playWhenReady;
    }

    public void seekTo(long positionMs) {
        this.positionMs = positionMs;
        seekTo();
    }

    private void seekTo() {
        if (positionMs != null && startTime != null) {
            startTime -= positionMs;
            positionMs = null;
        }
        sync();
    }

    private void sync() {
        if (player == null || startTime == null) {
            return;
        }
        long currentPosition = System.currentTimeMillis() - startTime;
        long difference = player.getCurrentPosition() - currentPosition;
        if (difference < -MAX_DELAY || difference > MAX_DELAY) {
            player.seekTo(currentPosition + DELAY_COMPENSATION);
        }
    }

    public void setView(AspectRatioFrameLayout frame, SurfaceHolder holder) {
        frameLayout = frame;
        frameLayout.setAspectRatio(aspectRatio);
        surfaceHolder = holder;
        surface = surfaceHolder.getSurface();
        surfaceHolder.addCallback(this);
        updateVideo();
        Log.d(TAG, "View set");
    }

    public void removeView() {
        frameLayout = null;
        surfaceHolder.removeCallback(this);
        surface = null;
        surfaceHolder = null;
        updateVideo();
        Log.d(TAG, "View removed");
    }

    private void updateVideo() {
        boolean disable = surface == null;
        disableVideo(disable);
        pushSurface(disable);
    }

    private void disableVideo(boolean disable) {
        if (player == null) {
            return;
        }
        if (disable) {
            player.setSelectedTrack(TYPE_VIDEO, ExoPlayer.TRACK_DISABLED);
        } else {
            player.setSelectedTrack(TYPE_VIDEO, ExoPlayer.TRACK_DEFAULT);
        }
    }

    private void pushSurface(boolean blockForSurfacePush) {
        if (player == null || renderers == null) {
            return;
        }
        if (blockForSurfacePush) {
            player.blockingSendMessage(renderers[TYPE_VIDEO], MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, surface);
        } else {
            player.sendMessage(renderers[TYPE_VIDEO], MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, surface);
        }
    }

    private void buildRenderers() {
        LoadControl loadControl = new DefaultLoadControl(new DefaultAllocator(BUFFER_SEGMENT_SIZE));
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();

        // Build video renderer
        DataSource videoDataSource = new DefaultUriDataSource(context, bandwidthMeter, userAgent, true);
        ChunkSource videoChunkSource = new DashChunkSource(manifest, DefaultDashTrackSelector.newVideoInstance(context, true, false), videoDataSource, new FormatEvaluator.AdaptiveEvaluator(bandwidthMeter));
        ChunkSampleSource videoSampleSource = new ChunkSampleSource(videoChunkSource, loadControl, VIDEO_BUFFER_SEGMENTS * BUFFER_SEGMENT_SIZE);
        TrackRenderer videoRenderer = new MediaCodecVideoTrackRenderer(context, videoSampleSource, MediaCodecSelector.DEFAULT, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT, 5000, handler, this, -1);

        // Build audio renderer
        DataSource audioDataSource = new DefaultUriDataSource(context, bandwidthMeter, userAgent, true);
        ChunkSource audioChunkSource = new DashChunkSource(manifest, DefaultDashTrackSelector.newAudioInstance(), audioDataSource, new FormatEvaluator.FixedEvaluator());
        ChunkSampleSource audioSampleSource = new ChunkSampleSource(audioChunkSource, loadControl, AUDIO_BUFFER_SEGMENTS * BUFFER_SEGMENT_SIZE);
        TrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(audioSampleSource, MediaCodecSelector.DEFAULT, null, true, handler, this, AudioCapabilities.getCapabilities(context), AudioManager.STREAM_MUSIC);

        // Add renderers to array
        TrackRenderer[] trackRenderers = new TrackRenderer[RENDERER_COUNT];
        trackRenderers[TYPE_VIDEO] = videoRenderer;
        trackRenderers[TYPE_AUDIO] = audioRenderer;
        renderers = trackRenderers;
    }

    // ManifestFetcher.ManifestCallback<MediaPresentationDescription>
    @Override
    public void onSingleManifest(MediaPresentationDescription mpd) {
        if (manifestFetcher == null) {
            return;
        }
        mpd = manifestFetcher.getManifest();
        if (mpd == null) {
            return;
        }
        manifestFetcher = null;
        manifest = mpd;
        startTime = System.currentTimeMillis();
        preparePlayer();
    }

    @Override
    public void onSingleManifestError(IOException e) {
        // TODO: Handle error
        e.printStackTrace();
    }

    // SurfaceHolder.Callback
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        surface = holder.getSurface();
        updateVideo();
        Log.v(TAG, "Surface created");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.v(TAG, "Surface changed: width = " + width + ", height = " + height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        surface = null;
        updateVideo();
        Log.v(TAG, "Surface destroyed");
    }

    // MediaCodecVideoTrackRenderer.EventListener
    @Override
    public void onDroppedFrames(int count, long elapsed) {
        Log.v(TAG, "Dropped " + count + " frames in " + elapsed + "ms");
    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
        aspectRatio = height == 0 ? DEFAULT_ASPECT_RATIO : (width * pixelWidthHeightRatio) / height;
        if (frameLayout != null) {
            frameLayout.setAspectRatio(aspectRatio);
        }
        Log.v(TAG, "Video size changed to " + aspectRatio);
    }

    @Override
    public void onDrawnToSurface(Surface surface) {
        Log.v(TAG, "Drawn to Surface");
    }

    @Override
    public void onDecoderInitializationError(MediaCodecTrackRenderer.DecoderInitializationException e) {
        e.printStackTrace();
    }

    @Override
    public void onCryptoError(MediaCodec.CryptoException e) {
        e.printStackTrace();
    }

    @Override
    public void onDecoderInitialized(String decoderName, long elapsedRealtimeMs, long initializationDurationMs) {
        Log.v(TAG, "Decoder " + decoderName + " initialised on " + elapsedRealtimeMs + "ms in " + initializationDurationMs + "ms");
    }

    // MediaCodecAudioTrackRenderer.EventListener
    @Override
    public void onAudioTrackInitializationError(AudioTrack.InitializationException e) {
        e.printStackTrace();
    }

    @Override
    public void onAudioTrackWriteError(AudioTrack.WriteException e) {
        e.printStackTrace();
    }

    @Override
    public void onAudioTrackUnderrun(int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {
        Log.v(TAG, "Audio track underrun by " + bufferSize + " bytes of " + bufferSizeMs + "ms at " + elapsedSinceLastFeedMs + "ms");
    }

    // Exoplayer.Listener
    // TODO: Add callback for listeners
    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        String text = "playWhenReady = " + playWhenReady + ", playbackState = ";
        switch (playbackState) {
            case ExoPlayer.STATE_BUFFERING:
                text += "buffering";
                break;
            case ExoPlayer.STATE_ENDED:
                release(false);
                // TODO: update service notification
                text += "ended";
                break;
            case ExoPlayer.STATE_IDLE:
                text += "idle";
                break;
            case ExoPlayer.STATE_PREPARING:
                text += "preparing";
                break;
            case ExoPlayer.STATE_READY:
                text += "ready";
                if (playWhenReady) {
                    sync();
                }
                break;
            default:
                Log.e(TAG, "Unknown playback state: " + playbackState);
                return;
        }
        Log.v(TAG, text);
    }

    @Override
    public void onPlayWhenReadyCommitted() {
        Log.v(TAG, "Play when ready committed");
    }

    @Override
    public void onPlayerError(ExoPlaybackException e) {
        // TODO: Handle more types of errors
        error = true;
        e.printStackTrace();
    }
}
