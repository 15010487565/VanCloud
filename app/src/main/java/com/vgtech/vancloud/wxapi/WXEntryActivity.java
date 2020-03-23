package com.vgtech.vancloud.wxapi;


import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.EventBusMsg;
import com.google.gson.Gson;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.ShowMessageFromWX;
import com.tencent.mm.sdk.modelmsg.WXAppExtendObject;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.vgtech.common.wxapi.Constants;
import com.vgtech.common.wxapi.Util;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.module.help.HelpDetailActivity;
import com.vgtech.vancloud.ui.module.share.SharedInfoActivity;
import com.vgtech.vancloud.utils.PublishUtils;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public class WXEntryActivity extends BaseActivity implements IWXAPIEventHandler {
    // IWXAPI 是第三方app和微信通信的openapi接口
    private IWXAPI api;
    // 获取第一步的code后，请求以下链接获取access_token
    private String GetCodeRequest = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";
    // 获取用户个人信息
    private String GetUserInfo = "https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID";
    private String vantop_isemulator_url;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = WXAPIFactory.createWXAPI(this, Constants.APP_ID, false);
        api.registerApp(Constants.APP_ID);
        api.handleIntent(getIntent(), this);

    }

    @Override
    protected void onDestroy() {
        api = null;
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected int getContentView() {
        return R.layout.share_over;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
        api.handleIntent(intent, this);
    }


    private void sharedText(int shareType, String text) {
        //初始化一个WXTextObject对象
        WXTextObject textObj = new WXTextObject();
        textObj.text = text;
        //用WXTextObject对象初始化一个WXMediaMessage对象
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = textObj;
        msg.description = text;
        //构造一个Req
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        //transaction字段用于唯一标识一个请求
        req.transaction = getTranscation("textshare");
        req.message = msg;
        //发送的目标场景， 可以选择发送到会话 WXSceneSession 或者朋友圈 WXSceneTimeline。 默认发送到会话。
        req.scene = shareType;
        api.sendReq(req);
    }

    public void sharedImg(int shareType, Bitmap bitmap, String description) {
        //初始化一个WXTextObject对象
//		Bitmap bmp = BitmapFactory.decodeResource(getResources(), shareContent.getPicResource());
        WXImageObject imgObj = new WXImageObject(bitmap);

        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = imgObj;
        msg.description = description;
        msg.title = getString(R.string.vancloud_my_app);


        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = getTranscation("imgshareappdata");
        req.message = msg;
        req.scene = shareType;
        api.sendReq(req);
    }

    /*
     * 分享链接
     */
    public void shareWebPage(int shareType, Bitmap bitmap, String url, String title, String description) {
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = url;
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = title;

        if (bitmap != null&&!bitmap.isRecycled()) {
            msg.thumbData = Util.bmpToByteArray(bitmap, false);
        }
        if (shareType == SendMessageToWX.Req.WXSceneSession)
            msg.description = description.length() > 100 ? description.substring(0, 100) : description;
        else
            msg.title = description.length() > 100 ? description.substring(0, 100) : description;
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = getTranscation("webpage");
        req.message = msg;

        req.scene = shareType;

        if (!api.isWXAppInstalled() || !api.isWXAppSupportAPI()) {
            Toast.makeText(this, getString(R.string.please_install_weixin), Toast.LENGTH_SHORT).show();
            return;
        }
        api.sendReq(req);
    }

    public String getTranscation(String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }


    /*
     * 分享链接
     */
    public void shareApp(String title, String description, Bitmap bitmap, int shareType, String url, Map<String, String> obj) {

        if (obj == null || TextUtils.isEmpty(obj.get("megTypeId")) || (obj.get("resId").equals(PublishUtils.COMMENTTYPE_HELP + "") && obj.get("resId").equals(PublishUtils.COMMENTTYPE_SHARE + "")))
            return;

        WXMediaMessage msg = new WXMediaMessage();
        WXAppExtendObject app = new WXAppExtendObject();
        app.filePath = url;

        Gson gson = new Gson();
        try {
            app.extInfo = gson.toJson(obj);
        } catch (Exception e) {
        }
        msg.thumbData = Util.bmpToByteArray(bitmap, false);
        msg.mediaObject = app;
        msg.title = title;
        msg.description = description;

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = getTranscation("webpage");
        req.message = msg;
        req.scene = shareType;

        if (!api.isWXAppInstalled() || !api.isWXAppSupportAPI()) {
            Toast.makeText(this, getString(R.string.please_install_weixin), Toast.LENGTH_SHORT).show();
            return;
        }

        api.sendReq(req);

    }


    // 微信发送请求到第三方应用时，会回调到该方法
    @Override
    public void onReq(BaseReq req) {
        int type = req.getType();
        switch (type) {
            case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
                break;
            case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
                ShowMessageFromWX.Req r = (ShowMessageFromWX.Req) req;
                String json = ((WXAppExtendObject) r.message.mediaObject).extInfo;
                Gson gson = new Gson();
                Map<String, String> obj = gson.fromJson(json, Map.class);
                Intent intent = new Intent();
                if (obj.get("megTypeId").equals(PublishUtils.COMMENTTYPE_HELP + "")) {
                    intent.setClass(WXEntryActivity.this, HelpDetailActivity.class);
                } else if (obj.get("megTypeId").equals(PublishUtils.COMMENTTYPE_SHARE + "")) {
                    intent.setClass(WXEntryActivity.this, SharedInfoActivity.class);
                }
                intent.putExtra("id", obj.get("resId"));
                startActivity(intent);

                break;
            default:
                break;
        }

        finish();
        overridePendingTransition(0, 0);
    }

    // 第三方应用发送到微信的请求处理后的响应结果，会回调到该方法
    @Override
    public void onResp(BaseResp resp) {
        int result = 0;
        switch (resp.getType()){
            case ConstantsAPI.COMMAND_SENDAUTH://授权返回

                String code = ((SendAuth.Resp) resp).code;
                String openId = resp.openId;
                vantop_isemulator_url = getCodeRequest(code);
                Thread thread=new Thread(downloadRun);
                thread.start();
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                break;
                default:
                    Log.d("ceshi", "onPayFinish, errCode = " + resp.errCode);
                    switch (resp.errCode) {
                        case BaseResp.ErrCode.ERR_OK:
                        {
                            Intent action = new Intent();
                            action.setAction(com.vgtech.common.Constants.SHARE_SUCCESS);
                            LocalBroadcastManager.getInstance(this).sendBroadcast(action);
                        }
                        break;
                        case BaseResp.ErrCode.ERR_USER_CANCEL: {
                            Intent action = new Intent();
                            action.setAction(com.vgtech.common.Constants.SHARE_CANCLE);
                            LocalBroadcastManager.getInstance(this).sendBroadcast(action);
                        }
                        result = R.string.errcode_cancel;
                        break;

                        case BaseResp.ErrCode.ERR_AUTH_DENIED: {
                            Intent action = new Intent();
                            action.setAction(com.vgtech.common.Constants.SHARE_ERROR);
                            LocalBroadcastManager.getInstance(this).sendBroadcast(action);
                        }
                        result = R.string.errcode_deny;
                        break;
                        default: {
                            Intent action = new Intent();
                            action.setAction(com.vgtech.common.Constants.SHARE_ERROR);
                            LocalBroadcastManager.getInstance(this).sendBroadcast(action);
                        }
                        result = R.string.errcode_unknown;
                        break;
                    }

                    break;
        }
        finish();
        overridePendingTransition(0, 0);
    }
    /**
     * 获取access_token的URL（微信）
     * @param code 授权时，微信回调给的
     * @return URL
     */
    public String getCodeRequest(String code) {
        String result = null;
        GetCodeRequest = GetCodeRequest.replace("APPID",
                urlEnodeUTF8(Constants.APP_ID));//AppId
        GetCodeRequest = GetCodeRequest.replace("SECRET",
                urlEnodeUTF8(Constants.WXAPP_APPSECRET));//AppSecret码
        GetCodeRequest = GetCodeRequest.replace("CODE",urlEnodeUTF8( code));
        result = GetCodeRequest;
        return result;
    }
    /**
     * 获取用户个人信息的URL（微信）
     * @param access_token 获取access_token时给的
     * @param openid 获取access_token时给的
     * @return URL
     */
    public String getUserInfo(String access_token,String openid){
        String result = null;
        GetUserInfo = GetUserInfo.replace("ACCESS_TOKEN",
                urlEnodeUTF8(access_token));
        GetUserInfo = GetUserInfo.replace("OPENID",
                urlEnodeUTF8(openid));
        result = GetUserInfo;
        return result;
    }
    public static String urlEnodeUTF8(String str) {
        String result = str;
        try {
            result = URLEncoder.encode(str, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    public  Runnable downloadRun = new Runnable() {

        @Override
        public void run() {
            WXGetAccessToken();

        }
    };

    /**
     * 获取access_token等等的信息(微信)
     */
    private  void WXGetAccessToken(){
        HttpClient vantop_isemulator_httpClient = new DefaultHttpClient();;
        String access_token="";
        String openid ="";
        try {
            HttpPost postMethod = new HttpPost(vantop_isemulator_url);
            HttpResponse response = vantop_isemulator_httpClient.execute(postMethod); // 执行POST方法
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                InputStream is = response.getEntity().getContent();
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(is));
                String str = "";
                StringBuffer sb = new StringBuffer();
                while ((str = br.readLine()) != null) {
                    sb.append(str);
                }
                is.close();
                String josn = sb.toString();
                JSONObject json1 = new JSONObject(josn);
                access_token = (String) json1.get("access_token");
                openid = (String) json1.get("openid");


            } else {
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String get_user_info_url=getUserInfo(access_token,openid);
        WXGetUserInfo(get_user_info_url);
    }

    /**
     * 获取微信用户个人信息
     * @param get_user_info_url 调用URL
     */
    private  void WXGetUserInfo(String get_user_info_url){
        HttpClient vantop_isemulator_httpClient = new DefaultHttpClient();
        String openid="";
        String nickname="";
        String headimgurl="";
        try {
            HttpGet getMethod = new HttpGet(get_user_info_url);
            HttpResponse response = vantop_isemulator_httpClient.execute(getMethod); // 执行GET方法
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                InputStream is = response.getEntity().getContent();
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(is));
                String str = "";
                StringBuffer sb = new StringBuffer();
                while ((str = br.readLine()) != null) {
                    sb.append(str);
                }
                is.close();
                String josn = sb.toString();
                JSONObject json1 = new JSONObject(josn);
                openid = (String) json1.get("openid");
                nickname = (String) json1.get("nickname");
                headimgurl=(String)json1.get("headimgurl");
//                Intent intent = new Intent(WXEntryActivity.this, WeXinPayActivity.class);
//                intent.putExtra("openid",openid);
//                intent.putExtra("nickname",nickname);
//                startActivity(intent);
                EventBusMsg messageEvent = new EventBusMsg();
                messageEvent.setAuthor(true);
                messageEvent.setOppnId(openid);
                messageEvent.setWeName(nickname);
                EventBus.getDefault().post(messageEvent);
            } else {
                EventBusMsg messageEvent = new EventBusMsg();
                messageEvent.setAuthor(false);
                EventBus.getDefault().post(messageEvent);
            }
        } catch (Exception e) {
            e.printStackTrace();
            EventBusMsg messageEvent = new EventBusMsg();
            messageEvent.setAuthor(false);
            EventBus.getDefault().post(messageEvent);
        }

    }

}