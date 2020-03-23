package com.vgtech.common.ui.zxing.zxing;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.R;
import com.vgtech.common.ui.BaseActivity;
import com.vgtech.common.ui.zxing.camera.CameraManager;
import com.vgtech.common.ui.zxing.decoding.BitmapLuminanceSource;
import com.vgtech.common.ui.zxing.decoding.CaptureActivityHandler;
import com.vgtech.common.ui.zxing.decoding.InactivityTimer;
import com.vgtech.common.ui.zxing.view.ViewfinderView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

public class MipcaActivityCapture extends BaseActivity implements Callback {
    // public static final String ACTIVITY_MIPCA_CAPTURE =
    // "action.MipcaActivityCapture.finsh"; // 广播标志
    private static final int RQUEST_BAR_CODE = 1002;
    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private boolean hasSurface;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet, mac, uid;
    private InactivityTimer inactivityTimer;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private static final float BEEP_VOLUME = 0.10f;
    private boolean vibrate;

    private static final int REQUEST_CODE = 100;
    private static final int PARSE_BARCODE_SUC = 300;
    private static final int PARSE_BARCODE_FAIL = 303;
    private ProgressDialog mProgress;
    private String photo_path;
    private String templates_url;//公司版扫码地址
    private Bitmap scanBitmap;
    private TextView btn_photo;
    CameraManager cameraManager;

    @Override
    protected int getContentView() {
        return R.layout.activity_capture;
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ViewUtil.addTopView(getApplicationContext(), this,
        // R.string.scan_card);
        // CameraManager.init(getApplication());
        setTitle(getResources().getString(R.string.scan_qr_code));
        Intent intent = getIntent();
        templates_url = intent.getStringExtra("templates_url");
        String style = intent.getStringExtra("style");
        if ("company".equals(style)) {
            View bgTitleBar = findViewById(R.id.bg_titlebar);
            bgTitleBar.setBackgroundColor(ContextCompat.getColor(this,R.color.comment_blue));
        }
        cameraManager = new CameraManager(getApplication());
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        viewfinderView.setCameraManager(cameraManager);
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
        String app_propertyJson = PrfUtils.getPrfparams(this, "app_property");
        if (!TextUtils.isEmpty(templates_url)) {
            TextView tempUrl = (TextView) findViewById(R.id.tv_pcurl);
            tempUrl.setText(getResources().getString(R.string.pc_browser_opens)
                    +"\n" + templates_url + "\n"+getResources().getString(R.string.and_scan_qr_code));
        } else {
            if(!TextUtils.isEmpty(app_propertyJson))
            {
                try {
                    JSONObject jsonObject = new JSONObject(app_propertyJson);
                    String url = jsonObject.getString("personal_pc");
                    TextView urlTv = (TextView) findViewById(R.id.tv_pcurl);
                    urlTv.setText(getResources().getString(R.string.pc_browser_opens)
                            +"\n"+url+"\n"+getResources().getString(R.string.and_scan_qr_code));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        // IntentFilter intentFilter2 = new
        // IntentFilter(ACTIVITY_MIPCA_CAPTURE);
        // registerReceiver(MipcaActivityCaptureBroadcastReceiver,
        // intentFilter2);// 注册广播
    }

    // private BroadcastReceiver MipcaActivityCaptureBroadcastReceiver = new
    // BroadcastReceiver() {
    // @Override
    // public void onReceive(Context context, Intent intent) {
    // if (intent == null)
    // return;
    // boolean isFinish = intent.getBooleanExtra("isFinish", false);
    // if (isFinish) {
    // MipcaActivityCapture.this.finish();
    // } else {
    // restartPreviewAfterDelay(0L);
    // }
    // }
    // };


    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            mProgress.dismiss();
            switch (msg.what) {
                case PARSE_BARCODE_SUC:
                    onResultHandler((String) msg.obj, scanBitmap);
                    break;
                case PARSE_BARCODE_FAIL:
                    Toast.makeText(MipcaActivityCapture.this, (String) msg.obj,
                            Toast.LENGTH_LONG).show();
                    break;

            }
        }

    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE:
                    // /获取选中图片的路径
                    Cursor cursor = getContentResolver().query(data.getData(),
                            null, null, null, null);
                    if (cursor.moveToFirst()) {
                        photo_path = cursor.getString(cursor
                                .getColumnIndex(MediaStore.Images.Media.DATA));
                    }
                    cursor.close();

                    mProgress = new ProgressDialog(MipcaActivityCapture.this);
                    mProgress.setMessage(getResources().getString(R.string.scanning));
                    mProgress.setCancelable(false);
                    mProgress.show();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Result result = scanningImage(photo_path);
                            if (result != null) {
                                Message m = mHandler.obtainMessage();
                                m.what = PARSE_BARCODE_SUC;
                                m.obj = result.getText();
                                mHandler.sendMessage(m);
                            } else {
                                Message m = mHandler.obtainMessage();
                                m.what = PARSE_BARCODE_FAIL;
                                m.obj = getResources().getString(R.string.scan_fail);
                                mHandler.sendMessage(m);
                            }
                        }
                    }).start();

