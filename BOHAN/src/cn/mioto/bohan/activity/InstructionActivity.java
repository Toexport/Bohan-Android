package cn.mioto.bohan.activity;

import java.util.Locale;

import cn.mioto.bohan.Constant;
import cn.mioto.bohan.R;
import cn.mioto.bohan.utils.ViewUtil;

import com.umeng.analytics.MobclickAgent;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class InstructionActivity extends SteedAppCompatActivity {
	
	private WebView webView1;
	private boolean isZH;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewUtil.initToolbar(this, R.id.toolbar, R.id.toolbar_title, R.string.title_activity_instruction, true, false, 0, 0);
		webView1 = findView(R.id.webView2);
		// 启用支持javascript
		WebSettings settings = webView1.getSettings();
		settings.setJavaScriptEnabled(true);
		webView1.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		isZH = isZh();
		// webView1.loadUrl("https://www.baidu.com/");
		if(isZH){
			webView1.loadUrl("http://www.bohanserver.top:8088/APPHelp.html");
		}else{
			webView1.loadUrl("http://www.bohanserver.top:8088/APPHelp_en.html");
		}
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
	
	/**
	 * 获取系统语言
	 * 
	 * @return
	 */
	private boolean isZh() {
		Locale locale = getResources().getConfiguration().locale;
		String language = locale.getLanguage();
		if (language.endsWith("zh")) {
			isZH = true;
			return true;
		} else {
			isZH = false;
			return false;
		}

	}
	
}
