package com.vgtech.vancloud.ui.chat;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.vgtech.vancloud.R;
import com.vgtech.vancloud.utils.VgTextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xuanqiang
 */
public class EmojiFragment extends Fragment {

    ViewPager viewPager;
    LinearLayout pointsView;

    public EmojiFragment() {
        emojis = new ArrayList<List<Map<String, Object>>>();
        int i = 0;
        for (Map.Entry<String, Integer> entry : EMOJIS.entrySet()) {
            int p = i / 32;
            if (i % 32 == 0) {
                List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
                emojis.add(data);
            }
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("image", entry.getValue());
            map.put("text", entry.getKey());
            emojis.get(p).add(map);
            i++;
        }
        points = new ImageView[4];
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.message_emoji, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewPager = (ViewPager) getView().findViewById(R.id.emojiViewPager);
        pointsView = (LinearLayout) getView().findViewById(R.id.points);
        for (int j = 0; j < points.length; j++) {
            points[j] = (ImageView) pointsView.getChildAt(j);
        }
        if (views == null) {
            views = new ArrayList<View>(4);
            for (int j = 0; j < 4; j++) {
                GridView gridview = (GridView) getLayoutInflater(null).inflate(R.layout.message_emoji_page, null);
                views.add(gridview);
                assert gridview != null;
                gridview.setAdapter(new SimpleAdapter(getActivity(), emojis.get(j), R.layout.emoji_page_item,
                        new String[]{"image"},
                        new int[]{R.id.imageView}));
                gridview.setOnItemClickListener(listener);
            }
        }
        points[page].setSelected(true);
        viewPager.setAdapter(adapter);
        viewPager.setOnPageChangeListener(pageListener);
    }

    public void setListener(AdapterView.OnItemClickListener listener) {
        this.listener = listener;
    }

    AdapterView.OnItemClickListener listener;

    private ImageView[] points;
    public int page = 0;
    public List<List<Map<String, Object>>> emojis;
    List<View> views;

    PagerAdapter adapter = new PagerAdapter() {
        @Override
        public int getCount() {
            return views.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == o;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(views.get(position));
            return views.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(views.get(position));
        }
    };

