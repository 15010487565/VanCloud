package com.vgtech.vancloud.ui.chat.net;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.vgtech.common.utils.FileUtils;
import com.vgtech.common.view.IphoneDialog;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.models.Entity;
import com.vgtech.vancloud.ui.chat.controllers.Controller;
import com.vgtech.vancloud.ui.chat.controllers.NetController;

import org.springframework.web.client.RestClientException;

import java.util.Map;

import roboguice.inject.ContextScopedProvider;
import roboguice.util.Ln;
import roboguice.util.RoboAsyncTask;

/**
 * @param <ResultT>
 * @author xuanqiang
 */
public abstract class NetAsyncTask<ResultT> extends RoboAsyncTask<ResultT> {

    protected boolean afterAnim;
    private IphoneDialog progressHUD;

    public void showLoadingDialog(String contentStr) {
        if (progressHUD == null) {
            progressHUD = new IphoneDialog(context);
        }
        progressHUD.setMessage(contentStr);
        progressHUD.show(true);
    }

    public NetAsyncTask(final Context context) {
        super(context);
    }

    protected void handlerServerError(final Entity entity) {
        if ("-2".equals(entity.code)) {
            FileUtils.writeString(context,"NetAsyncTask -> handlerServerError() 退出到登录界面！\r\n");
            controller.logout();
        } else {
            showErrorText(entity.msg, Toast.LENGTH_LONG);
        }
    }

    protected boolean isPwdVerify(final String code) {
        return "-3".equals(code);
    }

    protected boolean handlerServerError(final Map map) {
        Object err = map.get("_msg");
        if (err != null) {
            Entity entity = new Entity();
            entity.msg = (String) err;
            entity.code = (String) map.get("_code");
            if (entity.hasError()) {
                handlerServerError(entity);
                return false;
            }
        }
        return true;
    }

    protected void showProgress(final CharSequence message) {
        showLoadingDialog(String.valueOf(message));
    }

    public void hideProgress() {
        if (progressHUD != null) {
            progressHUD.dismiss();
            progressHUD = null;
        }
    }

    private long startTime;

    @Override
    protected void onPreExecute() throws Exception {
        startTime = System.currentTimeMillis();
        checkNetwork();
        if (cpuNum != 1) {
            showProgress();
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    showProgress();
                }
            }, animDelayTime - 200);
        }
    }

    abstract protected ResultT doInBackground() throws Exception;

    @Override
    public ResultT call() throws Exception {
        ResultT ret = doInBackground();
        waitAnimEnd();
        return ret;
    }

    private void waitAnimEnd() throws Exception {
        if (afterAnim) {
            long t = System.currentTimeMillis() - startTime;
            if (t < animDelayTime) {
                Thread.sleep(animDelayTime - t);
            }
        }
    }

    protected void showProgress() {
        showProgress(context.getString(R.string.loading));
    }

    public void checkNetwork() throws NetworkException {
        if (net().hasNetwork()) {
            return;
        }
        throw new NetworkException();
    }

    @Override
    protected void onThrowable(Throwable t) throws RuntimeException {
        if (t instanceof NetworkException) {
            showErrorText(context.getString(R.string.network_error), Toast.LENGTH_SHORT);
            Ln.d(t);
        } else if (t instanceof RestClientException) {
            showErrorText(context.getString(R.string.request_failure), Toast.LENGTH_SHORT);
            Ln.d(t);
        } else {
            if (t instanceof IllegalStateException) {
                if (t.getMessage().contains("Connection pool")) {
                    return;
                }
            }
            showErrorText(context.getString(R.string.operation_failure), Toast.LENGTH_SHORT);
            Ln.e(t);
        }
    }

    @SuppressWarnings("MagicConstant")
    protected void showErrorText(final String msg, final int duration) {
        Toast.makeText(context, msg, duration).show();
    }

    @Override
    protected void onFinally() throws RuntimeException {
        if (cpuNum != 1) {
            hideProgress();
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    hideProgress();
                }
            }, animDelayTime - 200);
            super.onFinally();
        }
    }

    @Override
    protected void onInterrupted(Exception e) {
//    Ln.e(e);
    }

    public boolean cancel() {
        if (netController != null) {
            if (netController.getResponse() != null) {
                netController.getResponse().close();
            }
            netController = null;
        }
        return this.future == null || super.cancel(true);
    }

    @SuppressWarnings("SynchronizeOnNonFinalField")
    protected NetController net() {
        synchronized (netControllerProvider) {
            if (netController == null) {
                netController = netControllerProvider.get(context);
            }
            return netController;
        }
    }

    private NetController netController;
    @Inject
    ContextScopedProvider<NetController> netControllerProvider;
    @Inject
    Controller controller;
    @Inject
    @Named("animTime")
    long animDelayTime;
    @Inject
    @Named("cpuNum")
    int cpuNum;

}
