/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package anticlimacticteleservices.bitchutetv;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;

;

/**
 * A fullscreen activity to play audio or video streams.
 */
public class PlayerActivity extends AppCompatActivity {

  private PlaybackStateListener playbackStateListener;
  private static final String TAG = PlayerActivity.class.getName();

  private PlayerView playerView;
  private SimpleExoPlayer player;
  private boolean playWhenReady = true;
  private int currentWindow = 0;
  private long playbackPosition = 0;
  private boolean playing;
  private String playerState;
  private String speed="";
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_player);

    playerView = findViewById(R.id.video_view);

    playbackStateListener = new PlaybackStateListener();
  }

  @Override
  public void onStart() {
    super.onStart();
    if (Util.SDK_INT > 23) {
      initializePlayer();
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    hideSystemUi();
    if ((Util.SDK_INT <= 23 || player == null)) {
      initializePlayer();
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    if (Util.SDK_INT <= 23) {
      releasePlayer();
    }
  }

  @Override
  public void onStop() {
    super.onStop();
    if (Util.SDK_INT > 23) {
      releasePlayer();
    }
  }

  private void initializePlayer() {
    Log.e("WTF", "started initialize");
    if (player == null) {
      DefaultTrackSelector trackSelector = new DefaultTrackSelector(this);
      trackSelector.setParameters(
              trackSelector.buildUponParameters().setMaxVideoSizeSd());
      player = new SimpleExoPlayer.Builder(this)
              .setTrackSelector(trackSelector)
              .build();
    }
    Log.e("WTF", "setting player");
    playerView.setPlayer(player);
    Log.e("WTF", "creating media item " + MainActivity.data.getActiveVideo().getTitle() + " " + MainActivity.data.getActiveVideo().getMp4());
    MediaItem mediaItem = new MediaItem.Builder()
            .setUri(Uri.parse(MainActivity.data.getActiveVideo().getMp4()))
            .setMimeType(MimeTypes.BASE_TYPE_VIDEO)
            .build();
    Log.e("WTF", "setting media item");
    player.setMediaItem(mediaItem);

    player.setPlayWhenReady(playWhenReady);
    player.seekTo(currentWindow, playbackPosition);
    player.addListener(playbackStateListener);
    player.prepare();
  }

  private void releasePlayer() {
    if (player != null) {
      playbackPosition = player.getCurrentPosition();
      currentWindow = player.getCurrentWindowIndex();
      playWhenReady = player.getPlayWhenReady();
      player.removeListener(playbackStateListener);
      player.release();
      player = null;
    }
  }

  @SuppressLint("InlinedApi")
  private void hideSystemUi() {
    playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
  }

  private class PlaybackStateListener implements Player.EventListener {

    @Override
    public void onPlaybackStateChanged(int playbackState) {
      String stateString;
      switch (playbackState) {
        case ExoPlayer.STATE_IDLE:
          stateString = "ExoPlayer.STATE_IDLE      -";
          playing=false;
          playerState=stateString;
          break;
        case ExoPlayer.STATE_BUFFERING:
          stateString = "ExoPlayer.STATE_BUFFERING -";
          playerState=stateString;
          break;
        case ExoPlayer.STATE_READY:
          stateString = "ExoPlayer.STATE_READY     -";
          playerState=stateString;
          break;
        case ExoPlayer.STATE_ENDED:
          stateString = "ExoPlayer.STATE_ENDED     -";
          playing=false;
          playerState=stateString;
          break;
        default:
          stateString = "UNKNOWN_STATE             -";
          break;
      }
      Log.d(TAG, "changed state to " + stateString);
    }
  }
  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    View current = getCurrentFocus();
    Log.i("WTF", String.valueOf(event.getKeyCode()) + KeyEvent.keyCodeToString(event.getKeyCode()));





    int code = event.getKeyCode();
    int act = event.getAction();
    //center button
    if ((act == KeyEvent.ACTION_UP) && code == 23){
      Log.i("WTF","center button clicked");

      if (!playing){
        player.play();
        playing=true;
        Log.i("WTF", "playing video");
        return true;
      } else {
        player.pause();
        playing = false;
        Log.i("WTF", "pausing video");
        return true;
      }
    }
    /*//menu
    if ((act == KeyEvent.ACTION_DOWN && code ==82) || (code==23 && event.isLongPress()))  {
      Log.e("WTF","need to bring up menu");
      player.
      webviewFragment.getWebView().loadUrl("javascript:videojsPlayer.pause()");
      playing = false;
      webviewFragment.getMoreButton().callOnClick();
      menu=true;
      return true;
    }

     */
    //back
    if ((act == KeyEvent.ACTION_DOWN && (code == 286 || code == 21 || code ==88 ))){
      Log.e("WTF","need to backup 10 seconds, videojs player handling");
      player.seekTo(player.getCurrentPosition()-5000);
      return true;
    }
    //forward
    if ((act == KeyEvent.ACTION_DOWN && (code == 287 || code == 22 || code ==87 ))){
      Log.e("WTF","need to advance 10 seconds, videojs player handling");
      player.seekTo(player.getCurrentPosition()+5000);
      return true;
    }
    //up
    if ((act == KeyEvent.ACTION_DOWN && (code == 288 || code == 19))){
      Log.e("WTF","videojs raise volume 10 percent, currently disabled");
      player.increaseDeviceVolume();
      return true;
    }
    //DOWN
    if ((act == KeyEvent.ACTION_DOWN && (code == 289 || code == 20))) {
      Log.e("WTF", "videojs lowers volume 10 percent, currently disabled");
      player.decreaseDeviceVolume();
      return true;
    }
    return(super.dispatchKeyEvent(event));
  }

}