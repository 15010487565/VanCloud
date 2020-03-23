package com.vgtech.vancloud.ui.common.group;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.vgtech.common.api.Node;
import com.vgtech.common.config.ImageOptions;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.common.group.tree.TreeHelper;
import com.vgtech.vancloud.ui.common.group.tree.TreeListViewAdapter;
import com.vgtech.vancloud.ui.group.CreateWorkGroupActivity;
import com.vgtech.vancloud.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class GroupAdapter<T> extends TreeListViewAdapter<T> implements View.OnClickListener {


    private ArrayList<Node> mSelectedNodes;
    public JoinDepartmentListener joinDepartmentListener;
    public EditDepartmentListener editDepartmentListener;


    public GroupAdapter(ListView mTree, Context context, List<Node> datas,
                        int defaultExpandLevel, boolean hasHeadView) {
        super(mTree, context, datas, defaultExpandLevel, hasHeadView);
        mSelectedNodes = new ArrayList<Node>();
        mViewHolder = new ArrayList<>();
    }

    public void setJoinDepartmentListener(JoinDepartmentListener joinDepartmentListener) {
        this.joinDepartmentListener = joinDepartmentListener;
    }

    public void setEditDepartmentListener(EditDepartmentListener editDepartmentListener) {
        this.editDepartmentListener = editDepartmentListener;
    }

    public boolean edit;
    public boolean join;
    public boolean infoedit;

    public void addSelectData(List<Node> nodes) {
        mSelectedNodes.clear();
        if (nodes != null) {
            mSelectedNodes.addAll(nodes);
        }
    }

    private List<ViewHolder> mViewHolder;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Node node = mNodes.get(position);
        ViewHolder viewHolder = null;
        if (convertView == null) {
            int resId = R.layout.list_item_single;
            convertView = mInflater.inflate(resId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.icon = (ImageView) convertView
                    .findViewById(R.id.id_treenode_icon);
            viewHolder.userIcon = (SimpleDraweeView) convertView
                    .findViewById(R.id.user_photo);
            viewHolder.label = (TextView) convertView
                    .findViewById(R.id.id_treenode_label);
            viewHolder.countTv = (TextView) convertView
                    .findViewById(R.id.id_treenode_count);
            viewHolder.demartTv = (TextView) convertView
                    .findViewById(R.id.id_treenode_sublabel);
            viewHolder.editView = convertView.findViewById(R.id.btn_edit);
            viewHolder.editView.setOnClickListener(this);
            viewHolder.joinView = convertView.findViewById(R.id.btn_join);
            viewHolder.joinView.setOnClickListener(this);
            viewHolder.infoeditView = convertView.findViewById(R.id.btn_info_edit);
            viewHolder.infoeditView.setOnClickListener(this);
            viewHolder.checkBox = (CheckBox) convertView.findViewById(android.R.id.checkbox);
            viewHolder.checkBox.setVisibility(View.GONE);
            mViewHolder.add(viewHolder);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.url = "" + position;
        viewHolder.userIcon.setTag(position);
        if (node.getIcon() == -1) {
            viewHolder.icon.setVisibility(View.INVISIBLE);
            viewHolder.editView.setVisibility(View.GONE);
            viewHolder.joinView.setVisibility(View.GONE);
            viewHolder.infoeditView.setVisibility(View.GONE);
        } else {
            viewHolder.icon.setVisibility(View.VISIBLE);
            viewHolder.icon.setImageResource(node.getIcon());
        }
        viewHolder.editView.setTag(node);
        viewHolder.joinView.setTag(node);
        viewHolder.infoeditView.setTag(node);
        viewHolder.editView.setVisibility(!node.isUser() && edit ? View.VISIBLE : View.GONE);
        viewHolder.joinView.setVisibility(!node.isUser() && join ? View.VISIBLE : View.GONE);
        viewHolder.infoeditView.setVisibility(!node.isUser() && infoedit ? View.VISIBLE : View.GONE);
        if (!node.isUser()) {
            viewHolder.demartTv.setVisibility(View.GONE);
            if (node.type == 1) {
                viewHolder.userIcon.setImageResource(R.mipmap.icon_default_group);
            } else if (node.type == 2||node.type==0) {
                viewHolder.userIcon.setImageResource(R.mipmap.icon_depart);
            }
        } else {
            ImageOptions.setUserImage(viewHolder.userIcon,node.getPhoto());
            viewHolder.demartTv.setVisibility(View.VISIBLE);
            viewHolder.demartTv.setText(node.getJob());
        }
        viewHolder.label.setText(node.getName());
        if (node.isUser()) {
            viewHolder.countTv.setText("");
        } else {
            viewHolder.countTv.setText("(" + TreeHelper.getChildCount(node) + ")");
        }
        convertView.setPadding(node.getLevel() * Utils.convertDipOrPx(mContext, 20), 3, 3, 3);
        return convertView;
    }

    public void release() {
        displayedImages.clear();
    }

    List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_edit:
                Node node = (Node) v.getTag();
                Intent intent = new Intent(mContext, CreateWorkGroupActivity.class);
                intent.putExtra("node", node);
                ((Activity) mContext).startActivityForResult(intent, 101);
                break;
            case R.id.btn_join:
                Node nodes = (Node) v.getTag();
                joinDepartmentListener.JoinDepartmentAction(nodes);
                break;
            case R.id.btn_info_edit:
                Node node1 = (Node) v.getTag();
                editDepartmentListener.EditDepartmentAction(node1);
                break;
        }
    }


    private final class ViewHolder {
        ImageView icon;
        TextView label;
        CheckBox checkBox;
        SimpleDraweeView userIcon;
        TextView demartTv;
        TextView countTv;
        View editView;
        View joinView;
        View infoeditView;
        public String url;
    }

    public interface JoinDepartmentListener {
        void JoinDepartmentAction(Node node);
    }

    public interface EditDepartmentListener {
        void EditDepartmentAction(Node node);
    }

}
