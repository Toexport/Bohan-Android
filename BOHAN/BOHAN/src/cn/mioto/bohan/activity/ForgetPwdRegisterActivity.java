package cn.mioto.bohan.activity;

import cn.mioto.bohan.R;
import cn.mioto.bohan.utils.Enterface;
import cn.mioto.bohan.utils.ExceptionUtil;
import cn.mioto.bohan.utils.LogUtilNIU;
import cn.mioto.bohan.utils.StringUtil;
import cn.mioto.bohan.utils.ToastUtils;
import cn.mioto.bohan.utils.ViewUtil;

import com.umeng.analytics.MobclickAgent;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
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
					tvTryAgain.setText(msg.arg1 + getResources().getString(R.string.s_try_again));
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
	}

	private TextWatcher textWatcher = new TextWatcher() {
		private CharSequence temp;

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
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
			if (checkMobile(userName)) {
				tvPhoneNumber.setText(userName);
				etPhoneNumber.setVisibility(View.GONE);// 编辑框不可见
				// 改变所显示的按钮为灰色的按钮
				// 灰色按钮开始显示60秒倒计时
				tvGetVerCode.setVisibility(View.GONE);
				tvTryAgain.setText("获取验证码");
				// 编辑电话框不可编辑
				getCode = true;// 已经获取过验证码
				// 要求服务器给此号码发送验证码
				if (barTitle.equals("注册账号")) {
					LogUtilNIU.value("注册");
					sendPhoneNumberToService("sendCodeForRegister.act");
				} else if (barTitle.equals("忘记密码")) {
					LogUtilNIU.value("忘记密码");
					sendPhoneNumberToService("sendCodeForResetPassword.act");
				}
			}
		} else if (v == tvNext) {
			verCode = etVerCode.getText().toString().trim();
			// 发送验证码到服务器看是否正确
			// 判断输入框是否有数据
			String userName = etPhoneNumber.getText().toString().trim();
			if (userName.equals("")) {
				ToastUtils.shortToast(ForgetPwdRegisterActivity.this, "手机号不能为空");
			} else {
				// 如果符合手机号格式
				if (checkMobile(userName)) {
					// TODO 发送用户输入的验证码到服务器，如果验证成功，跳转到重设密码页面
					checkCode(verCode);
				}
			}
		}
		super.onClick(v);
	}

	private void sendPhoneNumberToService(String act) {
		LogUtilNIU.value("获取验证码类型为" + act + "--参数phonenumber---》" + userName);
		new Enterface(act).addParam("phonenumber", userName).doRequest(new JsonClientHandler2() {
			@Override
			public void onInterfaceSuccess(String message, String contentJson) {
				LogUtilNIU.value("获取验证码结果---->" + message);
				// 启动 倒计时线程
				new CountDown60sThreand().start();
				tvRemindCodeHavaeSent.setVisibility(View.VISIBLE);
				tvRemindCodeHavaeSent.setText("验证码发送成功，请留意手机号" + userName + "的短信消息");
			}

			@Override
			public void onInterfaceFail(String json) {
				LogUtilNIU.value("获取验接口失败证码结果---->" + json);
				if (barTitle.equals("注册账号")) {
					ToastUtils.shortToast(ForgetPwdRegisterActivity.this, "该账号已被注册");
					tvGetVerCode.setVisibility(View.VISIBLE);
					tvTryAgain.setText("获取验证码");
				} else if (barTitle.equals("忘记密码")) {
					ToastUtils.shortToast(ForgetPwdRegisterActivity.this, "发送验证码失败");
					tvGetVerCode.setVisibility(View.VISIBLE);
					tvTryAgain.setText("获取验证码");
				}
			}

			@Override
			public void onFailureConnected(Boolean canConnect) {
				ToastUtils.shortToast(ForgetPwdRegisterActivity.this, "网络错误，发送验证码失败");
				tvGetVerCode.setVisibility(View.VISIBLE);
				tvTryAgain.setText("获取验证码");
			}
		});
	}

	private void checkCode(String vercode) {
		LogUtilNIU.value("验证验证码--参数phonenumber---》" + userName + "--验证码为--》" + vercode);
		new Enterface("checkVerification.act").addParam("phonenumber", userName).addParam("verification", vercode)
				.doRequest(new JsonClientHandler2() {

					@Override
					public void onInterfaceSuccess(String message, String contentJson) {
						LogUtilNIU.value("验证验证码--message-->" + message + "contentJson" + contentJson);
						Intent intent = new Intent(ForgetPwdRegisterActivity.this, ResetPwdActivity.class);
						if (barTitle.equals("注册账号")) {
							Bundle bundle = new Bundle();
							bundle.putString("title", "注册账号");
							bundle.putString("userName", userName);
							intent.putExtras(bundle);
						} else if (barTitle.equals("忘记密码")) {
							Bundle bundle = new Bundle();
							bundle.putString("title", "重设密码");
							bundle.putString("userName", userName);
							intent.putExtras(bundle);
						}
						startActivity(intent);// 跳到设置密码的页面
						finish();
					}

					@Override
					public void onInterfaceFail(String json) {
						LogUtilNIU.value("验证验证码接口失败证码结果---->" + json);
						ToastUtils.shortToast(ForgetPwdRegisterActivity.this, "验证码错误");
					}

					@Override
					public void onFailureConnected(Boolean canConnect) {
						ToastUtils.shortToast(ForgetPwdRegisterActivity.this, "网络错误");
					}
				});
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

}
