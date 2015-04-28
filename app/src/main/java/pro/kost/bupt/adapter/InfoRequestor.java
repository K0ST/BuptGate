package pro.kost.bupt.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.baidu.mobstat.StatService;
import com.umeng.update.UmengUpdateAgent;

import java.util.ArrayList;

import cn.edu.bupt.AndroidWifiHelper;
import cn.edu.bupt.IPLocationMap;
import pro.kost.bupt.AboutActivity;
import pro.kost.bupt.BuptAuthService;
import pro.kost.bupt.ChartActivity;
import pro.kost.bupt.LoginActivity;
import pro.kost.bupt.R;
import pro.kost.bupt.datas.SecurePreferences;
import pro.kost.bupt.network.SimpleLogin;
import pro.kost.bupt.Utils;
import cn.edu.bupt.WifiHelperInterface;

/**
 * Created by kost on 14/11/24.
 */
public class InfoRequestor implements View.OnClickListener, SimpleLogin.OnAuthFinished,
        SimpleLogin.OnLogout,InfoAdapter.OnRecyclerViewItemClickListener,WifiHelperInterface {
    private final static int MSG_LOGIN_SUCCESS = 0;
    private final static int MSG_LOGOUT_SUCCESS = 1;
    private final static int MSG_LOGOUT_FAILED = 2;
    private final static int INIT_DEVICE_HELPER = 3;
    private final static int LOGIN_DEVICE_HELPER= 4;
    private final static int UPDATE_DATA= 5;
    private Context context = null;
    private View mRootView;
    private RecyclerView mRecycler;
    private LinearLayoutManager mLayoutManager;
    private InfoAdapter mAdapter;
    private Button mLogoutButton;
    private Button mAboutButton;
    private SimpleLogin mLogin = new SimpleLogin();
    private String mUserName = null;
    private String mPassword = null;
    private AndroidWifiHelper mDeviceHelper = null;
    private double mFee = 0;
    private long mTime = 0;
    private long mUsage = 0;
    private String mLastOfflineIp = null;
    private boolean disableDetailClick = false;

    public InfoRequestor(Context context) {
        this.context = context;
        mLogin.setOnAuthFinished(InfoRequestor.this);
        mLogin.setOnLogout(InfoRequestor.this);
        initViewsInThread();
        mHandler.sendEmptyMessage(INIT_DEVICE_HELPER);

    }
    public void check() {
        mLogin.checkLogin();
    }
    private void checkDevices(final String username, final String password) {
        mDeviceHelper.loginGW(username,password);
        Utils.log("device helper logging in");
    }
    public void request(final String username, final String password) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                initViews();
                mUserName = username;
                mPassword = password;
                StatService.onEvent(context.getApplicationContext(), "click_login", "default");
                mLogin.login(username, password);
            }
        }).start();
    }
    private void initViewsInThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                initViews();
            }
        }).start();
    }
    private void initViews() {
        if (mRootView == null) {
            LayoutInflater inflater = (LayoutInflater) context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mRootView = inflater.inflate(R.layout.info_layout, null);
            mAboutButton = (Button) mRootView.findViewById(R.id.about);
            mLogoutButton = (Button) mRootView.findViewById(R.id.logout);
            mRecycler = (RecyclerView) mRootView.findViewById(R.id.info_view);
            mLayoutManager = new LinearLayoutManager(context);
            mRecycler.setItemAnimator(new DefaultItemAnimator());
            mRecycler.setLayoutManager(mLayoutManager);
            mRecycler.setHasFixedSize(true);
            mLogoutButton.setOnClickListener(InfoRequestor.this);
            mAboutButton.setOnClickListener(this);
        }
    }
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOGIN_SUCCESS:
                    if (mListener != null)
                        mListener.onUpdate(mRootView);
                    break;
                case MSG_LOGOUT_SUCCESS:
                    ((LoginActivity) context).logout();
                    break;
                case MSG_LOGOUT_FAILED:
                    mLogoutButton.setText("Log Out Failed!");
                    break;
                case INIT_DEVICE_HELPER:
                    mDeviceHelper = new AndroidWifiHelper((Activity)context);
                    mDeviceHelper.setProcessor(InfoRequestor.this);
                    break;
                case LOGIN_DEVICE_HELPER:
                    checkDevices(mUserName,mPassword);
                    break;
                case UPDATE_DATA:
                    mAdapter.notifyDataSetChanged();
                    break;
            }

            return false;
        }
    });

    public ArrayList<String> getNewTitles(boolean withLoading) {
        ArrayList<String> titles = new ArrayList<String>();
        titles.add("在线时长");
        titles.add("账户余额");
        titles.add("本月流量");
        if (withLoading)
            titles.add("终端");
        return titles;
    }

    public ArrayList<String> getNewDetails(long time, long bytes, double fee,boolean withLoading) {
        ArrayList<String> details = new ArrayList<String>();
        details.add(Utils.getTime(time));
        details.add(Utils.getFee(fee));
        details.add(Utils.getUsage(bytes));
        if (withLoading)
            details.add("获取在线终端");
        return details;
    }

    public void setOnViewUpdateListener(OnViewUpdateListener listener) {
        this.mListener = listener;
    }

    private OnViewUpdateListener mListener;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.logout:
                StatService.onEvent(context.getApplicationContext(), "click_quit", "default");
                ((Activity)context).finish();
                System.exit(0);
                break;
            case R.id.about:
                StatService.onEvent(context.getApplicationContext(), "click_about", "default");
                context.startActivity(new Intent(context,AboutActivity.class));
                break;
        }
    }
    private void initListsDatas(long delay) {
        initViews();
        mAdapter = new InfoAdapter(getNewTitles(true), getNewDetails(mTime, mUsage, mFee,true));
        mAdapter.setOnItemClickListener(this);
        mRecycler.setAdapter(mAdapter);
        mHandler.sendEmptyMessage(MSG_LOGIN_SUCCESS);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(LOGIN_DEVICE_HELPER);
            }
        },delay);

    }
    @Override
    public void onFinished(boolean success, long time, long bytes, double fee) {
        if (success) {
            UmengUpdateAgent.update(context);
            StatService.onEvent(context.getApplicationContext(), "login_success", "default");
            mTime = time;
            mFee = fee;
            mUsage = bytes;
            initListsDatas(1000);
            SecurePreferences preferences = new SecurePreferences(context);
            if(mUserName != null && !mUserName.equals("")) {
                preferences.put("username", mUserName);
                preferences.put("password", mPassword);
                Intent intent = new Intent(context,BuptAuthService.class);
                intent.setAction("psw");
                intent.putExtra("username", mUserName);
                intent.putExtra("password", mPassword);
                intent.putExtra("time", time);
                intent.putExtra("fee", fee);
                intent.putExtra("bytes", bytes);
                context.startService(intent);
            } else {
                mUserName = preferences.getString("username");
                mPassword = preferences.getString("password");
            }
        } else {
            StatService.onEvent(context.getApplicationContext(), "login_failed", "default");
            ((LoginActivity)context).setLoginButtonText("Login Failed!");
        }
    }

    @Override
    public void onLogout(boolean success) {
        if (success) {
            mHandler.sendEmptyMessage(MSG_LOGOUT_SUCCESS);
            Intent logoutIntent = new Intent(context,BuptAuthService.class);
            logoutIntent.setAction("logout");
            context.startService(logoutIntent);
        } else {
            mHandler.sendEmptyMessage(MSG_LOGOUT_FAILED);
        }
    }
    private void showForceOfflineDialog(final String ip) {
        new MaterialDialog.Builder(context)
                .title("强制离线")
                .content("是否将IP为 " + ip + " 的设备离线?")
                .positiveText("是滴")
                .negativeText("不")
                .positiveColor(Color.parseColor("#ed8871"))
                .negativeColor(Color.BLACK)
                .callback(new MaterialDialog.SimpleCallback() {
                    @Override
                    public void onPositive(MaterialDialog materialDialog) {
                        mDeviceHelper.forceIpOffLine(ip);
                        mLastOfflineIp = ip;

                    }
                })
                .show();
    }
    @Override
    public void onClick(View view, String detail) {
        if (Utils.isIpAddress(detail.trim())) {
            StatService.onEvent(context.getApplicationContext(), "logout", "default");
            if (Utils.isLoaclIp(detail.trim()))
                mLogin.logout();
            else
                showForceOfflineDialog(detail);
        }else if (disableDetailClick) {
            return;
        }else if (((TextView)view.findViewById(R.id.detail)).getText().toString().contains("点击重试")) {
            refreshLoadingData("正在重试");
            mDeviceHelper.loginGW(mUserName,mPassword);
        }else if (((TextView)view.findViewById(R.id.title)).getText().toString().contains("在线时长")) {
            startLogActivity("time");
        } else if (((TextView)view.findViewById(R.id.title)).getText().toString().contains("账户余额")) {
            startLogActivity("money");
        } else if (((TextView)view.findViewById(R.id.title)).getText().toString().contains("本月流量")) {
            startLogActivity("byte");
        }

    }
    private void startLogActivity(String type) {
        if (AndroidWifiHelper.sDataSet == null) {
            Toast.makeText(context,"正在获取数据",Toast.LENGTH_SHORT).show();
            return;
        }
        if (!AndroidWifiHelper.isDataValid()) {
            Toast.makeText(context,"暂无足够数据展示",Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(context, ChartActivity.class);
        intent.putExtra("type",type);
        context.startActivity(intent);
    }
    @Override
    public void processWifiHelperStatusChanged(Status status) {
        switch (status) {
            case LOADING_INDEX_PAGE:
                refreshLoadingData("正在访问网关认证");
                break;
            case TRY_TO_LOGIN:
                refreshLoadingData("正在登录");
                break;
            case LOGIN_FAILED:
                refreshLoadingData("登录失败");
                break;
            case TRY_TO_FETCH_IP:
                refreshLoadingData("正在获取在线设备");
                break;
        }
    }
    private void refreshLoadingData(String message) {
        if(mAdapter.getDetails().size() >= 4) {
            mAdapter.getDetails().set(3, message);
            mAdapter.notifyDataSetChanged();
        }
    }
    private void removeIp(String ip) {
        int position = mAdapter.getDetails().indexOf(ip.trim());
        mAdapter.getDetails().remove(position);
        mAdapter.getTitles().remove(position);
        mAdapter.notifyDataSetChanged();
    }
    @Override
    public void processIpInUse(final ArrayList<String> ipList) {
        Utils.log("iplist = " + ipList);

        mAdapter.setDatas(getNewTitles(false),getNewDetails(mTime,mUsage,mFee,false));
        for (String loggedIp : ipList) {
            mAdapter.getTitles().add("终端" + (Utils.isLoaclIp(loggedIp) ? "(本机)" : ""));
            mAdapter.getDetails().add(loggedIp);
        }
        mHandler.sendEmptyMessage(UPDATE_DATA);
        mDeviceHelper.getMonthLogDatas();
        mDeviceHelper.getMoney(new AndroidWifiHelper.WifiHelperMoneyInterface() {
            @Override
            public void money(float money) {
                if (ipList != null && ipList.size() != 0)
                    showDifferDialog(money);
            }
        });
    }
    public void showDifferDialog(final float money) {
        ((Activity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (money <= 0.1f)
                    return;
                if (Math.abs(mFee - money) < 3f) {
                    return;
                }
                disableDetailClick = true;
                new MaterialDialog.Builder(context)
                        .title("注意")
                        .content("您连接的网络已别人被登录过。在线时长、账户余额、本月流量等可能并非您的账户数据。")
                        .positiveColor(Color.BLACK)
                        .theme(Theme.LIGHT)  // the default is light, so you don't need this line
                        .positiveText("OK")
                        .show();
            }
        });
    }
    @Override
    public void processForceOfflineResponse(boolean succeed) {

        new MaterialDialog.Builder(context)
                .title("强制离线")
                .content("离线 " + mLastOfflineIp + (succeed ? "成功" : "失败"))
                .positiveColor(Color.BLACK)
                .theme(Theme.LIGHT)  // the default is light, so you don't need this line
                .positiveText("OK")
                .show();
        if (succeed) {
            removeIp(mLastOfflineIp);
            mLastOfflineIp = null;
        }
    }

    @Override
    public void processUnknownError(final String message) {
        ((Activity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context,"获取在线设备失败，点击重试。",Toast.LENGTH_SHORT).show();
                if (!TextUtils.isEmpty(mUserName))
                    refreshLoadingData("点击重试");
                else
                    refreshLoadingData("未填写账户密码");

            }
        });

    }

    public interface OnViewUpdateListener {
        void onUpdate(View view);
    }
}
