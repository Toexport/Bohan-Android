package cn.mioto.bohan.activity;

import cn.mioto.bohan.R;
import cn.mioto.bohan.utils.ViewUtil;

import com.umeng.analytics.MobclickAgent;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

/** 
 * 类说明：设备信息设置页面
 * 此页面初始化本设备的socket连接
 */
public class MenuDeviceInfoMenuActivity extends BaseUDPActivity{
	/**********布局控件*************/
	private LinearLayout llnowTimeData;
	private LinearLayout llParaSetting;
	private LinearLayout llTimeCount;
	private LinearLayout llDurTime;
	private LinearLayout llBackTime;
	private LinearLayout llPowerSum;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/***************************************************/
		ViewUtil.initToolbar(this, R.id.toolbar, R.id.toolbar_title, R.string.device_setting, true, false, 0, 0);
		ViewUtil.setToolbarTitle(getResources().getString(R.string.device_info));
	}
	
	@Override
	public void onClick(View v) {
		if(v==llnowTimeData){
			Intent intent = new Intent(MenuDeviceInfoMenuActivity.this, MenuNowDataActivity.class);
			startActivity(intent);
		}else if(v==llParaSetting){
			Intent intent = new Intent(MenuDeviceInfoMenuActivity.this, MenuParaSettingActivity.class);
			startActivity(intent);
		}else if(v==llTimeCount){
			Intent intent = new Intent(MenuDeviceInfoMenuActivity.this, MenuSetTimeSetActivity.class);
			startActivity(intent);
		}else if(v==llDurTime){
			Intent intent = new Intent(MenuDeviceInfoMenuActivity.this, Menu9ONOFFActivity.class);
			startActivity(intent);
		}else if(v==llBackTime){
			Intent intent = new Intent(MenuDeviceInfoMenuActivity.this, MenuCountDownActivity.class);
			startActivity(intent);
		}else if(v==llPowerSum){
			Intent intent = new Intent(MenuDeviceInfoMenuActivity.this, MenuSingleDeviceSumActivity.class);
//			Intent intent = new Intent(DeviceInfoMenuActivity.this, TestBrocastActivity.class);
			startActivity(intent);
		}
		super.onClick(v);
	}

	@Override
	protected Integer getLayoutId() {
		return R.layout.activity_menu_device;
	}
	
	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("设备信息菜单"); //统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
		MobclickAgent.onResume(this);          //统计时长
	}
	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("设备信息菜单"); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息。"SplashScreen"为页面名称，可自定义
		MobclickAgent.onPause(this);
	}

}
