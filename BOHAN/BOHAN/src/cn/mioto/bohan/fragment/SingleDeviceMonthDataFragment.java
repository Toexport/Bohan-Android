package cn.mioto.bohan.fragment;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.mioto.bohan.BApplication;
import cn.mioto.bohan.Constant;
import cn.mioto.bohan.R;
import cn.mioto.bohan.activity.MenuSingleDeviceSumActivity;
import cn.mioto.bohan.entity.SingleDeviceMonth;
import cn.mioto.bohan.exception.ExceptionManager;
import cn.mioto.bohan.utils.Enterface;
import cn.mioto.bohan.utils.ExceptionUtil;
import cn.mioto.bohan.utils.HttpUtils;
import cn.mioto.bohan.utils.LoadDataThreadSendManayUtil;
import cn.mioto.bohan.utils.LoadDataThreadUtil;
import cn.mioto.bohan.utils.LogUtilNIU;
import cn.mioto.bohan.utils.ModbusCalUtil;
import cn.mioto.bohan.utils.NetworkUtils;
import cn.mioto.bohan.utils.ToastUtils;
import cn.mioto.bohan.view.AreaChart01ViewDay;
import cn.mioto.bohan.view.CustomBarChartViewDay;
import cn.mioto.bohan.view.timepicker.DatePicker;

import com.mioto.bohan.webservice.WebServiceClient;
import com.umeng.analytics.MobclickAgent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
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
 * 类说明：单个设备月数据 内嵌3个fragment 分别放每10天数据 共30天
 */
