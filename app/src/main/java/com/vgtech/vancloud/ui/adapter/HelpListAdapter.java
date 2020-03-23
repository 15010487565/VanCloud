package com.vgtech.vancloud.ui.adapter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.sina.weibo.sdk.api.WebpageObject;
import com.sina.weibo.sdk.api.WeiboMessage;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.constant.WBConstants;
import com.sina.weibo.sdk.utils.Utility;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.AudioInfo;
import com.vgtech.common.api.HelpListItem;
import com.vgtech.common.api.ImageInfo;
import com.vgtech.common.api.NewUser;
import com.vgtech.common.api.RootData;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.common.image.ImageGridviewAdapter;
import com.vgtech.common.image.ImageLoadFresco;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.provider.db.PublishTask;
import com.vgtech.common.view.NoScrollGridview;
import com.vgtech.common.view.NoScrollListview;
import com.vgtech.common.weiboapi.Constants;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.MainActivity;
import com.vgtech.vancloud.ui.SearchBaseActivity;
import com.vgtech.vancloud.ui.chat.EmojiFragment;
import com.vgtech.vancloud.ui.common.publish.NewPublishedActivity;
import com.vgtech.vancloud.ui.module.help.HelpDetailActivity;
import com.vgtech.vancloud.ui.view.MoreButtonPopupWindow;
import com.vgtech.vancloud.utils.EditUtils;
import com.vgtech.vancloud.utils.PublishUtils;
import com.vgtech.common.utils.UserUtils;
import com.vgtech.vancloud.utils.Utils;

import org.json.JSONException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by app02 on 2015/9/10.
 */
