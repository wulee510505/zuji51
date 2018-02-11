package com.wulee.administrator.zuji.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.wulee.administrator.zuji.R;
import com.wulee.administrator.zuji.entity.HistoryInfo;

import java.util.ArrayList;


public class TodayInHistoryAdapter extends BaseQuickAdapter<HistoryInfo,BaseViewHolder> {

    public TodayInHistoryAdapter(int layoutResId, ArrayList<HistoryInfo> dataList) {
        super(layoutResId, dataList);
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, HistoryInfo historyInfo) {
        baseViewHolder.setText(R.id.tv_title,historyInfo.getTitle());
        baseViewHolder.setText(R.id.tv_date,historyInfo.getLsdate());
    }
}
