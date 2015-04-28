package pro.kost.bupt;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.List;

import pro.kost.bupt.R;

public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        getString(R.string.itemhaha);
        getActionBar().hide();
    }
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.email:
                feedback();
                break;
            case R.id.share:
                share();
                break;
            case R.id.clicktofollow:
            case R.id.logo:
                try {
                    openWeibo(this, "http://weibo.com/kosts", "KOST-昱东");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }
    public static final String SINA_WEIBO_URI = "sinaweibo://userinfo?nick=%s&sid=";
    public static void openWeibo(Context context, String webUrl, String nickName) {
        boolean appOpen = true;
        if (TextUtils.isEmpty(nickName)) {
            appOpen = false;
        }
        String nick = String.format(SINA_WEIBO_URI, nickName);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(nick));
        intent.setPackage("com.sina.weibo");
        List<ResolveInfo> infos =
                context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (null != infos && !infos.isEmpty()) {
            try {
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                appOpen = false;
            }
        } else {
            appOpen = false;
        }
        if (!appOpen) {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(webUrl));
            context.startActivity(intent);
        }
    }
    private void feedback() {

    }
    private void share() {
        String shareBody = "北邮网关登录软件: http://buptauth.sinaapp.com/download.php";
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.app_name)));
    }
    public static String getVersion(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            String version = info.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
//    @Override
//    protected void onPause() {
//        StatService.onPause(this);
//        super.onPause();
//    }
//    @Override
//    protected void onResume() {
//        StatService.onResume(this);
//        super.onResume();
//    }
}