    ViewPager.SimpleOnPageChangeListener pageListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            if (position < 0 || position > points.length - 1 || page == position) {
                return;
            }
            points[position].setSelected(true);
            points[page].setSelected(false);
            page = position;
        }
    };

    public static Spannable getEmojiContent(final Context context, float imgSize, final CharSequence content) {

        return VgTextUtils.textViewSpan(context, content, null, (int) imgSize, false, false);
    }

    public static Spannable getEmojiContentWithAt(final Context context, float imgSize, final CharSequence content) {

        return VgTextUtils.textViewSpan(context, content, null, (int) imgSize, false, true);
    }

    public static Spannable getEmojiContent(final Context context, final CharSequence content, TextView tv) {
        return VgTextUtils.textViewSpan(context, content, tv, (int) tv.getTextSize(), false, false);
    }

    @SuppressWarnings("serial")
    public static final Map<String, Integer> EMOJIS = new LinkedHashMap<String, Integer>() {{
        put("[/微笑]", R.drawable.emoji_weixiao);
        put("[/喜欢]", R.drawable.emoji_xihuan);
        put("[/衰]", R.drawable.emoji_weiqu);
        put("[/瞪眼]", R.drawable.emoji_dengyan);
        put("[/酷]", R.drawable.emoji_ku);
        put("[/大哭]", R.drawable.emoji_daku);
        put("[/害羞]", R.drawable.emoji_haixiu);
        put("[/闭嘴]", R.drawable.emoji_bizui);
        put("[/睡觉]", R.drawable.emoji_shuijiao);
        put("[/流泪]", R.drawable.emoji_liulei);
        put("[/尴尬]", R.drawable.emoji_ganga);
        put("[/发怒]", R.drawable.emoji_fanu);
        put("[/调皮]", R.drawable.emoji_tiaopi);
        put("[/呲牙]", R.drawable.emoji_ciya);
        put("[/吃惊]", R.drawable.emoji_chijing);
        put("[/委屈]", R.drawable.emoji_nanguo);
        put("[/眼镜]", R.drawable.emoji_yanjing);
        put("[/脸红]", R.drawable.emoji_lianhong);
        put("[/发火]", R.drawable.emoji_fahuo);
        put("[/吐]", R.drawable.emoji_tu);
        put("[/偷笑]", R.drawable.emoji_touxiao);
        put("[/笑脸]", R.drawable.emoji_xiaolian);
        put("[/挑眉]", R.drawable.emoji_tiaomei);
        put("[/瘪嘴]", R.drawable.emoji_biezui);
        put("[/饿]", R.drawable.emoji_e);
        put("[/困]", R.drawable.emoji_kun);
        put("[/大惊]", R.drawable.emoji_dajing);
        put("[/汗]", R.drawable.emoji_han);
        put("[/哈哈]", R.drawable.emoji_haha);
        put("[/钢盔]", R.drawable.emoji_gangkui);
        put("[/奋斗]", R.drawable.emoji_fendou);
        put("[/删除]", R.drawable.emoji_del);

        put("[/咒骂]", R.drawable.emoji_zhouma);
        put("[/问号]", R.drawable.emoji_wenhao);
        put("[/嘘]", R.drawable.emoji_xu);
        put("[/晕]", R.drawable.emoji_yun);
        put("[/我晕]", R.drawable.emoji_woyun);
        put("[/黑脸]", R.drawable.emoji_heilian);
        put("[/骷髅]", R.drawable.emoji_kulou);
        put("[/敲头]", R.drawable.emoji_qiaotou);
        put("[/再见]", R.drawable.emoji_zaijian);
        put("[/我汗]", R.drawable.emoji_wohan);
        put("[/抠鼻]", R.drawable.emoji_koubi);
        put("[/鼓掌]", R.drawable.emoji_guzhang);
        put("[/囧]", R.drawable.emoji_jiong);
        put("[/坏笑]", R.drawable.emoji_huaixiao);
        put("[/左哼]", R.drawable.emoji_zuoheng);
        put("[/右哼]", R.drawable.emoji_youheng);
        put("[/哈欠]", R.drawable.emoji_haqian);
        put("[/鄙视]", R.drawable.emoji_bishi);
        put("[/难过]", R.drawable.emoji_shuai);
        put("[/想哭]", R.drawable.emoji_xiangku);
        put("[/阴险]", R.drawable.emoji_yinxian);
        put("[/嘴]", R.drawable.emoji_zui);
        put("[/吓]", R.drawable.emoji_xia);
        put("[/可怜]", R.drawable.emoji_kelian);
        put("[/菜刀]", R.drawable.emoji_caidao);
        put("[/西瓜]", R.drawable.emoji_xigua);
        put("[/啤酒]", R.drawable.emoji_pijiu);
        put("[/篮球]", R.drawable.emoji_lanqiu);
        put("[/乒乓球]", R.drawable.emoji_pingpang);
        put("[/咖啡]", R.drawable.emoji_kafei);
        put("[/米饭]", R.drawable.emoji_mifan);
        put("[/删除1]", R.drawable.emoji_del);
        put("[/猪头]", R.drawable.emoji_zhutou);
        put("[/玫瑰]", R.drawable.emoji_meigui);
        put("[/凋谢]", R.drawable.emoji_diaoxie);
        put("[/嘴唇]", R.drawable.emoji_zuichun);
        put("[/红心]", R.drawable.emoji_hongxin);
        put("[/心碎]", R.drawable.emoji_xinsui);
        put("[/蛋糕]", R.drawable.emoji_dangao);
        put("[/闪电]", R.drawable.emoji_shandian);
        put("[/炸弹]", R.drawable.emoji_zhadan);
        put("[/匕首]", R.drawable.emoji_bishou);
        put("[/足球]", R.drawable.emoji_zuqiu);
        put("[/虫子]", R.drawable.emoji_chongzi);
        put("[/便便]", R.drawable.emoji_bianbian);
        put("[/月亮]", R.drawable.emoji_yueliang);
        put("[/太阳]", R.drawable.emoji_taiyang);
        put("[/礼物]", R.drawable.emoji_liwu);
        put("[/小孩]", R.drawable.emoji_xiaohai);
        put("[/赞]", R.drawable.emoji_zan);
        put("[/踩]", R.drawable.emoji_cai);
        put("[/握手]", R.drawable.emoji_woshou);
        put("[/耶]", R.drawable.emoji_ye);
        put("[/抱拳]", R.drawable.emoji_baoquan);
        put("[/勾手]", R.drawable.emoji_goushou);
        put("[/拳头]", R.drawable.emoji_quantou);
        put("[/小指]", R.drawable.emoji_xiaozhi);
        put("[/手指]", R.drawable.emoji_shouzhi);
        put("[/不行]", R.drawable.emoji_buxing);
        put("[/OK]", R.drawable.emoji_ok);
        put("[/情侣]", R.drawable.emoji_qinglv);
        put("[/右心]", R.drawable.emoji_youxin);
        put("[/摇摆]", R.drawable.emoji_yaobai);
        put("[/删除2]", R.drawable.emoji_del);
        put("[/发抖]", R.drawable.emoji_fadou);
        put("[/张嘴]", R.drawable.emoji_zhangzui);
        put("[/跳舞]", R.drawable.emoji_tiaowu);
        put("[/站立]", R.drawable.emoji_zhanli);
        put("[/背影]", R.drawable.emoji_beiying);
        put("[/举手]", R.drawable.emoji_jushou);
        put("[/跳跳]", R.drawable.emoji_tiaotiao);
        put("[/转圈]", R.drawable.emoji_zhuanquan);
        put("[/投降]", R.drawable.emoji_touxiang);
        put("[/左转圈]", R.drawable.emoji_zuozhuanquan);
        put("[/左太极]", R.drawable.emoji_zuotaiji);
        put("[/太极]", R.drawable.emoji_taiji);
        put("[/删除3]", R.drawable.emoji_del);
    }};

}
