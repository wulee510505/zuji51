package com.wulee.administrator.zuji.base;


import android.app.Activity;
import android.support.v4.app.Fragment;

import com.wulee.administrator.zuji.widget.BaseProgressDialog;


/**
 * Created by wulee on 2016/12/15
 */

public abstract class BaseFragment extends Fragment {

    private BaseProgressDialog mProgressDialog = null;
    /**
     * @author wulee
     */

    public void showProgressDialog(Activity activity, BaseProgressDialog.OnCancelListener cancelListener, boolean cancelable) {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            return;
        }
        mProgressDialog = new BaseProgressDialog(activity);
        if (cancelListener != null) {
            mProgressDialog.setOnCancelListener(cancelListener);
        }
        mProgressDialog.setCancelable(cancelable);
        mProgressDialog.show();
    }

    public void showProgressDialog(Activity activity, boolean cancelable) {
        showProgressDialog(activity,null, cancelable);
    }

    public void stopProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.stop();
        }
        mProgressDialog = null;
    }
}
