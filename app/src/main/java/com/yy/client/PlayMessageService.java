package com.yy.client;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;

public class PlayMessageService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private MediaPlayer player = null;

    @Override
    public void onCreate() {
        super.onCreate();

        Uri pickUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.test);
        player = new MediaPlayer();
        try {
            final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) != 0) {
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            }
            player.setDataSource(this, pickUri);
            player.prepare();
            player.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player != null) {
            if (player.isPlaying()) player.stop();
            player.release();
            player = null;
        }
    }
}
