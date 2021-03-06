package cn.mioto.bohan.activity;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mioto.bohan.webservice.WebServiceClient;
import com.vilyever.socketclient.SocketClient;

import cn.mioto.bohan.BApplication;
import cn.mioto.bohan.Constant;
import cn.mioto.bohan.R;
import cn.mioto.bohan.adapter.OnlineStatusListAcapter;
import cn.mioto.bohan.entity.SingleDevice;
import cn.mioto.bohan.exception.ExceptionManager;
import cn.mioto.bohan.utils.Enterface;
import cn.mioto.bohan.utils.LoadDataThreadUtil;
import cn.mioto.bohan.utils.LogUtilNIU;
import cn.mioto.bohan.utils.ModbusCalUtil;
import cn.mioto.bohan.utils.NetworkUtils;
import cn.mioto.bohan.utils.ToastUtils;
import cn.mioto.bohan.utils.ViewUtil;
import cn.mioto.bohan.view.pulltorefresh.MaterialRefreshLayout;
import cn.mioto.bohan.view.pulltorefresh.MaterialRefreshListener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import cz.msebera.android.httpclient.Header;
import steed.framework.android.client.JsonClientHandler2;

/**
 * 类说明：位置和类型在线列表的显示类
 */
public class OnlineStatusActivity extends BaseUDPActivityNoCurrent {
	protected List<SingleDevice> deviceList = new ArrayList<>();
	protected OnlineStatusListAcapter adapter;
	protected String thisType;
	protected BroadcastReceiver receiver;
	protected ListView listView;
	protected MaterialRefreshLayout refreshLayout;
	Boolean isRefreshing = false;
	protected boolean refreshing = false;
	protected int STOP_PULL_REFRESH = 100;
	private String PosName = "", LoadName = "";

	public static Socket socket;
	public static BufferedReader in;
	public static PrintWriter out;
	public ReceiveThread receiveThread;

