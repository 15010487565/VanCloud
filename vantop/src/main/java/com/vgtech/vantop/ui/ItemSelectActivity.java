package com.vgtech.vantop.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.vgtech.common.ui.BaseActivity;
import com.vgtech.vantop.R;
import com.vgtech.vantop.adapter.ItemSelectedAdapter;
import com.vgtech.vantop.moudle.ItemSelectMoudle;

import java.util.ArrayList;
import java.util.List;

/***
 * 条目选择
 * 需要intent中传入参数
 * EXTRA_DATA ArrayList<ItemSelectMoudle> (Serializable)
 * EXTRA_MODE {ItemSelectedAdapter.SELECTED_MODE_IMG 右侧对勾选中
 *              ItemSelectAdapter.SELECTED_MODE_RADIOBTN 左侧radio选中}
 * EXTRA_CHECK_MODE CHECK_MODE_SINGLE 单选
 *                  CHECK_MODE_MULTI 多选
 * 返回选中结果EXTRA_RESAULT ArrayList<ItemSelectMoudle>
 */
public class ItemSelectActivity extends BaseActivity implements AdapterView.OnItemClickListener{

    public static final String EXTRA_DATA = "data";
    public static final String EXTRA_RESAULT = "resault";
    public static final String EXTRA_MODE = "mode";
    public static final String EXTRA_CHECK_MODE = "checkMode";
    private int mMode;
    private LinearLayout mLlBack;
    private ImageButton mTvRight;
    private ListView mListView;
    private ItemSelectedAdapter mAdapter;
    private ArrayList<ItemSelectMoudle> mDatas;
    private TextView mTvFinished;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initDatas();
        initViews();
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_item_select;
    }

    private void initViews() {
        mLlBack = (LinearLayout) findViewById(R.id.btn_back);
        mLlBack.setOnClickListener(this);
        //mLlBack.getChildAt(2).setVisibility(View.GONE);
        //mTvRight = (ImageButton) findViewById(R.id.btn_apply);
        //mTvRight.setVisibility(View.VISIBLE);
        //mTvRight.setOnClickListener(this);

        mListView = (ListView) findViewById(R.id.list_items);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mTvFinished = (TextView) findViewById(R.id.tv_submit);
        mTvFinished.setOnClickListener(this);
        setTitle(getString(R.string.vantop_selectactivity));
    }

    private void initDatas() {
        Intent intent = getIntent();
        mDatas = (ArrayList<ItemSelectMoudle>) intent.getSerializableExtra(EXTRA_DATA);
        if(mDatas == null) {
            mDatas = new ArrayList<>();
        }

        mMode = intent.getIntExtra(EXTRA_MODE,ItemSelectedAdapter.SELECTED_MODE_IMG);
        mAdapter = new ItemSelectedAdapter(this,mDatas);
        mAdapter.setMode(mMode);
        int checkMode = intent.getIntExtra(EXTRA_CHECK_MODE,ItemSelectedAdapter.CHECK_MODE_SINGLE);
        mAdapter.setCheckMode(checkMode);
    }

    @Override
    public void onClick(View v) {
        //super.onClick(v);
        if(v == mLlBack) {
            finish();
        }

        if(v == mTvFinished) {
            submitResault();
        }
    }

    private void submitResault() {

        List<Integer> sels = mAdapter.getSelected();
        if(sels.isEmpty()) {
            Toast.makeText(this,getString(R.string.vantop_select_null),Toast.LENGTH_SHORT).show();
            return;
        }
        ArrayList<ItemSelectMoudle> datas = new ArrayList<>();
        for(int i = 0 ; i < sels.size(); i++) {
            ItemSelectMoudle data = mDatas.get(sels.get(i));
            datas.add(data);
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_RESAULT,datas);
        setResult(RESULT_OK,intent);
        finish();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        mAdapter.setSelected(i);
    }
}
