package com.wulee.administrator.zuji.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.wulee.administrator.zuji.R;
import com.wulee.administrator.zuji.base.BaseActivity;
import com.wulee.administrator.zuji.database.bean.PersonInfo;
import com.wulee.administrator.zuji.entity.StepInfo;
import com.wulee.administrator.zuji.utils.DateTimeUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

/**
 * Created by wulee on 2017/6/14 15:57
 */

public class StepHistoryActivity extends BaseActivity {

    @InjectView(R.id.iv_back)
    ImageView ivBack;
    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.bar_chart)
    LinearLayout barChart;
    @InjectView(R.id.chart)
    BarChart mChart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_step_history);
        ButterKnife.inject(this);

        initData();
    }

    private void initData() {
        title.setText("周运动历史");

        PersonInfo piInfo = BmobUser.getCurrentUser(PersonInfo.class);
        BmobQuery<StepInfo> query = new BmobQuery<>();
        query.addWhereEqualTo("personInfo", piInfo);    // 查询当前用户的所有计步信息
        query.include("personInfo");
        query.findObjects(new FindListener<StepInfo>() {
            @Override
            public void done(List<StepInfo> dataList, BmobException e) {
                if (e == null) {
                    if (null != dataList && dataList.size() > 0) {
                        List<Map.Entry<String, List<StepInfo>>> retMapList = processDataList(dataList);
                        if (retMapList != null && retMapList.size() > 0) {
                            generateData();
                        }
                    }
                } else {
                    Toast.makeText(StepHistoryActivity.this, "查询失败" + e.getMessage() + "," + e.getErrorCode(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        BarData data = generateData();

        // apply styling
        //data.setValueTypeface(mTfLight);
        data.setValueTextColor(Color.BLACK);
        mChart.getDescription().setEnabled(false);
        mChart.setDrawGridBackground(false);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //xAxis.setTypeface(mTfLight);
        xAxis.setDrawGridLines(false);

        YAxis leftAxis = mChart.getAxisLeft();
        //leftAxis.setTypeface(mTfLight);
        leftAxis.setLabelCount(20, false);
        leftAxis.setAxisMaximum(20000);
        leftAxis.setAxisMinimum(0);
        leftAxis.setSpaceTop(15f);

        YAxis rightAxis = mChart.getAxisRight();
        //rightAxis.setTypeface(mTfLight);
        rightAxis.setLabelCount(20, false);
        rightAxis.setAxisMaximum(20000);
        rightAxis.setAxisMinimum(0);
        rightAxis.setSpaceTop(15f);

        // set data
        mChart.setData(data);
        mChart.setFitBars(true);

        // do not forget to refresh the chart
        mChart.animateY(1000);
    }


    private List<Map.Entry<String, List<StepInfo>>> processDataList(List<StepInfo> dataList) {
        Date date1 = DateTimeUtils.getDateBefore(new Date(), 7);
        Date date2 = new Date();
        Iterator<StepInfo> iter = dataList.iterator();
        while (iter.hasNext()) {
            StepInfo step = iter.next();

            Date date = null;
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                date = format.parse(step.getUpdatedAt());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (date.before(date1) || date.after(date2)) {//去除7天前的数据，以及当天之后的数据
                iter.remove();
            }
        }

        //对数据按日期分组
        Map<String, List<StepInfo>> map = new HashMap<>();
        for (StepInfo stepInfo : dataList) {
            String key = stepInfo.groupKey();
            // 按照key取出子集合
            List<StepInfo> subStepList = map.get(key);

            // 若子集合不存在，则重新创建一个新集合，并把当前stepInfo加入，然后put到map中
            if (subStepList == null) {
                subStepList = new ArrayList<>();
                subStepList.add(stepInfo);
                map.put(key, subStepList);
            } else {
                // 若子集合存在，则直接把当前stepInfo加入即可
                subStepList.add(stepInfo);
            }
        }

        List<Map.Entry<String, List<StepInfo>>> list = new ArrayList<>(map.entrySet());
        //升序排序
        Collections.sort(list, (o1, o2) -> o1.getKey().compareTo(o2.getKey()));
        return list;
    }


    @OnClick(R.id.iv_back)
    public void onViewClicked() {
        finish();
    }


    private BarData generateData() {

        ArrayList<BarEntry> entries = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            entries.add(new BarEntry(i, (float)(Math.random()*20000)));
        }

        BarDataSet d = new BarDataSet(entries, "周运动历史");
        d.setColors(ColorTemplate.VORDIPLOM_COLORS);
        d.setBarShadowColor(Color.rgb(203, 203, 203));

        ArrayList<IBarDataSet> sets = new ArrayList<>();
        sets.add(d);

        BarData cd = new BarData(sets);
        cd.setBarWidth(0.9f);
        return cd;
    }
}
