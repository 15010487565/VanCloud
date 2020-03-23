package com.vgtech.vancloud.ui.chat.controllers;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import static android.media.MediaPlayer.OnCompletionListener;
import static android.media.MediaPlayer.OnErrorListener;
import static android.media.MediaPlayer.OnPreparedListener;

/**
 * @author xuanqiang
 * @date 14-2-28
 */
public class MediaController {

  public static long getAmrDuration(final File file) throws IOException {
    long duration = -1;
    int[] packedSize = { 12, 13, 15, 17, 19, 20, 26, 31, 5, 0, 0, 0, 0, 0, 0, 0 };
    RandomAccessFile randomAccessFile = null;
    try {
      randomAccessFile = new RandomAccessFile(file, "rw");
      long length = file.length();
      int pos = 6;//设置初始位置
      int frameCount = 0;//初始帧数
      int packedPos;
      byte[] datas = new byte[1];//初始数据值
      while (pos <= length) {
        randomAccessFile.seek(pos);
        if (randomAccessFile.read(datas, 0, 1) != 1) {
          duration = length > 0 ? ((length - 6) / 650) : 0;
          break;
        }
        packedPos = (datas[0] >> 3) & 0x0F;
        pos += packedSize[packedPos] + 1;
        frameCount++;
      }
      duration += frameCount * 20;//帧数*20
    } finally {
      if (randomAccessFile != null) {
        randomAccessFile.close();
      }
    }
    return duration;
  }


