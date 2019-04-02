package cn.mioto.bohan.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import steed.framework.android.core.ActivityStack;

/**
 * 类说明： 作者： jiemai liangminhua 联系方式: elecat@126.com, QQ:349635073
 * 创建时间：2016年10月10日 下午1:38:13
 */
public class BaseActivity extends AppCompatActivity {

	// 添加堆栈管理
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActivityStack.pushActivity(this);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		ActivityStack.popActivity(this);
	}
}
