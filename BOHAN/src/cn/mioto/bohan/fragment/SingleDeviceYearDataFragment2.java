package cn.mioto.bohan.fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import cn.mioto.bohan.BApplication;
import cn.mioto.bohan.Constant;
import cn.mioto.bohan.R;
import cn.mioto.bohan.activity.MenuSingleDeviceSumActivity;
import cn.mioto.bohan.entity.SingleDeviceMonth;
import cn.mioto.bohan.exception.ExceptionManager;
import cn.mioto.bohan.utils.HttpUtils;
import cn.mioto.bohan.utils.LoadDataThreadSendManayUtil;
import cn.mioto.bohan.utils.LoadDataThreadUtil;
import cn.mioto.bohan.utils.LogUtilNIU;
import cn.mioto.bohan.utils.ModbusCalUtil;
import cn.mioto.bohan.utils.NetworkUtils;
import cn.mioto.bohan.utils.ToastUtils;
import cn.mioto.bohan.view.AreaChart01ViewMonth;
import cn.mioto.bohan.view.CustomBarChartViewMonth;
import cn.mioto.bohan.view.timepicker.DatePicker;
import cn.mioto.bohan.view.timepicker.DatePicker.OnYearPickListener;

import com.mioto.bohan.webservice.WebServiceClient;
import com.umeng.analytics.MobclickAgent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 类说明：单个设备年数据 不嵌套Fragment 只有一页数据 显示上12个月用电总量和平均功率
 */
