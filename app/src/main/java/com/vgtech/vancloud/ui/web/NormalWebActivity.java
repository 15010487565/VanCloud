package com.vgtech.vancloud.ui.web;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.GeolocationPermissions;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.huantansheng.easyphotos.EasyPhotos;
import com.vgtech.common.Constants;
import com.vgtech.common.ui.permissions.PermissionsActivity;
import com.vgtech.common.ui.permissions.PermissionsChecker;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.BaseActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 默认加载web页面，web页面不带标题
 */
public class NormalWebActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "NormalWebActivity";
    protected WebView mWebView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View mBtnClose;
    private TextView mTitleTv;
    private VancloudLoadingLayout mLoadingLayout;

    private String mUrl;
    private String mTitle;
    private JsInterface mJsInterface;
    private ValueCallback<Uri> uploadMessage;
    private ValueCallback<Uri[]> uploadMessageAboveL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() == null || TextUtils.isEmpty(getIntent().getStringExtra("url")) || TextUtils.isEmpty("title")) {
            Log.i(TAG, "onCreate: intent = null 或者 url = null 或者 title = null");
            mWebView.clearCache(true);
            this.finish();
        }
        mUrl = getIntent().getStringExtra("url");
        mTitle = getIntent().getStringExtra("title");
        findViewById(R.id.iv_back).setOnClickListener(this);
        mBtnClose = findViewById(R.id.iv_close);
        mBtnClose.setVisibility(View.GONE);
        mBtnClose.setOnClickListener(this);
        mTitleTv = (TextView) findViewById(R.id.tv_title);
        mTitleTv.setText(mTitle);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.my_swiperefreshlayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);

        mWebView = (WebView) findViewById(R.id.webview);
        WebSettings settings = mWebView.getSettings();
        mWebView.clearCache(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setBlockNetworkImage(false); // 解决图片不显示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
//        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setDatabaseEnabled(true);
        String dir = getAppliction().getDir("database", Context.MODE_PRIVATE).getPath();
        settings.setGeolocationDatabasePath(dir);
        settings.setGeolocationEnabled(true);
        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.setWebChromeClient(new MyWebChromeClient());
        mJsInterface = new JsInterface();
//        mWebView.addJavascriptInterface(new JsInterface(), "camera");
        mLoadingLayout = (VancloudLoadingLayout) findViewById(R.id.ll_loadingview);
        mLoadingLayout.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
            @Override
            public void loadAgain() {
                mWebView.loadUrl(mUrl);
            }
        });
        mLoadingLayout.setVisibility(View.GONE);
        mWebView.loadUrl(mUrl);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Constants.DEBUG) {
            mWebView.setWebContentsDebuggingEnabled(true);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            String url = mWebView.getUrl();
            if (!TextUtils.isEmpty(url) && url.endsWith("leadersearch/index.html")) {
                mWebView.clearCache(true);
                finish();
            } else {
                if (mWebView.canGoBack()) {
                    mBtnClose.setVisibility(View.VISIBLE);
                    mWebView.goBack();
                    return true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                String url = mWebView.getUrl();
                if (mWebView.canGoBack()) {
                    mBtnClose.setVisibility(View.VISIBLE);
                    mWebView.goBack();
                } else {
                    mWebView.clearCache(true);
                    finish();
                }
                break;
            case R.id.iv_close:
                mWebView.clearCache(true);
                finish();
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_neigou_web;
    }

    @Override
    public void onRefresh() {
        mWebView.reload();
    }


    private class MyWebViewClient extends WebViewClient {
        public boolean shouldOverrideUrlLoading(final WebView view, String url) {
            if (!(url.startsWith("http") || url.startsWith("https"))) {
                return true;
            }

            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
//            view.getSettings().setJavaScriptEnabled(true);
            mWebView.getSettings().setDomStorageEnabled(true);
            swipeRefreshLayout.setRefreshing(false);
            view.postDelayed(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }, 1000);
            super.onPageFinished(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                }
            });
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Toast.makeText(NormalWebActivity.this, getString(R.string.plans_web_toast), Toast.LENGTH_SHORT).show();
            super.onReceivedError(view, errorCode, description, failingUrl);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            if (url.contains("http://android/img")) {
                String imgPath = url.replace("http://android/img", "");
                int startIdx = imgPath.lastIndexOf(".");
                String type = imgPath.substring(startIdx + 1, imgPath.length());
                try {
                    FileInputStream inputStream = new FileInputStream(imgPath);
                    return new WebResourceResponse("image/" + type, "UTF-8", inputStream);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            return super.shouldInterceptRequest(view, url);
        }
    }

    public class MyWebChromeClient extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
        }

        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            callback.invoke(origin, true, false);
            super.onGeolocationPermissionsShowPrompt(origin, callback);
        }

        //For Android  >= 4.1
        public void openFileChooser(ValueCallback<Uri> valueCallback, String acceptType, String capture) {
            uploadMessage = valueCallback;
            showFileChooser();
        }

        //For Android  >= 5.0
        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            uploadMessageAboveL = filePathCallback;
            showFileChooser();
            return true;
        }

    }

    private final int CAPTURE_TAG = 1;
    private final int CHOOSE_TAG = 3;
