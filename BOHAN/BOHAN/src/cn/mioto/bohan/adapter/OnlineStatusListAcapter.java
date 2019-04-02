package cn.mioto.bohan.adapter;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import cn.mioto.bohan.BApplication;
import cn.mioto.bohan.Constant;
import cn.mioto.bohan.R;
import cn.mioto.bohan.activity.MenuDeviceInfoMenuActivity;
import cn.mioto.bohan.activity.OffLineDeviceRemindActivity;
import cn.mioto.bohan.activity.OnlineStatusActivity;
import cn.mioto.bohan.entity.SingleDevice;
import cn.mioto.bohan.utils.Enterface;
import cn.mioto.bohan.utils.LoadDataThreadUtil;
import cn.mioto.bohan.utils.LogUtilNIU;
import cn.mioto.bohan.utils.ModbusCalUtil;
import cn.mioto.bohan.utils.ToastUtils;
import cn.mioto.bohan.view.SwitchViewLongOnline;
import cn.mioto.bohan.view.SwitchViewLongOnline.OnStateChangedListener;


import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.storage.OnObbStateChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import steed.framework.android.client.JsonClientHandler2;

/**
 * 类说明：设备在线列表的adapter
 * 
 */
public class OnlineStatusListAcapter extends BaseAdapter {
	// 当开关在被控制开启或关闭继电器的时候，判断那3个开关是哪一个被控制
	private int kaiguanPositionControling;
	List<SingleDevice> deviceList;// 用户名下的所有设备信息
	int ListbyWhat;// 传进来的参数，表示按什么排列
	// listview里面，Item布局的类型。现在针对插座和开关，分别有两种布局
	private final int TYPE_K = 0, TYPE_C = 1;
	Context context;
	LayoutInflater inflater;
	private Animation animation;
	// 正在不控制的那个布局
	private View currentSettingView;
	// 被点击的那个位置
	private int clickPosition;
	// 上次控制时发送出去的指令（如果控制成功，这次的初始指令就要设置为这个）
	private String lastControlBinaryMessage;
	
	
	public static Socket socket;
	public static BufferedReader in;
	public static PrintWriter out;
	public ReceiveThread receiveThread;
	private Handler handler;
	
	
	public Handler h = new Handler() {// 处理返回的消息
		public void handleMessage(android.os.Message msg) {
			if (msg.what == Constant.MSG_WHAT_DISMISS_DIALOG_OF_RESEND_TASK) {
				// 加载到数据后正在加载消失
			} else if (msg.what == Constant.MSG_WHAT_SHOW_DIALOG_OF_RESEND_TASK) {

			} else if (msg.what == Constant.MSG_WHAT_DISMISS_DIALOG_OF_RESEND_TASK_FAIL) {
				// 转呀转消失
				Intent intent = new Intent();
				intent.setAction(Constant.BROCAST_ONLINE_LIST_DIMISS_DIALOG);
				context.sendBroadcast(intent);
			} else if (msg.what == Constant.MSG_WHAT_SEND_MSG_BY_SERVICE_RESEND_TASK_FAIL) {
				// 通过服务器去查询信息
				// 通过服务器查询设备实时数据
				// 发广播要求从服务器查询数据
				LogUtilNIU.value("********adapter收到要求用服务器发送" + ((String) msg.obj).substring(14, 18) + "指令号的命令,\n命令内容为"
						+ (String) msg.obj);
				LogUtilNIU.web("********adapter收到要求用服务器发送" + ((String) msg.obj).substring(14, 18) + "指令号的命令,\n命令内容为"
						+ (String) msg.obj);
				//checkByService((String) msg.obj);
			} else if(msg.what == Constant.MSG_SUCCESS){
				dismissDialog();
				String message = (String) msg.obj;
				onServiceUDPBack(message);
			} else if(msg.what == Constant.MSG_EXCPTION){
				dismissDialog();
				ToastUtils.shortToast(context,context.getString(R.string.no_connection));
			}
		};
	};