	protected Handler h = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == STOP_PULL_REFRESH) {
				refreshLayout.finishRefresh();
				// isRefreshing = false;
				// ToastUtils.shortToast(OnlineStatusActivity.this, "刷新成功");
			} else if (msg.what == Constant.MSG_SUCCESS) {
				progressGettingDataDismiss("");
				String message = (String) msg.obj;
				if (message.equals("")) {
					// ToastUtils.shortToast(OnlineStatusActivity.this,
					// "当前没有在线设备");
				} else {
					String[] msgs = message.split(",");
					LogUtilNIU.value("包含逗号" + Arrays.toString(msgs));
					for (int i = 0; i < msgs.length; i++) {
						String msg1 = msgs[i];// 获得单个返回信息
						LogUtilNIU.value("****被处理的指令为****" + msg1);
						// 处理在线状况的变更
						dealJidianqiStatus(msg1);
						// checkCharAndDealList(msg);//这个是旧的解析方法
					}
				}
			} else if (msg.what == Constant.MSG_SEND_CONTENT) {
				String message = (String) msg.obj;
				BApplication.instance.socketSend(message);
				receiveThread = new ReceiveThread();
				receiveThread.start();
			} else if(msg.what == Constant.MSG_EXCPTION){
				progressGettingDataDismissNoToast();
				ToastUtils.shortToast(OnlineStatusActivity.this, "控制失败或该类无在线设备");
			} else if(msg.what == Constant.MSG_FAILURE){
				progressGettingDataDismiss("设备已离线");
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_online_list);
		Intent i = getIntent();
		Bundle b = i.getExtras();
		thisType = b.getString(Constant.INTENT_KEY_THIS_SORT);
		PosName = b.getString("PosName");
		LoadName = b.getString("LoadName");
		// 初始化Socket
		socket = BApplication.instance.getSocket();
		getCheckThisTypeList(PosName, LoadName);
		/***************************************************/
		bindViews();
		ViewUtil.initToolbar(this, thisType, true, false, 0);// 初始化标题为单个分类
		progressGettingDataShow("正在查询列表信息");
		initListView();
		/***************************************************/
		receiver = new UDPBrocastReceiver();// 注册广播
		// 正在开启
		filter.addAction(Constant.BROCAST_ONLINE_LIST_SHOW_OPEN_DIALOG);
		// 正在关闭
		filter.addAction(Constant.BROCAST_ONLINE_LIST_SHOW_CLOSE_DIALOG);
		// 加载成功后，dialog消失
		filter.addAction(Constant.BROCAST_ONLINE_LIST_DIMISS_DIALOG);
		// 刷新单个继电器状态
		filter.addAction(Constant.BROCAST_REFRESHING0002_DIALOG);
		registerReceiver(receiver, filter);
		refreshLayout.setMaterialRefreshListener(new MaterialRefreshListener() {

			@Override
			public void onRefresh(MaterialRefreshLayout materialRefreshLayout) {
				isRefreshing = true;
				checkOnlineStatus();
			}
		});
		// checkOnlineStatus();
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(receiver);
		super.onDestroy();
	}

	// ------------------------------------------------------------------------------

	protected void bindViews() {
		listView = (ListView) findViewById(R.id.listView);
		refreshLayout = (MaterialRefreshLayout) findViewById(R.id.refreshLayout);
	}

	protected void initListView() {
		adapter = new OnlineStatusListAcapter(this, deviceList, h);
		listView.setAdapter(adapter);
	}

	/**
	 * @Description:从服务器查询本数据 Parameters: return:void
	 */
	protected void checkThisTypeList(final String interfaceType) {
		new Enterface("getAllDevices.act").addParam(interfaceType, thisType)
				.doRequest(new JsonClientHandler2() {
					@Override
					public void onInterfaceSuccess(String message,
							String contentJson) {
						LogUtilNIU.value("得到" + interfaceType
								+ "别下的ID的Json为---->" + contentJson);
						try {
							JSONArray arry = new JSONArray(contentJson);
							for (int i = 0; i < arry.length(); i++) {
								SingleDevice singleDevice = new SingleDevice();
								JSONObject obj = arry.getJSONObject(i);
								if (obj.has("position")
										&& !obj.isNull("position")) {
									singleDevice.setDeviceLocation(obj
											.getString("position"));
								} else {
									singleDevice.setDeviceLocation("");
								}

								if (obj.has("id") && !obj.isNull("id")) {
									singleDevice.setDeviceID(obj
											.getString("id"));
								} else {
									singleDevice.setDeviceID("");
								}

								if (obj.has("sort") && !obj.isNull("sort")) {
									singleDevice.setDeviceType(obj
											.getString("sort"));
								} else {
									singleDevice.setDeviceType("");
								}
								if (obj.has("brand") && !obj.isNull("brand")) {
									singleDevice.setDeviceBrand(obj
											.getString("brand"));
								} else {
									singleDevice.setDeviceBrand("");
								}

								if (obj.has("name") && !obj.isNull("name")) {
									singleDevice.setDeviceName(obj
											.getString("name"));
								} else {
									singleDevice.setDeviceName("");
								}
								if (obj.has("appliancesort")
										&& !obj.isNull("appliancesort")) {
									singleDevice.setDeviceAppType(obj
											.getString("appliancesort"));
								} else {
									singleDevice.setDeviceAppType("");
								}

								if (obj.has("wifibssid")
										&& !obj.isNull("wifibssid")) {
									singleDevice.setDeviceWIFIBSSID(obj
											.getString("wifibssid"));
								} else {
									singleDevice.setDeviceWIFIBSSID("");
								}
								deviceList.add(singleDevice);
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
						adapter.notifyDataSetChanged();
						// String messages =
						// ModbusCalUtil.combindMsgs(deviceList);//
						// 指令们
						// 先更新为所有都不在线
						// 查询在线状态
						checkOnlineStatus();
					}

					@Override
					public void onInterfaceFail(String json) {
						progressGettingDataDismiss("查询失败");
					}

					@Override
					public void onFailureConnected(Boolean canConnect) {
						progressGettingDataDismiss("网络异常，查询失败");
					}
				});
	}

	public class UDPBrocastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 在线列表查询设备继电器状态，如果在局域网内有查询到数据，代表在家
			BApplication.instance.setIsAtHome(true);
			// 区分接收到的是哪种广播
			String action = intent.getAction();
			if (action.equals(Constant.SOCKET_BROCAST_ONRECEIVED)) {
				String message = intent
						.getStringExtra(Constant.SOCKET_BROCAST_EXTRA_MESSAGE);
				showContent(message);// 收到UDP广播，解析数据
			} else if (action
					.equals(Constant.BROCAST_ONLINE_LIST_SHOW_OPEN_DIALOG)) {
				// 正在开启
				progressGettingDataShow("正在打开");
			} else if (action
					.equals(Constant.BROCAST_ONLINE_LIST_DIMISS_DIALOG)) {
				progressGettingDataDismissNoToast();// 超时或设置成功
				LogUtilNIU.value("收到要求转圈消失的广播！！！！！！！！！！！！！！！！");
			} else if (action
					.equals(Constant.BROCAST_ONLINE_LIST_SHOW_CLOSE_DIALOG)) {
				// 正在关闭
				progressGettingDataShow("正在关闭");
			} else if (action.equals(Constant.BROCAST_REFRESHING0002_DIALOG)) {
				// 正在关闭
				progressGettingDataShow("正在刷新状态");
			}
		}
	}

	/**
	 * 
	 * @Description:改变设备列表设备的在线状态显示
	 */
	protected void changeOnlineIDStatus(String msg) {
		for (int j = 0; j < deviceList.size(); j++) {
			SingleDevice a = new SingleDevice();
			String msg_id = msg.substring(2, 14);
			a = deviceList.get(j);
			String id = a.getDeviceID();
			if (msg_id.equals(id)) {
				// 设备在线
				a.setIsOnline(true);
				// 设置继电器状态
				String status = msg.substring(24, 26);
				if (status.equals("01")) {// 红色，断开
					a.setIsOpened(false);
				} else if (status.equals("00")) {// 绿色，闭合
					a.setIsOpened(true);
				}
			} else {
				// 该设备不在线
				a.setIsOnline(false);
				a.setIsOpened(false);
			}
		}
		adapter.notifyDataSetChanged();
	}

	/**
	 * 
	 * @Description:设置所有设备不在线和继电器关
	 */
	protected void setAllOffLine() {
		for (int i = 0; i < deviceList.size(); i++) {
			deviceList.get(i).setIsOnline(false);
			deviceList.get(i).setIsOpened(false);
			deviceList.get(i).setJidianqi1Open(false);
			deviceList.get(i).setJidianqi2Open(false);
			deviceList.get(i).setJidianqi3Open(false);
		}
	}

	// 查询在线用电类型信息 就是刷新在线状态和继电器状态 TODO
	public void checkOnlineStatus() {
		LogUtilNIU.value("在线状况查询了！！！！！！！！！！！！！！！！！！！！！！");
		setAllOffLine();// 把所有设备设置为离线
		String verCode = ModbusCalUtil.verNumber("13570856740" + "10010011");
		final String message = "3A" + "10010011" + "13570856740" 
				+ "0D";// 0001
		BApplication.instance.socketSend(message);
		receiveThread = new ReceiveThread();
		receiveThread.start();

		// new
		// Enterface("getOnLiveDevicesWithStatus.act").addParam(interfaceType,
		// thisType)
		// .doRequest(new JsonClientHandler2() {
		//
		// @Override
		// public void onInterfaceFail(String json) {
		// super.onInterfaceFail(json);
		// if (isRefreshing) {
		// isRefreshing = false;
		// refreshLayout.finishRefresh();
		// }
		// }
		//
		// @Override
		// public void onInterfaceSuccess(String message, String contentJson) {
		// //
		// // "content": "681607220002,681607220003"
		// if (isRefreshing) {
		// isRefreshing = false;
		// refreshLayout.finishRefresh();
		// ToastUtils.shortToast(OnlineStatusActivity.this, "刷新成功");
		// }
		// progressGettingDataDismissNoToast();
		// LogUtilNIU
		// .value("************得到用电类型*************" + thisType +
		// "在线信息的Json为---->" + contentJson);
		//
		// if (contentJson.equals("")) {
		// // ToastUtils.shortToast(OnlineStatusActivity.this,
		// // "当前没有在线设备");
		// } else {
		// String[] msgs = contentJson.split(",");
		// LogUtilNIU.value("包含逗号" + Arrays.toString(msgs));
		// for (int i = 0; i < msgs.length; i++) {
		// String msg = msgs[i];// 获得单个返回信息
		// LogUtilNIU.value("****被处理的指令为****" + msg);
		// // 处理在线状况的变更
		// dealJidianqiStatus(msg);
		// // checkCharAndDealList(msg);//这个是旧的解析方法
		// }
		// }
		// }
		//
		// @Override
		// public void onFailureConnected(Boolean canConnect) {
		// // TODO Auto-generated method stub
		// LogUtilNIU.value("在线状况！！！！！！！！！！！！！！！！！！！！！！");
		// if (isRefreshing) {
		// isRefreshing = false;
		// refreshLayout.finishRefresh();
		// }
		// }
		// }, true);

	}

	// 新的处理设备在线状况的方式 这里才是新的查在线方案的处理继电器的方法
	protected void dealJidianqiStatus(String message) {
		if (isRefreshing) {
			refreshLayout.finishRefresh();
			LogUtilNIU.value("停止刷新了************************");
			ToastUtils.shortToast(OnlineStatusActivity.this, "刷新成功");
			isRefreshing = false;
		}
		LogUtilNIU.value("*********处理在线指令************" + message);
		LogUtilNIU.online("*********处理在线指令************" + message);
		String mID = message.substring(0, 12);// 指令id
		// 从指令解析到的继电器状态 00 01 或其他
		String jidianstatus = message.substring(12, 14);
		String powerstatus = "";
		if (message.length() == 16) {
			powerstatus = message.substring(14); // TODO
		} else {
			LogUtilNIU.value("设备状态为默认的00值");
			powerstatus = "00";
		}
		for (int i = 0; i < deviceList.size(); i++) {
			SingleDevice d = new SingleDevice();
			d = deviceList.get(i);
			String id = d.getDeviceID();// 列表id
			if (id.equals(mID)) {
				LogUtilNIU.value("设备" + id + "在线");
				d.setIsOnline(true);
				// 设置继电器状态。继电器分为开关和插座，开关有三个继电器，插座只有一个继电器
				// 所以开关要设置3个继电器的状态
				// 插座只需要设置一个继电器的状态
				// 首先判断改设备是插座还是开关
				if (id.substring(0, 2).equals("68")) {// 插座
					// 更新继电器的开和关状态
					if (jidianstatus.equals("00")) {
						d.setIsOpened(true);
					} else if (jidianstatus.equals("01")) {
						d.setIsOpened(false);
					}
					// 设置用电状态（adapter里面有算法，会根据此设置来显示）
					d.setDevicePowerStatus(powerstatus);
				} else if (id.substring(0, 2).equals("69")) {// 开关
					// 解析jidianstatus，知道三个继电器的状态
					// 把16进制化为2进制的形式
					String binaryString = ModbusCalUtil
							.hexStringToBinaryString(jidianstatus);
					LogUtilNIU.value("16进制表示的开关继电器状态为" + jidianstatus
							+ ",转为2进制表示为" + binaryString);
					// 从2进制形式得到开关的三个继电器的状态，并在activity修改开关对象的状态
					// 16进制表示的开关继电器状态为86,转为2进制表示为10000110
					String jidianqi1 = binaryString.substring(7);
					String jidianqi2 = binaryString.substring(6, 7);
					String jidianqi3 = binaryString.substring(5, 6);
					LogUtilNIU.value("继电器1" + jidianqi1 + "继电器2" + jidianqi2
							+ "继电器3" + jidianqi3);
					// 10000011
					// 解析开关的额3个继电器的状态
					if (jidianqi1.equals("0")) {
						d.setJidianqi1Open(true);
					} else {
						d.setJidianqi1Open(false);
					}
					if (jidianqi2.equals("0")) {
						d.setJidianqi2Open(true);
					} else {
						d.setJidianqi2Open(false);
					}
					if (jidianqi3.equals("0")) {
						d.setJidianqi3Open(true);
					} else {
						d.setJidianqi3Open(false);
					}
				}
			}
		}

		adapter.notifyDataSetChanged();

	}

	/**
	 * 
	 * @Description:对于单个数据时，设置在线状态 t为从服务器收到单个透传消息
	 * 
	 *                             3a69322857221100000200020100520d
	 * 
	 * 
	 *                             此方法不在用在刷新整个在线状态列表，不过用在刷新单个设备0002状态
	 */
	public void reviceServiceBackSingleDevice0002(String t) {
		showContent(t);
	}

	// 3A68160805009500000200020100250D 插座
	// 3A69322857931100000200028500470D 开关
	// 收到广播时对数据的处理
	protected void showContent(String message) { // TODO
		LogUtilNIU.value("activity收到UDP数据" + message);
		/***************************************************/
		if (isReqCodeEqual(message, "0002")) {// 查询继电器状态的指令返回结果
			// 提取返回的表地址
			BApplication.instance.setResendTaskShowBreak(true);
			LogUtilNIU.value("activityUDP处理，判断为0002继电器状态返回，结果为" + message);
			if (isRefreshing) {
				refreshLayout.finishRefresh();
				LogUtilNIU.value("停止刷新了************************");
				ToastUtils.shortToast(OnlineStatusActivity.this, "刷新成功");
				isRefreshing = false;
			}
			String resuldCode = message.substring(14, 16);
			if (resuldCode.equals("00")) {// 接收数据正确
				// 处理0002指令成功状态下的信息
				deal0002Data(message);// 以前的判断全部设备是否在线的方式
				// 更新单个设备的继电器状态，在设备继电器被控制后，会执行这步操作
			} else {
				LogUtilNIU.e("传过去的参数有误，结果码非00");
			}
		} else if (isReqCodeEqual(message, "0013")) {
			BApplication.instance.setResendTaskShowBreak(true);
			LogUtilNIU.value("activityUDP处理，判断为0013，指令" + message);
			adapter.onServiceUDPBack(message);
		}
	}

	private void deal0002Data(String t) {
		LogUtilNIU.value("*********单个设备继电器状态更新************" + t);
		LogUtilNIU.online("*********单个设备继电器状态更新************" + t);
		String mID = t.substring(2, 14);// 指令id
		for (int i = 0; i < deviceList.size(); i++) {
			SingleDevice d = new SingleDevice();
			d = deviceList.get(i);
			String id = d.getDeviceID();// 列表id
			String jidianstatus = t.substring(24, 26);// 继电器状态
			// 插座用电3种状态（开关不解析此项）
			String powerstatus = t.substring(26, 28);
			// 00为开，01为关
			if (id.equals(mID)) {
				LogUtilNIU.value("设备" + id + "在线");
				d.setIsOnline(true);
				// 设置继电器状态。继电器分为开关和插座，开关有三个继电器，插座只有一个继电器
				// 所以开关要设置3个继电器的状态
				// 插座只需要设置一个继电器的状态
				// 首先判断改设备是插座还是开关
				if (id.substring(0, 2).equals("68")) {// 插座
					// 更新继电器的开和关状态
					if (jidianstatus.equals("00")) {
						d.setIsOpened(true);
					} else if (jidianstatus.equals("01")) {
						d.setIsOpened(false);
					}
					// 设置用电状态（adapter里面有算法，会根据此设置来显示）
					d.setDevicePowerStatus(powerstatus);
				} else if (id.substring(0, 2).equals("69")) {// 开关
					// 解析jidianstatus，知道三个继电器的状态
					// 把16进制化为2进制的形式
					String binaryString = ModbusCalUtil
							.hexStringToBinaryString(jidianstatus);
					LogUtilNIU.value("16进制表示的开关继电器状态为" + jidianstatus
							+ ",转为2进制表示为" + binaryString);
					// 从2进制形式得到开关的三个继电器的状态，并在activity修改开关对象的状态
					String jidianqi1 = binaryString.substring(7);
					String jidianqi2 = binaryString.substring(6, 7);
					String jidianqi3 = binaryString.substring(5, 6);
					// 解析开关的额3个继电器的状态
					if (jidianqi1.equals("0")) {
						d.setJidianqi1Open(true);
					} else {
						d.setJidianqi1Open(false);
					}
					if (jidianqi2.equals("0")) {
						d.setJidianqi2Open(true);
					} else {
						d.setJidianqi2Open(false);
					}
					if (jidianqi3.equals("0")) {
						d.setJidianqi3Open(true);
					} else {
						d.setJidianqi3Open(false);
					}
				}
			}
		}
		// 更新列表
		adapter.notifyDataSetChanged();
	}

	private Handler defalutHandler = new Handler() {
		public void handleMessage(Message msg) {
			progressGettingDataDismissNoToast();
			switch (msg.what) {
			case Constant.MSG_SUCCESS:
				adapter.notifyDataSetChanged();
				break;
			case Constant.MSG_FAILURE:
				toast((String) msg.obj);
				break;
			case Constant.MSG_EXCPTION:
				toast(ExceptionManager.getErrorDesc(OnlineStatusActivity.this,
						(Exception) msg.obj));
				break;
			case Constant.MSG_TIME_OUT:
				toast(getString(R.string.connection_timeout));
				break;
			case Constant.MSG_NETWORK_ERROR:
				toast(getString(R.string.no_connection));
				break;
			}
		}

		;
	};

	private void toast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_LONG).show();
	}

	private Runnable defalutTimeout = new Runnable() {
		@Override
		public void run() {
			defalutHandler.obtainMessage(Constant.MSG_TIME_OUT).sendToTarget();
		}
	};

	// 获取设备列表
	private void getCheckThisTypeList(final String PosName,
			final String LoadName) {
		if (NetworkUtils.isNetworkConnected(this)) {
			new Thread() {
				public void run() {
					Message msg = new Message();
					msg.what = Constant.MSG_FAILURE;
					msg.obj = getString(R.string.cannot_connection_server);
					try {
						Map map = new HashMap<>();
						if (TextUtils.isEmpty(PosName)) {
							map.put("LoadName", LoadName);
						}
						if (TextUtils.isEmpty(LoadName)) {
							map.put("PosName", PosName);
						}
						
						String result = WebServiceClient.CallWebService(
								"GetUserDeviceList", map);
						Log.d("resultLogin", result);
						if (result != null) {
							JSONObject obj = new JSONObject(result);
							int code = obj.getInt("statusCode");
							if (code == 0) {
								JSONArray array = obj.getJSONArray("content");
								for (int i = 0; i < array.length(); i++) {
									JSONObject jsonObject = array
											.getJSONObject(i);
									SingleDevice singleDevice = new SingleDevice();
									singleDevice.setIsOnline(true);
									singleDevice.setDeviceLocation(jsonObject
											.getString("position"));
									singleDevice.setDeviceBrand(jsonObject
											.getString("brand"));
									singleDevice.setDeviceID(jsonObject
											.getString("id"));
									singleDevice.setDeviceName(jsonObject
											.getString("name"));
//									singleDevice.setDeviceWIFIBSSID(jsonObject
//											.getString("wifibssid"));
									singleDevice.setDeviceType(jsonObject
											.getString("sort"));
									deviceList.add(singleDevice);
								}
								msg.what = Constant.MSG_SUCCESS;
							} else {
								msg.obj = obj.getString("message");
								msg.what = Constant.MSG_FAILURE;
							}
						}
						checkOnlineStatus();
					} catch (Exception e) {
						e.printStackTrace();
						msg.obj = e;
						msg.what = Constant.MSG_EXCPTION;
					}
					// if (isRequesting) {
					defalutHandler.sendMessage(msg);
					// }
				}
			}.start();
		} else {
			defalutHandler.sendEmptyMessage(Constant.MSG_NETWORK_ERROR);
		}
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
			if (result != null) {
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
						} else{
							msg.what = Constant.MSG_FAILURE;
						}
					}
				} catch (Exception e) {
					Log.e("Receive", e.getMessage());
					e.printStackTrace();
					msg.obj = e;
					msg.what = Constant.MSG_EXCPTION;
				}
			} else {
				try {
					Thread.sleep(1000L);
				} catch (InterruptedException e) {
					e.printStackTrace();
					msg.obj = e;
					msg.what = Constant.MSG_EXCPTION;
				}
			}
			h.sendMessage(msg);
		}
	}

}
