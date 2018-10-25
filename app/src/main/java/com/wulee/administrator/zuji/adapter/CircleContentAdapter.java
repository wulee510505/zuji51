package com.wulee.administrator.zuji.adapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.facebook.stetho.common.LogUtil;
import com.jaeger.ninegridimageview.GridImageView;
import com.jaeger.ninegridimageview.NineGridImageView;
import com.jaeger.ninegridimageview.NineGridImageViewAdapter;
import com.wulee.administrator.zuji.R;
import com.wulee.administrator.zuji.database.bean.PersonInfo;
import com.wulee.administrator.zuji.entity.CircleComment;
import com.wulee.administrator.zuji.entity.CircleContent;
import com.wulee.administrator.zuji.ui.BigMultiImgActivity;
import com.wulee.administrator.zuji.ui.PersonalInfoActivity;
import com.wulee.administrator.zuji.ui.PublishCircleActivity;
import com.wulee.administrator.zuji.ui.UserInfoActivity;
import com.wulee.administrator.zuji.utils.DateTimeUtils;
import com.wulee.administrator.zuji.utils.ImageUtil;
import com.wulee.administrator.zuji.utils.NoFastClickUtils;
import com.wulee.administrator.zuji.utils.OtherUtil;
import com.wulee.administrator.zuji.utils.PhoneUtil;
import com.wulee.administrator.zuji.widget.CircleImageView;
import com.wulee.administrator.zuji.widget.CommonPopupWindow;
import com.wulee.administrator.zuji.widget.ExpandableTextView;
import com.wulee.administrator.zuji.widget.NoScrollListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;


public class CircleContentAdapter extends BaseMultiItemQuickAdapter<CircleContent, BaseViewHolder> {

    private Context mcontext;
    private PersonInfo piInfo;
    private HashMap<String,Integer> likeNumMap = new HashMap<>();
    private HashMap<Integer,CommonPopupWindow> mapPopwindow = new HashMap<>();

    protected boolean isScrolling = false;

    public CircleContentAdapter(ArrayList<CircleContent> dataList, Context context) {
        super(dataList);
        this.mcontext = context;
        addItemType(CircleContent.TYPE_TEXT_AND_IMG, R.layout.circle_content_text_and_img_item);
        addItemType(CircleContent.TYPE_ONLY_TEXT, R.layout.circle_content_only_text_item);
        piInfo = BmobUser.getCurrentUser(PersonInfo.class);
    }

