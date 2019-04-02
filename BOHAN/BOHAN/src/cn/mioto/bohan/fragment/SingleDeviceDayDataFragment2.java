package cn.mioto.bohan.fragment;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import cn.mioto.bohan.BApplication;
import cn.mioto.bohan.Constant;
import cn.mioto.bohan.R;
import cn.mioto.bohan.activity.MenuSingleDeviceSumActivity;
import cn.mioto.bohan.exception.ExceptionManager;
import cn.mioto.bohan.utils.Enterface;
import cn.mioto.bohan.utils.HttpUtils;
import cn.mioto.bohan.utils.LoadDataThreadUtil;
import cn.mioto.bohan.utils.LogUtilNIU;
import cn.mioto.bohan.utils.ModbusCalUtil;
import cn.mioto.bohan.utils.NetworkUtils;
import cn.mioto.bohan.utils.ToastUtils;
import cn.mioto.bohan.view.AreaChart01ViewHour;
import cn.mioto.bohan.view.CustomBarChartViewHour;
import cn.mioto.bohan.view.timepicker.DatePicker;
import cn.mioto.bohan.view.timepicker.DatePicker.OnYearMonthDayPickListener;

import com.mioto.bohan.webservice.WebServiceClient;
import com.umeng.analytics.MobclickAgent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import steed.framework.android.client.JsonClientHandler2;

/**
 * 类说明：单个设备日数据 内嵌两个fragment 分别存12小时24小时数据
 */
