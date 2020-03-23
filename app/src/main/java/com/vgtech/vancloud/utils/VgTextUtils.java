package com.vgtech.vancloud.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.TextView;

import com.vgtech.common.api.NewUser;
import com.vgtech.common.api.SearchItem;
import com.vgtech.common.provider.db.User;
import com.vgtech.common.utils.UserUtils;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.chat.EmojiFragment;
import com.vgtech.vancloud.ui.register.utils.GetPinyinUtil;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 处理字体高亮
 *
 * @author zeng
 */
public class VgTextUtils {
    //    public static boolean isContains(String q, String name) {
//        String qpy = GetPinyinUtil.getPingYin(q);
//
//        String namepy = GetPinyinUtil.getPingYin(name);
//
//    }
    public static void match(List<SearchItem> filterDateList, User sortModel, String filterStr) {
        boolean isMatch = false;
        String car_title = sortModel.getName();
//        String car_title = GetPinyinUtil.getPingYin(sortModel.getName());
        int sellingCount = matchText(car_title, filterStr);
        SearchItem searchItem = new SearchItem();
        if (sellingCount != 0) {
            isMatch = true;
            searchItem.id = filterStr;
            searchItem.type = SearchItem.Type.user;
            searchItem.title = "<font color='#3ab5ff'>" + car_title.substring(0, sellingCount) + "</font>" + car_title.substring(sellingCount);
            searchItem.subtitle = sortModel.job;
            searchItem.icon = sortModel.photo;
            searchItem.itemObj = sortModel;
        }

        int index = car_title.toLowerCase().indexOf(filterStr.toLowerCase().toString());
        int length = filterStr.length();
        if (index != -1) {
            isMatch = true;
            searchItem.id = filterStr;
            searchItem.type = SearchItem.Type.user;
            searchItem.title = car_title.substring(0, index) + "<font color='#3ab5ff'>" + filterStr + "</font>" + car_title.substring(index + length);
            searchItem.subtitle = sortModel.job;
            searchItem.icon = sortModel.photo;
            searchItem.itemObj = sortModel;
        }
        if (isMatch) {
            filterDateList.add(searchItem);
        }
    }

    /**
     * 匹配字符串
     *
     * @param filterStr
     */
    public static int matchText(String name, String filterStr) {
        int sellingcount = 0;
        String[] sellingArray = GetPinyinUtil.getSellingArray(name);
        for (String selling : sellingArray) {
            if ("".equals(filterStr)) {
                return sellingcount;
            }
            if (filterStr.startsWith(selling)) {
                sellingcount++;
                filterStr = filterStr.substring(selling.length(), filterStr.length());
            } else if (selling.startsWith(filterStr)) {
                sellingcount++;
                return sellingcount;
            } else {
                return 0;
            }
        }
        return sellingcount;
    }

    public static int getTextWidth(Context context, int textSize, String text) {
        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(Utils.convertDipOrPx(context, textSize));
        return (int) textPaint.measureText(text);
    }

    private static Map<String, SoftReference<Drawable>> imageCache = new HashMap<String, SoftReference<Drawable>>();

