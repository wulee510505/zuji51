package com.wulee.administrator.zuji.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.stetho.common.LogUtil;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.beta.upgrade.UpgradeStateListener;
import com.wulee.administrator.zuji.R;
import com.wulee.administrator.zuji.base.BaseActivity;
import com.wulee.administrator.zuji.database.DBHandler;
import com.wulee.administrator.zuji.database.bean.PersonInfo;
import com.wulee.administrator.zuji.database.bean.PushMessage;
import com.wulee.administrator.zuji.entity.Constant;
import com.wulee.administrator.zuji.entity.SignInfo;
import com.wulee.administrator.zuji.service.UploadLocationService;
import com.wulee.administrator.zuji.ui.fragment.CircleFragment;
import com.wulee.administrator.zuji.ui.fragment.FriendFragment;
import com.wulee.administrator.zuji.ui.fragment.JokeFragment;
import com.wulee.administrator.zuji.ui.fragment.MainBaseFrag;
import com.wulee.administrator.zuji.ui.fragment.NewsFragment;
import com.wulee.administrator.zuji.ui.fragment.ZujiFragment;
import com.wulee.administrator.zuji.ui.pushmsg.PushMsgListActivity;
import com.wulee.administrator.zuji.utils.AppUtils;
import com.wulee.administrator.zuji.utils.Config;
import com.wulee.administrator.zuji.utils.ConfigKey;
import com.wulee.administrator.zuji.utils.DataCleanManager;
import com.wulee.administrator.zuji.utils.HolidayUtil;
import com.wulee.administrator.zuji.utils.ImageUtil;
import com.wulee.administrator.zuji.utils.LocationUtil;
import com.wulee.administrator.zuji.utils.MarketUtils;
import com.wulee.administrator.zuji.utils.OtherUtil;
import com.wulee.administrator.zuji.utils.PhoneUtil;
import com.wulee.administrator.zuji.widget.BottomNavigationViewEx;
import com.wulee.administrator.zuji.widget.CoolImageView;
import com.yanzhenjie.permission.AndPermission;
import com.zhouwei.blurlibrary.EasyBlur;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.newim.core.ConnectionStatus;
import cn.bmob.newim.listener.ConnectListener;
import cn.bmob.newim.listener.ConnectStatusChangeListener;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.CountListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.update.BmobUpdateAgent;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

import static cn.bmob.v3.BmobUser.getCurrentUser;
import static com.wulee.administrator.zuji.App.aCache;

/**
 * Created by wulee on 2017/9/6 09:52
 */

public class MainNewActivity extends BaseActivity implements  ViewPager.OnPageChangeListener,NavigationView.OnNavigationItemSelectedListener,ZujiFragment.OnMenuBtnClickListener{
    private BottomNavigationViewEx mBnve;
    private ViewPager mViewPager;
    private NavigationView navigationView;
    private View menuHeaderView;
    private ImageView ivHeader;
    private TextView mTvName;
    private TextView mTvMobile;
    private TextView mTvSign;
    private ImageView mIvSign;
    private TextView mTvIntegral;
    private TextView tvNewPushMsg;
    private TextView tvCacheSize;

    private MainFPagerAdaper mainFPagerAdaper;


    private  DrawerLayout mDrawerLayout;

    // 标示了当前位置
    private final int POS_ONE = 0, POS_TWO = 1, POS_THREE = 2, POS_FOUR = 3, POS_FIVE = 4;
    // 是否已经展示过Fragment，默认是false，没有展示过
    private boolean[] hasFragSelected = new boolean[POS_FIVE + 1];
    private MainBaseFrag[] mainBaseFrags = new MainBaseFrag[POS_FIVE + 1];

