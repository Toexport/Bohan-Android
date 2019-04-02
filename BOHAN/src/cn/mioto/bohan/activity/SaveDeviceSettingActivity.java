package cn.mioto.bohan.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import cn.mioto.bohan.BApplication;
import cn.mioto.bohan.Constant;
import cn.mioto.bohan.R;
import cn.mioto.bohan.entity.SingleDevice;
import cn.mioto.bohan.utils.Enterface;
import cn.mioto.bohan.utils.ExceptionUtil;
import cn.mioto.bohan.utils.LogUtilNIU;
import cn.mioto.bohan.utils.ViewUtil;
import cn.mioto.bohan.view.pop.DropDownMenu;
import cn.mioto.bohan.view.pop.DropDownMenuTextView;
import cn.mioto.bohan.view.pop.OnMenuSelectedListener;

import com.umeng.analytics.MobclickAgent;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import steed.framework.android.client.JsonClientHandler2; 

/** 
 * 类说明：保存配置页面 带toolbar返回键
 */
public class SaveDeviceSettingActivity extends SteedAppCompatActivity{
	/***************************************************/
	//控件
	private EditText etDeviceName;
	private TextView tvDeviceId;
	private LinearLayout llLocationSet;
	private TextView tvLocationName;
	private LinearLayout llBrandSet;
	private LinearLayout llRemindSmarkLinkSpace;
	private TextView tvBrandName;
	private TextView tvSave;
	private LinearLayout refresh1;
	private LinearLayout refresh2;
	private LinearLayout refresh3;
	private TextView tvSmarkLink;
	private TextView tvNoLink;
	private LinearLayout llRemindSmarkLink;
	private SingleDevice singledevice;
	private String deviceid;
	private TextView tvLocation;
	private DropDownMenu locationDropMenu;
	private DropDownMenu brandDropMenu;
	private DropDownMenuTextView typeDropMenu;
	private int lo_index;
	/***************************************************/
	//位置菜单属性
	String[] locations;
	/**************************************************/
	//品牌菜单属性
	String[] brands;
	String[] apptypes;
	/***************************************************/
	//初始化，在没有选择任何下拉的时候自动选择第一个客厅
	/***************************************************/
	
