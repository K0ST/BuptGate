package pro.kost.bupt;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.mobstat.SendStrategyEnum;
import com.baidu.mobstat.StatService;
import com.squareup.picasso.Picasso;
import com.umeng.update.UmengUpdateAgent;

import java.util.Random;

import pro.kost.bupt.adapter.InfoRequestor;
import pro.kost.bupt.datas.SecurePreferences;
import pro.kost.bupt.views.AnimateBackground;

public class LoginActivity extends Activity implements View.OnClickListener,
        AnimateBackground.OnStateChangeListener,InfoRequestor.OnViewUpdateListener{
    private static int[] backs = {
            R.drawable.bg,
            R.drawable.hehe,
            R.drawable.hehe2,
            R.drawable.hehe5,
            R.drawable.hehe6
    };
    private AnimateBackground mAnimBack;
    private ImageView mImageBack;
    private TextView mTitle;
    private TextView mSummary;
    private EditText mUserNameEditText;
    private EditText mPasswordEditText;
    private Button mLoginButton;
    private Button mManageButton;
    private View mLoginViews;
    private View mLoginTexts;
    private RelativeLayout mRoot;
    private View mInfoView;

    private InfoRequestor mRequestor;
    private String mUsername = null;
    private String mPassword = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        initViews();
        initAccouts();

        startService(new Intent(this,BuptAuthService.class));
        initLog();
        UmengUpdateAgent.update(this);
    }
    private void initViews() {
        if(getActionBar() != null)
            getActionBar().hide();
        mRoot = (RelativeLayout) findViewById(R.id.root);
        mImageBack = (ImageView) findViewById(R.id.bg);
        mTitle = (TextView) findViewById(R.id.title);
        mSummary = (TextView) findViewById(R.id.summary);
        mUserNameEditText = (EditText) findViewById(R.id.username);
        mPasswordEditText = (EditText) findViewById(R.id.psw);
        mLoginButton = (Button) findViewById(R.id.login);
        mManageButton = (Button) findViewById(R.id.about);
        mAnimBack = (AnimateBackground) findViewById(R.id.animBack);
        mLoginViews = findViewById(R.id.login_all);
        mLoginTexts = findViewById(R.id.texts);
        Picasso.with(this).load(backs[new Random().nextInt(backs.length)]).into(mImageBack);

        mManageButton.setOnClickListener(this);
        mLoginButton.setOnClickListener(this);
        mAnimBack.setOnStateChangeListener(this);

        mRequestor = new InfoRequestor(this);
        mRequestor.setOnViewUpdateListener(this);




    }
    private void initLog() {
        StatService.setAppKey("556226b966");
        StatService.setOn(this,StatService.EXCEPTION_LOG);
        StatService.setLogSenderDelayed(10);
        StatService.setSendLogStrategy(this, SendStrategyEnum.APP_START, 1);
        StatService.setDebugOn(true);
    }
    public void initAccouts() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SecurePreferences preferences = new SecurePreferences(LoginActivity.this);
                mUsername = preferences.getString("username");
                mPassword = preferences.getString("password");
                if (mUsername == null || mUsername.equals("")
                        || mPassword == null || mPassword.equals("")) {
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mUserNameEditText.setText(mUsername);
                            mPasswordEditText.setText(mPassword);

                        }
                    });
                    mRequestor.check();
                }
            }
        }).start();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void fadeLogins() {
        mLoginViews.setVisibility(View.GONE);
    }
    private void showLogins() {
        mLoginViews.setVisibility(View.VISIBLE);
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login:
                login();
                break;
            case R.id.about:
                StatService.onEvent(getApplicationContext(), "click_about", "default");
                startActivity(new Intent(this,AboutActivity.class));
                break;
        }
    }
    public void login() {
        if (!Utils.isWifiConnected(this)) {
            mLoginButton.setText("死鬼，连WIFI了吗!?");
            return;
        }
        mRequestor.request(
                mUserNameEditText.getText().toString(),
                mPasswordEditText.getText().toString()
        );
        mUserNameEditText.setEnabled(false);
        mPasswordEditText.setEnabled(false);
        mLoginButton.setText("Logging In...");
        mLoginButton.setEnabled(false);
    }
    public void logout() {
        if (mAnimBack.getState() == AnimateBackground.AnimState.OPENED) {
            mAnimBack.close();
            mLoginButton.setText("Log In");
            mUserNameEditText.setEnabled(true);
            mPasswordEditText.setEnabled(true);
            mLoginButton.setEnabled(true);
        }
    }
    public void setLoginButtonText(final String text){
        mLoginButton.post(new Runnable() {
            @Override
            public void run() {
                mLoginButton.setText(text);
                mLoginButton.setEnabled(true);
                mUserNameEditText.setEnabled(true);
                mPasswordEditText.setEnabled(true);
            }
        });
    }
    @Override
    public void onChanged(AnimateBackground.AnimState state) {
        switch (state) {
            case OPENNING:
                fadeLogins();
                break;
            case CLOSING:
                mRoot.removeView(mInfoView);
                mInfoView.setTag(null);
                break;
            case OPENED:
                if (mRoot.findViewWithTag("info") == null)
                    mRoot.addView(mInfoView);
                break;
            case INIT:
                showLogins();
                break;
        }
    }

    @Override
    public void onUpdate(View view) {
        mAnimBack.open();
        mInfoView = view;
        mInfoView.setTag("info");
    }
}
