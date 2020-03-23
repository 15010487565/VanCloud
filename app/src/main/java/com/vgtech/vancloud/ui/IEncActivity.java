
package com.vgtech.vancloud.ui;

import android.content.Context;
import android.view.View;

import com.vgtech.common.Constants;
import com.vgtech.vancloud.VanCloudApplication;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;

public interface IEncActivity extends View.OnClickListener, Constants {

    void finish();
    Context getApplicationContext();
    VanCloudApplication getAppliction();
    void notifyErrDlg(int callbackId,NetworkPath path,HttpListener<String> listener);
    void showError(com.vgtech.common.api.Error error);
}
