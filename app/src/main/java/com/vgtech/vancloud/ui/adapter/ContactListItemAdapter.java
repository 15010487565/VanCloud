package com.vgtech.vancloud.ui.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.common.provider.db.User;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.register.utils.TextUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by app02 on 2015/9/14.
 */
public class ContactListItemAdapter extends BaseAdapter implements SectionIndexer {

    private Activity mContext;
    private List<User> data;
    private List<Character> firstSpells;


    private int p = 'a';
    private int lashPosition;

    public ContactListItemAdapter(Activity mContext, List<User> data) {
        this.mContext = mContext;
        this.data = data;
        firstSpells = new ArrayList<Character>();
        for (User user : data) {
            if (!firstSpells.contains(user.firstSpell))
                firstSpells.add(user.firstSpell);
        }
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public User getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mViewHolder = null;
        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.contect_list_item, null);

            mViewHolder.firstSpell = (TextView) convertView.findViewById(R.id.first_spell_char);
            mViewHolder.userHeadImg = (SimpleDraweeView) convertView.findViewById(R.id.user_head);
            mViewHolder.userName = (TextView) convertView.findViewById(R.id.user_name);
            mViewHolder.userDetail = (TextView) convertView.findViewById(R.id.user_duty);

            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        User info = data.get(position);

        mViewHolder.firstSpell.setVisibility(View.GONE);
        if (TextUtil.isEmpty(info.photo))
            mViewHolder.userHeadImg.setImageResource(R.mipmap.user_photo_default_small);
        else
            ImageOptions.setUserImage(mViewHolder.userHeadImg, info.photo);

        if (info.isShowHead) {
            mViewHolder.firstSpell.setVisibility(View.VISIBLE);
            mViewHolder.firstSpell.setText((info.firstSpell + "").toUpperCase());
        } else {
            mViewHolder.firstSpell.setVisibility(View.GONE);
        }

        mViewHolder.userName.setText(info.name);
        mViewHolder.userDetail.setText(info.job);


        return convertView;
    }

    class ViewHolder {
        TextView firstSpell;
        SimpleDraweeView userHeadImg;
        TextView userName;
        TextView userDetail;
    }


    String[] sections = new String[]{/*"#",*/ "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#"};

    @Override
    public Object[] getSections() {
        return sections;
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        if (sectionIndex >= 0 && sectionIndex <= 26) {
            char thisSection = (char) ('a' + sectionIndex);

            for (int i = 0; i < data.size(); i++) {
                User user = data.get(i);
                if (user.firstSpell == thisSection)
                    break;
                else if (user.firstSpell > thisSection) {
                    if (i >= 1)
                        if (data.get(i - 1).firstSpell < thisSection) {
                            thisSection = user.firstSpell;
                            break;
                        }
                }
            }

            for (int i = 0; i < data.size(); i++) {
                User user = data.get(i);
                if ((int) user.firstSpell == thisSection) {
                    return i;
                }
            }

        }
        return 0;
    }

    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }

//    private int getIndex(Map<Character,Integer>postions,List<Character> firstSpells,int i){
//        if(firstSpells.get(i)!=null)
//            return postions.get(firstSpells.get(i));
//        else
//            return getIndex(postions,firstSpells,--i);
//    }
}