  public static synchronized void playAudio(final File audioFile, final OnPreparedListener preparedListener,
                                            final OnCompletionListener completionListener,
                                            final OnErrorListener errorListener){
    FileInputStream fis = null;
    try{
      stopAudio();
      player = new MediaPlayer();
      playerErrorListener = errorListener;
      player.setOnPreparedListener(preparedListener);
      player.setOnCompletionListener(completionListener);
      player.setOnErrorListener(errorListener);
      fis = new FileInputStream(audioFile);
      player.reset();
      player.setDataSource(fis.getFD());
      player.prepare();
      player.start();
    }catch(IOException e){
      e.printStackTrace();
    }finally{
      if(fis != null){
        try {
          fis.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public static synchronized void stopAudio(){
    if(playerErrorListener != null){
      playerErrorListener.onError(player,MediaPlayer.MEDIA_ERROR_UNKNOWN,1);
      playerErrorListener = null;
    }
    if(player != null){
      player.stop();
      player.release();
      player = null;
    }
  }

  public static void storageAudioMode(final Activity activity, boolean inCall) {
    SharedPreferences pref = activity.getSharedPreferences(PREF_AUDIO, Context.MODE_PRIVATE);
    pref.edit().putBoolean("inCall", inCall).commit();
  }

  public static boolean isInCall(final Activity activity) {
    SharedPreferences pref = activity.getSharedPreferences(PREF_AUDIO, Context.MODE_PRIVATE);
    return pref.getBoolean("inCall",false);
  }

  public static void setAudioMode(final Activity activity) {
    setAudioMode(activity, isInCall(activity));
  }

  public static void setAudioMode(final Activity activity, boolean isInCall) {
    AudioManager audioManager = (AudioManager)activity.getSystemService(Context.AUDIO_SERVICE);
    if(audioManager != null){
      if(isInCall) {
        audioManager.setMode(AudioManager.MODE_IN_CALL);
        audioManager.setSpeakerphoneOn(false);
        activity.setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
      }else {
        audioManager.setMode(AudioManager.MODE_NORMAL);
        audioManager.setSpeakerphoneOn(true);
      }
    }
  }

  public static  String getFilePathByContentResolver(final Context context, final Uri uri) {
    if (null == uri) {
      return null;
    }
    Cursor c = context.getContentResolver().query(uri, null, null, null, null);
    if (null == c) {
      return null;
    }
    String filePath  = null;
    try{
      if(c.getCount() == 1 && c.moveToFirst()) {
        filePath = c.getString(c.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
      }
    }finally {
      c.close();
    }
    return filePath;
  }

  public static String saveImageToPhotoAlbum(final Context context, final String imagePath) throws FileNotFoundException {
    String uriString = MediaStore.Images.Media.insertImage(context.getContentResolver(), imagePath, null, null);
    String filePath = getFilePathByContentResolver(context, Uri.parse(uriString));
    context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(filePath))));
    return filePath;
  }

//  public static String queryImagePath(final Context context, final Uri uri) {
//    String imagePath = null;
//    if(uri != null) {
//      String[] projection = {MediaStore.Images.Media.DATA};
//      Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
//      if(cursor != null) {
//        try {
//          if(cursor.moveToFirst()) {
//            imagePath = cursor.getString(cursor.getColumnIndex(projection[0]));
//          }
//        }finally {
//          cursor.close();
//        }
//      }
//    }
//    return imagePath;
//  }

  public static boolean isExternalStorageDocument(Uri uri) {
    return "com.android.externalstorage.documents".equals(uri.getAuthority());
  }

  public static boolean isDownloadsDocument(Uri uri) {
    return "com.android.providers.downloads.documents".equals(uri.getAuthority());
  }

  public static String getDataColumn(Context context, Uri uri, String selection,
                                     String[] selectionArgs) {

    Cursor cursor = null;
    final String column = "_data";
    final String[] projection = {
      column
    };

    try {
      cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                                                  null);
      if (cursor != null && cursor.moveToFirst()) {
        final int index = cursor.getColumnIndexOrThrow(column);
        return cursor.getString(index);
      }
    } finally {
      if (cursor != null)
        cursor.close();
    }
    return null;
  }

  public static boolean isMediaDocument(Uri uri) {
    return "com.android.providers.media.documents".equals(uri.getAuthority());
  }

  public static boolean isGooglePhotosUri(Uri uri) {
    return "com.google.android.apps.photos.content".equals(uri.getAuthority());
  }

  @TargetApi(Build.VERSION_CODES.KITKAT)
  public static String queryImagePath(final Context context, final Uri uri) {
    final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    // DocumentProvider
    if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
      // ExternalStorageProvider
      if (isExternalStorageDocument(uri)) {
        final String docId = DocumentsContract.getDocumentId(uri);
        final String[] split = docId.split(":");
        final String type = split[0];

        if ("primary".equalsIgnoreCase(type)) {
          return Environment.getExternalStorageDirectory() + "/" + split[1];
        }
      }
      // DownloadsProvider
      else if (isDownloadsDocument(uri)) {
        final String id = DocumentsContract.getDocumentId(uri);
        final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

        return getDataColumn(context, contentUri, null, null);
      }
      // MediaProvider
      else if (isMediaDocument(uri)) {
        final String docId = DocumentsContract.getDocumentId(uri);
        final String[] split = docId.split(":");
        final String type = split[0];

        Uri contentUri = null;
        if ("image".equals(type)) {
          contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else if ("video".equals(type)) {
          contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else if ("audio".equals(type)) {
          contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

        final String selection = "_id=?";
        final String[] selectionArgs = new String[] {
          split[1]
        };

        return getDataColumn(context, contentUri, selection, selectionArgs);
      }
    }
    // MediaStore (and general)
    else if ("content".equalsIgnoreCase(uri.getScheme())) {

      // Return the remote address
      if (isGooglePhotosUri(uri))
        return uri.getLastPathSegment();

      return getDataColumn(context, uri, null, null);
    }
    // File
    else if ("file".equalsIgnoreCase(uri.getScheme())) {
      return uri.getPath();
    }

    return null;
  }

  private static MediaPlayer player;
  private static OnErrorListener playerErrorListener;
  private static final String PREF_AUDIO = "audio";

}
