package cn.mioto.bohan.activity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONObject;

import cn.mioto.bohan.BApplication;
import cn.mioto.bohan.Constant;
import cn.mioto.bohan.R;
import cn.mioto.bohan.activity.BaseUDPActivityNoCurrent.CountingThread;
import cn.mioto.bohan.entity.Config;
import cn.mioto.bohan.entity.MD5Utils;
import cn.mioto.bohan.exception.ExceptionManager;
import cn.mioto.bohan.utils.Enterface;
import cn.mioto.bohan.utils.HttpUtils;
import cn.mioto.bohan.utils.LogUtilNIU;
import cn.mioto.bohan.utils.ModbusCalUtil;
import cn.mioto.bohan.utils.NetworkUtils;
import cn.mioto.bohan.utils.ToastUtils;
import cn.mioto.bohan.view.ClearEditText;

import com.mioto.bohan.webservice.WebServiceClient;
import com.umeng.analytics.MobclickAgent;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import steed.framework.android.client.JsonClientHandler2;

/**
 * 类说明：登录页面
 */
public class LoginActivity extends SteedAppCompatActivity {
	private ClearEditText username_editText;
	private EditText pwd_editText;
	private Button login_button;
	// private ProgressButton login_button;
	private ImageView ivSeePassword;
	private TextView tvWebLink;
	Boolean verifySuccess;
	private long currentBackTime;
	private long lastBackTime;
	private String userName;
	private String userPassword;
	int requestCode = 1;
	private boolean mbDisplayFlg = false;// 默认为不显示
	private LinearLayout ll_ForgetPwd;
	private LinearLayout ll_Register;

