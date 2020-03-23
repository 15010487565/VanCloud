package com.vgtech.vancloud.ui.chat;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.google.inject.Inject;
import com.vgtech.common.Constants;
import com.vgtech.common.PrfUtils;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.models.Staff;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.chat.controllers.AvatarController;
import com.vgtech.vancloud.ui.chat.controllers.Controller;
import com.vgtech.vancloud.ui.chat.controllers.XmppController;
import com.vgtech.vancloud.ui.chat.models.ChatGroup;

import java.util.ArrayList;
import java.util.List;

import roboguice.event.EventManager;

/**
 * Created by vic on 2016/8/12.
 */
public class SingleChatActivity extends BaseActivity {
    @Inject
    AvatarController avatarController;
    @Inject
    XmppController xmpp;
    @Inject
    EventManager eventManager;
    @Inject
    public Controller controller;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        List<Staff> contactses = new ArrayList<Staff>();
        Staff staff = new Staff(Constants.SERVICE_USERID, Constants.SERVICE_USERID, getString(R.string.vancloud_coustom_service), "", PrfUtils.getTenantId(this));
        contactses.add(staff);
        UsersMessagesFragment fragment = UsersMessagesFragment.newInstance(
                ChatGroup.fromStaff(contactses.get(0), PrfUtils.getUserId(this), PrfUtils.getTenantId(this)), null);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, fragment, null);
        ft.commitAllowingStateLoss();
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_single_chat;
    }
}
