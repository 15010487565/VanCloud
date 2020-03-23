package com.vgtech.common.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.vgtech.common.R;
import com.vgtech.common.URLAddr;
import com.vgtech.common.adapter.DataAdapter;
import com.vgtech.common.api.Dict;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.listener.ApplicationProxy;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by zhangshaofang on 2016/5/23.
 */
public class JobDictSelectActivity extends BaseActivity implements HttpListener<String>, AdapterView.OnItemClickListener {
    public static final int HANGYE = 2;
    public static final int ZHINENG = 3;
    public static final int AREA = 4;
    private DataAdapter<Dict> mAreaAdapter;
    private LinearLayout mSelectedLayout;
    private LayoutInflater mInflater;
    private TextView mOpenTv;
    private ImageView mOpenIv;
    private TextView mCountTv;
    private boolean mOpen;
    private int MAX_COUNT = 5;
    private boolean mSub;
    private int mType;
    private TextView mSelectedTv;
    private int mLeave = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        initRightTv(getString(R.string.personal_submit));
        String style = getIntent().getStringExtra("style");
        if ("company".equals(style)) {
            View bgTitleBar = findViewById(R.id.bg_titlebar);
            bgTitleBar.setBackgroundColor(ContextCompat.getColor(this,R.color.comment_blue));
        }