	private boolean isRequesting = true;
	private ProgressDialog dialog;
	private ClearEditText username_editText_en;
	private boolean isZH;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*** textView设置下划线 *********************************************/
		tvWebLink.setText(Html.fromHtml("<u>" + Constant.BOHAN_WEB + "</u>"));
		/***************************************************/
		// 显示密码和用户名
		SharedPreferences sp = getSharedPreferences(Config.FILE, MODE_APPEND);// 得到全局
		username_editText.setText(sp.getString("username", "")); // 得到值，如果没有就显示空
		username_editText.setSelection(username_editText.getText().toString()
				.trim().length());
		pwd_editText.setText(sp.getString("password", ""));
		isZH = isZh();
		if(isZH){
			//中文
			username_editText.setVisibility(View.VISIBLE);
			username_editText_en.setVisibility(View.GONE);
		}else{
			username_editText.setVisibility(View.GONE);
			username_editText_en.setVisibility(View.VISIBLE);
		}
		
	}

	@Override
	protected Integer getLayoutId() {
		return R.layout.activity_login;
	}

	@Override
	public void onClick(View v) {
		if (v == login_button) {
			if(isZH){
				userName = username_editText.getText().toString();
			}else{
				userName = username_editText_en.getText().toString();
			}
			userPassword = pwd_editText.getText().toString();
			if (userName.equals("") || userName == null) {
				ToastUtils.shortToast(LoginActivity.this, getString(R.string.username_not_null));
			} else if (userPassword.equals("") || userPassword == null) {
				ToastUtils.shortToast(LoginActivity.this, getString(R.string.pwd_not_null));
			} else {
				LogUtilNIU.value("用户名为" + userName + "密码为" + userPassword);
				// login();
				startLogin(userName, userPassword);
			}
		} else if (v == ll_ForgetPwd) {
			// 忘记密码
			Intent intent = new Intent(LoginActivity.this,
					ForgetPwdRegisterActivity.class);
			intent.putExtra("title", getString(R.string.forget_password2));
			startActivity(intent);
		} else if (v == tvWebLink) {
			Intent intent = new Intent(LoginActivity.this, WebActivity.class);
			startActivity(intent);
		} else if (v == ll_Register) {
			// 跳转到注册账号页面
			Intent intent = new Intent(LoginActivity.this,
					ForgetPwdRegisterActivity.class);
			intent.putExtra("title", getString(R.string.register));
			startActivity(intent);
		} else if (v == ivSeePassword) {
			// 控制密码为可见或不可见
			if (!mbDisplayFlg) { // 不可见状态
				pwd_editText
						.setTransformationMethod(HideReturnsTransformationMethod
								.getInstance());
				ivSeePassword.setImageDrawable(getResources().getDrawable(
						R.drawable.login_see));
			} else { // 可见状态
				pwd_editText
						.setTransformationMethod(PasswordTransformationMethod
								.getInstance());
				ivSeePassword.setImageDrawable(getResources().getDrawable(
						R.drawable.login_no_see));
			}
			mbDisplayFlg = !mbDisplayFlg;
			pwd_editText.postInvalidate();
		}
		super.onClick(v);
	}

	/**********************************************************/
	/**
	 * 注册页返回的用户名和密码信息 实现自动填写
	 */
	@Override
	protected void onActivityResult(int reqCode, int resCode, Intent data) {
		if (resCode == Constant.REGISTER_OK_RESULT) {
			username_editText
					.setText(data
							.getStringExtra(Constant.REGISTER_OK_RESULT_USER_NAME_BUNDLE_KEY));
			pwd_editText
					.setText(data
							.getStringExtra(Constant.REGISTER_OK_RESULT_USER_PASSWORD_BUNDLE_KEY));
		}
	}


	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("登录页面"); // 统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
		MobclickAgent.onResume(this); // 统计时长
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("登录页面"); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证
											// onPageEnd 在onPause 之前调用,因为
											// onPause
											// 中会保存信息。"SplashScreen"为页面名称，可自定义
		MobclickAgent.onPause(this);
	}

	private void startLogin(final String username, final String pwd) {
		if (NetworkUtils.isNetworkConnected(this)) {
			getDefaultProgressDialog(getString(R.string.loging), false);
			isRequesting = true;
			new Thread() {
				public void run() {
					Message msg = new Message();
					msg.what = Constant.MSG_FAILURE;
					msg.obj = getString(R.string.cannot_connection_server);
					try {
						Map map = new HashMap<>();
						map.put("userName", username);
						map.put("password", pwd);
						String result = WebServiceClient.CallWebService(
								"Login", map,LoginActivity.this);
						Log.d("resultLogin", result);
						if (result != null) {
							JSONObject obj = new JSONObject(result);
							int statusCode = obj.getInt("statusCode");
							if (statusCode == 0) {
								String token = obj.getString("content");
								writeUserInfo(username, pwd,token);
								msg.what = Constant.MSG_SUCCESS;
							} else {
								msg.obj = obj.getString("message");
								msg.what = Constant.MSG_FAILURE;
							}
						}

					} catch (Exception e) {
						e.printStackTrace();
						msg.obj = e;
						msg.what = Constant.MSG_EXCPTION;
					}
					if (isRequesting) {
						defalutHandler.sendMessage(msg);
					}
				}
			}.start();
		} else {
			defalutHandler.sendEmptyMessage(Constant.MSG_NETWORK_ERROR);
		}
	}

	private Handler defalutHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Constant.MSG_SUCCESS:
				loginSuccess();
				break;
			case Constant.MSG_FAILURE:
				loginFailure();
				toast((String) msg.obj);
				break;
			case Constant.MSG_EXCPTION:
				loginFailure();
				toast(ExceptionManager.getErrorDesc(getApplicationContext(),
						(Exception) msg.obj));
				break;
			case Constant.MSG_TIME_OUT:
				loginFailure();
				toast(getString(R.string.connection_timeout));
				break;
			case Constant.MSG_NETWORK_ERROR:
				loginFailure();
				toast(getString(R.string.no_connection));
				break;
			}
		}

		;
	};

	private void loginSuccess() {
		defalutHandler.removeCallbacks(defalutTimeout);
		dismissProgressDialog();
		Intent intent = new Intent(LoginActivity.this, MainActivity2.class);
		startActivity(intent);
		finish();
	}

	private void loginFailure() {
		defalutHandler.removeCallbacks(defalutTimeout);
		dismissProgressDialog();
		isRequesting = false;
	}

	private boolean dismissProgressDialog() {
		if (dialog != null) {
			dialog.dismiss();
			dialog = null;
			return true;
		}
		return false;
	}

	private void getDefaultProgressDialog(String msg, boolean cancelable) {
		dialog = new ProgressDialog(this);
		dialog.setMessage(msg);
		dialog.setCancelable(cancelable);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				defalutHandler.removeCallbacks(defalutTimeout);
				isRequesting = false;
			}

		});
		dialog.show();
	}

	private void toast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_LONG).show();
	}

	private Runnable defalutTimeout = new Runnable() {
		@Override
		public void run() {
			defalutHandler.obtainMessage(Constant.MSG_TIME_OUT).sendToTarget();
		}
	};

	private void writeUserInfo(String username, String passowrd,String token) {
		SharedPreferences sharedPref = getSharedPreferences(Config.FILE,
				MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(Config.USERNAME, username);
//		editor.putString(Config.PASSWORD,
//				MD5Utils.getMD5Encoding(passowrd.getBytes()));
		editor.putString(Config.PASSWORD,
				(passowrd));
		editor.putString(Config.TOKEN,token);
		editor.commit();
		HttpUtils.TOKIN = Config.getToken(this);
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
