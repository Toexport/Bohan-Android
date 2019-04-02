package cn.mioto.bohan.activity;

import cn.mioto.bohan.R;
import cn.mioto.bohan.utils.ViewUtil;

import com.umeng.analytics.MobclickAgent;

import android.os.Bundle;

public class InstructionActivity extends SteedAppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewUtil.initToolbar(this, R.id.toolbar, R.id.toolbar_title, R.string.title_activity_instruction, true, false, 0, 0);
	}
	
	@Override
	protected Integer getLayoutId() {
		return R.layout.activity_instruction;
	}
	
	public void onResume() {
	    super.onResume();
	    MobclickAgent.onPageStart("操作指南Activity"); //统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
	    MobclickAgent.onResume(this);          //统计时长
	}
	public void onPause() {
	    super.onPause();
	    MobclickAgent.onPageEnd("操作指南Activity"); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息。"SplashScreen"为页面名称，可自定义
	    MobclickAgent.onPause(this);
	}
	
}
