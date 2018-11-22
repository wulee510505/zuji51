package com.wulee.administrator.zuji.ui;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.GridView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.wulee.administrator.zuji.R;
import com.wulee.administrator.zuji.adapter.PublishPicGridAdapter;
import com.wulee.administrator.zuji.base.BaseActivity;
import com.wulee.administrator.zuji.database.bean.PersonInfo;
import com.wulee.administrator.zuji.entity.CircleContent;
import com.wulee.administrator.zuji.entity.Constant;
import com.wulee.administrator.zuji.entity.PublishPicture;
import com.wulee.administrator.zuji.utils.FileUtils;
import com.wulee.administrator.zuji.utils.ImageUtil;
import com.wulee.administrator.zuji.utils.NewGlideEngine;
import com.wulee.administrator.zuji.utils.OtherUtil;
import com.wulee.administrator.zuji.utils.UIUtils;
import com.wulee.administrator.zuji.widget.AnFQNumEditText;
import com.wulee.administrator.zuji.widget.BaseTitleLayout;
import com.wulee.administrator.zuji.widget.TitleLayoutClickListener;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.datatype.BmobGeoPoint;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UploadBatchListener;

import static com.wulee.administrator.zuji.App.aCache;

/**
 * Created by wulee on 2017/8/22 11:40
 */

public class PublishCircleActivity extends BaseActivity {

    @InjectView(R.id.edittext)
    AnFQNumEditText edittext;
    @InjectView(R.id.gridview_pic)
    GridView gridviewPic;
    @InjectView(R.id.titlelayout)
    BaseTitleLayout titlelayout;
    @InjectView(R.id.tbtn_location)
    ToggleButton tbtnLocation;


    public static final String PUBLISH_TYPE = "publish_type";

    private int mType;
    public static final int TYPE_PUBLISH_TEXT_AND_IMG = 0;
    public static final int TYPE_PUBLISH_TEXT_ONLY = 1;

    private PublishPicGridAdapter mGridAdapter;
    private ArrayList<PublishPicture> picList = new ArrayList<>();
    private int maxSelPicNum = 9;

    public static final String ACTION_PUBLISH_CIRCLE_OK = "action_publish_circle_ok";