	/**
	 * setOKRefleshView 供外部调用的方 接收到设置成功的广播后，view改变
	 * 
	 * 开关和插座都用这个方法
	 * 
	 * controlState代表是否控制成功
	 */
	private void setOKRefleshView(Boolean isControlSus) {
		// 接收成功，转呀转消失
		Intent intent = new Intent();
		intent.setAction(Constant.BROCAST_ONLINE_LIST_DIMISS_DIALOG);
		context.sendBroadcast(intent);
		// 这里要判断一下是哪个开关

		// 通过改变对象状态，再刷新adapter来改变
		String deviceType = deviceList.get(clickPosition).getDeviceID().substring(0, 2);
		SingleDevice device = deviceList.get(clickPosition);

		if(isControlSus){
			if (deviceType.equals("68")) {
				if (device.getIsOpened()) {
					device.setIsOpened(false);
				} else {
					device.setIsOpened(true);
				}
			} else if (deviceType.equals("69")) {
				
				// 判断被控制的继电器是第几位
				// kaiguanPositionControling 被控制的开关是哪个，1，2，3
				LogUtilNIU.value("********************刷新状态，当前被控制的kaiguanPositionControling是"+kaiguanPositionControling);
				switch (kaiguanPositionControling) {
				case 1:
					
					if (device.getJidianqi1Open()) {
						LogUtilNIU.value("1关********");
						device.setJidianqi1Open(false);
					} else if(!device.getJidianqi1Open()){
						LogUtilNIU.value("1开********");
						device.setJidianqi1Open(true);
					}
					break;
				case 2:
					
					if (device.getJidianqi2Open()) {
						LogUtilNIU.value("2关********");
						device.setJidianqi2Open(false);
					} else if(!device.getJidianqi2Open()){
						LogUtilNIU.value("1开********");
						device.setJidianqi2Open(true);
					}
					break;
				case 3:
			
					if (device.getJidianqi3Open()) {
						LogUtilNIU.value("3关********");
						device.setJidianqi3Open(false);
					} else if(!device.getJidianqi3Open()){
						LogUtilNIU.value("1开********");
						device.setJidianqi3Open(true);
					}
					break;
				}
			}
			notifyDataSetChanged();
		}else{
			
			LogUtilNIU.value("操作不成成功，有没有做任何事情");
			if (deviceType.equals("68")) {
				ViewHolderC viewHolderc =(ViewHolderC) currentSettingView.getTag();	
				if (device.getIsOpened()) {
					device.setIsOpened(true);
					viewHolderc.sv.toggleSwitch(true);
				} else {
					device.setIsOpened(false);
					viewHolderc.sv.toggleSwitch(false);
				}
			} else if (deviceType.equals("69")) {
				ViewHolderK viewHolderk =(ViewHolderK) currentSettingView.getTag();
				// 判断被控制的继电器是第几位
				// kaiguanPositionControling 被控制的开关是哪个，1，2，3
				LogUtilNIU.value("***刷新状态，当前被控制的kaiguanPositionControling是"+kaiguanPositionControling);
				switch (kaiguanPositionControling) {
				case 1:
					
					if (device.getJidianqi1Open()) {
						LogUtilNIU.value("1开********");
						viewHolderk.swichView1.toggleSwitch(true);
						device.setJidianqi1Open(true);
					} else if(!device.getJidianqi1Open()){
						LogUtilNIU.value("1关********");
						viewHolderk.swichView1.toggleSwitch(false);
						device.setJidianqi1Open(false);
					}
					
					
					break;
				case 2:
					if (device.getJidianqi2Open()) {
						LogUtilNIU.value("2开********");
						viewHolderk.swichView2.toggleSwitch(true);
						device.setJidianqi2Open(true);
					} else if(!device.getJidianqi2Open()){
						LogUtilNIU.value("2关********");
						viewHolderk.swichView2.toggleSwitch(false);
						device.setJidianqi2Open(false);
					}
					break;
				case 3:
					if (device.getJidianqi3Open()) {
						LogUtilNIU.value("3开********");
						device.setJidianqi3Open(true);
						viewHolderk.swichView3.toggleSwitch(true);
					} else if(!device.getJidianqi3Open()){
						LogUtilNIU.value("3关********");
						viewHolderk.swichView3.toggleSwitch(false);
						device.setJidianqi3Open(false);
					}
					break;
				}
			}
			notifyDataSetChanged();
		
		}
		
		// // 设备的类型，68或69
		// String deviceType =
		// deviceList.get(clickPosition).getDeviceID().substring(0, 2);
		// SwitchViewLongOnline sv = null;
		// ImageView ivOpenStatus = null;
		// if (deviceType.equals("68")) {
		//
		//
		// ViewHolderC viewholderc= (ViewHolderC) currentSettingView.getTag();
		// sv = viewholderc.sv;
		// ivOpenStatus = viewholderc.ivOpenStatus;
		// } else if (deviceType.equals("69")) {
		// ViewHolderK viewholderk= (ViewHolderK) currentSettingView.getTag();
		// // 判断是哪个开关被控制
		// LogUtilNIU.value("判断被控制的为开关，得到开关的sv按钮对象");
		// switch (kaiguanPositionControling) {
		// case 1:
		// sv = viewholderk.swichView1;
		// ivOpenStatus = viewholderk.ivOpenStatus1;
		// break;
		// case 2:
		// sv = viewholderk.swichView2;
		// ivOpenStatus = viewholderk.ivOpenStatus2;
		// break;
		// case 3:
		// sv = viewholderk.swichView3;
		// ivOpenStatus = viewholderk.ivOpenStatus3;
		// break;
		// }
		// }
		//
		// if (isControlSus) {
		// LogUtilNIU.value("控制成功，改变按钮状态");
		// if (sv.isOpened()) {
		// sv.setOpened(false);// 设置为关闭
		// ivOpenStatus.setImageResource(R.drawable.open_close);
		// } else {
		// sv.setOpened(true);// 设置为打开
		// ivOpenStatus.setImageResource(R.drawable.open_open);
		// }
		// } else {
		// LogUtilNIU.value("控制失败，改变按钮状态");
		// if (sv.isOpened()) {
		// sv.setOpened(true);// 设置为关闭
		// ivOpenStatus.setImageResource(R.drawable.open_open);
		// } else {
		// sv.setOpened(false);// 设置为打开
		// ivOpenStatus.setImageResource(R.drawable.open_close);
		// }
		// }
	}

	// 控制继电器开关的那些指令，都是在这里直接调用服务器接口直接查
	protected void checkByService(final String content) {
		LogUtilNIU.value("开始用服务器去发送指令啦！！！！！！！！！调用服务器sendToDevice.act接口传递的参数有--->" + deviceList.get(clickPosition).getDeviceID() + "--" + content);
		new Enterface("sendToDevice.act").addParam("deviceid", deviceList.get(clickPosition).getDeviceID())
				.addParam("content", content).doRequest(new JsonClientHandler2() {

					@Override
					public void onInterfaceSuccess(String message, String contentJson) {
						LogUtilNIU.value("得到服务器查询的单个设备信息为---->" + contentJson);
						contentJson = contentJson.toUpperCase();
						onServiceUDPBack(contentJson);
					}

					@Override
					public void onInterfaceFail(String json) {
						LogUtilNIU.value("控制继电器开关的时候，服务器返回非0的失败" + json);
						// 这里包括两种指令的处理0002 和0013 的
						// 0013是控制处理
						if (content.substring(14, 18).equals("0002")) {
							// 0002是控制处理
							dismissDialog();
							on0012CheckSingleJidianqiFail(json);
						} else if (content.substring(14, 18).equals("0013")) {
							on0013ControlFail(json);
						}
					}

					@Override
					public void onFailureConnected(Boolean canConnect) {
						BApplication.instance.setResendTaskShowBreak(true);
						// 让按钮变回可点击
						BApplication.instance.setOpenSetCanSet(true);
						dismissDialog();
						// setOKRefleshView(false);
					}
				});
	}

