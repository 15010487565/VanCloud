package com.vgtech.vancloud.ui.chat;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.inject.Inject;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.models.Entity;
import com.vgtech.vancloud.ui.chat.controllers.XmppController;
import com.vgtech.vancloud.ui.chat.models.ChatGroup;
import com.vgtech.vancloud.ui.chat.net.NetEntityAsyncTask;

import roboguice.inject.InjectView;
import roboguice.util.Strings;

/**
 * @author xuanqiang
 */
public class MessageGroupNameFragment extends ActionBarFragment {

    @InjectView(R.id.message_group_name_edit)
    EditText editText;
    @InjectView(R.id.tv_right)
    TextView doneView;

    public static MessageGroupNameFragment newInstance(final ChatGroup group) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("group", group);
        MessageGroupNameFragment fragment = new MessageGroupNameFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        group = (ChatGroup) bundle.getSerializable("group");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return attachToSwipeBack(createContentView(R.layout.message_group_name));
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        titleView.setText(R.string.modify_chat_group_name);
        editText.setText(group.groupNick);
        doneView.setText(R.string.save);
        doneView.setVisibility(View.VISIBLE);
        doneView.setOnClickListener(this);
        editText.requestFocus();
        imManager.showSoftInput(editText, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public void onDestroyView() {
        imManager.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
        super.onDestroyView();
    }

    @Override
    public void onClick(View view) {
        if (view == doneView) {
            save();
        } else {
            super.onClick(view);
        }
    }

    public void save() {
        editText.setError(null);
        final String name = Strings.toString(editText.getText());
        if (TextUtils.isEmpty(name)) {
            editText.setError(getResources().getString(R.string.error_field_required));
            editText.requestFocus();
            imManager.showSoftInput(editText, InputMethodManager.HIDE_NOT_ALWAYS);
        } else {
            imManager.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
            new NetEntityAsyncTask<Entity>(controller.getActivity()) {
                @Override
                protected Entity doInBackground() throws Exception {
                    if (xmpp.modifyGroupName(group, name)) {
                        return new Entity();
                    }
                    return null;
                }

                @Override
                protected void success(Entity entity) throws Exception {
                    listener.onModifyGroupName(group.groupNick);
                    controller.fm().popBackStack();
                }
            }.execute();
        }
    }

    private ChatGroup group;
    MessageGroupNameListener listener;
    @Inject
    InputMethodManager imManager;
    @Inject
    XmppController xmpp;

}
