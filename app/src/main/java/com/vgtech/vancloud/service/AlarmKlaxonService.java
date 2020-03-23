/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.vgtech.vancloud.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.vgtech.vancloud.R;


/**
 * 闹钟响铃、震动服务
 * 
 * @author zhangshaofang
 * 
 */
public class AlarmKlaxonService extends Service {

	private Vibrator mVibrator;
	private MediaPlayer mMediaPlayer;
	private TelephonyManager mTelephonyManager;
	private int mInitialCallState;
	private static final long[] sVibratePattern = new long[] { 500, 500 };
	private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
		@Override
		public void onCallStateChanged(int state, String ignored) {
			// The user might already be in a call when the alarm fires. When
			// we register onCallStateChanged, we get the initial in-call state
			// which kills the alarm. Check against the initial call state so
			// we don't kill the alarm during a call.
			System.out.println(state);
			if (state != TelephonyManager.CALL_STATE_IDLE && state != mInitialCallState) {
				stop();
			} else {
				start();
			}
		}
	};
	private boolean mBell;
	private boolean mShock;

	@Override
	public void onCreate() {
		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		// Listen for incoming calls to kill the alarm.
		mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		int bell = preferences.getInt("PREF_TIP_BELL", 1);
		int shock = preferences.getInt("PREF_TIP_SHOCK", 0);
		mBell = bell != 0;
		mShock = shock != 0;
		if (mBell)
			init();
		start();
	}

	@Override
	public void onDestroy() {
		stop();
		// Stop listening for incoming calls.
		mTelephonyManager.listen(mPhoneStateListener, 0);
		super.onDestroy();
	}

	public class LocalBinder extends Binder {
		public AlarmKlaxonService getService() {
			return AlarmKlaxonService.this;
		}
	}

	private final IBinder mBinder = new LocalBinder();

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// No intent, tell the system not to restart us.
		if (intent == null) {
			stopSelf();
			return START_NOT_STICKY;
		}

		// Record the initial call state here so that the new alarm has the
		// newest state.
		mInitialCallState = mTelephonyManager.getCallState();

		return START_STICKY;
	}

	// Volume suggested by media team for in-call alarms.
	private static final float IN_CALL_VOLUME = 0.125f;

	private void init() {
		Uri alert = null;
		if (alert == null) {
			alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
		}

		// TODO: Reuse mMediaPlayer instead of creating a new one and/or use
		// RingtoneManager.
		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setOnErrorListener(new OnErrorListener() {
			public boolean onError(MediaPlayer mp, int what, int extra) {
				mp.stop();
				mp.release();
				mMediaPlayer = null;
				return true;
			}
		});

		try {
			// Check if we are in a call. If we are, use the in-call alarm
			// resource at a low volume to not disrupt the call.
			if (mTelephonyManager.getCallState() != TelephonyManager.CALL_STATE_IDLE) {
				mMediaPlayer.setVolume(IN_CALL_VOLUME, IN_CALL_VOLUME);
				setDataSourceFromResource(getResources(), mMediaPlayer, R.raw.fallbackring);
			} else {
				mMediaPlayer.setDataSource(this, alert);
			}
			startAlarm(mMediaPlayer);
		} catch (Exception ex) {
			try {
				// Must reset the media player to clear the error state.
				mMediaPlayer.reset();
				setDataSourceFromResource(getResources(), mMediaPlayer, R.raw.fallbackring);
				startAlarm(mMediaPlayer);
			} catch (Exception ex2) {
			}
		}
	}

	private void startAlarm(MediaPlayer player) throws java.io.IOException, IllegalArgumentException, IllegalStateException {
		final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
			player.setAudioStreamType(AudioManager.STREAM_ALARM);
			player.setLooping(true);
			player.prepare();
			player.start();
		}
	}

	private void setDataSourceFromResource(Resources resources, MediaPlayer player, int res) throws java.io.IOException {
		AssetFileDescriptor afd = resources.openRawResourceFd(res);
		if (afd != null) {
			player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
			afd.close();
		}
	}

	/**
	 * Stops alarm audio and disables alarm if it not snoozed and not repeating
	 */
	public void stop() {
		if (mBell)
			if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
				mMediaPlayer.stop();
				mMediaPlayer.release();
				mMediaPlayer = null;
			}
		if (mShock)
			mVibrator.cancel();
	}


	public void start() {
		if (mBell)
			mMediaPlayer.start();
		if (mShock)
			mVibrator.vibrate(sVibratePattern, 0);
	}

	public void pause() {
		if (mBell)
			mMediaPlayer.pause();
	}

}
