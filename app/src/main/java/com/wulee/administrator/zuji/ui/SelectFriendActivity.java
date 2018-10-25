package com.wulee.administrator.zuji.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.facebook.stetho.common.LogUtil;
import com.wulee.administrator.zuji.R;
import com.wulee.administrator.zuji.adapter.FriendAdapter;
import com.wulee.administrator.zuji.base.BaseActivity;
import com.wulee.administrator.zuji.chatui.enity.Friend;
import com.wulee.administrator.zuji.database.bean.PersonInfo;
import com.wulee.administrator.zuji.utils.OtherUtil;
import com.wulee.administrator.zuji.widget.BaseTitleLayout;
import com.wulee.administrator.zuji.widget.RecycleViewDivider;
import com.wulee.administrator.zuji.widget.TitleLayoutClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;

/**
 * create by  wulee   2018/10/16 14:18
 * desc:
 */
public class SelectFriendActivity extends BaseActivity {

    @InjectView(R.id.titlelayout)
    BaseTitleLayout titlelayout;
    @InjectView(R.id.recyclerview)
    RecyclerView recyclerview;
    @InjectView(R.id.swipeLayout)
    SwipeRefreshLayout swipeLayout;


    private static final int STATE_REFRESH = 0;// 下拉刷新
    private static final int STATE_MORE = 1;// 加载更多
    private int PAGE_SIZE = 10;
    private int curPage = 0;
    private boolean isRefresh = false;

    private FriendAdapter mAdapter;
    private PersonInfo currPiInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_friend);
        ButterKnife.inject(this);

        currPiInfo = BmobUser.getCurrentUser(PersonInfo.class);

        initView();
        addListener();

        getFiendsList(0, STATE_REFRESH);
    }

    private void initView() {
        swipeLayout.setColorSchemeResources(R.color.left_menu_bg, R.color.colorAccent);
        mAdapter = new FriendAdapter(R.layout.friend_list_item, null, this);
        recyclerview.setLayoutManager(new LinearLayoutManager(this));
        recyclerview.addItemDecoration(new RecycleViewDivider(this, LinearLayoutManager.HORIZONTAL, 1, ContextCompat.getColor(this, R.color.grayline)));
        recyclerview.setAdapter(mAdapter);
    }


    private void addListener() {
        titlelayout.setOnTitleClickListener(new TitleLayoutClickListener() {
            @Override
            public void onLeftClickListener() {
                super.onLeftClickListener();
                finish();
            }
        });
        mAdapter.setOnItemClickListener((adapter, view, position) -> {
            List<Friend> piInfoList = mAdapter.getData();
            if (null != piInfoList && piInfoList.size() > 0) {
                Friend friend = piInfoList.get(position);
                PersonInfo personInfo = friend.getFriendUser();
                if (null != personInfo) {
                    if(currPiInfo != null && TextUtils.equals(currPiInfo.getObjectId(),personInfo.getObjectId())){
                        OtherUtil.showToastText("不能选择自己哦@~@");
                        return;
                    }
                    Intent intent = getIntent();
                    intent.putExtra("piInfo",personInfo);
                    setResult(RESULT_OK,intent);
                    finish();
                }
            }
        });
        swipeLayout.setOnRefreshListener(() -> {
            isRefresh = true;
            curPage = 0;
            getFiendsList(curPage, STATE_REFRESH);
        });
        //加载更多
        mAdapter.setEnableLoadMore(true);
        //mAdapter.setPreLoadNumber(PAGE_SIZE);
        mAdapter.setOnLoadMoreListener(() -> getFiendsList(curPage, STATE_MORE));

        titlelayout.setOnTitleClickListener(new TitleLayoutClickListener() {
            @Override
            public void onRightImg1ClickListener() {
                startActivity(new Intent(SelectFriendActivity.this, UserGroupActivity.class));
            }
        });

        recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                // 查看源码可知State有三种状态：SCROLL_STATE_IDLE（静止）、SCROLL_STATE_DRAGGING（上升）、SCROLL_STATE_SETTLING（下落）
                if (newState == SCROLL_STATE_IDLE) { // 滚动静止时才加载数据，极大提升流畅度
                    mAdapter.setScrolling(false);
                    mAdapter.notifyDataSetChanged(); // notify调用后onBindViewHolder会响应调用
                } else
                    mAdapter.setScrolling(true);
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
    }

    /**
     * 分页获取数据
     */
    private void getFiendsList(final int page, final int actionType) {
        PersonInfo user = BmobUser.getCurrentUser(PersonInfo.class);
        BmobQuery<Friend> query = new BmobQuery<>();
        query.addWhereEqualTo("user", user);
        query.include("friendUser");
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
        query.findObjects(new FindListener<Friend>() {
            @Override
            public void done(List<Friend> list, BmobException e) {
                stopProgressDialog();
                if (swipeLayout != null && swipeLayout.isRefreshing()) {
                    swipeLayout.setRefreshing(false);
                }
                if (e == null) {
                    List<Friend> dataList = removeDuplicte(list);
                    curPage++;
                    if (isRefresh) {//下拉刷新需清理缓存
                        mAdapter.setNewData(dataList);
                        isRefresh = false;
                    } else {//正常请求 或 上拉加载更多时处理流程
                        if (dataList.size() > 0) {
                            mAdapter.addData(dataList);
                        }
                    }
                    if (dataList.size() < PAGE_SIZE) {
                        //第一页如果不够一页就不显示没有更多数据布局
                        mAdapter.loadMoreEnd(true);
                    } else {
                        mAdapter.loadMoreComplete();
                    }
                    if (mAdapter.getData().size() == 0) {
                        mAdapter.setEmptyView(LayoutInflater.from(SelectFriendActivity.this).inflate(R.layout.com_view_empty, (ViewGroup) recyclerview.getParent(), false));
                    }
                } else {
                    LogUtil.d("查询UserInfo失败" + e.getMessage() + "," + e.getErrorCode());
                }
            }
        });
    }

    /**
     * 将列表中重复的用户移除，重复指的是ObjectId相同
     * @return
     */
    public static ArrayList<Friend> removeDuplicte(List<Friend> userList) {
        Set<Friend> s = new TreeSet<>((o1, o2) -> o1.getFriendUser().getObjectId().compareTo(o2.getFriendUser().getObjectId()));
        s.addAll(userList);
        return new ArrayList<>(s);
    }
}