public class HelpListAdapter extends BaseAdapter implements ViewListener,
        MoreButtonPopupWindow.SharedBottomBar,
        HttpListener<String>,
        IWeiboHandler.Response {

    private final int COLLECTION_THIS_HELP = 1;
    private final int DELETE_THIS_HELP = 2;

    private NetworkManager mNetworkManager;

    private SearchBaseActivity mContext;
    private List<HelpListItem> data;
    private int mPosition;

    private Bitmap bitmap;

    private MoreButtonPopupWindow menuWindow;

    public HelpListAdapter(SearchBaseActivity mContext, List<HelpListItem> data) {
        this.mContext = mContext;
        this.data = data;
    }

    public List<HelpListItem> getData() {
        return data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public void clear() {
        this.data.clear();
        try {
            this.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder mViewHolder = null;
        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.help_list_item, null);

            mViewHolder.userHead = (SimpleDraweeView) convertView.findViewById(R.id.user_photo);
            mViewHolder.userName = (TextView) convertView.findViewById(R.id.user_name);
            mViewHolder.createTime = (TextView) convertView.findViewById(R.id.timestamp);
            mViewHolder.helperContent = (TextView) convertView.findViewById(R.id.content_text);

            EditUtils.SetTextViewMaxLines(mViewHolder.helperContent, 5);

            mViewHolder.imageGridView = (NoScrollGridview) convertView.findViewById(R.id.imagegridview);
            mViewHolder.voiceListview = (NoScrollListview) convertView.findViewById(R.id.voice_listview);

            mViewHolder.replyButton = (TextView) convertView.findViewById(R.id.comment_num);
            mViewHolder.praiseButton = (TextView) convertView.findViewById(R.id.praise_num);
            mViewHolder.moreButton = (TextView) convertView.findViewById(R.id.more_num);

            mViewHolder.replyButtonOnclick = (RelativeLayout) convertView.findViewById(R.id.comment_click);
            mViewHolder.praiseButtonClick = (RelativeLayout) convertView.findViewById(R.id.praise_click);
            mViewHolder.moreButtonClick = (RelativeLayout) convertView.findViewById(R.id.more_click);
            mViewHolder.scheduleListItemPraiseIcon = (ImageView) convertView.findViewById(R.id.schedule_list_item_praise_icon);

            mViewHolder.sharedContent = (LinearLayout) convertView.findViewById(R.id.shared_content_layout);
            mViewHolder.sharedContentText = (TextView) convertView.findViewById(R.id.shared_content_text);
            EditUtils.SetTextViewMaxLines(mViewHolder.sharedContentText, 5);
            mViewHolder.sharedImagegridview = (NoScrollGridview) convertView.findViewById(R.id.shared_imagegridview);
            mViewHolder.sharedvoiceListview = (NoScrollListview) convertView.findViewById(R.id.shared_voice_listview);

            mViewHolder.moreNum = (TextView) convertView.findViewById(R.id.more_num);

            convertView.findViewById(R.id.time_layout).setVisibility(View.GONE);
            mViewHolder.replyButton.setCompoundDrawablePadding(Utils.convertDipOrPx(mContext, 8));
            mViewHolder.praiseButton.setCompoundDrawablePadding(Utils.convertDipOrPx(mContext, 8));
            mViewHolder.moreButton.setCompoundDrawablePadding(Utils.convertDipOrPx(mContext, 8));

            mViewHolder.moreNum.setText(mContext.getString(R.string.more));

            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        final HelpListItem item = data.get(position);
        final NewUser user = item.getData(NewUser.class);
        HelpListItem helpItem = item.getData(HelpListItem.class);
        final List<ImageInfo> imags = item.getArrayData(ImageInfo.class);
        final List<AudioInfo> audios = item.getArrayData(AudioInfo.class);

        mViewHolder.replyButtonOnclick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (item.comments > 0) {
                    Intent intent = new Intent(mContext, HelpDetailActivity.class);
                    intent.putExtra("json", data.get(position).getJson().toString());
                    intent.putExtra("position", position);
                    intent.putExtra("showcomment", true);
                    mContext.startActivityForResult(intent, 1);
                } else {
                    PublishUtils.addComment(mContext, PublishUtils.COMMENTTYPE_HELP, item.helpId + "", position);
                }
            }
        });

        if (imags != null && imags.size() > 0) {
            mViewHolder.imageGridView.setVisibility(View.VISIBLE);
            ImageGridviewAdapter imageGridviewAdapter = new ImageGridviewAdapter(mViewHolder.imageGridView, mContext, imags);
            mViewHolder.imageGridView.setAdapter(imageGridviewAdapter);

        } else {
            mViewHolder.imageGridView.setVisibility(View.GONE);
            bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher);
        }


        if (audios != null && !audios.isEmpty()) {
            mViewHolder.voiceListview.setVisibility(View.VISIBLE);
            AudioListAdapter audioListAdapter = new AudioListAdapter(mContext, this);
            audioListAdapter.dataSource.clear();
            audioListAdapter.dataSource.addAll(audios);
            audioListAdapter.notifyDataSetChanged();
            mViewHolder.voiceListview.setAdapter(audioListAdapter);
        } else {
            mViewHolder.voiceListview.setVisibility(View.GONE);

        }

        if (user != null) {
            ImageOptions.setUserImage(mViewHolder.userHead, user.photo);
            mViewHolder.userName.setText(Html.fromHtml(user.name));
            UserUtils.enterUserInfo(mContext, user.userid, user.name, user.photo, mViewHolder.userHead);
        }


        mViewHolder.createTime.setText(Utils.getInstance(mContext).dateFormat(Long.parseLong(item.timestamp)));
        mViewHolder.helperContent.setText(EmojiFragment.getEmojiContentWithAt(mContext, mViewHolder.helperContent.getTextSize(),Html.fromHtml(item.content)));
        if (item.comments > 0)
            mViewHolder.replyButton.setText(item.comments + "");
        else
            mViewHolder.replyButton.setText(mContext.getString(R.string.detail_reply));

        if (item.praises > 0)
            mViewHolder.praiseButton.setText(item.praises + "");
        else
            mViewHolder.praiseButton.setText(mContext.getString(R.string.help_list_item_praise_in_detail));


        if (item.ispraise) {
            mViewHolder.scheduleListItemPraiseIcon.setImageResource(R.drawable.item_help_dig_orange);
            mViewHolder.praiseButton.setTextColor(EditUtils.redCreateColorStateList());
        } else {
            mViewHolder.scheduleListItemPraiseIcon.setImageResource(R.drawable.item_help_dig);
            mViewHolder.praiseButton.setTextColor(EditUtils.greyCreateColorStateList());
        }

        mViewHolder.praiseButtonClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PublishUtils.toDig(mContext, item.helpId + "", PublishUtils.COMMENTTYPE_HELP, data.get(position).ispraise, new PublishUtils.DigCallBack() {

                    @Override
                    public void successful(boolean digType) {
                        if (digType)
                            data.get(position).praises -= data.get(position).praises > 0 ? 1 : 0;
                        else
                            data.get(position).praises += 1;
                        data.get(position).ispraise = !digType;
                        try {
                            data.get(position).getJson().put("praises", data.get(position).praises);
                        } catch (Exception e) {
                        }
                        try {
                            data.get(position).getJson().put("ispraise", !digType);
                        } catch (Exception e) {
                        }
                        notifyDataSetChanged();
                    }
                });
            }
        });


        final NoScrollGridview iv = mViewHolder.imageGridView;
        mViewHolder.moreButtonClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (menuWindow != null && menuWindow.isShowing()) {
                    menuWindow.dismiss();
                    return;
                }
                mPosition = position;
                if (imags != null && imags.size() > 0) {
                    new ImageLoadFresco.LoadImageFrescoBuilder(mContext, new SimpleDraweeView(mContext), imags.get(0).thumb)
                            .setBitmapDataSubscriber(new BaseBitmapDataSubscriber() {
                                @Override
                                protected void onNewResultImpl(Bitmap bitmap) {
                                    if (bitmap == null) {
                                        HelpListAdapter.this.bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher);
                                    } else {
                                        HelpListAdapter.this.bitmap = bitmap;
                                    }
                                }

                                @Override
                                protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
                                    HelpListAdapter.this.bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher);
                                }
                            })
                            .build();
                }else {
                    HelpListAdapter.this.bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher);
                }
                menuWindow = new MoreButtonPopupWindow(mContext, HelpListAdapter.this, item.helpId, item.getJson().toString());
                //显示窗口
                menuWindow.show();
                boolean isMyShared = false;
                if (user != null && user.userid != null && user.userid.equals(PrfUtils.getUserId(mContext)))
                    isMyShared = true;


                menuWindow.setIsMine(isMyShared);
                if (item.type == 1)
                    menuWindow.setIsCollection(mContext, true);
                else
                    menuWindow.setIsCollection(mContext, false);
                mPosition = position;

            }
        });

        if (helpItem != null) {

            mViewHolder.sharedContent.setVisibility(View.VISIBLE);

            NewUser shareUser = helpItem.getData(NewUser.class);
            String userName = null;
            if (shareUser != null)
                userName = "@" + (TextUtils.isEmpty(shareUser.name) ? "" : shareUser.name) + ":";
            while (helpItem.getData(HelpListItem.class) != null) {
                helpItem = helpItem.getData(HelpListItem.class);
                if (helpItem != null)
                    userName += "@" + (TextUtils.isEmpty(shareUser.name) ? "" : shareUser.name) + ":";
            }

            if (!helpItem.state.equals("2")) {
                mViewHolder.sharedContentText.setText(EmojiFragment.getEmojiContentWithAt(mContext, mViewHolder.sharedContentText.getTextSize(),Html.fromHtml(userName + helpItem.content)));

                final List<ImageInfo> sharedImages = helpItem.getArrayData(ImageInfo.class);
                final List<AudioInfo> sharedAudios = helpItem.getArrayData(AudioInfo.class);

                if (sharedImages != null && sharedImages.size() > 0) {
                    mViewHolder.sharedImagegridview.setVisibility(View.VISIBLE);
                    ImageGridviewAdapter imageGridviewAdapter = new ImageGridviewAdapter(mViewHolder.sharedImagegridview, mContext, sharedImages);
                    mViewHolder.sharedImagegridview.setAdapter(imageGridviewAdapter);
                } else {
                    mViewHolder.sharedImagegridview.setVisibility(View.GONE);
                }

                if (sharedAudios != null && !sharedAudios.isEmpty()) {
                    mViewHolder.sharedvoiceListview.setVisibility(View.VISIBLE);
                    AudioListAdapter audioListAdapter = new AudioListAdapter(mContext, this);
                    audioListAdapter.dataSource.clear();
                    audioListAdapter.dataSource.addAll(sharedAudios);
                    audioListAdapter.notifyDataSetChanged();
                    mViewHolder.sharedvoiceListview.setAdapter(audioListAdapter);
                } else {
                    mViewHolder.sharedvoiceListview.setVisibility(View.GONE);
                }
                final String json = helpItem.getJson().toString();
                mViewHolder.sharedContent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, HelpDetailActivity.class);
                        intent.putExtra("json", json);
                        mContext.startActivity(intent);
                    }
                });

            } else {
                mViewHolder.sharedContentText.setText(mContext.getString(R.string.raw_deleted));

                mViewHolder.sharedvoiceListview.setVisibility(View.GONE);
                mViewHolder.sharedImagegridview.setVisibility(View.GONE);

                mViewHolder.sharedContent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                });
            }

        } else {
            mViewHolder.sharedContent.setVisibility(View.GONE);
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, HelpDetailActivity.class);
                intent.putExtra("json", data.get(position).getJson().toString());
                intent.putExtra("position", position);
                mContext.startActivityForResult(intent, 1);
            }
        });


        return convertView;
    }

    class ViewHolder {
        SimpleDraweeView userHead;
        TextView userName;
        TextView createTime;
        TextView helperContent;

        NoScrollGridview imageGridView;
        NoScrollListview voiceListview;

        TextView replyButton;
        TextView praiseButton;
        TextView moreButton;

        RelativeLayout replyButtonOnclick;
        RelativeLayout praiseButtonClick;
        RelativeLayout moreButtonClick;

        ImageView scheduleListItemPraiseIcon;

        LinearLayout sharedContent;

        TextView sharedContentText;
        NoScrollGridview sharedImagegridview;
        NoScrollListview sharedvoiceListview;

        TextView moreNum;
    }


    private View lastView;

    @Override
    public View getLastView() {
        return lastView;
    }

    @Override
    public void setLastView(View view) {
        lastView = view;
    }


    @Override
    public void collection(String id) {
        collectionThisShared(id);
    }

    @Override
    public void deleted(String id) {
        deleteThisHelp(id);
    }

    @Override
    public void sharedToWeiBo(String id) {
        menuWindow.dismiss();
        String content = initBitmapAndGetDescription();
        shareWeibo(content);
    }

    @Override
    public void sharedToWeiXinSession(String id) {
        String content = initBitmapAndGetDescription();

        HelpListItem item = data.get(mPosition);

        String url = ApiUtils.generatorUrl(mContext, URLAddr.SHARE_URL + "1/" + item.helpId);
        mContext.shareWebPage(SendMessageToWX.Req.WXSceneSession, bitmap, url, mContext.getString(R.string.app_name), item.content);
    }

    @Override
    public void sharedToWeiXin(String id) {

        HelpListItem item = data.get(mPosition);
        String content = initBitmapAndGetDescription();
        String title = content.length() > 20 ? content.substring(0, 17) + "…" : content;
        String url = ApiUtils.generatorUrl(mContext, URLAddr.SHARE_URL + "1/" + item.helpId);
        mContext.shareWebPage(SendMessageToWX.Req.WXSceneTimeline, bitmap, url, mContext.getString(R.string.app_name), content);

    }

    private String initBitmapAndGetDescription() {
        menuWindow.dismiss();
        HelpListItem item = data.get(mPosition);
        return item.content;
    }

    @Override
    public void forwardTo(String id, String json) {
        menuWindow.dismiss();
        Intent intent = new Intent(mContext, NewPublishedActivity.class);
        intent.putExtra(NewPublishedActivity.PUBLISH_TYPE, NewPublishedActivity.PUBLISH_FORWARD);
        intent.putExtra("subtypeId", 1);
        intent.putExtra("forwardId", id);
        intent.putExtra("json", json);
        mContext.startActivity(intent);
    }


    //网络请求
    private void collectionThisShared(String id) {
        menuWindow.dismiss();
        mContext.showLoadingDialog(mContext, mContext.getString(R.string.prompt_info_01));
        mNetworkManager = mContext.getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(mContext));
        params.put("tenantid", PrfUtils.getTenantId(mContext));
        params.put("helpid", id);
        if (data.get(mPosition).type != 1)
            params.put("type", "1");
        else
            params.put("type", "2");

        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(mContext, URLAddr.URL_HELP_COLLECTION), params, mContext);
        mNetworkManager.load(COLLECTION_THIS_HELP, path, this);
    }

    private void deleteThisHelp(String id) {
        menuWindow.dismiss();
        mContext.showLoadingDialog(mContext, "", false);
        mNetworkManager = mContext.getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(mContext));
        params.put("tenantid", PrfUtils.getTenantId(mContext));
        params.put("helpid", id);
        params.put("state", "2");

        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(mContext, URLAddr.URL_HELP_DELETE), params, mContext);
        mNetworkManager.load(DELETE_THIS_HELP, path, this);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        mContext.dismisLoadingDialog();
        boolean mSafe = ActivityUtils.prehandleNetworkData(mContext, this, callbackId, path, rootData, true);
        if (!mSafe) {
            return;
        }
        switch (callbackId) {
            case COLLECTION_THIS_HELP:
                try {
                    switch (data.get(mPosition).type) {
                        case 0:
                        case 2:
                            data.get(mPosition).type = 1;
                            Toast.makeText(mContext, mContext.getString(R.string.shared_collection_success), Toast.LENGTH_SHORT).show();
                            break;
                        case 1:
                            data.get(mPosition).type = 2;
                            Toast.makeText(mContext, mContext.getString(R.string.shared_discollection_success), Toast.LENGTH_SHORT).show();
                            break;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case DELETE_THIS_HELP:
                try {
                    if (rootData.result) {
                        Toast.makeText(mContext, mContext.getString(R.string.shared_delete_success), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.RECEIVER_DRAFT);
                        intent.putExtra("type", PublishTask.PUBLISH_HELP);
                        mContext.sendBroadcast(intent);
                    } else
                        Toast.makeText(mContext, mContext.getString(R.string.shared_delete_fail), Toast.LENGTH_SHORT).show();

                    menuWindow.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }

    public void chaneCommentNum(int position) {
        HelpListItem item = data.get(position);
        int num = item.comments;
        item.comments = num + 1;
        try {
            item.getJson().put("comments", item.comments);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        notifyDataSetChanged();
    }

    public void chaneCommentNum(int position, int commentCount) {
        HelpListItem item = data.get(position);
        item.comments = commentCount;
        try {
            item.getJson().put("comments", item.comments);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        notifyDataSetChanged();
    }

    public void chaneScheduleState(int position, boolean isPraise, int paraiseCount, int type) {
        if (position == -1)
            return;
        HelpListItem item = data.get(position);
        if (type != -1)
            item.type = type;
        item.ispraise = isPraise;
        item.praises = paraiseCount;
        try {
            item.getJson().put("ispraise", item.ispraise);
            item.getJson().put("praises", item.praises);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        notifyDataSetChanged();
    }

    @Override
    public void onResponse(BaseResponse baseResp) {
        switch (baseResp.errCode) {
            case WBConstants.ErrorCode.ERR_OK:
                break;
            case WBConstants.ErrorCode.ERR_CANCEL:
                break;
            case WBConstants.ErrorCode.ERR_FAIL:
                break;
        }
    }

    private IWeiboShareAPI mWeiboShareAPI = null;

    private IWXAPI api;


    public void shareWeibo(String content) {
        AuthInfo mAuthInfo = new AuthInfo(mContext, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE);
        SsoHandler mSsoHandler = new SsoHandler(mContext, mAuthInfo);
        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(mContext, Constants.APP_KEY);


        if (!mWeiboShareAPI.isWeiboAppInstalled() || !mWeiboShareAPI.isWeiboAppSupportAPI()) {
            Toast.makeText(mContext, mContext.getString(R.string.please_install_weibo), Toast.LENGTH_SHORT).show();
            return;
        }

        mWeiboShareAPI.registerApp();
        mWeiboShareAPI.handleWeiboResponse(mContext.getIntent(), this);

        WeiboMessage weiboMessage = new WeiboMessage();

        WebpageObject mediaObject = new WebpageObject();
        mediaObject.identify = Utility.generateGUID();
        mediaObject.title = mContext.getString(R.string.app_name);
        mediaObject.description = content;

        mediaObject.setThumbImage(bitmap);
        HelpListItem item = data.get(mPosition);
        mediaObject.actionUrl = ApiUtils.generatorUrl(mContext, URLAddr.SHARE_URL + "1/" + item.helpId);

        weiboMessage.mediaObject = mediaObject;

        SendMessageToWeiboRequest request = new SendMessageToWeiboRequest();
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.message = weiboMessage;

        mWeiboShareAPI.sendRequest(mContext, request);
    }

    public void cancleNetWork() {
        if (mNetworkManager != null) {

            mNetworkManager.cancle(this);
            mNetworkManager = null;
        }
    }
}
