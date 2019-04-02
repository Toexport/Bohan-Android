package cn.mioto.bohan.activity;

import cn.mioto.bohan.Constant;
import cn.mioto.bohan.R;

import com.umeng.analytics.MobclickAgent;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * 类说明： BOHAN主页显示 作者： jiemai liangminhua 创建时间：2016年6月2日 上午11:32:27
 */
public class WebActivity extends SteedAppCompatActivity {
	/********** DECLARES *************/
	private WebView webView1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		webView1 = findView(R.id.webView1);
		// 启用支持javascript
		WebSettings settings = webView1.getSettings();
		settings.setJavaScriptEnabled(true);
		webView1.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		// webView1.loadUrl("https://www.baidu.com/");
		webView1.loadUrl(Constant.BOHAN_WEB);
		// webView1.loadUrl("http://blog.csdn.net/developer_jiangqq/article/details/7054762");

	}

	@Override
	protected Integer getLayoutId() {
		return R.layout.activity_web;
	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("伯瀚网页页"); // 统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
		MobclickAgent.onResume(this); // 统计时长
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("伯瀚网页页"); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证
											// onPageEnd 在onPause 之前调用,因为
											// onPause
											// 中会保存信息。"SplashScreen"为页面名称，可自定义
		MobclickAgent.onPause(this);
	}
}
