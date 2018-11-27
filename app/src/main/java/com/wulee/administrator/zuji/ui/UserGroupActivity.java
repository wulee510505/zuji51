package com.wulee.administrator.zuji.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.stetho.common.LogUtil;
import com.wulee.administrator.zuji.R;
import com.wulee.administrator.zuji.adapter.UserGroupAdapter;
import com.wulee.administrator.zuji.base.BaseActivity;
import com.wulee.administrator.zuji.database.bean.PersonInfo;
import com.wulee.administrator.zuji.entity.UserGroupItem;
import com.wulee.administrator.zuji.utils.OtherUtil;
import com.wulee.administrator.zuji.widget.BaseTitleLayout;
import com.wulee.administrator.zuji.widget.RecycleViewDivider;
import com.wulee.administrator.zuji.widget.TitleLayoutClickListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

/**
 * Created by wulee on 2017/12/6 09:52
 */
public class UserGroupActivity extends BaseActivity {

    @InjectView(R.id.titlelayout)
    BaseTitleLayout titlelayout;
    @InjectView(R.id.recyclerview)
    RecyclerView recyclerview;
    @InjectView(R.id.swipeLayout)
    SwipeRefreshLayout swipeLayout;
    @InjectView(R.id.et_search)
    EditText etSearch;
    @InjectView(R.id.tv_search)
    TextView tvSearch;
    private Context mContext;

    private static final int STATE_REFRESH = 0;// 下拉刷新
    private static final int STATE_MORE = 1;// 加载更多
    private int PAGE_SIZE = 10;
    private int curPage = 0;
    private boolean isRefresh = false;

