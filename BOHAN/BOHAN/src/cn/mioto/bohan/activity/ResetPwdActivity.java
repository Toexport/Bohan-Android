package cn.mioto.bohan.activity;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import cn.mioto.bohan.BApplication;
import cn.mioto.bohan.Constant;
import cn.mioto.bohan.R;
import cn.mioto.bohan.exception.ExceptionManager;
import cn.mioto.bohan.utils.Enterface;
import cn.mioto.bohan.utils.HttpUtils;
import cn.mioto.bohan.utils.LogUtilNIU;
import cn.mioto.bohan.utils.NetworkUtils;
import cn.mioto.bohan.utils.ToastUtils;
import cn.mioto.bohan.utils.ViewUtil;

import com.mioto.bohan.webservice.WebServiceClient;
import com.umeng.analytics.MobclickAgent;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import steed.framework.android.client.JsonClientHandler2;

/**
 * 类说明：重置密码页面和注册时设置密码的页面
 */
public class ResetPwdActivity extends SteedAppCompatActivity {
	/********** DECLARES *************/
	private EditText etPwd1;
	private EditText etPwd2;
	// 确认按钮
	private TextView tvConfirm;
	// 用户手机号
	private String userName;
	private String bartitle;
	private String pwd1;
	private ProgressDialog dialog;
	private boolean isRequesting = true;
	private static final int RESPWD_SUCCESS = 10;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		bartitle = (String) getIntent().getExtras().get("title");
		userName = (String) getIntent().getExtras().get("userName");
		ViewUtil.initToolbar(this, bartitle, true, false, 0);
	}

	@Override
	protected Integer getLayoutId() {
		return R.layout.activity_reset_pwd;
	}

	@Override
	public void onClick(View v) {
		if (v == tvConfirm) {

			pwd1 = etPwd1.getText().toString().trim();
			String pwd2 = etPwd1.getText().toString().trim();
			if (!(pwd1.equals("") || pwd2.equals(""))) {
				if (pwd1.equals(pwd2)) {
					if (bartitle.equals("重设密码")) {
						resetPwd(pwd1);// 发送密码到服务器
					} else if (bartitle.equals("注册账号")) {
						// register(userName,pwd1);//发送密码到服务器
						registerAccount(userName, pwd1);
					}
				}
			} else {
				ToastUtils.shortToast(ResetPwdActivity.this, "密码不能为空");
			}
		}
		super.onClick(v);
	}

	private void register(String userName2, final String pwd1) {
		new Enterface("register.act").addParam("user.username", userName2)
				.addParam("user.password", pwd1)
				.doRequest(new JsonClientHandler2() {
					@Override
					public void onInterfaceSuccess(String message,
							String contentJson) {
						LogUtilNIU.value("注册---->" + contentJson);
						ToastUtils.shortToast(ResetPwdActivity.this, "注册成功");
						Intent data = new Intent();
						Bundle bundle = new Bundle();
						bundle.putString(
								Constant.REGISTER_OK_RESULT_USER_NAME_BUNDLE_KEY,
								userName);
						bundle.putString(
								Constant.REGISTER_OK_RESULT_USER_PASSWORD_BUNDLE_KEY,
								pwd1);
						data.putExtras(bundle);
						setResult(Constant.REGISTER_OK_RESULT, data);
						finish();
					}

					@Override
					public void onInterfaceFail(String json) {
						ToastUtils.shortToast(ResetPwdActivity.this, json);
					}

					@Override
					public void onFailureConnected(Boolean canConnect) {
						ToastUtils.shortToast(ResetPwdActivity.this, "网络不给力");
					}
				});
	}

	private void resetPwd(final String pwd1) {
		// TODO Auto-generated method stub
		new Enterface("resetPassword.act").addParam("user.username", userName)
				.addParam("user.password", pwd1)
				.doRequest(new JsonClientHandler2() {
					@Override
					public void onInterfaceSuccess(String message,
							String contentJson) {
						LogUtilNIU.value("重设密码Json为---->" + contentJson);
						// 成功后服务器返回""
						Intent data = new Intent();
						Bundle bundle = new Bundle();
						bundle.putString(
								Constant.REGISTER_OK_RESULT_USER_NAME_BUNDLE_KEY,
								userName);
						bundle.putString(
								Constant.REGISTER_OK_RESULT_USER_PASSWORD_BUNDLE_KEY,
								pwd1);
						data.putExtras(bundle);
						setResult(Constant.REGISTER_OK_RESULT, data);
						finish();
					}

					@Override
					public void onInterfaceFail(String json) {
						ToastUtils.shortToast(ResetPwdActivity.this, "重置密码失败");
						LogUtilNIU.interfaceWrong("重置密码失败");
					}

					@Override
					public void onFailureConnected(Boolean canConnect) {
						// TODO Auto-generated method stub

					}
				});
	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("重设密码页"); // 统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
		MobclickAgent.onResume(this); // 统计时长
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("重设密码页"); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证
											// onPageEnd 在onPause 之前调用,因为
											// onPause
											// 中会保存信息。"SplashScreen"为页面名称，可自定义
		MobclickAgent.onPause(this);
	}

	private void registerAccount(final String username, final String pwd) {
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
								"Register", map);
						Log.d("resultRegister", result);
						if (result != null) {
							JSONObject obj = new JSONObject(result);
							int statusCode = obj.getInt("statusCode");
							if (statusCode == 0) {
								HttpUtils.TOKIN = obj.getString("content");
								// if (isRequesting) {
								// Editor e = ((BApplication) getApplication())
								// .getEditor();
								// e.putString("USER_NAME", userName);
								// e.putString("PASSWORD", userPassword);
								// e.putString("token", HttpUtils.TOKIN);
								// e.commit();// 收藏密码
								// }
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

	private void resetPwd2(final String username, final String pwd) {
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
								"Register", map);
						Log.d("resultRegister", result);
						if (result != null) {
							JSONObject obj = new JSONObject(result);
							int statusCode = obj.getInt("statusCode");
							if (statusCode == 0) {
								HttpUtils.TOKIN = obj.getString("content");
								// if (isRequesting) {
								// Editor e = ((BApplication) getApplication())
								// .getEditor();
								// e.putString("USER_NAME", userName);
								// e.putString("PASSWORD", userPassword);
								// e.putString("token", HttpUtils.TOKIN);
								// e.commit();// 收藏密码
								// }
								msg.what = RESPWD_SUCCESS;
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
				regSuccess();
				break;
			case Constant.MSG_FAILURE:
				regFailure();
				toast((String) msg.obj);
				break;
			case Constant.MSG_EXCPTION:
				regFailure();
				toast(ExceptionManager.getErrorDesc(getApplicationContext(),
						(Exception) msg.obj));
				break;
			case Constant.MSG_TIME_OUT:
				regFailure();
				toast(getString(R.string.connection_timeout));
				break;
			case Constant.MSG_NETWORK_ERROR:
				regFailure();
				toast(getString(R.string.no_connection));
				break;
			case RESPWD_SUCCESS:
				// 成功后服务器返回""
				Intent data = new Intent();
				Bundle bundle = new Bundle();
				bundle.putString(
						Constant.REGISTER_OK_RESULT_USER_NAME_BUNDLE_KEY,
						userName);
				bundle.putString(
						Constant.REGISTER_OK_RESULT_USER_PASSWORD_BUNDLE_KEY,
						pwd1);
				data.putExtras(bundle);
				setResult(Constant.REGISTER_OK_RESULT, data);
				finish();
				break;
			}
		}

		;
	};

	private void regSuccess() {
		defalutHandler.removeCallbacks(defalutTimeout);
		dismissProgressDialog();
		Intent data = new Intent();
		Bundle bundle = new Bundle();
		bundle.putString(Constant.REGISTER_OK_RESULT_USER_NAME_BUNDLE_KEY,
				userName);
		bundle.putString(Constant.REGISTER_OK_RESULT_USER_PASSWORD_BUNDLE_KEY,
				pwd1);
		data.putExtras(bundle);
		setResult(Constant.REGISTER_OK_RESULT, data);
		finish();
	}

	private void regFailure() {
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
}
