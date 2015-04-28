package pro.kost.bupt;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.apache.http.conn.util.InetAddressUtils;

import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class Utils {
	//public static String USER_AGENT = "Mozilla/5.0";
	public static String sBuptLoginAuthUrl = "http://gw.bupt.edu.cn/a11.htm";
	public static String sBuptManageUrl = "http://gwself.bupt.edu.cn/nav_login";
	public static String sTestUrl = "http://buptauth.sinaapp.com";

    public static String getFee(double fee) {
        return fee + "元";
    }
    public static String getTime(long time) {
        if (time < 60)
            return time + "分钟";
        else {
            return (String.format("%.2f 小时", (double)time/60));
        }
    }
    public static String getUsage(long bytes) {
        if (bytes < 1024)
            return bytes + "Bytes";
        else if (bytes < 1024 * 1024) {
            return String.format("%.2fMB", (double)bytes/1024);
        } else {
            return String.format("%.2fGB", (double)bytes/(1024*1024));
        }
    }
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi.isConnected();
    }
    public static boolean isLoaclIp(String ip) {
        return getLocalIpv4Address().contains(ip);
    }
    public static ArrayList<String> getLocalIpv4Address(){
        ArrayList<String> result = new ArrayList<String>();
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        result.add(inetAddress.getHostAddress().toString());
                    }
                }
            }
        } catch (SocketException ex) {
        }
        return result;
    }

    public static void log(String message) {
        //Log.e("KOST",message);
    }
    public static boolean isIpAddress(String ip) {
        try {
            if (ip == null || ip.isEmpty()) {
                return false;
            }

            String[] parts = ip.split( "\\." );
            if ( parts.length != 4 ) {
                return false;
            }

            for ( String s : parts ) {
                int i = Integer.parseInt( s );
                if ( (i < 0) || (i > 255) ) {
                    return false;
                }
            }
            if(ip.endsWith(".")) {
                return false;
            }

            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

}