    private UserGroupAdapter mAdapter;
    private PersonInfo currPiInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_group_list_main);
        mContext = this;
        ButterKnife.inject(this);

        currPiInfo = BmobUser.getCurrentUser(PersonInfo.class);

        initView();
        addListener();

        showProgressDialog(true);
        getUserList(0, STATE_REFRESH);
    }

    private void initView() {
        ImageView topHeaderIv = findViewById(R.id.ivstatebar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            topHeaderIv.setVisibility(View.VISIBLE);
        } else {
            topHeaderIv.setVisibility(View.GONE);
        }
        swipeLayout.setColorSchemeResources(R.color.left_menu_bg, R.color.colorAccent);

        mAdapter = new UserGroupAdapter(mContext ,null);
        recyclerview.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerview.addItemDecoration(new RecycleViewDivider(mContext, LinearLayoutManager.HORIZONTAL, 1, ContextCompat.getColor(mContext, R.color.grayline)));
        recyclerview.setAdapter(mAdapter);

    }


    private void addListener() {
        mAdapter.setOnItemClickListener((adapter, view, position) -> {
            List<UserGroupItem> piInfoList = mAdapter.getData();
            if (null != piInfoList && piInfoList.size() > 0) {
                UserGroupItem groupItem = piInfoList.get(position);
                if (groupItem != null && groupItem.mItemType == UserGroupItem.ITEM) {
                    Intent intent = null;
                    if (null != groupItem.mPersonInfo) {
                        if(currPiInfo != null && TextUtils.equals(currPiInfo.getObjectId(),groupItem.mPersonInfo.getObjectId())){
                            intent = new Intent(mContext, PersonalInfoActivity.class);
                        }else{
                            intent = new Intent(mContext, UserInfoActivity.class);
                            intent.putExtra("piInfo",groupItem.mPersonInfo);
                        }
                        startActivity(intent);
                    }
                }
            }
        });
        swipeLayout.setOnRefreshListener(() -> {
            String keyWords = etSearch.getText().toString().trim();
            isRefresh = true;
            if(TextUtils.isEmpty(keyWords)){
                curPage = 0;
                getUserList(curPage, STATE_REFRESH);
            }else{
                serachUserByKeyWorlds(keyWords);
            }
        });
        //加载更多
        mAdapter.setEnableLoadMore(true);
        mAdapter.setOnLoadMoreListener(() -> getUserList(curPage, STATE_MORE),recyclerview);
        titlelayout.setOnTitleClickListener(new TitleLayoutClickListener() {
            @Override
            public void onLeftClickListener() {
               finish();
            }
            @Override
            public void onRightImg1ClickListener() {
                startActivity(new Intent(mContext, NearUserActivity.class));
            }
        });
    }


    /**
     * 分页获取数据
     */
    private void getUserList(final int page, final int actionType) {
        BmobQuery<PersonInfo> query = new BmobQuery<>();
        query.order("-createdAt");
        // 如果是加载更多
        if (actionType == STATE_MORE) {
            // 跳过之前页数并去掉重复数据
            query.setSkip(page * PAGE_SIZE);
        } else {
            query.setSkip(0);
        }
        // 设置每页数据个数
        query.setLimit(PAGE_SIZE);
        query.findObjects(new FindListener<PersonInfo>() {
            @Override
            public void done(List<PersonInfo> dataList, BmobException e) {
                stopProgressDialog();
                if (swipeLayout != null && swipeLayout.isRefreshing()) {
                    swipeLayout.setRefreshing(false);
                }
                if (e == null) {
                    curPage++;
                    ArrayList<UserGroupItem> list = null;
                    if (dataList.size() > 0) {
                        list = UserGroupItem.getData(dataList);
                    }
                    if (isRefresh) {//下拉刷新需清理缓存
                        mAdapter.setNewData(list);
                        isRefresh = false;
                    } else {//正常请求 或 上拉加载更多时处理流程
                        if (dataList.size() > 0) {
                            mAdapter.addData(list);
                        }
                    }
                    if (dataList.size() < PAGE_SIZE) {
                        //第一页如果不够一页就不显示没有更多数据布局
                        mAdapter.loadMoreEnd(true);
                    } else {
                        mAdapter.loadMoreComplete();
                    }
                    if (mAdapter.getData().size() == 0) {
                        mAdapter.setEmptyView(LayoutInflater.from(UserGroupActivity.this).inflate(R.layout.com_view_empty, (ViewGroup) recyclerview.getParent(), false));
                    }
                } else {
                    LogUtil.d("查询UserInfo失败" + e.getMessage() + "," + e.getErrorCode());
                }
            }
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
    }


    @OnClick(R.id.tv_search)
    public void onViewClicked() {
        String keyWords = etSearch.getText().toString().trim();
        if(TextUtils.isEmpty(keyWords)){
            OtherUtil.showToastText("请输入关键字搜索");
            return;
        }
        serachUserByKeyWorlds(keyWords);
    }

    private void serachUserByKeyWorlds(String keyWords) {
        BmobQuery<PersonInfo> query1 = new BmobQuery<>();
        query1.addWhereEqualTo("username",keyWords);
        BmobQuery<PersonInfo> query2 = new BmobQuery<>();
        query2.addWhereContains("name",keyWords);
        List<BmobQuery<PersonInfo>> queries = new ArrayList<>();
        queries.add(query1);
        queries.add(query2);
        BmobQuery<PersonInfo> mainQuery = new BmobQuery<>();
        mainQuery.or(queries);
        if(!isRefresh)
           showProgressDialog(true);
        mainQuery.findObjects(new FindListener<PersonInfo>() {
            @Override
            public void done(List<PersonInfo> dataList, BmobException e) {
                stopProgressDialog();
                if (swipeLayout != null && swipeLayout.isRefreshing()) {
                    swipeLayout.setRefreshing(false);
                }
                if(e == null){
                    if (dataList!=null && dataList.size() > 0) {
                        ArrayList<UserGroupItem>  list = UserGroupItem.getData(dataList);
                        mAdapter.setNewData(list);
                    }
                    mAdapter.loadMoreEnd(true);
                }else{
                    OtherUtil.showToastText(e.getMessage());
                    Log.i("bmob","失败："+e.getMessage()+","+e.getErrorCode());
                }
            }
        });
    }

}