    private long mLastClickReturnTime = 0l; // 记录上一次点击返回按钮的时间
    private Bitmap finalBitmap;
    private final int INTENT_TO_PUSHMSG_LIST = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_new_activity);

        init();

        int pos = 0;
        try {
            String posStr = aCache.getAsString(ConfigKey.KEY_MAIN_TAB_POS);
            if(!TextUtils.isEmpty(posStr))
                pos = Integer.parseInt(posStr);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        initSelectTab(pos);
        sendFragmentFirstSelectedMsg(pos);

        EventBus.getDefault().register(this);

        long lastShowNoticeTime = 0L;
        try {
            String timeStr = aCache.getAsString(Constant.KEY_LAST_SHOW_NOTICE_TIME);
            if(!TextUtils.isEmpty(timeStr)){
                lastShowNoticeTime = Long.parseLong(timeStr);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        long interal = System.currentTimeMillis() - lastShowNoticeTime;
        if(interal > Constant.SHOW_NOTICE_INTERVAL){
            startActivity(new Intent(this,NoticeActivity.class));
        }

        if(HolidayUtil.isHoliday(new Date())){
            startActivity(new Intent(this,FallingViewActivity.class).putExtra(FallingViewActivity.CURR_HOLIDAYS,HolidayUtil.getCurrHolidays()));
        }else{
            startActivity(new Intent(this,TodayInHistoryActivity.class));
        }

        connectIMServer();
    }




    @Override
    protected void onResume() {
        super.onResume();
        initMenuHeaderInfo();

        long lastUpdateCurrPersonInfoTime = 0L;
        try {
            String timeStr = aCache.getAsString(Constant.KEY_LAST_UPDATE_CURR_PERSONINFO_TIME);
            if(!TextUtils.isEmpty(timeStr)){
                lastUpdateCurrPersonInfoTime = Long.parseLong(timeStr);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        long interal = System.currentTimeMillis() - lastUpdateCurrPersonInfoTime;
        if(interal > Constant.UPDATE_CURR_PERSONINFO_INTERVAL){
            final PersonInfo personInfo = getCurrentUser(PersonInfo.class);
            if(null == personInfo)
                return;
            personInfo.update(new UpdateListener() {
                @Override
                public void done(BmobException e) {
                    if(e == null){
                        aCache.put(Constant.KEY_LAST_UPDATE_CURR_PERSONINFO_TIME,String.valueOf(System.currentTimeMillis()));
                        personInfo.setMobile(DBHandler.getCurrPesonInfo().getMobile());
                        DBHandler.insertPesonInfo(personInfo);
                    }else{
                        if(e.getErrorCode() == 206){
                            OtherUtil.showToastText("您的账号在其他地方登录，请重新登录");
                            Config.get(MainNewActivity.this).remove(ConfigKey.KEY_HAS_LOGIN);
                            LocationUtil.getInstance().stopGetLocation();
                            AppUtils.AppExit(MainNewActivity.this);
                            PersonInfo.logOut();
                            startActivity(new Intent(MainNewActivity.this,LoginActivity.class));
                        }
                    }
                }
            });
        }

        File file =new File(getCacheDir().getPath());
        try {
            tvCacheSize.setText(DataCleanManager.getCacheSize(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
       long lastCheckUpdateTime = 0L;
        try {
            String timeStr = aCache.getAsString(Constant.KEY_LAST_CHECK_UPDATE_TIME);
            if(!TextUtils.isEmpty(timeStr)){
                lastCheckUpdateTime = Long.parseLong(timeStr);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        long interal = System.currentTimeMillis() - lastCheckUpdateTime;
        if(interal > Constant.CHECK_UPDATE_INTERVAL){
            BmobUpdateAgent.setUpdateOnlyWifi(true);
            checkUpdate();
            aCache.put(Constant.KEY_LAST_CHECK_UPDATE_TIME,String.valueOf(System.currentTimeMillis()));
        }
    }

    private void init() {
        initView();
        initData();
        addListner();
    }

    private void checkUpdate(){
        AndPermission
                .with(this)
                .runtime()
                .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .onGranted(permissions -> {
                    BmobUpdateAgent.forceUpdate(MainNewActivity.this);
                })
                .onDenied(permissions -> {

                }).start();
    }


    private void initView() {
        mDrawerLayout = findViewById(R.id.id_drawerLayout);
        mDrawerLayout.setScrimColor(0x80000000);

        mBnve =  findViewById(R.id.bottom_navigation_view);
        mBnve.enableShiftingMode(false);
        mBnve.enableItemShiftingMode(false);

        mViewPager =  findViewById(R.id.mviewpager);
        navigationView = findViewById(R.id.nav_view);

        //自定义menu菜单icon和title颜色
        int[][] states = new int[][]{
                new int[]{ -android.R.attr.state_pressed},
                new int[]{android.R.attr.state_checked}
        };
        final int[] colors = new int[]{              ContextCompat.getColor(this,R.color.ctv_black_2),
                ContextCompat.getColor(this,R.color.colorAccent)
        };
        ColorStateList csl = new ColorStateList(states, colors);

        navigationView.setItemTextColor(csl);
        navigationView.setItemIconTintList(csl);

        menuHeaderView = navigationView.inflateHeaderView(R.layout.nav_menu_header);

        CoolImageView ivBg =  menuHeaderView.findViewById(R.id.iv_menu_header_bg);
        Drawable drawable = ivBg.getBackground();
        BitmapDrawable bd = (BitmapDrawable) drawable;
        Bitmap bm = bd.getBitmap();
        finalBitmap = EasyBlur.with(getApplicationContext())
                .bitmap(bm) //要模糊的图片
                .radius(50)//模糊半径
                .scale(4)//指定模糊前缩小的倍数
                .policy(EasyBlur.BlurPolicy.FAST_BLUR)//使用fastBlur
                .blur();
        ivBg.setImageBitmap(finalBitmap);

        ivHeader = (ImageView) menuHeaderView.findViewById(R.id.circle_img_header);
        mTvName = (TextView) menuHeaderView.findViewById(R.id.tv_name);
        mTvMobile = (TextView) menuHeaderView.findViewById(R.id.tv_mobile);
        mTvSign = (TextView) menuHeaderView.findViewById(R.id.tv_sign);
        mIvSign  = menuHeaderView.findViewById(R.id.iv_sign);
        mTvIntegral= (TextView) menuHeaderView.findViewById(R.id.tv_integral);

        LinearLayout llpushmsg = (LinearLayout) navigationView.getMenu().findItem(R.id.item_pushmsg).getActionView();
        tvNewPushMsg = llpushmsg.findViewById(R.id.tv_new_msg);

        LinearLayout llcache = (LinearLayout) navigationView.getMenu().findItem(R.id.item_clear_cache).getActionView();
        tvCacheSize  = llcache.findViewById(R.id.tv_cache_size);


        final PersonInfo piInfo = BmobUser.getCurrentUser(PersonInfo.class);
        if(piInfo == null)
            return;

        mTvIntegral.setText(piInfo.getIntegral()+"");

        String currDateStr = aCache.getAsString(Constant.KEY_CURR_SERVER_TIME);

        BmobQuery<SignInfo> query1 = new BmobQuery<>();
        query1.addWhereEqualTo("personInfo",piInfo);

        BmobQuery<SignInfo> query2 = new BmobQuery<>();
        query2.addWhereEqualTo("date",currDateStr);

        List<BmobQuery<SignInfo>> andQuerys = new ArrayList<>();
        andQuerys.add(query1);
        andQuerys.add(query2);

        BmobQuery<SignInfo> query = new BmobQuery<>();
        query.and(andQuerys);

        query.count(SignInfo.class, new CountListener() {
            @Override
            public void done(Integer count, BmobException e) {
                if(e == null && count > 0){
                    mTvSign.setTextColor(ContextCompat.getColor(MainNewActivity.this,R.color.baseGrayNor));
                    mTvSign.setText("已签到");
                    mIvSign.setVisibility(View.VISIBLE);
                    mTvSign.setEnabled(false);
                }else{
                    mTvSign.setEnabled(true);
                    mIvSign.setVisibility(View.GONE);
                }
            }
        });

        mTvSign.setOnClickListener(view -> Bmob.getServerTime(new QueryListener<Long>() {
            @Override
            public void done(Long time, BmobException e) {
                if(e == null){
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                    final String date = formatter.format(new Date(time * 1000L));
                    LogUtil.i("bmob","当前服务器时间为:" + date);

                    SignInfo signInfo = new SignInfo();
                    signInfo.hasSign = true;
                    signInfo.date = date;
                    signInfo.personInfo = piInfo;
                    signInfo.save(new SaveListener<String>() {
                        @Override
                        public void done(String s, BmobException e) {
                           if(e == null){

                               final int integral = piInfo.getIntegral();
                               piInfo.setIntegral(integral+1);
                               piInfo.update(new UpdateListener() {
                                   @Override
                                   public void done(BmobException e) {
                                     if(e == null){
                                         aCache.put(Constant.KEY_SIGN_DATE,date);
                                         mTvSign.setTextColor(ContextCompat.getColor(MainNewActivity.this,R.color.baseGrayNor));
                                         mTvSign.setText("已签到");
                                         mTvSign.setEnabled(false);

                                         mTvIntegral.setText(Integer.valueOf(integral+1)+"");

                                         toast("签到成功");
                                     }else{
                                         mTvSign.setEnabled(true);

                                         toast("签到失败");
                                     }
                                   }
                               });

                           }else{
                               toast("签到失败");
                               mTvSign.setEnabled(true);
                           }
                        }
                    });
                }else{
                    LogUtil.i("bmob","获取服务器时间失败:" + e.getMessage());
                }
            }

        }));
    }

    private void initData() {
        mainFPagerAdaper = new MainFPagerAdaper(getSupportFragmentManager());
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(mainFPagerAdaper);

        mHandler.sendEmptyMessageDelayed(MSG_QUERY_MESSAGE_COUNT,1500);
    }

    private void initMenuHeaderInfo() {
        PersonInfo personalInfo = DBHandler.getCurrPesonInfo();
        if(personalInfo == null){
            PersonInfo pi = BmobUser.getCurrentUser(PersonInfo.class);
            if(null != pi){
                personalInfo = pi;
                DBHandler.insertPesonInfo(pi);
            }
        }
        if(null != personalInfo){
            if(!TextUtils.isEmpty(personalInfo.getName()))
                mTvName.setText(personalInfo.getName());
            else
                mTvName.setText("游客");
            mTvMobile.setText(PhoneUtil.encryptTelNum(personalInfo.getMobile()));
            ImageUtil.setCircleImageView(ivHeader,personalInfo.getHeader_img_url(),R.mipmap.icon_user_def,this);
        }
    }

    private void addListner() {
         mBnve.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            private int previousPosition = -1;
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int position = 0;
                switch (item.getItemId()) {
                    case R.id.menu_zuji:
                        position = 0;
                        break;
                    case R.id.menu_circle:
                        position = 1;
                        break;
                    case R.id.menu_group:
                        position = 2;
                        break;
                    case R.id.menu_joke:
                        position = 3;
                        break;
                    case R.id.menu_news:
                        position = 4;
                        break;
                    default:
                        break;
                }
                if (previousPosition != position) {
                    mViewPager.setCurrentItem(position, false);
                    previousPosition = position;
                }
                return true;
            }
        });
        mViewPager.setOnPageChangeListener(this);
        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }
            @Override
            public void onDrawerOpened(View drawerView) {
                mHandler.sendEmptyMessage(MSG_QUERY_MESSAGE_COUNT);
            }
            @Override
            public void onDrawerClosed(View drawerView) {

            }
            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
        navigationView.setNavigationItemSelectedListener(this);
        ivHeader.setOnClickListener(view -> startActivity(new Intent(MainNewActivity.this,PersonalInfoActivity.class)));
    }

    private void initSelectTab(int pos) {
        mViewPager.setCurrentItem(pos, false);
    }


    private void sendFragmentFirstSelectedMsg(int position) {
        if(false == hasFragSelected[position]) {
            hasFragSelected[position] = true;
            int msgWhat = position + 1000; // 消息和位置的规则为相差1000
            FragmentSelectedHandler.sendEmptyMessageDelayed(msgWhat, 10);
        }
    }

    private Handler FragmentSelectedHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int position = msg.what - 1000;
            if(position>=POS_ONE && position<=POS_FIVE) {
                if(mainBaseFrags[position] == null) {
                    this.removeMessages(msg.what);
                    this.sendEmptyMessageDelayed(msg.what, 10);
                } else {
                    mainBaseFrags[position].onFragmentFirstSelected();
                }
            }
            super.handleMessage(msg);
        }
    };



    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        aCache.put(ConfigKey.KEY_MAIN_TAB_POS, position+"");
        sendFragmentFirstSelectedMsg(position);
        mBnve.setCurrentItem(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id ==  R.id.item_loginout) {
            showLogoutDialog();
        }else  if (id ==  R.id.item_pushmsg) {
            startActivityForResult(new Intent(this,PushMsgListActivity.class),INTENT_TO_PUSHMSG_LIST);
        }else  if (id ==  R.id.item_checkupdate) {
            /* 设置更新状态回调接口 */
            Beta.upgradeStateListener = new UpgradeStateListener() {
                /**
                 * @param isManual true:手动检查 false:自动检查
                 */
                @Override
                public void onUpgradeSuccess(boolean isManual) {
                    OtherUtil.showToastText("更新成功");
                }
                @Override
                public void onUpgradeFailed(boolean isManual) {
                    OtherUtil.showToastText("更新失败");
                }

                @Override
                public void onUpgrading(boolean isManual) {
                    OtherUtil.showToastText("正在更新...");
                }
                @Override
                public void onUpgradeNoVersion(boolean isManual) {
                    OtherUtil.showToastText("版本无更新");
                }
                @Override
                public void onDownloadCompleted(boolean b) {
                    OtherUtil.showToastText("下载完成");
                }
            };
            Beta.checkUpgrade();
        }else  if (id ==  R.id.item_feedback) {
            startActivity(new Intent(this,FeedBackActivity.class));
        }else  if (id ==  R.id.item_setting) {
            startActivity(new Intent(this,SettingActivity.class));
        }else  if (id ==  R.id.item_about) {
            startActivity(new Intent(this,AboutMeActivity.class));
        }else  if (id ==  R.id.item_goto_market) {
            PackageManager pm = getPackageManager();
            try {
                PackageInfo pi = pm.getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
                MarketUtils.goToMarket(this,pi.packageName,null);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }else  if (id ==  R.id.item_clear_cache) {
            String cacheSize = tvCacheSize.getText().toString().trim();
            if(TextUtils.equals("0.0KB",cacheSize)){
                return true;
            }
            showProgressDialog(true);
            try {
                DataCleanManager.cleanInternalCache(getApplicationContext());
                mHandler.sendEmptyMessageDelayed( 0x01,1000);
            } catch (Exception e) {
                mHandler.sendEmptyMessageDelayed( 0x02,1000);
            }
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage("确定要退出吗？");
        builder.setPositiveButton("确定", (dialog, which) -> {
            Config.get(MainNewActivity.this).remove(ConfigKey.KEY_HAS_LOGIN);
            LocationUtil.getInstance().stopGetLocation();
            AppUtils.AppExit(MainNewActivity.this);
            PersonInfo.logOut();
            startActivity(new Intent(MainNewActivity.this,LoginActivity.class));
            UploadLocationService.stopService();

            BmobIM.getInstance().disConnect();//断开与IM服务器的连接
        });
        builder.setNegativeButton("取消", null);
        builder.create().show();
    }

    /**
     * 打开左侧Menu的监听事件
     */
    @Override
    public void OpenLeftMenu() {
        mDrawerLayout.openDrawer(Gravity.LEFT);
    }

    /**
     * 页面适配器start
     */
    public class MainFPagerAdaper extends FragmentPagerAdapter {

        public MainFPagerAdaper(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment retFragment = null;
            switch (position) {
                case 0:
                    retFragment = new ZujiFragment();
                    mainBaseFrags[0] = (ZujiFragment) retFragment;
                    break;
                case 1:
                    retFragment = new CircleFragment();
                    mainBaseFrags[1] = (CircleFragment) retFragment;
                    break;
                case 2:
                    retFragment = new FriendFragment();
                    mainBaseFrags[2] = (FriendFragment) retFragment;
                    break;
                case 3:
                    retFragment = new JokeFragment();
                    mainBaseFrags[3] = (JokeFragment) retFragment;
                    break;
                case 4:
                    retFragment = new NewsFragment();
                    mainBaseFrags[4] = (NewsFragment) retFragment;
                    break;
            }
            return retFragment;
        }

        @Override
        public int getCount() {
            return POS_FIVE + 1;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            return super.instantiateItem(container, position);
        }
    }

    /**
     * 关闭Menu
     */
    public boolean CloseMenu() {
        if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            mDrawerLayout.closeDrawers();
            return true;
        }
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                if (CloseMenu())
                    return true;
                if(System.currentTimeMillis() - mLastClickReturnTime > 1000L) {
                    mLastClickReturnTime = System.currentTimeMillis();
                    toast("再按一次退出程序");
                    return true;
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == INTENT_TO_PUSHMSG_LIST){
                tvNewPushMsg.setVisibility(View.GONE);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread) //在ui线程执行
    public void onPushMsgEvent(PushMessage pushMessage) {
        tvNewPushMsg.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CloseMenu();
        mDrawerLayout = null;
        if(!finalBitmap.isRecycled() ){
            finalBitmap.recycle();//回收图片所占的内存
            finalBitmap=null;
            System.gc();  //提醒系统及时回收
        }
        EventBus.getDefault().unregister(this);
    }


    private final int MSG_QUERY_MESSAGE_COUNT = 101;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                 case MSG_QUERY_MESSAGE_COUNT:
                     //queryMessageCount();
                 break;
                case 0x01:
                    stopProgressDialog();
                    tvCacheSize.setText("0.0KB");
                    break;
                case 0x02:
                    stopProgressDialog();
                    break;
            }
            super.handleMessage(msg);
        }
    };


   /* public void queryMessageCount() {
        PersonInfo piInfo = BmobUser.getCurrentUser(PersonInfo.class);
        BmobQuery<MessageInfo> messageQuery = new BmobQuery<>();
        messageQuery.addWhereEqualTo("owner", piInfo);

        messageQuery.count(MessageInfo.class, new CountListener() {
            @Override
            public void done(Integer count, BmobException e) {
                if(e == null){
                    int oldCount = 0;
                    try {
                        String oldCountStr = aCache.getAsString(ConfigKey.KEY_MESSAGE_COUNT);
                        if(!TextUtils.isEmpty(oldCountStr)){
                            oldCount = Integer.parseInt(oldCountStr);
                        }
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    int diffCount = count - oldCount;
                    if(diffCount >0){
                        tvNewMsg.setVisibility(View.VISIBLE);
                        tvNewMsg.setText(diffCount+"");
                        if(!Text2Speech.isSpeeching()){
                            Text2Speech.speech(App.context,"您有新的留言，请注意查看",true);
                        }
                    }else{
                        tvNewMsg.setVisibility(View.GONE);
                    }
                }else{
                    Log.i("bmob","失败："+e.getMessage()+","+e.getErrorCode());
                }
            }
        });
    }*/

    private void connectIMServer() {
        PersonInfo piInfo = BmobUser.getCurrentUser(PersonInfo.class);
        if(null != piInfo){
            if (!TextUtils.isEmpty(piInfo.getObjectId()) &&
                    BmobIM.getInstance().getCurrentStatus().getCode() != ConnectionStatus.CONNECTED.getCode()) {
                BmobIM.connect(piInfo.getObjectId(), new ConnectListener() {
                    @Override
                    public void done(String uid, BmobException e) {
                        if (e == null) {
                            //连接成功
                            LogUtil.i("连接成功");
                            BmobIM.getInstance().updateUserInfo(new BmobIMUserInfo(piInfo.getObjectId(),
                                    piInfo.getUsername(), piInfo.getHeader_img_url()));
                        } else {
                            //连接失败
                            LogUtil.i("连接失败-----》"+ e.getMessage());
                        }
                    }
                });
            }
        }
        BmobIM.getInstance().setOnConnectStatusChangeListener(new ConnectStatusChangeListener() {
            @Override
            public void onChange(ConnectionStatus status) {
                LogUtil.i(BmobIM.getInstance().getCurrentStatus().getMsg());
            }
        });
    }
}
