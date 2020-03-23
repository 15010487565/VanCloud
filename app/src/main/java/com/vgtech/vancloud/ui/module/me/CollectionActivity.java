package com.vgtech.vancloud.ui.module.me;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.vgtech.vancloud.R;
import com.vgtech.vancloud.ui.BaseActivity;

/**
 * Created by frances on 2015/9/24.
 */
public class CollectionActivity extends BaseActivity{
    private RelativeLayout sharecollection;
    private RelativeLayout helpcollection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.collection_me));
        initView();
    }

    @Override
    protected int getContentView() {
        return R.layout.collection_list;
    }
    private void initView() {
        sharecollection = (RelativeLayout) findViewById(R.id.share_collection);
        helpcollection = (RelativeLayout) findViewById(R.id.help_collection);
        sharecollection.setOnClickListener(this);
        helpcollection.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.share_collection:
                Intent intent = new Intent(this, ShareCollectionActivity.class);
                startActivity(intent);
                break;
            case R.id.help_collection:
                Intent intent1 = new Intent(this, HelpCollectionActivity.class);
                startActivity(intent1);
                break;
            default:
                super.onClick(v);
                break;
        }
    }
}
