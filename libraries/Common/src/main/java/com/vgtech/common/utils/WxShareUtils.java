package com.vgtech.common.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.widget.Toast;

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
import com.vgtech.common.wxapi.Constants;
import com.vgtech.common.wxapi.Util;

/**
 * Created by code on 2016/3/18.
 */
public class WxShareUtils implements IWXAPIEventHandler {

    private static IWXAPI api;

    public void sharedText(Activity mContext,int shareType, String text) {

        api = WXAPIFactory.createWXAPI(mContext, Constants.APP_ID, false);

        if (!api.isWXAppInstalled() || !api.isWXAppSupportAPI()) {
            Toast.makeText(mContext, mContext.getString(R.string.install_wx), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(mContext, mContext.getString(R.string.install_wx), Toast.LENGTH_SHORT).show();
            return;
        }

        api.registerApp(Constants.APP_ID);
        api.handleIntent(mContext.getIntent(), this);

        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = url;
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = title;
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
            Toast.makeText(mContext, mContext.getString(R.string.install_wx), Toast.LENGTH_SHORT).show();
            return;
        }
        api.sendReq(req);
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

    }
}
