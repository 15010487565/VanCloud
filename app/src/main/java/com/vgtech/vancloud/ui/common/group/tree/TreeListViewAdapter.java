package com.vgtech.vancloud.ui.common.group.tree;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.vgtech.common.api.Node;

import java.util.ArrayList;
import java.util.List;

public abstract class TreeListViewAdapter<T> extends BaseAdapter {

    protected Context mContext;
    /**
     * 存储所有可见的Node
     */
    protected List<Node> mNodes;
    protected LayoutInflater mInflater;
    /**
     * 存储所有的Node
     */
    protected List<Node> mAllNodes;

    /**
     * 点击的回调接口
     */
    private OnTreeNodeClickListener onTreeNodeClickListener;
    private OnTreeNodeLongClickListener onTreeNodeLongClickListener;

    public interface OnTreeNodeClickListener {
        void onClick(Node node, int position);
    }

    public void setOnTreeNodeClickListener(
            OnTreeNodeClickListener onTreeNodeClickListener) {
        this.onTreeNodeClickListener = onTreeNodeClickListener;
    }

    public interface OnTreeNodeLongClickListener {
        void onLongClick(Node node, int position);
    }

    public void setOnTreeNodeLongClickListener(OnTreeNodeLongClickListener onTreeNodeLongClickListener) {
        this.onTreeNodeLongClickListener = onTreeNodeLongClickListener;
    }

    protected List<Node> mAllDatas;
    public int mExpandLevel;
    private ListView mTree;
    private boolean mHasheadview;

    /**
     * @param context
     * @param datas
     * @param defaultExpandLevel 默认展开几级树
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public TreeListViewAdapter(ListView tree, Context context, List<Node> datas,
                               int defaultExpandLevel, boolean hasHeadView) {
        mTree = tree;
        mAllDatas = new ArrayList<Node>();
        mAllDatas.addAll(datas);
        mContext = context;
        mExpandLevel = defaultExpandLevel;
        mHasheadview = hasHeadView;
        mInflater = LayoutInflater.from(context);
        generaTreeData();
    }

    public void add(Node t) {
        mAllDatas.add(t);
        generaTreeData();
        notifyDataSetChanged();
    }

    public void remove(Node t) {
        mAllDatas.remove(t);
        generaTreeData();
        notifyDataSetChanged();
    }

    public void addAll(List<Node> list) {
        mAllDatas.addAll(list);
        generaTreeData();
        notifyDataSetChanged();
    }

    public void clear() {
        mAllDatas.clear();
        if (mAllNodes != null)
            mAllNodes.clear();
        if (mNodes != null)
            mNodes.clear();
        notifyDataSetChanged();
    }

    public void generaTreeData() {
        try {
            /**
             * 对所有的Node进行排序
             */
            mAllNodes = TreeHelper.getSortedNodes(mAllDatas, mExpandLevel);
            /**
             * 过滤出可见的Node
             */
            mNodes = TreeHelper.filterVisibleNode(mAllNodes);

            /**
             * 设置节点点击时，可以展开以及关闭；并且将ItemClick事件继续往外公布
             */
            mTree.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    if (mHasheadview) {
                        expandOrCollapse(position - 1);
                    } else {
                        expandOrCollapse(position);
                    }
                    if (onTreeNodeClickListener != null) {
                        if (mHasheadview) {
                            onTreeNodeClickListener.onClick(mNodes.get(position - 1),position - 1);
                        } else {
                            onTreeNodeClickListener.onClick(mNodes.get(position),position);
                        }

                    }
                }

            });
            /**
             * 设置item长按
             */
            mTree.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    if (onTreeNodeLongClickListener != null) {
                        if (mHasheadview) {
                            onTreeNodeLongClickListener.onLongClick(mNodes.get(position - 1), position - 1);
                        } else {
                            onTreeNodeLongClickListener.onLongClick(mNodes.get(position), position);
                        }

                    }
                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 相应ListView的点击事件 展开或关闭某节点
     *
     * @param position
     */
    public void expandOrCollapse(int position) {
        Node n = mNodes.get(position);

        if (n != null)// 排除传入参数错误异常
        {
            if (!n.isLeaf()) {
                n.setExpand(!n.isExpand());
                mNodes = TreeHelper.filterVisibleNode(mAllNodes);
                notifyDataSetChanged();// 刷新视图
            }
        }
    }

    @Override
    public int getCount() {
        return mNodes.size();
    }

    @Override
    public Node getItem(int position) {
        return mNodes.get(position);
    }

    public List<Node> getNodes() {
        return mAllDatas;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
