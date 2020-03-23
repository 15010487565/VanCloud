package com.vgtech.common.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.android.volley.VolleyError;
import com.vgtech.common.R;
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
import java.util.List;

/**
 * Created by zhangshaofang on 2016/5/23.
 */
public class DictSelectActivity extends BaseActivity implements HttpListener<String>, OnItemClickListener {
    public static final int DICT_TITLE = 1;
    private DataAdapter<Dict> mAreaAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ListView listView = (ListView) findViewById(android.R.id.list);
        listView.setOnItemClickListener(this);
        mAreaAdapter = new DataAdapter<>(this);
        listView.setAdapter(mAreaAdapter);
        Intent intent = getIntent();
        String json = intent.getStringExtra("json");
        String title = intent.getStringExtra("title");

        setTitle(title);
        if (!TextUtils.isEmpty(json)) {
            try {
                List<Dict> dicts = JsonDataFactory.getDataArray(Dict.class, new JSONArray(json));
                String selectedId = intent.getStringExtra("id");
                String selectedName = getIntent().getStringExtra("name");
                if (TextUtils.isEmpty(selectedId) && !TextUtils.isEmpty(selectedName)) {
                    for (Dict area : dicts) {
                        List<Dict> areaList = area.getArrayData(Dict.class);
                        for (Dict d : areaList) {
                            if (selectedName.equals(d.name)) {
                                selectedId = d.getId();
                                break;
                            }
                        }
                        if (selectedName.equals(area.name)) {
                            selectedId = area.getId();
                            break;
                        }
                    }
                }


                mAreaAdapter.setSelectedId(selectedId);
                mAreaAdapter.add(dicts);
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
        String url = getIntent().getDataString();
        showLoadingDialog(this, "");
        ApplicationProxy proxy = (ApplicationProxy) getApplication();
        NetworkManager networkManager = proxy.getNetworkManager();
        NetworkPath path = new NetworkPath(url);
        path.setType(NetworkPath.TYPE_JSONARRAY);
        networkManager.load(1, path, this, true);
    }

    private boolean mInit;

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {
        dismisLoadingDialog();
        if (!TextUtils.isEmpty(rootData.responce)) {
            int type = getIntent().getIntExtra("type", 0);
            if (type == DICT_TITLE) {
                try {
                    if (mInit) {
                        return;
                    }
                    mInit = true;
                    List<Dict> list = JsonDataFactory.getDataArray(Dict.class, new JSONArray(rootData.responce));
                    List<Dict> areas = new ArrayList<>();
                    String selectedId = getIntent().getStringExtra("id");
                    String selectedName = getIntent().getStringExtra("name");
                    for (Dict area : list) {
                        List<Dict> areaList = area.getArrayData(Dict.class);
                        if (!areaList.isEmpty()) {
                            Dict first = areaList.get(0);
                            first.title = area.name;
                        }
                        if (TextUtils.isEmpty(selectedId) && !TextUtils.isEmpty(selectedName)) {
                            for (Dict d : areaList) {
                                if (selectedName.equals(d.name)) {
                                    selectedId = d.getId();
                                    break;
                                }
                            }
                            if (selectedName.equals(area.name)) {
                                selectedId = area.getId();
                                break;
                            }
                        }
                        areas.addAll(areaList);
                    }

                    mAreaAdapter.setSelectedId(selectedId);
                    mAreaAdapter.add(areas);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    if (mInit) {
                        return;
                    }
                    mInit = true;
                    List<Dict> list = JsonDataFactory.getDataArray(Dict.class, new JSONArray(rootData.responce));
                    String selectedId = getIntent().getStringExtra("id");
                    String selectedName = getIntent().getStringExtra("name");
                    if (TextUtils.isEmpty(selectedId) && !TextUtils.isEmpty(selectedName)) {
                        for (Dict area : list) {
                            List<Dict> areaList = area.getArrayData(Dict.class);
                            for (Dict d : areaList) {
                                if (selectedName.equals(d.name)) {
                                    selectedId = d.getId();
                                    break;
                                }
                            }
                            if (selectedName.equals(area.name)) {
                                selectedId = area.getId();
                                break;
                            }
                        }
                    }
                    mAreaAdapter.setSelectedId(selectedId);
                    mAreaAdapter.add(list);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object obj = parent.getItemAtPosition(position);
        if (obj instanceof Dict) {
            Dict area = (Dict) obj;
            if (area.getArrayData(Dict.class).isEmpty()) {
                Intent intent = new Intent();
                intent.putExtra("id", area.id);
                intent.putExtra("name", area.name);
                intent.putExtra("referCode", area.referCode);
                setResult(RESULT_OK, intent);
                finish();
            } else {
                Intent intent = new Intent(this, DictSelectActivity.class);
                intent.putExtra("title", area.name);
                String style = getIntent().getStringExtra("style");
                intent.putExtra("style", style);
                String selectedId = getIntent().getStringExtra("id");
                String selectedName = getIntent().getStringExtra("name");
                intent.putExtra("id", selectedId);
                intent.putExtra("name", selectedName);
                try {
                    intent.putExtra("json", area.getJson().getJSONArray("sub_data").toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                startActivityForResult(intent, 1001);
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            String id = data.getStringExtra("id");
            String name = data.getStringExtra("name");
            String referCode = data.getStringExtra("referCode");
            Intent intent = new Intent();
            intent.putExtra("id", id);
            intent.putExtra("name", name);
            intent.putExtra("referCode", referCode);
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}
