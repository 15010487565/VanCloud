package com.vgtech.common.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.sina.weibo.sdk.api.WebpageObject;
import com.sina.weibo.sdk.api.WeiboMessage;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.utils.Utility;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXAppExtendObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.vgtech.common.R;
import com.vgtech.common.URLAddr;
import com.vgtech.common.wxapi.Constants;
import com.vgtech.common.wxapi.Util;

/**
 * Created by swj on 15/11/16.
 */
public class ShareUtils implements
        IWeiboHandler.Response,
        IWXAPIEventHandler {

    private static IWeiboShareAPI mWeiboShareAPI = null;

    private static IWXAPI api;

    public ShareUtils(){

    }
    public  IWeiboHandler.Response mWeiboHandler;
    public ShareUtils(IWeiboHandler.Response response)
    {
        mWeiboHandler = response;
    }
    public void sharedText(Activity mContext,int shareType, String text) {

        api = WXAPIFactory.createWXAPI(mContext, Constants.APP_ID, false);

        if (!api.isWXAppInstalled() || !api.isWXAppSupportAPI()) {
            Toast.makeText(mContext, mContext.getResources().getString(R.string.install_wx), Toast.LENGTH_SHORT).show();
            return;
        }

        api.registerApp(Constants.APP_ID);
        api.handleIntent(mContext.getIntent(), this);

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
        req.transaction =   "text" + System.currentTimeMillis();
        req.message = msg;
        //发送的目标场景， 可以选择发送到会话 WXSceneSession 或者朋友圈 WXSceneTimeline。 默认发送到会话。
        req.scene = shareType;
        api.sendReq(req);
    }

    /*

     * @param mContext     传入一个activity
     * @param shareType
     *                  SendMessageToWX.Req.WXSceneTimeline  是分享到朋友圈
     *                  SendMessageToWX.Req.WXSceneSession   是直接发送到一个聊天
     * @param bitmap
     * @param url
     * @param title
     *
     */
    public void shareWebPage(Activity mContext,int shareType, Bitmap bitmap, String url, String title,String content) {

        api = WXAPIFactory.createWXAPI(mContext, Constants.APP_ID, false);

        if (!api.isWXAppInstalled() || !api.isWXAppSupportAPI()) {
            Toast.makeText(mContext, mContext.getResources().getString(R.string.install_wx), Toast.LENGTH_SHORT).show();
            return;
        }

        api.registerApp(Constants.APP_ID);
        api.handleIntent(mContext.getIntent(), this);

        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = url;
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = title;
        if(shareType==SendMessageToWX.Req.WXSceneTimeline)
        msg.title = content;
        msg.description = content;
        if (bitmap != null) {
            msg.thumbData = Util.bmpToByteArray(bitmap, false);
        }

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction =  "webpage" + System.currentTimeMillis();
        req.message = msg;
        req.scene = shareType;
        api.sendReq(req);
    }

    /*
     * 分享链接
     */
    public void shareApp(Activity mContext, int shareType,String url) {
//        WXWebpageObject webpage = new WXWebpageObject();
//        webpage.webpageUrl = url;
        WXAppExtendObject msg = new WXAppExtendObject();
        msg.extInfo=url;
//		msg.title = title;
//
//		if(bitmap != null) {
//			msg.thumbData = Util.bmpToByteArray(bitmap,false);
//		}

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = "app" + System.currentTimeMillis();
//		req.message = msg;
        req.scene = shareType;

        if(!api.isWXAppInstalled() || !api.isWXAppSupportAPI()){
            Toast.makeText(mContext, mContext.getResources().getString(R.string.install_wx), Toast.LENGTH_SHORT).show();
            return;
        }
        api.sendReq(req);
    }

    /**
     *
     * @param mContext
     * @param content
     * @param bitmap
     *
     * */
    public IWeiboShareAPI shareWeibo(Activity mContext,String content,Bitmap bitmap) {
        AuthInfo mAuthInfo = new AuthInfo(mContext, com.vgtech.common.weiboapi.Constants.APP_KEY, com.vgtech.common.weiboapi.Constants.REDIRECT_URL, com.vgtech.common.weiboapi.Constants.SCOPE);
        SsoHandler mSsoHandler = new SsoHandler(mContext, mAuthInfo);
        // 创建微博分享接口实例
        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(mContext, com.vgtech.common.weiboapi.Constants.APP_KEY);


        if (!mWeiboShareAPI.isWeiboAppInstalled() || !mWeiboShareAPI.isWeiboAppSupportAPI()) {
            Toast.makeText(mContext, mContext.getResources().getString(R.string.install_wb), Toast.LENGTH_SHORT).show();
            return null;
        }

        mWeiboShareAPI.registerApp();
        if(mWeiboHandler!=null)
        mWeiboShareAPI.handleWeiboResponse(mContext.getIntent(), mWeiboHandler);
        else
        mWeiboShareAPI.handleWeiboResponse(mContext.getIntent(), this);

        WeiboMessage weiboMessage = new WeiboMessage();

        WebpageObject mediaObject = new WebpageObject();
        mediaObject.identify = Utility.generateGUID();
        mediaObject.title = mContext.getResources().getString(R.string.application_name);
        mediaObject.description = content;

//        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_logo);
        // 设置 Bitmap 类型的图片到视频对象里         设置缩略图。 注意：最终压缩过的缩略图大小不得超过 32kb。
        mediaObject.setThumbImage(/*Util.compressBitmapToWeibo(*/bitmap/*)*/);
        mediaObject.actionUrl =  URLAddr.URL_APPLIST;
//        mediaObject.defaultText = "Webpage 默认文案";

        weiboMessage.mediaObject = mediaObject;

        // 2. 初始化从第三方到微博的消息请求
        SendMessageToWeiboRequest request = new SendMessageToWeiboRequest();
        // 用transaction唯一标识一个请求
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.message = weiboMessage;

        // 3. 发送请求消息到微博，唤起微博分享界面
        mWeiboShareAPI.sendRequest(mContext, request);
        return mWeiboShareAPI;
    }

    @Override
    public void onResponse(BaseResponse baseResponse) {

        Log.e("share",""+baseResponse);
    }

    @Override
    public void onReq(BaseReq baseReq) {
        switch (baseReq.getType()) {
            case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
//                goToGetMsg();
                break;
            case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
//                goToShowMsg((ShowMessageFromWX.Req) req);
//                Log.i("swj",)
                break;
            default:
                break;
        }
    }

    @Override
    public void onResp(BaseResp baseResp) {
        Log.e("share",""+baseResp);
    }
}
