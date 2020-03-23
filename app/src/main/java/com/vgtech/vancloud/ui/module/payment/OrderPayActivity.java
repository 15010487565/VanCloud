package com.vgtech.vancloud.ui.module.payment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;
import com.android.volley.VolleyError;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.AppPermission;
import com.vgtech.common.api.BalanceInfo;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkManager;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.ui.PasswordFragment;
import com.vgtech.common.utils.MD5;
import com.vgtech.common.view.AlertDialog;
import com.vgtech.common.wxapi.Constants;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.alipay.PayResult;
import com.vgtech.vancloud.presenter.AppPermissionPresenter;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.BaseActivity;
import com.vgtech.vancloud.ui.FindPwdStartActivity;
import com.vgtech.vancloud.ui.module.financemanagement.NewOrderDetailActivity;
import com.vgtech.vancloud.ui.view.BalancePaymentDialog;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.vgtech.common.ui.PasswordFragment.COMPANYUSER;


/**
 * 订单支付
 * Created by Duke on 2016/4/6.
 */
public class OrderPayActivity extends BaseActivity implements HttpListener<String> {

    private ImageView balanceImag;
    private ImageView aliImag;
    private ImageView wxImag;

    private int paymentMethod = 1;

    private TextView orderTypeView;
    private TextView orderDescriptionView;
    private TextView orderIdView;
    private TextView needMoneyView;

    private String orderid;
    private String orderdescription;
    private String amount;
    private String ordertype;
    private int position;


    protected static final int REQUESTCODE = 200;
    public static final String WXPAY = "WXPAY";

    public static final String INVESTIGATE = "investigate";
    public static final String RECRUIT = "recruit";
    public static final String MEETING = "meeting";

    private final int CALLBACK_WXPAY = 1;
    private final int CALLBACK_ALIPAY = 2;
    private final int CALLBACK_ALIPAYRESULT = 3;
    private final int CALLBACK_WXPAYRESULT = 4;
    private static final int CALLBACK_BALANCEINFO = 5;
    private static final int CALLBACK_TO_PAY = 6;
    private NetworkManager mNetworkManager;

    private boolean payresult;

    private boolean isFromDetails;

    private int userType;

    BalancePaymentDialog dialog;