        mInflater = getLayoutInflater();
        ListView listView = (ListView) findViewById(android.R.id.list);
        listView.setOnItemClickListener(this);
        mAreaAdapter = new DataAdapter<>(this);
        listView.setAdapter(mAreaAdapter);
        ViewStub vs_header = (ViewStub) findViewById(R.id.vs_header);
        vs_header.inflate();
        mSelectedTv = (TextView) findViewById(R.id.tv_selected_lable);
        mCountTv = (TextView) findViewById(R.id.count);
        mOpenTv = (TextView) findViewById(R.id.tv_open);
        mOpenIv = (ImageView) findViewById(R.id.iv_open);
        findViewById(R.id.btn_open).setOnClickListener(this);
        mSelectedLayout = (LinearLayout) findViewById(R.id.selected_layout);
        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mOpen)
                    openSelectedLayout(mOpen);
                return mOpen;
            }
        });
        Intent intent = getIntent();
        mSub = intent.getBooleanExtra("sub", false);
        String json = intent.getStringExtra("json");
        if (!TextUtils.isEmpty(json)) {
            String title = intent.getStringExtra("title");
            setTitle(title);
            try {
                List<Dict> dicts = JsonDataFactory.getDataArray(Dict.class, new JSONArray(json));
                String idStr = intent.getStringExtra("id");
                String nameStr = intent.getStringExtra("name");
                mType = getIntent().getIntExtra("type", 0);
                if (mType != AREA) {
                    Dict dict = new Dict();
                    dict.name = getString(R.string.personal_jobdict_zhineng);
                    dict.id = "all";
                    mAllDict = dict;
                }
                mLeave = intent.getIntExtra("leave", 0) + 1;
                switch (mType) {
                    case HANGYE:
                        mSelectedTv.setText(getString(R.string.personal_jobdict_hangye_select));
                        break;
                    case ZHINENG:
                        mSelectedTv.setText(getString(R.string.personal_jobdict_zhineng_select));
                        break;
                    case AREA:
                        mSelectedTv.setText(getString(R.string.personal_jobdict_choose_city));
                        Dict pDict = null;
                        if (dicts.size() == 1) {
                            Dict dict = dicts.get(0);
                            Dict d = new Dict();
                            d.id = dict.id;
                            d.name = dict.name;
                            d.isAll = true;
                            d.getArrayDatas().put(Dict.class, dicts);
                            pDict = d;
                            dicts = dict.getArrayData(Dict.class);
                        } else if (mLeave == 2) {
                            Dict subDict = new Dict();
                            String parent_id = intent.getStringExtra("parent_id");
                            String parent_name = intent.getStringExtra("parent_name");
                            subDict.id = parent_id;
                            subDict.name = parent_name;
                            subDict.isAll = true;
                            subDict.getArrayDatas().put(Dict.class, dicts);
                            pDict = subDict;

                        }
                        if (pDict != null) {
                            for (Dict d : dicts) {
                                d.pDict = pDict;
                            }
                            dicts.add(0, pDict);
                        }
                        mAllDictList = dicts;
                        break;
                }
                refreshSubData(idStr, nameStr, dicts);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else
            loadDictData();
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_data_select;
    }

    private void loadDictData() {
        String url = "";
        String title = "";
        int type = getIntent().getIntExtra("type", 0);
        mType = type;
        switch (type) {
            case HANGYE:
                url = URLAddr.URL_INDUSTRY;
                title = getString(R.string.personal_jobdict_hangye);
                mSelectedTv.setText(getString(R.string.personal_jobdict_hangye_select));
                break;
            case ZHINENG:
                url = URLAddr.URL_DICT_VANCLOUD_FUNCTIONS;
                title = getString(R.string.personal_jobdict_choose_zhineng);
                mSelectedTv.setText(getString(R.string.personal_jobdict_zhineng_select));
                break;
            case AREA:
                url = URLAddr.URL_DICT_VANCLOUD_CITYS;
                title = getString(R.string.personal_jobdict_city);
                mSelectedTv.setText(getString(R.string.personal_jobdict_choose_city));
                break;
        }
        setTitle(title);
        showLoadingDialog(this, "");
        ApplicationProxy proxy = (ApplicationProxy) getApplication();
        NetworkManager networkManager = proxy.getNetworkManager();
        NetworkPath path = new NetworkPath(url);
        path.setType(NetworkPath.TYPE_JSONARRAY);
        networkManager.load(type, path, this, true);
    }

    private boolean mInit;
    private Dict mAllDict;

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
        if (!TextUtils.isEmpty(rootData.responce)) {
            switch (callbackId) {
                case HANGYE:
                    try {
                        if (mInit) {
                            return;
                        }
                        mInit = true;
                        Intent intent = getIntent();
                        String idStr = intent.getStringExtra("id");
                        List<String> idList = new ArrayList<>();
                        if (!TextUtils.isEmpty(idStr)) {
                            idList.addAll(Arrays.asList(idStr.split(",")));
                        }
                        List<Dict> list = JsonDataFactory.getDataArray(Dict.class, new JSONArray(rootData.responce));
                        List<Dict> areas = new ArrayList<>();
                        Dict dict = new Dict();
                        dict.name = getString(R.string.personal_jobdict_all_hangye);
                        dict.id = "all";
                        mAllDict = dict;
                        areas.add(dict);
                        for (Dict area : list) {
                            List<Dict> areaList = area.getArrayData(Dict.class);
                            if (!areaList.isEmpty()) {
                                Dict first = areaList.get(0);
                                first.title = area.name;
                            }
                            areas.addAll(areaList);
                        }
                        if (idList.isEmpty()) {
                            idList.add(dict.getId());
                            mSelectedDict.add(dict);
                        } else {
                            for (Dict d : areas) {
                                if (idList.contains(d.getId())) {
                                    mSelectedDict.add(d);
                                }
                            }
                        }
                        mAreaAdapter.addSelected(idList);
                        mAreaAdapter.add(areas);
                        for (Dict d : mSelectedDict) {
                            View itemView = mInflater.inflate(R.layout.selected_item, null);
                            TextView nameTv = (TextView) itemView.findViewById(R.id.tv_selected_name);
                            itemView.setTag(d);
                            nameTv.setTag(itemView);
                            nameTv.setOnClickListener(this);
                            nameTv.setText(d.name);
                            mSelectedLayout.addView(itemView);
                        }
                        mCountTv.setText(mSelectedLayout.getChildCount() + "/" + MAX_COUNT);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case ZHINENG:
                    try {
                        if (mInit) {
                            return;
                        }
                        mInit = true;
                        Intent intent = getIntent();
                        String idStr = intent.getStringExtra("id");
                        String nameStr = intent.getStringExtra("name");
                        List<Dict> list = JsonDataFactory.getDataArray(Dict.class, new JSONArray(rootData.responce));
                        Dict dict = new Dict();
                        dict.name = getString(R.string.personal_jobdict_zhineng);
                        dict.id = "all";
                        mAllDict = dict;
                        List<Dict> areas = new ArrayList<>();
                        areas.add(mAllDict);
                        for (Dict area : list) {
                            List<Dict> areaList = area.getArrayData(Dict.class);
                            if (!areaList.isEmpty()) {
                                Dict first = areaList.get(0);
                                first.title = area.name;
                            }
                            areas.addAll(areaList);
                        }
                        mAllDictList = areas;
                        refreshData(idStr, nameStr, areas);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case AREA:
                    try {
                        if (mInit) {
                            return;
                        }
                        mInit = true;
                        Intent intent = getIntent();
                        String idStr = intent.getStringExtra("id");
                        String nameStr = intent.getStringExtra("name");
                        List<Dict> list = JsonDataFactory.getDataArray(Dict.class, new JSONArray(rootData.responce));
                        mAllDictList = list;
                        refreshData(idStr, nameStr, list);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    private List<Dict> mAllDictList;

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }

    private void openSelectedLayout(boolean open) {
        mOpen = !open;
        mOpenTv.setText(mOpen ? getString(R.string.personal_jobdict_shou) : getString(R.string.personal_jobdict_zhan));
        mOpenIv.setImageResource(mOpen ? R.mipmap.common_arrow_up : R.mipmap.common_arrow_down);
        mSelectedLayout.setVisibility(mOpen ? View.VISIBLE : View.GONE);
    }

    private boolean mClickFinish;

    @Override
    public void finish() {
        if (mSub) {
            StringBuilder ids = new StringBuilder();
            StringBuilder names = new StringBuilder();
            for (Dict dict : mSelectedDict) {
                ids.append(dict.getId()).append(",");
                names.append(dict.name).append(",");
            }
            if (!TextUtils.isEmpty(ids))
                ids.deleteCharAt(ids.length() - 1);
            if (!TextUtils.isEmpty(names))
                names.deleteCharAt(names.length() - 1);
            Intent intent = new Intent();
            intent.putExtra("id", ids.toString());
            intent.putExtra("name", names.toString());
            intent.putExtra("finish", mClickFinish);
            setResult(RESULT_OK, intent);
        }
        super.finish();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_selected_name) {
            View view = (View) v.getTag();
            Dict dict = (Dict) view.getTag();
            if (mAllDict != null && dict.equals(mAllDict)) {
                Toast.makeText(this, getString(R.string.personal_jobdict_msg), Toast.LENGTH_SHORT).show();
                return;
            }
            onClick(dict);
        } else if (id == R.id.btn_open) {
            openSelectedLayout(mOpen);
        } else if (id == R.id.tv_right) {
            mSub = true;
            mClickFinish = true;
            finish();
        } else {
            super.onClick(v);
        }
    }

    private void onClick(Dict dict) {


        List<Dict> currentSelectedDict = new ArrayList<>();
        currentSelectedDict.addAll(mSelectedDict);
        if (dict.isAll) {
            List<Dict> subs = dict.getArrayData(Dict.class);
            if (subs.size() == 1) {
                subs.addAll(subs.get(0).getArrayData(Dict.class));
            }
            currentSelectedDict.removeAll(subs);
//            for (Dict d : mSelectedDict) {
//                if (dict == d.pDict) {
//                    currentSelectedDict.remove(d);
//                }
//            }
        } else {
            if (dict.pDict != null && currentSelectedDict.contains(dict.pDict))
                currentSelectedDict.remove(dict.pDict);
        }
        mSelectedDict = currentSelectedDict;
        if (mAllDict != null) {
            if (dict.equals(mAllDict)) {
                mAreaAdapter.getSelectedList().clear();
                mSelectedDict.clear();
            } else {
                mSelectedDict.remove(mAllDict);
            }
        }
        if (mSelectedDict.contains(dict)) {
            mSelectedDict.remove(dict);
            if (mSelectedDict.isEmpty() && mAllDict != null)
                mSelectedDict.add(mAllDict);
        } else {
            if (mSelectedDict.size() >= MAX_COUNT) {
                Toast.makeText(this, getString(R.string.personal_jobdict_max_msg) + MAX_COUNT + getString(R.string.personal_jobdict_ge), Toast.LENGTH_SHORT).show();
                return;
            }
            mSelectedDict.add(dict);
        }
        mAreaAdapter.getSelectedList().clear();
        mSelectedLayout.removeAllViews();
        List<Dict> selectedDict = new ArrayList<>();
        for (Dict d : mSelectedDict) {
            mAreaAdapter.addSelectedId(d.getId());
            View itemView = mInflater.inflate(R.layout.selected_item, null);
            TextView nameTv = (TextView) itemView.findViewById(R.id.tv_selected_name);
            itemView.setTag(d);
            nameTv.setTag(itemView);
            nameTv.setOnClickListener(this);
            nameTv.setText(d.name);
            mSelectedLayout.addView(itemView);
        }
        if (mAllDictList != null)
            for (Dict area : mAllDictList) {
                int subCount = 0;
                List<Dict> areaList = area.getArrayData(Dict.class);
                for (Dict d : mSelectedDict) {
                    if (!area.isAll && areaList.contains(d)) {
                        d.pDict = area;
                        subCount += 1;
                    }
                }
                area.subSelect = subCount;
                if (mType == AREA) {
                    if (mSelectedDict.contains(area)&&!areaList.isEmpty()&&!area.isAll)
                        area.subSelect = -1;
                    else if (areaList.size() == 1 && mSelectedDict.contains(areaList.get(0))) {
                        area.subSelect = -1;
                    }
                }
            }
        mAreaAdapter.notifyDataSetChanged();
        mCountTv.setText(mSelectedLayout.getChildCount() + "/" + MAX_COUNT);
    }

    private List<Dict> mSelectedDict = new ArrayList<>();

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object obj = parent.getItemAtPosition(position);
        if (obj instanceof Dict) {
            Dict dict = (Dict) obj;
            if (dict.getArrayData(Dict.class).isEmpty() || dict.isAll) {
                openSelectedLayout(true);
                onClick(dict);
            } else {
                Intent intent = new Intent(this, JobDictSelectActivity.class);
                intent.putExtra("title", dict.name);
                intent.putExtra("leave", mLeave);
                intent.putExtra("parent_id", dict.getId());
                intent.putExtra("parent_name", dict.getName());
                intent.putExtra("style", getIntent().getStringExtra("style"));
                intent.putExtra("sub", true);
                intent.putExtra("type", mType);
                StringBuilder ids = new StringBuilder();
                StringBuilder names = new StringBuilder();
                for (Dict d : mSelectedDict) {
                    ids.append(d.getId()).append(",");
                    names.append(d.name).append(",");
                }
                if (!TextUtils.isEmpty(ids))
                    ids.deleteCharAt(ids.length() - 1);
                if (!TextUtils.isEmpty(names))
                    names.deleteCharAt(names.length() - 1);
                intent.putExtra("id", ids.toString());
                intent.putExtra("name", names.toString());
                try {
                    intent.putExtra("json", dict.getJson().getJSONArray("sub_data").toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                startActivityForResult(intent, 1001);
            }

        }
    }

    private void refreshData(String idStr, String nameStr, List<Dict> list) {
        mSelectedDict.clear();
        List<String> idList = new ArrayList<>();
        if (!TextUtils.isEmpty(idStr)) {
            idList.addAll(Arrays.asList(idStr.split(",")));
        }
        List<String> nameList = new ArrayList<>();
        if (!TextUtils.isEmpty(nameStr)) {
            nameList.addAll(Arrays.asList(nameStr.split(",")));
        }
        if (idList.isEmpty()) {
            if (mAllDict != null) {
                idList.add(mAllDict.getId());
                mSelectedDict.add(mAllDict);
            }
        } else {
            for (int i = 0; i < idList.size(); i++) {
                Dict d = new Dict();
                d.id = idList.get(i);
                if (!TextUtils.isEmpty(nameStr) && nameList.size() > 0) {
                    d.name = nameList.get(i);
                }
                mSelectedDict.add(d);
            }
        }
        for (Dict area : list) {
            int subCount = 0;
            List<Dict> areaList = area.getArrayData(Dict.class);
            List<Dict> subList = new ArrayList<>();
            subList.addAll(areaList);
            if (areaList.size() == 1) {
                subList.addAll(areaList.get(0).getArrayData(Dict.class));
            }
            for (Dict d : mSelectedDict) {
                if (subList.contains(d)) {
                    d.pDict = area;
                    subCount += 1;
                }
            }
            area.subSelect = subCount;
            if (mType == AREA) {
                if (mSelectedDict.contains(area)&&!areaList.isEmpty()&&!area.isAll)
                    area.subSelect = -1;
                else if (areaList.size() == 1 && mSelectedDict.contains(areaList.get(0))) {
                    area.subSelect = -1;
                }
            }
        }
        mAreaAdapter.getSelectedList().clear();
        mAreaAdapter.clear();
        mAreaAdapter.addSelected(idList);
        mAreaAdapter.add(list);
        mSelectedLayout.removeAllViews();
        for (Dict d : mSelectedDict) {
            View itemView = mInflater.inflate(R.layout.selected_item, null);
            TextView nameTv = (TextView) itemView.findViewById(R.id.tv_selected_name);
            itemView.setTag(d);
            nameTv.setTag(itemView);
            nameTv.setOnClickListener(this);
            nameTv.setText(d.name);
            mSelectedLayout.addView(itemView);
        }
        mCountTv.setText(mSelectedLayout.getChildCount() + "/" + MAX_COUNT);
    }

    private void refreshSubData(String idStr, String nameStr, List<Dict> list) {
        List<String> idList = new ArrayList<>();
        if (!TextUtils.isEmpty(idStr)) {
            idList.addAll(Arrays.asList(idStr.split(",")));
        }
        List<String> nameList = new ArrayList<>();
        if (!TextUtils.isEmpty(nameStr)) {
            nameList.addAll(Arrays.asList(nameStr.split(",")));
        }
        if (idList.isEmpty()) {
            if (mAllDict != null) {
                idList.add(mAllDict.getId());
                mSelectedDict.add(mAllDict);
            }
        } else {
            for (int i = 0; i < idList.size(); i++) {
                Dict d = new Dict();
                d.id = idList.get(i);
                if (!TextUtils.isEmpty(nameStr) && nameList.size() > 0) {
                    d.name = nameList.get(i);
                }
                mSelectedDict.add(d);
            }
        }
        mAreaAdapter.addSelected(idList);
        mAreaAdapter.add(list);
        for (Dict d : mSelectedDict) {
            View itemView = mInflater.inflate(R.layout.selected_item, null);
            TextView nameTv = (TextView) itemView.findViewById(R.id.tv_selected_name);
            itemView.setTag(d);
            nameTv.setTag(itemView);
            nameTv.setOnClickListener(this);
            nameTv.setText(d.name);
            mSelectedLayout.addView(itemView);
        }
        mCountTv.setText(mSelectedLayout.getChildCount() + "/" + MAX_COUNT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            String id = data.getStringExtra("id");
            String name = data.getStringExtra("name");
            boolean finish = data.getBooleanExtra("finish", false);
            refreshData(id, name, mAllDictList);
            if (finish) {
                mSub = true;
                finish();
            }
        }
    }
}
