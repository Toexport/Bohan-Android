package cn.mioto.bohan.adapter;

import java.util.ArrayList;
import java.util.List;

import cn.mioto.bohan.Constant;
import cn.mioto.bohan.R;
import cn.mioto.bohan.activity.OnlineStatusActivity;
import cn.mioto.bohan.adapter.TypeListAdapter.ViewHolder;
import cn.mioto.bohan.utils.Enterface;
import cn.mioto.bohan.utils.LogUtilNIU;
import cn.mioto.bohan.utils.ToastUtils;
import cn.mioto.bohan.view.SwitchViewOnlineType;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;
import steed.framework.android.client.JsonClientHandler2;

/** 
* 类说明：对位置和用电类型adapter的封装
*/
public class BaseTypeListAdapter extends BaseAdapter{
	protected String kindkind;
	List<String> datas = new ArrayList<>();
	Context context;
	LayoutInflater inflater;

	public BaseTypeListAdapter(Context context,List<String> datas) {
		inflater = LayoutInflater.from(context);
		this.context=context;
		if(datas!=null){
			this.datas=datas;
		}else{
			datas= new ArrayList<String>();
			this.datas=datas;
		}
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
			convertView = inflater.inflate(R.layout.item_online_type_list,
					null);
			viewHolder = new ViewHolder();
			viewHolder.switchView = (SwitchViewOnlineType) convertView.findViewById(R.id.switchView);
			viewHolder.tvType = (TextView) convertView.findViewById(R.id.tvType);
			viewHolder.tvOn = (TextView) convertView.findViewById(R.id.tvOn);
			viewHolder.tvOff = (TextView) convertView.findViewById(R.id.tvOff);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder) convertView.getTag();
		}
		final String type = datas.get(position);
		viewHolder.tvType.setText(type);
		viewHolder.tvType.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context,OnlineStatusActivity.class);
				String thisType = type;
				Bundle b = new Bundle();
				b.putString(Constant.INTENT_KEY_THIS_SORT, thisType);
				b.putString("interfaceType", "appliancesort");
				intent.putExtras(b);
				context.startActivity(intent);
			}
		});

		viewHolder.tvOn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 全开被点击
				setToControl(3,type);
				ToastUtils.shortToast(context, context.getString(R.string.control)+type+context.getString(R.string.all_open)+"content"+3);
				//发广播命令状态出现
				Intent intent = new Intent();
				intent.setAction(Constant.BROCAST_ONLINE_LIST_SHOW_OPEN_DIALOG);
				context.sendBroadcast(intent );
			}
		});

		viewHolder.tvOff.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 全关被点击
				setToControl(2,type);
				ToastUtils.shortToast(context, context.getString(R.string.control)+type+context.getString(R.string.all_close)+"content"+2);
				//发广播命令状态出现
				Intent intent = new Intent();
				intent.setAction(Constant.BROCAST_ONLINE_LIST_SHOW_CLOSE_DIALOG);
				context.sendBroadcast(intent );
			}
		});
		return convertView;
	}

	//批量控制打开或关闭
	protected void setToControl(int i, String type) {
		new Enterface("batchSendToDevice.act").addParam("appliancesort", type).addParam("content", i).doRequest(new JsonClientHandler2() {

			@Override
			public void onInterfaceSuccess(String message, String contentJson) {
				LogUtilNIU.value("控制开关返回信息---->"+contentJson);
				//3a681608050094000013000100330d
				if(contentJson.equals("")){
					ToastUtils.shortToast(context, context.getString(R.string.control_equipment_failure_or_the_class_online));
					Intent intent = new Intent();
					intent.setAction(Constant.BROCAST_ONLINE_LIST_DIMISS_DIALOG);
					context.sendBroadcast(intent);
				}else{
					ToastUtils.shortToast(context, context.getString(R.string.control_all_or_part_of_success));
					Intent intent = new Intent();
					intent.setAction(Constant.BROCAST_ONLINE_LIST_DIMISS_DIALOG);
					context.sendBroadcast(intent);
				}
			}

			@Override
			public void onInterfaceFail(String json) {
				LogUtilNIU.value("控制开关返回信息onInterfaceFail---->"+json);
				Intent intent = new Intent();
				intent.setAction(Constant.BROCAST_ONLINE_LIST_DIMISS_DIALOG);
				context.sendBroadcast(intent);
			}

			@Override
			public void onFailureConnected(Boolean canConnect) {
				LogUtilNIU.value("控制开关返回信息onFailureConnected---->"+canConnect);
				Intent intent = new Intent();
				intent.setAction(Constant.BROCAST_ONLINE_LIST_DIMISS_DIALOG);
				context.sendBroadcast(intent);
			}
		});
	}

	public class ViewHolder{
		TextView tvType;
		TextView tvOn;
		TextView tvOff;
		SwitchViewOnlineType switchView;
	}
}
