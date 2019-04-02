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
import cn.mioto.bohan.activity.OnlineStatusActivity;
import cn.mioto.bohan.utils.ModbusCalUtil;
import cn.mioto.bohan.utils.ToastUtils;
import cn.mioto.bohan.view.SwitchViewOnlineType;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * 类说明：主页的设备分类列表adapter
 * 
 * 
 */
public class LocationListAdapter extends BaseAdapter {
	// 传到服务器
	String type = "position";
	List<String> datas = new ArrayList<>();
	Context context;
	LayoutInflater inflater;

	public static Socket socket;
	public static BufferedReader in;
	public static PrintWriter out;
	public ReceiveThread receiveThread;

	public LocationListAdapter(Context context, List<String> datas) {
		inflater = LayoutInflater.from(context);
		this.context = context;
		if (datas != null) {
			this.datas = datas;
		} else {
			datas = new ArrayList<String>();
			this.datas = datas;
		}
		// 初始化Socket
		socket = BApplication.instance.getSocket();
	}

	@Override
	public int getCount() {
		return datas.size();
	}

	@Override
	public Object getItem(int position) {
		return datas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = inflater
					.inflate(R.layout.item_online_type_list, null);
			viewHolder = new ViewHolder();
			viewHolder.tvType = (TextView) convertView
					.findViewById(R.id.tvType);
			viewHolder.tvOn = (TextView) convertView.findViewById(R.id.tvOn);
			viewHolder.tvOff = (TextView) convertView.findViewById(R.id.tvOff);
			viewHolder.switchView = (SwitchViewOnlineType) convertView
					.findViewById(R.id.switchView);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		final String type = datas.get(position);
		viewHolder.tvType.setText(type);
		viewHolder.tvType.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, OnlineStatusActivity.class);
				String thisType = type;
				Bundle b = new Bundle();
				b.putString(Constant.INTENT_KEY_THIS_SORT, thisType);
				b.putString("PosName", thisType);
				b.putString("LoadName", "");
				intent.putExtras(b);
				context.startActivity(intent);
			}
		});

		viewHolder.tvOn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO 全开被点击
				setToControl("00", type);
				// ToastUtils.shortToast(context, "控制" + type + "设备全部打开");
				// 发广播命令状态出现
				Intent intent = new Intent();
				intent.setAction(Constant.BROCAST_ONLINE_LIST_SHOW_OPEN_DIALOG);
				context.sendBroadcast(intent);
			}
		});

		viewHolder.tvOff.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO 全关被点击
				setToControl("01", type);
				// ToastUtils.shortToast(context, "控制" + type + "设备全部关闭");
				// 发广播命令状态出现
				Intent intent = new Intent();
				intent.setAction(Constant.BROCAST_ONLINE_LIST_SHOW_CLOSE_DIALOG);
				context.sendBroadcast(intent);
			}
		});

		return convertView;
	}

	protected void setToControl(String i, String type) {
		String verCode = ModbusCalUtil.verNumber("13570856740" + "10010011");
		int length = ("13570856740;" + "PosName;"+ type + ";" + i).length();
		final String message = "3A" + "100200"+length + "13570856740;" + "PosName;"
				+ type + ";" + i + "0D";// 0001
		BApplication.instance.socketSend(message);
		receiveThread = new ReceiveThread();
		receiveThread.start();

		// new Enterface("batchSendToDevice.act").addParam("position",
		// type).addParam("content", i).doRequest(new JsonClientHandler2() {
		//
		// @Override
		// public void onInterfaceSuccess(String message, String contentJson) {
		// LogUtilNIU.value("控制开关返回信息---->"+contentJson);
		// if(contentJson.equals("")){
		// ToastUtils.shortToast(context, "控制失败或该类无在线设备");
		// Intent intent = new Intent();
		// intent.setAction(Constant.BROCAST_ONLINE_LIST_DIMISS_DIALOG);
		// context.sendBroadcast(intent);
		// }else{
		// ToastUtils.shortToast(context, "全部或部分控制成功");
		// Intent intent = new Intent();
		// intent.setAction(Constant.BROCAST_ONLINE_LIST_DIMISS_DIALOG);
		// context.sendBroadcast(intent);
		// }
		// }
		//
		// @Override
		// public void onInterfaceFail(String json) {
		// LogUtilNIU.value("控制开关返回信息onInterfaceFail---->"+json);
		// Intent intent = new Intent();
		// intent.setAction(Constant.BROCAST_ONLINE_LIST_DIMISS_DIALOG);
		// context.sendBroadcast(intent);
		// }
		//
		// @Override
		// public void onFailureConnected(Boolean canConnect) {
		// LogUtilNIU.value("控制开关返回信息onFailureConnected---->"+canConnect);
		// Intent intent = new Intent();
		// intent.setAction(Constant.BROCAST_ONLINE_LIST_DIMISS_DIALOG);
		// context.sendBroadcast(intent);
		// }
		// });
	}

	public class ViewHolder {
		TextView tvType;
		TextView tvOn;
		TextView tvOff;
		SwitchViewOnlineType switchView;
	}

	/**
	 * 接收数据线程
	 */
	private class ReceiveThread extends Thread {
		@Override
		public void run() {
			Message msg = new Message();
			msg.what = Constant.MSG_FAILURE;
			msg.obj = context.getString(R.string.cannot_connection_server);
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
							handler.sendEmptyMessage(Constant.MSG_SUCCESS);
							Intent intent = new Intent();
							intent.setAction(Constant.BROCAST_ONLINE_LIST_DIMISS_DIALOG);
							context.sendBroadcast(intent);
						}else {
							msg.what = Constant.MSG_EXCPTION;
							handler.sendEmptyMessage(Constant.MSG_EXCPTION);
							Intent intent = new Intent();
							intent.setAction(Constant.BROCAST_ONLINE_LIST_DIMISS_DIALOG);
							context.sendBroadcast(intent);
						}
					}
				} catch (Exception e) {
					Log.e("Receive", e.getMessage());
					e.printStackTrace();
					msg.obj = e;
					msg.what = Constant.MSG_EXCPTION;
					handler.sendEmptyMessage(Constant.MSG_EXCPTION);
					Intent intent = new Intent();
					intent.setAction(Constant.BROCAST_ONLINE_LIST_DIMISS_DIALOG);
					context.sendBroadcast(intent);
				}
			} else {
				try {
					Thread.sleep(1000L);
				} catch (InterruptedException e) {
					e.printStackTrace();
					msg.obj = e;
					msg.what = Constant.MSG_EXCPTION;
					handler.sendEmptyMessage(Constant.MSG_EXCPTION);
					Intent intent = new Intent();
					intent.setAction(Constant.BROCAST_ONLINE_LIST_DIMISS_DIALOG);
					context.sendBroadcast(intent);
				}
			}
			handler.sendMessage(msg);
		}
	}

	public Handler handler = new Handler() {// 处理返回的消息
		public void handleMessage(android.os.Message msg) {
			if (msg.what == Constant.MSG_SUCCESS) {
				ToastUtils.shortToast(context, "控制成功");
			} else if(msg.what == Constant.MSG_EXCPTION) {
				ToastUtils.shortToast(context, "控制失败或该类无在线设备");
			}
		}
	};

}
