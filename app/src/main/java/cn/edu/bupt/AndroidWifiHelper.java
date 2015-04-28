package cn.edu.bupt;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import pro.kost.bupt.Utils;


/**
 * Created by kost on 14/12/24.
 */
public class AndroidWifiHelper {
    private Context mContext;
    private GWSelf mGwself = null;
    private boolean mIsLoggedIn = false;
    public static Dataset sDataSet = null;
    public AndroidWifiHelper(Context context) {
        this.mContext = context;
        mGwself = new GWSelf();
    }
    public static boolean isDataValid() {
        if (sDataSet == null)
            return false;
        if (sDataSet.timeDatas == null || sDataSet.byteDatas == null || sDataSet.moneyDatas == null)
            return  false;
        if (sDataSet.timeDatas.values == null || sDataSet.byteDatas.values == null || sDataSet.moneyDatas.values == null)
            return  false;
        if (sDataSet.timeDatas.values.length <= 1 || sDataSet.byteDatas.values .length <= 1 || sDataSet.moneyDatas.values .length <= 1)
            return  false;
        return true;
    }
    public void loginGW(final String username, final String psw) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (TextUtils.isEmpty(username))
                        return;
                    Utils.log("username:" + username);
                    mGwself.login(username,psw);
                    mIsLoggedIn = true;
                    ArrayList<String> ips = new ArrayList<String>(mGwself.getOnlineIps());
                    if (mInterface != null)
                        mInterface.processIpInUse(ips);
                }catch (Exception e) {
                    Utils.log("loginGW failed = " + e.toString());
                    if (mInterface != null)
                        mInterface.processUnknownError(e.toString());
                }
            }
        }).start();
    }
    private Handler mHandler = new Handler(Looper.myLooper());
    public void forceIpOffLine(final String ip) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mIsLoggedIn) {
                    try {
                        GWJsonMessage message = mGwself.forceOffline(ip.trim());
                        if (message.date.equals("success") && mInterface != null) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mInterface.processForceOfflineResponse(true);
                                }
                            });

                        }
                    } catch (IOException e) {
                        if (mInterface != null)
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mInterface.processForceOfflineResponse(false);
                                }
                            });
                    }
                }
            }
        }).start();

    }
    public void getMoney(final WifiHelperMoneyInterface wifiHelperInterface) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    GWJsonMessage message =  mGwself.getInformation();
                    wifiHelperInterface.money(Float.parseFloat(message.note.leftmoeny.substring(0,message.note.leftmoeny.length()-1)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    public interface WifiHelperMoneyInterface{
        public void money(float money);
    }
    public void getMonthLogDatas() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!mIsLoggedIn)
                    return;
                try {
                    //LoginLog logList = mGwself.getLoginLog("2014-12-01","2014-12-02");
                    LoginLog logList = mGwself.getLoginLog(LoginLog.LogType.MONTH);
                    sDataSet = processLogs(logList);
                }catch (IOException e) {

                }
            }
        }).start();
    }
    private Dataset processLogs(LoginLog logList) {
        Collections.reverse(logList.logs);
        int max = 0;
        for (LoginLog.LogItem item : logList.logs) {
            int index = Integer.parseInt(item.loginTime.substring(8,10));
            max = index > max ? index : max;
        }
        Dataset dataset = new Dataset(max);
        for (int index = 0; index < max; index ++) {
            dataset.setIndex(index);
            Datas datas = getDatas(index + 1, logList);
            if (index == 0) {
                dataset.timeDatas.values[0] = datas.time;
                dataset.moneyDatas.values[0] = datas.money;
                dataset.byteDatas.values[0] = datas.bytes;
                dataset.upDatas.values[0] = datas.up;
                dataset.downDatas.values[0] = datas.down;
            } else {
                dataset.timeDatas.values[index] = datas.time + dataset.timeDatas.values[index - 1];
                dataset.moneyDatas.values[index] = datas.money + dataset.moneyDatas.values[index - 1];
                dataset.byteDatas.values[index] = datas.bytes + dataset.byteDatas.values[index - 1];
                dataset.upDatas.values[index] = datas.up + dataset.upDatas.values[index - 1];
                dataset.downDatas.values[index] = datas.down + dataset.downDatas.values[index - 1];
            }
        }

        for (int i = 0; i < dataset.timeDatas.values.length; i++) {
            dataset.timeDatas.values[i] = dataset.timeDatas.values[i] / 60f;
        }
        for (int i = 0; i < dataset.byteDatas.values.length; i++) {
            dataset.byteDatas.values[i] = dataset.byteDatas.values[i] / (1024f);
        }
        for (int i = 0; i < dataset.upDatas.values.length; i++) {
            dataset.upDatas.values[i] = dataset.upDatas.values[i] / (1024f);
        }
        for (int i = 0; i < dataset.downDatas.values.length; i++) {
            dataset.downDatas.values[i] = dataset.downDatas.values[i] / (1024f);
        }
        return  dataset;
    }
    public Datas getDatas(int day,LoginLog logList) {
        Datas datas = new Datas();
        for (LoginLog.LogItem item : logList.logs) {
            if (Integer.parseInt(item.loginTime.substring(8,10)) > day)
                return  datas;
            if (Integer.parseInt(item.loginTime.substring(8,10)) == day) {
                datas.bytes += item.total;
                datas.up += item.upload;
                datas.down += item.download;
                datas.money += item.fees;
                datas.time += item.minutes;
            }
        }
        return  datas;
    }
    public void setProcessor(WifiHelperInterface myInterface) {
        this.mInterface = myInterface;
    }
    private WifiHelperInterface mInterface;
    public class Datas{
        public float money = 0f;
        public float bytes = 0f;
        public float up = 0f;
        public float down = 0f;
        public float time = 0f;
    }
    public class Dataset{
        public ChartDatas timeDatas;
        public ChartDatas moneyDatas;
        public ChartDatas byteDatas;
        public ChartDatas upDatas;
        public ChartDatas downDatas;
        public Dataset(int size)  {
             timeDatas = new ChartDatas(size);
             moneyDatas = new ChartDatas(size);
             byteDatas = new ChartDatas(size);
             upDatas = new ChartDatas(size);
             downDatas = new ChartDatas(size);
        }
        public void setIndex(int value) {
            timeDatas.titles[value] = Integer.toString(value + 1);
            moneyDatas.titles[value] = Integer.toString(value + 1);
            byteDatas.titles[value] = Integer.toString(value + 1);
            upDatas.titles[value] = Integer.toString(value + 1);
            downDatas.titles[value] = Integer.toString(value + 1);
        }
    }
    public class ChartDatas{
        public ChartDatas(int length) {
            values = new float[length];
            titles = new String[length];
        }
        public float[] values;
        public String[] titles;
    }
}
