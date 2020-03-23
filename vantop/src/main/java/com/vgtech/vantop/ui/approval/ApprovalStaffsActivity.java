package com.vgtech.vantop.ui.approval;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.ui.BaseActivity;
import com.vgtech.vantop.R;
import com.vgtech.vantop.adapter.StaffAdapter;
import com.vgtech.vantop.moudle.StaffInfo;

import org.json.JSONArray;

import java.util.List;

/**
 * Created by Duke on 2016/9/30.
 */

public class ApprovalStaffsActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private ListView listView;
    private StaffAdapter adapter;


    @Override
    protected int getContentView() {
        return R.layout.approval_staffs_layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.vantop_select_approver));
        listView = (ListView) findViewById(R.id.list_view);

        String jsonText = getIntent().getStringExtra("json");
        try {
            if (!TextUtils.isEmpty(jsonText)) {
                List<StaffInfo> list = JsonDataFactory.getDataArray(StaffInfo.class, new JSONArray(jsonText));
                adapter = new StaffAdapter(this, list);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        StaffInfo staffInfo = adapter.getList().get(position);
        Intent intent = new Intent();
        intent.putExtra("json", staffInfo.getJson().toString());
        setResult(RESULT_OK, intent);
        finish();

    }
}
