package com.vgtech.vancloud;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.PackageUtil;
import com.activeandroid.ActiveAndroid;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ClearCacheRequest;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.Volley;
import com.baidu.mapapi.SDKInitializer;
import com.chenzhanyang.behaviorstatisticslibrary.BehaviorStatistics;
import com.igexin.sdk.PushManager;
import com.nostra13.universalimageloader.cache.disc.impl.ext.LruDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.smtt.sdk.QbSdk;
import com.vgtech.common.ACache;
import com.vgtech.common.BaseApp;
import com.vgtech.common.FileCacheUtils;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.Node;
import com.vgtech.common.api.RootData;
import com.vgtech.common.listener.ApplicationProxy;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.provider.db.Department;
import com.vgtech.common.provider.db.User;
import com.vgtech.common.utils.ActivityUtils;
import com.vgtech.vancloud.statistics.NetBehaviorStatisticsHandler;
import com.vgtech.vancloud.ui.common.group.tree.TreeHelper;
import com.vgtech.vancloud.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by zhangshaofang on 2015/7/21.
 */
public class VanCloudApplication extends BaseApp implements ApplicationProxy, HttpListener<String> {
    private NetworkManager mNetworkManager;
    private String channelId;
    private List<Node> mTreeNode;

    public List<Node> getCacheTreeNode() {
        if (mTreeNode == null) {
            mTreeNode = getTreeNode();
        } else {
            for (Node node : mTreeNode) {
                node.setExpand(false);
            }
        }
        return mTreeNode;
    }


    public List<Node> getVanTopCacheUserNode() {
        return User.queryVantopUser(this);
    }

    public List<Node> getTreeNode() {
        List mDatas = new ArrayList();
        List<Department> groups = Department.queryDepartment(this);
        mDatas.addAll(groups);
        List<User> users = User.queryUser(this);
        mDatas.addAll(users);
        try {
            mTreeNode = TreeHelper.convetData2Node(mDatas);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return mTreeNode;
    }

    public void release() {
        mTreeNode = null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        //初始化Fresco
        ActivityUtils.initFresco(getApplicationContext());
        //初始化聊天
        BehaviorStatistics.getInstance().setDefaultBehaviorHandler(new NetBehaviorStatisticsHandler());
        //初始化百度
        SDKInitializer.initialize(getApplicationContext());

        //非wifi情况下，主动下载x5内核
        QbSdk.setDownloadWithoutWifi(true);
        //搜集本地tbs内核信息并上报服务器，服务器返回结果决定使用哪个内核。
        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {
            @Override
            public void onViewInitFinished(boolean arg0) {
                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
//                Log.e(  "TAG_x5内核","arg0="+arg0);
            }

            @Override
            public void onCoreInitFinished() {

            }
        };
        //x5内核初始化接口
        QbSdk.initX5Environment(getApplicationContext(), cb);

        PushManager.getInstance().initialize(this.getApplicationContext(),com.vgtech.vancloud.reciver.GetuiPushReceiver.class);

        ActivityUtils.setAppLanguage(this);
        CrashReport.initCrashReport(getApplicationContext(), "e75fa2d1e9", false);
        ActiveAndroid.initialize(getApplicationContext());

        PackageUtil.init(this);

        initImageLoader(this);

    }
    /**
     * 初始化ImageLoader配置参数
     */
    public void initImageLoader(Context context) {

        DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .showImageOnLoading(com.vgtech.common.R.drawable.img_default)
                .showImageOnFail(com.vgtech.common.R.drawable.img_default)
                .showImageForEmptyUri(com.vgtech.common.R.drawable.img_default)
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
//                .imageScaleType(ImageScaleType.EXACTLY)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        File cacheDir = FileCacheUtils.getImageDir(context);// 新建图片缓存文件夹

        ImageLoaderConfiguration config = null;
        try {
            config = new ImageLoaderConfiguration.Builder(context)

                    .memoryCacheExtraOptions(480, 800)//保存的每个缓存文件的最大长宽
                    //                .diskCacheExtraOptions(720, 1280, null)//设置缓存的详细信息
                    .threadPoolSize(3)//线程池内加载数量
                    .denyCacheImageMultipleSizesInMemory()
                    .diskCacheFileNameGenerator(new Md5FileNameGenerator())//缓存文件名称MD5加密
                    .memoryCache(new LruMemoryCache(2 * 1024 * 1024))//内存缓存
                    .memoryCacheSize(2 * 1024 * 1024)//内存缓存最大值
                    .diskCacheFileCount(400)//缓存文件数量
                    .diskCacheSize(50 * 1024 * 1024)// 文件缓存最大值50 MiB
                    //                .diskCache(new UnlimitedDiscCache(cacheDir))//自定义缓存文件夹
                    .diskCache(new LruDiskCache(cacheDir, new Md5FileNameGenerator(), 50 * 1024 * 1024))
                    .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                    .defaultDisplayImageOptions(displayImageOptions)
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ImageLoader.getInstance().init(config);
        com.nostra13.universalimageloader.utils.L.disableLogging();
    }


    @Override
    public void onTerminate() {
        super.onTerminate();

        ActiveAndroid.dispose();
        clear();
    }

    public String getChannelId() {
        if (TextUtils.isEmpty(channelId)) {
            try {
                ApplicationInfo appInfo = this.getPackageManager()
                        .getApplicationInfo(getPackageName(),
                                PackageManager.GET_META_DATA);
                channelId = appInfo.metaData.getString("UMENG_CHANNEL");
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return channelId;
    }

    public NetworkManager getNetworkManager() {
        if (mNetworkManager == null) {
            ACache aCache = ACache.get(this);
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            File cacheDir = new File(this.getCacheDir(), "volley");
            DiskBasedCache cache = new DiskBasedCache(cacheDir);
            requestQueue.start();
            requestQueue.add(new ClearCacheRequest(cache, null));
            mNetworkManager = new NetworkManager(requestQueue, getApiUtils(), aCache);
        }
        return mNetworkManager;
    }

    @Override
    public void clear() {
        Utils.clearUserInfo(this);
    }

    public void sendMessage(String clientid) {
        updateClientId(clientid);
    }
    private void updateClientId(String clientid) {

        NetworkManager networkManager = getNetworkManager();
        Map<String, String> params = new HashMap<String, String>();
        params.put("ownid", PrfUtils.getUserId(this));
        params.put("tenantid", PrfUtils.getTenantId(this));
        params.put("devicetype", "android");
        params.put("clientid", clientid);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_VCHAT_PNS), params, this);
        networkManager.load(1, path, this);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {

    }

    @Override
    public void onResponse(String response) {

    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }
}