    public void setScrolling(boolean scrolling) {
        isScrolling = scrolling;
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, final CircleContent circleContent) {
        CircleImageView ivAvatar = baseViewHolder.getView(R.id.userAvatar);
        if (circleContent.personInfo != null && !TextUtils.isEmpty(circleContent.personInfo.getHeader_img_url())) {
            ImageUtil.setDefaultImageView(ivAvatar, circleContent.personInfo.getHeader_img_url(), R.mipmap.icon_user_def_colorized, mcontext);
        } else {
            ImageUtil.setDefaultImageView(ivAvatar, "", R.mipmap.icon_user_def_colorized, mcontext);
        }

        ivAvatar.setOnClickListener(view -> {
            if (NoFastClickUtils.isFastClick()) {
                return;
            }
            if (null != piInfo) {
                Intent intent = null;
                if (TextUtils.equals(piInfo.getUsername(), circleContent.personInfo.getUsername())) {
                    intent = new Intent(mcontext, PersonalInfoActivity.class);
                } else {
                    intent = new Intent(mcontext, UserInfoActivity.class);
                    intent.putExtra("piInfo", circleContent.personInfo);
                }
                mcontext.startActivity(intent);
            }
        });

        String userName = circleContent.personInfo.getName();
        String encryptTelNum = PhoneUtil.encryptTelNum(circleContent.personInfo.getUsername());
        baseViewHolder.setText(R.id.userNick, !TextUtils.isEmpty(userName)?userName:encryptTelNum);
        ExpandableTextView tvContent = baseViewHolder.getView(R.id.circle_content);
        tvContent.setContent(circleContent.getContent());
        tvContent.setLinkClickListener((linkType, content) -> {
            //根据类型去判断
            if (linkType.equals(ExpandableTextView.LinkType.LINK_TYPE)) {
                LogUtil.d("你点击了链接 内容是：" + content);

                Uri uri = Uri.parse(content);
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                intent.setData(uri);
                mcontext.startActivity(intent);
            } else if (linkType.equals(ExpandableTextView.LinkType.MENTION_TYPE)) {
                LogUtil.d("你点击了@用户 内容是：" + content);
            }
        });
        TextView tvCopy = baseViewHolder.getView(R.id.tv_copy);
        tvCopy.setOnClickListener(v -> {
            OtherUtil.copy(circleContent.getContent(),mContext);
            OtherUtil.showToastText("已复制到剪切板，快去粘贴吧~");
        });

        TextView tvLocation = baseViewHolder.getView(R.id.location);
        if (!TextUtils.isEmpty(circleContent.getLocation())) {
            tvLocation.setVisibility(View.VISIBLE);
            tvLocation.setText(circleContent.getLocation());
        } else {
            tvLocation.setVisibility(View.GONE);
        }
        baseViewHolder.setText(R.id.time, DateTimeUtils.showDifferenceTime(DateTimeUtils.parseDateTime(circleContent.getCreatedAt()), System.currentTimeMillis()) + "前");

        TextView tvDel = baseViewHolder.getView(R.id.tv_delete);

        if (null != piInfo) {
            if (TextUtils.equals(piInfo.getUsername(), circleContent.personInfo.getUsername())) {
                tvDel.setVisibility(View.VISIBLE);
            } else {
                tvDel.setVisibility(View.GONE);
            }
        }
        final int pos = baseViewHolder.getAdapterPosition();
        tvDel.setOnClickListener(view -> {
            if (mListener != null) {
                //因为有headerview
                mListener.onDelBtnClick(pos - 1);
            }
        });

        TextView tvLikesNum = baseViewHolder.getView(R.id.tv_likes_num);
        int likeNum = circleContent.getLikeNum();
        if(null != tvLikesNum){
            if(likeNum >0 ){
                tvLikesNum.setText(likeNum+"");
                tvLikesNum.setVisibility(View.VISIBLE);
            }else{
                tvLikesNum.setVisibility(View.GONE);
            }
        }

        ImageView ivOpt = baseViewHolder.getView(R.id.album_opt);
        ivOpt.setOnClickListener(view -> {
            CommonPopupWindow popupWindow;
            if(mapPopwindow.containsKey(pos)){
                popupWindow = mapPopwindow.get(pos);
            }else{
                popupWindow = new CommonPopupWindow.Builder(mcontext)
                        //设置PopupWindow布局
                        .setView(R.layout.circle_opt_pop_layout)
                        //设置宽高
                        .setWidthAndHeight(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT)
                        //设置背景颜色，取值范围0.0f-1.0f 值越小越暗 1.0f为透明
                        .setBackGroundLevel(0.5f)
                        //设置PopupWindow里的子View及点击事件
                        .setViewOnclickListener((view1, layoutResId) -> {
                            RelativeLayout rlLike =  view1.findViewById(R.id.toolbarLike);
                            RelativeLayout rlComment = view1.findViewById(R.id.toolbarComment);
                            rlLike.setOnClickListener(view2 -> addLikes(pos,circleContent.getObjectId(),circleContent.getItemType(),likeNum,tvLikesNum));

                            rlComment.setOnClickListener(view2 -> showComentDialog(circleContent));
                        })
                        //设置外部是否可点击 默认是true
                        .setOutsideTouchable(true)
                        //开始构建
                        .create();
                mapPopwindow.put(pos,popupWindow);
            }
            //弹出PopupWindow
            popupWindow.showAsDropDown(view, -view.getWidth(), -view.getHeight());
        });

        TextView tvLikes = baseViewHolder.getView(R.id.tv_likes);
        StringBuilder sbLikes = new StringBuilder();
        List<PersonInfo> likePiList = circleContent.getLikeList();
        if (likePiList != null && likePiList.size() > 0) {
            tvLikes.setVisibility(View.VISIBLE);
            for (int i = 0; i < likePiList.size(); i++) {
                PersonInfo pi = likePiList.get(i);
                if (null != pi) {
                    sbLikes.append(pi.getName()).append("，");
                }
            }
            String str = sbLikes.toString();
            if (str.length() > 0) {
                tvLikes.setText(str.substring(0, str.length() - 1));
            }
        } else {
            tvLikes.setVisibility(View.GONE);
        }


        NoScrollListView lvComment = baseViewHolder.getView(R.id.lv_comment);
        ArrayList<String> name;
        ArrayList<String> toName;
        ArrayList<String> comment;

        List<CircleComment> commentList = circleContent.getCommentList();
        if (commentList != null && commentList.size() > 0 && !isScrolling) {
            name = new ArrayList<>();
            toName = new ArrayList<>();
            comment = new ArrayList<>();
            lvComment.setVisibility(View.VISIBLE);
            for (int i = 0; i < commentList.size(); i++) {
                CircleComment com = commentList.get(i);
                if (com.getPersonInfo() != null) {
                    name.add(com.getPersonInfo().getName());
                }
                if (com.getCircleContent().personInfo != null) {
                    toName.add(com.getCircleContent().personInfo.getName());
                }
                comment.add(com.getContent());
            }
            CircleCommentAdapter commentAdapter = new CircleCommentAdapter(circleContent, name, toName, comment, mContext);
            lvComment.setAdapter(commentAdapter);
        } else {
            lvComment.setVisibility(View.GONE);
        }

        switch (baseViewHolder.getItemViewType()) {
            case CircleContent.TYPE_TEXT_AND_IMG:
                NineGridImageViewAdapter<CircleContent.CircleImageBean> mAdapter = new NineGridImageViewAdapter<CircleContent.CircleImageBean>() {
                    @Override
                    protected void onDisplayImage(Context context, ImageView imageView, CircleContent.CircleImageBean img) {
                        ImageUtil.setDefaultImageView(imageView,img.getUrl(),R.mipmap.bg_pic_def_rect,context);
                    }
                    @Override
                    protected ImageView generateImageView(Context context) {
                        GridImageView imageView = new GridImageView(context);
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        return imageView;
                    }

                    @Override
                    protected void onItemImageClick(Context context, ImageView imageView, int index, List<CircleContent.CircleImageBean> photoList) {

                    }
                };
                NineGridImageView nineGridImageView = baseViewHolder.getView(R.id.nine_grid_view);
                nineGridImageView.setAdapter(mAdapter);
                nineGridImageView.setImagesData(circleContent.getImageList());
                nineGridImageView.setItemImageClickListener((context, imageView, index, imgList) -> {

                    if (imgList != null && imgList.size() > 0) {
                        Intent intent = new Intent(context, BigMultiImgActivity.class);
                        intent.putExtra(BigMultiImgActivity.IMAGES_URL, circleContent.getImgUrls());
                        intent.putExtra(BigMultiImgActivity.IMAGE_INDEX, index);
                        context.startActivity(intent);
                    }

                });
                break;
            case CircleContent.TYPE_ONLY_TEXT:
                //do nothing
                break;
            default:
                break;
        }
    }


