package com.vgtech.vancloud.ui.module.financemanagement;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.PopupWindow;

import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.vgtech.common.PrfUtils;
import com.vgtech.common.URLAddr;
import com.vgtech.common.api.AppPermission;
import com.vgtech.common.api.JsonDataFactory;
import com.vgtech.common.api.OrderDetail;
import com.vgtech.common.api.OrderType;
import com.vgtech.common.api.RootData;
import com.vgtech.common.network.ApiUtils;
import com.vgtech.common.network.NetworkPath;
import com.vgtech.common.network.android.HttpListener;
import com.vgtech.common.view.NoScrollListview;
import com.vgtech.common.view.VancloudLoadingLayout;
import com.vgtech.vancloud.R;
import com.vgtech.vancloud.api.OrderStatus;
import com.vgtech.vancloud.presenter.AppPermissionPresenter;
import com.vgtech.vancloud.ui.ActivityUtils;
import com.vgtech.vancloud.ui.SearchBaseActivity;
import com.vgtech.vancloud.ui.adapter.OrderManagementAdapter;
import com.vgtech.vancloud.ui.adapter.XmlDataAdapter;
import com.vgtech.vancloud.ui.view.StateIndicator;
import com.vgtech.vancloud.ui.view.TypeIndicator;
import com.vgtech.vancloud.utils.Utils;
import com.vgtech.vancloud.utils.XMLResParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Duke on 2016/3/18.
 */
public class OrderListActivity extends SearchBaseActivity implements HttpListener<String>, PullToRefreshListView.OnRefreshListener2 {

    @Override
    protected int getContentView() {
        return R.layout.order_list_layout;
    }

    private final int CALLBACK_ORDERTYPES = 1;

    private boolean isMyorder = true; //true、我的订单，false、全部订单

