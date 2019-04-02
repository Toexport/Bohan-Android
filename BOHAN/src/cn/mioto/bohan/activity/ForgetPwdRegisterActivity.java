package cn.mioto.bohan.activity;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import cn.mioto.bohan.BApplication;
import cn.mioto.bohan.Constant;
import cn.mioto.bohan.R;
import cn.mioto.bohan.exception.ExceptionManager;
import cn.mioto.bohan.utils.Enterface;
import cn.mioto.bohan.utils.ExceptionUtil;
import cn.mioto.bohan.utils.HttpUtils;
import cn.mioto.bohan.utils.LogUtilNIU;
import cn.mioto.bohan.utils.NetworkUtils;
import cn.mioto.bohan.utils.StringUtil;
import cn.mioto.bohan.utils.ToastUtils;
import cn.mioto.bohan.utils.ViewUtil;

import com.mioto.bohan.webservice.WebServiceClient;
import com.umeng.analytics.MobclickAgent;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import steed.framework.android.client.JsonClientHandler2;

/**
 * 类说明：忘记密码页面和注册页面
 */
public class ForgetPwdRegisterActivity extends SteedAppCompatActivity {

	/********** DECLARES *************/
	// 电话号码输入框
	private EditText etPhoneNumber;
	// 输入验证码的输入框
	private EditText etVerCode;
	// 获取验证码按钮
	private TextView tvGetVerCode;
	//
	private TextView tvTryAgain;
	// 提醒验证码发送成功的tv
	private TextView tvRemindCodeHavaeSent;
	private TextView tvNext;
	private TextView tvPhoneNumber;
	private Toolbar toolbar;
	private String userName;
	private String verCode;
	Boolean getCode = false;
	private static final int VERCODE_FAILE = 10, VERCODE_SUCCESS = 11;
	private ProgressDialog p;
	private boolean isZH = false;