    /**
     * 点赞
     */
    private  void addLikes(int postion,final String objId,int itemType, int likeNum,final TextView tv){
        if (NoFastClickUtils.isFastClick()) {
            Toast.makeText(mContext, "点击过快", Toast.LENGTH_SHORT).show();
            return;
        }
        CircleContent circleContent = new CircleContent(itemType);
        circleContent.setObjectId(objId);
        int num = ++likeNum;
        if(likeNumMap.containsKey(objId)){
            num = likeNumMap.get(objId);
            num ++ ;
            likeNumMap.remove(objId);
        }
        circleContent.setLikeNum(num);
        likeNumMap.put(objId,num);
        circleContent.update(new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if(e == null){
                    mcontext.sendBroadcast(new Intent(PublishCircleActivity.ACTION_PUBLISH_CIRCLE_OK));
                    tv.setText(likeNumMap.get(objId)+"");
                    OtherUtil.showToastText("点赞成功");
                    if(mapPopwindow.get(postion) != null && mapPopwindow.get(postion).isShowing())
                        mapPopwindow.get(postion).dismiss();
                }else{
                    OtherUtil.showToastText("点赞失败");
                }
            }
        });
    }

    /**
     * 评论Dialog
     */
    private void showComentDialog(final CircleContent content) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mcontext);
        builder.setTitle("评论");
        View dialogView = LayoutInflater.from(mcontext).inflate(R.layout.circle_comment_dialog, null);
        final EditText etComment = dialogView.findViewById(R.id.et_comment);


        builder.setView(dialogView);
        builder.setPositiveButton("确定", (dialog, which) -> {
            final CircleComment comment = new CircleComment();
            comment.setContent(etComment.getText().toString().trim());
            comment.setCircleContent(content);
            comment.setPersonInfo(piInfo);
            comment.save(new SaveListener<String>() {
                @Override
                public void done(String objectId, BmobException e) {
                    if (e == null) {
                        mcontext.sendBroadcast(new Intent(PublishCircleActivity.ACTION_PUBLISH_CIRCLE_OK));
                        LogUtil.i("zuji", "评论发表成功");
                    } else {
                        OtherUtil.showToastText("评论失败" + e.getMessage());
                    }
                }
            });
        });
        builder.setNegativeButton("取消", null);
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }



    public void setDelBtnClickListenerListener(OnDelBtnClickListener mListener) {
        this.mListener = mListener;
    }

    private OnDelBtnClickListener mListener;

    public interface OnDelBtnClickListener {
        void onDelBtnClick(int postion);
    }
}
