package cn.mioto.bohan.activity;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import cn.mioto.bohan.BApplication;
import cn.mioto.bohan.Constant;
import cn.mioto.bohan.R;
import cn.mioto.bohan.adapter.NineSetBaseAdapter;
import cn.mioto.bohan.utils.LoadDataThreadUtil;
import cn.mioto.bohan.utils.LogUtilNIU;
import cn.mioto.bohan.utils.ModbusCalUtil;
import cn.mioto.bohan.utils.ToastUtils;
import cn.mioto.bohan.utils.ViewUtil;
import cn.mioto.bohan.view.pulltorefresh.MaterialRefreshLayout;
import cn.mioto.bohan.view.pulltorefresh.MaterialRefreshListener;

import com.umeng.analytics.MobclickAgent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 类说明：9组时段设置页面 UDP改写中 作者： jiemai liangminhua 创建时间：2016年5月24日 下午5:27:13
 */
public class Menu9ONOFFActivity extends BaseCheckDataMenuActivity {
	/********** 初始化布局控件 *************/
	// private ListView m9setList;
	private MaterialRefreshLayout refresh;
	private Boolean refreshing = false;
	// 9组定时那9组的设置信息
	private Map<Integer, String> mNineSetSetting = new HashMap<>();
	// 下面的确认当前修改按钮
	private TextView tvBottomBtnConRev;
	// 底下的修改设置区域
	private LinearLayout llBottomRev;

	/******* 广播接收者 ********************************************/
	private NineSetBaseAdapter adapter;
	private List<String> strings;
	// udp指令的类型
	private int udpKind = 0;

	/***************************************************/
	private List<LinearLayout> setlist = new ArrayList<>();
	private int revicePosition;
	/***************************************************/
	private ListView m9setList;

	public static Socket socket;
	public static BufferedReader in;
	public static PrintWriter out;
	public ReceiveThread receiveThread;