public class SingleDeviceDayDataFragment2 extends BaseSingleDeviceDataFragment
		implements OnClickListener {
	private View view;
	private LayoutInflater inflater;
	private ViewPager viewPager;
	private LinearLayout view1;
	private LinearLayout view2;
	private LinearLayout view3;
	private LinearLayout view4;
	private LinearLayout llYear2;
	private List<LinearLayout> views = new ArrayList<>();
	private Calendar c = Calendar.getInstance();
	private String year;
	private String month;
	private String today;
	private String hour;
	private String yesterday;
	/***************************************************/
	private TextView tvYear;
	private TextView tvYear2;
	private TextView tvMonth;
	private TextView tvMonth2;
	private TextView tvDate;
	private LinearLayout llLast;
	private TextView tvDate2;
	private TextView tvL1;
	private TextView tvL2;
	private LinearLayout llLable;
	private LinearLayout llSelectTime;// 选择时间的点击区域
	/***************************************************/
	private cn.mioto.bohan.view.CustomBarChartViewHour bar11;
	private cn.mioto.bohan.view.AreaChart01ViewHour bar12;
	private cn.mioto.bohan.view.CustomBarChartViewHour bar21;
	private cn.mioto.bohan.view.AreaChart01ViewHour bar22;
	private cn.mioto.bohan.view.CustomBarChartViewHour bar31;
	private cn.mioto.bohan.view.AreaChart01ViewHour bar32;
	private cn.mioto.bohan.view.CustomBarChartViewHour bar41;
	private cn.mioto.bohan.view.AreaChart01ViewHour bar42;

	/***************************************************/
	private List<String> xLables = new ArrayList<>();
	// 折线图的横坐标
	private List<String> xLablesLineChart = new ArrayList<>();
	private List<String> colors = new ArrayList<>();
	private List<String> xLablesHistory = new ArrayList<>();
	private List<String> xLablesLineHistory = new ArrayList<>();
	private List<String> colorsHistory = new ArrayList<>();
	private Boolean showOneDay = false;
	/***************************************************/
	private UDPBrocastReceiver receiver;
	private LinearLayout lllowcolor;
	private LinearLayout lluptime;
	// 现在历史资料后，显示的历史事件区域
	private LinearLayout llShowHistory;
	// 历史事件显示
	private TextView tvShowSelectTime;
	// 标题
	/********** DECLARES *************/
	private TextView title11;
	private TextView title12;
	/********** DECLARES *************/
	private TextView title21;
	private TextView title22;
	/********** DECLARES *************/
	private TextView title31;
	private TextView title32;
	/********** DECLARES *************/
	private TextView title41;
	private TextView title42;

	public static Socket socket;
	public static BufferedReader in;
	public static PrintWriter out;
	public ReceiveThread receiveThread;

	/* Please visit http://www.ryangmattison.com for updates */

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		view = inflater.inflate(R.layout.fragment_single_device_day_data, null);
		viewPager = (ViewPager) view.findViewById(R.id.viewPager);
		/***************************************************/
		bindViews();
		viewPagerAddContent();
		/********* 初始化广播 ******************************************/
		receiver = new UDPBrocastReceiver();
		getContext().registerReceiver(receiver, filter);
		// 查询数据，显示dialog
		// 初始化Socket
		socket = BApplication.instance.getSocket();
		checkDataAndUpdateCharts();// 查询数据
		llSelectTime.setOnClickListener(this);
		// h = ((MenuSingleDeviceSumActivity)getActivity()).getHandler();
		// 初始化横历史数据横坐标
		for (int i = 0; i < 25; i++) {
			xLablesHistory.add(String.valueOf(i));
			colorsHistory.add("light");
		}
		for (int i = 0; i < 33; i++) {// 用一个循环，把所有月份初始到xLables这个集合里
			if (i == 0 || i % 8 == 0) {
				xLablesLineHistory.add("");// 0位或被8整除的位，都加“”
			} else if (i == 7 || (i - 7) % 8 == 0) {
				xLablesLineHistory.add("");
			} else if (i < 7) {
				xLablesLineHistory.add(xLablesHistory.get(i - 1));
			} else if (i < 15) {
				xLablesLineHistory.add(xLablesHistory.get(i - 3));
			} else if (i < 23) {
				xLablesLineHistory.add(xLablesHistory.get(i - 5));
			} else if (i < 31) {
				xLablesLineHistory.add(xLablesHistory.get(i - 7));
			}
		}
		return view;
	}

	// ------------------------------------------------------------------------------
	// 初始化view部分
	private void initDay(String year, String month, String today, String hour) {
		this.year = year;
		this.month = month;
		this.hour = hour;
		this.today = today;
		LogUtilNIU.value("年" + this.year + "月" + this.month + "日" + this.today);
		// 第一栏时间
		tvYear.setText(year);
		tvMonth.setText(month);
		tvDate.setText(today);
		LogUtilNIU.value("小时数是" + hour);// 00
		// tvHour.setText(calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE));//显示系统当前的时间,24小时制
		if (!hour.equals("24")) {
			// 只有整24点才会显示1天的数据，柱状图柱子一种颜色。
			// 会显示跨度2天的数据,两种颜色表示
			/*
			 * 考虑几种情况 普通月份的某天 okay 普通月份的1号 okay 1月1号 okay
			 */
			LogUtilNIU.value("小时数是不为0的判断");
			if (Integer.valueOf(today) == 1) {
				// 如果是1号，就要显示上个月最后一天的日期作为标题日期。
				// 得到上个月的总天数
				if (month.equals("1")) {// 1月1号
					c.set(Calendar.YEAR, Integer.valueOf(year));
					c.set(Calendar.MONTH, Integer.valueOf(month) - 1 - 1);// 上个月。减去1为今月，减去2为上个月
					int dateOfMonth = c.getActualMaximum(Calendar.DATE);// 获得当前月份的最大天数，数据来源为手机，需要改为设备TODO
					tvMonth2.setText(12 + "");// 月份设置为12月
					tvDate2.setText(dateOfMonth + "");
					// 还要显示年份
					llYear2.setVisibility(View.VISIBLE);
					tvYear2.setText(Integer.valueOf(year) - 1 + "");
					LogUtilNIU.value("执行了fragment1的yesterday赋值");
					tvL1.setText(month
							+ getResources().getString(R.string.month1) + today
							+ getResources().getString(R.string.day1));
					tvL2.setText(12 + getResources().getString(R.string.month1)
							+ dateOfMonth
							+ getResources().getString(R.string.day1));
				} else if (!month.equals("1")) {// 普通月份的1号
					LogUtilNIU.value("日数据，2颜色，其他月份1号");
					// 显示上个月的最后一天
					c.set(Calendar.YEAR, Integer.valueOf(year));
					c.set(Calendar.MONTH, Integer.valueOf(month) - 1 - 1);// 上个月。减去1为今月，减去2为上个月
					int dateOfMonth = c.getActualMaximum(Calendar.DATE);
					tvYear.setText(year);
					tvMonth.setText(month);
					tvDate.setText(today);
					tvL1.setText(month
							+ getResources().getString(R.string.month1) + today
							+ getResources().getString(R.string.day1));
					tvMonth2.setText(Integer.valueOf(month) - 1 + "");
					tvDate2.setText(dateOfMonth + "");
					tvL2.setText(Integer.valueOf(month) - 1 + ""
							+ getResources().getString(R.string.month1)
							+ dateOfMonth
							+ getResources().getString(R.string.day1));
				}
			} else {// 普通月份的某天
				tvMonth2.setText(month);
				tvDate2.setText(Integer.valueOf(today) - 1 + "");
				LogUtilNIU.value("执行了fragment1的yesterday赋值");
				tvL1.setText(month + getResources().getString(R.string.month1)
						+ today + getResources().getString(R.string.day1));
				tvL2.setText(month + getResources().getString(R.string.month1)
						+ (Integer.valueOf(today) - 1)
						+ getResources().getString(R.string.day1));
			}
			/***************************************************/
		} else {
			// 0点的情况，显示前天1整天的数据，1种颜色显示
			/*
			 * 考虑几种情况 普通月份的某天 okay 普通月份的1号 okay 1月1号 okay
			 */
			showOneDay = true;
			if (!month.equals("1") && !(Integer.valueOf(today) == 1)) {// 普通月份的某天
				// 第二级标签隐藏
				llLable.setVisibility(View.GONE);
				llLast.setVisibility(View.GONE);
				tvYear.setText(year);
				tvMonth.setText(month);
				tvDate.setText(Integer.valueOf(today) - 1 + "");// 设为前一天一整天
				tvL1.setText(month + getResources().getString(R.string.month1)
						+ (Integer.valueOf(today) - 1)
						+ getResources().getString(R.string.day1));
			} else if (!month.equals("1") && (Integer.valueOf(today) == 1)) {// 普通月份的1号
				// 第二级标签隐藏
				LogUtilNIU.value("日数据，普通月份1号");
				llLable.setVisibility(View.GONE);
				llLast.setVisibility(View.GONE);
				tvYear.setText(year);
				tvMonth.setText(Integer.valueOf(month) - 1 + "");// 显示为上个月最后一天
				c.set(Calendar.YEAR, Integer.valueOf(year));
				c.set(Calendar.MONTH, Integer.valueOf(month) - 1 - 1);// 上个月。减去1为今月，减去2为上个月
				int dateOfMonth = c.getActualMaximum(Calendar.DATE);
				tvDate.setText(dateOfMonth + "");// 设为上个月最后一天
				tvL1.setText((Integer.valueOf(month) - 1)
						+ getResources().getString(R.string.month1)
						+ dateOfMonth + getResources().getString(R.string.day1));
			} else if (month.equals("1") && today.equals("1")) {// 1月1号
				// 1月1日晚上12点后的情况
				// 需要显示上一年最后一天全天24小时的数据
				llLable.setVisibility(View.GONE);
				llLast.setVisibility(View.GONE);
				tvDate.setText("31");// 设为前一天一整天
				tvYear.setText(Integer.valueOf(year) - 1 + "");
				tvMonth.setText("12");
				tvL1.setText("12" + getResources().getString(R.string.month1)
						+ "31" + getResources().getString(R.string.day1));
				today = c.get(Calendar.DAY_OF_MONTH) - 1 + "";// 标题数据为昨天
			}
		}
	}

	/**
	 * @Title: initX
	 * @Description:初始化两个图表的横坐标（和颜色安排）
	 * @return void
	 * @throws
	 */
	private void initX() {
		// 当前钟点数
		int m0 = Integer.valueOf(hour);
		LogUtilNIU.value("*******从设备查得的现在的点数为******" + m0);
		/*
		 * 特别说明，原集合只要24和元素 但是因为sublist方法不能截取到最后，所以List要加多1个元素
		 */
		for (int i = 0; i < 25; i++) {// 用一个循环，把所有月份初始到xLables这个集合里
			if ((m0 - i) == 0) {
				// 如果当时间为0点
				// 刚好为时间跨度
				// 则从m0从24开始算
				xLables.add("24");
				m0 = 24 + i;
			} else if (!((m0 - i) == 0)) {// 当时间不是刚好跨越1天的时候
				xLables.add((m0 - i) + "");
			}
		}

		for (int i = 0; i < 33; i++) {// 用一个循环，把所有月份初始到xLables这个集合里
			if (i == 0 || i % 8 == 0) {
				xLablesLineChart.add("");// 0位或被8整除的位，都加“”
			} else if (i == 7 || (i - 7) % 8 == 0) {
				xLablesLineChart.add("");
			} else if (i < 7) {
				xLablesLineChart.add(xLables.get(i - 1));
			} else if (i < 15) {
				xLablesLineChart.add(xLables.get(i - 3));
			} else if (i < 23) {
				xLablesLineChart.add(xLables.get(i - 5));
			} else if (i < 31) {
				xLablesLineChart.add(xLables.get(i - 7));
			}
		}
		LogUtilNIU.value("xLablesLineChart" + xLablesLineChart.toString());

		LogUtilNIU.value("钟点数为" + xLables.toString());
		String color = "light";
		// if(BApplication.isSmallScreen){
		for (int i = 0; i < xLables.size(); i++) {
			if (i != 0) {
				if (xLables.get(i).equals("24")) {// 浅色。这个脑残的做法，用来区别柱子颜色。
					if (!showOneDay) {
						color = "dark";
					}
				}
			}
			colors.add(color);
		}
		bar11.setXlables(xLables.subList(0, 6));
		bar12.setXLables(xLablesLineChart.subList(0, 8));
		bar21.setXlables(xLables.subList(6, 12));
		bar22.setXLables(xLablesLineChart.subList(8, 16));
		bar31.setXlables(xLables.subList(12, 18));
		bar32.setXLables(xLablesLineChart.subList(16, 24));
		bar41.setXlables(xLables.subList(18, 24));
		bar42.setXLables(xLablesLineChart.subList(24, 32));
	}

	private void viewPagerAddContent() {
		view1 = (LinearLayout) inflater.inflate(
				R.layout.view_single_device_day_data, null);
		view2 = (LinearLayout) inflater.inflate(
				R.layout.view_single_device_day_data2, null);
		view3 = (LinearLayout) inflater.inflate(
				R.layout.view_single_device_day_data3, null);
		view4 = (LinearLayout) inflater.inflate(
				R.layout.view_single_device_day_data4, null);
		views.add(view1);
		views.add(view2);
		views.add(view3);
		views.add(view4);
		viewPager.setAdapter(new DataAdapter());
		/***************************************************/
		// 第一个view里的控件
		bar11 = (CustomBarChartViewHour) view1.findViewById(R.id.bar1);
		bar12 = (AreaChart01ViewHour) view1.findViewById(R.id.bar2);
		// 第二个view里的控件
		bar21 = (CustomBarChartViewHour) view2.findViewById(R.id.bar1);
		bar22 = (AreaChart01ViewHour) view2.findViewById(R.id.bar2);
		// 第三个view里的控件
		bar31 = (CustomBarChartViewHour) view3.findViewById(R.id.bar1);
		bar32 = (AreaChart01ViewHour) view3.findViewById(R.id.bar2);
		// 第四个view里的控件
		bar41 = (CustomBarChartViewHour) view4.findViewById(R.id.bar1);
		bar42 = (AreaChart01ViewHour) view4.findViewById(R.id.bar2);
		// 获得所有的标题
		/********** INITIALIZES *************/
		title11 = (TextView) view1.findViewById(R.id.title1);
		title12 = (TextView) view1.findViewById(R.id.title2);
		/********** INITIALIZES *************/
		title21 = (TextView) view2.findViewById(R.id.title1);
		title22 = (TextView) view2.findViewById(R.id.title2);
		/********** INITIALIZES *************/
		title31 = (TextView) view3.findViewById(R.id.title1);
		title32 = (TextView) view3.findViewById(R.id.title2);
		/********** INITIALIZES *************/
		title41 = (TextView) view4.findViewById(R.id.title1);
		title42 = (TextView) view4.findViewById(R.id.title2);
	}

	private void bindViews() {
		tvYear = (TextView) view.findViewById(R.id.tvYear);
		tvYear2 = (TextView) view.findViewById(R.id.tvYear2);
		tvMonth = (TextView) view.findViewById(R.id.tvMonth);
		tvMonth2 = (TextView) view.findViewById(R.id.tvMonth2);
		tvDate = (TextView) view.findViewById(R.id.tvDate);
		llLast = (LinearLayout) view.findViewById(R.id.llLast);
		tvDate2 = (TextView) view.findViewById(R.id.tvDate2);
		tvL1 = (TextView) view.findViewById(R.id.tvL1);
		tvL2 = (TextView) view.findViewById(R.id.tvL2);
		llLable = (LinearLayout) view.findViewById(R.id.llLable);
		llYear2 = (LinearLayout) view.findViewById(R.id.llYear2);
		llSelectTime = (LinearLayout) view.findViewById(R.id.llSelectTime);
		llShowHistory = (LinearLayout) view.findViewById(R.id.llShowHistory);
		tvShowSelectTime = (TextView) view.findViewById(R.id.tvShowSelectTime);
		lllowcolor = (LinearLayout) view.findViewById(R.id.lllowcolor);
		lluptime = (LinearLayout) view.findViewById(R.id.lluptime);
	}

	public void addTitle() {
		// 标题显示方式1
		// title11.setText(R.string.device_last_5_hours);
		// title12.setText(R.string.device_last_5_hours_power);
		// title21.setText(R.string.device_last_610_hours);
		// title22.setText(R.string.device_last_610_hours_power);
		// title31.setText(R.string.device_last_1115_hours);
		// title32.setText(R.string.device_last_1115_hours_power);
		// title41.setText(R.string.device_last_1620_hours);
		// title42.setText(R.string.device_last_1620_hours_power);
		// 标题显示方式2
		title11.setText(R.string.device_last_24_hours);
		title12.setText(R.string.device_last_24_hours_rates);
		title21.setText(R.string.device_last_24_hours);
		title22.setText(R.string.device_last_24_hours_rates);
		title31.setText(R.string.device_last_24_hours);
		title32.setText(R.string.device_last_24_hours_rates);
		title41.setText(R.string.device_last_24_hours);
		title42.setText(R.string.device_last_24_hours_rates);
	}

	public void clearTitle() {
		title11.setText("");
		title12.setText("");
		title21.setText("");
		title22.setText("");
		title31.setText("");
		title32.setText("");
		title41.setText("");
		title42.setText("");
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

	// ------------------------------------------------------------------------------
	// 数据处理部分
	public class UDPBrocastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// 区分接收到的是哪种广播
			String action = intent.getAction();
			if (action.equals(Constant.SOCKET_BROCAST_ONRECEIVED)) {
				String message = intent
						.getStringExtra(Constant.SOCKET_BROCAST_EXTRA_MESSAGE);
				if (isReqCodeEqual(message, "0010")
						|| isReqCodeEqual(message, "0011")) {
					showContent(message);
				}
			}
		}
	}

	/**
	 * @Title: checkDataAndUpDateView
	 * @Description:创建一个查询方法，适当的时候调用去查询数据 查询数据以后更新图表
	 * @return void
	 * @throws
	 */
	public void checkDataAndUpdateCharts() {
		addTitle();
		llShowHistory.setVisibility(View.GONE);
		lllowcolor.setVisibility(View.VISIBLE);
		lluptime.setVisibility(View.VISIBLE);
		// 发UDP指令查询单个设备的日数据信息
		// 发送查询单个设备用电数据的指令
		/********* 查询上24小时的用电量 ******************************************/
		// Intent intent = new Intent();
		// intent.setAction(Constant.BROCAST_CHART_SHOW_DIALOG);
		// getContext().sendBroadcast(intent);
		checkNowData();// 查询数据
	}

	// 查询用电量
	private void checkNowData() {
		udpKind = 0;
		String verCode = ModbusCalUtil.verNumber(deviceId + "00100000");
		final String msg1 = "E7" + deviceId + "00100000" + verCode + "0D";// 0010
																			// 查询上24小时的用电量
		// new LoadDataThreadUtil(msg1, h, deviceBSSID, getContext()).start();
		BApplication.instance.socketSend(msg1);
		receiveThread = new ReceiveThread();
		receiveThread.start();
	}

	// 查询功率
	protected void checkRate() {
		udpKind = 1;
		String verCode2 = ModbusCalUtil.verNumber(deviceId + "00110000");
		final String msg2 = "E7" + deviceId + "00110000" + verCode2 + "0D";// 0011
																			// 询上24小时的用电平均功率
		// new LoadDataThreadUtil(msg2, h, deviceBSSID, getContext()).start();
		BApplication.instance.socketSend(msg2);
		receiveThread = new ReceiveThread();
		receiveThread.start();
	}

	/**
	 * @Title: checkNowDataFromService
	 * @Description:想服务器查询数据的方法，注意回调对数据的处理
	 * @return void
	 * @throws
	 */
	protected void checkNowDataFromService(String smessage) {
		new Enterface("sendToDevice.act").addParam("deviceid", deviceId)
				.addParam("String content", smessage)
				.doRequest(new JsonClientHandler2() {
					@Override
					public void onInterfaceSuccess(String message,
							String contentJson) {
						LogUtilNIU.j("单个设备日数据服务器回调内容--->" + contentJson);
						if (isReqCodeEqual(message, "0010")) {// 判断接收的数据是针对哪个指令的
							udpok = true;
							showContent(contentJson);
						} else if (isReqCodeEqual(message, "0011")) {
							udpok2 = true;
							showContent(contentJson);
						}
					}

					@Override
					public void onInterfaceFail(String json) {

					}

					@Override
					public void onFailureConnected(Boolean canConnect) {

					}
				});
	}

	/**
	 * @Title: showContent
	 * @Description: 处理返回回来的指令
	 * @return void
	 * @param 回调数据
	 * @throws
	 */
	private Boolean rightDataOk1 = false;
	private Boolean rightDataOk2 = false;

	@Override
	public void showContent(String contentJson) {
		if (checkUDPMessage(contentJson)) {
			if (isReqCodeEqual(contentJson, "0010")) {// 单个设备日用电量
				BApplication.instance.setResendTaskShowBreak(true);
				progressGettingDataDismissNoToast();
				// 获得设备时间并显示
				// 3a6932285722110000100066000000270000005600000056000000410000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000160923232914790d
				LogUtilNIU.value("0010总位数" + contentJson.length());
				String yymmddhh = contentJson.substring(216, 224);// 年月日时
																	// 16092323
				LogUtilNIU.value("年月日时-------"
						+ contentJson.substring(216, 224));
				// 16092323
				year = "20" + yymmddhh.substring(0, 2);
				month = yymmddhh.substring(2, 4);
				today = yymmddhh.substring(4, 6);
				hour = yymmddhh.substring(6);
				if (hour.equals("00")) {
					hour = "24";
				}
				// 初始化日
				initDay(year, month, today, hour);
				initX();
				dealPowerDataForEachID(contentJson.substring(24, 216));
				progressGettingDataShow("最近用电功率查询中");
				h.postDelayed(new Runnable() {
					@Override
					public void run() {
						// 查询功率
						checkRate();
					}
				}, Constant.RESENDTIME);
			} else if (isReqCodeEqual(contentJson, "0011")) {// 单个设备日平均功率
				// BApplication.instance.setResendTaskShowBreak(true);
				progressGettingDataDismissNoToast();
				dealPowerRateForEachID(contentJson.substring(24, 168));// 解析每个设备的用电功率
			}
		}
		// if(rightDataOk1){//如果两个消息都收到就发广播要dialog消失
		// if(rightDataOk2){
		// Intent intent = new Intent();
		// intent.setAction(Constant.BROCAST_CHART_DIMISS_DIALOG);
		// getContext().sendBroadcast(intent);}
		// }
	}

	/*
	 * 
	 * 设备的用电量的集合，是最近24小时的 处理后最后得到用电量 list<Double>集合
	 */
	private List<String> hoursPowerList = new ArrayList<>();
	private List<Double> powerDatas = new ArrayList<>();

	private void dealPowerDataForEachID(String content) {
		hoursPowerList.clear();
		powerDatas.clear();
		LogUtilNIU.value("日用电数据截取为" + content);
		// 截取为12分的list集合
		for (int i = 0; i < content.length(); i += 8) {
			hoursPowerList.add(content.substring(i, i + 8));// 天才！！！！！有木有！！！
		}
		for (int i = 0; i < hoursPowerList.size(); i++) {
			String data = hoursPowerList.get(i);
			data = ModbusCalUtil.addDotDel0(data, 6);
			Double d = Double.valueOf(data);
			powerDatas.add(d);
		}
		LogUtilNIU.value("处理后的日用电量数据为" + powerDatas);
		initBarDatas(powerDatas);
	}

	private void initBarDatas(List<Double> powerDatas) {
		// 数据改变柱子颜色和设置数据
		Double max = ModbusCalUtil.countBiggestShowItsUpper(powerDatas);
		LogUtilNIU.value("max" + max);
		bar11.setBarColorAndData(colors.subList(0, 6),
				powerDatas.subList(0, 6), max);
		/***************************************************/
		bar21.setBarColorAndData(colors.subList(6, 12),
				powerDatas.subList(6, 12), max);
		/***************************************************/
		bar31.setBarColorAndData(colors.subList(12, 18),
				powerDatas.subList(12, 18), max);
		/***************************************************/
		bar41.setBarColorAndData(colors.subList(18, 24),
				powerDatas.subList(18, 24), max);
	}

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
		// 把数据做小数处理
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

	// 给下面的折线图图加上数据
	private void initArea(List<Double> datas) {
		// 全部数据都乘以1000
		// 把上面存入的连贯数据作处理，变成前后数据都是0的折线图形式
		List<Double> lineDatas = new ArrayList<>();
		for (int i = 0; i < 33; i++) {
			if (i == 0 || i % 8 == 0) {
				lineDatas.add(0d);// 0位或被8整除的位，都加“”
			} else if (i == 7 || (i - 7) % 8 == 0) {
				lineDatas.add(0d);
			} else if (i < 7) {
				lineDatas.add(datas.get(i - 1) * 1000);
			} else if (i < 15) {
				lineDatas.add(datas.get(i - 3) * 1000);
			} else if (i < 23) {
				lineDatas.add(datas.get(i - 5) * 1000);
			} else if (i < 31) {
				lineDatas.add(datas.get(i - 7) * 1000);
			}
		}
		Double max = ModbusCalUtil.countBiggestShowItsUpper(lineDatas);
		bar12.setBarColorAndData(lineDatas.subList(0, 8), max);
		bar22.setBarColorAndData(lineDatas.subList(8, 16), max);
		bar32.setBarColorAndData(lineDatas.subList(16, 24), max);
		bar42.setBarColorAndData(lineDatas.subList(24, 32), max);
	}

	/***************************************************/
	private void sendDelay(final String msg, final Boolean udpReceived) {// 延迟向服务器查询数据
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (!udpReceived) {
					checkNowDataFromService(msg);
				} else {
				}
			}
		}, Constant.DELAY);// 定时间后执行判断，看有没有收到指令，如果没有，则进行服务器查询
	}

	@Override
	public void onDestroy() {
		getContext().unregisterReceiver(receiver);
		super.onDestroy();
	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("单个设备日数据"); // 统计页面，"MainScreen"为页面名称，可自定义
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("单个设备日数据");
	}

	@Override
	public void onClick(View v) {
		if (v == llSelectTime) {
			DatePicker picker = new DatePicker(getActivity());
			// 出现年月日时间选择器
			picker.setRange(2016, Integer.valueOf(year));
			Calendar calendar = Calendar.getInstance();
			picker.setSelectedItem(calendar.get(Calendar.YEAR),
					calendar.get(Calendar.MONTH) + 1,
					calendar.get(Calendar.DAY_OF_MONTH));
			picker.setOnDatePickListener(new OnYearMonthDayPickListener() {

				@Override
				public void onDatePicked(String year, String month, String day) {
					LogUtilNIU.value(year + month + day);
					LogUtilNIU.value(isFuture(Integer.valueOf(year + month
							+ day)));
					if (isFuture(Integer.valueOf(year + month + day))) {
						ToastUtils.shortToast(getContext(), "请选择昨天或以前的日期");
					} else {
						checkDataFromeService(deviceId, "year_month_day", year
								+ month + day);
						tvShowSelectTime
								.setText(year + "." + month + "." + day);
						llShowHistory.setVisibility(View.VISIBLE);
						lllowcolor.setVisibility(View.GONE);
						lluptime.setVisibility(View.GONE);
					}
				}
			});
			picker.show();
		}
	}

	protected boolean isFuture(Integer value) {
		int now = Integer.valueOf(year + ModbusCalUtil.add0fillLength(month, 2)
				+ ModbusCalUtil.add0fillLength(today, 2));
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
		bar11.setXlables(xLablesHistory.subList(0, 6));
		bar12.setXLables(xLablesLineHistory.subList(0, 8));
		bar21.setXlables(xLablesHistory.subList(6, 12));
		bar22.setXLables(xLablesLineHistory.subList(8, 16));
		bar31.setXlables(xLablesHistory.subList(12, 18));
		bar32.setXLables(xLablesLineHistory.subList(16, 24));
		bar41.setXlables(xLablesHistory.subList(18, 24));
		bar42.setXLables(xLablesLineHistory.subList(24, 32));
		initBarDatas(powers);
		initArea(rates);
	}

	@Override
	protected void onServiceFailureConnected() {

	}

	@Override
	protected void onServiceInterfaceFail(String json) {

	}

	/**
	 * 接收数据线程
	 */
	private class ReceiveThread extends Thread {
		@Override
		public void run() {
			Message msg = new Message();
			msg.what = Constant.MSG_FAILURE;
			msg.obj = getString(R.string.cannot_connection_server);
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
							String content = obj.getString("content");
							msg.obj = content;
							msg.what = Constant.MSG_SUCCESS;
						}
					}
				} catch (Exception e) {
					Log.e("Receive", e.getMessage());
					e.printStackTrace();
					msg.obj = e;
					msg.what = Constant.MSG_EXCPTION;
				}
			} else {
				try {
					Thread.sleep(1000L);
				} catch (InterruptedException e) {
					e.printStackTrace();
					msg.obj = e;
					msg.what = Constant.MSG_EXCPTION;
				}
			}
			handler.sendMessage(msg);
		}
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == Constant.MSG_SUCCESS) {
				progressGettingDataDismiss("");
				String message = (String) msg.obj;
				showContent(message);
			} else if (msg.what == Constant.MSG_EXCPTION) {
				progressGettingDataDismiss("数据读取失败");
			}
		}
	};
	
	
	/**
	 * 从服务器获取数据
	 */
	private void getData() {
		if (NetworkUtils.isNetworkConnected(getActivity())) {
			new Thread() {
				public void run() {
					Message msg = new Message();
					msg.what = Constant.MSG_FAILURE;
					msg.obj = getString(R.string.cannot_connection_server);
					try {
						HttpUtils.TOKIN = "";
						Map map = new HashMap();
//						map.put("deviceCode", deviceCode);
//						map.put("strYearMonth", value);
						String result = WebServiceClient.CallWebService(
								"GetDeviceMonthDetailPowerData", null); // "{\"code\":2,\"result\":{\"logincode\":1, \"result\":{\"id\":\"10001\"}}}";
						Log.d("resultLogin", result);
						if (result != null) {
							JSONObject obj = new JSONObject(result);
							int statusCode = obj.getInt("statusCode");
							if (statusCode == 0) {
								String[] arry = new String[] {};
								String res = obj.getString("content");
								if (!TextUtils.isEmpty(res)) {
									arry = res.split(",");
								}
								msg.what = Constant.MSG_SUCCESS;
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
			switch (msg.what) {
			case Constant.MSG_SUCCESS:
				break;
			case Constant.MSG_FAILURE:
				toast((String) msg.obj);
				break;
			case Constant.MSG_EXCPTION:
				toast(ExceptionManager.getErrorDesc(
						getActivity(), (Exception) msg.obj));
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

}
