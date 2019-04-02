package cn.mioto.bohan.activity;

import cn.mioto.bohan.Constant;
import cn.mioto.bohan.R;
import cn.mioto.bohan.utils.Enterface;
import cn.mioto.bohan.utils.LogUtilNIU;
import cn.mioto.bohan.utils.ToastUtils;
import cn.mioto.bohan.utils.ViewUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.VideoView;
import steed.framework.android.client.JsonClientHandler2;

/** 
 * 类说明：扫描分享码后的结果
 */
public class ShareResultActivity extends AppCompatActivity{
	private LinearLayout llSuccess;
	private LinearLayout llFail;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share_result);
		ViewUtil.initToolbar(this, "设备授权结果", true, false, 0);
		bindViews();
		String code=getIntent().getStringExtra(Constant.INTENT_KEY_SHARE_CODE);
		LogUtilNIU.value("发送到服务器的分享码是-->"+code);
		sendShareCodeToService(code);
	}

	private void bindViews() {
		llSuccess = (LinearLayout) findViewById(R.id.llShareSuccess);
		llFail = (LinearLayout) findViewById(R.id.llShareFail);
	}

	private void sendShareCodeToService(String sharID) {
		new Enterface("getDeviceFromShare.act").addParam("shareid", sharID).doRequest(new JsonClientHandler2() {
			@Override
			public void onInterfaceSuccess(String message, String contentJson) {
				LogUtilNIU.value("发送分享码后返回信息---->"+contentJson);
				llSuccess.setVisibility(View.VISIBLE);
			}

			@Override
			public void onInterfaceFail(String json) {
				LogUtilNIU.value("发送分享码后返回错误信息---->"+json);
				llFail.setVisibility(View.VISIBLE);
			}

			@Override
			public void onFailureConnected(Boolean canConnect) {
				LogUtilNIU.value("接口无效");
			}
		},true);
	}

}
