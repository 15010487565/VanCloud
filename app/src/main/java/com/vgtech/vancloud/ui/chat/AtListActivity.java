package com.vgtech.vancloud.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.vgtech.vancloud.R;
import com.vgtech.common.api.NewUser;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.adapter.PraiseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangshaofang on 2015/11/19.
 */
public class AtListActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.lable_common_user));
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String photo = intent.getStringExtra("photo");
        ListView listView = (ListView) findViewById(R.id.atlist);

        String[] names = name.split(",");
        String[] photos = photo.split(",");
        List<NewUser> list = new ArrayList<NewUser>();
        for (int i = 0; i < names.length; i++) {
            list.add(new NewUser("", names[i], photos[i]));
        }
        PraiseAdapter praiseAdapter = new PraiseAdapter(this, list);
        listView.setAdapter(praiseAdapter);
        listView.setOnItemClickListener(this);
    }

    @Override
    protected int getContentView() {
        return R.layout.atlist;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object obj = parent.getItemAtPosition(position);
        if (obj instanceof NewUser) {
            NewUser user = (NewUser) obj;
            Intent intent = new Intent();
            intent.putExtra("name", user.name);
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}
