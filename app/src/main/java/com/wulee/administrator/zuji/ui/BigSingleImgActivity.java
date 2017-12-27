package com.wulee.administrator.zuji.ui;

import android.os.Bundle;

import com.wulee.administrator.zuji.R;
import com.wulee.administrator.zuji.base.BaseActivity;
import com.wulee.administrator.zuji.utils.ImageUtil;
import com.wulee.administrator.zuji.utils.UIUtils;
import com.wulee.administrator.zuji.widget.SuperImageView;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by wulee on 2017/8/25 13:50
 */

public class BigSingleImgActivity extends BaseActivity {

    @InjectView(R.id.iv_bigimg)
    SuperImageView ivBigimg;

    public static final String IMAGE_URL = "image_url";

    private String imgUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.big_single_image);
        ButterKnife.inject(this);


        int sw = UIUtils.getScreenWidthAndHeight(this)[0];
       /* RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) ivBigimg.getLayoutParams();
        rlp.width = sw;
        rlp.height = sw * 3/2;
        ivBigimg.setLayoutParams(rlp);*/

        imgUrl = getIntent().getStringExtra(IMAGE_URL);
        ImageUtil.setDefaultImageView(ivBigimg,imgUrl,R.mipmap.bg_pic_def_rect,this);
    }

    @Override
    protected int getStateBarColor() {
        return R.color.color_transparent;
    }

}
