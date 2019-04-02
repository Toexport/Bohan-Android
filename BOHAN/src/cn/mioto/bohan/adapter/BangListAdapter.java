package cn.mioto.bohan.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.mioto.bohan.BApplication;
import cn.mioto.bohan.R;
import cn.mioto.bohan.activity.ResetDataActivity;
import cn.mioto.bohan.entity.SingleDevice;
import cn.mioto.bohan.fragment.MainListBangFragment;
import cn.mioto.bohan.fragment.MainListBangFragment2;
import cn.mioto.bohan.utils.Enterface;
import cn.mioto.bohan.utils.ExceptionUtil;
import cn.mioto.bohan.utils.LogUtilNIU;
import cn.mioto.bohan.utils.ToastUtils;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import steed.framework.android.client.JsonClientHandler2;

/** 
 * 类说明：设备列表的adapter
 */
public class BangListAdapter extends BaseAdapter {
	// 填充数据的list
	List<SingleDevice> deviceList = new ArrayList<>();
	Context context;
	LayoutInflater inflater;
	MainListBangFragment listfragment;

	private Boolean showLeft = false;

	public Boolean getShowLeft() {
		return showLeft;
	}

	public void setShowLeft(Boolean showLeft) {
		this.showLeft = showLeft;
	}

	// HashMap用来控制选中状况
	private static HashMap<Integer,Boolean> isSelected=new HashMap<Integer, Boolean>();

	public static HashMap<Integer,Boolean> getIsSelected() {
		return isSelected;
	}

	public BangListAdapter(Context context,List<SingleDevice> datas,MainListBangFragment listfragment ){
		inflater = LayoutInflater.from(context);
		this.listfragment = listfragment;
		this.context=context;
		if(datas!=null){
			this.deviceList=datas;
		}else{
			datas= new ArrayList<SingleDevice>();
			this.deviceList=datas;
		}
		//初始化选择
		initDate();//注意初始数据就为0的情况，还没有得到
	}

