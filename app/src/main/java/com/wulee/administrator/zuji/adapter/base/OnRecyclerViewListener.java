package com.wulee.administrator.zuji.adapter.base;

/**为RecycleView添加点击事件
 */
public interface OnRecyclerViewListener {
    void onItemClick(int position);
    boolean onItemLongClick(int position);
}
