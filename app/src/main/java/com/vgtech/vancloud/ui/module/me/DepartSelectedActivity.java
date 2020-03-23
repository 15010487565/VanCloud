package com.vgtech.vancloud.ui.module.me;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.vgtech.vancloud.R;
import com.vgtech.common.api.EditDepartmentBean;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.provider.db.Department;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.chat.controllers.PreferencesController;
import com.vgtech.vancloud.ui.common.group.DepartSelectedAdapter;
import com.vgtech.vancloud.ui.common.group.GroupAdapter;
import com.vgtech.common.api.Node;
import com.vgtech.vancloud.ui.common.group.tree.TreeHelper;
import com.vgtech.vancloud.ui.common.group.tree.TreeListViewAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangshaofang on 2015/9/22.
 */
public class DepartSelectedActivity extends BaseActivity implements GroupAdapter.OnTreeNodeClickListener {
    private ListView mTree;
    private DepartSelectedAdapter mAdapter;
    private List<Node> mAllNodes;
    private NetworkManager mNetworkManager;
    private EditText mNameEt;
    private String department_id;
    private List<EditDepartmentBean> departmentInfos;
    private Node mNode;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mAllNodes = (List<Node>) msg.obj;
            try {
                mAdapter = new DepartSelectedAdapter(mTree, DepartSelectedActivity.this, mAllNodes, 1, false, mRightTv);
                mAdapter.setOnTreeNodeClickListener(DepartSelectedActivity.this);
                Node node = getIntent().getParcelableExtra("node");
                if (node != null) {
                    mAdapter.setSelect(node);
                    mRightTv.setEnabled(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            mTree.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
        }
    };
    private TextView mRightTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(new PreferencesController().getAccount().tenant_name);
        mTree = (ListView) findViewById(android.R.id.list);
        mRightTv = initRightTv(getString(R.string.ok));
        mRightTv.setEnabled(false);
        updataUIAction();

    }

    @Override
    protected int getContentView() {
        return R.layout.depart_selectedlist;
    }

    public void updataUIAction() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Department> groups = Department.queryDepartment(getApplicationContext());
                List mDatas = new ArrayList();
                mDatas.addAll(groups);
                try {
                    List<Node> nodes = TreeHelper.convetData2Node(mDatas);
                    Message msg = new Message();
                    msg.what = 1;
                    msg.obj = nodes;
                    mHandler.sendMessage(msg);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_right:
                Node node = mAdapter.getSelectedNode();
                Intent intent = new Intent();
                intent.putExtra("node", node);
                setResult(RESULT_OK, intent);
                finish();
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    @Override
    public void onClick(Node node, int position) {
        try {
            mAdapter.setOnTreeNodeClickListener(new TreeListViewAdapter.OnTreeNodeClickListener() {
                @Override
                public void onClick(Node node, int position) {
                    if (node.isLeaf()) {
                        {
                            mRightTv.setEnabled(true);
                            mAdapter.setSelect(node);
                        }
                    }
                }
            });        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