//    private String IMG_PATH = null;

    private void showFileChooser() {
        String[] selectPicTypeStr = {"拍照", "从相册中选"};
//        IMG_PATH = Environment.getExternalStorageDirectory() + File.separator + "vancloud" + File.separator + System.currentTimeMillis() + ".jpg";
        AlertDialog mAlertDialog = new AlertDialog.Builder(this)
                .setItems(selectPicTypeStr,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                switch (which) {
                                    // 拍照
                                    case 0:
//                                        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                                        File file = new File(IMG_PATH);
//                                        if (!file.exists()) {
//                                            try {
//                                                file.createNewFile();
//                                            } catch (IOException e) {
//                                                e.printStackTrace();
//                                            }
//                                        }
//                                        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
//                                        startActivityForResult(captureIntent, CAPTURE_TAG);
                                        PermissionsChecker mChecker = new PermissionsChecker(NormalWebActivity.this);
                                        if (mChecker.lacksPermissions(CAMERAPERMISSION)) {
                                            // 请求权限
                                            PermissionsActivity.startActivityForResult(NormalWebActivity.this, CAMERA_REQUESTCODE, CAMERAPERMISSION);
                                        } else {
                                            // 全部权限都已获取
                                            EasyPhotos.createCamera(NormalWebActivity.this)
                                                    .setFileProviderAuthority("com.vgtech.vantop.ui.userinfo.fileprovider")
                                                    .start(CAPTURE_TAG);
                                        }
                                        break;
                                    // 手机相册
                                    case 1:
                                        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                                        i.addCategory(Intent.CATEGORY_OPENABLE);
                                        i.setType("image/*");
                                        startActivityForResult(Intent.createChooser(i, "选择您要添加的照片"), CHOOSE_TAG);
                                        break;
                                    default:
                                        break;
                                }

                            }
                        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        //DO NOTHING
                    }
                }).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_TAG || requestCode == CHOOSE_TAG) {

            if (uploadMessage == null && uploadMessageAboveL == null) {
                return;
            }
            if (resultCode != RESULT_OK) {
                //一定要返回null,否则<input file> 就是没有反应
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                    uploadMessage = null;
                }
                if (uploadMessageAboveL != null) {
                    uploadMessageAboveL.onReceiveValue(null);
                    uploadMessageAboveL = null;
                }
                return;
            }
            Uri imageUri = null;
            switch (requestCode) {
                case CAPTURE_TAG:
//                    if (data == null) {//指定了uri则data 为空
                        //返回图片地址集合：如果你只需要获取图片的地址，可以用这个
                        ArrayList<String> resultPaths = data.getStringArrayListExtra(EasyPhotos.RESULT_PATHS);
                        //返回图片地址集合时如果你需要知道用户选择图片时是否选择了原图选项，用如下方法获取
                        boolean selectedOriginal = data.getBooleanExtra(EasyPhotos.RESULT_SELECTED_ORIGINAL, false);
                        if (resultPaths != null && resultPaths.size() > 0) {
                           String path = resultPaths.get(0);
                            File file = new File(path);
                            if (file.exists()) {
                                Uri localUri = Uri.fromFile(file);
                                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, localUri);
                                sendBroadcast(intent);
                                imageUri = Uri.fromFile(file);
                            }
                        }

//                    }
                    break;
                case CHOOSE_TAG:
                    if (data != null) {
                        Uri uri = data.getData();
//                        String path = FileUtils.getFilePathByUri(this, uri);
                        if (!TextUtils.isEmpty(uri.toString())) {
                            imageUri = uri;
                        }
                    }
                    break;
            }
            if (imageUri != null) {
                Toast.makeText(this, imageUri.toString(), Toast.LENGTH_SHORT).show();
            }
            //上传文件
            if (uploadMessage != null) {
                uploadMessage.onReceiveValue(imageUri);
                uploadMessage = null;
            }
            if (uploadMessageAboveL != null) {
                uploadMessageAboveL.onReceiveValue(new Uri[]{imageUri});
                uploadMessageAboveL = null;

            }
        }
    }

    //    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        switch (requestCode) {