    private boolean showErrorLayout = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        getOrderTypes();
    }

    public void getOrderTypes() {
        typeLoadingView.setVisibility(View.GONE);
        showLoadingDialog(this, "");
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("tenant_id", PrfUtils.getTenantId(this));
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_ORDERS_ORDERTYPES), params, this);
        getAppliction().getNetworkManager().load(CALLBACK_ORDERTYPES, path, this);

    }

    private StateIndicator mStateIndicator;
    private String n = "12";
    private String nextId = "0";
    PullToRefreshListView pullToRefreshListView;
    public VancloudLoadingLayout loadingLayout;
    private final int CALLBACK_ORDERS_MANAGEMENTS = 2;
    OrderManagementAdapter orderManagementAdapter;
    private String payment_status;
    private String order_type;
    private VancloudLoadingLayout typeLoadingView;

    public void initViews() {
        setTitle(getString(R.string.my_lable_order));
        if ((AppPermissionPresenter.hasPermission(this, AppPermission.Type.order, AppPermission.Order.all.toString())) ||
                AppPermissionPresenter.hasPermission(this, AppPermission.Type.order, AppPermission.Order.account.toString())) {
            findViewById(R.id.btn_action_more).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_action_more).setOnClickListener(this);
        } else {
            findViewById(R.id.btn_action_more).setVisibility(View.GONE);
        }
        mStateIndicator = (StateIndicator) findViewById(R.id.order_state);
        pullToRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_list);
        pullToRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
        loadingLayout = (VancloudLoadingLayout) findViewById(R.id.loading);
        orderManagementAdapter = new OrderManagementAdapter(new ArrayList<OrderDetail>(), this);
        pullToRefreshListView.setAdapter(orderManagementAdapter);
        pullToRefreshListView.setOnRefreshListener(this);
        typeLoadingView = (VancloudLoadingLayout) findViewById(R.id.type_loading);

        loadingLayout.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
            @Override
            public void loadAgain() {
                loadData();
            }
        });

        typeLoadingView.setDoLoadAgain(new VancloudLoadingLayout.DoLoadAgain() {
            @Override
            public void loadAgain() {
                getOrderTypes();
            }
        });

    }

    private TypeIndicator.TypeSelectListener mTypeListener = new TypeIndicator.TypeSelectListener() {
        @Override
        public void onSelect(OrderType orderType) {
            try {
                List<OrderStatus> orderStatusList = JsonDataFactory.getDataArray(OrderStatus.class, orderType.getJson().getJSONArray("title"));
                orderStatusList.add(0, new OrderStatus("", getString(R.string.all)));
                mStateIndicator.init(orderType, orderStatusList, mStateListener);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    private StateIndicator.StateSelectListener mStateListener = new StateIndicator.StateSelectListener() {
        @Override
        public void onSelect(OrderType orderType, OrderStatus os, int index) {
            payment_status = os.key;
            order_type = orderType.order_type;
            nextId = "0";
            loadData();
        }
    };

    private void loadData() {
        loadingLayout.showLoadingView(pullToRefreshListView, "", true);
        if (isMyorder) {
            getMyOrders("0");
        } else {
            getOrders("0");
        }
    }

    private void getOrders(String nextid) {

        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("status", payment_status);
        params.put("order_type", order_type);
        params.put("n", n);
        params.put("s", nextid);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_ORDERS_MANAGEMENTS), params, this);
        getAppliction().getNetworkManager().load(CALLBACK_ORDERS_MANAGEMENTS, path, this, false);
    }

    //我的订单
    private void getMyOrders(String nextid) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", PrfUtils.getUserId(this));
        params.put("tenant_id", PrfUtils.getTenantId(this));
        params.put("status", payment_status);
        params.put("order_type", order_type);
        params.put("n", n);
        params.put("s", nextid);
        NetworkPath path = new NetworkPath(ApiUtils.generatorUrl(this, URLAddr.URL_ORDERS_MY), params, this);
        getAppliction().getNetworkManager().load(CALLBACK_ORDERS_MANAGEMENTS, path, this, false);
    }

    @Override
    public void dataLoaded(int callbackId, NetworkPath path, RootData rootData) {

        if (callbackId == CALLBACK_ORDERTYPES) {
            dismisLoadingDialog();
            boolean safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
            if (!safe) {
                typeLoadingView.showErrorView(pullToRefreshListView);
                return;
            }
        } else {
            loadingLayout.dismiss(pullToRefreshListView);
            pullToRefreshListView.onRefreshComplete();
            boolean safe = ActivityUtils.prehandleNetworkData(this, this, callbackId, path, rootData, true);
            if (!safe) {
                if (showErrorLayout)
                    loadingLayout.showErrorView(pullToRefreshListView);
                else {
                    showErrorLayout = true;
                    if (orderManagementAdapter.getList().size() <= 0) {
                        loadingLayout.showErrorView(pullToRefreshListView);
                    }
                }

                return;
            }
        }

        switch (callbackId) {

            case CALLBACK_ORDERTYPES:
                try {
                    JSONObject jsonObject = rootData.getJson();
                    List<OrderType> orderTypeList = JsonDataFactory.getDataArray(OrderType.class, jsonObject.getJSONArray("data"));
                    TypeIndicator typeIndicator = (TypeIndicator) findViewById(R.id.order_type);
                    typeIndicator.init(orderTypeList, mTypeListener);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case CALLBACK_ORDERS_MANAGEMENTS:
                if (!showErrorLayout)
                    showErrorLayout = true;

                List<OrderDetail> orderDetails = new ArrayList<OrderDetail>();
                String oldeNextId = path.getPostValues().get("s");
                try {
                    JSONObject jsonObject = rootData.getJson().getJSONObject("data");
                    String id = jsonObject.getString("nextid");
                    if (!TextUtils.isEmpty(id) && !"0".equals(id)) {
                        nextId = id;
                    }
                    orderDetails = JsonDataFactory.getDataArray(OrderDetail.class, jsonObject.getJSONArray("manager_list"));
                } catch (Exception e) {
                    e.printStackTrace();
                }

//                if (orderManagementAdapter == null) {
//                    orderManagementAdapter = new OrderManagementAdapter(orderDetails,this, payment_status);
//                    pullToRefreshListView.setAdapter(orderManagementAdapter);
//                } else {
                if ("0".equals(oldeNextId)) {
                    orderManagementAdapter.myNotifyDataSetChanged(orderDetails);
                } else {
                    List<OrderDetail> list = orderManagementAdapter.getList();
                    list.addAll(orderDetails);
                    orderManagementAdapter.myNotifyDataSetChanged(list);
                }
//                }
                if (orderManagementAdapter.getList().size() <= 0) {
                    loadingLayout.showEmptyView(pullToRefreshListView, getString(R.string.no_order), true, true);
                    pullToRefreshListView.setVisibility(View.VISIBLE);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
    }

    @Override
    public void onResponse(String response) {

    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase refreshView) {
        showErrorLayout = false;
        if (isMyorder) {
            getMyOrders("0");
        } else {
            getOrders("0");
        }
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase refreshView) {
        showErrorLayout = false;
        if (isMyorder) {
            getMyOrders(nextId);
        } else {
            getOrders(nextId);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case 200:
                if (resultCode == Activity.RESULT_OK) {
                    int position = data.getIntExtra("position", -1);
                    if (position >= 0 && "pending".equals(payment_status)) {
                        orderManagementAdapter.removeOrder(position);
                    } else {
                        nextId = "0";
                        loadData();
                    }
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_action_more) {
            showPop(v);
        } else if (v.getId() == R.id.btn_back) {
            if (!isMyorder) {
                isMyorder = true;
                setTitle(getString(R.string.my_lable_order));
                findViewById(R.id.btn_action_more).setVisibility(View.VISIBLE);
                getMyOrders("0");
                return;
            } else {
                finish();
            }
        } else {
            super.onClick(v);
        }
    }

    private PopupWindow popupWindow;

    public void showPop(final View v) {
        if (popupWindow == null) {
            XMLResParser parser = new XMLResParser(this);
            XmlDataAdapter menuAdapter = new XmlDataAdapter<>(this);
            XMLResParser.RootData rootData = parser
                    .parser(R.xml.order_list_menu);
            XMLResParser.MenuItem[] items = rootData
                    .getChildren(XMLResParser.MenuItem.class, true);
            menuAdapter.add(items);
            if (!AppPermissionPresenter.hasPermission(this, AppPermission.Type.order, AppPermission.Order.account.toString())) {
                menuAdapter.remove(1);
            }
            if (!AppPermissionPresenter.hasPermission(this, AppPermission.Type.order, AppPermission.Order.all.toString())) {
                menuAdapter.remove(0);
            }
            View popView = getLayoutInflater().inflate(R.layout.action_pop_layout, null);
            NoScrollListview listView = (NoScrollListview) popView.findViewById(R.id.listview);
            listView.setAdapter(menuAdapter);
            listView.setItemClick(true);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    popupWindow.dismiss();
                    XMLResParser.MenuItem menuItem = (XMLResParser.MenuItem) parent.getItemAtPosition(position);
                    switch (menuItem.getId()) {
                        case "action_menu_my_lable"://全部订单
                            isMyorder = false;
                            setTitle(getString(R.string.all_orders));
                            findViewById(R.id.btn_action_more).setVisibility(View.GONE);
                            getOrders("0");
                            break;
                        case "action_menu_admin"://账户明细
                            Intent toTradeList = new Intent(OrderListActivity.this, TradeListActivity.class);
                            startActivity(toTradeList);
                            break;
                    }
                }
            });
            popupWindow = new PopupWindow(popView, Utils.convertDipOrPx(this, 210),
                    ViewGroup.LayoutParams.WRAP_CONTENT);// 创建一个PopuWidow对象
            popupWindow.setFocusable(true);// 使其聚集
            popupWindow.setOutsideTouchable(true);// 设置允许在外点击消失
            popupWindow.setBackgroundDrawable(getResources().getDrawable(
                    R.drawable.abc_popup_background_mtrl_mult));// 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
            popupWindow.update();
        }
        popupWindow.showAsDropDown(v, 0 - Utils.convertDipOrPx(this, 10), 0 - Utils.convertDipOrPx(this, 8));

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!isMyorder) {
                isMyorder = true;
                setTitle(getString(R.string.my_lable_order));
                findViewById(R.id.btn_action_more).setVisibility(View.VISIBLE);
                getMyOrders("0");
            } else {
                onBackPressed();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

