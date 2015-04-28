package pro.kost.bupt;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.model.Point;
import com.db.chart.view.LineChartView;
import com.db.chart.view.YController;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.bounce.BounceEaseOut;
import com.db.chart.view.animation.easing.circ.CircEaseOut;
import com.db.chart.view.animation.easing.cubic.CubicEaseOut;
import com.db.chart.view.animation.easing.elastic.ElasticEaseOut;
import com.db.chart.view.animation.easing.expo.ExpoEaseOut;
import com.db.chart.view.animation.easing.linear.LinearEase;
import com.db.chart.view.animation.easing.quad.QuadEaseOut;
import com.db.chart.view.animation.easing.sine.SineEaseOut;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import cn.edu.bupt.AndroidWifiHelper;
import pro.kost.bupt.R;

public class ChartActivity extends Activity implements View.OnClickListener{
    private LineChartView mChart;
    private String[] titles;
    private float[] values;
    private int step = 100;
    private String hintString = "";
    private String metrix = "";
    private TextView mHint = null;
    private boolean isBytes = false;
    private int ByteType = 0;
     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        if(getActionBar() != null)
            getActionBar().hide();
        initValues();
        initChart();
    }
    private void initValues() {
        if (getIntent().getStringExtra("type").equals("time")) {
            titles = AndroidWifiHelper.sDataSet.timeDatas.titles;
            values = AndroidWifiHelper.sDataSet.timeDatas.values;
            metrix = " 小时";
            hintString = "累计时长 ";
        } else  if (getIntent().getStringExtra("type").equals("money")) {

            for (String title : AndroidWifiHelper.sDataSet.moneyDatas.titles) {
                Utils.log("hehe = " + title);
            }
            titles = AndroidWifiHelper.sDataSet.moneyDatas.titles;
            values = AndroidWifiHelper.sDataSet.moneyDatas.values;
            metrix = " 元";
            hintString = "累计消费  ";
        } if (getIntent().getStringExtra("type").equals("byte")) {
            titles = AndroidWifiHelper.sDataSet.byteDatas.titles;
            values = AndroidWifiHelper.sDataSet.byteDatas.values;
            metrix = " GB";
            hintString = "累计总流量 ";
            isBytes = true;
        }

        float max = values[0];
        for (float value : values) {
            max = value > max ? value : max;
        }
        step = (int)(max / 5);

    }
    private void initChart() {
        mHint = (TextView) findViewById(R.id.hint);
        mChart = (LineChartView) findViewById(R.id.chart);
        mChart.setOnClickListener(this);
        mHint.setText(hintString);
        Paint gridPaint = new Paint();
        gridPaint.setAlpha(30);
        gridPaint.setColor(Color.parseColor("#d6d6d6"));
        gridPaint.setStrokeWidth(1f);
        mChart.setLabelsMetric(metrix);

        mChart.setStep(step > 2 ? step : 2);
        mChart.setHorizontalGrid(gridPaint);
        mChart.setBackgroundColor(Color.parseColor("#eb7054"));
        mChart.setLabelColor(Color.WHITE);
        mChart.setYLabels(YController.LabelPosition.OUTSIDE);
        mChart.addData(getLineSet(titles,values));
//        if (getIntent().getStringExtra("type").equals("byte")) {
//            mChart.addData(getLineSet(AndroidWifiHelper.sDataSet.upDatas.titles,
//                    AndroidWifiHelper.sDataSet.upDatas.values,Color.parseColor("#e8c698"),true));
//            mChart.addData(getLineSet(AndroidWifiHelper.sDataSet.downDatas.titles,
//                    AndroidWifiHelper.sDataSet.downDatas.values,Color.parseColor("#fff586"),true));
//        }
        mChart.show(getAnimation());
    }
    private LineSet getLineSet(String[] titles,float[] values) {
        return getLineSet(titles,values,Color.WHITE,false);
    }
    private LineSet getLineSet(String[] titles,float[] values,int color,boolean dash) {
        LineSet lineSet = new LineSet();
        lineSet.addPoints(titles,values);
        lineSet.setDotsColor(Color.BLACK);
        lineSet.setLineColor(color);
        lineSet.setLineThickness(Tools.fromDpToPx(3f));
        lineSet.setDots(true);
        lineSet.setDashed(dash);
        lineSet.setDotsColor(color);
        lineSet.setDotsRadius(0.4f);
        lineSet.setSmooth(true);
        return lineSet;
    }
    private Animation getAnimation() {
        Animation animation = new Animation().setEasing(new QuadEaseOut());
        animation.setDuration(320);
        animation.setOverlap(0.7f);
        return animation;
    }

    @Override
    public void onClick(View v) {
        if(!isBytes)
            return;
        ByteType ++;
        switch (ByteType % 3) {
            case 0:
                mHint.setText("累计总流量 ");
                mChart.updateValues(0, AndroidWifiHelper.sDataSet.byteDatas.values);
                mChart.notifyDataUpdate();
                break;
            case 1:
                mHint.setText("累计上传 ");
                mChart.updateValues(0,AndroidWifiHelper.sDataSet.upDatas.values);
                mChart.notifyDataUpdate();
                break;
            case 2:
                mHint.setText("累计下载 ");
                mChart.updateValues(0,AndroidWifiHelper.sDataSet.downDatas.values);
                mChart.notifyDataUpdate();
                break;
        }


    }
}