//            case CAPTURE_TAG://拍照
//                if (data != null && data.getData() != null) {//指定了uri则data 为空
//                } else {
//                    Bitmap bitmap = BitmapFactory.decodeFile(IMG_PATH);
//                    if (bitmap != null) {
////                        mJsInterface.onCaptureFinished(mWebView, IMG_PATH);
//                    } else {
////                        mJsInterface.onCaptureFinished(mWebView, "");
////                        Toast.makeText(this, "bitmap is empty", Toast.LENGTH_SHORT).show();
//                    }
//                }
//                break;
//            case CHOOSE_TAG://从相册中选
//                if (data != null) {
//                    Uri uri = data.getData();
//                    Log.i(TAG, "onActivityResult: uri = " + uri.toString() + "scheme = " + uri.getScheme());
//                    String path = FileUtils.getFilePathByUri(this, uri);
//                    Log.i(TAG, "onActivityResult: path = " + path);
//                    if (TextUtils.isEmpty(path)) {
////                        mJsInterface.onCaptureFinished(mWebView, "");
//                    } else {
////                        mJsInterface.onCaptureFinished(mWebView, path);
//                    }
//                } else {
////                    mJsInterface.onCaptureFinished(mWebView, "");
//                }
//                break;
//        }
//    }

    /**
     * 将bitmap转成base64
     *
     * @param bitmap
     * @return
     */
    private String castBitmapToBase64(Bitmap bitmap) {
        String base64String = "";
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, bos);
            byte[] bitmapBytes = bos.toByteArray();
            bos.flush();
            bos.close();
            base64String = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return base64String;
    }

    public class JsInterface {

        public void onCaptureFinished(WebView webView, String imgPath) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                webView.evaluateJavascript("javascript:onCaptureFinished('" + imgPath + "')", null);
            } else {
                webView.loadUrl("javascript:onCaptureFinished('" + imgPath + "')");
            }
        }
    }

    public static class FileUtils {

        public static String getFilePathByUri(Context context, Uri uri) {
            String path = null;
            // 以 file:// 开头的
            if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
                path = uri.getPath();
                return path;
            }
            // 以 content:// 开头的，比如 content://media/extenral/images/media/17766
            if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme()) && Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                Cursor cursor = null;
                try {
                    cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA);
                            if (columnIndex > -1) {
                                path = cursor.getString(columnIndex);
                            }
                        }

                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                        cursor = null;
                    }
                }
                return path;
            }
            // 4.4及之后的 是以 content:// 开头的，比如 content://com.android.providers.media.documents/document/image%3A235700
            if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme()) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (DocumentsContract.isDocumentUri(context, uri)) {
                    if (isExternalStorageDocument(uri)) {
                        // ExternalStorageProvider
                        final String docId = DocumentsContract.getDocumentId(uri);
                        final String[] split = docId.split(":");
                        final String type = split[0];
                        if ("primary".equalsIgnoreCase(type)) {
                            path = Environment.getExternalStorageDirectory() + "/" + split[1];
                            return path;
                        }
                    } else if (isDownloadsDocument(uri)) {
                        // DownloadsProvider
                        final String id = DocumentsContract.getDocumentId(uri);
                        final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                                Long.valueOf(id));
                        path = getDataColumn(context, contentUri, null, null);
                        return path;
                    } else if (isMediaDocument(uri)) {
                        // MediaProvider
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
                        final String[] selectionArgs = new String[]{split[1]};
                        path = getDataColumn(context, contentUri, selection, selectionArgs);
                        return path;
                    }
                } else {
                    Cursor cursor = null;
                    try {
                        cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
                        if (cursor != null) {
                            if (cursor.moveToFirst()) {
                                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA);
                                if (columnIndex > -1) {
                                    path = cursor.getString(columnIndex);
                                }
                            }

                        }
                    } finally {
                        if (cursor != null) {
                            cursor.close();
                            cursor = null;
                        }
                    }
                    return path;
                }
            }
            return null;
        }

        private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
            Cursor cursor = null;
            final String column = "_data";
            final String[] projection = {column};
            try {
                cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
                if (cursor != null && cursor.moveToFirst()) {
                    final int column_index = cursor.getColumnIndexOrThrow(column);
                    return cursor.getString(column_index);
                }
            } finally {
                if (cursor != null)
                    cursor.close();
            }
            return null;
        }

        private static boolean isExternalStorageDocument(Uri uri) {
            return "com.android.externalstorage.documents".equals(uri.getAuthority());
        }

        private static boolean isDownloadsDocument(Uri uri) {
            return "com.android.providers.downloads.documents".equals(uri.getAuthority());
        }

        private static boolean isMediaDocument(Uri uri) {
            return "com.android.providers.media.documents".equals(uri.getAuthority());
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CookieSyncManager.createInstance(this.getApplicationContext());
        CookieManager cookieManager = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.removeSessionCookies(null);
            cookieManager.removeAllCookie();
            cookieManager.flush();
        } else {
            cookieManager.removeAllCookie();
            CookieSyncManager.getInstance().sync();
        }
        WebStorage.getInstance().deleteAllData(); //清空WebView的localStorage
    }
}
