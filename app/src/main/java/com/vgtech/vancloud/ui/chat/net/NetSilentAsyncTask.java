package com.vgtech.vancloud.ui.chat.net;

import android.content.Context;

/**
 * @author xuanqiang
 * @date 14-2-28
 */
public abstract class NetSilentAsyncTask<ResultT> extends NetAsyncTask<ResultT> {
  public NetSilentAsyncTask(final Context context) {
    super(context);
  }

  @Override
  protected void showProgress() {}

  @Override
  protected void showErrorText(String msg, int duration) {}
}
