package com.wulee.administrator.bmobtest.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.wulee.administrator.bmobtest.R;
import com.wulee.administrator.bmobtest.entity.LocationInfo;

import java.util.ArrayList;


public class LocationAdapter extends BaseQuickAdapter<LocationInfo> {

    public LocationAdapter(int layoutResId, ArrayList<LocationInfo> dataList) {
        super(layoutResId, dataList);
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, LocationInfo locationInfo) {

        baseViewHolder.setText(R.id.tv_location,locationInfo.address);
        baseViewHolder.setText(R.id.tv_time , locationInfo.getCreatedAt());

    }
}