	protected void on0012CheckSingleJidianqiFail(String json) {
		((OnlineStatusActivity) context).checkOnlineStatus();
	}

	protected void on0013ControlFail(String json) {
		ToastUtils.shortToast(context, "服务器返回提示" + json); // TODO
		LogUtilNIU.value("控制继电器开关的时候，服务器返回非0的失败" + json+"这里不做任何操作");
		// 重查线程停止
		ToastUtils.shortToast(context, "服务器返回提示" + json); // TODO
		BApplication.instance.setResendTaskShowBreak(true);
		// 让按钮变回可点击
		BApplication.instance.setOpenSetCanSet(true);
		// 找到正在被控制的按钮，改变一下状态
		// 转圈消失
		Intent intent = new Intent();
		intent.setAction(Constant.BROCAST_ONLINE_LIST_DIMISS_DIALOG);
		context.sendBroadcast(intent);
		
		// 方法1 通过查询在线状况，更新界面
		 setOKRefleshView(false);
//		Handler handler = new Handler();
//		handler.postDelayed(new Runnable() {
//
//			@Override
//			public void run() {
//				// update0002ForThisDevice(String.valueOf(clickPosition));
//				((OnlineStatusActivity) context).checkOnlineStatus();
//			}
//		}, 3000);
	}

	/**
	 * 
	 * @Description:
	 * 
	 * 				1、在刷新整个在线列表在线情况下，处理服务器返回的控制继电器开和关的控制数据的返回命令
	 * 
	 *               处理包括服务器返回的数据和UDP返回的数据
	 * 
	 *               2、针对单个设备的继电器状态发生变化，处理单个设备从服务器返回来的0002指令
	 * 
	 */
	public void onServiceUDPBack(String message) {
		// 3A681608050093000013000100320D
		if (ModbusCalUtil.isReqCodeEqual(message, "0013")) {
			LogUtilNIU.value("处理继电器开关结果" + message);
			// 设置继电器开或继电器器关的指令的成功与否结果
			String resuldCode = message.substring(14, 16);
			if (resuldCode.equals("00")) {// 错误标志
				// 3A XXXXXXXXXXXX 00 0013 0001 00 99 OD
				LogUtilNIU.value("设置后返回结果");
				String id = message.substring(2, 14);// 获得12位表地址
				if (message.substring(24, 26).equals("00")) {// 数据块结果
					BApplication.instance.setOpenSetCanSet(true);
					// 重查线程停止
					BApplication.instance.setResendTaskShowBreak(true);
					// 3A681607220002 00 0013 0001 00 BD0D
					LogUtilNIU.value("设置后返回结果数据块00");
					ToastUtils.shortToast(context, "操作成功");// TODO
					// 转圈消失
					dismissDialog();
					// update0002ForThisDevice(String.valueOf(clickPosition));
					setOKRefleshView(true);
					// // this.setOKRefleshView(true);// 方法2 不重新查询，自己根据成功，修改界面
					//
					// // 方法1 通过查询在线状况，更新界面
					// Handler handler = new Handler();
					// handler.postDelayed(new Runnable() {
					//
					// @Override
					// public void run() {
					// //
					// ((OnlineStatusActivity) context).checkOnlineStatus();
					// }
					// }, Constant.REFRESH_ONLINE);

				} else {
					// this.setOKRefleshView(false);
					LogUtilNIU.value("设置后返回结果数据块非00，操作不成功");
					ToastUtils.shortToast(context, "操作失败");// TODO
					Intent intent = new Intent();
					intent.setAction(Constant.BROCAST_ONLINE_LIST_DIMISS_DIALOG);
					context.sendBroadcast(intent);
					// 马上查询单个设备的继电器状况
					setOKRefleshView(false);
					// update0002ForThisDevice(String.valueOf(clickPosition));
					// Handler handler = new Handler();
					// handler.postDelayed(new Runnable() {
					//
					// @Override
					// public void run() {
					// //
					// ((OnlineStatusActivity) context).checkOnlineStatus();
					// }
					// }, Constant.REFRESH_ONLINE);
				}
			} else if (message.substring(24, 26).equals("01")) {
				ToastUtils.testToast(context, "数据超出值域");
			} else if (message.substring(24, 26).equals("02")) {
				ToastUtils.testToast(context, "写FLASH出错");
			}
		} else if (ModbusCalUtil.isReqCodeEqual(message, "0002")) {
			// 服务器返回的是0002返回帧
			// 说明是控制了单个继电器，更新单个设备的继电器状态
			// dialog消失
			dismissDialog();
			((OnlineStatusActivity) context).reviceServiceBackSingleDevice0002(message);
		} else {
			// MCU模块发送错误，返回非0013指令
			LogUtilNIU.value("收到返回非0013,0002指令");
			this.setOKRefleshView(false);
			Intent intent = new Intent();
			intent.setAction(Constant.BROCAST_ONLINE_LIST_DIMISS_DIALOG);
			context.sendBroadcast(intent);
			ToastUtils.testToast(context, "收到返回非0013或0002的错误指令，退出此界面再进入继续测试");
			// 重查线程停止
			BApplication.instance.setResendTaskShowBreak(true);
			// 让按钮变回可点击
			BApplication.instance.setOpenSetCanSet(true);
		}
	}

	// 0002 发送查询当前设备的继电器状态的0002指令
	public void update0002ForThisDevice(String itemPosition) {
		// 显示dialog
		showRefresh0002Dialog();
		String deviceId = deviceList.get(Integer.valueOf(itemPosition)).getDeviceID();
		String verCode = ModbusCalUtil.verNumber(deviceId + "00020000");
		String msg = "E7" + deviceId + "00020000" + verCode + "0D";// 查询继电器开关状态
		String deviceDBssid = deviceList.get(Integer.valueOf(itemPosition)).getDeviceWIFIBSSID();
		//new LoadDataThreadUtil(msg, h, deviceDBssid, context).start();
		BApplication.instance.socketSend(msg);
		receiveThread = new ReceiveThread();
		receiveThread.start();
	}

