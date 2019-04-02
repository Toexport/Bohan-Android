package cn.mioto.bohan.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import cn.mioto.bohan.BApplication;
import cn.mioto.bohan.Constant;
import cn.mioto.bohan.utils.NetworkUtils;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

/**
 * 心跳服务
 * 
 * @author jiangsl
 * 
 */
public class BackService extends Service {
	private final static String TAG = BackService.class.getSimpleName();
	public final static String ACTION_NOTIFY = "Bohan"
			+ ".WorkStationService.notify";
	private final static String IP = "121.42.149.11";
	private final static String IP2 = "182.92.130.111"; // 暂未启用，备用IP
	private final static int PORT = 65039;
	private final static int STEP_INIT = 0;
	private final static int STEP_REG = 1;
	private final static int STEP_HEART = 2;
	private final static int MAX_CREATE_COUNT = 20;
	private final static int MAX_REG_COUNT = 30;
	private final static int MAX_HEART_COUNT = 30;
	private int step = STEP_INIT;
	private String imei = null;
	private int createCount = 0;
	private int regCount = 0;
	private int heartCount = 0;
	private Socket socket = null;
	private OutputStream output = null;
	private InputStream input = null;
	private ReceiveThread receiveThread;
	private Context context;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate()");
		context = getApplicationContext();
		timer.schedule(task, 1000L, 40*1000L);//40S
		receiveThread = new ReceiveThread();
		receiveThread.start();
		socket = BApplication.instance.socket;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand()");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy()");
		timer.cancel();
		if (receiveThread != null) {
			receiveThread.setStop(true);
			receiveThread = null;
		}
		closeSocket();
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	Timer timer = new Timer();
	TimerTask task = new TimerTask() {
		@Override
		public void run() {
			doSomething();
		}
	};

	/**
	 * 执行任务
	 */
	private void doSomething() {
		Log.d(TAG, "doSomething()");
		if (NetworkUtils.isNetworkConnected(getApplicationContext())) {
			// 发送心跳
				if (heartCount > MAX_HEART_COUNT) {
					createCount = 0;
					regCount = 0;
					heartCount = 0;
					closeSocket();
					return;
				}
				if (heartCount % 10 == 0) {
					sendString("0");
				}
				heartCount++;
		}
	}

	/**
	 * 通知通讯失败
	 */
	private void sendFailure() {
		Log.d(TAG, "sendFailure()");
		Intent intent = new Intent(ACTION_NOTIFY);
		// intent.putExtra(Globals.EXTRA_WHAT, Globals.MSG_FAILURE);
		context.sendBroadcast(intent);
	}

	/**
	 * 发送数据给UI
	 */
	private void sendData(String data) {
		Log.d(TAG, "sendData() " + data);
		Intent intent = new Intent(ACTION_NOTIFY);
		// intent.putExtra(Globals.EXTRA_WHAT, Globals.MSG_SUCCESS);
		// intent.putExtra(Globals.EXTRA_DATA, data);
		context.sendBroadcast(intent);
	}

	/**
	 * 创建连接的方法
	 */
	private boolean createSocket() {
		Log.d(TAG, "createSocket() " + PORT);
		boolean result = false;
		try {
			socket = new Socket();
			socket.setSoTimeout(2 * 60 * 1000);
			int count = createCount / 5;
			if (count % 2 == 0) {
				Log.d(TAG, "connect() to " + IP);
				socket.connect(new InetSocketAddress(IP, PORT), 10000);
			} else {
				Log.d(TAG, "connect() to " + IP2);
				socket.connect(new InetSocketAddress(IP2, PORT), 10000);
			}
			if (socket.isConnected()) {
				output = socket.getOutputStream();
				input = socket.getInputStream();
				result = true;
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (!result) {
			closeSocket();
		}
		return result;
	}

	/**
	 * 关闭连接的方法
	 */
	private void closeSocket() {
		Log.d(TAG, "closeSocket()");
		step = STEP_INIT;
		try {
			if (socket != null) {
				socket.close();
				socket = null;
			}
			input = null;
			output = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 发送数据给服务器
	 */
	public void sendString(String data) {
		Log.d(TAG, "socket send:" + data);
		try {
			output.write(data.getBytes("UTF-8"));
			output.flush();
		} catch (Exception e) {
			e.printStackTrace();
			closeSocket();
		}
	}

	/**
	 * 接收数据
	 */
	public String readString() {
		if (input != null) {
			try {
				byte buffer[] = new byte[2048];
				int count = input.read(buffer);
				return new String(buffer, 0, count, "UTF-8");
			} catch (Exception e) {
				e.printStackTrace();
				closeSocket();
			}
		}
		return null;
	}

	/**
	 * 接收数据线程
	 */
	private class ReceiveThread extends Thread {
		private boolean stop = false;

		@Override
		public void run() {
			Log.d(TAG, "ReceiveThread() start");
			while (!stop) {
				String result = readString();
				if (result != null) {
					try {
						Log.d(TAG, "socket receive:" + result);
						if (result.startsWith("$$") && result.endsWith("*")) {
							String subResult = result.substring(2,
									result.length() - 1);
							if (subResult.contains("*$$")) { // 处理同时接收到多条数据的情况
								String[] array = subResult.replace("*$$", "$")
										.split("\\$");
								for (String temp : array) {
									preccessData(temp);
								}
							} else {
								preccessData(subResult);
							}
						}
					} catch (Exception e) {
						Log.e(TAG, e.getMessage());
						e.printStackTrace();
					}
				} else {
					try {
						Thread.sleep(1000L);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

			}
			Log.d(TAG, "ReceiveThread() end");
		}

		public void setStop(boolean stop) {
			this.stop = stop;
		}

	}

	/**
	 * 处理数据
	 * 
	 * @param data
	 * @throws Exception
	 */
	private void preccessData(String data) throws Exception {
		if (data.startsWith("V1")) {
			if (data.contains("OK")) { // 注册成功
				regCount = 0;
				step = STEP_HEART;
			}
		} else if (data.startsWith("V2")) { // 位置数据
			sendData(data);
		} else if (data.startsWith("V4")) { // 下行心跳包
			heartCount = 1;
		} else if (data.startsWith("V5")) { // 位置数据，新增3个数据项
			sendData(data);
		}
	}

	/**
	 * 启动服务
	 * 
	 * @param context
	 * @param imei
	 */
	public static void startService(Context context) {
		Log.d(TAG, "startService()");
		Intent intent = new Intent(context, BackService.class);
		context.startService(intent);
	}

	/**
	 * 停止服务
	 * 
	 * @param context
	 * @param imei
	 */
	public static void stopService(Context context) {
		Log.d(TAG, "stopService()");
		Intent intent = new Intent(context, BackService.class);
		context.stopService(intent);
	}

}
