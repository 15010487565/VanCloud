package com.vgtech.vancloud.ui.module.announcement;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.view.View;
import android.view.ViewStub;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.AnnounceNotify;
import com.vgtech.common.api.AttachFile;
import com.vgtech.common.api.AudioInfo;
import com.vgtech.common.api.ImageInfo;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.NewUser;
import com.vgtech.common.api.RootData;
import com.vgtech.common.image.ImageGridviewAdapter;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.provider.db.PublishTask;
import com.vgtech.common.view.NoScrollGridview;
import com.vgtech.common.view.NoScrollListview;
import com.vgtech.common.view.TabComPraiseIndicator;
import com.vgtech.common.view.TabInfo;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.MainActivity;
import com.vgtech.vancloud.ui.adapter.AudioListAdapter;
import com.vgtech.vancloud.ui.adapter.FileListAdapter;
import com.vgtech.vancloud.ui.adapter.MyFragmentPagerAdapter;
import com.vgtech.vancloud.ui.adapter.ViewListener;
import com.vgtech.vancloud.ui.common.CountListener;
import com.vgtech.vancloud.ui.common.commentandpraise.CommentListFragment;
import com.vgtech.vancloud.ui.common.commentandpraise.PraiseListFragment;
import com.vgtech.vancloud.ui.register.utils.TextUtil;
import com.vgtech.vancloud.ui.view.scrollablelayoutlib.ScrollableHelper;
import com.vgtech.vancloud.ui.view.scrollablelayoutlib.ScrollableLayout;
import com.vgtech.vancloud.utils.EditUtils;
import com.vgtech.vancloud.utils.PublishUtils;
import com.vgtech.vancloud.utils.Utils;
import com.vgtech.vancloud.utils.VgTextUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by app02 on 2015/9/9.
 */
public class AnnouncementDetailActivity extends BaseActivity implements HttpListener<String>, ViewListener, CountListener, AbsListView.OnScrollListener {

    private final int ANNOUNCEMENT_INFO = 3453;
    private final int ANNOUNCEMENT_STATE = 342;
    private NetworkManager mNetworkManager;

    private AnnounceNotify data;
    private TextView announcementDetailTitle;
    private TextView creater;
    private TextView createTime;
//    private TextView announcementDetailContent;
    private WebView wvAnnouncementDetaiContent;
    private NoScrollListview attachementList;
    private NoScrollGridview imagegridview;
    private NoScrollListview voiceListview;

    private ImageView praiseIcon;
    private TextView replyButton;
    private TextView praiseButton;

    private RelativeLayout replyButtonOnclick;
    private RelativeLayout praiseButtonClick;

    private int times;
    private int position;
    private int commentCount;
    private boolean isShowCommment;

    private VancloudLoadingLayout LoadingView;
    private LinearLayout dataInfoLayout;
    private boolean fromeNotice;

    private boolean isRefresh = false;
    private ScrollableLayout mScrollLayout;
    private ViewPager mViewPager;
    private TabComPraiseIndicator mTitleIndicator;


    boolean init = true;