    @Override
    protected int getContentView() {
        return R.layout.order_pay_layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.order_pay));
        setSwipeBackEnable(false);
        api = WXAPIFactory.createWXAPI(this, Constants.APP_ID);

        Intent intent = getIntent();
        orderid = intent.getStringExtra("orderid");
        orderdescription = intent.getStringExtra("orderdescription");
        amount = intent.getStringExtra("amount");
        ordertype = intent.getStringExtra("ordertype");
        position = intent.getIntExtra("position", -1);
        isFromDetails = intent.getBooleanExtra("isfromdetails", false);
        userType = intent.getIntExtra("userType", 1);

        initView();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WXPAY);
        registerReceiver(mReceiver, intentFilter);

        mNetworkManager = getAppliction().getNetworkManager();
    }
    public void initView() {

        balanceImag = (ImageView) findViewById(R.id.balance_imag);
        aliImag = (ImageView) findViewById(R.id.ali_imag);
        wxImag = (ImageView) findViewById(R.id.wx_imag);

        orderTypeView = (TextView) findViewById(R.id.order_type);
        orderDescriptionView = (TextView) findViewById(R.id.order_description);
        orderIdView = (TextView) findViewById(R.id.order_id);
        needMoneyView = (TextView) findViewById(R.id.need_money);

        orderIdView.setText(orderid);
        orderDescriptionView.setText(orderdescription);
        needMoneyView.setText(String.format(getString(R.string.balance_money), amount));
//        if (INVESTIGATE.equals(ordertype))
//            orderTypeView.setDettailText(getString(R.string.lable_investigate));
//        else if (MEETING.equals(ordertype))
//            orderTypeView.setDettailText(getString(R.string.lable_vidio_metting));
//        else if (RECRUIT.equals(ordertype))
//            orderTypeView.setDettailText(getString(R.string.buy_resume));

        orderTypeView.setText(ordertype);
        findViewById(R.id.balance_pay).setOnClickListener(this);
        findViewById(R.id.ali_pay).setOnClickListener(this);
        findViewById(R.id.wx_pay).setOnClickListener(this);
        Button payButton = (Button) findViewById(R.id.btn_pay);
        payButton.setOnClickListener(this);
        findViewById(R.id.order_info_header).setOnClickListener(this);
        // 招聘购买简历、视频会议、背景调查 余额支付 权限判断
        if ((AppPermissionPresenter.hasPermission(this, AppPermission.Type.zhaopin, AppPermission.Zhaopin.pay.toString())) ||
                AppPermissionPresenter.hasPermission(this, AppPermission.Type.meeting, AppPermission.Zhaopin.pay.toString()) ||
                AppPermissionPresenter.hasPermission(this, AppPermission.Type.beidiao, AppPermission.Beidiao.pay.toString())) {
            findViewById(R.id.balance_pay).setVisibility(View.VISIBLE);
            balanceImag.setSelected(true);
            paymentMethod = 1;
        } else {
            findViewById(R.id.balance_pay).setVisibility(View.GONE);
            aliImag.setSelected(true);
            paymentMethod = 2;
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.balance_pay:
                balanceImag.setSelected(true);
                aliImag.setSelected(false);
                wxImag.setSelected(false);
                paymentMethod = 1;
                break;

            case R.id.ali_pay:
                balanceImag.setSelected(false);
                aliImag.setSelected(true);
                wxImag.setSelected(false);
                paymentMethod = 2;
                break;

            case R.id.wx_pay:
                balanceImag.setSelected(false);
                aliImag.setSelected(false);
                wxImag.setSelected(true);
                paymentMethod = 3;
                break;

            case R.id.btn_pay:

                switch (paymentMethod) {
                    case 1:
                        //TODO 余额支付
//                        Intent intent = new Intent(this, PayActivity.class);
//                        intent.putExtra("usertype", userType);
//                        intent.putExtra("order_id", orderid);
//                        intent.putExtra("order_total", amount);
//                        startActivityForResult(intent, REQUESTCODE);

                        if (TextUtils.isEmpty(orderid)) {

                            new BalancePaymentDialog(this).builder().setMsg(getString(R.string.not_using_order)).setOneButton("", null).show();

                        } else {
                            getBalanceInfo();
                        }
                        break;

                    case 2:
                        //TODO 支付宝支付
                        getAliPayInfo();
                        break;

                    case 3:
                        //TODO 微信支付
                        if (!api.isWXAppInstalled() || !api.isWXAppSupportAPI()) {
                            Toast.makeText(OrderPayActivity.this, getString(R.string.please_install_weixin), Toast.LENGTH_SHORT).show();
                        } else {
                            getWxPayInfo();
                        }
                        break;
                }


                break;

            case R.id.order_info_header:
                if (userType == COMPANYUSER) {
                    Intent intent = new Intent(OrderPayActivity.this, NewOrderDetailActivity.class);
                    intent.putExtra("infoid", orderid);
                    intent.putExtra("ifshowpaybutton", false);
                    startActivity(intent);
                }
                break;

            case R.id.btn_back:
                onBackPressed();
                break;

            default:
                super.onClick(v);
                break;

        }
    }

    private IWXAPI api;

    /**
     * 微信支付
     */
    public void wxPay(RootData rootData) {

        PayReq req = new PayReq();
        try {
            String wxPayInfo = rootData.getJson().getJSONObject("data").toString();
            JSONObject json = new JSONObject(wxPayInfo);
            req.appId = json.getString("appid");
            req.partnerId = json.getString("partnerid");
            req.prepayId = json.getString("prepayid");
            req.nonceStr = json.getString("noncestr");
            req.timeStamp = json.getString("timestamp");
            req.packageValue = json.getString("package");
            req.sign = json.getString("sign");
            req.extData = "app data"; // optional
        } catch (Exception e) {
            e.printStackTrace();
        }
//        Toast.makeText(OrderPayActivity.this, "正常调起支付", Toast.LENGTH_SHORT).show();
        // 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
        api.sendReq(req);
    }

    public void getAliPayInfo() {

        showLoadingDialog(this, getString(R.string.prompt_info_02));
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(this));
        if (userType == PasswordFragment.INDIVIDUALUSER) {
            params.put("tenant_id", "0");
        } else
            params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("order_info_id", orderid);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_ALIPAY_PREPAY), params, this);
        mNetworkManager.load(CALLBACK_ALIPAY, path, this, false);
    }


    public void getWxPayInfo() {

        showLoadingDialog(this, getString(R.string.prompt_info_02));
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(this));
        if (userType == PasswordFragment.INDIVIDUALUSER) {
            params.put("tenant_id", "0");
        } else
            params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("order_info_id", orderid);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_WXPAY_PREPAY), params, this);
        mNetworkManager.load(CALLBACK_WXPAY, path, this, false);
    }

    public void getAliPayResult() {

        showLoadingDialog(this, getString(R.string.prompt_info_02));
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(this));
        if (userType == PasswordFragment.INDIVIDUALUSER) {
            params.put("tenant_id", "0");
        } else
            params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("out_trade_no", orderid);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_ALIPAY_APP_QUERY_TRADE), params, this);
        mNetworkManager.load(CALLBACK_ALIPAYRESULT, path, this, false);
    }

    public void getWxPayResult() {

        showLoadingDialog(this, getString(R.string.prompt_info_02));
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(this));
        if (userType == PasswordFragment.INDIVIDUALUSER) {
            params.put("tenant_id", "0");
        } else
            params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("out_trade_no", orderid);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_WXPAY_APP_QUERY_TRADE), params, this);
        mNetworkManager.load(CALLBACK_WXPAYRESULT, path, this, false);
    }

    public void getBalanceInfo() {

        showLoadingDialog(this, getString(R.string.prompt_info_02));
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("tenant_id", PrfUtils.getTenantId(this));
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_ACCOUNTS_BALANCE), params, this);
        mNetworkManager.load(CALLBACK_BALANCEINFO, path, this);
    }


    //支付订单
    public void balancePay(String password) {

        showLoadingDialog(this, getString(R.string.prompt_info_02));
        Map<String, String> params = new HashMap<String, String>();
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("order_info_id", orderid);
        params.put("payment_password", MD5.getMD5(password));
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_ORDERS_PAYMENT), params, this);
        mNetworkManager.load(CALLBACK_TO_PAY, path, this);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {

        dismisLoadingDialog();
        boolean safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
        if (!safe) {
            switch (callbackId) {
                case CALLBACK_ALIPAYRESULT:
                case CALLBACK_WXPAYRESULT:
                    payresult = false;
                    if (userType == COMPANYUSER) {
                        Intent intent = new Intent(OrderPayActivity.this, PayResultActivity.class);
                        intent.putExtra("paymentmethod", paymentMethod);
                        intent.putExtra("ordertype", ordertype);
                        intent.putExtra("payresult", payresult);
                        startActivityForResult(intent, REQUESTCODE);
                    }

                    break;
                case CALLBACK_TO_PAY:

                    final BalancePaymentDialog balancePaymentDialog = new BalancePaymentDialog(this).builder();

                    if (rootData.code == 1000)
                        balancePaymentDialog.setMsg(rootData.msg).setOneButton("", null).show();
                    else
                        balancePaymentDialog.setMsg(rootData.msg).setNegativeButton(getString(R.string.cancel_dialog), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (dialog == null) {
                                    getBalanceInfo();
                                } else {
                                    dialog.getEditer().setText("");
                                    dialog.show();
                                }
                            }
                        }).setPositiveButton(getString(R.string.forget_password), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                dialog.dismiss();
                                Intent intent = new Intent(OrderPayActivity.this, FindPwdStartActivity.class);
                                startActivity(intent);
                            }
                        }).show();

                    break;
            }
            return;
        }
        switch (callbackId) {
            case CALLBACK_ALIPAY:
                try {
                    String aliPayInfo = rootData.getJson().getJSONObject("data").getString("payInfo");
                    aliPay(aliPayInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case CALLBACK_WXPAY:
                wxPay(rootData);
                break;

            case CALLBACK_ALIPAYRESULT:
            case CALLBACK_WXPAYRESULT:
                if (rootData.code == 200) {
                    payresult = true;
                    if (userType == COMPANYUSER) {
                        Intent intent = new Intent(OrderPayActivity.this, PayResultActivity.class);
                        intent.putExtra("paymentmethod", paymentMethod);
                        intent.putExtra("ordertype", ordertype);
                        intent.putExtra("payresult", payresult);
                        startActivityForResult(intent, REQUESTCODE);
                    }
                }
                break;

            case CALLBACK_BALANCEINFO:

                BalanceInfo balanceInfo;
                try {
                    JSONObject resutObject = rootData.getJson().getJSONObject("data");
                    balanceInfo = JsonDataFactory.getData(BalanceInfo.class, resutObject);
                    dialog = new BalancePaymentDialog(this).builder()
                            .setTitle(getString(R.string.payment_dialog_title))
                            .setInfo(balanceInfo.balance, amount).setPositiveButton("", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    EditText editText = dialog.getEditer();
                                    String password = editText.getText().toString();
                                    if (TextUtils.isEmpty(password)) {
                                        Toast.makeText(OrderPayActivity.this, getString(R.string.prompt_pass), Toast.LENGTH_SHORT).show();
                                    } else {
                                        dialog.dismiss();
                                        balancePay(password);
                                    }
                                }
                            }).setNegativeButton("", null);
                    dialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;

            case CALLBACK_TO_PAY:

                payresult = rootData.result;
                Intent intent = new Intent(OrderPayActivity.this, PayResultActivity.class);
                intent.putExtra("paymentmethod", 1);
                intent.putExtra("ordertype", ordertype);
                intent.putExtra("payresult", payresult);
                startActivityForResult(intent, REQUESTCODE);
                break;
        }

    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {

    }


    /**
     * 支付宝支付
     */
    public void aliPay(final String payInfo) {
        Runnable payRunnable = new Runnable() {

            @Override
            public void run() {
                // 构造PayTask 对象
                PayTask alipay = new PayTask(OrderPayActivity.this);
                // 调用支付接口，获取支付结果
                String result = alipay.pay(payInfo, true);
                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        };

        // 必须异步调用
        Thread payThread = new Thread(payRunnable);
        payThread.start();

    }


    private static final int SDK_PAY_FLAG = 1;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressWarnings("unused")
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SDK_PAY_FLAG: {
                    PayResult payResult = new PayResult((String) msg.obj);
                    /**
                     * 同步返回的结果必须放置到服务端进行验证（验证的规则请看https://doc.open.alipay.com/doc2/
                     * detail.htm?spm=0.0.0.0.xdvAU6&treeId=59&articleId=103665&
                     * docType=1) 建议商户依赖异步通知
                     */
                    String resultInfo = payResult.getResult();// 同步返回需要验证的信息

                    String resultStatus = payResult.getResultStatus();
                    // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                    if (TextUtils.equals(resultStatus, "9000")) {
                        getAliPayResult();
//                        Toast.makeText(OrderPayActivity.this, "支付成功", Toast.LENGTH_SHORT).show();
                    } else {
                        // 判断resultStatus 为非"9000"则代表可能支付失败
                        // "8000"代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                        if (TextUtils.equals(resultStatus, "8000")) {
                            Toast.makeText(OrderPayActivity.this, getString(R.string.vancloud_payment_confirmation), Toast.LENGTH_SHORT).show();
                        } else {
                            // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
//                            Toast.makeText(OrderPayActivity.this, getString(R.string.payment_failure01), Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                }
                default:
                    break;
            }
        }
    };


    //resultCode 10支付结果查看详情,11从支付结果详情里面返回
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUESTCODE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        int myPaymentMethod = data.getIntExtra("paymentmethod", -1);
                        boolean result = data.getBooleanExtra("result", false);
                        payresult = result;
                        if (myPaymentMethod == 1) {
                            if (userType == COMPANYUSER) {
                                Intent intent = new Intent(OrderPayActivity.this, PayResultActivity.class);
                                intent.putExtra("paymentmethod", myPaymentMethod);
                                intent.putExtra("ordertype", ordertype);
                                intent.putExtra("payresult", result);
                                startActivityForResult(intent, REQUESTCODE);
                            }
                        }
                        break;
                    case 10:
                        if (isFromDetails) {
                            if (payresult)
                                setResult(-1);
                            else
                                setResult(-2);
                            finish();
                        } else {
                            Intent intent = new Intent(OrderPayActivity.this, NewOrderDetailActivity.class);
                            intent.putExtra("infoid", orderid);
                            intent.putExtra("isfrompay", true);
                            startActivityForResult(intent, REQUESTCODE);
                        }
                        break;
                    case 11:
                        Intent intent = new Intent();
                        intent.putExtra("position", position);
                        intent.putExtra("paymentmethod", paymentMethod);
                        if (payresult) {
                            setResult(-1, intent);
                            finish();
                        } else {
                            if (userType == COMPANYUSER) {
                                setResult(-2);
                                finish();
                            }
                        }
                        break;
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    /**
     * wxPayErrCode
     * 0 成功	展示成功页面
     * -1 错误	可能的原因：签名错误、未注册APPID、项目设置APPID不正确、注册的APPID与设置的不匹配、其他异常等。
     * -2 用户取消	无需处理。发生场景：用户不支付了，点击取消，返回APP。
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WXPAY.equals(action)) {
                int wxPayErrCode = intent.getIntExtra("resp", 1);
                Log.d("mReceiver", "onPayFinish, errCode = " + wxPayErrCode);
//                Toast.makeText(OrderPayActivity.this, "微信支付返回结果：" + wxPayErrCode, Toast.LENGTH_SHORT).show();
                if (wxPayErrCode == 0) {
                    getWxPayResult();
                } else if (wxPayErrCode == -1) {
                    Toast.makeText(OrderPayActivity.this, getString(R.string.wxpay_failure), Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    @Override
    public void finish() {
        if (mReceiver != null)
            unregisterReceiver(mReceiver);
        mReceiver = null;
        if (mNetworkManager != null)
            mNetworkManager.cancle(this);
        super.finish();
    }

    @Override
    public void onBackPressed() {

        new AlertDialog(this).builder().setTitle(getString(R.string.frends_tip))
                .setMsg(getString(R.string.waiver_of_payment))
                .setPositiveButton(getString(R.string.yes), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.putExtra("position", position);
                        if (payresult)
                            setResult(-1, intent);
                        else
                            setResult(-2);
                        finish();
                    }
                }).setNegativeButton(getString(R.string.no), new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        }).show();
    }

    public void dialog(String msg) {

        new BalancePaymentDialog(this).setMsg(msg).setOneButton("", new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        }).show();
    }
}
