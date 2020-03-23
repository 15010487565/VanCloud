package com.vgtech.common.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.vgtech.common.R;
import com.vgtech.common.adapter.DataAdapter;
import com.vgtech.common.api.IdName;
import com.vgtech.common.api.JsonDataFactory;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangshaofang on 2016/5/17.
 */
public class DataProviderActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    public static final int TIAOCAO = 1;//跳槽次数
    public static final int GONGZUONIANXIAN = 2;//工作年限
    public static final int GONGSIXINGZHI = 3;//公司性质
    public static final int GONGSIGUIMO = 4;//公司规模
    public static final int CHUANYEJINGLI = 5;//创业经历
    public static final int XINZIFANWEI = 6;//薪资范围
    public static final int ZHIWEISHU = 7;//职位数
    public static final int FABURIQI = 8;//发布日期
    public static final int CITY = 9;//城市
    public static final int ZHIYE = 10;//职业
    public static final int ZHUANYE = 11;//专业
    public static final int XUELI = 12;//学历
    public static final int HUNYIN = 13;//婚姻
    public static final int HANGYE = 14;//行业
    public static final int AGE = 15;//年龄
    private DataAdapter<IdName> mDataAdapater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int type = getIntent().getIntExtra("type", 0);
        String title = getIntent().getStringExtra("title");
        String id = getIntent().getStringExtra("id");
        String style = getIntent().getStringExtra("style");
        if ("company".equals(style)) {
            View bgTitleBar = findViewById(R.id.bg_titlebar);
            bgTitleBar.setBackgroundColor(ContextCompat.getColor(this,R.color.comment_blue));
        }

        setTitle(title);
        int rawId = -1;
        switch (type) {
            case GONGZUONIANXIAN:
                rawId = R.raw.gongzuonianxian;
                if(TextUtils.isEmpty(title))
                setTitle(getString(R.string.personal_choose_work_years));
                break;
            case GONGSIXINGZHI:
                setTitle(getString(R.string.personal_choose_company_type));
                rawId = R.raw.gongsixingzhi;
                break;
            case GONGSIGUIMO:
                setTitle(getString(R.string.personal_choose_company_size));
                rawId = R.raw.gongsiguimo;
                break;
            case CHUANYEJINGLI:
                setTitle(getString(R.string.personal_choose_chuangye));
                rawId = R.raw.chuangyejingli;
                break;
            case XINZIFANWEI:
                setTitle(getString(R.string.personal_choose_pay));
                rawId = R.raw.xinzifanwei;
                break;
            case ZHIWEISHU:
                rawId = R.raw.zhiweishu;
                setTitle(getString(R.string.personal_choose_zhiwei_num));
                break;
            case FABURIQI:
                setTitle(getString(R.string.personal_choose_fabu));
                rawId = R.raw.faburiqi;
                break;
            case CITY:
                rawId = R.raw.city;
                setTitle(getString(R.string.personal_choose_city));
                break;
            case ZHIYE:
                setTitle(getString(R.string.personal_choose_zhiye));
                rawId = R.raw.zhiye;
                break;
            case ZHUANYE:
                setTitle(getString(R.string.personal_choose_zhuanye));
                rawId = R.raw.zhuanye;
                break;
            case XUELI:
                setTitle(getString(R.string.personal_choose_xueli));
                rawId = R.raw.xueli;
                break;
            case HUNYIN:
                setTitle(getString(R.string.personal_choose_hunyin));
                rawId = R.raw.hunyin;
                break;
            case HANGYE:
                setTitle(getString(R.string.personal_choose_hangye));
                rawId = R.raw.hangye;
                break;
            case AGE:
                setTitle(getString(R.string.personal_choose_age));
                rawId = R.raw.hangye;
                break;
        }
        ListView listView = (ListView) findViewById(android.R.id.list);
        mDataAdapater = new DataAdapter<>(this);
        if (!TextUtils.isEmpty(id)) {
            mDataAdapater.setSelectedId(id);
        }
        listView.setAdapter(mDataAdapater);
        listView.setOnItemClickListener(this);
        List<IdName> idNames = null;
        if (type == AGE) {
            idNames = new ArrayList<>();
            for (int i = 16; i <= 70; i++) {
                idNames.add(new IdName(String.valueOf(i), String.valueOf(i)));
            }
        } else if (rawId == -1) {
            Toast.makeText(this, "UnknowType", Toast.LENGTH_SHORT).show();
            finish();
            return;
        } else {
            InputStream inputStream = getResources().openRawResource(rawId);
            String json = new String(read(inputStream));
            try {
                JSONObject jsonObject = new JSONObject(json);
                idNames = JsonDataFactory.getDataArray(IdName.class, jsonObject.getJSONArray("data"));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mDataAdapater.add(idNames);
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_data_select;
    }

    public static byte[] read(InputStream inStream) {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        try {
            while ((len = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            inStream.close();
            return outStream.toByteArray();
        } catch (IOException e) {
        }
        return new byte[0];
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object obj = parent.getItemAtPosition(position);
        if (obj instanceof IdName) {
            IdName tmpName = (IdName) obj;
            Intent intent = new Intent();
            intent.putExtra("id", tmpName.id);
            intent.putExtra("name", tmpName.name);
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}
