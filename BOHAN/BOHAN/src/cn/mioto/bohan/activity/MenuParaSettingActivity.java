package cn.mioto.bohan.activity;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import cn.mioto.bohan.BApplication;
import cn.mioto.bohan.Constant;
import cn.mioto.bohan.R;
import cn.mioto.bohan.utils.CommonUtils;
import cn.mioto.bohan.utils.Enterface;
import cn.mioto.bohan.utils.LoadDataThreadUtil;
import cn.mioto.bohan.utils.LogUtilNIU;
import cn.mioto.bohan.utils.ModbusCalUtil;
import cn.mioto.bohan.utils.ToastUtils;
import cn.mioto.bohan.utils.ViewUtil;
import cn.mioto.bohan.view.timepicker.NumberPicker;
import cn.mioto.bohan.view.timepicker.TimePickerForCountDown;
import cn.mioto.bohan.view.timepicker.OptionPicker.OnOptionPickListener;

import com.umeng.analytics.MobclickAgent;
import com.vilyever.socketclient.helper.SocketClientDelegate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import steed.framework.android.client.JsonClientHandler2;

/**
 * 类说明：用于校时和设置负荷门限，单价的页面 用socket协议收发(完成)
 */
public class MenuParaSettingActivity extends BaseCheckDataMenuActivity {
	/********** 布局变量 *************/
	private EditText etSinglePrice;
	private TextView tvSinglePrice;
	private EditText etMaxSuffer;
	private TextView tvMaxSuffer;
	private TextView tvSetTime;
	private TextView tvSaveSetting;
	private TextView tvTime;
	private CheckBox checkBox1;
	private CheckBox checkBox2;
	// 待机断时间设置
	private ImageView reviseTime;
	// 待机断电时间显示
	private TextView tvWaitTime;
	// 当前设置了的待机断电时间
	private String choosingWaitTime;
	private String newSettingTime;

	/*********** MODBUS协议参数 ************************************/
	private String verNum;// 效验码
	private SocketClientDelegate delegate;
	private TextView menu_tv;
	/********* 广播 ******************************************/
	// 指令的种类 0代表查询单价门限 1代表校时 2代表设置代价门限存信息
	private int udpKind = 0;

	private TextView tvSavePrice;// 保存单价
	private TextView tvMenxian;// 保存门限

	public static Socket socket;
	public static BufferedReader in;
	public static PrintWriter out;
	public ReceiveThread receiveThread;

	private static final int REFRESH_DATA = 10;
	private String newPrice, newMaxMenXian;
	DecimalFormat df = new DecimalFormat("######0.00");