	// 标题
	private String barTitle;
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == 100) {
				if (msg.arg1 == 0) {
					tvGetVerCode.setVisibility(View.VISIBLE);
					etPhoneNumber.setVisibility(View.VISIBLE);
					// getCode=false;
				} else {
					tvTryAgain.setText(msg.arg1
							+ getResources().getString(R.string.s_try_again));
				}
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		barTitle = getIntent().getStringExtra("title");
		ViewUtil.initToolbar(this, barTitle, true, false, 0);
		tvNext.setVisibility(View.GONE);
		etVerCode.addTextChangedListener(textWatcher);
		if (isZh()) {
			etPhoneNumber.setHint(getString(R.string.enter_phone_number));
		} else {
			etPhoneNumber.setHint(getString(R.string.enter_emil));
		}
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

	private TextWatcher textWatcher = new TextWatcher() {
		private CharSequence temp;

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			temp = s;
			if (temp.length() == 6) {
				if (getCode) {
					tvNext.setVisibility(View.VISIBLE);
				}
			} else {
				tvNext.setVisibility(View.GONE);
			}
		}

		@Override
		public void afterTextChanged(Editable s) {

		}

	};

	@Override
	protected Integer getLayoutId() {
		return R.layout.activity_forget_pwd_ver;
	}

	@Override
	public void onClick(View v) {
		if (v == tvGetVerCode) {
			tvRemindCodeHavaeSent.setVisibility(View.GONE);
			// 判断账号是否为空
			userName = etPhoneNumber.getText().toString().trim();
			if (isZH) {
				// 如果是中文
				if (checkMobile(userName)) {
					tvPhoneNumber.setText(userName);
					etPhoneNumber.setVisibility(View.GONE);// 编辑框不可见
					// 改变所显示的按钮为灰色的按钮
					// 灰色按钮开始显示60秒倒计时
					tvGetVerCode.setVisibility(View.GONE);
					tvTryAgain.setText(getString(R.string.get_verCode));
					// 编辑电话框不可编辑
					getCode = true;// 已经获取过验证码
					// 要求服务器给此号码发送验证码
					if (barTitle.equals(getString(R.string.register))) {
						LogUtilNIU.value("注册");
						// sendPhoneNumberToService("sendCodeForRegister.act");
						sendCode(0);
					} else if (barTitle
							.equals(getString(R.string.forget_password2))) {
						LogUtilNIU.value("忘记密码");
						// sendPhoneNumberToService("sendCodeForResetPassword.act");
						sendCode(1);
					}
				}
			} else {
				//英文
				if (TextUtils.isEmpty(userName)) {
					ToastUtils.longToast(this, R.string.email_not_null);
				} else {
					if (!isValidEmail(userName)) {
						ToastUtils.longToast(this, R.string.email_format_error);
					} else {
						tvPhoneNumber.setText(userName);
						etPhoneNumber.setVisibility(View.GONE);// 编辑框不可见
						// 改变所显示的按钮为灰色的按钮
						// 灰色按钮开始显示60秒倒计时
						tvGetVerCode.setVisibility(View.GONE);
						tvTryAgain.setText(getString(R.string.get_verCode));
						// 编辑电话框不可编辑
						getCode = true;// 已经获取过验证码
						// 要求服务器给此号码发送验证码
						if (barTitle.equals(getString(R.string.register))) {
							LogUtilNIU.value("注册");
							// sendPhoneNumberToService("sendCodeForRegister.act");
							sendCode(0);
						} else if (barTitle
								.equals(getString(R.string.forget_password2))) {
							LogUtilNIU.value("忘记密码");
							// sendPhoneNumberToService("sendCodeForResetPassword.act");
							sendCode(1);
						}
					}
				}

			}
		} else if (v == tvNext) {
			verCode = etVerCode.getText().toString().trim();
			// 发送验证码到服务器看是否正确
			// 判断输入框是否有数据
			String userName = etPhoneNumber.getText().toString().trim();
			if (userName.equals("")) {
				if(isZH){
					ToastUtils.shortToast(ForgetPwdRegisterActivity.this,
							getString(R.string.phone_null_remind));
				}else{
					ToastUtils.shortToast(ForgetPwdRegisterActivity.this,
							getString(R.string.email_not_null));
				}
				
			} else {
				if(isZH){
				
				// 如果符合手机号格式
				if (checkMobile(userName)) {
					// TODO 发送用户输入的验证码到服务器，如果验证成功，跳转到重设密码页面
					if (barTitle.equals(getString(R.string.register))) {
						checkCode(verCode, 0);
					} else if (barTitle
							.equals(getString(R.string.forget_password2))) {
						checkCode(verCode, 1);
					}
				}
				
				}else{
					if(isValidEmail(userName)){
						if (barTitle.equals(getString(R.string.register))) {
							checkCode(verCode, 0);
						} else if (barTitle
								.equals(getString(R.string.forget_password2))) {
							checkCode(verCode, 1);
						}
					}
				}
			}
		}
		super.onClick(v);
	}

	public class CountDown60sThreand extends Thread {

		@Override
		public void run() {
			for (int i = 60; i >= 0; i--) {
				Message msg = new Message();
				msg.what = 100;// 更新数字
				msg.arg1 = i;
				handler.sendMessage(msg);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					ExceptionUtil.handleException(e);
					;
				}
			}
			super.run();
		}
	}

	/**********************************************************/
	/**
	 * 判断输入的手机号格式是否正确
	 */
	public Boolean checkMobile(String number) {
		// 判断账号是否为空
		if (number.length() != 0) {
			// 判断账号是否手机号
			if (StringUtil.isMobileNO(number)) {
				return true;
			} else {
				ToastUtils.longToast(this, R.string.phone_wrong_remind);
				return false;
			}
		} else {
			ToastUtils.longToast(this, R.string.phone_null_remind);
			return false;
		}
	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("忘记密码Activity"); // 统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
		MobclickAgent.onResume(this); // 统计时长
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("忘记密码Activity"); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证
													// onPageEnd 在onPause
													// 之前调用,因为 onPause
													// 中会保存信息。"SplashScreen"为页面名称，可自定义
		MobclickAgent.onPause(this);
	}

	/**
	 * 获取验证码
	 */
	private void sendCode(final int flag) {
		if (NetworkUtils.isNetworkConnected(this)) {
			new Thread() {
				public void run() {
					Message msg = new Message();
					msg.what = Constant.MSG_FAILURE;
					msg.obj = getString(R.string.cannot_connection_server);
					try {

						Map map = new HashMap<>();
						String result = "";
						if(isZH){
							map.put("mobileNum", userName);
							map.put("flag", flag + "");
							result = WebServiceClient.CallWebService(
									"GetRegisterCode", map,
									ForgetPwdRegisterActivity.this);
						}else{
							map.put("reciver", userName);
							map.put("flag", flag + "");
							result = WebServiceClient.CallWebService(
									"GetRegisterCodeByMail", map,
									ForgetPwdRegisterActivity.this);
						}
						Log.d("resultGetRegisterCode", result);
						if (result != null) {
							JSONObject obj = new JSONObject(result);
							int statusCode = obj.getInt("statusCode");
							msg.obj = obj.getString("message");
							if (statusCode == 0) {
								msg.what = Constant.MSG_SUCCESS;
							} else {
								msg.what = Constant.MSG_FAILURE;
							}
						}

					} catch (Exception e) {
						e.printStackTrace();
						msg.obj = e;
						msg.what = Constant.MSG_EXCPTION;
					}
					defalutHandler.sendMessage(msg);
				}
			}.start();
		} else {
			defalutHandler.sendEmptyMessage(Constant.MSG_NETWORK_ERROR);
		}
	}

	/**
	 * 检测验证码是否正确
	 */
	private void checkCode(final String vercode, final int flag) {
		showDialog();
		if (NetworkUtils.isNetworkConnected(this)) {
			new Thread() {
				public void run() {
					Message msg = new Message();
					msg.what = Constant.MSG_FAILURE;
					msg.obj = getString(R.string.cannot_connection_server);
					try {

						Map map = new HashMap<>();
						map.put("RegName", userName);
						map.put("checkCode", vercode);
						map.put("flag", flag + "");
						String result = WebServiceClient.CallWebService(
								"ValidateCheckCode", map,
								ForgetPwdRegisterActivity.this);
						Log.d("resultValidateCheckCode", result);
						if (result != null) {
							JSONObject obj = new JSONObject(result);
							int statusCode = obj.getInt("statusCode");
							msg.obj = obj.getString("message");
							if (statusCode == 0) {
								msg.what = VERCODE_SUCCESS;
							} else {
								msg.what = VERCODE_FAILE;
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						msg.obj = e;
						msg.what = Constant.MSG_EXCPTION;
					}
					defalutHandler.sendMessage(msg);
				}
			}.start();
		} else {
			defalutHandler.sendEmptyMessage(Constant.MSG_NETWORK_ERROR);
		}
	}

	private Handler defalutHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (p != null) {
				p.dismiss();
				p = null;
			}
			switch (msg.what) {
			case Constant.MSG_SUCCESS:
				// 启动 倒计时线程
				new CountDown60sThreand().start();
				tvRemindCodeHavaeSent.setVisibility(View.VISIBLE);
				tvRemindCodeHavaeSent
						.setText(getString(R.string.send_code_tip1) + userName
								+ getString(R.string.send_code_tip2));
				toast((String) msg.obj);
				break;
			case Constant.MSG_FAILURE:
				// ToastUtils.shortToast(ForgetPwdRegisterActivity.this,
				// "网络错误，发送验证码失败");
				tvGetVerCode.setVisibility(View.VISIBLE);
				tvTryAgain.setText(getString(R.string.get_verCode));
				toast((String) msg.obj);
				break;
			case Constant.MSG_EXCPTION:
				ToastUtils.shortToast(ForgetPwdRegisterActivity.this,
						getString(R.string.send_code_error_tip));
				tvGetVerCode.setVisibility(View.VISIBLE);
				tvTryAgain.setText(getString(R.string.get_verCode));
				toast(ExceptionManager.getErrorDesc(getApplicationContext(),
						(Exception) msg.obj));
				break;
			case Constant.MSG_TIME_OUT:
				ToastUtils.shortToast(ForgetPwdRegisterActivity.this,
						getString(R.string.send_code_error_tip));
				tvGetVerCode.setVisibility(View.VISIBLE);
				tvTryAgain.setText(getString(R.string.get_verCode));
				toast(getString(R.string.connection_timeout));
				break;
			case Constant.MSG_NETWORK_ERROR:
				ToastUtils.shortToast(ForgetPwdRegisterActivity.this,
						getString(R.string.send_code_error_tip));
				tvGetVerCode.setVisibility(View.VISIBLE);
				tvTryAgain.setText(getString(R.string.get_verCode));
				toast(getString(R.string.no_connection));
				break;
			case VERCODE_FAILE:
				toast((String) msg.obj);
				break;
			case VERCODE_SUCCESS:
				Intent intent = new Intent(ForgetPwdRegisterActivity.this,
						ResetPwdActivity.class);
				if (barTitle.equals(getString(R.string.register))) {
					Bundle bundle = new Bundle();
					bundle.putString("title", getString(R.string.register));
					bundle.putString("userName", userName);
					bundle.putString("checkCode", verCode);
					intent.putExtras(bundle);
				} else if (barTitle
						.equals(getString(R.string.forget_password2))) {
					Bundle bundle = new Bundle();
					bundle.putString("title", getString(R.string.repwd));
					bundle.putString("userName", userName);
					bundle.putString("checkCode", verCode);
					intent.putExtras(bundle);
				}
				startActivity(intent);// 跳到设置密码的页面
				finish();
				break;
			}
		}

		;
	};

	private void toast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_LONG).show();
	}

	private void showDialog() {
		// 显示正在查询progress
		p = new ProgressDialog(this);
		p.setMessage(getResources().getString(R.string.verifying));
		p.setCanceledOnTouchOutside(false);
		p.show();
	}

	private boolean isValidEmail(String mail) {
		Pattern pattern = Pattern
				.compile("^[A-Za-z0-9][\\w\\._]*[a-zA-Z0-9]+@[A-Za-z0-9-_]+\\.([A-Za-z]{2,4})");
		Matcher mc = pattern.matcher(mail);
		return mc.matches();
	}
}
