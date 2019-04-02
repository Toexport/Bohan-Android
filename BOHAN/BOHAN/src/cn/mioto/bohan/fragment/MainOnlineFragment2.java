package cn.mioto.bohan.fragment;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mioto.bohan.webservice.WebServiceClient;

import cn.mioto.bohan.BApplication;
import cn.mioto.bohan.Constant;
import cn.mioto.bohan.R;
import cn.mioto.bohan.activity.BindDeviceActivity;
import cn.mioto.bohan.activity.MainActivity;
import cn.mioto.bohan.activity.OnlineStatusActivity;
import cn.mioto.bohan.activity.ScanActivity;
import cn.mioto.bohan.adapter.LocationListAdapter;
import cn.mioto.bohan.adapter.TypeListAdapter;
import cn.mioto.bohan.exception.ExceptionManager;
import cn.mioto.bohan.utils.Enterface;
import cn.mioto.bohan.utils.HttpUtils;
import cn.mioto.bohan.utils.LogUtilNIU;
import cn.mioto.bohan.utils.NetworkUtils;
import cn.mioto.bohan.utils.ViewUtil;
import cn.mioto.bohan.view.loadingview.LoadingView;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import steed.framework.android.client.JsonClientHandler2;

/**
 * 类说明：设备类型列表Fragment 下面两个子页面用的是view 子view里面分别有一个Listview
 * 
 */
