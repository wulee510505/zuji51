package com.wulee.administrator.zuji.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.wulee.administrator.zuji.R;
import com.wulee.administrator.zuji.base.BaseActivity;
import com.wulee.administrator.zuji.database.bean.PersonInfo;
import com.wulee.administrator.zuji.entity.StepInfo;
import com.wulee.administrator.zuji.utils.DateTimeUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
                        Map<String, List<StepInfo>> retMapList = processDataList(dataList);
                        if (retMapList != null && retMapList.size() > 0) {
                            //generateData(retMapList);
                        }
                    }
                } else {
                    Toast.makeText(StepHistoryActivity.this, "查询失败" + e.getMessage() + "," + e.getErrorCode(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        BarData data = generateData(new HashMap<>());

        data.setValueTextColor(Color.BLACK);
        mChart.getDescription().setEnabled(false);
        mChart.setDrawGridBackground(false);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);//不绘制格网线
        xAxis.setGranularity(1f);//设置最小间隔，防止当放大时，出现重复标签
        // 显示x轴标签
        String[] xlableArray = new String[]{"周一","周二","周三","周四","周五","周六","周日"};
        List<String> xLabels = Arrays.asList(xlableArray);
        IAxisValueFormatter formatter = (value, axis) -> {
            int index = (int) value;
            if (index < 0 || index >= xLabels.size()) {
                return "";
            }
            return xLabels.get(index);
        };
        xAxis.setValueFormatter(formatter);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setLabelCount(10, false);
        leftAxis.setAxisMaximum(20000);
        leftAxis.setAxisMinimum(0);
        leftAxis.setSpaceTop(15f);


        leftAxis.setDrawGridLines(true); //从y轴发出横向直线
        //水平方向辅助线的颜色
        leftAxis.setGridColor(Color.parseColor("#F5F5F9"));
        leftAxis.setGridLineWidth(1f);
        leftAxis.enableGridDashedLine(10f, 10f, 0f); //设置横向表格为虚线

        mChart.getAxisRight().setEnabled(false);//禁用右侧y轴
        mChart.getLegend().setEnabled(false);
        mChart.setHighlightFullBarEnabled(false);
        mChart.setTouchEnabled(false);
        // set data
        mChart.setData(data);
        mChart.setFitBars(true);

        // do not forget to refresh the chart
        mChart.animateY(1000);
    }


    private Map<String, List<StepInfo>> processDataList(List<StepInfo> dataList) {
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
        return map;
    }


    @OnClick(R.id.iv_back)
    public void onViewClicked() {
        finish();
    }


    private BarData generateData(Map<String, List<StepInfo>> map) {
        ArrayList<BarEntry> entries = new ArrayList<>();

        // 定义输出日期格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date currentDate = new Date();

        // 比如今天是2018-07-20
        List<Date> days =  DateTimeUtils.dateToWeek(currentDate);
        for (Date date : days) {
            System.out.println(sdf.format(date));
        }

        // 用迭代器遍历map
        Iterator iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String key = (String) entry.getKey();
            for(Date date : days){
                if(date.equals(DateTimeUtils.stringToDate(key))){

                }
            }

        }



        for (int i = 0; i < 7; i++) {
            entries.add(new BarEntry(i, (float)(Math.random()*20000)));
        }

        BarDataSet d = new BarDataSet(entries, "周运动历史");
        d.setColors(ContextCompat.getColor(this,R.color.light_red));
        d.setBarShadowColor(Color.rgb(203, 203, 203));

        ArrayList<IBarDataSet> sets = new ArrayList<>();
        sets.add(d);

        BarData cd = new BarData(sets);
        cd.setBarWidth(0.7f);
        return cd;
    }
}
