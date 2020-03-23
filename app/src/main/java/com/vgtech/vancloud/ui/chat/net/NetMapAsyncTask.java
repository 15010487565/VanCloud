package com.vgtech.vancloud.ui.chat.net;

import android.content.Context;
import android.widget.Toast;

import com.vgtech.vancloud.R;

import java.util.Map;

/**
 * @param <ResultT>
 * @author xuanqiang
 */
public abstract class NetMapAsyncTask<ResultT extends Map> extends NetAsyncTask<ResultT>{

  public NetMapAsyncTask(final Context context){
    super(context);
  }

  @Override
  protected void onSuccess(ResultT map) throws Exception{
    super.onSuccess(map);
    if(map == null){
      showErrorText(context.getString(R.string.request_failure),Toast.LENGTH_SHORT);
    }else{
      if(handlerServerError(map)) {
        success(map);
      }
    }
  }

  protected abstract void success(ResultT map) throws Exception;

}