public class SingleDeviceMonthDataFragment extends BaseSingleDeviceDataFragment
		implements OnClickListener {
	private View view;
	private ViewPager viewPager;
	/********** DECLARES *************/
	private TextView tvYear;
	private TextView tvMonth;
	private TextView tvMonth2;
	private TextView tvMonth3;
	private TextView tvYear2;
	private LinearLayout llSelectTime;// 选择时间的点击区域
	private LinearLayout llLast;
	private LinearLayout llLastLast;
	private TextView tvL1;
	private TextView tvL2;
	private TextView tvL3;
	private LinearLayout llLable0;
	private LinearLayout llLable;
	private LinearLayout llLable2;
	// 现在历史资料后，显示的历史事件区域
	private LinearLayout llShowHistory;
	// 历史事件显示
	private TextView tvShowSelectTime;
	private LinearLayout view1;
	private LinearLayout view2;
	private LinearLayout view3;
	private LinearLayout view4;
	private LinearLayout view5;
	private LinearLayout llYear2;
	private LayoutInflater inflater;
	private List<LinearLayout> views = new ArrayList<>();
	private String year = "", thismonth = "", today = "", showingMonth = "",
			lastMonth = "";
	private Calendar calendar = Calendar.getInstance();
	private CustomBarChartViewDay bar11;
	private AreaChart01ViewDay bar12;
	private CustomBarChartViewDay bar21;
	private AreaChart01ViewDay bar22;
	private CustomBarChartViewDay bar31;
	private AreaChart01ViewDay bar32;
	private CustomBarChartViewDay bar41;
	private AreaChart01ViewDay bar42;
	private CustomBarChartViewDay bar51;
	private AreaChart01ViewDay bar52;
	/***************************************************/
	private UDPBrocastReceiver receiver;
	private List<String> colors = new ArrayList<>();
	private LinearLayout lllowcolor;
	private LinearLayout lluptime;
	private List<String> xLablesHistory = new ArrayList<>();
	private List<String> xLablesLineHistory = new ArrayList<>();
	private List<String> colorsHistory = new ArrayList<>();
	// 折线图的横坐标
	private List<String> xLablesLineChart = new ArrayList<>();
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
	/********** DECLARES *************/
	private TextView title51;
	private TextView title52;

	private boolean isRequesting = true;

	// 月数据
	private List<SingleDeviceMonth> listMonth = new ArrayList<>();
	
	private SimpleDateFormat dataFormat=new SimpleDateFormat("yyyyMM");
	private String currentTime;//系统当前时间

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		view = inflater.inflate(R.layout.fragment_single_device_month_data,
				null);
		viewPager = (ViewPager) view.findViewById(R.id.viewPager);
		/***************************************************/
		viewPagerAddContent();
		bindViews();
		// initDay();//初始化选择的时间为现在的月份
		/********* 初始化广播 ******************************************/
		//receiver = new UDPBrocastReceiver();
		//getContext().registerReceiver(receiver, filter);
		/***************************************************/
		//checkDataAndUpdateCharts();// 获得数据
		llSelectTime.setOnClickListener(this);
		for (int i = 1; i < 31; i++) {
			xLablesHistory.add(String.valueOf(i));
			colorsHistory.add("light");
		}

		// 初始化折线图横坐标标签
		for (int i = 0; i < 41; i++) {// 用一个循环，把所有月份初始到xLables这个集合里
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
			} else if (i < 39) {
				xLablesLineHistory.add(xLablesHistory.get(i - 9));
			}
		}
		Date curDate  = new Date(System.currentTimeMillis());//获取当前时间       
		currentTime=  dataFormat.format(curDate);   
		getData(currentTime);//获取数据
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
		// initX();
		llShowHistory.setVisibility(View.GONE);
		lllowcolor.setVisibility(View.VISIBLE);
		lluptime.setVisibility(View.VISIBLE);
		// 发UDP指令查询单个设备的日数据信息
		// 发送查询单个设备用电数据的指令
		/********* 查询上30天的用电量 ******************************************/
		Intent intent = new Intent();
		intent.setAction(Constant.BROCAST_CHART_SHOW_DIALOG);
		getContext().sendBroadcast(intent);
		checkNowData();// 查询数据
	}

	private void checkNowData() {
		udpKind = 0;
		String verCode = ModbusCalUtil.verNumber(deviceId + "000E0000");
		final String msg1 = "3A" + deviceId + "000E0000" + verCode + "0D";// 0010
																			// 查询上24小时的用电量
		new LoadDataThreadUtil(msg1, h, deviceBSSID, getContext()).start();
	}

	// 查询功率
	protected void checkRate() {
		udpKind = 1;
		String verCode2 = ModbusCalUtil.verNumber(deviceId + "000F0000");
		final String msg2 = "3A" + deviceId + "000F0000" + verCode2 + "0D";// 0011
																			// 询上24小时的用电平均功率
		new LoadDataThreadUtil(msg2, h, deviceBSSID, getContext()).start();
	}

	private void initX() {
		/***************************************************/
		// 暂时不考虑颜色，只考虑下标正确显示
		// 获得下标数据
		List<String> xLables = new ArrayList<>();
		int beginDate = Integer.valueOf(today);// 以今天为起点
		showingMonth = thismonth;// 这个月为正在计量显示的月
		String showingColor = "light";
		for (int i = 0; i < 31; i++) {
			String date = beginDate - i + "";// date为显示的下标
			if (date.equals("0")) {// 如果为0，则设置为上个月的最后一天，也就算1号之后一天
				// 得到上个月总天数
				calendar.set(Calendar.YEAR, Integer.valueOf(year));
				calendar.set(Calendar.MONTH,
						Integer.valueOf(showingMonth) - 1 - 1);// 上个月。减去1为今月，减去2为上个月
				int dateOfMonth = calendar.getActualMaximum(Calendar.DATE);
				showingMonth = Integer.valueOf(showingMonth) - 1 + "";// 正在显示的月-1
				date = dateOfMonth + "";// 所要设置的日期由0变为上个月最后1天
				beginDate = Integer.valueOf(dateOfMonth) + i;// 又从最后一天开始算起
				if (i != 0) {// 如果不是第一个月份
					if ((Integer.valueOf(showingMonth) + 2) == Integer
							.valueOf(thismonth)) {// 如果横跨三个月
						if (showingColor.equals("dark")) {
							showingColor = "3rdColor";// 第三种颜色
						} else if (showingColor.equals("light")) {
							showingColor = "dark";// 第三种颜色
						}
						LogUtilNIU.value("显示第三种颜色");
						// 设置第三种标签和标题
						// if(i!=30){//如果是最后一个，就不要显示了
						// llLable2.setVisibility(view.VISIBLE);
						// }
						// tvL3.setText(showingMonth+getResources().getString(R.string.month1));
						// llLastLast.setVisibility(view.VISIBLE);
						// tvMonth3.setText(showingMonth);
					} else {
						showingColor = "dark";// 深色//第二个月份
					}
				}
			}
			colors.add(showingColor);
			xLables.add(date);
		}

		// 初始化折线图横坐标标签
		for (int i = 0; i < 41; i++) {// 用一个循环，把所有月份初始到xLables这个集合里
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
			} else if (i < 39) {
				xLablesLineChart.add(xLables.get(i - 9));
			}
		}

		LogUtilNIU.value("月数据下标生成xLables" + xLables);
		bar11.setXlables(xLables.subList(0, 6));
		bar12.setXLables(xLablesLineChart.subList(0, 8));
		bar21.setXlables(xLables.subList(6, 12));
		bar22.setXLables(xLablesLineChart.subList(8, 16));
		bar31.setXlables(xLables.subList(12, 18));
		bar32.setXLables(xLablesLineChart.subList(16, 24));
		bar41.setXlables(xLables.subList(18, 24));
		bar42.setXLables(xLablesLineChart.subList(24, 32));
		bar51.setXlables(xLables.subList(24, 30));
		bar52.setXLables(xLablesLineChart.subList(32, 40));
	}

	private void initDay(String cyear, String cmonth, String ctoday) {

		this.year = cyear;
		this.thismonth = cmonth;
		this.lastMonth = Integer.valueOf(thismonth) - 1 + "";
		this.today = ctoday;
		LogUtilNIU.value("现在要初始化头顶的日期显示，传过来的年月日参数，" + "年" + this.year + "月"
				+ this.thismonth + "日" + this.today);
		/*
		 * 月数据要考虑的情况
		 * 
		 * 当前月为1月 1月1日，显示一个月份，上年12月 okay 1月N日 okay
		 * 
		 * 普通月普通日 okay
		 * 
		 * 闰年3月1日 okay
		 * 
		 * 非闰年3月1日 okay
		 * 
		 * 闰年3月2日 okay
		 * 
		 * 非闰年3月2日 okay
		 * 
		 * N月1日 okay
		 */
		if (Integer.valueOf(thismonth) == 1) {// 如果当前月份为1月，则上个月要显示为12月还要显示年
			if (Integer.valueOf(thismonth) == 1 && !today.equals("1")) {// 1月N日
				LogUtilNIU.value("1月N日");
				tvYear.setText(year);// 年
				tvMonth.setText(thismonth);// 今个月需要加1，因为Java月份是从0开始的
				llYear2.setVisibility(View.VISIBLE);
				tvYear2.setText(Integer.valueOf(year) - 1 + "");// 年份显示为上一年
				tvMonth2.setText("12");// 月份设置为12月
				tvL1.setText(year + getResources().getString(R.string.year1)
						+ thismonth + getResources().getString(R.string.month1));// 这个月
				tvL2.setText((Integer.valueOf(year) - 1)
						+ getResources().getString(R.string.year1) + "12"
						+ getResources().getString(R.string.month1));// 上个月
			} else if (Integer.valueOf(thismonth) == 1 && today.equals("1")) {// 1月1日
				LogUtilNIU.value("1月1日");
				llLast.setVisibility(View.GONE);// 第二个年份隐藏
				tvYear.setText(Integer.valueOf(year) - 1 + "");// 显示上一年
				tvMonth.setText("12");// 显示为12月
				llLable.setVisibility(View.GONE);
				tvL1.setText(Integer.valueOf(year) - 1
						+ getResources().getString(R.string.year1) + "12"
						+ getResources().getString(R.string.month1));
			}
		} else {// 不为1月
			if (thismonth.equals("3") && Integer.valueOf(today) == 2) {// 3月2日
				// 非闰年3月1日
				// 得到上个月总天数
				calendar.set(Calendar.YEAR, Integer.valueOf(year));
				calendar.set(Calendar.MONTH, Integer.valueOf(thismonth) - 1 - 1);// 上个月。减去1为今月，减去2为上个月
				int dateOfMonth = calendar.getActualMaximum(Calendar.DATE);
				if (dateOfMonth == 28) {// 上个月总日数为28日，非闰年
					LogUtilNIU.value("非闰年3月2日");// 显示3种颜色
					tvYear.setText(year);// 年
					tvMonth.setText(thismonth);// 上个月
					tvMonth2.setText(Integer.valueOf(thismonth) - 1 + "");// 上上个月
					tvL1.setText(thismonth
							+ getResources().getString(R.string.month1));
					tvL2.setText(lastMonth
							+ getResources().getString(R.string.month1));
					llLable2.setVisibility(View.VISIBLE);
					llLastLast.setVisibility(View.VISIBLE);
					tvMonth3.setText(Integer.valueOf(lastMonth) - 1 + "");
					tvL3.setText((Integer.valueOf(lastMonth) - 1)
							+ getResources().getString(R.string.month1));
				} else if (dateOfMonth == 29) {// 闰年3月2日，显示2种颜色
					LogUtilNIU.value("闰年3月1日");
					tvYear.setText(year);// 年
					tvMonth.setText(lastMonth);// 上个月
					tvMonth2.setText(Integer.valueOf(lastMonth) - 1 + "");// 上上个月
					tvL1.setText(lastMonth
							+ getResources().getString(R.string.month1));
					tvL2.setText((Integer.valueOf(lastMonth) - 1)
							+ getResources().getString(R.string.month1));
					llLable2.setVisibility(View.GONE);
				}
				// 如果为1号，则标题不显示这个月，显示上个月和上个月(不是1月1日的情况)
			} else if (Integer.valueOf(today) == 1) {// N月1日
				LogUtilNIU.value("N月1日");
				tvYear.setText(year);// 年
				tvMonth.setText(lastMonth);// 上个月
				tvL1.setText(lastMonth
						+ getResources().getString(R.string.month1));
				calendar.set(Calendar.YEAR, Integer.valueOf(year));
				calendar.set(Calendar.MONTH, Integer.valueOf(thismonth) - 1 - 1);// 上个月。减去1为今月，减去2为上个月
				int dateOfMonth = calendar.getActualMaximum(Calendar.DATE);
				if (dateOfMonth >= 30) {// 月份天数大于30天
					llLable.setVisibility(View.GONE);
					llLast.setVisibility(View.GONE);
				} else {
					tvMonth2.setText(Integer.valueOf(lastMonth) - 1 + "");// 上上个月
					tvL2.setText((Integer.valueOf(lastMonth) - 1)
							+ getResources().getString(R.string.month1));
				}
			} else {
				LogUtilNIU.value("N月N日");
				tvYear.setText(year);// 年
				tvMonth.setText(thismonth);// 今个月需要加1，因为Java月份是从0开始的
				tvMonth2.setText(lastMonth + "");// 上个月
				tvL1.setText(thismonth
						+ getResources().getString(R.string.month1));// 这个月
				tvL2.setText(lastMonth
						+ getResources().getString(R.string.month1));// 上个月
			}
		}
		LogUtilNIU.value("calendar.getActualMaximum(Calendar.DATE)"
				+ calendar.getActualMaximum(Calendar.DATE));
	}

	private void viewPagerAddContent() {
		view1 = (LinearLayout) inflater.inflate(
				R.layout.view_single_device_month_data, null);
		view2 = (LinearLayout) inflater.inflate(
				R.layout.view_single_device_month_data2, null);
		view3 = (LinearLayout) inflater.inflate(
				R.layout.view_single_device_month_data3, null);
		view4 = (LinearLayout) inflater.inflate(
				R.layout.view_single_device_month_data4, null);
		view5 = (LinearLayout) inflater.inflate(
				R.layout.view_single_device_month_data5, null);
		views.add(view1);
		views.add(view2);
		views.add(view3);
		views.add(view4);
		views.add(view5);
		viewPager.setAdapter(new DataAdapter());
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
		/********** INITIALIZES *************/
		title51 = (TextView) view5.findViewById(R.id.title1);
		title52 = (TextView) view5.findViewById(R.id.title2);
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
		tvMonth = (TextView) view.findViewById(R.id.tvMonth);
		tvYear2 = (TextView) view.findViewById(R.id.tvYear2);
		llLast = (LinearLayout) view.findViewById(R.id.llLast);
		llLastLast = (LinearLayout) view.findViewById(R.id.llLastLast);
		tvMonth2 = (TextView) view.findViewById(R.id.tvMonth2);
		tvMonth3 = (TextView) view.findViewById(R.id.tvMonth3);
		tvL1 = (TextView) view.findViewById(R.id.tvL1);
		llLable0 = (LinearLayout) view.findViewById(R.id.llLable0);
		llLable = (LinearLayout) view.findViewById(R.id.llLable);
		llLable2 = (LinearLayout) view.findViewById(R.id.llLable2);
		llYear2 = (LinearLayout) view.findViewById(R.id.llYear2);
		tvL2 = (TextView) view.findViewById(R.id.tvL2);
		tvL3 = (TextView) view.findViewById(R.id.tvL3);
		bar11 = (CustomBarChartViewDay) view1.findViewById(R.id.bar11);
		bar12 = (AreaChart01ViewDay) view1.findViewById(R.id.bar12);
		bar21 = (CustomBarChartViewDay) view2.findViewById(R.id.bar21);
		bar22 = (AreaChart01ViewDay) view2.findViewById(R.id.bar22);
		bar31 = (CustomBarChartViewDay) view3.findViewById(R.id.bar31);
		bar32 = (AreaChart01ViewDay) view3.findViewById(R.id.bar32);
		bar41 = (CustomBarChartViewDay) view4.findViewById(R.id.bar41);
		bar42 = (AreaChart01ViewDay) view4.findViewById(R.id.bar42);
		bar51 = (CustomBarChartViewDay) view5.findViewById(R.id.bar51);
		bar52 = (AreaChart01ViewDay) view5.findViewById(R.id.bar52);
		llShowHistory = (LinearLayout) view.findViewById(R.id.llShowHistory);
		tvShowSelectTime = (TextView) view.findViewById(R.id.tvShowSelectTime);
		lllowcolor = (LinearLayout) view.findViewById(R.id.lllowcolor);
		lluptime = (LinearLayout) view.findViewById(R.id.lluptime);
	}

	public class UDPBrocastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 区分接收到的是哪种广播
			String action = intent.getAction();
			if (action.equals(Constant.SOCKET_BROCAST_ONRECEIVED)) {
				String message = intent
						.getStringExtra(Constant.SOCKET_BROCAST_EXTRA_MESSAGE);
				if (isReqCodeEqual(message, "000E")
						|| isReqCodeEqual(message, "000F")) {
					showContent(message);
				}
			}
		}
	}

	/**
	 * @Title: showContent
	 * @Description: 处理返回回来的指令
	 * @return void
	 * @param 回调数据
	 * @throws
	 */
	public void showContent(String contentJson) {
		ToastUtils.testToast(getContext(), "收到数据" + contentJson);
		if (checkUDPMessage(contentJson)) {
			if (isReqCodeEqual(contentJson, "000E")) {// 单个设备月电量
				BApplication.instance.setResendTaskShowBreak(true);
				progressGettingDataDismissNoToast();
				// 获得设备时间并显示
				// 3a68160805009500000e007e000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000161012095219580d
				// 280
				LogUtilNIU.value("000E总位数" + contentJson.length());
				String yymmdd = contentJson.substring(264, 270);// 年月日时 16092323
				LogUtilNIU.value("月数据的---年月日-------"
						+ contentJson.substring(264, 270));
				// 年月日 160923
				year = "20" + yymmdd.substring(0, 2);
				thismonth = yymmdd.substring(2, 4);
				today = yymmdd.substring(4, 6);
				// 初始化日
				initDay(year, thismonth, today);
				initX();
				dealPowerDataForEachID(contentJson.substring(24, 264));
				progressGettingDataShow("最近用电功率查询中");
				h.postDelayed(new Runnable() {
					@Override
					public void run() {
						checkRate();
					}
				}, Constant.RESENDTIME);
			} else if (isReqCodeEqual(contentJson, "000F")) {// 单个设备日平均功率
				BApplication.instance.setResendTaskShowBreak(true);
				progressGettingDataDismissNoToast();
				dealPowerRateForEachID(contentJson.substring(24, 204));// 解析每个设备的用电功率
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
		LogUtilNIU.value("月用电数据截取为" + content);
		// 截取为12分的list集合
		for (int i = 0; i < content.length(); i += 8) {
			powerList.add(content.substring(i, i + 8));// 天才！！！！！有木有！！！
		}
		// 得到这个数据后，更新view1中图表1 TODO
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
		// TODO Auto-generated method stub
		// 数据改变柱子颜色和设置数据
		Double max = ModbusCalUtil.countBiggestShowItsUpper(powerDatas);
		LogUtilNIU.value("月数据用电量最大值是--->" + max);
		bar11.setBarColorAndData(colors.subList(0, 6),
				powerDatas.subList(0, 6), max);
		bar21.setBarColorAndData(colors.subList(7, 12),
				powerDatas.subList(7, 12), max);
		bar31.setBarColorAndData(colors.subList(13, 18),
				powerDatas.subList(13, 18), max);
		bar41.setBarColorAndData(colors.subList(19, 24),
				powerDatas.subList(19, 24), max);
		bar51.setBarColorAndData(colors.subList(25, 30),
				powerDatas.subList(25, 30), max);
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
		// TODO
		for (int i = 0; i < 41; i++) {
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
			} else if (i < 39) {
				lineDatas.add(datas.get(i - 9) * 1000);
			}
		}
		Double max = ModbusCalUtil.countBiggestShowItsUpper(lineDatas);
		bar12.setBarColorAndData(lineDatas.subList(0, 8), max);
		bar22.setBarColorAndData(lineDatas.subList(8, 16), max);
		bar32.setBarColorAndData(lineDatas.subList(16, 24), max);
		bar42.setBarColorAndData(lineDatas.subList(24, 32), max);
		bar52.setBarColorAndData(lineDatas.subList(32, 40), max);
	}

	@Override
	public void onDestroy() {
		getContext().unregisterReceiver(receiver);
		super.onDestroy();
	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart(getClass().getName()); // 统计页面，"MainScreen"为页面名称，可自定义
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd(getClass().getName());
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
						LogUtilNIU.j("单个设备月数据服务器回调内容--->" + contentJson);
						if (isReqCodeEqual(message, "000E")) {// 判断接收的数据是针对哪个指令的
							udpok = true;
							showContent(contentJson);
						} else if (isReqCodeEqual(message, "000F")) {
							udpok2 = true;
							showContent(contentJson);
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

	@Override
	public void onClick(View v) {
		if (v == llSelectTime) {
			// 出现年月选择器
			DatePicker picker = new DatePicker(getActivity(),
					DatePicker.YEAR_MONTH);
			picker.setRange(2016, 2050);
			picker.setOnDatePickListener(new DatePicker.OnYearMonthPickListener() {
				@Override
				public void onDatePicked(String year, String month) {
					LogUtilNIU.value(year + month);
					// 显示历史事件的区域出现
					if (isFuture(Integer.valueOf(year + month))) {
						ToastUtils.shortToast(getContext(), "请选择之前的月份");
					} else {
//						checkDataFromeService(deviceId, "year_month", year
//								+ month);
						getData(dataFormat.format(year+month));
						tvShowSelectTime.setText(year + "." + month);
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
		int now = Integer.valueOf(year
				+ ModbusCalUtil.add0fillLength(thismonth, 2));
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
		bar51.setXlables(xLablesHistory.subList(24, 30));
		bar52.setXLables(xLablesLineHistory.subList(32, 40));
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
		title31.setText("");
		title32.setText("");
		title41.setText("");
		title42.setText("");
		title51.setText("");
		title52.setText("");
	}

	@Override
	protected void addTitle() {
		// title11.setText(R.string.device_last_6_days);
		// title12.setText(R.string.device_last_6_days_power);
		// title21.setText(R.string.device_last_7_12_days);
		// title22.setText(R.string.device_last_7_12_days_power);
		// title31.setText(R.string.device_last_13_18_days);
		// title32.setText(R.string.device_last_13_18_days_power);
		// title41.setText(R.string.device_last_19_24_days);
		// title42.setText(R.string.device_last_19_24_days_power);
		// title51.setText(R.string.device_last_25_30_days);
		// title52.setText(R.string.device_last_25_30_days_power);
		//
		title11.setText(R.string.device_last_30_days);
		title12.setText(R.string.device_last_30_days_rates);
		title21.setText(R.string.device_last_30_days);
		title22.setText(R.string.device_last_30_days_rates);
		title31.setText(R.string.device_last_30_days);
		title32.setText(R.string.device_last_30_days_rates);
		title41.setText(R.string.device_last_30_days);
		title42.setText(R.string.device_last_30_days_rates);
		title51.setText(R.string.device_last_30_days);
		title52.setText(R.string.device_last_30_days_rates);
	}

	/**
	 * 获取设备月数据
	 * 
	 * @param time
	 */
	private void getData(final String time) {
		if (NetworkUtils.isNetworkConnected(getActivity())) {
			// getDefaultProgressDialog(getString(R.string.loging), false);
			isRequesting = true;
			new Thread() {
				public void run() {
					Message msg = new Message();
					msg.what = Constant.MSG_FAILURE;
					msg.obj = getString(R.string.cannot_connection_server);
					try {
						Map map = new HashMap<>();
						map.put("deviceCode", deviceId);
						map.put("strYearMonth", time);
						String result = WebServiceClient.CallWebService(
								"GetDeviceMonthDetailPowerData", map);
						Log.d("resultLogin", result);
						if (result != null) {
							JSONObject obj = new JSONObject(result);
							int statusCode = obj.getInt("statusCode");
							if (statusCode == 0) {
								dataView(obj);
								msg.what = Constant.MSG_SUCCESS;
							} else {
								msg.obj = obj.getString("message");
								msg.what = Constant.MSG_FAILURE;
							}
						}

					} catch (Exception e) {
						e.printStackTrace();
						msg.obj = e;
						msg.what = Constant.MSG_EXCPTION;
					}
					if (isRequesting) {
						defalutHandler.sendMessage(msg);
					}
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
				// 年月日 20160923
				if (listMonth.size() != 0) {
					SingleDeviceMonth data = listMonth.get(0);
					String yymmdd = currentTime+data.getDate();
					Log.d("yymmddyymmddyymmddyymmdd", yymmdd);
					year = yymmdd.substring(0, 4);
					thismonth = yymmdd.substring(4, 6);
					today = yymmdd.substring(6, 8);
					// 初始化日
					initDay(year, thismonth, today);
					initX();
					//处理用电量
					initBarDatas(powerDatas);
					//处理功率
					initArea(powerRates);// 初始化区域图
				}

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
			}
		}

		;
	};

	private void toast(String text) {
		Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
	}

	// 数据处理
	private void dataView(JSONObject obj) {
		JSONArray array = null;
		try {
			array = obj.getJSONArray("content");
			listMonth.clear();
			powerDatas.clear();
			powerRates.clear();
			for (int i = 0; i < array.length(); i++) {
				JSONObject res = array.getJSONObject(i);
				SingleDeviceMonth data = new SingleDeviceMonth();
				data.setDate(res.getString("date"));
				data.setPowerData(res.getString("powerData"));
				data.setAvgPowerData(res.getString("avgPowerData"));
				listMonth.add(data);
				powerDatas.add(Double.parseDouble(data.getPowerData()));
				powerRates.add(Double.parseDouble(data.getAvgPowerData()));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}
}
