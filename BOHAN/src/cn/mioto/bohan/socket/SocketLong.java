package cn.mioto.bohan.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import org.json.JSONException;
import org.json.JSONObject;
import cn.mioto.bohan.BApplication;
import cn.mioto.bohan.Constant;
import cn.mioto.bohan.aidl.IBackService;
import cn.mioto.bohan.entity.Config;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

public class SocketLong extends Service {
	private static final String TAG = "SocketLong";
	/** 心跳检测时间 */
	private static final long HEART_BEAT_RATE = 40 * 1000;
	/** 主机IP地址 */
	private static final String HOST = "192.168.1.104";
	/** 端口号 */
	public static final int PORT = 9800;
	/** 消息广播 */
	public static final String MESSAGE_ACTION = "org.feng.message_ACTION";
	/** 心跳广播 */
	public static final String HEART_BEAT_ACTION = "org.feng.heart_beat_ACTION";

	private static long sendTime = 0L;

	/** 弱引用 在引用对象的同时允许对垃圾对象进行回收 */
	public static Socket mSocket;

	private static ReadThread mReadThread;

	private String token;

	public static String SOCKET_IP = "www.bohanserver.top";
	// public static String SOCKET_IP = "61.145.190.175";
	public static int SOCKET_PORT = 36868;
	private static Context context;

	private IBackService.Stub iBackService = new IBackService.Stub() {
		@Override
		public boolean sendMessage(String message) throws RemoteException {
			try {
				return sendMsg(message);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}
	};

	@Override
	public IBinder onBind(Intent arg0) {
		return (IBinder) iBackService;
	}

	@Override
	public void unbindService(ServiceConnection conn) {
		// releaseLastSocket(BApplication.instance.socket);
		super.unbindService(conn);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		token = Config.getToken(this);
		context = getApplicationContext();
		// token = getSharedPreferences(Config.TOKEN,
		// Context.MODE_PRIVATE).getString("token", null);
		new InitSocketThread().start();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
//		if(null == mSocket ){
//			new InitSocketThread().start();
//		}
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	@Deprecated
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
//		if (null == mSocket) {
//			new InitSocketThread().start();
//		}
	}
	
	// 发送心跳包
	private  static Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			if(msg.what == Constant.SEND_FAIL){
//				Intent intent = new Intent(
//						Constant.SOCKET_SEND_FAIL);
//				context.sendBroadcast(intent);
				//发送数据失败则socket断开，重新连接
				stopService(context);
				startService(context);
			}
		}
	};
	private Runnable heartBeatRunnable = new Runnable() {
		@Override
		public void run() {
			if (System.currentTimeMillis() - sendTime >= HEART_BEAT_RATE) {
				String msg = "3A" + "1004" + "0027" + token + "0D";
				boolean isSuccess = false;
				try {
					isSuccess = sendMsg(msg);
				} catch (Exception e) {
					e.printStackTrace();
				}// 就发送一个\r\n过去,
													// 如果发送失败，就重新初始化一个socket
				if (!isSuccess) {
					mHandler.removeCallbacks(heartBeatRunnable);
					releaseLastSocket();
					new InitSocketThread().start();
				}
			}
			mHandler.postDelayed(this, HEART_BEAT_RATE);
		}
	};

	public static  boolean sendMsg(final String msg){
		if (null == mSocket) {
			return false;
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					if (!mSocket.isClosed() && !mSocket.isOutputShutdown()) {
						OutputStream os = mSocket.getOutputStream();
						if (!TextUtils.isEmpty(msg)) {
							Log.i(TAG, "发送msg: " + msg);
							if (mSocket.isConnected()
									&& !mSocket.isOutputShutdown()) {
								os.write(msg.getBytes());
								os.flush();
							}
							sendTime = System.currentTimeMillis();// 每次发送成功数据，就改一下最后成功发送的时间，节省心跳间隔时间
							Log.i(TAG, "发送成功的时间：" + sendTime);
						}
					} else {
					}
				} catch (IOException e) {
					e.printStackTrace();
					mHandler.sendEmptyMessage(Constant.SEND_FAIL);
				}
			}
		}).start();
		return true;
	}

	public void initSocket() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					mSocket = new Socket(SOCKET_IP, SOCKET_PORT);
