package com.wulee.administrator.zuji.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wulee.administrator.zuji.R;
import com.wulee.administrator.zuji.base.BaseActivity;
import com.wulee.administrator.zuji.database.DBHandler;
import com.wulee.administrator.zuji.database.bean.PersonInfo;
import com.wulee.administrator.zuji.utils.AppUtils;
import com.wulee.administrator.zuji.utils.FileUtils;
import com.wulee.administrator.zuji.utils.ImageUtil;
import com.wulee.administrator.zuji.utils.LocationUtil;
import com.wulee.administrator.zuji.utils.NewGlideEngine;
import com.wulee.administrator.zuji.utils.OtherUtil;
import com.wulee.administrator.zuji.widget.ActionSheet;
import com.yanzhenjie.permission.AndPermission;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;

import java.io.File;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

import static cn.bmob.v3.BmobUser.getCurrentUser;
import static com.wulee.administrator.zuji.App.aCache;


/**
 * Created by wulee on 2016/12/15 17:25
 */

public class PersonalInfoActivity extends BaseActivity implements ActionSheet.MenuItemClickListener {

    private static final int AVATAR_REQUEST_CODE = 100;
    private static final int BIRTHDAY_REQUEST_CODE = 101;

    @InjectView(R.id.iv_back)
    ImageView ivBack;
    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.iv_submit)
    ImageView ivSubmit;
    @InjectView(R.id.titlelayout)
    RelativeLayout titlelayout;
    @InjectView(R.id.user_photo)
    ImageView userPhoto;
    @InjectView(R.id.name_tv)
    TextView nameTv;
    @InjectView(R.id.et_name)
    EditText etName;
    @InjectView(R.id.rl_name)
    RelativeLayout rlName;
    @InjectView(R.id.et_gender)
    TextView etGender;
    @InjectView(R.id.rl_gender)
    RelativeLayout rlGender;
    @InjectView(R.id.container)
    LinearLayout container;
    @InjectView(R.id.tv_birthday)
    TextView tvBirthday;
    @InjectView(R.id.rl_birthday)
    RelativeLayout rlBirthday;


    private String headerimgurl;
    private String mBirthday;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.me_personal_center);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        AppUtils.setStateBarColor(this, R.color.colorAccent);

        ButterKnife.inject(this);

        initData();
    }


    private void initData() {
        title.setText("个人中心");
        PersonInfo personInfo = DBHandler.getCurrPesonInfo();
        if (null != personInfo) {
            ImageUtil.setCircleImageView(userPhoto, personInfo.getHeader_img_url(), R.mipmap.icon_user_def, this);

            if (!TextUtils.isEmpty(personInfo.getName())){
                etName.setText(personInfo.getName());
            } else{
                etName.setText("游客");
            }
            if (!TextUtils.isEmpty(personInfo.getSex())){
                etGender.setText(personInfo.getSex());
            } else {
                etGender.setText("其他");
            }
            if (!TextUtils.isEmpty(personInfo.getBirthday())){
                tvBirthday.setText(personInfo.getBirthday());
            } else {
                tvBirthday.setText("未选择");
            }
        }
    }


    private void updatePersonInfo() {
        final String name = etName.getText().toString().trim();
        String genderStr = etGender.getText().toString().trim();

        PersonInfo personInfo = getCurrentUser(PersonInfo.class);
        if(personInfo == null){
            return;
        }
        personInfo.setName(name);
        personInfo.setSex(genderStr);
        if(!TextUtils.isEmpty(mBirthday)){
            personInfo.setBirthday(mBirthday);
        }
        personInfo.setHeader_img_url(headerimgurl);
        personInfo.update(new UpdateListener() {
            @Override
            public void done(BmobException e) {
                 //stopProgressDialog();
                if (e == null) {
                    PersonInfo pi = DBHandler.getCurrPesonInfo();
                    if (null != pi) {
                        pi.setName(name);
                        if (!TextUtils.isEmpty(headerimgurl))
                            pi.setHeader_img_url(headerimgurl);
                        DBHandler.updatePesonInfo(pi);
                    }
                    OtherUtil.showToastText("更新个人信息成功");
                } else {
                    if (e.getErrorCode() == 206) {
                        OtherUtil.showToastText("您的账号在其他地方登录，请重新登录");
                        aCache.put("has_login", "no");
                        LocationUtil.getInstance().stopGetLocation();
                        AppUtils.AppExit(PersonalInfoActivity.this);
                        PersonInfo.logOut();
                        startActivity(new Intent(PersonalInfoActivity.this, LoginActivity.class));
                    } else {
                        OtherUtil.showToastText("更新个人信息失败:" + e.getMessage());
                    }
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case AVATAR_REQUEST_CODE:// 头像的返回
                if (resultCode == RESULT_OK && data != null) {
                    List<Uri> selectedUri = Matisse.obtainResult(data);
                    if(selectedUri!=null&& selectedUri.size()>0){
                        Uri uri = selectedUri.get(0);
                        String path = FileUtils.getFilePathFromContentUri(uri,getContentResolver());
                        if (!TextUtils.isEmpty(path)) {
                            Bitmap suitBitmap = BitmapFactory.decodeFile(path);
                            if (null != suitBitmap)
                                userPhoto.setImageBitmap(ImageUtil.toRoundBitmap(suitBitmap));
                            if (!checkInternetConnection()) {
                                return;
                            }
                            uploadImgFile(path);
                        }
                    }
                }
                break;
            case BIRTHDAY_REQUEST_CODE:// 生日的返回
                if (resultCode == RESULT_OK && data != null) {
                     String birthday = data.getStringExtra(SelectDateActivity.SELECT_DATE);
                     if (!TextUtils.isEmpty(birthday)) {
                         tvBirthday.setText(birthday);
                         mBirthday = birthday;
                     }
                }
                break;
        }
    }



    /**
     * 上传图片
     *
     * @param picPath
     */
    private void uploadImgFile(final String picPath) {
        final BmobFile bmobFile = new BmobFile(new File(picPath));
        bmobFile.uploadblock(new UploadFileListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    headerimgurl = bmobFile.getFileUrl();

                    PersonInfo personInfo = getCurrentUser(PersonInfo.class);
                    personInfo.setHeader_img_url(headerimgurl);
                    personInfo.update(new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            //stopProgressDialog();
                            if (e == null) {
                                PersonInfo piInfo = DBHandler.getCurrPesonInfo();
                                if (null != piInfo) {
                                    piInfo.setHeader_img_url(headerimgurl);
                                }
                                DBHandler.updatePesonInfo(piInfo);
                                Toast.makeText(PersonalInfoActivity.this, "更新个人头像成功", Toast.LENGTH_SHORT).show();
                            } else {
                                if (e.getErrorCode() == 206) {
                                    OtherUtil.showToastText("您的账号在其他地方登录，请重新登录");
                                    aCache.put("has_login", "no");
                                    LocationUtil.getInstance().stopGetLocation();
                                    AppUtils.AppExit(PersonalInfoActivity.this);
                                    PersonInfo.logOut();
                                    startActivity(new Intent(PersonalInfoActivity.this, LoginActivity.class));
                                } else {
                                    OtherUtil.showToastText("更新个人头像失败:" + e.getMessage());
                                }
                            }
                        }
                    });
                } else {
                    Toast.makeText(PersonalInfoActivity.this, "头像上传失败"+ e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onProgress(Integer value) {
                // 返回的上传进度（百分比）
                OtherUtil.showToastText("上传" + value + "%");
            }
        });
    }

    @OnClick({R.id.iv_back, R.id.iv_submit, R.id.user_photo, R.id.rl_gender,R.id.rl_birthday})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.iv_submit:
                //showProgressDialog(false);
                updatePersonInfo();
                break;
            case R.id.user_photo:
               /* Intent intent = new Intent(this, PictureActivity.class);
                //必须传入的输出临时文件夹路径
                intent.putExtra(PictureActivity.INTENT_KEY_PHOTO_TMP_PATH_DIR, Constant.TEMP_FILE_PATH);
                // 按比例剪切
                intent.putExtra(PictureActivity.INTENT_KEY_CAN_CUT_PHOTO, true);
                intent.putExtra(PictureActivity.INTENT_KEY_CUT_PHOTO_ASPECTX, 1);
                intent.putExtra(PictureActivity.INTENT_KEY_CUT_PHOTO_ASPECTY, 1);
                // 压缩，最大长宽在600左右；小于600则原样输出，大于600则进行压缩
                intent.putExtra(PictureActivity.INTENT_KEY_COMPRESS_PHOTO, true);
                intent.putExtra(PictureActivity.INTENT_KEY_ENABLE_CUSCOMPRESS, true);
                intent.putExtra(PictureActivity.INTENT_KEY_COMPRESS_PHOTO_MAXPIXEL, 600);
                startActivityForResult(intent, AVATAR_REQUEST_CODE);*/


                AndPermission.with(this)
                        .runtime()
                        .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .onGranted(permissions -> {
                            Matisse.from(PersonalInfoActivity.this)
                                    .choose(MimeType.allOf())
                                    .countable(true)
                                    //.capture(true)
                                    //.captureStrategy(new CaptureStrategy(false,"com.wulee.administrator.zuji"))
                                    .maxSelectable(1)
                                    .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                                    . thumbnailScale(0.85f)
                                    .imageEngine(new NewGlideEngine())
                                    .forResult(AVATAR_REQUEST_CODE);
                        })
                        .start();
                break;
            case R.id.rl_gender:
                ActionSheet menuView = new ActionSheet(this);
                menuView.setCancelButtonTitle("取消");
                menuView.addItems("男", "女", "其他");
                menuView.setItemClickListener(this);
                menuView.setCancelableOnTouchMenuOutside(true);
                menuView.showMenu();
                break;
            case R.id.rl_birthday:
                startActivityForResult(new Intent(this,SelectDateActivity.class),BIRTHDAY_REQUEST_CODE);
                break;
        }
    }


    @Override
    public void onItemClick(int pos) {
        switch (pos) {
            case 0:
                etGender.setText("男");
                break;
            case 1:
                etGender.setText("女");
                break;
            case 2:
                etGender.setText("其他");
                break;
        }
        PersonInfo personInfo = DBHandler.getCurrPesonInfo();
        if (null != personInfo) {
            personInfo.setSex(etGender.getText().toString().trim());
        }
    }

}
