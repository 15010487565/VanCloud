package com.vgtech.vancloud.ui.module;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.NewUser;
import com.vgtech.common.ui.BaseActivity;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.adapter.ReciverUserAdapter;
import com.vgtech.common.utils.UserUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

/**
 * 新版抄送人列表界面
 * Created by Duke on 2016/10/20.
 */

public class ReciverUserActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private ListView listView;
    private ReciverUserAdapter adapter;


    @Override
    protected int getContentView() {
        return R.layout.reciver_user_layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        if (!TextUtils.isEmpty(title)) {
            setTitle(title);
        } else {
            setTitle(getString(R.string.recruit_detail_copyer));
        }

        String json = getIntent().getStringExtra("json");

        listView = (ListView) findViewById(R.id.list_view);

        if (!TextUtils.isEmpty(json)) {
            try {
                List<NewUser> list = JsonDataFactory.getDataArray(NewUser.class, new JSONArray(json));
                adapter = new ReciverUserAdapter(list, this);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(this);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        NewUser user = adapter.getList().get(position);
        UserUtils.enterUserInfo(this, user.userid + "", user.name, user.photo);
    }
}
