package cn.mioto.bohan.activity;

import cn.mioto.bohan.R;

import com.umeng.analytics.MobclickAgent;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;

/** 
 * 类说明：欢迎页面
 */
public class SplashActivity extends AppCompatActivity {
	private Handler mHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View rootView = LayoutInflater.from(this).inflate(R.layout.activity_welcome, null);
		setContentView(rootView);
		mHandler = new Handler();
		//		AlphaAnimation aa = new AlphaAnimation(0.0f, 1.0f);//创建一个AlphaAnimation 对象，渐变从1->0
		//		aa.setDuration(4000);//设置持续时间
		//		aa.setFillAfter(true);//设置最后的动画效果，这里是显示状态
		//		rootView.startAnimation(aa);
		//程序启动的时候，查询位置列表，品牌列表，类型列表，然后保存到sp
		new Handler().postDelayed(r, 3000);
		//		接入友盟统计
		MobclickAgent.openActivityDurationTrack(false);
		/** 设置是否对日志信息进行加密, 默认false(不加密). */
		MobclickAgent.enableEncrypt(true);//6.0.0版本及以后
		
	}

	Runnable r = new Runnable() {
		@Override
		public void run() {
			Intent intent = new Intent();
			intent.setClass(SplashActivity.this, LoginActivity.class);
			startActivity(intent);
			finish();
		}
	};

	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("欢迎页"); //统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
		MobclickAgent.onResume(this);          //统计时长
	}
	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("欢迎页"); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息。"SplashScreen"为页面名称，可自定义
		MobclickAgent.onPause(this);
	}
}
