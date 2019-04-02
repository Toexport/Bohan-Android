package cn.mioto.bohan;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.xutils.x;

import cn.mioto.bohan.entity.Config;
import cn.mioto.bohan.entity.SingleDevice;
import cn.mioto.bohan.entity.User;
import cn.mioto.bohan.netty.DeviceController;
import cn.mioto.bohan.utils.Enterface;
import cn.mioto.bohan.utils.LogUtilNIU;
import cn.mioto.bohan.utils.ModbusCalUtil;

import com.vilyever.socketclient.SocketClient;
import com.vilyever.socketclient.helper.SocketClientAddress;
import com.vilyever.socketclient.helper.SocketClientDelegate;
import com.vilyever.socketclient.helper.SocketResponsePacket;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import cz.msebera.android.httpclient.Header;
import steed.framework.android.client.ClientUtil;
import steed.framework.android.client.JsonClientHandler2;
import steed.framework.android.client.SimpleTokenEngine;
import steed.framework.android.client.TokenUtil;
import steed.framework.android.core.ContextUtil;
import steed.framework.android.util.base.PathUtil;

/**
 * 储存全局变量 初始化代码
 * 
 * 
 */
public class BApplication extends Application implements
		Thread.UncaughtExceptionHandler {

	private static BApplication bainstance;
	private List<Activity> activityList = new LinkedList<Activity>();
	/**
	 * 应用包名与LAUNCHER
	 */
	public static final String PKG = "cn.mioto.bohan";
	public static final String CLS = "cn.mioto.bohan.activity.LoginActivity";

	// 启动线程 每10分钟向服务器发一个心跳
	public void startHeartBreakStart() {
		sendHeartShouldBreak = false;
		new HeartBreakThread().start();
	}

	// 停止向服务器发心跳的线程
	public void startHeartBreakStop() {
		LogUtilNIU.value("发送心跳包的线程停止");
		sendHeartShouldBreak = true;
	}

	private boolean sendHeartShouldBreak = true;

	// 每8分钟发一次心跳的线程
	public class HeartBreakThread extends Thread {

		@Override
		public void run() {
			super.run();
			LogUtilNIU.value("发送心跳包的线程开始");
			while (!sendHeartShouldBreak) {
				sendHeart();
				try {
					// 5分钟发一次
					Thread.sleep(1000 * 60 * 5);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void sendHeart() {
		new Enterface("keepOnline.act").doRequest(new JsonClientHandler2() {

			@Override
			public void onInterfaceSuccess(String message, String contentJson) {
				LogUtilNIU.circle("发送心跳到服务器成功了！！！！！");
			}

			@Override
			public void onFailureConnected(Boolean canConnect) {
				LogUtilNIU.circle("发送心跳到服务器接口不通！！！！！");
			}

			@Override
			public void onInterfaceFail(String json) {
				LogUtilNIU.circle("发送心跳到服务器返回非0！！！！！");
			}
		});
	}

	// 9组定时的待确认状态
	public Boolean nineSetWaitConfirm = false;

	public Boolean getNineSetWaitConfirm() {
		return nineSetWaitConfirm;
	}

	public void setNineSetWaitConfirm(Boolean nineSetWaitConfirm) {
		this.nineSetWaitConfirm = nineSetWaitConfirm;
	}

	// 用户下设备总个数，用于设备管理列表对map的初始化
	public int deviceQua = 0;

	public int getDeviceQua() {
		return deviceQua;
	}

	public void setDeviceQua(int deviceQua) {
		this.deviceQua = deviceQua;
	}

	// 当前wifi的BSSID(唯一MAC地址)
	public String mWIFIRouterBSSID = "";

	public String getmWIFIRouterSSID() {
		return mWIFIRouterBSSID;
	}

	public void setmWIFIRouterSSID(String mWIFIRouterSSID) {
		this.mWIFIRouterBSSID = mWIFIRouterSSID;
	}

	/*
	 * 重发机制的线程是否要停止
	 */
	private Boolean resendTaskShowBreak = true;

	public Boolean getResendTaskShowBreak() {
		return resendTaskShowBreak;
	}

	public void setResendTaskShowBreak(Boolean resendTaskShowBreak) {
		this.resendTaskShowBreak = resendTaskShowBreak;
	}

	// 9组时段的其他按钮能否点击
	Boolean nineSetCanSet = true;

	public Boolean getNineSetCanSet() {
		return nineSetCanSet;
	}

	public void setNineSetCanSet(Boolean nineSetCanSet) {
		this.nineSetCanSet = nineSetCanSet;
	}

	private List<String> currentDeviceIds = new ArrayList<>();

	public List<String> getCurrentDeviceIds() {
		return currentDeviceIds;
	}

	public void setCurrentDeviceIds(List<String> currentDeviceIds) {
		this.currentDeviceIds = currentDeviceIds;
	}

	/**
	 * @Title: clearThisUserFlashDatasOfApplication @Description:清空此用户的内存数据,
	 *         恢复到app登录前 @return void @throws
	 */
	public void clearThisUserFlashDatasOfApplication() {
		startHeartBreakStop();// 停止向服务器发送心跳
		deviceDataHasChanged = true;
		currentDeviceIds.clear();
		deviceQua = 0;
		nineSetSettingPosition = 0;
		nineSetCanSet = true;
		nineSetWaitConfirm = false;
	}

	/*
	 * 设备数据有没有更改的全局变量
	 */
	private Boolean deviceDataHasChanged = true;

	public Boolean getDeviceDataHasChanged() {
		return deviceDataHasChanged;
	}

	public void setDeviceDataHasChanged(Boolean deviceDataHasChanged) {
		this.deviceDataHasChanged = deviceDataHasChanged;
	}

	// 分别表示在线列表开关的三个继电器是否可以被控制
	private Boolean open1CanSet = true;
	private Boolean open2CanSet = true;
	private Boolean open3CanSet = true;

	public Boolean getOpen1CanSet() {
		return open1CanSet;
	}

	public void setOpen1CanSet(Boolean open1CanSet) {
		this.open1CanSet = open1CanSet;
	}

	public Boolean getOpen2CanSet() {
		return open2CanSet;
	}

	public void setOpen2CanSet(Boolean open2CanSet) {
		this.open2CanSet = open2CanSet;
	}

	public Boolean getOpen3CanSet() {
		return open3CanSet;
	}

	public void setOpen3CanSet(Boolean open3CanSet) {
		this.open3CanSet = open3CanSet;
	}

	// 在线列表是否可以控制继电器
	private Boolean openSetCanSet = true;

	public Boolean getOpenSetCanSet() {
		return openSetCanSet;
	}

	public void setOpenSetCanSet(Boolean openSetCanSet) {
		this.openSetCanSet = openSetCanSet;
	}

	// 继电器正在修改的那个开关
	private int openSettingPosition;

	public int getOpenSettingPosition() {
		return openSettingPosition;
	}

	public void setOpenSettingPosition(int openSettingPosition) {
		this.openSettingPosition = openSettingPosition;
	}

	// 9组时段的正在设置按钮位置
	private int nineSetSettingPosition;

	public int getNineSetSettingPosition() {
		return nineSetSettingPosition;
	}

	public void setNineSetSettingPosition(int nineSetSettingPosition) {
		this.nineSetSettingPosition = nineSetSettingPosition;
	}

	// 增加一个是否在家的判断 TODO 看用什么方式可以判断出来
	private Boolean isAtHome = false;

	public Boolean getIsAtHome() {
		return isAtHome;
	}

	public void setIsAtHome(Boolean isAtHome) {
		this.isAtHome = isAtHome;
	}

	public static Boolean isSmallScreen = false;// 判断是否为小屏幕
	/*
	 * 离线模式
	 */
	public static Boolean offLineMode;
	// 管理Log,未发布
	public static boolean isRelease = false;
	// 把对象作为静态全局变量
	public static BApplication instance;
	// 全局的Activity集合
	public static List<Activity> listActivity = new ArrayList<Activity>();
	public static User CurrentUser;// 记录当前正登录的用户
	// socket
	public static Socket socket;
	public static InputStream in;
	public static OutputStream out;
	public static Boolean listener;
	public static String SOCKET_IP = "122.10.97.35";
	public static int SOCKET_PORT = 6869;

	private SharedPreferences sp;
	private Editor editor;
	private static SocketClient socketClient;
	private static SocketClientDelegate delegate;
	/***************************************************/
	// UDP 变量
	private String BROADCAST_IP = "122.10.97.35";
	private MulticastSocket multicastSocket;
	private DatagramPacket dpSend;
	private InetAddress serverAddress;
	private String message;// 发送的内容
	public int BROADCAST_PORT = 6869;// 目标端口
	public int BROADCAST_MYPORT = 4366;// 本机端口号
	/***************************************************/
	/*
	 * 用户绑定的所有设备，先保存在全局。
	 */
	public List<SingleDevice> devices = new ArrayList<SingleDevice>();

	public List<SingleDevice> getDevices() {
		return devices;
	}

	public void setDevices(List<SingleDevice> devices) {
		this.devices = devices;
	}

	public static DeviceController deviceController;

	private SingleDevice currentDevice;

	public SingleDevice getCurrentDevice() {
		return currentDevice;
	}

	public User getCurrentUser() {
		return CurrentUser;
	}

	public void setCurrentDevice(SingleDevice device) {
		this.currentDevice = device;
	}

	/***************************************************/
	@Override
	public void onCreate() {
		super.onCreate();
		bainstance = this;
		CrashHandler crashHandler = CrashHandler.getInstance();
		crashHandler.init(getApplicationContext());
		crashHandler.sendPreviousReportsToServer();
		// 断屏幕大小
		DisplayMetrics dm = getResources().getDisplayMetrics();
		int w_screen = dm.widthPixels;
		int h_screen = dm.heightPixels;
		LogUtilNIU.value("屏幕宽度" + w_screen + "屏幕高度" + h_screen);
		if (w_screen * h_screen <= 480 * 854) {
			isSmallScreen = true;
		}
		/***************************************************/
		sp = this.getSharedPreferences("userInfo", MODE_PRIVATE); // 私有的权限
		editor = sp.edit();
		offLineMode = false;
		/************ 建立数据库 ***************************************/
		// mDatabaseHelper =
		// DatabaseHelper.getInstance(getApplicationContext());
		/********* 新建设备数据库操作类对象 ******************************************/
		// mDeviceDataSource = new DeviceDataSource(mDatabaseHelper);
		instance = this;
		// deviceController = new DeviceController();
		ContextUtil.setApplicationContext(this);
		x.Ext.init(this);
		CurrentUser = new User();
		/***************************************************/
		// 根据asset的config.properties获取token，每隔一秒获取一次。如果刷新超时，回调下面的run方法
		// SimpleTokenEngine.baseUrl = Constant.baseUrl;
		// ClientUtil.setBaseUrl(PathUtil.mergePath(Constant.baseUrl,
		// "/client/bohan/"));
		// TokenUtil.init(new Runnable() {
		//
		// @Override
		// public void run() {
		// // 刷新超时的话回调
		// }
		// });
		/***************************************************/
		try {
			initUdp();// 初始化UDP
		} catch (IOException e) {
			e.printStackTrace();
		}

		initReceiveThread();// 初始化接收UDP消息的线程
		/***************************************************/
		// 初始的时候默认为在家 TODO
		setIsAtHome(true);
		// 初始化socket
		initSocket();
	}

	/**
	 * @Title: checkIfAtHome @Description:用户登录后，判断用户是否在设备局域网环境内
	 *         如果是，给application的全局布尔值赋值 如果在局域网内 在查询设备信息的时候，先用UDP再查服务器
	 * 
	 *         如果不在局域网内 直接查服务器数据，忽略UDP查数据这一步，直接服务器查数据
	 * 
	 *         但是用户有没有在局域网内是一个动态事件。 所有需要监听android系统广播观察WIFI的变化情况
	 * 
	 * @return void
	 * 
	 * @throws
	 */
	public void checkIfAtHome() {
		// TODO 检查是否在设备的局域网环境内
		// 检查方法，发任意UDP广播，看是否有设备返回信息。
	}

	public SharedPreferences getSp() {
		return this.sp;
	}

	public Editor getEditor() {
		return this.editor;
	}

	private void initUdp() throws IOException {
		multicastSocket = new MulticastSocket(BROADCAST_MYPORT);// 数据接收的目标端口
		serverAddress = InetAddress.getByName(BROADCAST_IP); // 设置地址
		multicastSocket.joinGroup(serverAddress); // 将该MulticastSocket对象加入到指定的多点广播地址
	}

	private void initReceiveThread() {
		new UDPReceiveThread().start();
	}

	// ------------------------------------------------------------------------------
	// 此线程在application内运行，一收到有UDP消息就会向外发广播。
	private class UDPReceiveThread extends Thread {
		@Override
		public void run() {
			super.run();
			byte data[] = new byte[256];
			DatagramPacket dpReceive = new DatagramPacket(data, data.length,
					serverAddress, BROADCAST_MYPORT);
			while (true) {
				try {
					multicastSocket.receive(dpReceive);
					InetAddress address = multicastSocket.getInetAddress();
					byte[] r = dpReceive.getData();
					String resultMsg = new String(dpReceive.getData(),
							dpReceive.getOffset(), dpReceive.getLength());
					resultMsg = ModbusCalUtil.bytesToHexString(r);
					resultMsg = resultMsg.toUpperCase();
					// LogUtilNIU.value("接收到的16进制数据数据转为String--->"+resultMsg);
					// resultMsg为收到的消息
					// 收到消息后，发广播，指定Constant.SOCKET_BROCAST_ONRECEIVED为广播频道
					// 在任何地方注册了这个频道的广播接收者都可以收到这条消息
					// if(resultMsg.contains("0D")){
					// int a = resultMsg.indexOf("0D");
					// resultMsg =resultMsg.substring(0,a+2);
					// }
					BApplication.instance.setIsAtHome(true);
					resultMsg = resultMsg.replace(" ", "");// 去掉字符中间的空格
					LogUtilNIU.value("数据去掉空格处理后为" + resultMsg);
					Intent intent = new Intent();
					intent.setAction(Constant.SOCKET_BROCAST_ONRECEIVED);
					intent.putExtra(Constant.SOCKET_BROCAST_EXTRA_MESSAGE,
							resultMsg);
					sendBroadcast(intent);// 接收到数据后发广播
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**********************************************************/
	/**
	 * @Title: sendUDPMsg @Description:UDP的公开方法， 可在其他activity调用次方法去发送UDP数据 @return
	 *         void @throws
	 */
	long lastTime = 0;

	public void sendUDPMsg(String msg) throws IOException {
		LogUtilNIU.circle("*******发送帧，指令号" + msg.substring(14, 18) + "指令内容"
				+ msg);
		message = msg;
		// 16进制字符转数字转数组
		byte data[] = ModbusCalUtil.decodeHex(message.toCharArray());
		// byte data[]=ModbusCalUtil.getHexByteArray(message);
		// byte data[] = message.getBytes();
		// LogUtilNIU.value("data[]"+Arrays.toString(message.getBytes()));
		dpSend = new DatagramPacket(data, data.length, serverAddress,
				BROADCAST_PORT);// 数据发送的目标端口号
		new SendUdpMsgThread().start();// 启动线程发送消息
		LogUtilNIU.circle("距离上一次指令的发送时间为"
				+ ((System.currentTimeMillis() - lastTime)) + "毫秒");
		lastTime = System.currentTimeMillis();
	}

	/*
	 * 发送UDP数据的线程
	 */
	private class SendUdpMsgThread extends Thread {
		@Override
		public void run() {
			super.run();
			try {
				multicastSocket.send(dpSend);
			} catch (IOException e) {
				e.printStackTrace();
			} // 调用MulticastSocket对象的send方法 发送数据
		}
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		System.out.println("uncaughtException");
		System.exit(0);
		// 捕获异常后退出程序
	}

	/**
	 * 发送数据
	 * 
	 * @param mess
	 */
	public void socketSend(String mess) {
		try {
			if (socket != null) {
				LogUtilNIU.circle("发送" + mess + "至");
				out.write(mess.getBytes("UTF-8"));
				out.flush();
				LogUtilNIU.circle("发送成功");
			} else {
				LogUtilNIU.circle("client 不存在");
				LogUtilNIU.circle("连接不存在重新连接");
				initSocket();
			}

		} catch (Exception e) {
			LogUtilNIU.circle("send error");
			e.printStackTrace();
		} finally {
			LogUtilNIU.circle("发送完毕");

		}
	}

	public static InputStream getIn() {
		return in;
	}

	public static void setIn(InputStream in) {
		BApplication.in = in;
	}

	public static OutputStream getOut() {
		return out;
	}

	public static void setOut(OutputStream out) {
		BApplication.out = out;
	}

	// 初始化socket
	public void initSocket() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					socket = new Socket(SOCKET_IP, SOCKET_PORT);
					out = socket.getOutputStream();
					in = socket.getInputStream();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}).start();
	}

	public static Socket getSocket() {
		return socket;
	}

	public static void setSocket(Socket socket) {
		BApplication.socket = socket;
	}

	/**
	 * 接收数据
	 */
	public String readString() throws Exception {
		if (in != null) {
			byte buffer[] = new byte[2048];
			int count = in.read(buffer);
			return new String(buffer, 0, count, "UTF-8");
		}
		return null;
	}

	/**
	 * 获到app实例
	 * 
	 * @return
	 */
	public static BApplication getInstance() {
		if (null == bainstance) {
			bainstance = new BApplication();
		}
		return bainstance;

	}

	/**
	 * 关闭所有activity
	 */
	public void finishAllActivity() {
		for (Activity activity : activityList) {
			if (activity != null) {
				activity.finish();
			}
		}
	}

	/**
	 * 重启app
	 */
	public void restartApp() {
		if (isRestart()) {
			Intent intent = new Intent();
			intent.setClassName(PKG, CLS);
			PendingIntent restartIntent = PendingIntent.getActivity(
					getApplicationContext(), 0, intent,
					Intent.FLAG_ACTIVITY_NEW_TASK);
			AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			am.set(AlarmManager.RTC, System.currentTimeMillis() + 3000,
					restartIntent);
		}
		finishAllActivity();
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(0);
	}

	/**
	 * 防止不断重启，10秒内不进行重复重启
	 * 
	 * @return
	 */
	private boolean isRestart() {
		SharedPreferences shared = getSharedPreferences(Config.FILE,
				Context.MODE_APPEND);
		long restartTime = shared.getLong("restart_time", 0);
		long currentTime = System.currentTimeMillis();
		SharedPreferences.Editor editor = shared.edit();
		editor.putLong("restart_time", currentTime);
		editor.commit();
		if ((currentTime - restartTime) > 10000L) {
			return true;
		} else {
			return false;
		}
	}

}