public class SingleDeviceYearDataFragment2 extends BaseSingleDeviceDataFragment
		implements OnClickListener {
	private View view;
	private ViewPager viewPager;
	private List<Fragment> fragments = new ArrayList<>();
	/********** DECLARES *************/
	private LinearLayout llSelectTime;
	private TextView tvYear;
	private LinearLayout llLast;
	private TextView tvYear2;
	private TextView tvL1;
	private LinearLayout llLable;
	private TextView tvL2;
	private LayoutInflater inflater;
	private List<LinearLayout> views = new ArrayList<>();
	private LinearLayout view1;
	private LinearLayout view2;
	// 显示时间的地方
	private LinearLayout lllowcolor;
	// 显示颜色的地方
	private LinearLayout lluptime;
	private CustomBarChartViewMonth bar11;
	private AreaChart01ViewMonth bar12;
	private CustomBarChartViewMonth bar21;
	private AreaChart01ViewMonth bar22;
	private Calendar calendar = Calendar.getInstance();
	private List<String> xLables = new ArrayList<>();
	// 现在历史资料后，显示的历史事件区域
	private LinearLayout llShowHistory;
	// 历史事件显示
	private TextView tvShowSelectTime;
	// 折线图的横坐标
	private List<String> xLablesLineChart = new ArrayList<>();
	private List<String> xLablesLineHistory = new ArrayList<>();
	/***************************************************/
	private UDPBrocastReceiver receiver;
	/********** DECLARES *************/
	private TextView title11;
	private TextView title12;
	/********** DECLARES *************/
	private TextView title21;
	private TextView title22;
	private String year;
	private String month;

	private List<SingleDeviceMonth> list = new ArrayList<>();
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
	private long currentTime;// 当前系统时间

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		view = inflater
				.inflate(R.layout.fragment_single_device_year_data, null);
		viewPager = (ViewPager) view.findViewById(R.id.viewPager);
		/***************************************************/
		viewPagerAddContent();
		bindViews();
		// initDay();
		/********* 初始化广播 ******************************************/
		receiver = new UDPBrocastReceiver();
		getContext().registerReceiver(receiver, filter);
		/***************************************************/
		checkDataAndUpdateCharts();
		llSelectTime.setOnClickListener(this);
		return view;
	}

	/**
	 * @Title: checkDataAndUpDateView
	 * @Description:创建一个查询方法，适当的时候调用去查询数据 查询数据以后更新图表
	 * @return void
	 * @throws
	 */
	public void checkDataAndUpdateCharts() {
		addTitle();
		// 发UDP指令查询单个设备的日数据信息
		// 发送查询单个设备用电数据的指令

		/********* 查询上12个月的用电量 ******************************************/
		llShowHistory.setVisibility(View.GONE);
//		lllowcolor.setVisibility(View.VISIBLE);
		lluptime.setVisibility(View.VISIBLE);
		// Intent intent = new Intent();
		// intent.setAction(Constant.BROCAST_CHART_SHOW_DIALOG);
		// getContext().sendBroadcast(intent);
		// checkNowData();//查询数据
		h.sendEmptyMessage(Constant.MSG_WHAT_SHOW_DIALOG_OF_RESEND_TASK);
		udpKind = 0;
		currentTime = System.currentTimeMillis();
		getData(dateFormat.format(currentTime));
	}

	private void checkNowData() {
		udpKind = 0;
		String verCode = ModbusCalUtil.verNumber(deviceId + "000C0000");
		final String msg1 = "3A" + deviceId + "000C0000" + verCode + "0D";// 0010
																			// 查询上24小时的用电量
		new LoadDataThreadUtil(msg1, h, deviceBSSID, getContext()).start();
	}

	// 查询功率
	protected void checkRate() {
		udpKind = 1;
		String verCode2 = ModbusCalUtil.verNumber(deviceId + "000D0000");
		final String msg2 = "3A" + deviceId + "000D0000" + verCode2 + "0D";
		new LoadDataThreadUtil(msg2, h, deviceBSSID, getContext()).start();
	}

	public class UDPBrocastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 区分接收到的是哪种广播
			String action = intent.getAction();
			if (action.equals(Constant.SOCKET_BROCAST_ONRECEIVED)) {
				String message = intent
						.getStringExtra(Constant.SOCKET_BROCAST_EXTRA_MESSAGE);
				if (isReqCodeEqual(message, "000C")
						|| isReqCodeEqual(message, "000D")) {
					showContent(message);
				}
			}
		}
	}

	private void initDay(String year, String month) {
		tvYear.setText(year);
		tvL1.setText(year + "" + getResources().getString(R.string.year1));
		/*
		 * 考虑两种情况 1月 okay 非1月 okay
		 */
		if (Integer.valueOf(month) != 0) {// 只要不为1月份就需要显示2个时间
			tvYear2.setText(Integer.valueOf(year) - 1 + "");
			tvL2.setText(Integer.valueOf(year) - 1 + ""
					+ getResources().getString(R.string.year1));
			/***************************************************/
			// 初始化图表的下标
		} else {// 为一月的时候，显示上年全年，所以需要显示年份-1
			llLast.setVisibility(View.GONE);// 年份标题隐藏
			llLable.setVisibility(View.GONE);// 小颜色标签隐藏
			tvYear.setText(Integer.valueOf(year) - 1 + "");
			tvL1.setText(Integer.valueOf(year) - 1 + ""
					+ getResources().getString(R.string.year1));
			// 由于标签默认显示1-12所以不用另外设置了。
		}
	}

	/**
	 * @Title: showContent
	 * @Description: 处理返回回来的指令
	 * @return void
	 * @param 回调数据
	 * @throws
	 */
	@Override
	public void showContent(String contentJson) {
		contentJson = contentJson.toUpperCase();
		if (checkUDPMessage(contentJson)) {
			if (isReqCodeEqual(contentJson, "000C")) {// 单个设备日用电量
				BApplication.instance.setResendTaskShowBreak(true);
				progressGettingDataDismissNoToast();
				// 2 秒后才查询功率
				LogUtilNIU.value("000C总位数" + contentJson.length());
				String yymm = contentJson.substring(120, 124);// 年月日时 1609
				LogUtilNIU.value("年数据的---年月-------"
						+ contentJson.substring(120, 124));
				// 年月日 160923
				year = "20" + yymm.substring(0, 2);
				month = yymm.substring(2, 4);

				// 得到用电量数据后初始化上面标题显示的日期和x左边显示的月份
				initDay(year, month);
				initX();
				// 然后把数据显示在柱状图上（上部分）
				dealPowerDataForEachID(contentJson.substring(24, 120));
				progressGettingDataShow(getString(R.string.recent_electricity_power_query));
				h.postDelayed(new Runnable() {
					@Override
					public void run() {
						// 查询功率
						checkRate();
					}
				}, Constant.RESENDTIME);
			} else if (isReqCodeEqual(contentJson, "000D")) {// 单个设备日平均功率
				BApplication.instance.setResendTaskShowBreak(true);
				progressGettingDataDismissNoToast();
				dealPowerRateForEachID(contentJson.substring(24, 96));// 解析每个设备的用电功率
			}
		}
	}

	/***************************************************/
	/*
	 * 
	 * 设备的用电量的集合，是最近30天 处理后最后得到用电量 list<Double>集合
	 */
	private List<String> powerList = new ArrayList<>();
	private List<Double> powerDatas = new ArrayList<>();

	private void dealPowerDataForEachID(String content) {
		powerList.clear();
		powerDatas.clear();
		LogUtilNIU.value("年用电数据截取为" + content);
		// 截取为12分的list集合
		for (int i = 0; i < content.length(); i += 8) {
			powerList.add(content.substring(i, i + 8));// 天才！！！！！有木有！！！
		}
		for (int i = 0; i < powerList.size(); i++) {
			String data = powerList.get(i);
			data = ModbusCalUtil.addDotDel0(data, 6);
			Double d = Double.valueOf(data);
			powerDatas.add(d);
		}
		LogUtilNIU.value("powerList" + powerList + "powerDatas" + powerDatas);
		initBarDatas(powerDatas);
	}

	private void initBarDatas(List<Double> powerDatas) {
		// 数据改变柱子颜色和设置数据
		LogUtilNIU.value("！！！！！！！！！！！！！！！！！！！！！" + powerDatas);
		LogUtilNIU.value("！！！！！！！！！！！！！！！！！！！！！" + xLables);
		Double max = ModbusCalUtil.countBiggestShowItsUpper(powerDatas);
		bar11.setBarColorAndData(xLables.subList(0, 6),
				powerDatas.subList(0, 6), max);
		bar21.setBarColorAndData(xLables.subList(6, 12),
				powerDatas.subList(6, 12), max);
	}

	/***************************************************/
	/*
	 * 设备的用电功率的集合，是最近24小时的
	 */
	private List<String> listsRateList = new ArrayList<>();
	private List<Double> powerRates = new ArrayList<>();

	private void dealPowerRateForEachID(String content) {
		listsRateList.clear();
		powerRates.clear();
		// 截取为12分的list集合
		for (int i = 0; i < content.length(); i += 6) {
			listsRateList.add(content.substring(i, i + 6));// 天才！！！！！有木有！！！
		}
		// 把次数据做小数处理
		for (int i = 0; i < listsRateList.size(); i++) {
			String data = listsRateList.get(i);
			data = ModbusCalUtil.addDotDel0(data, 2);
			Double d = Double.valueOf(data);
			powerRates.add(d);
		}
		LogUtilNIU.value("listsRateList" + listsRateList + "powerRates"
				+ powerRates);
		initArea(powerRates);// 初始化区域图
	}

	private void initArea(List<Double> datas) {
		// 全部数据都乘以1000
		// 把上面存入的连贯数据作处理，变成前后数据都是0的折线图形式
		List<Double> lineDatas = new ArrayList<>();
		for (int i = 0; i < 17; i++) {
			if (i == 0 || i % 8 == 0) {
				lineDatas.add(0d);// 0位或被8整除的位，都加“”
			} else if (i == 7 || (i - 7) % 8 == 0) {
				lineDatas.add(0d);
			} else if (i < 7) {
				lineDatas.add(datas.get(i - 1) * 1000);
			} else if (i < 15) {
				lineDatas.add(datas.get(i - 3) * 1000);
			}
		}
		Double max = ModbusCalUtil.countBiggestShowItsUpper(lineDatas);
		bar12.setBarColorAndData(lineDatas.subList(0, 6), max);
		bar22.setBarColorAndData(lineDatas.subList(6, 12), max);
	}

	private void initX() {
		// 初始化集合
		// int m0=Integer.valueOf(month);//当前月份
		xLables.clear();
		xLablesLineChart.clear();
		int m0 = 1;
		for (int i = 0; i < 12; i++) {// 用一个循环，把所有月份初始到xLables这个集合里
			if ((m0 - i) == 0) {// 当月份等于0
				m0 = 12 + i;
			}
			xLables.add(m0 - i + "");// 每一个月加到集合里
		}
		LogUtilNIU.value("生成年数据的横坐标是-->" + xLables.toString());

		// 初始化折线图横坐标标签
		for (int i = 0; i < 17; i++) {// 用一个循环，把所有月份初始到xLables这个集合里
			if (i == 0 || i % 8 == 0) {
				xLablesLineChart.add("");// 0位或被8整除的位，都加“”
			} else if (i == 7 || (i - 7) % 8 == 0) {
				xLablesLineChart.add("");
			} else if (i < 7) {
				xLablesLineChart.add(xLables.get(i - 1));
			} else if (i < 15) {
				xLablesLineChart.add(xLables.get(i - 3));
			}
		}
		List intList = new ArrayList<>();
		// 对xLables排序
		for (String str : xLables) {
			int i = Integer.parseInt(str);
			intList.add(i);
		}
		Collections.sort(intList);
		xLables.clear();
		for (int i = 0; i < intList.size(); i++) {
			xLables.add(intList.get(i) + "");
		}
//		//折线图排序
		List intList2 = new ArrayList<>();
		for (String str : xLablesLineChart) {
			if(!TextUtils.isEmpty(str)){
				int i = Integer.parseInt(str);
				intList2.add(i);
			}
		}
		Collections.sort(intList2);
		xLablesLineChart.clear();
		for (int i = 0; i < intList2.size(); i++) {
			xLablesLineChart.add(intList2.get(i) + "");
		}
		xLablesLineChart.add(0, "");
		xLablesLineChart.add(7, "");
		xLablesLineChart.add(8, "");
		xLablesLineChart.add(15, "");
		xLablesLineChart.add(16, "");
		// 初始化标签
		bar11.setXlables(xLables.subList(0, 6));
		bar12.setXLables(xLablesLineChart.subList(0, 8));
		bar21.setXlables(xLables.subList(6, 12));
		bar22.setXLables(xLablesLineChart.subList(8, 16));
	}

	private void viewPagerAddContent() {
		view1 = (LinearLayout) inflater.inflate(
				R.layout.view_single_device_year_data, null);
		view2 = (LinearLayout) inflater.inflate(
				R.layout.view_single_device_year_data2, null);
		views.add(view1);
		views.add(view2);
		viewPager.setAdapter(new DataAdapter());
		/********** INITIALIZES *************/
		title11 = (TextView) view1.findViewById(R.id.title1);
		title12 = (TextView) view1.findViewById(R.id.title2);
		/********** INITIALIZES *************/
		title21 = (TextView) view2.findViewById(R.id.title1);
		title22 = (TextView) view2.findViewById(R.id.title2);
	}

	/***************************************************/
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

	private void bindViews() {
		llSelectTime = (LinearLayout) view.findViewById(R.id.llSelectTime);
		tvYear = (TextView) view.findViewById(R.id.tvYear);
		llLast = (LinearLayout) view.findViewById(R.id.llLast);
		tvYear2 = (TextView) view.findViewById(R.id.tvYear2);
		tvL1 = (TextView) view.findViewById(R.id.tvL1);
		llLable = (LinearLayout) view.findViewById(R.id.llLable);
		tvL2 = (TextView) view.findViewById(R.id.tvL2);
		bar11 = (CustomBarChartViewMonth) view1.findViewById(R.id.bar1);
		bar12 = (AreaChart01ViewMonth) view1.findViewById(R.id.bar2);
		bar21 = (CustomBarChartViewMonth) view2.findViewById(R.id.bar1);
		bar22 = (AreaChart01ViewMonth) view2.findViewById(R.id.bar2);
		llShowHistory = (LinearLayout) view.findViewById(R.id.llShowHistory);
		tvShowSelectTime = (TextView) view.findViewById(R.id.tvShowSelectTime);
		lllowcolor = (LinearLayout) view.findViewById(R.id.lllowcolor);
		lluptime = (LinearLayout) view.findViewById(R.id.lluptime);
	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart(getClass().getName()); // 统计页面，"MainScreen"为页面名称，可自定义
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd(getClass().getName());
	}

	@Override
	public void onClick(View v) {
		if (v == llSelectTime) {
			// 出现年选择器
			DatePicker picker = new DatePicker(getActivity(), DatePicker.YEAR);
			picker.setRange(2015, 2050);
			picker.setSelectedItem(calendar.get(Calendar.YEAR),
					calendar.get(Calendar.MONTH) + 1,
					calendar.get(Calendar.DAY_OF_MONTH));
			picker.setOnDatePickListener(new OnYearPickListener() {

				@Override
				public void onDatePicked(String year) {
					LogUtilNIU.value(year);

//					if (isFuture(Integer.valueOf(year))) {
//						ToastUtils.shortToast(getContext(), "请选择较前的年份");
//					} else {
						// checkDataFromeService(deviceId,"year",year);
						h.sendEmptyMessage(Constant.MSG_WHAT_SHOW_DIALOG_OF_RESEND_TASK);
						udpKind = 0;
						getData(year);
						tvShowSelectTime.setText(year);
						llShowHistory.setVisibility(View.VISIBLE);
						lllowcolor.setVisibility(View.GONE);
						lluptime.setVisibility(View.GONE);
//					}
				}
			});
			picker.show();
		}
	}

	protected boolean isFuture(Integer value) {
		int now = Integer.valueOf(calendar.get(Calendar.YEAR));
		LogUtilNIU.value("value" + value + "now" + now);
		if (now <= value) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void dealHistoryDataFromService(List<Double> powers,
			List<Double> rates) {
		// 设置历史数据横坐标
		clearTitle();
		List<String> hisxlables = new ArrayList<>();
		for (int i = 1; i <= 13; i++) {
			hisxlables.add(String.valueOf(i));
		}

		for (int i = 0; i < 17; i++) {// 用一个循环，把所有月份初始到xLables这个集合里
			if (i == 0 || i % 8 == 0) {
				xLablesLineHistory.add("");// 0位或被8整除的位，都加“”
			} else if (i == 7 || (i - 7) % 8 == 0) {
				xLablesLineHistory.add("");
			} else if (i < 7) {
				xLablesLineHistory.add(hisxlables.get(i - 1));
			} else if (i < 15) {
				xLablesLineHistory.add(hisxlables.get(i - 3));
			}
		}
		List intList = new ArrayList<>();
		// 对xLables排序
		for (String str : hisxlables) {
			int i = Integer.parseInt(str);
			intList.add(i);
		}
		Collections.sort(intList);
		hisxlables.clear();
		for (int i = 0; i < intList.size(); i++) {
			hisxlables.add(intList.get(i) + "");
		}
		//
		List intList2 = new ArrayList<>();
		// 对xLables排序
		for (String str : xLablesLineHistory) {
			int i = Integer.parseInt(str);
			intList2.add(i);
		}
		Collections.sort(intList2);
		xLablesLineHistory.clear();
		for (int i = 0; i < xLablesLineHistory.size(); i++) {
			xLablesLineHistory.add(intList2.get(i) + "");
		}
		bar11.setXlables(hisxlables.subList(0, 6));
		bar12.setXLables(xLablesLineHistory.subList(0, 8));
		bar21.setXlables(hisxlables.subList(6, 12));
		bar22.setXLables(xLablesLineHistory.subList(8, 16));
		initBarDatas(powers);
		initArea(rates);
	}

	@Override
	protected void onServiceFailureConnected() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onServiceInterfaceFail(String json) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void clearTitle() {
		title11.setText("");
		title12.setText("");
		title21.setText("");
		title22.setText("");
	}

	@Override
	protected void addTitle() {
		// title11.setText(R.string.device_last_6_months);
		// title12.setText(R.string.device_last_6_months_power);
		// title21.setText(R.string.device_last_712_months);
		// title22.setText(R.string.device_last_712_months_power);
		title11.setText(R.string.device_month_of_year_power);
		title12.setText(R.string.device_month_of_year_avg_power);
		title21.setText(R.string.device_month_of_year_power);
		title22.setText(R.string.device_month_of_year_avg_power);

	}

	/**
	 * 从服务器获取数据
	 */
	private void getData(final String strYear) {
		if (NetworkUtils.isNetworkConnected(getActivity())) {
			new Thread() {
				public void run() {
					Message msg = new Message();
					msg.what = Constant.MSG_FAILURE;
					msg.obj = getString(R.string.cannot_connection_server);
					try {
						HttpUtils.TOKIN = "";
						Map map = new HashMap();
						map.put("deviceCode", deviceId);
						map.put("strYear", strYear);
						String result = WebServiceClient.CallWebService(
								"GetDeviceYearDetailPowerData", map,getActivity());
						Log.d("resultGetDeviceMonthDetailPowerData", result);
						if (result != null) {
							JSONObject obj = new JSONObject(result);
							int statusCode = obj.getInt("statusCode");
							if (statusCode == 0) {
								list.clear();
								JSONArray array = obj.getJSONArray("content");
								for (int i = 0; i < array.length(); i++) {
									JSONObject object = array.getJSONObject(i);
									SingleDeviceMonth data = new SingleDeviceMonth();
									if (TextUtils.isEmpty(object
											.getString("avgPowerData"))) {
										data.setAvgPowerData("0");
									} else {
										data.setAvgPowerData(object
												.getString("avgPowerData"));
									}
									if (TextUtils.isEmpty(object
											.getString("date"))) {
										data.setDate("0");
									} else {
										data.setDate(object.getString("date"));
									}
									if (TextUtils.isEmpty(object
											.getString("powerData"))) {
										data.setPowerData("0");
									} else {
										data.setPowerData(object
												.getString("powerData"));
									}
									list.add(data);
								}
								msg.what = Constant.MSG_SUCCESS;
							} else {
								Bundle b = new Bundle();
								b.putString("date", strYear);
								msg.setData(b);
								msg.obj = obj.getString("message");
								msg.what = Constant.MSG_FAILURE;
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

	private Handler defalutHandler = new Handler() {
		public void handleMessage(Message msg) {
			progressGettingDataDismissNoToast();
			switch (msg.what) {
			case Constant.MSG_SUCCESS:
				// 有数据
				dataView();
				break;
			case Constant.MSG_FAILURE:
				// 没有数据20170215
				Bundle resB = msg.getData();
				String resDate = resB.getString("date");
				year = resDate.substring(0, 4);
				month = "1";
				initDay(year, month);
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
			}
		};
	};

	private void toast(String text) {
		Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
	}

	// 处理返回数据显示
	private void dataView() {
		if (list.size() > 0) {
			SingleDeviceMonth data = list.get(0);
			// 年月日 201609
			year = data.getDate().substring(0, 4);
			month = data.getDate().substring(4, 6);
			// 初始化日
			initDay(year, month);
			initX();
			powerList.clear();
			powerDatas.clear();
			for (int i = 0; i < list.size(); i++) {
				powerList.add(list.get(i).getPowerData());
			}
			// 得到这个数据后，更新view1中图表1 TODO
			for (int i = 0; i < powerList.size(); i++) {
				String powerData = powerList.get(i);
				// if (!TextUtils.isEmpty(powerData)) {
				Double d = Double.valueOf(powerData);
				powerDatas.add(d);
				// }
			}
			// 用电量
			initBarDatas(powerDatas);
			// 用电功率
			listsRateList.clear();
			powerRates.clear();
			for (int i = 0; i < list.size(); i++) {
				listsRateList.add(list.get(i).getAvgPowerData());
			}
			// 把次数据做小数处理
			for (int i = 0; i < listsRateList.size(); i++) {
				String rateData = listsRateList.get(i);
				// if (!TextUtils.isEmpty(rateData)) {
				// String resData = ModbusCalUtil.addDotDel0(rateData, 2);
				Double d = Double.valueOf(rateData);
				powerRates.add(d);
				// } else {
				// powerRates.add(null);
				// }

			}
			LogUtilNIU.value("listsRateList" + listsRateList + "powerRates"
					+ powerRates);
			initArea(powerRates);// 初始化区域图
		}
	}
}
