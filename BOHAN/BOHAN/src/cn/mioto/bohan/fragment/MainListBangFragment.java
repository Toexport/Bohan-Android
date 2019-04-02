package cn.mioto.bohan.fragment;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.mioto.bohan.BApplication;
import cn.mioto.bohan.Constant;
import cn.mioto.bohan.R;
import cn.mioto.bohan.activity.ScanActivity;
import cn.mioto.bohan.adapter.BangListAdapter;
import cn.mioto.bohan.entity.SingleDevice;
import cn.mioto.bohan.utils.Enterface;
import cn.mioto.bohan.utils.LogUtilNIU;
import cn.mioto.bohan.utils.NetworkUtils;

import com.google.gson.Gson;
import com.mioto.bohan.webservice.WebServiceClient;
import com.umeng.analytics.MobclickAgent;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import steed.framework.android.client.JsonClientHandler2;

/** 
 * 类说明：已绑定设备列表界面（Fragment）
 * 显示用户名下的所有的已经绑定设备。
 */

public class MainListBangFragment extends BaseFragment {
	View view;
	ListView lvListBang;
	LinearLayout llNoData;//如果设备为空，则显示
	private TextView tvBang;
	List<SingleDevice> devices=new ArrayList<>();//最初为空数据;
	//	private LoadingView loadingView;
	private LinearLayout llLoading;
	BangListAdapter adapter;

