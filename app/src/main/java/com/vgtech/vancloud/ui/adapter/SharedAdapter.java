package com.vgtech.vancloud.ui.adapter;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.google.gson.Gson;
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
import com.vgtech.common.api.Comment;
import com.vgtech.common.api.ImageInfo;
import com.vgtech.common.api.NewUser;
import com.vgtech.common.api.RootData;
import com.vgtech.common.api.SharedListItem;
import com.vgtech.common.api.UserAccount;
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
import com.vgtech.vancloud.ui.chat.EmojiFragment;
import com.vgtech.vancloud.ui.chat.controllers.PreferencesController;
import com.vgtech.vancloud.ui.common.publish.NewPublishedActivity;
import com.vgtech.vancloud.ui.module.share.ShareActivity;
import com.vgtech.vancloud.ui.module.share.SharedInfoActivity;
import com.vgtech.vancloud.ui.view.MoreButtonPopupWindow;
import com.vgtech.vancloud.utils.EditUtils;
import com.vgtech.vancloud.utils.PublishUtils;
import com.vgtech.common.utils.UserUtils;
import com.vgtech.vancloud.utils.Utils;
import com.vgtech.vancloud.utils.VgTextUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by app02 on 2015/9/10.
 */
public class SharedAdapter extends BaseAdapter implements
        ViewListener, MoreButtonPopupWindow.SharedBottomBar,
        HttpListener<String>,
        IWeiboHandler.Response, View.OnClickListener {
    private final int COLLECTION_THIS_SHARED = 1;
    private final int FORWARD_THIS_SHARED = 2;
    private final int DELETE_THIS_SHARED = 3;

    private NetworkManager mNetworkManager;

    private ShareActivity mContext;
    private List<SharedListItem> data;

    private MoreButtonPopupWindow menuWindow;

    private int mPosition;

    private Bitmap bitmap;
    private View mAddView;

    private int mMaxWidth;

    public SharedAdapter(ShareActivity mContext, List<SharedListItem> data, View v) {
        this.mContext = mContext;
        this.data = data;
        mAddView = v;
        int width = mContext.getResources().getDisplayMetrics().widthPixels;
        mMaxWidth = width - Utils.convertDipOrPx(mContext, 110);
    }


    public List<SharedListItem> getData() {
        return data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public SharedListItem getItem(int position) {
        return data.get(position);
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.share_item, null);

            mViewHolder.userHead = (SimpleDraweeView) convertView.findViewById(R.id.user_photo);
            mViewHolder.userName = (TextView) convertView.findViewById(R.id.user_name);
            mViewHolder.createTime = (TextView) convertView.findViewById(R.id.timestamp);
            mViewHolder.helperContent = (TextView) convertView.findViewById(R.id.content_text);
            mViewHolder.tv_all_comment = (TextView) convertView.findViewById(R.id.tv_all_comment);
            mViewHolder.praise_view = convertView.findViewById(R.id.praise_view);
            mViewHolder.tv_goods = (TextView) convertView.findViewById(R.id.tv_goods);
            mViewHolder.comment_view = convertView.findViewById(R.id.comment_view);
//            mViewHolder.comment_view.setOnClickListener(this);
            mViewHolder.comment_line = convertView.findViewById(R.id.comment_line);

            EditUtils.SetTextViewMaxLines(mViewHolder.helperContent, 5);

            mViewHolder.imageGridView = (NoScrollGridview) convertView.findViewById(R.id.imagegridview);
            mViewHolder.voiceListview = (NoScrollListview) convertView.findViewById(R.id.voice_listview);
            mViewHolder.comment_list = (NoScrollListview) convertView.findViewById(R.id.comment_list);
            mViewHolder.sharedContent = (LinearLayout) convertView.findViewById(R.id.forward_view);
            mViewHolder.sharedContentText = (TextView) convertView.findViewById(R.id.forward_text);
            mViewHolder.btn_share_action = convertView.findViewById(R.id.btn_share_action);
            EditUtils.SetTextViewMaxLines(mViewHolder.sharedContentText, 5);
            mViewHolder.sharedImagegridview = (NoScrollGridview) convertView.findViewById(R.id.forward_image);

            AudioListAdapter audioListAdapter = new AudioListAdapter(mContext, this);
            mViewHolder.sharedvoiceListview = (NoScrollListview) convertView.findViewById(R.id.forward_audio);
            mViewHolder.sharedvoiceListview.setAdapter(audioListAdapter);

            mViewHolder.locate_text = (TextView) convertView.findViewById(R.id.locate_text);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        final SharedListItem item = data.get(position);
        final NewUser user = item.getData(NewUser.class);//创建人
        SharedListItem sharedItem = item.getData(SharedListItem.class);//转发
        final List<ImageInfo> imags = item.getArrayData(ImageInfo.class);
        final List<AudioInfo> audios = item.getArrayData(AudioInfo.class);
        if (imags != null && imags.size() > 0) {
            mViewHolder.imageGridView.setVisibility(View.VISIBLE);
            ImageGridviewAdapter imageGridviewAdapter = new ImageGridviewAdapter(mViewHolder.imageGridView, mContext, imags, Utils.dp2px(mContext, 60));
            mViewHolder.imageGridView.setAdapter(imageGridviewAdapter);
        } else {
            mViewHolder.imageGridView.setVisibility(View.GONE);
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
        mViewHolder.btn_share_action.setOnClickListener(this);
        mViewHolder.btn_share_action.setTag(item);
        mViewHolder.btn_share_action.setTag(R.string.app_name, position);
        if (user != null) {
            ImageOptions.setUserImage(mViewHolder.userHead, user.photo);
            mViewHolder.userName.setText(Html.fromHtml(user.name));
        }
        if (user != null)
            UserUtils.enterUserInfo(mContext, user.userid, user.name, user.photo, mViewHolder.userHead);
        else {
            NewUser u = new NewUser();
            try {
                item.getJson().put("user", u);
                UserUtils.enterUserInfo(mContext, "", "", "", mViewHolder.userHead);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        List<Comment> comments = item.getArrayData(Comment.class);
        if (item.comments > 0 && !comments.isEmpty()) {
            mViewHolder.comment_list.setVisibility(View.VISIBLE);
            ApiDataAdapter<Comment> commentApiDataAdapter = new ApiDataAdapter<>(mContext);
            commentApiDataAdapter.setSharedItem(item);
            commentApiDataAdapter.setPosition(position);
            commentApiDataAdapter.add(comments);
            mViewHolder.comment_list.setAdapter(commentApiDataAdapter);
            mViewHolder.tv_all_comment.setVisibility(View.VISIBLE);
            mViewHolder.comment_line.setVisibility(View.VISIBLE);
            if (item.comments > comments.size())
                mViewHolder.tv_all_comment.setText(mContext.getString(R.string.lable_comments_all, item.comments));
            else
                mViewHolder.tv_all_comment.setVisibility(View.GONE);
        } else {
            mViewHolder.comment_line.setVisibility(View.GONE);
            mViewHolder.comment_list.setVisibility(View.GONE);
            mViewHolder.tv_all_comment.setVisibility(View.GONE);
        }
        mViewHolder.comment_view.setTag(item);
        mViewHolder.comment_view.setTag(R.string.app_name, position);
        try {
            if (item.getJson().has("prise_list")) {
                JSONArray jsonArray = item.getJson().getJSONArray("prise_list");
                if (jsonArray.length() > 0) {
                    mViewHolder.comment_view.setVisibility(View.VISIBLE);
                    if (!comments.isEmpty()) {
                        mViewHolder.comment_line.setVisibility(View.VISIBLE);
                    }
                    StringBuffer stringBuffer = new StringBuffer();
                    List<String> nameList = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        if (i != 0) {
                            stringBuffer.append(",");
                        }
                        StringBuffer buffer = new StringBuffer(stringBuffer.toString());
                        buffer.append(mContext.getString(R.string.lable_praise_count, jsonArray.length()));
                        int textWidth = VgTextUtils.getTextWidth(mContext, 15, buffer.toString());
                        if (textWidth < mMaxWidth) {
                            String name = jsonArray.getString(i);
                            stringBuffer.append(name);
                            nameList.add(name);
                        } else {
                            nameList.remove(nameList.size() - 1);
                            break;
                        }
                    }
                    StringBuffer nameBuffer = new StringBuffer();
                    for (int i = 0; i < nameList.size(); i++) {
                        if (i != 0) {
                            nameBuffer.append("<font color=\"#929292\">,</font>");
                        }
                        nameBuffer.append(nameList.get(i));
                    }
                    if (nameList.size() < jsonArray.length()) {
                        nameBuffer.append(mContext.getString(R.string.lable_praise_count, jsonArray.length()));
                    }
                    mViewHolder.tv_goods.setText(Html.fromHtml(nameBuffer.toString()));


                    mViewHolder.praise_view.setVisibility(View.VISIBLE);
                } else {
                    if (comments.isEmpty()) {
                        mViewHolder.comment_view.setVisibility(View.GONE);
                    } else {
                        mViewHolder.comment_view.setVisibility(View.VISIBLE);
                    }

                    mViewHolder.praise_view.setVisibility(View.GONE);
                    mViewHolder.comment_line.setVisibility(View.GONE);
                }
            } else {
                mViewHolder.comment_view.setVisibility(View.GONE);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (item != null) {
            mViewHolder.createTime.setText(Utils.getInstance(mContext).dateFormat(Long.parseLong(item.timestamp)));
            mViewHolder.helperContent.setText(EmojiFragment.getEmojiContentWithAt(mContext, mViewHolder.helperContent.getTextSize(),Html.fromHtml(item.content)));
            if (!TextUtils.isEmpty(item.address)) {
                mViewHolder.locate_text.setVisibility(View.VISIBLE);
                mViewHolder.locate_text.setText(item.address);
            } else
                mViewHolder.locate_text.setVisibility(View.GONE);
        }
        if (sharedItem != null) {
            mViewHolder.sharedContent.setVisibility(View.VISIBLE);
            NewUser fwUser = sharedItem.getData(NewUser.class);
            String userName = "";
            if (fwUser != null) {
                userName = "@" + fwUser.name + ":";
            }
            while (sharedItem.getData(SharedListItem.class) != null) {
                sharedItem = sharedItem.getData(SharedListItem.class);
                NewUser sharedUser = sharedItem.getData(NewUser.class);
                if (sharedUser != null)
                    userName += "@" + sharedUser.name + ":";
            }


            if (!sharedItem.state.equals("2")) {
                mViewHolder.sharedContentText.setText(EmojiFragment.getEmojiContentWithAt(mContext, mViewHolder.sharedContentText.getTextSize(),Html.fromHtml(userName + sharedItem.content)));

                final List<ImageInfo> sharedImages = sharedItem.getArrayData(ImageInfo.class);
                final List<AudioInfo> sharedAudios = sharedItem.getArrayData(AudioInfo.class);

                if (sharedImages != null && sharedImages.size() > 0) {
                    mViewHolder.sharedImagegridview.setVisibility(View.VISIBLE);
                    ImageGridviewAdapter imageGridviewAdapter = new ImageGridviewAdapter(mViewHolder.sharedImagegridview, mContext, sharedImages, Utils.dp2px(mContext, 70));
                    mViewHolder.sharedImagegridview.setAdapter(imageGridviewAdapter);
                } else {
                    mViewHolder.sharedImagegridview.setVisibility(View.GONE);
                }

                AudioListAdapter audioListAdapter = (AudioListAdapter) mViewHolder.sharedvoiceListview.getAdapter();
                if (sharedAudios != null && !sharedAudios.isEmpty()) {
                    mViewHolder.sharedvoiceListview.setVisibility(View.VISIBLE);
                    audioListAdapter.dataSource.clear();
                    audioListAdapter.dataSource.addAll(sharedAudios);
                    audioListAdapter.notifyDataSetChanged();
                    mViewHolder.sharedvoiceListview.setAdapter(audioListAdapter);
                } else {
                    mViewHolder.sharedvoiceListview.setVisibility(View.GONE);

                }
                final String json = sharedItem.getJson().toString();
                mViewHolder.sharedContent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, SharedInfoActivity.class);
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
                Intent intent = new Intent(mContext, SharedInfoActivity.class);
                intent.putExtra("json", data.get(position).getJson().toString());
                intent.putExtra("position", position);
                mContext.startActivityForResult(intent, 400);
            }
        });

        return convertView;
    }

    private SharedListItem mShareItem;
    private int mSharePosition;
    private PopupWindow mActionPopView;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void showActionMenu(View v) {
        mShareItem = (SharedListItem) v.getTag();
        mSharePosition = (int) v.getTag(R.string.app_name);
        View view = LayoutInflater.from(mContext).inflate(R.layout.share_action, null);
        TextView ivPraise = (TextView) view.findViewById(R.id.iv_praise);
        if (mShareItem.ispraise) {
            ivPraise.setText(mContext.getString(R.string.cancel));
        } else {
            ivPraise.setText(mContext.getString(R.string.praise));
        }
        mActionPopView = new PopupWindow(view, Utils.convertDipOrPx(mContext, 187),
                ViewGroup.LayoutParams.WRAP_CONTENT);// 创建一个PopuWidow对象
        mActionPopView.setFocusable(true);// 使其聚集
        mActionPopView.setOutsideTouchable(true);// 设置允许在外点击消失
      /*  mActionPopView.setBackgroundDrawable(mContext.getResources().getDrawable(
                R.drawable.bg_share_action));// 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景*/
        mActionPopView.setBackgroundDrawable(new ColorDrawable(mContext.getResources().getColor(R.color.transparent)));
        mActionPopView.setAnimationStyle(R.style.Animation_PopupWindow);
        view.findViewById(R.id.btn_action_comment).setOnClickListener(this);
        view.findViewById(R.id.btn_action_praise).setOnClickListener(this);
        view.findViewById(R.id.btn_action_more).setOnClickListener(this);
        mActionPopView.update();
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= 19) {
            mActionPopView.showAsDropDown(v, 0 - Utils.convertDipOrPx(mContext, 187), 0 - Utils.convertDipOrPx(mContext, 25), Gravity.LEFT);
        } else {
            mActionPopView.showAsDropDown(v, 0 - Utils.convertDipOrPx(mContext, 187), 0 - Utils.convertDipOrPx(mContext, 25));
        }

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_share_action:
                showActionMenu(v);
                break;
            case R.id.comment_view:
                mShareItem = (SharedListItem) v.getTag();
                mSharePosition = (int) v.getTag(R.string.app_name);
                Intent intent = new Intent(mContext, SharedInfoActivity.class);
                intent.putExtra("json", mShareItem.getJson().toString());
                intent.putExtra("position", mSharePosition);
                intent.putExtra("showcomment", true);
                mContext.startActivityForResult(intent, 400);
                break;
            case R.id.btn_action_comment: {
                if (mActionPopView != null && mActionPopView.isShowing())
                    mActionPopView.dismiss();
                PublishUtils.addComment(mContext, PublishUtils.COMMENTTYPE_SHARE, mShareItem.topicId + "", mSharePosition);
            }
            break;
            case R.id.btn_action_praise: {
                if (mActionPopView != null && mActionPopView.isShowing())
                    mActionPopView.dismiss();
                PublishUtils.toDig(mContext, mShareItem.topicId + "", PublishUtils.COMMENTTYPE_SHARE, mShareItem.ispraise, new PublishUtils.DigCallBack() {

                    @Override
                    public void successful(boolean digType) {
                        PreferencesController preferencesController = new PreferencesController();
                        preferencesController.context = mContext;
                        UserAccount userAccount = preferencesController.getAccount();
                        if (digType) {
                            try {
                                if (mShareItem.getJson().has("prise_list")) {
                                    JSONArray jsonArray = mShareItem.getJson().getJSONArray("prise_list");
                                    List<String> nameList = new ArrayList<String>(jsonArray.length());
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        String name = jsonArray.getString(i);
                                        if (!name.equals(userAccount.nickname())) {
                                            nameList.add(name);
                                        }
                                    }
                                    Gson gson = new Gson();
                                    String praiseUser = gson.toJson(nameList);
                                    jsonArray = new JSONArray(praiseUser);
                                    mShareItem.getJson().put("prise_list", jsonArray);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            mShareItem.praises -= mShareItem.praises > 0 ? 1 : 0;
                        } else {
                            try {
                                if (mShareItem.getJson().has("prise_list")) {
                                    JSONArray allArry = new JSONArray();
                                    allArry.put(userAccount.nickname());
                                    JSONArray jsonArray = mShareItem.getJson().getJSONArray("prise_list");
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        allArry.put(jsonArray.getString(i));
                                    }
                                    mShareItem.getJson().put("prise_list", allArry);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            mShareItem.praises += 1;
                        }
                        mShareItem.ispraise = !digType;
                        try {
                            mShareItem.getJson().put("praises", mShareItem.praises);
                        } catch (Exception e) {
                        }
                        try {
                            mShareItem.getJson().put("ispraise", !digType);
                        } catch (Exception e) {
                        }
                        notifyDataSetChanged();
                    }
                });
            }
            break;
            case R.id.btn_action_more: {
                if (mActionPopView != null && mActionPopView.isShowing())
                    mActionPopView.dismiss();
                if (menuWindow != null && menuWindow.isShowing()) {
                    menuWindow.dismiss();
                    return;
                }
                mPosition = mSharePosition;
                final List<ImageInfo> imags = mShareItem.getArrayData(ImageInfo.class);
                if (imags != null && imags.size() > 0) {
                    new ImageLoadFresco.LoadImageFrescoBuilder(mContext, new SimpleDraweeView(mContext), imags.get(0).thumb)
                            .setBitmapDataSubscriber(new BaseBitmapDataSubscriber() {
                                @Override
                                protected void onNewResultImpl(Bitmap bitmap) {
                                    if (bitmap == null) {
                                        SharedAdapter.this.bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher);
                                    } else {
                                        SharedAdapter.this.bitmap = bitmap;
                                    }
                                }

                                @Override
                                protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
                                    SharedAdapter.this.bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher);
                                }
                            })
                            .build();
                } else
                    bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher);

                menuWindow = new MoreButtonPopupWindow(mContext, SharedAdapter.this, mShareItem.topicId, mShareItem.getJson().toString());
                menuWindow.show();

                boolean isMyShared = false;
                try {
                    final NewUser user = mShareItem.getData(NewUser.class);//创建人
                    if (user.userid.equals(PrfUtils.getUserId(mContext)))
                        isMyShared = true;
                } catch (Exception e) {
                }

                menuWindow.setIsMine(isMyShared);
                if (mShareItem.type == 1)
                    menuWindow.setIsCollection(mContext, true);
                else
                    menuWindow.setIsCollection(mContext, false);
            }
            break;
        }
    }

    class ViewHolder {
        SimpleDraweeView userHead;
        TextView userName;
        TextView createTime;
        TextView helperContent;
        NoScrollGridview imageGridView;
        NoScrollListview voiceListview;
        LinearLayout sharedContent;
        TextView sharedContentText;
        View btn_share_action;
        View comment_line;
        NoScrollGridview sharedImagegridview;
        NoScrollListview sharedvoiceListview;
        NoScrollListview comment_list;
        TextView locate_text;
        TextView tv_all_comment;
        View praise_view;
        View comment_view;
        TextView tv_goods;

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
        deleteThisShared(id);

    }

    @Override
    public void sharedToWeiBo(String id) {
        menuWindow.dismiss();
        shareWeibo();
    }

    @Override
    public void sharedToWeiXinSession(String id) {
        menuWindow.dismiss();
        SharedListItem item = data.get(mPosition);

        HashMap<String, String> obj = new HashMap<String, String>();
        obj.put("resId", id);
        obj.put("megTypeId", PublishUtils.COMMENTTYPE_SHARE + "");
        mContext.shareWebPage(SendMessageToWX.Req.WXSceneSession, bitmap, ApiUtils.generatorUrl(mContext, URLAddr.SHARE_URL + "2/" + item.topicId), mContext.getString(R.string.app_name), item.content);
    }

    @Override
    public void sharedToWeiXin(String id) {
        menuWindow.dismiss();
        SharedListItem item = data.get(mPosition);
        mContext.shareWebPage(SendMessageToWX.Req.WXSceneTimeline, bitmap,
                ApiUtils.generatorUrl(mContext, URLAddr.SHARE_URL + "2/" + item.topicId), mContext.getString(R.string.app_name), item.content);
    }

    @Override
    public void forwardTo(String id, String json) {
        menuWindow.dismiss();
        Intent intent = new Intent(mContext, NewPublishedActivity.class);
        intent.putExtra(NewPublishedActivity.PUBLISH_TYPE, NewPublishedActivity.PUBLISH_FORWARD);
        intent.putExtra("subtypeId", 2);
        intent.putExtra("forwardId", id);
        intent.putExtra("json", json);
        mContext.startActivity(intent);
    }

    //网络请求
    private void collectionThisShared(String id) {
        mContext.showLoadingDialog(mContext, mContext.getString(R.string.prompt_info_01));
        menuWindow.dismiss();
        mNetworkManager = mContext.getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(mContext));
        params.put("tenantid", PrfUtils.getTenantId(mContext));
        params.put("topicid", id);
        if (data.get(mPosition).type != 1)
            params.put("type", "1");
        else
            params.put("type", "2");

        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(mContext, URLAddr.URL_SHARED_COLLECTION), params, mContext);
        mNetworkManager.load(COLLECTION_THIS_SHARED, path, this);
    }

    private void deleteThisShared(String id) {
        menuWindow.dismiss();
        mContext.showLoadingDialog(mContext, "", false);
        mNetworkManager = mContext.getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(mContext));
        params.put("tenantid", PrfUtils.getTenantId(mContext));
        params.put("topicid", id);
        params.put("state", "2");

        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(mContext, URLAddr.URL_SHARED_REMOVE), params, mContext);
        mNetworkManager.load(DELETE_THIS_SHARED, path, this);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        mContext.dismisLoadingDialog();
        boolean mSafe = ActivityUtils.prehandleNetworkData(mContext, this, callbackId, path, rootData, true);
        if (!mSafe) {
            return;
        }
        switch (callbackId) {
            case COLLECTION_THIS_SHARED:
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
            case FORWARD_THIS_SHARED:
                try {
                    menuWindow.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case DELETE_THIS_SHARED:
                try {
                    if (rootData.result) {
                        Toast.makeText(mContext, mContext.getString(R.string.shared_delete_success), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.RECEIVER_DRAFT);
                        intent.putExtra("type", PublishTask.PUBLISH_SHARED);
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
        SharedListItem item = data.get(position);
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
        SharedListItem item = data.get(position);
        item.comments = commentCount;
        try {
            item.getJson().put("comments", item.comments);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        notifyDataSetChanged();
    }

    public SharedListItem getSharedItem(int position) {
        if (position < data.size())
            return data.get(position);
        else return null;
    }

    public void changeCollection(int position, int type) {
        SharedListItem item = data.get(position);
        item.type = type;
        try {
            item.getJson().put("type", item.type);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        notifyDataSetChanged();
    }

    public void chaneScheduleState(int position, boolean isPraise, int paraiseCount) {
        if (position == -1)
            return;
        SharedListItem item = data.get(position);
        item.ispraise = isPraise;
        item.praises = paraiseCount;
        try {
            item.getJson().put("ispraise", item.ispraise);
            item.getJson().put("praises", item.praises);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private IWXAPI api;

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

    public void shareWeibo() {
        AuthInfo mAuthInfo = new AuthInfo(mContext, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE);
        SsoHandler mSsoHandler = new SsoHandler(mContext, mAuthInfo);
        // 创建微博分享接口实例
        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(mContext, Constants.APP_KEY);

        if (!mWeiboShareAPI.isWeiboAppInstalled() || !mWeiboShareAPI.isWeiboAppSupportAPI()) {
            Toast.makeText(mContext, mContext.getString(R.string.please_install_weibo), Toast.LENGTH_SHORT).show();
            return;
        }

        mWeiboShareAPI.registerApp();
        mWeiboShareAPI.handleWeiboResponse(mContext.getIntent(), this);

        WeiboMessage weiboMessage = new WeiboMessage();

        SharedListItem item = data.get(mPosition);

        WebpageObject mediaObject = new WebpageObject();
        mediaObject.identify = Utility.generateGUID();
        mediaObject.title = mContext.getString(R.string.app_name);
        mediaObject.description = item.content == null ? "" : item.content;

        mediaObject.setThumbImage(bitmap);
        mediaObject.actionUrl = ApiUtils.generatorUrl(mContext, URLAddr.SHARE_URL + "2/" + item.topicId);

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
