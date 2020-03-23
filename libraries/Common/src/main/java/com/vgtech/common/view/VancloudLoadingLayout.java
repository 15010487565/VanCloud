package com.vgtech.common.view;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.mapapi.map.Text;
import com.vgtech.common.R;
import com.vgtech.common.view.progressbar.ProgressWheel;

/**
 * Created by Duke on 2016/11/11.
 */

public class VancloudLoadingLayout extends RelativeLayout implements View.OnClickListener {

    private ImageView iconView;
    private TextView messageView;
    private ProgressWheel loadingView;
    private Context context;
    private LinearLayout clickView;
    private DoLoadAgain doLoadAgain;


    public VancloudLoadingLayout(Context context) {
        super(context);
        init(context);
    }

    public VancloudLoadingLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VancloudLoadingLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        View rootView = inflate(getContext(), R.layout.vancloud_load_layout, this);

        iconView = (ImageView) rootView.findViewById(R.id.image_icon);
        loadingView = (ProgressWheel) rootView.findViewById(R.id.progress_view);
        messageView = (TextView) rootView.findViewById(R.id.message_text);
        clickView = (LinearLayout) rootView.findViewById(R.id.cilck_layout);
        clickView.setOnClickListener(this);
        this.setOnClickListener(this);

    }

    /**
     * 显示加载错误提示
     *
     * @param contentView
     * @param message     显示信息
     * @param showIcon    是否显示错误icon
     * @param showMessage 是否显示文字提示
     */
    public void showErrorView(View contentView, String message, boolean showIcon, boolean showMessage) {
        this.setVisibility(VISIBLE);
        contentView.setVisibility(INVISIBLE);
        loadingView.setVisibility(GONE);
        if (showMessage) {
            messageView.setVisibility(VISIBLE);
            if (TextUtils.isEmpty(message))
                messageView.setText(context.getString(R.string.load_fail_prompt));
            else
                messageView.setText(message);
        } else
            messageView.setVisibility(GONE);

        if (showIcon) {
            iconView.setImageResource(R.mipmap.load_fail_icon);
            iconView.setVisibility(VISIBLE);
        } else
            iconView.setVisibility(GONE);

        clickView.setClickable(true);

    }

    /**
     * 显示默认加载错误信息页面
     *
     * @param contentView
     */
    public void showErrorView(View contentView) {
        this.setVisibility(VISIBLE);
        contentView.setVisibility(INVISIBLE);
        loadingView.setVisibility(GONE);
        messageView.setVisibility(VISIBLE);
        messageView.setText(context.getString(R.string.load_fail_prompt));
        iconView.setImageResource(R.mipmap.load_fail_icon);
        iconView.setVisibility(VISIBLE);
        clickView.setClickable(true);
    }

    public void showLoadingView(View contentView, String message, boolean showMessage) {

        this.setVisibility(VISIBLE);
        contentView.setVisibility(INVISIBLE);
        iconView.setVisibility(GONE);
        loadingView.setVisibility(VISIBLE);
        if (showMessage) {
            messageView.setVisibility(VISIBLE);
            if (TextUtils.isEmpty(message))
                messageView.setText(context.getString(R.string.dataloading));
            else
                messageView.setText(message);
        } else
            messageView.setVisibility(GONE);
        clickView.setClickable(false);
    }

    public void showEmptyView(View contentView, String message, boolean showIcon, boolean showMessage) {

        this.setVisibility(VISIBLE);
        contentView.setVisibility(INVISIBLE);
        loadingView.setVisibility(GONE);
        if (showMessage) {
            messageView.setText(message);
            messageView.setVisibility(VISIBLE);
        } else
            messageView.setVisibility(GONE);

        if (showIcon) {
            iconView.setImageResource(R.mipmap.empty_data_logo);
            iconView.setVisibility(VISIBLE);
        } else
            iconView.setVisibility(GONE);
        clickView.setClickable(false);
    }

    public void showHtmlMessage(String message) {
        messageView.setText(Html.fromHtml(message));
    }

    public void onlyShowMessage(String message) {

        this.setVisibility(VISIBLE);
        loadingView.setVisibility(GONE);
        iconView.setVisibility(GONE);
        messageView.setVisibility(VISIBLE);
        messageView.setText(message);
        clickView.setClickable(false);

    }

    public void dismiss(View contentView) {
        this.setVisibility(GONE);
        iconView.setVisibility(GONE);
        messageView.setVisibility(GONE);
        loadingView.setVisibility(GONE);
        contentView.setVisibility(VISIBLE);
        clickView.setClickable(false);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.cilck_layout) {
            if (doLoadAgain != null) {
                v.setClickable(false);
                doLoadAgain.loadAgain();
            }
        } else {

        }
    }

    public DoLoadAgain getDoLoadAgain() {
        return doLoadAgain;
    }

    public void setDoLoadAgain(DoLoadAgain doLoadAgain) {
        this.doLoadAgain = doLoadAgain;
    }

    public interface DoLoadAgain {
        void loadAgain();
    }
}
