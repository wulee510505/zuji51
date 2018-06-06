package com.wulee.administrator.zuji.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.wulee.administrator.zuji.R;
import com.wulee.administrator.zuji.base.BaseActivity;
import com.wulee.administrator.zuji.utils.ImageUtil;
import com.wulee.administrator.zuji.widget.DotIndicator;
import com.wulee.administrator.zuji.widget.ZoomImageView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by wulee on 2017/10/13 13:50
 * 查看多图
 */

public class BigMultiImgActivity extends BaseActivity {

    @InjectView(R.id.viewpager_img)
    ViewPager viewpagerImg;
    @InjectView(R.id.dot_indicator)
    DotIndicator dotIndicator;

    public static final String IMAGES_URL = "images_url";
    public static final String IMAGE_INDEX = "image_index";
    public static final String SHOW_TITLE = "show_title";

    @InjectView(R.id.iv_back)
    ImageView ivBack;
    @InjectView(R.id.iv_delete)
    ImageView ivDelete;
    @InjectView(R.id.titlelayout)
    RelativeLayout titlelayout;



    private String[] imgUrlsArray;
    private List<String> imgUrls;
    private int index;
    private boolean showtitle;

    private BigImgPagerAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.big_mutil_image);
        ButterKnife.inject(this);

        initData();
        addListener();
    }

    private void initData() {
        Intent intent = getIntent();
        imgUrlsArray = intent.getStringArrayExtra(IMAGES_URL);
        index = intent.getIntExtra(IMAGE_INDEX, 0);
        showtitle = intent.getBooleanExtra(SHOW_TITLE, false);

        if(showtitle){
            titlelayout.setVisibility(View.VISIBLE);
        }else{
            titlelayout.setVisibility(View.GONE);
        }

        mAdapter = new BigImgPagerAdapter(imgUrls);
        viewpagerImg.setAdapter(mAdapter);

        if (imgUrlsArray != null && imgUrlsArray.length > 0) {
            imgUrls = new ArrayList<>();
            for (int i = 0; i < imgUrlsArray.length; i++) {
                imgUrls.add(imgUrlsArray[i]);
            }
            mAdapter.setmImgUrls(imgUrls);
            if (imgUrls.size() > 1) {
                dotIndicator.setViewPager(viewpagerImg);
            }
        }
        viewpagerImg.setCurrentItem(index);
    }

    private void addListener() {
        viewpagerImg.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                index = position;
                dotIndicator.onPageSelected(index);
            }
        });
    }


    @Override
    protected int getImmersionBarColor() {
        return R.color.transparent;
    }

    @OnClick({R.id.iv_back, R.id.iv_delete})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                Intent intent = getIntent();
                List<String> urllist = mAdapter.getmImgUrls();
                String[] array = new String[urllist.size()];
                for(int i=0;i<urllist.size();i++){
                    array[i]= urllist.get(i);
                }
                intent.putExtra("remain_urls", array);
                setResult(RESULT_OK,intent);
                finish();
                break;
            case R.id.iv_delete:
                mAdapter.setmImgUrls(deletePic(index));
                if(index == 0){
                    viewpagerImg.setCurrentItem(0);
                }else if(index > 0){
                    viewpagerImg.setCurrentItem(index-1);
                }
                dotIndicator.setViewPager(viewpagerImg);
                break;
        }
    }

    private List<String> deletePic(int index) {
        List<String> imgUrls = mAdapter.getmImgUrls();
        if (imgUrls != null && imgUrls.size() > 0) {
            if (index >= 0 && index < imgUrls.size()) {
                imgUrls.remove(index);
            }
        }
        return imgUrls;
    }


    public class BigImgPagerAdapter extends PagerAdapter {

        private List<String> mImgUrls;

        public BigImgPagerAdapter(List<String> imgUrls) {
            this.mImgUrls = imgUrls;
        }

        public void setmImgUrls(List<String> mImgUrls) {
            this.mImgUrls = mImgUrls;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mImgUrls != null ? mImgUrls.size() : 0;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            View view = LayoutInflater.from(BigMultiImgActivity.this).inflate(R.layout.big_single_image, null);
            ZoomImageView imageView = view.findViewById(R.id.iv_bigimg);
            ImageUtil.setDefaultImageView(imageView, mImgUrls.get(position), R.mipmap.bg_pic_def_rect, BigMultiImgActivity.this);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            RelativeLayout rlLayout = (RelativeLayout) object;
            container.removeView(rlLayout);
        }

        public List<String> getmImgUrls() {
            return mImgUrls;
        }
    }
}