    private String[] imgUrls;
    private boolean isShowLocation = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.circle_publish);

        ButterKnife.inject(this);

        mType = getIntent().getIntExtra(PUBLISH_TYPE, 0);

        initView();
        addListner();
    }

    private void initView() {
        edittext.setEtHint("说点什么吧...")//设置提示文字
                .setEtMinHeight(UIUtils.dip2px(120))//设置最小高度，单位px
                .setLength(1000)//设置总字数
                .setType(AnFQNumEditText.SINGULAR)//TextView显示类型(SINGULAR单数类型)(PERCENTAGE百分比类型)
                .show();
        if (mType == TYPE_PUBLISH_TEXT_AND_IMG) {
            gridviewPic.setVisibility(View.VISIBLE);

            PublishPicture pic = new PublishPicture();
            pic.setId(-1);
            pic.setPath("");
            picList.add(picList.size(), pic);
            mGridAdapter = new PublishPicGridAdapter(picList, this);
            gridviewPic.setAdapter(mGridAdapter);
        } else if (mType == TYPE_PUBLISH_TEXT_ONLY) {
            gridviewPic.setVisibility(View.GONE);
        }
    }

    private void addListner() {
        titlelayout.setOnTitleClickListener(new TitleLayoutClickListener() {
            @Override
            public void onLeftClickListener() {
                super.onLeftClickListener();
                finish();
            }

            @Override
            public void onRightTextClickListener() {
                super.onRightTextClickListener();
            }

            @Override
            public void onRightImg1ClickListener() {
                super.onRightImg1ClickListener();
                publishCircleContent();
            }
        });
        gridviewPic.setOnItemClickListener((adapterView, view, pos, l) -> {
            PublishPicture pic = picList.get(pos);
            if (null != pic) {
                if (pic.getId() == -1) {
                    Matisse.from(PublishCircleActivity.this)
                            .choose(MimeType.allOf())
                            .maxSelectable(maxSelPicNum - picList.size() + 1)
                            .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                            .thumbnailScale(0.85f)
                            .imageEngine(new NewGlideEngine())
                            .forResult(0);
                } else {
                    //预览大图
                    Intent intent = new Intent(PublishCircleActivity.this, BigMultiImgActivity.class);
                    intent.putExtra(BigMultiImgActivity.IMAGES_URL, imgUrls);
                    intent.putExtra(BigMultiImgActivity.IMAGE_INDEX, pos);
                    intent.putExtra(BigMultiImgActivity.SHOW_TITLE, true);
                    startActivityForResult(intent, 0);
                }
            }
        });
        tbtnLocation.setOnCheckedChangeListener((tb, b) -> {
           if(tb.isChecked()){
               isShowLocation =  true;
           }else{
               isShowLocation =  false;
           }
        });
    }

    /**
     * 发表圈子内容
     */
    private void publishCircleContent() {
        String content = edittext.getInputContent();
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(this, "说点什么吧@^@", Toast.LENGTH_SHORT).show();
            return;
        }
        PersonInfo piInfo = BmobUser.getCurrentUser(PersonInfo.class);
        if (null == piInfo) {
            OtherUtil.showToastText("请重新登录");
            return;
        }
        if (mType == TYPE_PUBLISH_TEXT_AND_IMG) {
            final CircleContent circlrContent = new CircleContent(CircleContent.TYPE_TEXT_AND_IMG);
            circlrContent.setId(System.currentTimeMillis());
            circlrContent.setUserId(piInfo.getUid());
            circlrContent.setContent(content);
            if(isShowLocation){
                double lastlat = Double.parseDouble(aCache.getAsString("lat"));
                double lastlon = Double.parseDouble(aCache.getAsString("lon"));
                circlrContent.setLocationInfo(new BmobGeoPoint(lastlon,lastlat));
                String currCity = aCache.getAsString("location_city");
                if (!TextUtils.isEmpty(currCity))
                    circlrContent.setLocation(currCity);
                String address = aCache.getAsString("address");
                if (!TextUtils.isEmpty(address))
                    circlrContent.setAddress(address);
            }
            circlrContent.personInfo = piInfo;
            if (picList.size() > 1) {
                picList.remove(picList.size() - 1);

                final String[] filePaths = new String[picList.size()];
                for (int i = 0; i < picList.size(); i++) {
                    filePaths[i] = picList.get(i).getPath();
                }
                showProgressDialog(true);
                BmobFile.uploadBatch(filePaths, new UploadBatchListener() {
                    @Override
                    public void onSuccess(List<BmobFile> files, List<String> urls) {
                        //1、files-上传完成后的BmobFile集合，是为了方便大家对其上传后的数据进行操作，例如你可以将该文件保存到表中
                        //2、urls-上传文件的完整url地址
                        if (urls.size() == filePaths.length) {//如果数量相等，则代表文件全部上传完成
                            String[] imgUrls = new String[urls.size()];
                            for (int i = 0; i < urls.size(); i++) {
                                imgUrls[i] = urls.get(i);
                            }
                            circlrContent.setImgUrls(imgUrls);
                            circlrContent.save(new SaveListener<String>() {
                                @Override
                                public void done(String s, BmobException e) {
                                    stopProgressDialog();
                                    if (e == null) {
                                        sendBroadcast(new Intent(ACTION_PUBLISH_CIRCLE_OK));
                                        PublishCircleActivity.this.finish();
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(int statuscode, String errormsg) {
                        Toast.makeText(PublishCircleActivity.this, "错误码" + statuscode + ",错误描述：" + errormsg, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProgress(int curIndex, int curPercent, int total, int totalPercent) {
                        //1、curIndex--表示当前第几个文件正在上传
                        //2、curPercent--表示当前上传文件的进度值（百分比）
                        //3、total--表示总的上传文件数
                        //4、totalPercent--表示总的上传进度（百分比）
                    }
                });
            } else {
                OtherUtil.showToastText("请添加图片");
            }
        } else if (mType == TYPE_PUBLISH_TEXT_ONLY) {
           showProgressDialog(true);
            final CircleContent circlrContent = new CircleContent(CircleContent.TYPE_ONLY_TEXT);
            circlrContent.setId(System.currentTimeMillis());
            circlrContent.setUserId(piInfo.getUid());
            circlrContent.setContent(content);
            if(isShowLocation){
                double lastlat = Double.parseDouble(aCache.getAsString("lat"));
                double lastlon = Double.parseDouble(aCache.getAsString("lon"));
                circlrContent.setLocationInfo(new BmobGeoPoint(lastlon,lastlat));
                String currCity = aCache.getAsString("location_city");
                if (!TextUtils.isEmpty(currCity))
                    circlrContent.setLocation(currCity);
                String address = aCache.getAsString("address");
                if (!TextUtils.isEmpty(address))
                    circlrContent.setAddress(address);
            }
            circlrContent.personInfo = piInfo;
            circlrContent.save(new SaveListener<String>() {
                @Override
                public void done(String s, BmobException e) {
                    stopProgressDialog();
                    if (e == null) {
                        sendBroadcast(new Intent(ACTION_PUBLISH_CIRCLE_OK));
                        PublishCircleActivity.this.finish();
                    }
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (picList != null) {
            picList.clear();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 0:
                    String[] imgUrlsArray = null;
                    List<Uri> selectedUri = Matisse.obtainResult(data);
                    ContentResolver resolver = getContentResolver();
                    if (selectedUri != null && selectedUri.size() > 0) {
                        imgUrlsArray = new String[selectedUri.size()];
                        for (int i = 0; i < selectedUri.size(); i++) {
                            Uri uri = selectedUri.get(i);
                            String path = FileUtils.getFilePathFromContentUri(uri, resolver);
                            Bitmap bmp = ImageUtil.resizeBitmap(path,1024,1024);
                            File file = null;
                            String dstFilePath = "";
                            try {
                                File dir = new File(Constant.TEMP_FILE_PATH);
                                if (!dir.exists()) {
                                    dir.mkdirs();
                                }
                                dstFilePath = Constant.TEMP_FILE_PATH + "circle_" + System.currentTimeMillis() + ".png";
                                file = ImageUtil.resizeBitmapAndSave(bmp, dstFilePath,0.7f);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (file != null && file.exists()) {
                                imgUrlsArray[i] = dstFilePath;
                            }
                        }
                    }
                    if (imgUrlsArray != null && imgUrlsArray.length > 0) {
                        imgUrls = imgUrlsArray;
                        picList.clear();
                        for (int i = 0; i < imgUrlsArray.length; i++) {
                            PublishPicture pic = new PublishPicture();
                            pic.setId(i);
                            pic.setPath(imgUrlsArray[i]);
                            picList.add(pic);
                        }
                        if (picList.size() < 9) {
                            PublishPicture pic = new PublishPicture();
                            pic.setId(-1);
                            pic.setPath("");
                            picList.add(picList.size(), pic);
                        }
                        mGridAdapter.setSelPic(picList);
                    }
                    break;
            }
        }
    }
}