	protected Handler h = new Handler() {// 另一个hanlder
		public void handleMessage(Message msg) {
			if (msg.what == Constant.MSG_WHAT_DISMISS_DIALOG_OF_RESEND_TASK) {
				// 加载到数据后正在加载消失
			} else if (msg.what == Constant.MSG_WHAT_SHOW_DIALOG_OF_RESEND_TASK) {
				LogUtilNIU.value("activity的hanlder收到线程开始的消息");
				if (udpKind == 1) {
					progressGettingDataShow("正在设置");
				}
			} else if (msg.what == Constant.MSG_WHAT_DISMISS_DIALOG_OF_RESEND_TASK_FAIL) {
				if (udpKind == 0) {
					progressGettingDataDismiss("加载数据失败");
				} else if (udpKind == 1) {
					progressGettingDataDismiss("设置失败");
				}
			} else if (msg.what == Constant.MSG_WHAT_SEND_MSG_BY_SERVICE_RESEND_TASK_FAIL) {
				// 通过服务器去查询信息
				// 通过服务器查询设备实时数据
				checkByService((String) msg.obj);
			} else if (msg.what == Constant.MSG_SUCCESS) {
				progressDismiss("");
				String message = (String) msg.obj;
				showContent(message);
			} else if (msg.what == Constant.MSG_EXCPTION) {
				progressGettingDataDismiss("连接超时");
			} else if (msg.what == Constant.MSG_FAILURE) {
				progressGettingDataDismiss("设备已离线");
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewUtil.initToolbar(this, R.id.toolbar, R.id.toolbar_title,
				R.string.time_setting, true, false, 0, 0);
		ViewUtil.setToolbarTitle(getResources().getString(R.string.dur_setting));
		strings = new ArrayList<>();// 初始化,数据开始为0，界面显示全部离线
		/***************************************************/
		// 初始化ListView数据
		adapter = new NineSetBaseAdapter(this, strings, deviceId, deviceDBssid,
				h);
		m9setList.setAdapter(adapter);
		/******* 判断，发送信息 ********************************************/
		progressGettingDataShow("正在查询9组定时数据");
		checkNowData();// 查询9组定时信息
		refresh.setMaterialRefreshListener(new MaterialRefreshListener() {

			@Override
			public void onRefresh(MaterialRefreshLayout materialRefreshLayout) {
				checkNowData();// 刷新
				refreshing = true;
			}
		});

		// 初始化Socket
		socket = BApplication.instance.getSocket();
	}

	/**********************************************************/
	/**
	 * 查询上次设置的9组定时信息
	 */
	private void checkNowData() {
		udpKind = 0;
		String verCode = ModbusCalUtil.verNumber(deviceId + "00080000");
		final String message = "E7" + deviceId + "00080000" + verCode + "0D";// 0008
																				// 查询9组继电器通断时间
		// new LoadDataThreadUtil(message, h,deviceDBssid,this).start();
		BApplication.instance.socketSend(message);
		try {
			BApplication.socket.setSoTimeout(10 * 1000);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		receiveThread = new ReceiveThread();
		receiveThread.start();
	}

	@Override
	protected Integer getLayoutId() {
		return R.layout.activity_menu_nine_setting;
	}

	/**
	 * 在设置页面选择了时间后，在这里更新数据
	 * 
	 * @Description:通过map里面的数据，设置新的stringList
	 */
	public void makeNewSetFromMap() {
		strings.clear();
		for (int i = 0; i < mNineSetSetting.size(); i++) {
			strings.add(mNineSetSetting.get(i));
		}
	}

	/**********************************************************/
	/**
	 * 收到数据后显示内容
	 */
	public void showContent(String content) {
		if (checkUDPMessage(content)) {
			LogUtilNIU.value("16到20命令号" + content.substring(16, 20)
					+ "**数据长度**" + content.length());
			if (isReqCodeEqual(content, "0008")) {// 收到9组定时数据
				BApplication.instance.setResendTaskShowBreak(true);// 停止重发机制
				progressGettingDataDismissNoToast();
				// ToastUtils.testToast(Menu9ONOFFActivity.this,
				// "收到9组定时数据"+content);
				strings.clear();
				if (refreshing) {
					refreshing = false;
					refresh.finishRefresh();
					ToastUtils.shortToast(Menu9ONOFFActivity.this, "刷新成功");
				}
				llBottomRev.setVisibility(View.GONE);
				// 查询查询9组继电器通断时间返回的信息
				String set0 = content.substring(24, 34);
				mNineSetSetting.put(0, set0);
				String set1 = content.substring(34, 44);
				mNineSetSetting.put(1, set1);
				String set2 = content.substring(44, 54);
				mNineSetSetting.put(2, set2);
				String set3 = content.substring(54, 64);
				mNineSetSetting.put(3, set3);
				String set4 = content.substring(64, 74);
				mNineSetSetting.put(4, set4);
				String set5 = content.substring(74, 84);
				mNineSetSetting.put(5, set5);
				String set6 = content.substring(84, 94);
				mNineSetSetting.put(6, set6);
				String set7 = content.substring(94, 104);
				mNineSetSetting.put(7, set7);
				String set8 = content.substring(104, 114);
				mNineSetSetting.put(8, set8);
				strings.add(set0);
				strings.add(set1);
				strings.add(set2);
				strings.add(set3);
				strings.add(set4);
				strings.add(set5);
				strings.add(set6);
				strings.add(set7);
				strings.add(set8);
				// TODO 先改为不做排序
				// NineSetComparator com = new NineSetComparator();
				// Collections.sort(strings,com);
				adapter.notifyDataSetChanged();// 更新数据
			} else if (isReqCodeEqual(content, "0009")) {// 设置9组继电器通断时间成功
				adapter.clearSettingMap();
				p.dismiss();
				LogUtilNIU.value("设置9组继电器通断时间反馈-->" + content);
				if (content.substring(24, 26).equals("00")) {// 设置成功
					BApplication.instance.setResendTaskShowBreak(true);// 停止重发机制
					progressGettingDataDismiss("设置成功");
					// 下面的修改区域消失
					/***************************************************/
					// 设置菊花消失和按钮新状态
					String id = content.substring(2, 14);
					// 判断该数据是lv第几位，然后把那个位置的按钮和其他控件的状态做相应改变
					int position = BApplication.instance
							.getNineSetSettingPosition();
					// 把旧数据修改后先放到这儿显示，防止滑动带来的bug
					h.postDelayed(new Runnable() {
						public void run() {
							progressGettingDataShow("正在刷新");
						}
					}, 500);
					// 真正更新
					h.postDelayed(new Runnable() {
						@Override
						public void run() {
							checkNowData();// 查询9组定时信息，来更新数据
						}
					}, Constant.RESENDTIME);
				} else if (content.substring(24, 26).equals("01")) {// 数值超出
					BApplication.instance.setResendTaskShowBreak(true);// 停止重发机制
					progressDismissNoToast();
					adapter.clearSettingMap();
					ToastUtils.shortToast(Menu9ONOFFActivity.this, "数值超出");
				} else if (content.substring(24, 26).equals("02")) {// FLASH出错
					BApplication.instance.setResendTaskShowBreak(true);// 停止重发机制
					progressDismissNoToast();
					adapter.clearSettingMap();
					ToastUtils.shortToast(Menu9ONOFFActivity.this, "FLASH出错");
				}
			}
		} else {// TODO 这是一个待修复的小BUG
				// 3A 681606250010 03 0009 0000 C5 0D
			BApplication.instance.setResendTaskShowBreak(true);
			LogUtilNIU.value("接收到BUG数据！！！！" + content);
			p.dismiss();
			ToastUtils.shortToast(Menu9ONOFFActivity.this, "操作失败");
			adapter.clearSettingMap();
			llBottomRev.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		// 点击下面的确认修改按钮
		if (v == tvBottomBtnConRev) {
			BApplication.instance.setNineSetWaitConfirm(false);// 待确认状态在按钮被点击后取消
			adapter.sendOrderToConfirmSetting();
			receiveThread = new ReceiveThread();
			receiveThread.start();
			udpKind = 1;// 正在设置
			// 点击确定修改按钮以后，收集所有当前的界面设置信息。再发送指令
			// //以下是根据选择框的情况修改9组定时。为设置单个的逻辑
			// //设置9组定时
			// StringBuffer sendToSetTime = new StringBuffer();
			// for(int i = 0 ; i < strings.size(); i++){
			// sendToSetTime.append(strings.get(i));
			// }
			// String setTime =sendToSetTime.toString();//指令的内容
			// String verCode =
			// ModbusCalUtil.verNumber(deviceId+"0009"+"002D"+setTime);
			// String msg = "3A"+deviceId+"0009"+"002D"+setTime+verCode+"0D";
			// LogUtilNIU.value("设置9组定时发送的数据为"+msg);
			// progressGettingDataShow("正在设置");
			// udpKind = 1;
			// new LoadDataThreadUtil(msg, h,deviceDBssid,this).start();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		/***************************************************/
		// 选择完时间，点击确定按钮后，返回到这里
		// 发送指令设置新的时间
		if (resultCode == RESULT_OK) {
			// 收到设置数据
			String time = data.getExtras().getString(
					Constant.DATA_NINE_SET_SET_RESULT_EXTRA);// 返回的时间
			revicePosition = data.getExtras().getInt(
					Constant.POSITION_NINE_SET_SETTING_ITEM);// 这个Item的位置
			LogUtilNIU.value("revicePosition--->" + revicePosition);
			// 判断该item 发送设置9组定时的指令,其他数据不变，只修改该item
			LogUtilNIU.value("9组界面activityResult收到数据了time----" + time);
			// 设置该position的Item为待确认字体

			// 把设置时间放在adapter的mNineSetSetting里存起来，用于发送指令时使用
			adapter.setmNineSetSetting(revicePosition, time);
			// 把新设置的时间放到集合里备用，用于发指令设置。
			mNineSetSetting.put(revicePosition, time);
			// 把这个item对应地设置成新的数据，然后更新一下显示
			makeNewSetFromMap();
			adapter.notifyDataSetChanged();
			// 页面的修改区域和按钮出现
			llBottomRev.setVisibility(View.VISIBLE);
		}
	}

	/**********************************************************/
	/**
	 * 取消注册广播
	 */
	@Override
	protected void onDestroy() {
		BApplication.instance.setNineSetWaitConfirm(false);
		super.onDestroy();
	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("9组时段设置"); // 统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
		MobclickAgent.onResume(this); // 统计时长
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("9组时段设置"); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证
											// onPageEnd 在onPause 之前调用,因为
											// onPause
											// 中会保存信息。"SplashScreen"为页面名称，可自定义
		MobclickAgent.onPause(this);
	}

	@Override
	protected String onServiceUDPBack(String content) {
		showContent(content);
		return null;
	}

	@Override
	protected String onCheckByServiceOnInterfaceFail(String json) {
		// TODO Auto-generated method stub
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
						msg.what = Constant.MSG_FAILURE;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				msg.obj = e;
				msg.what = Constant.MSG_EXCPTION;
			}
			h.sendMessage(msg);
		}
	}
}
