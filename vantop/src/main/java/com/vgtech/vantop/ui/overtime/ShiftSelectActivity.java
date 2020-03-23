package com.vgtech.vantop.ui.overtime;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.ui.BaseActivity;
import com.vgtech.vantop.R;
import com.vgtech.vantop.adapter.ShiftSelectAdapter;
import com.vgtech.vantop.moudle.ShiftSelect;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Duke on 2016/7/20.
 * 班值选择
 */
public class ShiftSelectActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private ListView listView;
    private ShiftSelectAdapter shiftSelectAdapter;
    private List<ShiftSelect> shifts = new ArrayList<>();
    private ShiftSelect checkedShiftSelect = null;

    @Override
    protected int getContentView() {
        return R.layout.activity_shift_select;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.vantop_shift_select));
        initRightTv(getString(R.string.vantop_confine));

        initData();
        listView = (ListView) findViewById(R.id.listview);
        shiftSelectAdapter = new ShiftSelectAdapter(this, shifts);
        listView.setAdapter(shiftSelectAdapter);
        listView.setOnItemClickListener(this);
    }


    private void initData() {
        Intent intent = getIntent();
        String shiftKey = intent.getStringExtra("shiftKey");
        String jsonText = intent.getStringExtra("json");
        if (!TextUtils.isEmpty(jsonText)) {
            try {
                shifts = JsonDataFactory.getDataArray(ShiftSelect.class, new JSONArray(jsonText));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!TextUtils.isEmpty(shiftKey)) {
            for (ShiftSelect shiftSelect : shifts) {
                if (shiftKey.equals(shiftSelect.shiftKey))
                    shiftSelect.checked = true;
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_right) {
            Intent intent = new Intent();

            if (checkedShiftSelect == null) {
                intent.putExtra("key", "");
                intent.putExtra("value", "");
            } else {
                intent.putExtra("key", checkedShiftSelect.shiftKey);
                intent.putExtra("value", checkedShiftSelect.shiftValue);
            }
            setResult(RESULT_OK, intent);
            finish();

        } else {
            super.onClick(v);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        List<ShiftSelect> list = shiftSelectAdapter.getLists();
        ShiftSelect shift = list.get(position);
        if (shift.checked) {
            checkedShiftSelect = shift;
            return;
        } else {
            for (ShiftSelect shiftSelect : list) {
                shiftSelect.checked = false;
            }
            ShiftSelect newshift = list.get(position);
            newshift.checked = true;
            checkedShiftSelect = newshift;
            shiftSelectAdapter.myNotifyDataSetChanged(list);
        }
    }
}