	public OnlineStatusListAcapter(Context context, List<SingleDevice> datas,Handler handler) {
		// 初始化Socket
		this.handler = handler;
		inflater = LayoutInflater.from(context);
		this.context = context;
		if (datas != null) {
			this.deviceList = datas;
		} else {
			datas = new ArrayList<SingleDevice>();
			this.deviceList = datas;
		}
	}

	@Override
	public int getCount() {
		return deviceList.size();
	}

	@Override
	public Object getItem(int position) {
		return deviceList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	/** 该方法返回多少个不同的布局 */
	@Override
	public int getViewTypeCount() {
		return 2;
	}

	// 判断要显示那个Item作为Listview的item
	@Override
	public int getItemViewType(int position) {
		if (deviceList.get(position).getDeviceType().equals("K")) {
			return TYPE_K;
		} else {
			return TYPE_C;
		}
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolderC viewHolderC = null;
		ViewHolderK viewHolderK = null;
		// 判断是插座还是开关，显示相应的布局
		final SingleDevice singleDevice = deviceList.get(position);
		switch (getItemViewType(position)) {
		case TYPE_K:
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.item_listview_device_online_k, null);
				viewHolderK = new ViewHolderK();
				viewHolderK.tvName = (TextView) convertView.findViewById(R.id.tvName);
				viewHolderK.tvLocation = (TextView) convertView.findViewById(R.id.tvLocation);
				viewHolderK.tvType = (TextView) convertView.findViewById(R.id.tvType);
				viewHolderK.tvNetStatus = (TextView) convertView.findViewById(R.id.tvNetStatus);
				viewHolderK.ivDeviceType = (ImageView) convertView.findViewById(R.id.ivType);
				viewHolderK.ivLocation = (ImageView) convertView.findViewById(R.id.ivLocation);
				viewHolderK.ivWifiStatus = (ImageView) convertView.findViewById(R.id.imageViewWifiOn);
				viewHolderK.llUp = (LinearLayout) convertView.findViewById(R.id.llUp);
				viewHolderK.swichView1 = (SwitchViewLongOnline) convertView.findViewById(R.id.switchView1);
				viewHolderK.swichView2 = (SwitchViewLongOnline) convertView.findViewById(R.id.switchView2);
				viewHolderK.swichView3 = (SwitchViewLongOnline) convertView.findViewById(R.id.switchView3);
				viewHolderK.ivOpenStatus1 = (ImageView) convertView.findViewById(R.id.imageViewStatus1);
				viewHolderK.ivOpenStatus2 = (ImageView) convertView.findViewById(R.id.imageViewStatus2);
				viewHolderK.ivOpenStatus3 = (ImageView) convertView.findViewById(R.id.imageViewStatus3);
				convertView.setTag(viewHolderK);
				LogUtilNIU.value("*****开关自创******");
			} else {
				viewHolderK = (ViewHolderK) convertView.getTag();
				LogUtilNIU.value("*****开关gettag执行******");
			}
			viewHolderK.tvName.setText(singleDevice.getDeviceName());
			viewHolderK.tvLocation.setText(singleDevice.getDeviceLocation());
			if (singleDevice.getIsOnline()) {
				// 3个switchview在在线情况下，按钮关状态，设置为红色
				viewHolderK.swichView1.setCloseColorRed();
				viewHolderK.swichView1.invalidate();// 刷新颜色
				viewHolderK.swichView2.setCloseColorRed();
				viewHolderK.swichView2.invalidate();// 刷新颜色
				viewHolderK.swichView3.setCloseColorRed();
				viewHolderK.swichView3.invalidate();// 刷新颜色
				// 设备WIFI在线
				viewHolderK.tvNetStatus.setText(context.getResources().getString(R.string.online));
				viewHolderK.ivWifiStatus.setImageResource(R.drawable.device_list_wifi_on);
				// 设备类型
				viewHolderK.tvType.setText(context.getResources().getString(R.string.switch2));// 开关在线
				viewHolderK.ivDeviceType.setImageResource(R.drawable.device_list_button);
				// 设备位置图标有颜色
				viewHolderK.ivLocation.setImageResource(R.drawable.device_list_location);
				// 继电器1
				if (singleDevice.getJidianqi1Open()) {
					// 开
					if (!viewHolderK.swichView1.isOpened()) {
						viewHolderK.swichView1.setOpened(true);
					}
					viewHolderK.ivOpenStatus1.setImageResource(R.drawable.open_open);
					LogUtilNIU.value("继电器1开+绿色");
				} else {
					// 关
					if (viewHolderK.swichView1.isOpened()) {
						viewHolderK.swichView1.setOpened(false);
					}
					viewHolderK.ivOpenStatus1.setImageResource(R.drawable.open_close);
					LogUtilNIU.value("继电器1关+红色");
				}

				// 继电器2
				if (singleDevice.getJidianqi2Open()) {
					// 开
					if (!viewHolderK.swichView2.isOpened()) {
						viewHolderK.swichView2.setOpened(true);
					}
					viewHolderK.ivOpenStatus2.setImageResource(R.drawable.open_open);
					LogUtilNIU.value("继电器2开+绿色");
				} else {
					// 关
					if (viewHolderK.swichView2.isOpened()) {
						viewHolderK.swichView2.setOpened(false);
					}
					viewHolderK.ivOpenStatus2.setImageResource(R.drawable.open_close);
					LogUtilNIU.value("继电器2关+红色");
				}

				// 继电器3
				if (singleDevice.getJidianqi3Open()) {
					// 开
					if (!viewHolderK.swichView3.isOpened()) {
						viewHolderK.swichView3.setOpened(true);
					}
					viewHolderK.ivOpenStatus3.setImageResource(R.drawable.open_open);
					LogUtilNIU.value("继电器3开+绿色");
				} else {
					// 关
					if (viewHolderK.swichView3.isOpened()) {
						viewHolderK.swichView3.setOpened(false);
					}
					viewHolderK.ivOpenStatus3.setImageResource(R.drawable.open_close);
					LogUtilNIU.value("继电器3关+红色");
				}
			} else {
				// 设备不在线
				// 3个switchview离线情况下，按钮关状态，设置为无色
				viewHolderK.swichView1.setCloseColorDark();
				viewHolderK.swichView1.invalidate();// 刷新颜色
				viewHolderK.swichView2.setCloseColorDark();
				viewHolderK.swichView2.invalidate();// 刷新颜色
				viewHolderK.swichView2.setCloseColorDark();
				viewHolderK.swichView2.invalidate();// 刷新颜色
				// 设备WIFI不在线
				viewHolderK.tvNetStatus.setText(context.getResources().getString(R.string.offline));
				viewHolderK.ivWifiStatus.setImageResource(R.drawable.device_list_wifi_no);
				// 设备类型
				viewHolderK.tvType.setText(context.getResources().getString(R.string.switch2));
				viewHolderK.ivDeviceType.setImageResource(R.drawable.device_list_button_no);
				// 设备位置设置为无色
				viewHolderK.ivLocation.setImageResource(R.drawable.device_list_location_no);
				// 3个继电器开关，设置为关闭状态
				if (viewHolderK.swichView1.isOpened()) {
					viewHolderK.swichView1.setOpened(false);
				}
				viewHolderK.ivOpenStatus1.setImageResource(R.drawable.open_offline);
				if (viewHolderK.swichView2.isOpened()) {
					viewHolderK.swichView2.setOpened(false);
				}
				viewHolderK.ivOpenStatus2.setImageResource(R.drawable.open_offline);
				if (viewHolderK.swichView3.isOpened()) {
					viewHolderK.swichView3.setOpened(false);
				}
				viewHolderK.ivOpenStatus3.setImageResource(R.drawable.open_offline);
			}

			/***************************************************/
			// 设置上区域点击 跳转
			// LinearLayout llUp= viewHolderC.llUp;
			viewHolderK.llUp.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (!deviceList.get(position).getIsOnline()) {// 如果设备不在线
						// //设备离线
						// //去提示离线的页面
						Intent intent = new Intent(context, OffLineDeviceRemindActivity.class);
						// 设置当前设备
						BApplication.instance.setCurrentDevice(deviceList.get(position));
						context.startActivity(intent);
					} else {
						// 设备在线
						// 去查询设备信息的界面
						Intent intent = new Intent(context, MenuDeviceInfoMenuActivity.class);
						// 设置当前设备
						BApplication.instance.setCurrentDevice(deviceList.get(position));
						context.startActivity(intent);
					}
				}
			});
			/***************************************************/
			// 设置继电器的开关监听

			final SwitchViewLongOnline sv1 = viewHolderK.swichView1;
			final SwitchViewLongOnline sv2 = viewHolderK.swichView2;
			final SwitchViewLongOnline sv3 = viewHolderK.swichView3;
			StringBuffer binaryMessage = new StringBuffer();
			binaryMessage.append("10000");

			// 这里要倒过append 3,2,1
			if (singleDevice.getJidianqi3Open()) {
				binaryMessage.append("0");
			} else {
				binaryMessage.append("1");
			}

			if (singleDevice.getJidianqi2Open()) {
				binaryMessage.append("0");
			} else {
				binaryMessage.append("1");
			}

			if (singleDevice.getJidianqi1Open()) {
				binaryMessage.append("0");
			} else {
				binaryMessage.append("1");
			}

			final View cv2 = convertView;
			LogUtilNIU.value("*********处理过后****知道*****原来的继电器状态为--->" + binaryMessage);
			LogUtilNIU.value("!!!!!!!!!!!!!!!!当前convertView！！！！！！！！！！！" + convertView.getId());
			lastControlBinaryMessage = binaryMessage.toString();
			setControlSwitchView(singleDevice, sv1, position, binaryMessage.toString(), 1, cv2);
			setControlSwitchView(singleDevice, sv2, position, binaryMessage.toString(), 2, cv2);
			setControlSwitchView(singleDevice, sv3, position, binaryMessage.toString(), 3, cv2);
			// 设置第二个开关监听
			// 设置第三个开关监听
			break;

		case TYPE_C:
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.item_listview_device_online_c, null);
				viewHolderC = new ViewHolderC();
				viewHolderC.tvName = (TextView) convertView.findViewById(R.id.tvName);
				viewHolderC.tvLocation = (TextView) convertView.findViewById(R.id.tvLocation);
				viewHolderC.tvType = (TextView) convertView.findViewById(R.id.tvType);
				viewHolderC.tvNetStatus = (TextView) convertView.findViewById(R.id.tvNetStatus);
				viewHolderC.ivOpenStatus = (ImageView) convertView.findViewById(R.id.ivOpenStatus);
				viewHolderC.ivDeviceType = (ImageView) convertView.findViewById(R.id.ivType);
				viewHolderC.ivLocation = (ImageView) convertView.findViewById(R.id.ivLocation);
				viewHolderC.ivWifiStatus = (ImageView) convertView.findViewById(R.id.imageViewWifiOn);
				viewHolderC.llUp = (LinearLayout) convertView.findViewById(R.id.llUp);
				viewHolderC.sv = (SwitchViewLongOnline) convertView.findViewById(R.id.switchView);
				viewHolderC.tvPowerStatus = (TextView) convertView.findViewById(R.id.tvStatus);
				convertView.setTag(viewHolderC);
			} else {
				viewHolderC = (ViewHolderC) convertView.getTag();
			}

			viewHolderC.tvName.setText(singleDevice.getDeviceName());
			viewHolderC.tvLocation.setText(singleDevice.getDeviceLocation());
			/***************************************************/
			// 判断设备是否在线，作相应显示
			// 设备在线 插座
			if (singleDevice.getIsOnline()) {
				// 设置用电状态
				if (singleDevice.getDevicePowerStatus().equals("00")) {
					viewHolderC.tvPowerStatus.setText("设备已断电");
				} else if (singleDevice.getDevicePowerStatus().equals("01")) {
					viewHolderC.tvPowerStatus.setText("设备正在正常用电");
				} else if (singleDevice.getDevicePowerStatus().equals("02")) {
					viewHolderC.tvPowerStatus.setText("设备正在待机");
				}
				// wifi状态在线,设置使用蓝色按钮
				viewHolderC.tvNetStatus.setText(context.getResources().getString(R.string.online));
				viewHolderC.sv.setCloseColorRed();
				viewHolderC.sv.invalidate();// 刷新颜色
				// wifi在线
				viewHolderC.ivWifiStatus.setImageResource(R.drawable.device_list_wifi_on);
				// 类型在线
				if (singleDevice.getDeviceType().equals("C")) {
					viewHolderC.tvType.setText(context.getResources().getString(R.string.socket));// 插座在线
					viewHolderC.ivDeviceType.setImageResource(R.drawable.device_list_socket);
				} else if (singleDevice.getDeviceType().equals("K")) {
					viewHolderC.tvType.setText(context.getResources().getString(R.string.switch2));// 开关在线
					viewHolderC.ivDeviceType.setImageResource(R.drawable.device_list_button);
				}
				// 位置在线
				viewHolderC.ivLocation.setImageResource(R.drawable.device_list_location);
				// 在线，按钮为蓝色
				if (singleDevice.getIsOpened()) {// 在线，且继电器开
					if (!viewHolderC.sv.isOpened()) {
						viewHolderC.sv.setOpened(true);
					}
					viewHolderC.ivOpenStatus.setImageResource(R.drawable.open_open);
					LogUtilNIU.value("状态判定为继电器开+绿色");
				} else {// 在线，继电器关
					if (viewHolderC.sv.isOpened()) {
						viewHolderC.sv.setOpened(false);
					}
					viewHolderC.ivOpenStatus.setImageResource(R.drawable.open_close);
					LogUtilNIU.value("状态判定为继电器关+红色");
				}
			} else {
				// 不在线情况，状态就是为空白
				viewHolderC.tvPowerStatus.setText("设备当前离线");
				viewHolderC.sv.setCloseColorDark();
				viewHolderC.sv.invalidate();
				viewHolderC.sv.setEnabled(false);
				// 设备不在线
				// wifi状态不在线
				viewHolderC.tvNetStatus.setText(context.getResources().getString(R.string.offline));
				viewHolderC.ivWifiStatus.setImageResource(R.drawable.device_list_wifi_no);
				// 类型不在线
				if (singleDevice.getDeviceType().equals("C")) {
					viewHolderC.tvType.setText(context.getResources().getString(R.string.socket));// 插座在线
					viewHolderC.ivDeviceType.setImageResource(R.drawable.device_list_socket_no);
				} else if (singleDevice.getDeviceType().equals("K")) {
					viewHolderC.tvType.setText(context.getResources().getString(R.string.switch2));// 开关在线
					viewHolderC.ivDeviceType.setImageResource(R.drawable.device_list_button_no);
				}
				// 位置在线
				viewHolderC.ivLocation.setImageResource(R.drawable.device_list_location_no);
				// 在线，按钮为灰色
				if (singleDevice.getIsOpened()) {// 不在线，且继电器开
					if (!viewHolderC.sv.isOpened()) {
						viewHolderC.sv.setOpened(true);
					}
					viewHolderC.ivOpenStatus.setImageResource(R.drawable.open_offline);
				} else {// 不在线，继电器关
					if (viewHolderC.sv.isOpened()) {
						viewHolderC.sv.setOpened(false);
					}
					viewHolderC.ivOpenStatus.setImageResource(R.drawable.open_offline);
				}
			}
			/***************************************************/
			// 设置上区域点击 跳转
			// LinearLayout llUp= viewHolderC.llUp;
			viewHolderC.llUp.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (!deviceList.get(position).getIsOnline()) {// 如果设备不在线
						// //设备离线
						// //去提示离线的页面
						Intent intent = new Intent(context, OffLineDeviceRemindActivity.class);
						// 设置当前设备
						BApplication.instance.setCurrentDevice(deviceList.get(position));
						context.startActivity(intent);
					} else {
						// 设备在线
						// 去查询设备信息的界面
						Intent intent = new Intent(context, MenuDeviceInfoMenuActivity.class);
						// 设置当前设备
						BApplication.instance.setCurrentDevice(deviceList.get(position));
						context.startActivity(intent);
					}
				}
			});
			/***************************************************/
			final SwitchViewLongOnline sv = viewHolderC.sv;

			final View cv = convertView;
			if (singleDevice.getIsOnline()) {// 如果设备在线，就可以控制开关继电器
				sv.setOnStateChangedListener(new OnStateChangedListener() {

					@Override
					public void toggleToOn(View view) {
						if (BApplication.instance.getOpenSetCanSet()) {
							BApplication.instance.setOpenSetCanSet(false);
							// 被点击后发广播，展示dialog
							showOpenDialog();
							currentSettingView = cv;
							LogUtilNIU.value("正在被控制的viewcurrentSettingView" + currentSettingView.getId());
							LogUtilNIU.value("发送继电器控制命令" + "00" + "控制继电器开");
							String verCode = ModbusCalUtil.verNumber(singleDevice.getDeviceID() + "0013000100");//
							String msg = "E7" + singleDevice.getDeviceID() + "0013000100" + verCode + "0D";
							// 发送UDP命令该设备继电器开
							LogUtilNIU.value("启动重连线程设置继电器开");
							clickPosition = position;
//							new LoadDataThreadUtil(msg, h, deviceList.get(position).getDeviceWIFIBSSID(), context)
//									.start();
							BApplication.instance.socketSend(msg);
							receiveThread = new ReceiveThread();
							receiveThread.start();
//							Message message = new Message();
//							message.obj = msg;
//							handler.sendEmptyMessage(Constant.MSG_SEND_CONTENT);
						} else {
							ToastUtils.shortToast(context, "请等待上个设置设置完毕");
							sv.toggleSwitch(false);
						}
					}

					@Override
					public void toggleToOff(View view) {
						if (BApplication.instance.getOpenSetCanSet()) {
							// 发广播
							showCloseDialog();

							BApplication.instance.setOpenSetCanSet(false);
							currentSettingView = cv;
							LogUtilNIU.value("正在被控制的viewcurrentSettingView" + currentSettingView.getId());
							LogUtilNIU.value("发送继电器控制命令" + "01" + "控制继电器关");
							String verCode = ModbusCalUtil.verNumber(singleDevice.getDeviceID() + "0013000101");// 01
							// 图开，表示关
							String msg = "E7" + singleDevice.getDeviceID() + "0013000101" + verCode + "0D";
							// 发送UDP命令该设备继电器开
							LogUtilNIU.value("启动重连线程设置继电器关");
							clickPosition = position;
//							new LoadDataThreadUtil(msg, h, singleDevice.getDeviceWIFIBSSID(), context).start();
							BApplication.instance.socketSend(msg);
							receiveThread = new ReceiveThread();
							receiveThread.start();
						} else {
							ToastUtils.shortToast(context, "请等待上个设置设置完毕");
							sv.toggleSwitch(true);
						}
					}
				});
			} else {// 如果不在线，点击会没有反应
				sv.setOnStateChangedListener(new SwitchViewLongOnline.OnStateChangedListener() {

					@Override
					public void toggleToOn(View view) {
						sv.toggleSwitch(false);
						ToastUtils.shortToast(context, "当前设备离线不可控制");
					}

					@Override
					public void toggleToOff(View view) {
						sv.toggleSwitch(false);
						ToastUtils.shortToast(context, "当前设备离线不可控制");
					}
				});
			}
			break;
		}
		return convertView;
	}

	/**
	 * i 代表是第几位开关要被控制
	 * 
	 * @Description: param:
	 */
	private void setControlSwitchView(final SingleDevice singleDevice, final SwitchViewLongOnline item,
			final int position, final String binaryString, final int i, final View cv) {
		final View contentview = cv;
		if (singleDevice.getIsOnline()) {
			item.setOnStateChangedListener(new OnStateChangedListener() {

				@Override
				public void toggleToOn(View view) {
                    kaiguanPositionControling = i;
                    LogUtilNIU.value("被控制的当前开关位置kaiguanPositionControling"+kaiguanPositionControling);
					LogUtilNIU.value("*******控制前的状态为*******" + binaryString);
					lastControlBinaryMessage = binaryString;
					// currentSettingView要设，用户更新显示
					currentSettingView = contentview;
					LogUtilNIU.value("Kaiguan 正在被控制的viewcurrentSettingView" + currentSettingView.getId());

					clickPosition = position;
					showOpenDialog();
					LogUtilNIU.value("控制开关1指令" + "00" + "控制继电器开");
					String new8bit = "";
					if (i == 1) {
						// 发送指令控制开关1继电器开
						// 原来指令的0到7为
						String new7bit = binaryString.substring(0, 7);
						new8bit = new7bit + "0";
						// 把二进制字符转16进制字符
						new8bit = ModbusCalUtil.binaryStringToHexString(new8bit);
						LogUtilNIU.value("控制第一个开关new8bit--->" + new8bit + "二进制为"
								+ ModbusCalUtil.hexStringToBinaryString(new8bit + ""));
						
					} else if (i == 2) {
						// 发送指令控制开关2继电器开
						// 原来指令的0到6位
						
						String new6bit = binaryString.substring(0, 6);
						String new7bit = new6bit + "0";
						new8bit = new7bit + binaryString.substring(7);
						// 把二进制字符转16进制字符
						new8bit = ModbusCalUtil.binaryStringToHexString(new8bit);
						LogUtilNIU.value("控制第二个开关new8bit--->" + new8bit + "二进制为"
								+ ModbusCalUtil.hexStringToBinaryString(new8bit));
					} else if (i == 3) {
						// 发送指令控制开关3继电器开
						// 原来指令的0到5位
						
						String new5bit = binaryString.substring(0, 5);
						String new6bit = new5bit + "0";
						new8bit = new6bit + binaryString.substring(6);
						// 把二进制字符转16进制字符
						new8bit = ModbusCalUtil.binaryStringToHexString(new8bit);
						LogUtilNIU.value("控制第三个开关new8bit--->" + new8bit + "二进制为"
								+ ModbusCalUtil.hexStringToBinaryString(new8bit + ""));
					}
					// 控制3个按钮其中一个
					String verCode = ModbusCalUtil.verNumber(singleDevice.getDeviceID() + "00130001" + new8bit);
					String msg = "E7" + singleDevice.getDeviceID() + "00130001" + new8bit + verCode + "0D";
//					new LoadDataThreadUtil(msg, h, deviceList.get(position).getDeviceWIFIBSSID(), context).start();
					BApplication.instance.socketSend(msg);
					receiveThread = new ReceiveThread();
					receiveThread.start();
				}

				@Override
				public void toggleToOff(View view) {
					   kaiguanPositionControling = i;
	                    LogUtilNIU.value("被控制的当前开关位置kaiguanPositionControling"+kaiguanPositionControling);
					lastControlBinaryMessage = binaryString;
					// TODO 关
					// currentSettingView要设,这个是被点击那个convertView，用户更新显示
					LogUtilNIU.value("*******控制前的状态为*******" + binaryString);
					currentSettingView = contentview;
					LogUtilNIU.value("Kaiguan 正在被控制的viewcurrentSettingView" + currentSettingView.getId());
					LogUtilNIU.value("点击控制时赋值" + currentSettingView.getId());
					clickPosition = position;
					showCloseDialog();
					LogUtilNIU.value("控制开关关闭1指令" + "01" + "控制继电器关");
					String new8bit = "";
					if (i == 1) {
						// 发送指令控制开关1继电器开
						// 原来指令的0到7为
						String new7bit = binaryString.substring(0, 7);
						new8bit = new7bit + "1";
						// 把二进制字符转16进制字符
						new8bit = ModbusCalUtil.binaryStringToHexString(new8bit);
						LogUtilNIU.value("控制第一个开关关闭new8bit" + new8bit + "二进制为"
								+ ModbusCalUtil.hexStringToBinaryString(new8bit + ""));
					} else if (i == 2) {
						// 发送指令控制开关2继电器开
						// 原来指令的0到6位
						String new6bit = binaryString.substring(0, 6);
						String new7bit = new6bit + "1";
						new8bit = new7bit + binaryString.substring(7);
						// 把二进制字符转16进制字符
						new8bit = ModbusCalUtil.binaryStringToHexString(new8bit);
						LogUtilNIU.value("控制第二个开关关闭new8bit" + new8bit + "二进制为"
								+ ModbusCalUtil.hexStringToBinaryString(new8bit + ""));
					} else if (i == 3) {
						// 发送指令控制开关3继电器开
						// 原来指令的0到5位
						String new5bit = binaryString.substring(0, 5);
						String new6bit = new5bit + "1";
						new8bit = new6bit + binaryString.substring(6);
						// 把二进制字符转16进制字符
						new8bit = ModbusCalUtil.binaryStringToHexString(new8bit);
						LogUtilNIU.value("控制第三个开关关闭new8bit" + new8bit + "二进制为"
								+ ModbusCalUtil.hexStringToBinaryString(new8bit + ""));
					}
					String verCode = ModbusCalUtil.verNumber(singleDevice.getDeviceID() + "00130001" + new8bit);
					String msg = "E7" + singleDevice.getDeviceID() + "00130001" + new8bit + verCode + "0D";
//					new LoadDataThreadUtil(msg, h, deviceList.get(position).getDeviceWIFIBSSID(), context).start();
					BApplication.instance.socketSend(msg);
					receiveThread = new ReceiveThread();
					receiveThread.start();
				}
			});
		} else {
			item.setOnStateChangedListener(new SwitchViewLongOnline.OnStateChangedListener() {

				@Override
				public void toggleToOn(View view) {
					item.toggleSwitch(false);
					ToastUtils.shortToast(context, "当前设备离线不可控制");
				}

				@Override
				public void toggleToOff(View view) {
					item.toggleSwitch(false);
					ToastUtils.shortToast(context, "当前设备离线不可控制");
				}
			});
		}
	}

	// dialog消失
	protected void dismissDialog() {
		LogUtilNIU.value("发广播要求dialog消失");
		Intent intent = new Intent();
		intent.setAction(Constant.BROCAST_ONLINE_LIST_DIMISS_DIALOG);
		context.sendBroadcast(intent);
	}

	// 显示正在控制开关正在关闭
	protected void showCloseDialog() {
		Intent intent = new Intent();
		intent.setAction(Constant.BROCAST_ONLINE_LIST_SHOW_CLOSE_DIALOG);
		context.sendBroadcast(intent);
	}

	// 显示正在控制开关正在打开
	protected void showOpenDialog() {
		Intent intent = new Intent();
		intent.setAction(Constant.BROCAST_ONLINE_LIST_SHOW_OPEN_DIALOG);
		context.sendBroadcast(intent);
	}

	// 显示正在控制开关正在打开
	protected void showRefresh0002Dialog() {
		Intent intent = new Intent();
		intent.setAction(Constant.BROCAST_REFRESHING0002_DIALOG);
		context.sendBroadcast(intent);
	}

	/***************************************************/
	// 插座的viewholder
	public class ViewHolderC {
		TextView tvName;// 设备名称，看数据
		TextView tvNetStatus;// 设备是否在线，看设备对象布尔值判断，默认为不在线
		TextView tvType;// 设备类型，看数据
		TextView tvLocation;// 设备位置，看数据
		TextView tvBtnOnOff;// 设备继电器BUTTON显示， 看对象属性，默认为关
		TextView tvBtnOn;
		ImageView ivWifiStatus;// 在线状态为点亮
		ImageView ivLocation;// 设备位置图标，在在线状态为点亮
		ImageView ivDeviceType;// 设备类型的图片，插座或开关
		ImageView ivOpenStatus;
		LinearLayout llUp;
		SwitchViewLongOnline sv;
		TextView tvPowerStatus;// 3种状态
		// ProgressBar progressBar;
	}

	// 开关的viewholder
	public class ViewHolderK {
		TextView tvName;// 设备名称，看数据
		TextView tvNetStatus;// 设备是否在线，看设备对象布尔值判断，默认为不在线
		TextView tvType;// 设备类型，看数据
		TextView tvLocation;// 设备位置，看数据
		TextView tvBtnOnOff;// 设备继电器BUTTON显示， 看对象属性，默认为关
		TextView tvBtnOn;
		ImageView ivWifiStatus;// 在线状态为点亮
		ImageView ivLocation;// 设备位置图标，在在线状态为点亮
		ImageView ivDeviceType;// 设备类型的图片，插座或开关
		LinearLayout llUp;
		ImageView ivOpenStatus1;
		ImageView ivOpenStatus2;
		ImageView ivOpenStatus3;
		SwitchViewLongOnline swichView1;
		SwitchViewLongOnline swichView2;
		SwitchViewLongOnline swichView3;
	}

	
	
	/**
	 * 接收数据线程
	 */
	private class ReceiveThread extends Thread {
		@Override
		public void run() {
			Message msg = new Message();
			msg.what = Constant.MSG_FAILURE;
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