//					mSocket.setSoTimeout(10 * 1000);
					// mSocket = new
					// WeakReference<Socket>(BApplication.instance.socket);
					mReadThread = new ReadThread(mSocket);
					mReadThread.start();
					mHandler.postDelayed(heartBeatRunnable, HEART_BEAT_RATE);// 初始化成功后，就准备发送心跳包
					// socket.setSoTimeout(300 * 1000);
					// isa = new InetSocketAddress(SOCKET_IP, SOCKET_PORT);
					// socket.connect(isa, 300 * 1000);
					// out = socket.getOutputStream();
					// in = socket.getInputStream();
					// socket.setTcpNoDelay(true);
					// socket.setKeepAlive(true);
				} catch (UnknownHostException e) {
					e.printStackTrace();
					new InitSocketThread().start();
				} catch (IOException e) {
					e.printStackTrace();
					new InitSocketThread().start();
				}

			}
		}).start();
	}

	// 释放socket
	public static void releaseLastSocket() {
		try {
			if (null != mSocket) {
				if (!mSocket.isClosed()) {
					mSocket.close();
				}
				mSocket = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	class InitSocketThread extends Thread {
		@Override
		public void run() {
			super.run();
			initSocket();
		}
	}

	public class ReadThread extends Thread {
		private WeakReference<Socket> mWeakSocket;
		private boolean isStart = true;

		public ReadThread(Socket socket) {
			mWeakSocket = new WeakReference<Socket>(socket);
		}

//		public void release() {
//			isStart = false;
//			releaseLastSocket();
//		}

		@Override
		public void run() {
			super.run();
			if (null != mSocket && mSocket.isConnected()) {
//				while (true) {
					try {
						InputStream is = null;
						if (null != mSocket) {
							is = mSocket.getInputStream();
							byte[] buffer = new byte[1024 * 4];
							int length = 0;
							while (((length = is.read(buffer)) != -1)) {
								if (length > 0) {
									String message = new String(buffer, 0,
											length);
									Log.i(TAG, "收到服务器发送来的消息：" + message);
									if (message != null) {
										JSONObject obj;
										try {
											obj = new JSONObject(message);
											int code = obj.getInt("statusCode");
											if (code == 0) {
												if (obj.getString("message")
														.equals("在线设备列表")) {
													Intent intent = new Intent(
															Constant.BROCAST_ONLINE_MSG);
													intent.putExtra("message",
															message);
													sendBroadcast(intent);
												} else if (obj.getString(
														"message").equals(
														"设备返回数据")) {
													String content = obj
															.getString("content");

													if (content.substring(16,
															20).equals("0001")) {
														// 查询用电参数,实时数据
														Intent intent1 = new Intent(
																Constant.BROCAST_NOW_DATA_MSG);
														intent1.putExtra(
																"message",
																content);
														sendBroadcast(intent1);
													} else if (content
															.substring(16, 20)
															.equals("0003")) {
														// 查询电价相关信息,负荷门限

													} else if (content
															.substring(16, 20)
															.equals("0007")) {
														// 查询定时计量数据
														Intent intent1 = new Intent(
																Constant.BROCAST_GET_TIME_JILIANG);
														intent1.putExtra(
																"message",
																content);
														sendBroadcast(intent1);

													} else if (content
															.substring(16, 20)
															.equals("0008")) {
														// 查询9组继电器通断时间
														Intent intent1 = new Intent(
																Constant.BROCAST_GET_9TIME);
														intent1.putExtra(
																"message",
																content);
														sendBroadcast(intent1);
													} else if (content
															.substring(16, 20)
															.equals("000C")) {
														// 查询上12个月的用电量

													} else if (content
															.substring(16, 20)
															.equals("000D")) {
														// 查询上12个月的用电平均功率

													} else if (content
															.substring(16, 20)
															.equals("000E")) {
														// 查询上30日的用电量

													} else if (content
															.substring(16, 20)
															.equals("000F")) {
														// 查询上30日的用电平均功率

													} else if (content
															.substring(16, 20)
															.equals("0010")) {
														// 查询上24小时的用电量

													} else if (content
															.substring(16, 20)
															.equals("0011")) {
														// 查询上24小时的用电平均功率

													} else if (content
															.substring(16, 20)
															.equals("0012")) {
														// 查询倒计时状态以及目标终点时间
														Intent intent1 = new Intent(
																Constant.BROCAST_COUNT_DOWN);
														intent1.putExtra(
																"message",
																content);
														sendBroadcast(intent1);
													} else if (content
															.substring(16, 20)
															.equals("0017")) {
														// 读取设备待机断电延时时间(开关无此项)Constant.BROCAST_GET_DAIJI_TIME
														Intent intent1 = new Intent(
																Constant.BROCAST_GET_DAIJI_TIME);
														intent1.putExtra(
																"message",
																content);
														sendBroadcast(intent1);

													} else if (content
															.substring(16, 20)
															.equals("0018")) {
														// 读取设备待机断电模式(开关无此项)

													} else if (content
															.substring(16, 20)
															.equals("0022")) {
														// 读取设备待机断电功率门限(开关无此项)

													} else if (content
															.substring(16, 20)
															.equals("0023")) {
														// 查询电价相关信息负荷门限
														Intent intent1 = new Intent(
																Constant.BROCAST_UNTIPIRCE_MSG);
														intent1.putExtra(
																"message",
																content);
														sendBroadcast(intent1);

													} else if (content
															.substring(16, 20)
															.equals("0025")) {
														// 查询设备心跳频率&模式

													} else if (content
															.substring(16, 20)
															.equals("0004")) {
														// 设置电价相关信息

													} else if (content
															.substring(16, 20)
															.equals("0006")) {
														// 设置定时计量的时间
														Intent intent1 = new Intent(
																Constant.BROCAST_SET_TIME_JILIANG);
														intent1.putExtra(
																"message",
																content);
														sendBroadcast(intent1);
													} else if (content
															.substring(16, 20)
															.equals("0009")) {
														// 设置9组继电器通断时间
														Intent intent1 = new Intent(
																Constant.BROCAST_SET_9TIME);
														intent1.putExtra(
																"message",
																content);
														sendBroadcast(intent1);
													} else if (content
															.substring(16, 20)
															.equals("000A")) {
														// 设置倒计时开时间
														Intent intent1 = new Intent(
																Constant.BROCAST_COUNT_DOWN_CLOSE);
														intent1.putExtra(
																"message",
																content);
														sendBroadcast(intent1);
													} else if (content
															.substring(16, 20)
															.equals("000B")) {
														// 设置倒计时关时间
														Intent intent1 = new Intent(
																Constant.BROCAST_COUNT_DOWN_OPEN);
														intent1.putExtra(
																"message",
																content);
														sendBroadcast(intent1);
													} else if (content
															.substring(16, 20)
															.equals("0013")) {
														// 设置插座继电器状态
														Intent intent = new Intent(
																Constant.BROCAST_ONLINE_ON_OFF_MSG);
														intent.putExtra(
																"message",
																message);
														sendBroadcast(intent);

													} else if (content
															.substring(16, 20)
															.equals("0015")) {
														// 设置设备是否待机断电(开关无此项)
														Intent intent = new Intent(
																Constant.BROCAST_SET_DAIJI_DUANDIAN);
														intent.putExtra(
																"message",
																content);
														sendBroadcast(intent);

													} else if (content
															.substring(16, 20)
															.equals("0016")) {
														// 设置设备待机断电延时时间(开关无此项)
														Intent intent = new Intent(
																Constant.BROCAST_SET_DAIJI_TIME);
														intent.putExtra(
																"message",
																content);
														sendBroadcast(intent);

													} else if (content
															.substring(16, 20)
															.equals("0019")) {
														// 设置电价
														Intent intent = new Intent(
																Constant.BROCAST_SAVE_DAIJI_DUANDIAN);
														intent.putExtra(
																"message",
																content);
														sendBroadcast(intent);

													} else if (content
															.substring(16, 20)
															.equals("0020")) {
														// 设置负荷门限
														Intent intent = new Intent(
																Constant.BROCAST_SAVE_DAIJI_DUANDIAN);
														intent.putExtra(
																"message",
																content);
														sendBroadcast(intent);

													} else if (content
															.substring(16, 20)
															.equals("0021")) {
														// 设置设备待机断电功率门限(开关无此项)

													} else if (content
															.substring(16, 20)
															.equals("0024")) {
														// 设置设备心跳频率&模式

													}else{
														//心跳数据
														//收到服务器发送来的消息：{"statusCode":0,"message":"发送指令给1个设备，其中成功1个"}
														String temp = obj.getString("message");
														if (TextUtils.isEmpty(temp)) {
															//如果为空则心跳数据断开重新连接socket
															mHandler.removeCallbacks(heartBeatRunnable);
															releaseLastSocket();
															new InitSocketThread().start(); 
														}
													}
												}

											} else if (code == 1) {
												Intent intent = new Intent(
														Constant.BROCAST_FAILE_MSG);
												String res = obj
														.getString("message");
												intent.putExtra("message", res);
												sendBroadcast(intent);
											} else if (code == 10) {
												String result = obj
														.getString("message");
												// 全开全关
												Intent intent = new Intent(
														Constant.BROCAST_ALL_OPEN_OR_CLOSE);
												intent.putExtra("message",
														result);
												sendBroadcast(intent);
											}
										} catch (JSONException e) {
											e.printStackTrace();
//											Intent intent = new Intent(Constant.BROCAST_SOCKET_FAIL);
//											sendBroadcast(intent);
										}

									}
								}
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
//						Intent intent = new Intent(Constant.BROCAST_SOCKET_FAIL);
//						sendBroadcast(intent);
						Intent intent = new Intent(
								Constant.SOCKET_SEND_FAIL);
						context.sendBroadcast(intent);
					}
				}

//			}
		}
	}

	@Override
	public void onDestroy() {
		mHandler.removeCallbacks(heartBeatRunnable);
		// mReadThread.release();
		// releaseLastSocket(BApplication.instance.socket);
		super.onDestroy();
	}

	/**
	 * 启动服务
	 * 
	 * @param context
	 * @param imei
	 */
	public static void startService(Context context) {
		Log.d(TAG, "startService()");
		Intent intent = new Intent(context, SocketLong.class);
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
		releaseLastSocket();
		Intent intent = new Intent(context, SocketLong.class);
		context.stopService(intent);
	}
	
}