	// 初始化isSelected的数据,最初的时候设置为全部都没有被选中
	private void initDate(){
		if(deviceList.size()>0){
			for(int i=0; i<deviceList.size();i++) {
				size = deviceList.size();
				getIsSelected().put(i,false);
			}
		}else{
			for(int i=0; i<BApplication.instance.getDeviceQua();i++) {
				getIsSelected().put(i,false);
			}
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

	private int size = -100;//列表长度
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if(size==-100){//没有进行过其他任何赋值
			if(deviceList.size()>0){
				//把数据保存到sp
				size = deviceList.size();
				for(int i = 0 ; i <size; i ++){
					getIsSelected().put(i,false);
				}
			}else{
				//list还没有数据时就不执行
				
			}
		}
		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_bang_list,
					null);
			viewHolder = new ViewHolder();
			viewHolder.deviceName = (TextView) convertView
					.findViewById(R.id.tvDeviceName);
			viewHolder.deviceId = (TextView) convertView
					.findViewById(R.id.tvDeviceId);
			viewHolder.deviceType=(TextView) convertView
					.findViewById(R.id.tvDeviceType);
			viewHolder.deviceLocation=(TextView) convertView
					.findViewById(R.id.tvDeviceLocation);
			viewHolder.leftLayout = (LinearLayout) convertView.findViewById(R.id.leftLayout);
			viewHolder.ivCheck = (ImageButton) convertView.findViewById(R.id.ivCheck);//左边选择区域
			viewHolder.rightLayout = (LinearLayout) convertView.findViewById(R.id.rightLayout);//右边区域
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		if(showLeft){//如果左边区域布尔值为显示则显示
			viewHolder.leftLayout.setVisibility(View.VISIBLE);
		}else{//否则隐藏
			viewHolder.leftLayout.setVisibility(View.GONE);
		}
		SingleDevice singleDevice = deviceList.get(position);

		viewHolder.deviceId.setText(context.getResources().getString(R.string.id_)+singleDevice.getDeviceID());
		viewHolder.deviceName.setText(singleDevice.getDeviceName());
		String type = singleDevice.getDeviceType();
		if(type.equals("C")){
			type = context.getString(R.string.socket);
		}else if (type.equals("K")){
			type = context.getString(R.string.switch2);
		}
		viewHolder.deviceType.setText(context.getResources().getString(R.string.type_)+type);
		viewHolder.deviceLocation.setText(context.getResources().getString(R.string.location_)+singleDevice.getDeviceLocation());
		//设置区域点击跳转到每个Item细项
		viewHolder.rightLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//点击绑定设备列表的每个Item，跳转至重设设备信息页
				Intent intent = new Intent(context,ResetDataActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//启动activity时，设置为FLAG_ACTIVITY_NEW_TASK
				//设置当前对象
				BApplication.instance.setCurrentDevice(deviceList.get(position));
				context.startActivity(intent);
			}
		});
		viewHolder.rightLayout.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				// TODO 右边区域被长按了以后
				//弹出对话框，询问是否需要删除该设备
				//弹出一个对话框
				AlertDialog.Builder alertDialogBuilder=new AlertDialog.Builder(context);
				alertDialogBuilder.setMessage(context.getString(R.string.delete_account));//设置显示文本
				alertDialogBuilder.setPositiveButton(R.string.cancle, null);
				alertDialogBuilder.setNegativeButton(R.string.sumit, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						//要求服务器删除此设备，服务器回调以后，更新Listview显示
						deleteTheDeviceFromService(deviceList.get(position).getDeviceID());
					}

					private void deleteTheDeviceFromService(String deviceID) {
						//设备解除绑定
						new Enterface("deleteDevice.act").addParam("deviceid",deviceID).doRequest(new JsonClientHandler2() {
							@Override
							public void onInterfaceSuccess(String message, String contentJson) {
								LogUtilNIU.value("要求删除设备后返回message---->"+message);
								ToastUtils.shortToast(context, message);
								//要求设备管理页更新数据
								listfragment.getDevices();
							}

							@Override
							public void onInterfaceFail(String json) {
								LogUtilNIU.value("删除设备失败---->"+json);
								ToastUtils.shortToast(context, json);
							}

							@Override
							public void onFailureConnected(Boolean canConnect) {
								ToastUtils.shortToast(context, context.getString(R.string.no_connection));
							}
						});

					}
				});
				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.show();
				return false;
			}
		});
		//设置左边的选择图标点击效果
		final ImageButton ib = viewHolder.ivCheck;
		if(size!=-100){//如果有数据
			try {
				if(getIsSelected().get(position)){//如果当前是被选中状态
					ib.setImageDrawable(context.getResources().getDrawable(R.drawable.manage_device_tick));
				}else{
					ib.setImageDrawable(context.getResources().getDrawable(R.drawable.manage_device_untick));
				}
				viewHolder.ivCheck.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if(!getIsSelected().get(position)){//没有被选中则设置为被选择，否则设置为没有选中
							ib.setImageDrawable(context.getResources().getDrawable(R.drawable.manage_device_tick));
							getIsSelected().put(position, true);//标记为被中
						}else{
							ib.setImageDrawable(context.getResources().getDrawable(R.drawable.manage_device_untick));
							getIsSelected().put(position, false);//标记没有被中
						}
					}
				});
			} catch (Exception e) {
				ExceptionUtil.handleException(e);
			}
		}
		return convertView;
	}

	public class ViewHolder{
		TextView deviceName;
		TextView deviceId;
		TextView deviceType;
		TextView deviceLocation;
		LinearLayout leftLayout;
		LinearLayout rightLayout;
		ImageButton ivCheck;
	}

	/**
	 * 
	 * @Description:统计有多少个被选中
	 */
	private List<String> selectedItemIds = new ArrayList<>();
	//获得被选中项目的id列表
	public List<String> howManySelected(){
		selectedItemIds.clear();
		for(int i = 0 ; i < deviceList.size(); i ++){
			if(getIsSelected().get(i)){
				//当前被选中
				String seledtedId=deviceList.get(i).getDeviceID();//获得被选中的项的id
				selectedItemIds.add(seledtedId);
			}
		}
		return selectedItemIds;
	}

	public void selectAll(){
		// 遍历list的长度，将MyAdapter中的map值全部设为true
		for (int i = 0; i < deviceList.size(); i++) {//把列表中所有设置为被选
			getIsSelected().put(i, true);
		}
	}

	public void disSelectAll(){
		// 遍历list的长度，将MyAdapter中的map值全部设为true
		for (int i = 0; i < deviceList.size(); i++) {//把列表中所有设置为被选
			getIsSelected().put(i, false);
		}
	}
}
