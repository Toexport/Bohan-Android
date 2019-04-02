package cn.mioto.bohan.activity;

import cn.mioto.bohan.R;
import cn.mioto.bohan.utils.ViewUtil;

import com.umeng.analytics.MobclickAgent;

import android.os.Bundle;

/** 
* 类说明：设备离线提醒页。网页
* 作者：  jiemai liangminhua 
* 创建时间：2016年6月28日 上午9:36:59 
*/
public class OffLineDeviceRemindActivity extends BaseUDPActivity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewUtil.initToolbar(this, R.id.toolbar, R.id.toolbar_title, R.string.time_setting, true, false, 0, 0);
		ViewUtil.setToolbarTitle(getResources().getString(R.string.device_info));
	}
	
	@Override
	protected Integer getLayoutId() {
		return R.layout.activity_off_line_device_remind;
	}
	
	public void onResume() {
	    super.onResume();
	    MobclickAgent.onPageStart("离线设备提醒页"); //统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
	    MobclickAgent.onResume(this);          //统计时长
	}
	public void onPause() {
	    super.onPause();
	    MobclickAgent.onPageEnd("离线设备提醒页"); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息。"SplashScreen"为页面名称，可自定义
	    MobclickAgent.onPause(this);
	}
	
}