	private static final int TOKEN_TIME_OUT = 12,
			TOKEN_TIME_OUT_NO_DIALOG = 13;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewUtil.initToolbar(this, getString(R.string.save_info), false, false, 0);
		/***************************************************/
		Intent intent = getIntent();
		deviceid=intent.getStringExtra(Constant.SERIAL_NUMBER_INTENT_KEY);
		tvDeviceId.setText(deviceid);
		etDeviceName.setText(getResources().getString(R.string.device)+deviceid);
		/****初始化下拉选择菜单数据***********************************************/
		getLocations();
		getBrands();
		getDeviceTypes();
	}

	private void getDeviceTypes() {
		// TODO 得到设备的用电类型列表
		String ty = BApplication.instance.getSp().getString(Constant.SP_KEY_TYPES,"no");
		if(!ty.equals("no")){//如果有数据
			String[] br=ty.split(",");
			apptypes = new String[br.length];
			apptypes = br;
			LogUtilNIU.value("赋值到的brands--->"+Arrays.toString(brands));
			LogUtilNIU.value("有数据，已经赋值");
			refresh3.setVisibility(View.GONE);
			initTypeDropMenu();
		}else{
			new Enterface("getApplicanceSorts.act").doRequest(new JsonClientHandler2() {
				@Override
				public void onInterfaceSuccess(String message, String contentJson) {
					LogUtilNIU.e("返回 types 的content--->"+contentJson);
					try {
						JSONArray arry=new JSONArray(contentJson);
						apptypes=new String[arry.length()];
						for(int i = 0; i < arry.length();i++){
							apptypes[i]=arry.getJSONObject(i).getString("name");
						}
						LogUtilNIU.e("types--->"+Arrays.toString(apptypes));
					} catch (JSONException e) {
						ExceptionUtil.handleException(e);
					}
					if(apptypes.length<1){
						apptypes=new String[]{getString(R.string.not_data)};
					}
					refresh3.setVisibility(View.GONE);
					initTypeDropMenu();
				}

				@Override
				public void onInterfaceFail(String json) {
					//接口statusCode不为0时回调该方法,
					//这行代码toast提示接口返回的message,你可以在这里写其它逻辑.
					LogUtilNIU.interfaceWrong("失败--->"+json);
					super.onInterfaceFail(json);
					brands=new String[]{getString(R.string.not_data)};
					initTypeDropMenu();
				}

				@Override
				public void onFailureConnected(Boolean canConnect) {

				}
			});}
	
	}


	private void initTypeDropMenu() {
		typeDropMenu.setmMenuCount(1);
		typeDropMenu.setmShowCount(5);
		typeDropMenu.setShowCheck(true);
		typeDropMenu.setmMenuTitleTextSize(14);
		typeDropMenu.setmMenuTitleTextColor(Color.parseColor("#0e7ef1"));
		typeDropMenu.setmMenuListTextSize(14);
		typeDropMenu.setmMenuListTextColor(Color.BLACK);
		typeDropMenu.setmMenuPressedBackColor(Color.WHITE);
		typeDropMenu.setmMenuPressedTitleTextColor(Color.BLACK);
		typeDropMenu.setmCheckIcon(R.drawable.ico_make);
		typeDropMenu.setmUpArrow(R.drawable.arrow_up);
		typeDropMenu.setmDownArrow(R.drawable.arrow_down);
		String[] brandstrings=new String[]{apptypes[0]};//设为查询到的第一个数据
		typeDropMenu.setDefaultMenuTitle(brandstrings);
		typeDropMenu.setmMenuListBackColor(getResources().getColor(R.color.white));
		typeDropMenu.setmMenuListSelectorRes(R.color.white);
		typeDropMenu.setmMenuBackColor(getResources().getColor(R.color.white));//展开list的背景色
		typeDropMenu.setmMenuListSelectorRes(R.color.white);//展开list的listselector
		typeDropMenu.setmArrowMarginTitle(20);//Menu上箭头图标距title的margin
		typeDropMenu.setmMenuPressedTitleTextColor(Color.BLACK);//Menu按下状态的title文字颜色

		typeDropMenu.setMenuSelectedListener(new OnMenuSelectedListener() {
			@Override
			public void onSelected(View listview, int RowIndex, int ColumnIndex) {

			}
		});
		List<String[]> items = new ArrayList<>();
		items.add(apptypes);
		typeDropMenu.setmMenuItems(items);
		typeDropMenu.setIsDebug(false);
	}


	private void initBrandDropMenu() {
		brandDropMenu.setmMenuCount(1);
		brandDropMenu.setmShowCount(5);
		brandDropMenu.setShowCheck(true);
		brandDropMenu.setmMenuTitleTextSize(14);
		brandDropMenu.setmMenuTitleTextColor(Color.parseColor("#0e7ef1"));
		brandDropMenu.setmMenuListTextSize(14);
		brandDropMenu.setmMenuListTextColor(Color.BLACK);
		brandDropMenu.setmMenuPressedBackColor(Color.WHITE);
		brandDropMenu.setmMenuPressedTitleTextColor(Color.BLACK);
		brandDropMenu.setmCheckIcon(R.drawable.ico_make);
		brandDropMenu.setmUpArrow(R.drawable.arrow_up);
		brandDropMenu.setmDownArrow(R.drawable.arrow_down);
		String[] brandstrings=new String[]{brands[0]};//设为查询到的第一个数据
		brandDropMenu.setDefaultMenuTitle(brandstrings);
		brandDropMenu.setmMenuListBackColor(getResources().getColor(R.color.white));
		brandDropMenu.setmMenuListSelectorRes(R.color.white);
		brandDropMenu.setmMenuBackColor(getResources().getColor(R.color.white));//展开list的背景色
		brandDropMenu.setmMenuListSelectorRes(R.color.white);//展开list的listselector
		brandDropMenu.setmArrowMarginTitle(20);//Menu上箭头图标距title的margin
		brandDropMenu.setmMenuPressedTitleTextColor(Color.BLACK);//Menu按下状态的title文字颜色

		brandDropMenu.setMenuSelectedListener(new OnMenuSelectedListener() {
			@Override
			public void onSelected(View listview, int RowIndex, int ColumnIndex) {

			}
		});
		List<String[]> items = new ArrayList<>();
		items.add(brands);
		brandDropMenu.setmMenuItems(items);
		brandDropMenu.setIsDebug(false);
	}


	private void initLocationDropMenu() {
		locationDropMenu.setmMenuCount(1);
		locationDropMenu.setmShowCount(6);
		locationDropMenu.setShowCheck(true);
		locationDropMenu.setmMenuTitleTextSize(14);
		locationDropMenu.setmMenuTitleTextColor(Color.parseColor("#0e7ef1"));
		locationDropMenu.setmMenuListTextSize(14);
		locationDropMenu.setmMenuListTextColor(getResources().getColor(R.color.middleGray));
		locationDropMenu.setmMenuPressedBackColor(Color.WHITE);
		locationDropMenu.setmMenuPressedTitleTextColor(Color.BLACK);
		locationDropMenu.setmCheckIcon(R.drawable.ico_make);
		locationDropMenu.setmUpArrow(R.drawable.arrow_up);
		locationDropMenu.setmDownArrow(R.drawable.arrow_down);
		String[] lostrings=new String[]{locations[0]};//设为查询到的第一个数据
		locationDropMenu.setDefaultMenuTitle(lostrings);
		locationDropMenu.setmMenuListBackColor(getResources().getColor(R.color.white));
		locationDropMenu.setmMenuListSelectorRes(R.color.white);
		locationDropMenu.setmMenuBackColor(getResources().getColor(R.color.white));//展开list的背景色
		locationDropMenu.setmMenuListSelectorRes(R.color.white);//展开list的listselector
		locationDropMenu.setmArrowMarginTitle(20);//Menu上箭头图标距title的margin
		locationDropMenu.setmMenuPressedTitleTextColor(Color.BLACK);//Menu按下状态的title文字颜色

		locationDropMenu.setMenuSelectedListener(new OnMenuSelectedListener() {
			@Override
			public void onSelected(View listview, int RowIndex, int ColumnIndex) {

			}
		});
		List<String[]> items = new ArrayList<>();
		items.add(locations);
		locationDropMenu.setmMenuItems(items);
		locationDropMenu.setIsDebug(false);
	}

	@Override
	protected Integer getLayoutId() {
		return R.layout.activity_save_device_setting;
	}

	@Override
	public void onClick(View v) {
		if(v==tvSave){
			/***************************************************/
			//保存设备信息
			String deviceName = etDeviceName.getText().toString().trim();
			saveDeviceInfo(deviceid,deviceName,locationDropMenu.getEditTextContent(), brandDropMenu.getEditTextContent(),typeDropMenu.getEditTextContent());

		}else if(v==tvSmarkLink){
			/***************************************************/
			//点击智能连接按钮
			Intent intent = new Intent(this,SmartLinkActivity.class);
			SingleDevice device = new SingleDevice();
			device.setDeviceID(deviceid);
			((BApplication)getApplication()).setCurrentDevice(device);
			startActivity(intent);
			finish();
		}else if(v==tvNoLink){
			/***************************************************/
			//点击暂不连接按钮，回到主页
			Intent intent = new Intent(this,MainActivity2.class);
			startActivity(intent);
		}
	}

	/**********************************************************/
	/**  
	 * 服务器
	 * 保存设备信息
	 */
	private void saveDeviceInfo(final String id, final String name, final String position, final String brand,final String apptype){
		new Enterface("setDeviceInfo.act").addParam("device.id",id)
		.addParam("device.name",name)
		.addParam("device.position",position)
		.addParam("device.brand",brand)
		.addParam("device.appliancesort",apptype)
		.doRequest(new JsonClientHandler2() {
			@Override
			public void onInterfaceSuccess(String message, String contentJson) {
				// TODO 服务器存信息成功
				LogUtilNIU.e("setDeviceInfo.act 返回 的content--->"+contentJson);
				LogUtilNIU.e("设置的设备位置为"+position+"设置的设备品牌为"+brand);
				//				ToastUtils.shortToast(SaveDeviceSettingActivity.this, getResources().getString(R.string.save_device_data_successfully));
				/***************************************************/
				//设备信息保存成功，出现智能连接按钮
				BApplication.instance.setDeviceDataHasChanged(true);
				llRemindSmarkLinkSpace.setVisibility(View.GONE);
				llRemindSmarkLink.setVisibility(View.VISIBLE);
			}

			@Override
			public void onInterfaceFail(String json) {
				//接口statusCode不为0时回调该方法,
				//这行代码toast提示接口返回的message,你可以在这里写其它逻辑.
				/*
				 * 设备信息保存失败，提示用户
				 */
				super.onInterfaceFail(json);
			}

			@Override
			public void onFailureConnected(Boolean canConnect) {
				// TODO Auto-generated method stub

			}
		},true);
	}

	/**********************************************************/
	/**
	 * 获取位置列表  string数组形式
	 */
	public void getLocations(){

		String location = BApplication.instance.getSp().getString(Constant.SP_KEY_LOCATIONS,"no");
		if(!location.equals("no")){//如果有数据
			String[] lo=location.split(",");
			locations  = new String[lo.length];
			locations = lo;
			LogUtilNIU.value("有数据，已经赋值");
			LogUtilNIU.value("赋值到的Location--->"+Arrays.toString(locations));
			refresh1.setVisibility(View.GONE);
			initLocationDropMenu();
		}else{

			new Enterface("getPositions.act").doRequest(new JsonClientHandler2() {
				@Override
				public void onInterfaceSuccess(String message, String contentJson) {
					LogUtilNIU.e("返回 locations的content--->"+contentJson);
					try {
						JSONArray arry=new JSONArray(contentJson);
						locations=new String[arry.length()];
						for(int i = 0; i < arry.length();i++){
							locations[i]=arry.getJSONObject(i).getString("name");
						}
						LogUtilNIU.e("brands--->"+Arrays.toString(locations));
					} catch (JSONException e) {
						ExceptionUtil.handleException(e);
					}
					if(locations.length<0){
						locations=new String[]{"客厅","厨房","卧室","洗手间","其他"};
					}
					refresh1.setVisibility(View.GONE);
					initLocationDropMenu();
				}

				@Override
				public void onInterfaceFail(String json) {
					//接口statusCode不为0时回调该方法,
					//这行代码toast提示接口返回的message,你可以在这里写其它逻辑.
					locations=new String[]{"客厅","厨房","卧室","洗手间","其他"};
					initLocationDropMenu();
					super.onInterfaceFail(json);
				}

				@Override
				public void onFailureConnected(Boolean canConnect) {
					// TODO Auto-generated method stub

				}
			});
		}

	}


	/**********************************************************/
	/**
	 * 获取品牌列表 string数组形式
	 */
	public void getBrands(){
		String b = BApplication.instance.getSp().getString(Constant.SP_KEY_LOCATIONS,"no");
		if(!b.equals("no")){//如果有数据
			String[] br=b.split(",");
			brands = new String[br.length];
			brands = br;
			LogUtilNIU.value("赋值到的brands--->"+Arrays.toString(brands));
			LogUtilNIU.value("有数据，已经赋值");
			refresh2.setVisibility(View.GONE);
			initBrandDropMenu();
		}else{
			new Enterface("getBrands.act").doRequest(new JsonClientHandler2() {
				@Override
				public void onInterfaceSuccess(String message, String contentJson) {
					LogUtilNIU.e("返回 brands 的content--->"+contentJson);
					//   [{"id":1,"name":"345"},{"id":2,"name":"4356"}]
					try {
						JSONArray arry=new JSONArray(contentJson);
						brands=new String[arry.length()];
						for(int i = 0; i < arry.length();i++){
							brands[i]=arry.getJSONObject(i).getString("name");
						}
						LogUtilNIU.e("brands--->"+Arrays.toString(brands));
					} catch (JSONException e) {
						ExceptionUtil.handleException(e);
					}
					if(brands.length<1){
						brands=new String[]{"格力","美的","海尔","松下","西门子","索尼","日立","其他"};
					}
					refresh2.setVisibility(View.GONE);
					initBrandDropMenu();
				}

				@Override
				public void onInterfaceFail(String json) {
					//接口statusCode不为0时回调该方法,
					//这行代码toast提示接口返回的message,你可以在这里写其它逻辑.
					LogUtilNIU.interfaceWrong("失败--->"+json);
					super.onInterfaceFail(json);
					brands=new String[]{"格力","美的","海尔","松下","西门子","索尼","日立","其他"};
					initBrandDropMenu();
				}

				@Override
				public void onFailureConnected(Boolean canConnect) {
					// TODO Auto-generated method stub

				}
			});}
	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("保存设备信息"); //统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
		MobclickAgent.onResume(this);          //统计时长
	}
	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("保存设备信息"); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息。"SplashScreen"为页面名称，可自定义
		MobclickAgent.onPause(this);
	}
}
