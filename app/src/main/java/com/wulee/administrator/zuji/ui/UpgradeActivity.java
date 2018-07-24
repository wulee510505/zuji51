package com.wulee.administrator.zuji.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.beta.UpgradeInfo;
import com.tencent.bugly.beta.download.DownloadListener;
import com.tencent.bugly.beta.download.DownloadTask;
import com.wulee.administrator.zuji.R;
import com.wulee.administrator.zuji.utils.DateTimeUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * 自定义Activity.
 */
public class UpgradeActivity extends Activity {
    @InjectView(R.id.tv_title)
    TextView tvTitle;
    @InjectView(R.id.tv_version)
    TextView tvVersion;
    @InjectView(R.id.tv_time)
    TextView tvTime;
    @InjectView(R.id.tv_content)
    TextView tvContent;
    @InjectView(R.id.btn_cancel)
    Button btnCancel;
    @InjectView(R.id.btn_start)
    Button btnStart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_upgrade);
        ButterKnife.inject(this);

        updateBtn(Beta.getStrategyTask());

        initView();
        addListener();
    }

    private void initView() {
        UpgradeInfo upgradeInfo = Beta.getUpgradeInfo();
        if(null != upgradeInfo){
            System.out.print(upgradeInfo.toString());

            tvVersion.setText(tvVersion.getText().toString() + upgradeInfo.versionName);
            tvTime.setText(tvTime.getText().toString() + DateTimeUtils.getStringDateTimeNew(upgradeInfo.publishTime));
            tvContent.setText(upgradeInfo.newFeature);
        }
    }

    private void addListener() {
        Beta.registerDownloadListener(new DownloadListener() {
            @Override
            public void onReceive(DownloadTask task) {
                updateBtn(task);
            }

            @Override
            public void onCompleted(DownloadTask task) {
                updateBtn(task);
            }

            @Override
            public void onFailed(DownloadTask task, int code, String extMsg) {
                updateBtn(task);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Beta.unregisterDownloadListener();
    }


    public void updateBtn(DownloadTask task) {
        switch (task.getStatus()) {
            case DownloadTask.INIT:
            case DownloadTask.DELETED:
            case DownloadTask.FAILED: {
                btnStart.setText("开始下载");
            }
            break;
            case DownloadTask.COMPLETE: {
                btnStart.setText("安装");
            }
            break;
            case DownloadTask.DOWNLOADING: {
                btnStart.setText("暂停");
            }
            break;
            case DownloadTask.PAUSED: {
                btnStart.setText("继续下载");
            }
            break;
            default:
                break;
        }
    }

    public <T extends View> T getView(int id) {
        return (T) findViewById(id);
    }

    @OnClick({R.id.btn_cancel, R.id.btn_start})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_cancel:
                Beta.cancelDownload();
                finish();
                break;
            case R.id.btn_start:
                DownloadTask task = Beta.startDownload();
                updateBtn(task);
                if (task.getStatus() == DownloadTask.DOWNLOADING) {
                    finish();
                }
                break;
        }
    }
}
