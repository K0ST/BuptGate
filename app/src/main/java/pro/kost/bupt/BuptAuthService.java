package pro.kost.bupt;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.baidu.mobstat.StatService;

import pro.kost.bupt.datas.SecurePreferences;
import pro.kost.bupt.network.SimpleLogin;
import pro.kost.bupt.network.SimpleLogin.OnAuthFinished;
import pro.kost.bupt.network.SimpleLogin.OnLogout;
import pro.kost.bupt.network.SimpleLogin.OnWifiChecked;

public class BuptAuthService extends Service implements OnAuthFinished, OnLogout, OnWifiChecked {
    private final static long INTERVAL = 3 * 60 * 1000;
    private final static int NO_WIFI = -1;
    private final static int OPTIONS = 0;
    private final static int CONNECTING = 1;
    private final static int CONNECTED = 2;
    private SimpleLogin mLogin = new SimpleLogin();
    private String mUserName = null;
    private String mPassword = null;
    private NotificationManager notificationManager;
    private boolean mCheckingWifi = false;
    private int mNotifyState = NO_WIFI;

    @Override
    public void onCreate() {
        mLogin.setOnAuthFinished(this);
        mLogin.setOnLogout(this);
        mLogin.setOnChecked(this);
        initReceiver();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initAccount();
        if (intent != null && intent.getAction() != null && intent.getAction().equals("connect")) {
            showBusyNotification("正在连接...");
            connect();
        } else if (intent != null && intent.getAction() != null && intent.getAction().equals("psw")) {
            SecurePreferences preferences = new SecurePreferences(getApplicationContext());
            preferences.put("username", intent.getStringExtra("username"));
            preferences.put("password", intent.getStringExtra("password"));

            mUserName = intent.getStringExtra("username");
            mPassword = intent.getStringExtra("password");
            onFinished(true, intent.getLongExtra("time", 0),
                    intent.getLongExtra("bytes", 0),
                    intent.getDoubleExtra("fee", 0f));
        } else if (intent != null && intent.getAction() != null && intent.getAction().equals("logout")) {
            if (notificationManager != null)
                notificationManager.cancel(0);
        } else {
            checkWifiValid();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void connect() {
        if (mUserName == null) {
            Intent myIntent = new Intent(this, LoginActivity.class);
            myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(myIntent);
            mNotifyState = NO_WIFI;
            notificationManager.cancel(0);
            return;
        }
        new Thread(new Runnable() {
            public void run() {
                StatService.onEvent(getApplicationContext(), "notify_click_login", "default");
                mLogin.checkLogin();
                mLogin.login(mUserName, mPassword);
            }
        }).start();
    }

    private void initAccount() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SecurePreferences preferences = new SecurePreferences(getApplicationContext());
                final String username = preferences.getString("username");
                final String password = preferences.getString("password");
                if (username == null || username.equals("") || password == null || password.equals("")) {
                } else {
                    mUserName = username;
                    mPassword = password;
                }
            }
        }).start();
    }

    private void initReceiver() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkStateReceiver, filter);
    }

    public void showConnectOptions() {
        if (mNotifyState == OPTIONS)
            return;
        mNotifyState = OPTIONS;
        notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent myIntent = new Intent(this, BuptAuthService.class);
        myIntent.setAction("connect");
        PendingIntent pIntent = PendingIntent.getService(this, 0, myIntent, 0);
        Notification n;
        if (android.os.Build.VERSION.SDK_INT < 16) {
            n = new NotificationCompat.Builder(this)
                    .setVibrate(getViberation())
                    .setContentTitle("检测到校园网")
                    .setContentText("使用自邮门登陆校园网关？")
                    .setSmallIcon(R.drawable.logo_sl)
                    .setContentIntent(pIntent)
                    .addAction(R.drawable.ic_action_accept, "登录", pIntent)
                    .addAction(R.drawable.ic_action_cancel, "取消", null).build();
        } else {
            n = new Notification.Builder(this)
                    .setVibrate(getViberation())
                    .setContentTitle("检测到校园网")
                    .setContentText("使用自邮门登陆校园网关？")
                    .setSmallIcon(R.drawable.logo_sl)
                    .setContentIntent(pIntent)
                    .addAction(R.drawable.ic_action_accept, "登录", pIntent)
                    .addAction(R.drawable.ic_action_cancel, "取消", null).build();
        }
        notificationManager.notify(0, n);
        StatService.onEvent(getApplicationContext(), "notify_alert", "default");
    }

    public void showBusyNotification(String text) {
        mNotifyState = CONNECTING;
        notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification n = new NotificationCompat.Builder(this)
                .setVibrate(getViberation())
                .setContentTitle("校园网关登录")
                .setContentText(text)
                .setSmallIcon(R.drawable.logo_sl).build();

        notificationManager.notify(0, n);
    }

    public void showUsageNotification(String fee, long time, long bytes) {
        mNotifyState = CONNECTED;
        Intent myIntent = new Intent(this, LoginActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, myIntent, 0);
        notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification n = new NotificationCompat.Builder(this)
                .setOngoing(true)
                .setContentTitle("校园网(Online)")
                .setContentIntent(pIntent)
                .setContentText("余额:" + fee + "元     " + "流量:" + getUsage(bytes))
                .setSmallIcon(R.drawable.logo_sl)
                .setAutoCancel(true).build();

        notificationManager.notify(0, n);
    }

    public String getTime(long time) {
        if (time < 60)
            return time + "分钟";
        else {
            return (String.format("%.2f 小时", (double) time / 60));
        }
    }

    public String getUsage(long bytes) {
        if (bytes < 1024)
            return bytes + "Bytes";
        else if (bytes < 1024 * 1024) {
            return String.format("%.2fMB", (double) bytes / 1024);
        } else {
            return String.format("%.2fGB", (double) bytes / (1024 * 1024));
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }


    @Override
    public void onLogout(boolean success) {

    }

    @Override
    public void onFinished(boolean success, long time, long bytes, double fee) {
        if (success) {
            StatService.onEvent(getApplicationContext(), "login_success", "default");
            showBusyNotification("登陆成功！" + "(" + getUsage(bytes) + ", " + fee +  "元)");
        } else {
            showBusyNotification("登陆失败！");
            StatService.onEvent(getApplicationContext(), "login_failed", "default");
        }
//        if (success) {
//            showUsageNotification(fee+"",time,bytes);
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    mLogin.checkLogin();
//                }
//            }, INTERVAL);
//        } else {
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    mLogin.checkLogin();
//                }
//            }, INTERVAL);
//            if(notificationManager!=null)
//                notificationManager.cancel(0);
//        }
    }

    BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkWifiValid();
        }
    };

    private void checkWifiValid() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if ((mWifi.isConnected()) && !mCheckingWifi) {
            mCheckingWifi = true;
            mLogin.checkWifi();
        } else if (mWifi.isConnectedOrConnecting()) {
        } else {
            if (notificationManager != null) {
                mNotifyState = NO_WIFI;
                notificationManager.cancel(0);
            }
        }
    }

    private Handler mHandler = new Handler(new Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            return false;
        }
    });

    @Override
    public void onChecked(boolean isValid) {
        mCheckingWifi = false;
        if (isValid) {
            showConnectOptions();
        } else {
        }
    }

    private static long lastVib = 0;

    private long[] getViberation() {
        if (System.currentTimeMillis() - lastVib > 1000) {
            lastVib = System.currentTimeMillis();
            return new long[]{0, 50};
        } else {
            return new long[]{};
        }
    }
}
