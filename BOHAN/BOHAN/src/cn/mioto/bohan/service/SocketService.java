package cn.mioto.bohan.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

/** 
 * 类说明：用于连接socket的service
 * 作者：  jiemai liangminhua 
 * 创建时间：2016年6月4日 下午8:59:05 
 */
public class SocketService extends Service {
	public static final String SERVERIP = "192.168.31.137"; //your computer IP address should be written here
	public static final int SERVERPORT = 8899;
	PrintWriter out;
	BufferedReader in;
	Socket socket;
	InetAddress serverAddr;
	String content;
	//接收线程发送过来信息
	//    public static Handler mHandler = new Handler() {
	//        public void handleMessage(Message msg) {
	//            super.handleMessage(msg);
	//        }
	//    };


	@Override
	public IBinder onBind(Intent intent) {
		Log.e("TCP Client", "I am in 服务的 Ibinder onBind method");
		return myBinder;
	}

	private final IBinder myBinder = new LocalBinder();
	//	TCPClient mTcpClient = new TCPClient();

	public class LocalBinder extends Binder {
		public SocketService getService() {
			Log.e("TCP Client", "返回服务对象给activity");
			return SocketService.this;

		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		//服务被启动
		Log.e("TCP Client", "服务onCreat()");
	}

	public void IsBoundable(){
		Toast.makeText(this,"绑定成功。I bind like butter", Toast.LENGTH_LONG).show();
	}
	/*
	 * 发送消息到服务器
	 */
	public void sendMessage(String message){
		if (out != null && !out.checkError()) {
			try {
				message = new String(message.getBytes(),"GBK");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				Log.e("TCP Client", "编码转换错误",e);
			}
			Log.e("TCP Client", "发送消息，消息为"+message);
			out.println(message);
			out.flush();
		}
	}
	//接收消息，启动线程，阻塞
	public void receiveMessage(){
		ReceiveThread rt = new ReceiveThread();
		rt.start();
	}
	public class ReceiveThread extends Thread{

		@Override
		public void run() {
			try {
				while (true) {
					if (!socket.isClosed()) {
						if (socket.isConnected()) {
							if (!socket.isInputShutdown()) {
								content = in.readLine();
									content += "\n";
									Log.e("TCP Client", "接收到数据"+content);
									// mHandler.sendMessage(mHandler.obtainMessage());
								
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	/*
	 *service 启动的时候执行
	 */
	@Override
	public int onStartCommand(Intent intent,int flags, int startId){
		super.onStartCommand(intent, flags, startId);
		Log.e("TCP Client","onStartCommand启动线程创建socket,返回socket,输入流，输出流到service的全局");
		//		  Toast.makeText(this,"Service created ...", Toast.LENGTH_LONG).show();
		Runnable connect = new connectSocket();
		new Thread(connect).start();
		return START_STICKY;
	}

	class connectSocket implements Runnable {

		@Override
		public void run() {

			try { 

				serverAddr = InetAddress.getByName(SERVERIP);//服务端ip地址

				socket = new Socket(serverAddr, SERVERPORT);//创建连接服务器的socket

				try {
					out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
					Log.e("TCP Client", "输出流创建成功");
				} 
				catch (Exception e) {
					Log.e("TCP Client", "输出流创建失败: Error", e);
				}

				try {
					in = new BufferedReader(new InputStreamReader(socket
							.getInputStream()));
					Log.e("TCP Client", "输入流创建成功");
				} 
				catch (Exception e) {
					Log.e("TCP Client", "输入流创建失败: Error", e);
				}

			} catch (Exception e) {

				Log.e("TCP Client", "Socket创建失败", e);

			}

		}

	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			socket.close();
			Log.e("TCP Client", "服务执行onDestroy(),Socket关闭");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		socket = null;
	}


}