    /**
     * @param mContext
     * @param content
     * @param hasClick 是否添加click
     */
    public static Spannable textViewSpan(Context mContext,
                                         CharSequence content, TextView textview, int imgSize, boolean hasClick, boolean at) {
        if (TextUtils.isEmpty(content))
            return new SpannableString("");
        List<PositionItem> list = paresString2(content, at);
        Spannable span = new SpannableString(content);
        int color = ContextCompat.getColor(mContext, com.vgtech.common.R.color.comment_blue);
        for (PositionItem pi : list) {
            if (pi.getPrefixType() == 4) {
                Integer resId = EmojiFragment.EMOJIS.get(pi.content);
                try {
                    if (resId != null) {
                        if (imgSize > 0) {
                            Drawable drawable = mContext.getResources().getDrawable(resId);
                            drawable.setBounds(0, 0, imgSize, imgSize);
                            span.setSpan(new ImageSpan(drawable), pi.start,
                                    pi.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        } else {
                            span.setSpan(new ImageSpan(mContext, resId), pi.start, pi.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    } else {
                        span.setSpan(new ForegroundColorSpan(color), pi.start,
                                pi.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                } catch (Exception e) {
                    // TODO: handle exception
                    span.setSpan(new ForegroundColorSpan(color), pi.start,
                            pi.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            } else {
                if (hasClick) {
                    TextClickSapn tcs = new TextClickSapn(mContext, pi, color, color);
                    span.setSpan(tcs, pi.start,
                            pi.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else
                    span.setSpan(new ForegroundColorSpan(color), pi.start,
                            pi.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        if (hasClick && textview != null)
            textview.setMovementMethod(LinkMovementMethod.getInstance());
        return span;

    }

    public static Spanned generaReceiver(Context context, List<NewUser> recivers) {
        return generaReceiver(context, recivers, 0);
    }

    public static Spanned generaReceiver(Context context, List<NewUser> recivers, int count) {
        int max = 10;
        int min = Math.min(recivers.size(), max);
        StringBuffer stringBuffer = new StringBuffer();
        List<PositionItem> list = new ArrayList<>();
        for (int i = 0; i < min; i++) {
            NewUser newUser = recivers.get(i);
            int start = stringBuffer.length();
            String str = newUser.name;
            stringBuffer.append(str);
            int end = stringBuffer.length();
            stringBuffer.append("，");
            list.add(new PositionItem(start, end, str, newUser));
        }
        if (!TextUtils.isEmpty(stringBuffer))
            stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        if (recivers.size() > max || count != 0)
            stringBuffer.append(context.getString(R.string.lable_praise_count, count == 0 ? recivers.size() : count));
        else {
            stringBuffer.append(" ");
        }
        Spannable span = new SpannableString(stringBuffer);
//        int color = Color.parseColor("#3ab5ff");
        int normalTextColor = Color.parseColor("#5577a7");
        int pressedTextColor = Color.parseColor("#4b5577a7");
        for (PositionItem pi : list) {
            TextClickSapn tcs = new TextClickSapn(context, pi, normalTextColor, pressedTextColor);
            span.setSpan(tcs, pi.start,
                    pi.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return span;
    }

    public static String find(String content) {
        String regex = "@[^\\s:：《]+([\\s:：《]|$)";
//        String regex = "|@[^\\s]+\\s?|\\[/[^]]+\\]";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(content);
        boolean b = m.find();
        if (b) {
            int start = m.start();
            int end = m.end();
            String str = m.group();
            return str;
        }
        return null;
    }

    /**
     * 这个是处理一条信息有多个#...
     *
     * @param content
     * @return
     */
    public static List<PositionItem> paresString2(CharSequence content, boolean at) {
        String regex = at ? "@[^\\s:：《]+([\\s:：《]|$)|\\[/[^]]+\\]" : "\\[/[^]]+\\]";

//        String regex = "@[^\\s:：《]+([\\s:：《]|$)|#(.+?)#|(http|ftp|https):\\/\\/[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.,@?^=%&amp;:/~\\+#]*[\\w\\-\\@?^=%&amp;/~\\+#])?|\\[/[^]]+\\]";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(content);
        boolean b = m.find();
        List<PositionItem> list = new ArrayList<PositionItem>();
        int count = 0;
        int lastIndex = 0;
        while (b) {
//            System.out.println(m.start());
//            System.out.println(m.end());
//            System.out.println(m.group());
            int start = m.start();
            int end = m.end();
            String str = m.group();
            if (str.startsWith("#")) {
                count++;
                if (count % 2 == 0) {
                    b = m.find(lastIndex);
                    continue;
                }
            }
            list.add(new PositionItem(start, end, str, content.length()));
            b = m.find(m.end() - 1);
            try {
                lastIndex = m.start() + 1;
            } catch (Exception e) {
                // TODO: handle exception
            }
        }

        return list;
    }

    public static class TextClickSapn extends ClickableSpan {
        private PositionItem item;
        private Context mContext;
        private boolean mIsPressed;
        private int mPressedBackgroundColor;
        private int mNormalTextColor;
        private int mPressedTextColor;

        public TextClickSapn(Context context, PositionItem item, int normalTextColor, int pressedTextColor) {
            // TODO Auto-generated constructor stub
            this.item = item;
            this.mContext = context;
            mNormalTextColor = normalTextColor;
            mPressedTextColor = pressedTextColor;
            mPressedBackgroundColor = Color.parseColor("#e8ecef");
        }

        public void setPressed(boolean isSelected) {
            mIsPressed = isSelected;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setColor(mNormalTextColor);
            ds.bgColor = mIsPressed ? mPressedBackgroundColor : Color.TRANSPARENT;
            ds.setUnderlineText(item.getPrefixType() == 3);
        }

        @Override
        public void onClick(View widget) {
            // TODO Auto-generated method stub
            switch (item.getPrefixType()) {
                case 1:
                    //TODO
//				Intent it_person = new Intent(mContext,
//						TestPersonActivity.class);
//				it_person.putExtra("content", item.getContentWithoutPrefix());
//				mContext.startActivity(it_person);
                    break;
                case 2:
//				Intent it_topic = new Intent(mContext, TestTopicActivity.class);
//				it_topic.putExtra("content", item.getContentWithoutPrefix());
//				mContext.startActivity(it_topic);
                    break;
                case 3:
                    // 直接使用调用浏览器
                    // 这个是短链 ，还需要条用微博接口，转成原始连接 才能访问
                    // 先使用短链去调用接口，获取长链，再启动浏览器
                    Intent intent = new Intent();
                    // intent.setAction("android.intent.action.VIEW");
                    Uri content_url = Uri.parse(item.content);
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(content_url);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                    break;
                case 5:
                    NewUser user = item.newUser;
                    UserUtils.enterUserInfo(mContext, user.userid + "", user.name, user.photo);
                    break;
                default:
                    break;
            }
        }
    }

    public static class PositionItem {
        public int start;
        public int end;
        private int prefixType;
        private String content;
        private int strLenght;
        private NewUser newUser;

        public PositionItem(int start, int end, String content, int strLenght) {
            // TODO Auto-generated constructor stub
            this.start = start;
            this.end = end;
            this.content = content;
            this.strLenght = strLenght;
        }

        public PositionItem(int start, int end, String content, NewUser newUser) {
            // TODO Auto-generated constructor stub
            this.start = start;
            this.end = end;
            this.content = content;
            this.newUser = newUser;
        }

        public PositionItem(int start, int end, String content) {
            // TODO Auto-generated constructor stub
            this.start = start;
            this.end = end;
            this.content = content;
        }

        public String getContent() {
            return content;
        }

        public String getContentWithoutPrefix() {
            switch (getPrefixType()) {
                case 1:
                    if (end == strLenght)
                        return content.substring(1, strLenght);
                    return content.substring(1, content.length() - 1);
                case 2:
                    return content.substring(1, content.length() - 1);
                case 3:
                    return content;
                default:
                    return content;
            }
        }

        /**
         * 1 @ 人物 2 # 话题 3 http://t.cn/ 短链 4 [ 表情
         *
         * @return
         */
        public int getPrefixType() {
            if (newUser != null) {
                return 5;
            }
            if (content.startsWith("@"))
                return 1;
            if (content.startsWith("#"))
                return 2;
            if (content.startsWith("http://"))
                return 3;
            if (content.startsWith("["))
                return 4;
            return -1;
        }
        // private String removePrefixSuffix(String str){
        //
        // }
    }
}