	private Handler handler = new Handler() {// 另一个hanlder
		public void handleMessage(Message msg) {
			if (msg.what == Constant.MSG_WHAT_DISMISS_DIALOG_OF_RESEND_TASK) {
				// 加载到数据后正在加载消失
			} else if (msg.what == Constant.MSG_WHAT_SHOW_DIALOG_OF_RESEND_TASK) {
				// 显示正在加载
				LogUtilNIU.value("收到Handler消息******启动查询******");
				if (udpKind == 0) {
					progressGettingDataShow("单价门限查询中");
				} else if (udpKind == 1) {
					progressGettingDataShow("校时中");
				} else if (udpKind == 2) {
					progressGettingDataShow("正在设置单价");
				} else if (udpKind == 6) {
					progressGettingDataShow("正在设置负荷门限");
				}
			} else if (msg.what == Constant.MSG_WHAT_DISMISS_DIALOG_OF_RESEND_TASK_FAIL) {
				// 发送几次后，加载失败
				if (udpKind == 0) {
					progressGettingDataDismiss("单价门限查询失败");
				} else if (udpKind == 1) {
					progressGettingDataDismiss("校时失败");
				} else if (udpKind == 2) {
					progressGettingDataDismiss("设置单价失败");
				} else if (udpKind == 3) {
					progressGettingDataDismiss("查询设备时间失败");
				} else if (udpKind == 4) {
					progressGettingDataDismiss("待机断电时间查询失败");
				} else if (udpKind == 5) {
					progressGettingDataDismiss("设置待机断电延时时间失败");
				} else if (udpKind == 6) {
					progressGettingDataDismiss("设置负荷门限失败");
				}
			} else if (msg.what == Constant.MSG_WHAT_SEND_MSG_BY_SERVICE_RESEND_TASK_FAIL) {
				// 通过服务器去查询信息
				// 通过服务器查询设备实时数据
				// LogUtilNIU.value("Handler收到消息，服务器查询");
				checkByService((String) msg.obj);
			} else if (msg.what == Constant.MSG_SUCCESS) {
				progressDismiss("");
				String message = (String) msg.obj;
				showContent(message);
			} else if (msg.what == Constant.MSG_EXCPTION) {
				progressGettingDataDismiss("连接超时");
			} else if (msg.what == Constant.MSG_FAILURE) {
				progressGettingDataDismiss("设备已离线");
			} else if (msg.what == REFRESH_DATA) {
				newPrice = etSinglePrice.getText().toString().trim();
				newMaxMenXian = etMaxSuffer.getText().toString().trim();
				if (!TextUtils.isEmpty(newPrice)) {
					tvSinglePrice.setText(df.format(Double
							.parseDouble(etSinglePrice.getText().toString()
									.trim()))
							+ "");
				}
				if (!TextUtils.isEmpty(newMaxMenXian)) {
					tvMaxSuffer.setText(df.format(Double
							.parseDouble(etMaxSuffer.getText().toString()
									.trim()))
							+ "");
				}
				/********** 清空设置输入框 *****************************************/
				etSinglePrice.setText("");
				etMaxSuffer.setText("");
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewUtil.initToolbar(this, R.id.toolbar, R.id.toolbar_title,
				R.string.device_name, true, true, R.id.menu_tv, R.string.update);
		ViewUtil.setToolbarTitle(getResources()
				.getString(R.string.para_setting));
		// 初始化Socket
		socket = BApplication.instance.getSocket();

		checkNowData();// 查询单价和负荷门限并显示
		checkBox1.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (checkBox1.isChecked()) {
					// 断电
					checkBox2.setChecked(false);
					String vercode = ModbusCalUtil.verNumber(deviceId + "0015"
							+ "0001" + "01");
					String message = "E7" + deviceId + "0015" + "0001" + "01"
							+ vercode + "0D";
					// 如果1被选中，发送指令设置待机
					progressGettingDataShow("正在设置待机断电");// 断电
					// new LoadDataThreadUtil(message, handler, deviceDBssid,
					// MenuParaSettingActivity.this).start();
					BApplication.instance.socketSend(message);
					try {
						BApplication.instance.socket.setSoTimeout(10 * 1000);
					} catch (SocketException e) {
						e.printStackTrace();
					}
					receiveThread = new ReceiveThread();
					receiveThread.start();
				}

			}
		});
		checkBox2.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (checkBox2.isChecked()) {
					// 如果1被选中，发送指令设置待机
					// 不断电
					checkBox1.setChecked(false);
					String vercode = ModbusCalUtil.verNumber(deviceId + "0015"
							+ "0001" + "00");
					String message = "E7" + deviceId + "0015" + "0001" + "00"
							+ vercode + "0D";
					// 如果1被选中，发送指令设置待机
					progressGettingDataShow("正在设置待机不断电");// 待机
					// new LoadDataThreadUtil(message, handler, deviceDBssid,
					// MenuParaSettingActivity.this).start();
					BApplication.instance.socketSend(message);
					receiveThread = new ReceiveThread();
					receiveThread.start();
				}
			}
		});
	}

	/**********************************************************/
	/**
	 * 判断返回的信息是处理查询负荷门限，电价后返回的信息还是设置成功的返回信息 处理查询后返回的信息 负荷门限 电价
	 */
	// //单价门限设置成功
	// Boolean singlePriceLimitOk = false;
	// //时间成功获取
	// Boolean timeGetted = false;
	protected void showContent(String content) {
		// 3a681606250049 00 0003 0005 0200 3000 00 2c0d
		if (checkUDPMessage(content)) {
			if (isReqCodeEqual(content, "0003")) {
				// 刷新单价和负荷门限信息
				// singlePriceLimitOk = true;
				// 处理返回的单价和门限信息
				String singlePrice = content.substring(24, 28);// 截取
				String maxSuffer = content.substring(28, 34);// 截取
				singlePrice = ModbusCalUtil.addDotDel0(singlePrice, 2);// 数值处理
				maxSuffer = ModbusCalUtil.addDotDel0(maxSuffer, 4);// 数值处理
				tvSinglePrice.setText(singlePrice);// 显示
				tvMaxSuffer.setText(maxSuffer);// 显示
				/***************************************************/
				BApplication.instance.setResendTaskShowBreak(true);// 停止重发机制
				progressGettingDataDismissNoToast();
				progressGettingDataShow("正在查询设备时间");
				// 刷新时间
				handler.postDelayed(new Runnable() {

					@Override
					public void run() {
						checkTime();
					}
				}, 2000);
			} else if (isReqCodeEqual(content, "0015")) {// 设置待机断电或不断电
				BApplication.instance.setResendTaskShowBreak(true);// 线程停止
				if (content.substring(14, 16).equals("00")) {// 设置OK
					progressGettingDataDismiss("设置成功");
				} else if (content.substring(24, 26).equals("01")) {// 数值超出
					progressGettingDataDismissNoToast();
					ToastUtils.shortToast(MenuParaSettingActivity.this, "数值超出");
				} else if (content.substring(24, 26).equals("02")) {// FLASH出错
					progressGettingDataDismissNoToast();
					ToastUtils.shortToast(MenuParaSettingActivity.this,
							"FLASH出错");
				} else {
					progressGettingDataDismissNoToast();
					LogUtilNIU.value("得到其他指令");
				}

			} else if (isReqCodeEqual(content, "0019")
					|| isReqCodeEqual(content, "0020")) {// 保存单价和负荷门限
				LogUtilNIU.value("**********0004指令返回*********" + content);
				// 3A 681606250049 00 0004 0001 00 F7 0D
				// 3A 693228572211 03 0004 0001 16 6B0D
				if (content.substring(14, 16).equals("00")) {
					if (content.substring(24, 26).equals("00")) {// 设置OK
						// 设置单价和负荷门限
						// BApplication.instance.setResendTaskShowBreak(true);//
						// 停止重发机制
						progressGettingDataDismiss("设置成功");
						/***************************************************/
						// 刷新时间和单价门限
						// handler.postDelayed(new Runnable() {
						//
						// @Override
						// public void run() {
						// checkNowData();
						// }
						// }, 2000);
						// 刷新控件值
						handler.sendEmptyMessage(REFRESH_DATA);
						/********** 清空设置输入框 *****************************************/
						// etSinglePrice.setText("");
						// etMaxSuffer.setText("");
					} else if (content.substring(24, 26).equals("01")) {// 数值超出
						ToastUtils.shortToast(MenuParaSettingActivity.this,
								"数值超出");
					} else if (content.substring(24, 26).equals("02")) {// FLASH出错
						ToastUtils.shortToast(MenuParaSettingActivity.this,
								"FLASH出错");
					} else {
						LogUtilNIU.value("得到其他指令");
					}
				}
			} else if (isReqCodeEqual(content, "0005")) {// 判断校时有没有成功
				if (content.substring(24, 26).equals("00")) {// 设置0O
					BApplication.instance.setResendTaskShowBreak(true);// 停止重发机制
					progressGettingDataDismiss("校时成功");
					progressGettingDataShow("正在刷新设备时间");
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							checkTime();
						}
					}, 2000);
					/*
					 * 显示逻辑
					 */
					// tvResultTimeSetResult.setText(content);
				} else if (content.substring(24, 26).equals("01")) {// 数值超出
					ToastUtils.shortToast(MenuParaSettingActivity.this, "数值超出");
				} else if (content.substring(24, 26).equals("02")) {// FLASH出错
					ToastUtils.shortToast(MenuParaSettingActivity.this,
							"FLASH出错");
				} else {
					// tvResultTimeSetResult.setText("得到其他指令"+content);
					LogUtilNIU.e("得到其他指令");
				}
			} else if (isReqCodeEqual(content, "0001")) {// 查询实时数据返回信息
				BApplication.instance.setResendTaskShowBreak(true);// 停止重发机制
				progressGettingDataDismissNoToast();
				LogUtilNIU.value("0001返回了信息");
				String hour = content.substring(80, 82);
				String minute = content.substring(82, 84);
				String second = content.substring(84, 86);
				tvTime.setText(hour + ":" + minute + ":" + second);// 设置时间
				progressGettingDataShow("正在查询待机断电时间");
				// 刷新时间
				handler.postDelayed(new Runnable() {

					@Override
					public void run() {
						checkWaitTime();
					}
				}, 2000);

			} else if (isReqCodeEqual(content, "0017")) {
				BApplication.instance.setResendTaskShowBreak(true);// 停止重发机制
				progressGettingDataDismissNoToast();
				LogUtilNIU.value("0017查询待机断电时间，返回了信息" + content);
				// 10017查询待机断电时间，返回了信息3A6816080500950000170001053D0D
				choosingWaitTime = content.substring(24, 26);
				if (choosingWaitTime.substring(0, 1).equals("0")) {
					choosingWaitTime = choosingWaitTime.substring(1);
				}
				tvWaitTime.setText("现在的设置为" + choosingWaitTime + "分钟");
			} else if (isReqCodeEqual(content, "0016")) {
				BApplication.instance.setResendTaskShowBreak(true);// 停止重发机制
				progressGettingDataDismissNoToast();
				LogUtilNIU.value("0016设置待机断电时间，返回了信息" + content);
				// 0016设置待机断电时间，返回了信息3A681608050095000016000100370D
				if (content.substring(24, 26).equals("00")) {
					ToastUtils.shortToast(MenuParaSettingActivity.this, "设置成功");
					// 把时间设置成最新的显示在界面上，这个数据是上次设置选择了时间是选的那个时间
					tvWaitTime.setText("现在的设置为" + newSettingTime + "分钟");
				} else {
					ToastUtils.shortToast(MenuParaSettingActivity.this,
							"收到设置失败反馈指令，设置失败");
				}
			}
		} else if (content.substring(14, 16).equals("03")) {
			// 3A 68 16 06 25 00 50 03 0004 000116170D
			LogUtilNIU.value("为03数据");
			if (isReqCodeEqual(content, "0020")) {
				LogUtilNIU.value("设置超出值，得到设备类型");
				String powerA = content.substring(24, 26);
				if (powerA.equals("10")) {
					// BApplication.instance.setResendTaskShowBreak(true);//
					// 停止重发机制
					progressGettingDataDismiss("该设备的最大负荷门限不得大于2500W");
				} else if (powerA.equals("13")) {
					// BApplication.instance.setResendTaskShowBreak(true);//
					// 停止重发机制
					progressGettingDataDismiss("该设备的最大负荷门限不得大于3400W");
				} else if (powerA.equals("16")) {
					// BApplication.instance.setResendTaskShowBreak(true);//
					// 停止重发机制
					progressGettingDataDismiss("该设备的最大负荷门限不得大于4000W");
				}
			}
		}
	}

	protected void checkWaitTime() {
		udpKind = 4;
		String verCode2 = ModbusCalUtil.verNumber(deviceId + "00170000");
		final String msg2 = "E7" + deviceId + "00170000" + verCode2 + "0D";// 0017
		BApplication.instance.socketSend(msg2); // 待机断电时间查询
		// new LoadDataThreadUtil(msg2, handler, deviceDBssid, this).start();
		receiveThread = new ReceiveThread();
		receiveThread.start();
	}

	private void checkTime() {
		// 查询设备当前时间
		udpKind = 3;
		String verCode2 = ModbusCalUtil.verNumber(deviceId + "00010000");
		final String msg2 = "E7" + deviceId + "00010000" + verCode2 + "0D";// 0001
		BApplication.instance.socketSend(msg2); // 查询用电参数
		// new LoadDataThreadUtil(msg2, handler, deviceDBssid, this).start();
		receiveThread = new ReceiveThread();
		receiveThread.start();
	}

	@Override
	protected Integer getLayoutId() {
		return R.layout.activity_menu_para_setting;
	}

	/**********************************************************/
	/**
	 * 设备里设置的 单价和负荷门限
	 */
	public void checkNowData() {
		// 显示正在加载
		progressGettingDataShow("实时数据加载中");
		udpKind = 0;
		String verCode = ModbusCalUtil.verNumber(deviceId + "00030000");
		final String message = "E7" + deviceId + "00030000" + verCode + "0D";// 0001
		BApplication.instance.socketSend(message); // 查询用电参数
		// new LoadDataThreadUtil(msg1, handler, deviceDBssid, this).start();
		receiveThread = new ReceiveThread();
		receiveThread.start();
	}

	protected void checkNowDataFromService(String smessage) {
		new Enterface("sendToDevice.act").addParam("deviceid", deviceId)
				.addParam("String content", smessage)
				.doRequest(new JsonClientHandler2() {
					@Override
					public void onInterfaceSuccess(String message,
							String contentJson) {
						LogUtilNIU.e("查询设备实时数据，服务器返回content--->" + contentJson);
						// 显示透传信息，逻辑一样
						showContent(contentJson);
					}

					@Override
					public void onInterfaceFail(String json) {

					}

					@Override
					public void onFailureConnected(Boolean canConnect) {
						ToastUtils.testToast(MenuParaSettingActivity.this,
								"远程控制失败");
					}
				});
	}

	@Override
	public void onClick(View v) {
		if (v == tvSetTime) {
			/*********
			 * 发送校时指令******发送手机当前系统时间设置电表时间
			 ************************************/
			proofreadingTime();
		} else if (v == tvSaveSetting) {
			/*********
			 * 设置单价和门限****输入设置单价门限后点击保存按钮
			 **************************************/
			// 隐藏键盘
			CommonUtils.hideSoftKeyboard(MenuParaSettingActivity.this);
			setPriceAndMax();
		} else if (v == menu_tv) {
			checkNowData();// 查询单价和负荷门限，显示
		} else if (v == reviseTime) {
			// 出现选择时间
			NumberPicker picker = new NumberPicker(this);
			picker.setOffset(2);// 偏移量
			picker.setRange(1, 60);// 数字范围
			picker.setSelectedItem(choosingWaitTime);
			picker.setLabel("分钟");
			picker.setOnOptionPickListener(new OnOptionPickListener() {

				@Override
				public void onOptionPicked(int position, String option) {
					setWaitingTime(option);// 发指令设置待机断电时间
					newSettingTime = option;
				}
			});
			picker.show();
		} else if (v == tvSavePrice) {
			// 保存单价
			// 隐藏键盘
			CommonUtils.hideSoftKeyboard(MenuParaSettingActivity.this);
			setPrice();
		} else if (v == tvMenxian) {
			// 保存门限
			// 隐藏键盘
			CommonUtils.hideSoftKeyboard(MenuParaSettingActivity.this);
			setMax();
		}
		super.onClick(v);
	}

	protected void setWaitingTime(String option) {
		// TODO 设置待机断电时间
		progressGettingDataShow("正在设置待机断电延时时间");
		udpKind = 5;
		String waitTimeNeedToSet = ModbusCalUtil.add0fillLength(option, 2);
		String verCode = ModbusCalUtil.verNumber(deviceId + "00160001"
				+ waitTimeNeedToSet);
		final String message = "E7" + deviceId + "00160001" + waitTimeNeedToSet
				+ verCode + "0D";// 0005
									// 校时
		LogUtilNIU.value("设置待机断电延时时间发送的指令为" + message);
		// new LoadDataThreadUtil(message, handler, deviceDBssid, this).start();
		BApplication.instance.socketSend(message);
		receiveThread = new ReceiveThread();
		receiveThread.start();

	}

	/**********************************************************/
	/**
	 * 发送校时指令
	 */
	private void proofreadingTime() {
		udpKind = 1;
		String systemTime = ModbusCalUtil.getSystemTimeFormat();// 获取系统时间，并处理为插座可识别的格式
		String verCode = ModbusCalUtil.verNumber(deviceId + "00050007"
				+ systemTime);
		final String message = "3A" + deviceId + "00050007" + systemTime
				+ verCode + "0D";// 0005
									// 校时
		new LoadDataThreadUtil(message, handler, deviceDBssid, this).start();
	}

	/**********************************************************/
	/**
	 * 4设置电价相关信息 UDP 设置单价和负荷门限
	 */
	private void setPriceAndMax() {
		udpKind = 2;
		String singlePrice = etSinglePrice.getText().toString().trim();// 获取单价
		String max = etMaxSuffer.getText().toString().trim();// 获取门限
		if (singlePrice.equals("") || max.equals("")) {
			// 判断有没有输入空数据
			ToastUtils.shortToast(MenuParaSettingActivity.this, "单价和负荷门限不能为空");
		} else {
			// 判断输入的单价是否正数或两位小数以内
			if (!ModbusCalUtil.is2SmallNumber(singlePrice)
					|| !ModbusCalUtil.is2SmallNumber(max)) {// 如果单价和门限都不是正浮点数，提示错误
				ToastUtils.shortToast(MenuParaSettingActivity.this,
						"输入数据格式有误，不得超过小数点后两位，不能为0");
			} else {
				if (Float.valueOf(singlePrice) >= 100) {// 判断单价要小于100
					ToastUtils.shortToast(MenuParaSettingActivity.this,
							"设置的单价必须小于100");
				}
				// TODO 提示的逻辑需要改一下 提示上限
				if (!(Float.valueOf(singlePrice) >= 100)) {
					/********
					 * 处理数据，如果有小数点，去除小数点，占满数位。如果是整数，按规则占满数位
					 *******************************************/
					if (!singlePrice.contains(".")) {
						singlePrice = singlePrice + ".0";// 天才！整数的话，先自己加个小数点
					}
					singlePrice = ModbusCalUtil.delDotFillLength(singlePrice,
							4, 2);//
					if (!max.contains(".")) {
						max = max + ".0";// 天才！
					}
					max = ModbusCalUtil.delDotFillLength(max, 6, 4);//
					LogUtilNIU.e("singlePrice处理后是" + singlePrice);
					LogUtilNIU.e("max处理后是" + max);
					/*********
					 * 如果没有小数点，按位置占满数位
					 ******************************************/
					verNum = ModbusCalUtil.verNumber(deviceId + "00040005"
							+ singlePrice + max);// 计算校验码
					final String setMsg = "3A" + deviceId + "00040005"
							+ singlePrice + max + verNum + "0D";
					progressGettingDataShow("正在保存");
					LogUtilNIU.value("保存电价信息的指令为" + setMsg);
					new LoadDataThreadUtil(setMsg, handler, deviceDBssid, this)
							.start();
				}
			}
		}
	}

	// 设备单价
	private void setPrice() {
		udpKind = 2;
		String singlePrice = etSinglePrice.getText().toString().trim();// 获取单价
		if (singlePrice.equals("")) {
			// 判断有没有输入空数据
			ToastUtils.shortToast(MenuParaSettingActivity.this, "单价不能为空");
		} else {
			if (Float.valueOf(singlePrice) >= 100) {// 判断单价要小于100
				ToastUtils.shortToast(MenuParaSettingActivity.this,
						"设置的单价必须小于100");
			}
			// TODO 提示的逻辑需要改一下 提示上限
			if (!(Float.valueOf(singlePrice) >= 100)) {
				/********
				 * 处理数据，如果有小数点，去除小数点，占满数位。如果是整数，按规则占满数位
				 *******************************************/
				if (!singlePrice.contains(".")) {
					singlePrice = singlePrice + ".0";// 天才！整数的话，先自己加个小数点
				}
				singlePrice = ModbusCalUtil.delDotFillLength(singlePrice, 4, 2);//
				LogUtilNIU.e("singlePrice处理后是" + singlePrice);
				/*********
				 * 如果没有小数点，按位置占满数位
				 ******************************************/
				verNum = ModbusCalUtil.verNumber(deviceId + "00190002"
						+ singlePrice);// 计算校验码
				final String setMsg = "E7" + deviceId + "00190002"
						+ singlePrice + verNum + "0D";
				progressGettingDataShow("正在保存");
				LogUtilNIU.value("保存电价信息的指令为" + setMsg);
				BApplication.instance.socketSend(setMsg);
				receiveThread = new ReceiveThread();
				receiveThread.start();
			}
		}
	}

	// 设备负荷门限
	private void setMax() {
		udpKind = 6;
		String max = etMaxSuffer.getText().toString().trim();// 获取门限
		if (max.equals("")) {
			// 判断有没有输入空数据
			ToastUtils.shortToast(MenuParaSettingActivity.this, "负荷门限不能为空");
		} else {
			/********
			 * 处理数据，如果有小数点，去除小数点，占满数位。如果是整数，按规则占满数位
			 *******************************************/
			if (!max.contains(".")) {
				max = max + ".0";// 天才！
			}
			max = ModbusCalUtil.delDotFillLength(max, 6, 4);//
			LogUtilNIU.e("max处理后是" + max);
			/*********
			 * 如果没有小数点，按位置占满数位
			 ******************************************/
			verNum = ModbusCalUtil.verNumber(deviceId + "00200003" + max);// 计算校验码
			final String setMsg = "E7" + deviceId + "00200003" + max + verNum
					+ "0D";
			progressGettingDataShow("正在保存");
			LogUtilNIU.value("保存负荷门限信息的指令为" + setMsg);
			BApplication.instance.socketSend(setMsg);
			receiveThread = new ReceiveThread();
			receiveThread.start();
		}
	}

	/**********************************************************/
	/**
	 * 取消注册
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("参数设置"); // 统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
		MobclickAgent.onResume(this); // 统计时长
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("参数设置"); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证
											// onPageEnd 在onPause 之前调用,因为
											// onPause
											// 中会保存信息。"SplashScreen"为页面名称，可自定义
		MobclickAgent.onPause(this);
	}

	@Override
	protected String onServiceUDPBack(String content) {
		LogUtilNIU.value("服务器接口回调-->" + content);
		showContent(content);
		return null;
	}

	@Override
	protected String onCheckByServiceOnInterfaceFail(String json) {
		BApplication.instance.setResendTaskShowBreak(true);// 线程停止
		progressGettingDataDismissNoToast();
		// 接口错误信息,执行回调{"statusCode":1,"message":"连接超时"}
		try {
			JSONObject jsonObject = new JSONObject(json);
			if (jsonObject.getInt("statusCode") == 1) {
				ToastUtils.shortToast(MenuParaSettingActivity.this, "连接超时");
			}
		} catch (JSONException e) {

			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 接收数据线程
	 */
	private class ReceiveThread extends Thread {
		@Override
		public void run() {
			Message msg = new Message();
			msg.what = Constant.MSG_FAILURE;
			msg.obj = getString(R.string.cannot_connection_server);
			Log.d("Receive", "ReceiveThread() start");
			String result = "";
			try {
				result = BApplication.instance.readString();
				Log.d("Receive", "socket receive:" + result);
				if (result != null) {
					JSONObject obj = new JSONObject(result);
					int code = obj.getInt("statusCode");
					if (code == 0) {
						String content = obj.getString("content");
						msg.obj = content;
						msg.what = Constant.MSG_SUCCESS;
					} else {
						msg.obj = obj.getString("Desc");
						msg.what = Constant.MSG_FAILURE;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				msg.obj = e;
				msg.what = Constant.MSG_EXCPTION;
			}
			handler.sendMessage(msg);
		}

	}
}