public class MainOnlineFragment2 extends BaseUDPNoCurrentFragment implements
		View.OnClickListener {
	/********** DECLARES *************/
	private LinearLayout tag_ll;
	private TextView tvLocation;
	private TextView tvType;
	private ImageView cursorIv;
	private android.support.v4.view.ViewPager innerViewPager;
	private LinearLayout view1;
	private LinearLayout view2;
	private ListView listViewType;
	private ListView listViewLocation;
	private LoadingView loading1;
	private LoadingView loading2;
	LocationListAdapter adapterLocation;
	TypeListAdapter adapterType;
	ImageView ivCancle;
	List<LinearLayout> views;
	int offset; // 下标的偏移量
	int currIndex = 0;// 当前页卡编号
	int ivWide;// 下标宽度
	int linerLayoutW;
	Handler handler = new Handler();
	View view;
	// 判断用户名下有没有设备
	private Boolean isFirstTimeShow;
	private Boolean hasDevice;
	TextView tvScanCode;
	View popView;
	PopupWindow mPopupWindow;
	SharedPreferences sp;
	private int currentPage = 0;
	/***************************************************/
	private BroadcastReceiver receiver;
	private LayoutInflater inflater;
	/***************************************************/
	private List<String> appTypesList = new ArrayList<>();
	private List<String> locationsList = new ArrayList<>();
	
	private static final int TYPE_SUCCESS = 10,LOCATION_SUCCESS = 11;
	
	private TextView toolbarTitle;
	private TextView toolbarMenu;
	private TextView menu_front;
	private ImageView ivScan;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// 初始化最基本的界面，包括上面的TAB和ViewPager
		this.inflater = inflater;
		view = inflater.inflate(R.layout.fragment_main_list2, null);
		tag_ll = (LinearLayout) view.findViewById(R.id.tag_ll);
		tvType = (TextView) view.findViewById(R.id.tvType);
		tvLocation = (TextView) view.findViewById(R.id.tvLocation);
		cursorIv = (ImageView) view.findViewById(R.id.cursor_iv);
		innerViewPager = (android.support.v4.view.ViewPager) view
				.findViewById(R.id.inner_viewPager);
		toolbarTitle = (TextView) view.findViewById(R.id.toolbar_title);
		toolbarTitle.setText(getString(R.string.device_list));
		ivScan = (ImageView) view.findViewById(R.id.ivScan);
		ivScan.setOnClickListener(this);
		initImageView();
		/***************************************************/
		getContext().registerReceiver(receiver, filter);
		viewPagerAddContent();// ViewPager加入界面
		// 让主线程代码延迟s
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				innerViewPager
						.setOnPageChangeListener(new MyOnPageChangeListener());
			}
		}, 100);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {

		super.onViewCreated(view, savedInstanceState);
		// 两个Listview，两个adapter
		adapterLocation = new LocationListAdapter(getActivity(), locationsList);
		listViewLocation.setAdapter(adapterLocation);
		adapterType = new TypeListAdapter(getActivity(), appTypesList);
		listViewType.setAdapter(adapterType);

		listViewLocation.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(getContext(),
						OnlineStatusActivity.class);
				String thisType = locationsList.get(position);
				Bundle b = new Bundle();
				b.putString(Constant.INTENT_KEY_THIS_SORT, thisType);
				b.putString("PosName", thisType);
				b.putString("LoadName", "");
				intent.putExtras(b);
				startActivity(intent);
			}
		});
		//
		listViewType.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(getContext(),
						OnlineStatusActivity.class);
				String thisType = appTypesList.get(position);
				Bundle b = new Bundle();
				b.putString(Constant.INTENT_KEY_THIS_SORT, thisType);
				b.putString("PosName", "");
				b.putString("LoadName", thisType);
				intent.putExtras(b);
				startActivity(intent);
			}
		});

	}

	@Override
	public void onResume() {
		super.onResume();
		// getTypesService();//从服务器获取类别列表信息 需要等待回调
		// getLocationsService();//从服务器获取位置的列表信息 需要等待回调
		getTypes();
		getLocations();
	}

	/**
	 * 
	 * @return
	 * @Description:获取所有设备已设置的位置 Parameters: return:void
	 * 
	 *                           服务器返回：用户曾经为ID选择了的分类，以及盖分类下的所有ID
	 * 
	 */
	private void getLocationsService() {// 获取所有设备已设置的位置
		new Enterface("getAllPositionDevices.act")
				.doRequest(new JsonClientHandler2() {
					@Override
					public void onInterfaceSuccess(String message,
							String contentJson) {
						LogUtilNIU.value("得到的位置分类信息的Json为---->" + contentJson);
						// ["厨房","书房","洗手间","主人房","衣帽间"]
						locationsList.clear();
						try {
							JSONArray arry = new JSONArray(contentJson);
							for (int i = 0; i < arry.length(); i++) {
								locationsList.add(arry.getString(i));
								// LogUtilNIU.value("位置"+i+"是"+arry.getString(i));
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
						adapterLocation.notifyDataSetChanged();
						loading2.setVisibility(View.GONE);// 加载效果消失
					}

					@Override
					public void onInterfaceFail(String json) {

					}

					@Override
					public void onFailureConnected(Boolean canConnect) {

					}
				});
	}

	private void getLocations() {
		if (NetworkUtils.isNetworkConnected(getActivity())) {
			new Thread() {
				public void run() {
					Message msg = new Message();
					msg.what = Constant.MSG_FAILURE;
					msg.obj = getString(R.string.cannot_connection_server);
					try {
						HttpUtils.TOKIN = "";
						String result = WebServiceClient.CallWebService("GetPosNameList",null);
						Log.d("resultLogin", result);
						if (result != null) {
							locationsList.clear();
							JSONObject obj = new JSONObject(result);
							int statusCode = obj.getInt("statusCode");
							if (statusCode == 0) {
								String[] arry = new String[] {};
								String rest = obj.getString("content");
								if (!TextUtils.isEmpty(rest)) {
									arry = rest.split(",");
								}
								for (int i = 0; i < arry.length; i++) {
									locationsList.add(arry[i]);
									// LogUtilNIU.value("位置"+i+"是"+arry.getString(i));
								}
								msg.what = LOCATION_SUCCESS;
							} 
						}
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
		} else {
			defalutHandler.sendEmptyMessage(Constant.MSG_NETWORK_ERROR);
		}
	}

	/**
	 * 
	 * @return
	 * @Description:获取所有设备已设置的用电类型 Parameters: return:void
	 * 
	 *                             服务器返回：用户曾经为ID选择了的分类，以及盖分类下的所有ID
	 * 
	 */
	private void getTypesService() {// 获取所有设备已设置的用电类型
		new Enterface("getAllApplianceSortDevices.act")
				.doRequest(new JsonClientHandler2() {
					@Override
					public void onInterfaceSuccess(String message,
							String contentJson) {
						LogUtilNIU
								.value("得到的用电类别分类信息的Json为---->" + contentJson);
						appTypesList.clear();
						try {
							JSONArray arry = new JSONArray(contentJson);
							for (int i = 0; i < arry.length(); i++) {
								appTypesList.add(arry.getString(i));
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
						// 正在加载效果消失
						adapterType.notifyDataSetChanged();
						loading1.setVisibility(View.GONE);
					}

					@Override
					public void onInterfaceFail(String json) {

					}

					@Override
					public void onFailureConnected(Boolean canConnect) {

					}
				});
	}

	private void getTypes() {
		if (NetworkUtils.isNetworkConnected(getActivity())) {
			new Thread() {
				public void run() {
					Message msg = new Message();
					msg.what = Constant.MSG_FAILURE;
					msg.obj = getString(R.string.cannot_connection_server);
					try {
						HttpUtils.TOKIN = "";
						String result = WebServiceClient
								.CallWebService("GetLoadNameList",null); // "{\"code\":2,\"result\":{\"logincode\":1, \"result\":{\"id\":\"10001\"}}}";
						Log.d("resultLogin", result);
						if (result != null) {
							appTypesList.clear();
							JSONObject obj = new JSONObject(result);
							int statusCode = obj.getInt("statusCode");
							if (statusCode == 0) {
								String[] arry = new String[] {};
								String res = obj.getString("content");
								if (!TextUtils.isEmpty(res)) {
									arry = res.split(",");
								}
								for (int i = 0; i < arry.length; i++) {
									appTypesList.add(arry[i]);
									// LogUtilNIU.value("位置"+i+"是"+arry.getString(i));
								}
								msg.what = TYPE_SUCCESS;
							}
						}
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
		} else {
			defalutHandler.sendEmptyMessage(Constant.MSG_NETWORK_ERROR);
		}
	}

	private void initImageView() {
		ViewTreeObserver vto = cursorIv.getViewTreeObserver();
		// 这个回调接口使view的计算推迟到view被加载完毕

		vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				cursorIv.getViewTreeObserver().removeGlobalOnLayoutListener(
						this);
				ivWide = cursorIv.getWidth();
				linerLayoutW = tag_ll.getWidth();
				offset = (linerLayoutW / 2 - ivWide) / 2;// 获取图片偏移量//这里有几份，linerLayoutW就除以几
				/*
				 * cursor的初始位置设定： 思路 屏幕总宽减去LinerLayout宽度再除以2在加上offset就得到初始位置
				 */
				DisplayMetrics dm = new DisplayMetrics();
				getActivity().getWindowManager().getDefaultDisplay()
						.getMetrics(dm);
				int screenW = dm.widthPixels; // 获取手机屏幕宽度分辨率
				int initX = (screenW - linerLayoutW) / 2 + offset;
				// 计算出初始位置后，设置控件的位置
				LinearLayout.MarginLayoutParams margin = new LinearLayout.MarginLayoutParams(
						cursorIv.getLayoutParams());
				margin.setMargins(initX, margin.topMargin, margin.rightMargin,
						margin.bottomMargin);
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
						margin);
				cursorIv.setLayoutParams(params);
			}
		});

	}

	private void viewPagerAddContent() {
		views = new ArrayList<LinearLayout>();
		view1 = (LinearLayout) inflater.inflate(R.layout.view_list_type, null);
		view2 = (LinearLayout) inflater.inflate(R.layout.view_list_location,
				null);
		views.add(view1);
		views.add(view2);
		listViewType = (ListView) view1.findViewById(R.id.listViewType);
		listViewLocation = (ListView) view2.findViewById(R.id.listViewLocation);
		loading1 = (LoadingView) view1.findViewById(R.id.loading);
		loading2 = (LoadingView) view2.findViewById(R.id.loading);
		innerViewPager.setAdapter(new DataAdapter());
		tvLocation.setOnClickListener(this);
		tvType.setOnClickListener(this);
	}

	private class DataAdapter extends PagerAdapter {
		@Override
		public int getCount() {
			return views.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			container.addView(views.get(position));
			return views.get(position);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(views.get(position));// 删除页卡
		}
	}

	/**
	 * 
	 * @Description:设置tab颜色
	 */
	private void textViewChangeColor(int i) {
		switch (i) {
		case 0:// 类别
			tvType.setTextColor(getResources().getColor(R.color.darkBlue));
			tvLocation
					.setTextColor(getResources().getColor(R.color.middleGray));
			break;
		case 1:// 位置
			tvType.setTextColor(getResources().getColor(R.color.middleGray));
			tvLocation.setTextColor(getResources().getColor(R.color.darkBlue));
			break;
		}
	}

	@Override
	public void onClick(View v) {
		if (v == tvType) {
			innerViewPager.setCurrentItem(0);
		}
		if (v == tvLocation) {
			innerViewPager.setCurrentItem(1);
		}
		if (v == tvScanCode) {
			Intent intent = new Intent(getContext(), ScanActivity.class);
			startActivity(intent);
		} else if (v == ivCancle) {
			mPopupWindow.dismiss();
		}
		if(v == ivScan){
			Intent intent = new Intent(getActivity(),
					BindDeviceActivity.class);
			startActivity(intent);
		}
	}

	public class MyOnPageChangeListener implements
			ViewPager.OnPageChangeListener {
		/*
		 * 计算出标签的偏移量 标签偏移量大小为offset * 2 + ivWide
		 */
		private int one = offset * 2 + ivWide;

		@Override
		public void onPageScrollStateChanged(int arg0) {

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		// 即将要被显示的页卡的index
		@Override
		public void onPageSelected(int arg0) {
			currentPage = arg0;
			// 初始化移动的动画（从当前位置，x平移到即将要到的位置）
			// 这个动画设定了平移效果，从哪个位置移动到哪个位置，记录了上次的位置，计算出下次的位置
			Animation animation = new TranslateAnimation(currIndex * one, arg0
					* one, 0, 0);
			currIndex = arg0;
			animation.setFillAfter(true); // 动画终止时停留在最后一帧，不然会回到没有执行前的状态
			animation.setDuration(200); // 动画持续时间，0.2秒
			cursorIv.startAnimation(animation); // 是用imageview来显示动画
			int i = currIndex + 1;
			// 改变textView的颜色
			textViewChangeColor(arg0);
		}
	}

	@Override
	public void onPause() {
		LogUtilNIU.circle("onlineList Fragment onPause");
		super.onPause();
	}

	private Handler defalutHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Constant.MSG_SUCCESS:
				break;
			case Constant.MSG_FAILURE:
				toast((String) msg.obj);
				break;
			case Constant.MSG_EXCPTION:
				toast(ExceptionManager.getErrorDesc(getActivity(),
						(Exception) msg.obj));
				break;
			case Constant.MSG_TIME_OUT:
				toast(getString(R.string.connection_timeout));
				break;
			case Constant.MSG_NETWORK_ERROR:
				toast(getString(R.string.no_connection));
				break;
			case TYPE_SUCCESS:
				// 正在加载效果消失
				adapterType.notifyDataSetChanged();
				loading1.setVisibility(View.GONE);
				break;
			case LOCATION_SUCCESS:
				adapterLocation.notifyDataSetChanged();
				loading2.setVisibility(View.GONE);// 加载效果消失
				break;
			}
		}

		;
	};

	private void toast(String text) {
		Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
	}

	private Runnable defalutTimeout = new Runnable() {
		@Override
		public void run() {
			defalutHandler.obtainMessage(Constant.MSG_TIME_OUT).sendToTarget();
		}
	};
}