	//从设备集合提出Id集合
	private List<String> getDevicesIds(List<SingleDevice> devices){
		List<String> devicesIds = new ArrayList<>();
		for(int i = 0 ; i <devices.size(); i ++){
			String sDeviceId=devices.get(i).getDeviceID();
			devicesIds.add(sDeviceId);
		}
		return devicesIds;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view =inflater.inflate(R.layout.fragment_main_list_bang, null);
		lvListBang=(ListView) view.findViewById(R.id.lvListBang);
		llNoData=(LinearLayout) view.findViewById(R.id.llNoData);
		tvBang = (TextView) view.findViewById(R.id.tvBang);
		llLoading = (LinearLayout) view.findViewById(R.id.llLoading);
		/***************************************************/
		adapter = new BangListAdapter(getActivity(),devices,this);
		lvListBang.setAdapter(adapter);
		/***************************************************/
		tvBang.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(),ScanActivity.class);
				// 跳到扫码页面
				startActivity(intent);
			}
		});
		return view;
	}

	@Override
	public void onResume() {
		//		adapter.notifyDataSetChanged();
		super.onResume();
		getDeviceCount();
//		getDevices();
		// 在扫描后数据发生变化时更新
		//		if(adapter.getShowLeft()){
		//			//从其他地方返回这个界面时，如果左边正在出现（分享状态），让左边消失
		//			adapter.setShowLeft(false);
		//			adapter.notifyDataSetChanged();
		//		}
		MobclickAgent.onPageStart("绑定列表");
	}

	private void checkDeviceSize(){
		if(devices.size()<1){
			LogUtilNIU.e("list无设备");
			llNoData.setVisibility(View.VISIBLE);
			llLoading.setVisibility(View.GONE);//加载中除去
			lvListBang.setVisibility(View.GONE);
		}else{
			LogUtilNIU.e("list有数据");
			llNoData.setVisibility(View.GONE);
			llLoading.setVisibility(View.GONE);//加载中除去
			lvListBang.setVisibility(View.VISIBLE);
			adapter.notifyDataSetChanged();
		}
	}

	/**
	 * 从服务器获取用户名下的设备列表
	 */
	public void getDevices() {
		new Enterface("getAllDevices.act").doRequest(new JsonClientHandler2() {
			@Override
			public void onInterfaceSuccess(String message, String contentJson) {
				LogUtilNIU.value("绑定列表getAllDevices.act返回的content--->"+contentJson);
				devices.removeAll(devices);
				try {
					JSONArray arry = new JSONArray(contentJson);
					for(int i = 0 ; i < arry.length(); i ++){
						SingleDevice singleDevice = new SingleDevice();
						JSONObject obj = arry.getJSONObject(i);
						if(obj.has("position")&&!obj.isNull("position")){
							singleDevice.setDeviceLocation(obj.getString("position"));
						}else{
							singleDevice.setDeviceLocation("");
						}

						if(obj.has("id")&&!obj.isNull("id")){
							singleDevice.setDeviceID(obj.getString("id"));
						}else{
							singleDevice.setDeviceID("");
						}

						if(obj.has("sort")&&!obj.isNull("sort")){
							singleDevice.setDeviceType(obj.getString("sort"));
						}else{
							singleDevice.setDeviceType("");
						}
						if(obj.has("brand")&&!obj.isNull("brand")){
							singleDevice.setDeviceBrand(obj.getString("brand"));
						}else{
							singleDevice.setDeviceBrand("");
						}

						if(obj.has("name")&&!obj.isNull("name")){
							singleDevice.setDeviceName(obj.getString("name"));
						}else{
							singleDevice.setDeviceName("");
						}
						if(obj.has("appliancesort")&&!obj.isNull("appliancesort")){
							singleDevice.setDeviceAppType(obj.getString("appliancesort"));
						}else{
							singleDevice.setDeviceAppType("");
						}
						//08-29 23:18:39.654: W/VALUE(18242): 绑定列表getAllDevices.act返回的content--->
						//[{"position":"345","id":"681608050093","sort":"C","appliancesort":"日光灯","name":"设备681608050093","wifibssid":"64:09:80:53:89:82","brand":"345"},{"position":"345","id":"681608050094","sort":"C","appliancesort":"日光灯","name":"设备681608050094","brand":"345"}]
						if(obj.has("wifibssid")&&!obj.isNull("wifibssid")){
							LogUtilNIU.value(singleDevice.getDeviceID()+"查询得到wifibssid--->"+obj.getString("wifibssid"));
							singleDevice.setDeviceWIFIBSSID(obj.getString("wifibssid"));
						}else{
							singleDevice.setDeviceWIFIBSSID("");
						}
						devices.add(singleDevice);
					}
					LogUtilNIU.value("***打印生成的设备管理List为***********"+devices.toString());
					//得到用户名下的所有设备后，把他们的id存在全局
					BApplication.instance.setCurrentDeviceIds(getDevicesIds(devices));
					checkDeviceSize();
				} catch (JSONException e) {
					e.printStackTrace();
					LogUtilNIU.value("************设备列表抛异常"+e);
				}
			}

			@Override
			public void onInterfaceFail(String json) {

			}

			@Override
			public void onFailureConnected(Boolean canConnect) {
				// TODO Auto-generated method stub

			}
		});
	}

	public void update(){
//		getDevices();// 
		getDeviceCount();
		LogUtilNIU.circle("绑定列表更新");
	}


	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("绑定列表页"); 
	}

	/**
	 * 
	 * @Description:供外部调用的，出现左边选择布局的方法
	 * Parameters: 
	 * return:void
	 */
	public void showLeftLayout(){
		adapter.setShowLeft(true);
		adapter.notifyDataSetChanged();
	}
	/**
	 * 
	 * @Description:供外部调用的，隐藏左边选择布局的方法
	 * Parameters: 
	 * return:void
	 */
	public void hideLeftLayout(){
		adapter.setShowLeft(false);
		adapter.notifyDataSetChanged();
	}

	/**
	 * 
	 * @Description:执行全选方法
	 * Parameters: 
	 * return:void
	 */
	public void selectAll(){
		adapter.selectAll();
		// 数量设为list的长度
		// 刷新listview和TextView的显示
		adapter.notifyDataSetChanged();
	}

	/**
	 * 
	 * @Description:执行全不选的方法
	 */
	public void disSelectAll(){//全不选
		adapter.disSelectAll();
		// 数量设为list的长度
		// 刷新listview和TextView的显示
		adapter.notifyDataSetChanged();
	}

	/**
	 * @Description:统计选中的有哪些id,并保存到全局变量
	 */
	public List<String> howManyIsSelected(){
		List<String> idSelectedList = new ArrayList<>();
		idSelectedList=adapter.howManySelected();
		return idSelectedList;
	}
	
	

	// 获取设备列表
	private void getDeviceCount() {
//		if (NetworkUtils.isNetworkConnected(getActivity())) {
			// getDefaultProgressDialog(getString(R.string.loging),false);
			// isRequesting = true;
			new Thread() {
				public void run() {
					Message msg = new Message();
					msg.what = Constant.MSG_FAILURE;
//					msg.obj = getString(R.string.cannot_connection_server);
					try {
						// HttpUtils.TOKIN = "";
						String result = WebServiceClient.CallWebService(
								"GetUserDeviceList", null);
						Log.d("resultgetDeviceCount", result);
						if (result != null) {
							JSONObject obj = new JSONObject(result);
							int code = obj.getInt("statusCode");
							if (code == 0) {
								devices.removeAll(devices);
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
									devices.add(singleDevice);
								}
								//得到用户名下的所有设备后，把他们的id存在全局
								BApplication.instance.setCurrentDeviceIds(getDevicesIds(devices));
								msg.what = Constant.MSG_SUCCESS;
							} else {
								msg.obj = obj.getString("message");
								msg.what = Constant.MSG_FAILURE;
							}
						}
						msg.what = Constant.MSG_SUCCESS;
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
		} 
//		else {
//			defalutHandler.sendEmptyMessage(Constant.MSG_NETWORK_ERROR);
//		}
//	}
	private Handler defalutHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Constant.MSG_SUCCESS:
				checkDeviceSize();
				break;
			case Constant.MSG_FAILURE:
//				toast((String) msg.obj);
				break;
			case Constant.MSG_EXCPTION:
//				toast(ExceptionManager.getErrorDesc(getApplicationContext(),
//						(Exception) msg.obj));
				break;
			case Constant.MSG_TIME_OUT:
//				toast(getString(R.string.connection_timeout));
				break;
			case Constant.MSG_NETWORK_ERROR:
//				toast(getString(R.string.no_connection));
				break;
			}
		}

		;
	};
}