    @Override
    protected int getContentView() {
        return R.layout.app_detail;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.announcement_detail_title));
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.RECEIVER_DRAFT);
        registerReceiver(mReceiver, intentFilter);
        Intent intent = getIntent();
        position = intent.getExtras().getInt("position");
        String json = intent.getStringExtra("json");
        final String id = intent.getStringExtra("id");
        fromeNotice = intent.getBooleanExtra("fromeNotice", false);
        //  mHeaderActionView = findViewById(R.id.header_action);
        isShowCommment = intent.getBooleanExtra("showcomment", false);

        ViewStub viewStub = (ViewStub) findViewById(R.id.action_announcement);
        viewStub.inflate();
        //底部评论和点赞
        LinearLayout lldetail = (LinearLayout) findViewById(R.id.ll_detail);
        boolean isShowNotice = intent.getBooleanExtra("isShowNotice", false);
        if (isShowNotice){
            lldetail.setVisibility(View.VISIBLE);
        }else {
            lldetail.setVisibility(View.GONE);
        }

        ViewStub detail_announcement = (ViewStub) findViewById(R.id.detail_announcement);
        detail_announcement.inflate();
        initView();
        if (!TextUtil.isEmpty(json)) {
            try {
                data = JsonDataFactory.getData(AnnounceNotify.class, new JSONObject(json));
                addCommentFragment();
                initData();
                init = false;
            } catch (Exception e) {
            }
        }
        loadAnnouncementInfo(data == null ? id : data.notifyid, init);

        LoadingView.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
            @Override
            public void loadAgain() {
                loadAnnouncementInfo(data == null ? id : data.notifyid, init);
            }
        });
    }

    private void initView() {
        mTitleIndicator = (TabComPraiseIndicator) findViewById(R.id.title_indicator);
        mScrollLayout = (ScrollableLayout) findViewById(R.id.scrollableLayout);
        mScrollLayout.setHeaderIndex(4);
        mScrollLayout.scrollToBar(isShowCommment);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        announcementDetailTitle = (TextView) findViewById(R.id.announcement_detail_title);
        creater = (TextView) findViewById(R.id.creater);
        createTime = (TextView) findViewById(R.id.create_time);
//        announcementDetailContent = (TextView) findViewById(R.id.announcement_detail_content);
        wvAnnouncementDetaiContent = (WebView) findViewById(R.id.wv_AnnouncementDetaiContent);
        wvAnnouncementDetaiContent.setVerticalScrollBarEnabled(false);
        wvAnnouncementDetaiContent.setHorizontalScrollBarEnabled(false);

        replyButton = (TextView) findViewById(R.id.reply_button);
        praiseButton = (TextView) findViewById(R.id.praise_button);

        praiseIcon = (ImageView) findViewById(R.id.img02);
        replyButtonOnclick = (RelativeLayout) findViewById(R.id.reply_button_onclick);
        praiseButtonClick = (RelativeLayout) findViewById(R.id.praise_button_click);

        attachementList = (NoScrollListview) findViewById(R.id.attachement_list);
        imagegridview = (NoScrollGridview) findViewById(R.id.imagegridview);
        voiceListview = (NoScrollListview) findViewById(R.id.voice_listview);

        LoadingView = (VancloudLoadingLayout) findViewById(R.id.load_view);
        dataInfoLayout = (LinearLayout) findViewById(R.id.info);
    }

    private void initData() {
        if (data == null)
            return;
        setListener();
        NewUser user = data.getData(NewUser.class);

        announcementDetailTitle.setText(data.title);
        creater.setText(user.name);
        createTime.setText(Utils.getInstance(this).dateFormat(data.timestamp));

//        announcementDetailContent.setText(VgTextUtils.textViewSpan(this, Html.fromHtml(getHtmlData(data.content)), announcementDetailContent,
//                (int) announcementDetailContent.getTextSize(), true, false));
        getHtmlData(data.content,wvAnnouncementDetaiContent);

        replyButton.setText(R.string.detail_reply);
        praiseButton.setText(R.string.detail_praise);

        commentCount = data.comments;
        if (data.ispraise) {
            praiseIcon.setImageResource(R.drawable.item_praise_click_red);
            praiseButton.setTextColor(EditUtils.redCreateColorStateList());
        } else {
            praiseIcon.setImageResource(R.drawable.item_praise_click);
            praiseButton.setTextColor(EditUtils.greyCreateColorStateList());
        }

        praiseButtonClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PublishUtils.toDig(AnnouncementDetailActivity.this, data.notifyid + "", PublishUtils.COMMENTTYPE_ANNOUNCEMENT, data.ispraise, new PublishUtils.DigCallBack() {

                    @Override
                    public void successful(boolean digType) {
                        if (digType)
                            data.praises -= data.praises > 0 ? 1 : 0;
                        else
                            data.praises += 1;
                        data.ispraise = !digType;
                        try {
                            data.getJson().put("praises", data.praises);
                        } catch (Exception e) {
                        }
                        try {
                            data.getJson().put("ispraise", data.ispraise);
                        } catch (Exception e) {
                        }
                        initData();
                        mTitleIndicator.updateTitle(1, getString(R.string.praise) + " " + data.praises);
                        mPriseFragment.refresh();
                        isRefresh = true;
                    }
                });
            }
        });

        List<ImageInfo> imags = data.getArrayData(ImageInfo.class);
        List<AudioInfo> audios = data.getArrayData(AudioInfo.class);
        List<AttachFile> files = data.getArrayData(AttachFile.class);
        if (imags != null && imags.size() > 0) {
            imagegridview.setVisibility(View.VISIBLE);
            ImageGridviewAdapter imageGridviewAdapter = new ImageGridviewAdapter(imagegridview, this, imags, null);
            imagegridview.setAdapter(imageGridviewAdapter);
        } else {
            imagegridview.setVisibility(View.GONE);
        }


        if (audios != null && !audios.isEmpty()) {
            voiceListview.setVisibility(View.VISIBLE);
            AudioListAdapter audioListAdapter = new AudioListAdapter(this, this);
            audioListAdapter.dataSource.clear();
            audioListAdapter.dataSource.addAll(audios);
            audioListAdapter.notifyDataSetChanged();
            voiceListview.setAdapter(audioListAdapter);
        } else {
            voiceListview.setVisibility(View.GONE);

        }

        if (files != null && !files.isEmpty()) {
            attachementList.setVisibility(View.VISIBLE);
            FileListAdapter fileListAdapter = new FileListAdapter(this, this);
            fileListAdapter.dataSource.clear();
            fileListAdapter.dataSource.addAll(files);
            attachementList.setAdapter(fileListAdapter);
            fileListAdapter.notifyDataSetChanged();

        } else
            attachementList.setVisibility(View.GONE);
    }

    public void getHtmlData(String bodyHTML, WebView webView) {
        String head = "<head>" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\"> " +
                "<style>img{max-width: 100%; width:auto; height:auto;}</style>" +
                "</head>";
        webView.loadData("<html>" + head + "<body>" + bodyHTML + "</body></html>", "text/html; charset=UTF-8", null);
    }

    private void setListener() {
        replyButtonOnclick.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.reply_button_onclick:
                PublishUtils.addComment(this, PublishUtils.COMMENTTYPE_ANNOUNCEMENT, data.notifyid);
                break;
            case R.id.praise_button_click:
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    //网络请求
    private void loadAnnouncementInfo(String id, boolean init) {
        if (init) {
            LoadingView.showLoadingView(dataInfoLayout, "", true);
        }
        mNetworkManager = getAppliction().getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("tenantid", PrfUtils.getTenantId(this));
        params.put("ownid", PrfUtils.getUserId(this));
        params.put("notifyid", id);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_ANNOUNCEMENT_DETAIL), params, this);
        mNetworkManager.load(ANNOUNCEMENT_INFO, path, this, true);
    }

    private void changeState() {
//        mNetworkManager = getAppliction().getNetworkManager();
//        Map<String, String> params = new HashMap<String, String>();
//        params.put("tenantid", PrfUtils.getTenantId(this));
//        params.put("ownid", PrfUtils.getUserId(this));
//        params.put("noticeccid", data.notifyid + "");
//        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_UPDATE_READ_STATE), params,this);
//        mNetworkManager.load(ANNOUNCEMENT_STATE, path, this);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
//        LoadingView.dismiss(dataInfoLayout);
        boolean mSafe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!mSafe) {
            switch (callbackId) {
                case ANNOUNCEMENT_INFO:
                    if (dataInfoLayout.getVisibility() != View.VISIBLE) {
                        LoadingView.showErrorView(dataInfoLayout);
                    }
                    break;
            }
            return;
        }
        switch (callbackId) {
            case ANNOUNCEMENT_INFO:
                LoadingView.setVisibility(View.GONE);
                dataInfoLayout.setVisibility(View.VISIBLE);
                try {
                    JSONObject jsonObject = rootData.getJson().getJSONObject("data");
                    data = JsonDataFactory.getData(AnnounceNotify.class, jsonObject);
                    initData();
                    addCommentFragment();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case ANNOUNCEMENT_STATE:
                try {
                    if (!rootData.result && times < 5) {
                        Thread.sleep(1000);
                        changeState();
                        times++;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        error.printStackTrace();
    }

    @Override
    public void onResponse(String response) {

    }

    private View mView;

    @Override
    public View getLastView() {
        return mView;
    }

    @Override
    public void setLastView(View view) {
        mView = view;

    }


    private void addCommentFragment() {
        if (!mInit) {
            initFragmentPager(mViewPager, mScrollLayout);
        }
    }

    private CommentListFragment mCommentFragment;
    private PraiseListFragment mPriseFragment;
    private boolean mInit;

    public void initFragmentPager(final ViewPager viewPager, final ScrollableLayout mScrollLayout) {
        mInit = true;
        viewPager.postDelayed(new Runnable() {
            @Override
            public void run() {
                viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                        if (position == 0) {
                            if (mPriseFragment.getScrollableView().getFirstVisiblePosition() < 2) ;
                            mCommentFragment.getScrollableView().setSelection(0);
                            mScrollLayout.getHelper().setCurrentScrollableContainer(mCommentFragment);
                        } else if (position == 1) {
                            if (mCommentFragment.getScrollableView().getFirstVisiblePosition() < 2)
                                ;
                            mPriseFragment.getScrollableView().setSelection(0);
                            mScrollLayout.getHelper().setCurrentScrollableContainer(mPriseFragment);
                        }
                        mTitleIndicator.onScrolled((mViewPager.getWidth() + mViewPager.getPageMargin()) * position + positionOffsetPixels);
                    }

                    @Override
                    public void onPageSelected(int position) {
                        mTitleIndicator.onSwitched(position);
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {

                    }
                });
            }
        }, 1000);
        final ArrayList<Fragment> fragmentList = new ArrayList<>();
        mCommentFragment = CommentListFragment.create(PublishUtils.COMMENTTYPE_ANNOUNCEMENT, data.notifyid);
        mPriseFragment = PraiseListFragment.create(PublishUtils.COMMENTTYPE_ANNOUNCEMENT, data.notifyid);
        fragmentList.add(mCommentFragment);
        fragmentList.add(mPriseFragment);
        viewPager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager(), fragmentList));
        mScrollLayout.getHelper().setCurrentScrollableContainer((ScrollableHelper.ScrollableContainer) fragmentList.get(0));
        viewPager.setCurrentItem(0);
        List<TabInfo> tabs = new ArrayList<>();
        tabs.add(new TabInfo(0, getString(R.string.comment) + " " + data.comments,
                CommentListFragment.class));
        tabs.add(new TabInfo(1, getString(R.string.praise) + " " + data.praises,
                PraiseListFragment.class));
        mTitleIndicator.init(0, tabs, mViewPager);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MainActivity.RECEIVER_DRAFT.equals(action)) {
                int receiverType = intent.getIntExtra("type", 0);
                switch (receiverType) {
                    case PublishTask.PUBLISH_COMMENT:
                        mCommentFragment.refresh();
                        isRefresh = true;
                        break;
                }
            }
        }
    };

    @Override
    public void finish() {
        setResult();
        super.finish();
    }

    private void setResult() {

        if (fromeNotice) {
            Intent intent = new Intent();
            intent.putExtra("position", position);
            intent.putExtra("backRefresh", isRefresh);
            setResult(RESULT_OK, intent);
        } else {
            if (isRefresh) {
                Intent response = new Intent();
                response.putExtra("position", position);
                response.putExtra("commentCount", commentCount);
                if (data != null) {
                    response.putExtra("ispraise", this.data.ispraise);
                    response.putExtra("paraiseCount", this.data.praises);
                }
                setResult(Activity.RESULT_OK, response);
                isRefresh = false;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mReceiver != null)
            unregisterReceiver(mReceiver);
        mReceiver = null;
    }

    @Override
    public void commentsCount(int count) {
        commentCount = count;
        data.comments = commentCount;
        mTitleIndicator.updateTitle(0, getString(R.string.comment) + " " + count);
    }

    @Override
    public void praiseCount(int count) {
        data.praises = count;
        mTitleIndicator.updateTitle(1, getString(R.string.praise) + " " + count);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
      /*  if (firstVisibleItem > 0) {
            mHeaderActionView.setVisibility(View.VISIBLE);
        } else if (firstVisibleItem < 1) {
            mHeaderActionView.setVisibility(View.GONE);
        }*/
    }
}
