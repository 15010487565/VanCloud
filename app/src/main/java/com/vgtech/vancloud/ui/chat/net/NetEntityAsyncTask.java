package com.vgtech.vancloud.ui.chat.net;

import android.content.Context;
import android.widget.Toast;

import com.vgtech.vancloud.R;
import com.vgtech.vancloud.models.Entity;


/**
 * @param <ResultT>
 * @author xuanqiang
 */
public abstract class NetEntityAsyncTask<ResultT extends Entity> extends NetAsyncTask<ResultT>{

  public NetEntityAsyncTask(final Context context){
    super(context);
  }

  @Override
  protected void onSuccess(ResultT entity) throws Exception{
    super.onSuccess(entity);
    if(entity == null){
      showErrorText(context.getString(R.string.request_failure),Toast.LENGTH_SHORT);
    }else if(entity.hasError()){
      handlerServerError(entity);
    }else{
      success(entity);
    }
  }

  protected abstract void success(ResultT entity) throws Exception;

}