                    break;

            }
        }
    }

    /**
     * 扫描二维码图片的方法
     *
     * @param path
     * @return
     */
    public Result scanningImage(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
        hints.put(DecodeHintType.CHARACTER_SET, "UTF-8"); // 设置二维码内容的编码

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // 先获取原大小
        scanBitmap = BitmapFactory.decodeFile(path, options);
        options.inJustDecodeBounds = false; // 获取新的大小
        int sampleSize = (int) (options.outHeight / (float) 200);
        if (sampleSize <= 0)
            sampleSize = 1;
        options.inSampleSize = sampleSize;
        scanBitmap = BitmapFactory.decodeFile(path, options);
        BitmapLuminanceSource source = new BitmapLuminanceSource(scanBitmap);
        BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();
        try {
            return reader.decode(bitmap1, hints);

        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (ChecksumException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        decodeFormats = null;
        characterSet = "UTF-8";

        playBeep = true;
        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initBeepSound();
        vibrate = true;

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        cameraManager.closeDriver();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    /**
     * 处理扫描结果
     *
     * @param result
     * @param barcode
     */
    public void handleDecode(Result result, Bitmap barcode) {
        inactivityTimer.onActivity();
        playBeepSoundAndVibrate();
        String resultString = result.getText();
        onResultHandler(resultString, barcode);
    }

    /**
     * 跳转到上一个页面
     *
     * @param resultString
     * @param bitmap
     */
    private void onResultHandler(String resultString, Bitmap bitmap) {
        // String spit_str = "&u=";
//        Toast.makeText(this,resultString,Toast.LENGTH_SHORT).show();
        if (TextUtils.isEmpty(resultString)) {
            ToastUtil.toast(getApplication(), getResources().getString(R.string.scan_fail));
            finish();
        } else {
            Intent data = new Intent();
            data.putExtra("barcode", resultString);
            setResult(RESULT_OK, data);
            finish();
        }

		/*
         * else { if (!resultString.contains(spit_str)) { ToastUtil.toast(this,
		 * "设备无效"); finish(); return; } String s1 =
		 * resultString.split("&u=")[1]; mac = s1.split("&m=")[1]; uid =
		 * s1.split("&m=")[0]; }
		 */

        // Intent resultIntent = new Intent();
        // Bundle bundle = new Bundle();
        // bundle.putString("result", resultString);
        // bundle.putParcelable("bitmap", bitmap);
        // resultIntent.putExtras(bundle);
        // this.setResult(RESULT_OK, resultIntent);
        // MipcaActivityCapture.this.finish();
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            cameraManager.openDriver(surfaceHolder);
        } catch (IOException ioe) {
            return;
        } catch (RuntimeException e) {
            return;
        }
        if (handler == null) {
            // handler = new CaptureActivityHandler(this,
            // decodeFormats,characterSet);
            handler = new CaptureActivityHandler(this, decodeFormats,
                    characterSet);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;

    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();

    }

    public void restartPreviewAfterDelay(long delayMS) {
        if (handler != null) {
            handler.sendEmptyMessageDelayed(MessageIDs.restart_preview, delayMS);
        }
    }

    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);

            AssetFileDescriptor file = getResources().openRawResourceFd(
                    R.raw.beep);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(),
                        file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }

    private static final long VIBRATE_DURATION = 200L;

    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final OnCompletionListener beepListener = new OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setResult(RESULT_CANCELED);
            finish();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_FOCUS
                || keyCode == KeyEvent.KEYCODE_CAMERA) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}