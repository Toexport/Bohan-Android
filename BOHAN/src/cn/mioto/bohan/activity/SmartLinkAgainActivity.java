package cn.mioto.bohan.activity;

import cn.mioto.bohan.R;

import com.umeng.analytics.MobclickAgent;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/** 
* 类说明：重新进行智能连接
* 作者：  jiemai liangminhua 
* 创建时间：2016年7月2日 上午11:19:56 
*/
public class SmartLinkAgainActivity  extends SteedAppCompatActivity {
	/**********DECLARES*************/
	private TextView tvSmarkLink;
	private TextView tvNoLink;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected Integer getLayoutId() {
		return R.layout.activity_smark_link_again;
	}
	
	@Override
	public void onClick(View v) {
		if(v==tvSmarkLink){
			
		}else if(v==tvNoLink){
			
		}
		super.onClick(v);
	}
	
	public void onResume() {
	    super.onResume();
	    MobclickAgent.onPageStart("重新智能连接"); //统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
	    MobclickAgent.onResume(this);          //统计时长
	}
	public void onPause() {
	    super.onPause();
	    MobclickAgent.onPageEnd("重新智能连接"); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息。"SplashScreen"为页面名称，可自定义
	    MobclickAgent.onPause(this);
	}
	
}
