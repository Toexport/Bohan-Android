package cn.mioto.bohan;

import cn.mioto.bohan.activity.SteedAppCompatActivity;
import cn.mioto.bohan.utils.ToastUtils;

import com.vilyever.socketclient.SocketClient;
import com.vilyever.socketclient.helper.SocketClientAddress;
import com.vilyever.socketclient.helper.SocketClientDelegate;
import com.vilyever.socketclient.helper.SocketResponsePacket;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/** 
 * 类说明：用于测试socket连接的类。此Activity用于创建socket连接
 */
public class TestSocketActivity extends SteedAppCompatActivity{
	/**********DECLARES*************/
	private EditText editText1;
	private Button button1;
	private TextView textView1;
	private Button button2;
	/*******socket连接*******************************************/
	private SocketClient socketClient;
	private String host="192.168.31.137";
	private Boolean canshowDisConnectMsg;
	private ProgressDialog disconnectDialog;
	/***************************************************/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*******连接服务器********************************************/
		link();
	}

	/**********************************************************/
	/**
	 * 连接服务器。第一次发送数据，初始化数据显示。
	 */
	private void link() {
		socketClient = new SocketClient(new SocketClientAddress(host, 3333, 15 * 1000));
		socketClient.connect();
		socketClient.registerSocketClientDelegate(new SocketClientDelegate() {

			@Override
			public void onConnected(SocketClient client) {
				canshowDisConnectMsg=true;
				if(disconnectDialog!=null){
					disconnectDialog.dismiss();
					ToastUtils.shortToast(TestSocketActivity.this, "连接成功了");
				}
				/******最初发送的消息*******************************************/
				String message="HELLO!";
				socketClient.sendString(message);
			}

			@Override
			public void onResponse(SocketClient client, SocketResponsePacket responsePacket) {
				String resMessage = responsePacket.getMessage();
				/****判断接收到的消息类型***********************************************/
				showRecMessage();
				showSetMessage();
				textView1.setText(resMessage);
			}

			private void showSetMessage() {
				// TODO 最初的显示
				
			}

			private void showRecMessage() {
				// TODO 设置后的显示
				
			}

			@Override
			public void onDisconnected(SocketClient client) {
				// 断开连接回调，可在此实现自动重连
				if(canshowDisConnectMsg){
					/******用dialog提示****************************************/
					disconnectDialog = new ProgressDialog(TestSocketActivity.this);
					disconnectDialog.setMessage("和插座的连接断开了,请检查你的插座是否断电，正尝试为你连接...");
					disconnectDialog.setCanceledOnTouchOutside(false);
					disconnectDialog.setCancelable(false);
					disconnectDialog.setButton(DialogInterface.BUTTON_POSITIVE,
							"返回", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
						}
					});
					disconnectDialog.setButton(DialogInterface.BUTTON_NEGATIVE,"帮助",new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub

						}
					});
					disconnectDialog.show();
					canshowDisConnectMsg=false;
				}
				socketClient.connect();
			}

		});
	}

	@Override
	protected Integer getLayoutId() {
		return R.layout.activity_socket_test;
	}

	@Override
	public void onClick(View v) {
		if(v==button1){//发送数据
			settingMessage();
		}else if (v==button2){//跳到另一个连接socket
			
		}
		super.onClick(v);
	}

	/**********************************************************/
	/**
	 * 设置发送的信息
	 */
	private void settingMessage() {
		String message="";
		/********发送消息*****************************************/
		socketClient.sendString(message);
	}

	/**********************************************************/
	/**
	 * 
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
	}


	//	/**********DECLARES*************/
	//	private EditText editText1;
	//	private Button button1;
	//	private Button button2;
	//	/**********SOCKET************/
	//	private String ip="";
	//	private int port =8899 ;
	//	private BufferedReader in;
	//	private PrintWriter out;
	//	private Boolean isInterrupted;
	//	private Socket client=null;
	//	private int CONNECT_SUCCESS=100;
	//	private int RECEIVE_SUCCESS=200;
	//	private String content;
	//	/*
	//	 * 处理socket连接成功以及socket接收数据成功的Intent
	//	 */
	//	Handler handler = new Handler(){
	//		@Override
	//		public void handleMessage(Message msg) {
	//			if(msg.what==CONNECT_SUCCESS){
	//				if(client!=null){
	//					in=SocketConnect.in;
	//					out=SocketConnect.out;
	//					client=SocketConnect.client;
	//					/*
	//					 * 发送数据
	//					 */
	//					if (client.isConnected()) {
	//						if (!client.isOutputShutdown()) {
	//							String send="ABCD";
	//							out.println(send);
	//						}
	//					}else{
	//						new Thread(runnable).start();
	//					}
	//					/*
	//					 * 连接后启动接收数据的线程
	//					 */
	//					if (client.isConnected()) {
	//						new Thread(TestSocketActivity.this).start();
	//					}else{
	//						new Thread(runnable).start();
	//					}
	//				}
	//			}else if(msg.what==RECEIVE_SUCCESS){
	//				if(content!=null){
	//					Toast.makeText(TestSocketActivity.this, "收到数据"+content, 0).show();
	//				}
	//			}
	//		}
	//	};
	//
	//	@Override
	//	protected void onCreate(Bundle savedInstanceState) {
	//		super.onCreate(savedInstanceState);
	//		setContentView(R.layout.activity_socket_test);
	//		bindViews();
	//		/*
	//		 * 初始化第一次连接
	//		 */
	//		new Thread(runnable).start();
	//		client=SocketConnect.client;
	//		/*
	//		 * 控制线程
	//		 */
	//		isInterrupted=false;
	//	}
	//	
	//
	//	/*
	//	 * 初始化socket的线程
	//	 */
	//	Runnable runnable = new Runnable(){
	//		@Override
	//		public void run() {
	//			client=SocketConnect.init(ip,port);
	//			if(client!=null){
	//				Message msg = new Message();
	//				msg.what=CONNECT_SUCCESS;
	//				handler.sendMessage(msg);
	//			}
	//		}
	//	};
	//
	//	private void bindViews() {
	//		editText1 = (EditText) findViewById(R.id.editText1);
	//		button1 = (Button) findViewById(R.id.button1);
	//		button2 = (Button) findViewById(R.id.button2);
	//	}
	//
	//	public void onClick(View v){
	//		if(v==button1){
	//			if (client.isConnected()) {
	//				String msg="";
	//				msg=editText1.getText().toString().trim();
	//				try {
	//					msg = new String(msg.getBytes(),"GBK");
	//				} catch (UnsupportedEncodingException e) {
	//					ExceptionUtil.handleException(e);
	//				}
	//				if (!client.isOutputShutdown()) {
	//					try {
	//						out.println(msg);
	//						Toast.makeText(this, "发送数据", 0).show();
	//					} catch (Exception e) {
	//						ExceptionUtil.handleException(e);
	//					}
	//				}
	//			}
	//		}else if (v==button2){
	//			Intent intent = new Intent(TestSocketActivity.this,TestSocketActivity2.class);
	//			startActivity(intent );
	//		}
	//	}
	//    /*
	//     * 启动线程接收数据
	//     */
	//	public void run() {
	//		try {
	//			while (!isInterrupted) {
	//				if (!client.isClosed()) {
	//					if (client.isConnected()) {
	//						if (!client.isInputShutdown()) {
	//							if ((content = in.readLine()) != null) {
	//								content += "\n";
	//								Message msg = new Message();
	//								msg.what=RECEIVE_SUCCESS;
	//								handler.sendMessage(msg);
	//							} 
	//						}
	//					}
	//				}
	//			}
	//		} catch (Exception e) {
	//			ExceptionUtil.handleException(e);
	//		}
	//	}
	//
	//	/*
	//	 * 手机按了返回键，停止线程
	//	 */
	//	@Override
	//	public void finish() {
	//		isInterrupted = true;
	//		super.finish();
	//	}
	//
	//	@Override
	//	protected void onDestroy() {
	//		super.onDestroy();
	//		//		doUnbindService();//取消绑定服务
	//	}
}
