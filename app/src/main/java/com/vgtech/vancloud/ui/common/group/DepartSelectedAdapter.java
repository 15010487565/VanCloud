package com.vgtech.vancloud.ui.common.group;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.vgtech.common.api.Node;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.common.group.tree.TreeListViewAdapter;
import com.vgtech.vancloud.utils.Utils;

import java.util.List;


public class DepartSelectedAdapter<T> extends TreeListViewAdapter<T> implements CompoundButton.OnCheckedChangeListener {


    public Node mSelectedNode;
    private TextView mRightTv;

    public DepartSelectedAdapter(ListView mTree, Context context, List<Node> datas,
                                 int defaultExpandLevel, boolean hasHeadView, TextView textView) {
        super(mTree, context, datas, defaultExpandLevel, hasHeadView);
        mRightTv = textView;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Node node = mNodes.get(position);
        ViewHolder viewHolder = null;
        if (convertView == null) {
            int resId = R.layout.radio_item_single;
            convertView = mInflater.inflate(resId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.icon = (ImageView) convertView
                    .findViewById(R.id.id_treenode_icon);
            viewHolder.userIcon = (SimpleDraweeView) convertView
                    .findViewById(R.id.user_photo);
            viewHolder.label = (TextView) convertView
                    .findViewById(R.id.id_treenode_label);
            viewHolder.demartTv = (TextView) convertView
                    .findViewById(R.id.id_treenode_sublabel);
            viewHolder.checkBox = (CheckBox) convertView.findViewById(android.R.id.checkbox);
            viewHolder.checkBox.setOnCheckedChangeListener(this);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.checkBox.setTag(node);
        if (mSelectedNode != null && mSelectedNode.equals(node)) {
            viewHolder.checkBox.setChecked(true);
        } else {
            viewHolder.checkBox.setChecked(false);
        }
        if (node.getIcon() == -1) {
            if (node.isUser()) {
                viewHolder.icon.setVisibility(View.GONE);
            } else {
                viewHolder.icon.setVisibility(View.INVISIBLE);
            }
        } else {
            viewHolder.icon.setVisibility(View.VISIBLE);
            viewHolder.icon.setImageResource(node.getIcon());
        }
        if (!node.isUser()) {
            viewHolder.demartTv.setVisibility(View.GONE);
            viewHolder.userIcon.setVisibility(View.GONE);
//            ImageLoader.getInstance().displayImage(null, viewHolder.userIcon, new DisplayImageOptions.Builder()
//                    .cacheInMemory(true)
//                    .cacheOnDisk(true)
//                    .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
//                    .showImageForEmptyUri(R.mipmap.icon_depart)
//                    .displayer(new FadeInBitmapDisplayer(100))
//                    .displayer(new RoundedBitmapDisplayer(Integer.MAX_VALUE))
//                    .build());
        } else {
//            viewHolder.userIcon.setVisibility(View.VISIBLE);
            ImageOptions.setUserImage(viewHolder.userIcon,node.getPhoto());
            viewHolder.demartTv.setVisibility(View.VISIBLE);
            viewHolder.demartTv.setText(node.getJob());
        }
        viewHolder.label.setText(node.getName());
        convertView.setPadding(node.getLevel() * Utils.convertDipOrPx(mContext, 20), 3, 3, 3);
        return convertView;
    }

    public void setSelect(Node node) {
        if (mSelectedNode != null && mSelectedNode.equals(node)) {
            return;
        }
        mSelectedNode = node;
        notifyDataSetChanged();
    }

    public Node getSelectedNode() {
        return mSelectedNode;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            mRightTv.setEnabled(true);
            Node node = (Node) buttonView.getTag();
            if (mSelectedNode != null && mSelectedNode.equals(node)) {
                return;
            }
            mSelectedNode = node;
            notifyDataSetChanged();
        } else {
            Node node = (Node) buttonView.getTag();
            if (mSelectedNode != null && mSelectedNode.equals(node)) {
                mSelectedNode = node;
                notifyDataSetChanged();
            }
        }

    }


    private final class ViewHolder {
        ImageView icon;
        TextView label;
        CheckBox checkBox;
        SimpleDraweeView userIcon;
        TextView demartTv;
        View editView;
        View joinView;
        View infoeditView;
    }

}
