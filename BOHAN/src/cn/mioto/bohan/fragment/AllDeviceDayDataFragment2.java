package cn.mioto.bohan.fragment;

import java.io.IOException;
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
import cn.mioto.bohan.activity.LoginActivity;
import cn.mioto.bohan.entity.SingleDeviceMonth;
import cn.mioto.bohan.exception.ExceptionManager;
import cn.mioto.bohan.utils.Enterface;
import cn.mioto.bohan.utils.ExceptionUtil;
import cn.mioto.bohan.utils.HttpUtils;
import cn.mioto.bohan.utils.LogUtilNIU;
import cn.mioto.bohan.utils.ModbusCalUtil;
import cn.mioto.bohan.utils.NetworkUtils;
import cn.mioto.bohan.utils.ToastUtils;
import cn.mioto.bohan.view.AllCustomBarChartViewHour;
import cn.mioto.bohan.view.AreaChart01ViewHour;
import cn.mioto.bohan.view.CustomBarChartViewHour;
import cn.mioto.bohan.view.timepicker.DatePicker;
import cn.mioto.bohan.view.timepicker.DatePicker.OnYearMonthDayPickListener;

import com.mioto.bohan.webservice.WebServiceClient;
import com.umeng.analytics.MobclickAgent;

import android.app.ProgressDialog;
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
 * 类说明：所有设备日数据
 * 
 * 此为日数据最外层fragment
 */
public class AllDeviceDayDataFragment2 extends BaseAllDeviceDataFragment
		implements OnClickListener {
	private View view;
	private LayoutInflater inflater;
	private ViewPager viewPager;
	private LinearLayout view1;
	private LinearLayout view2;
	private LinearLayout view3;
	private LinearLayout view4;
	private LinearLayout llYear2;
	private LinearLayout llSelectTime;// 选择时间的点击区域
	private List<LinearLayout> views = new ArrayList<>();
	private Calendar c = Calendar.getInstance();
	private String year;
	private String month;
	private String today;
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
	private Handler handler = new Handler();
	/***************************************************/
	private cn.mioto.bohan.view.AllCustomBarChartViewHour bar11;
	private cn.mioto.bohan.view.AreaChart01ViewHour bar12;
	private cn.mioto.bohan.view.CustomBarChartViewHour bar21;
	private cn.mioto.bohan.view.AreaChart01ViewHour bar22;
	private cn.mioto.bohan.view.CustomBarChartViewHour bar31;
	private cn.mioto.bohan.view.AreaChart01ViewHour bar32;
	private cn.mioto.bohan.view.CustomBarChartViewHour bar41;
	private cn.mioto.bohan.view.AreaChart01ViewHour bar42;
	/***************************************************/
	// 标题
	private TextView titlePower1;
	private TextView titlePower2;
	private TextView titlePower3;
	private TextView titlePower4;
	private TextView titleRate1;
	private TextView titleRate2;
	private TextView titleRate3;
	private TextView titleRate4;
	private DatePicker picker;
	/***************************************************/
	private List<String> xLables = new ArrayList<>();
	private List<String> colors = new ArrayList<>();
	private Boolean showOneDay = false;
	/***************************************************/
	// 现在历史资料后，显示的历史事件区域
	private LinearLayout llShowHistory;
	// 历史事件显示
	private TextView tvShowSelectTime;
	// 下面的颜色区域
	private LinearLayout lllowcolor;
	// 上面的时间区域
	private LinearLayout lluptime;
	private Calendar calendar = Calendar.getInstance();
	private Handler h = new Handler() {
		public void handleMessage(android.os.Message msg) {

		};
	};

	private List<SingleDeviceMonth> list = new ArrayList<>();
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
	private long currentTime;// 当前系统时间
	private ProgressDialog p;
	
	private static final int TOKEN_TIME_OUT = 12,TOKEN_TIME_OUT_NO_DIALOG =13;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (view == null) {
			view = inflater.inflate(R.layout.fragment_single_device_day_data,
					null);
			this.inflater = inflater;
			/***************************************************/
			viewPager = (ViewPager) view.findViewById(R.id.viewPager);
			/***************************************************/
			bindViews();
			viewPagerAddContent();// 初始化viewpager加载的view以及初始图表标题
			initDay();// 初始化日期的显示
			/***************************************************/
			checkDataAndUpdateCharts();// 查询数据
			llSelectTime.setOnClickListener(this);
			picker = new DatePicker(getActivity());// 创建时间选择器对象
		}
		// 缓存的rootView需要判断是否已经被加过parent，如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
		ViewGroup parent = (ViewGroup) view.getParent();
		if (parent != null) {
			parent.removeView(view);
		}
		return view;
	}

	public void checkDataAndUpdateCharts() {
		initX();
		llShowHistory.setVisibility(View.GONE);
//		lllowcolor.setVisibility(View.VISIBLE);
		lluptime.setVisibility(View.VISIBLE);
		checkData();// 下载数据
	}

	// ------------------------------------------------------------------------------
	// 初始化view部分
	private void initDay() {
		year = c.get(Calendar.YEAR) + "";
		month = (c.get(Calendar.MONTH) + 1) + "";
		today = c.get(Calendar.DAY_OF_MONTH) + "";
		LogUtilNIU.value("年" + year + "月" + month + "日" + today);
		tvYear.setText(year);
		tvMonth.setText(month);
		tvDate.setText(today);
		LogUtilNIU.value("小时数是" + (c.get(Calendar.HOUR_OF_DAY)));
		// tvHour.setText(calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE));//显示系统当前的时间,24小时制
		if (c.get(Calendar.HOUR_OF_DAY) != 0) {
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
					int dateOfMonth = c.getActualMaximum(Calendar.DATE);
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
				tvDate2.setText(c.get(Calendar.DAY_OF_MONTH) - 1 + "");
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
		// 初始化集合
		// int m0 = c.get(Calendar.HOUR_OF_DAY) + 1;
		int m0 = 0;
		LogUtilNIU.value("现在的点数为" + m0);
		/*
		 * 特别说明，原集合只要24和元素 但是因为sublist方法不能截取到最后，所以List要加多1个元素
		 */
		for (int i = 0; i < 24; i++) {// 用一个循环，把所有月份初始到xLables这个集合里
			if ((m0 - i) == 0) {
				// 如果当时间为0点
				// 则从24从新开始计算
				m0 = 24 + i;
			}
			// 刚好是时间跨越一天的时候 
			if (!((m0 - i) == 1)) {
				xLables.add((m0 - i - 1) + "");
			} else {
				xLables.add("24");
			}
		}

		String color = "light";
		for (int i = 0; i < xLables.size(); i++) {
			if (i != 0) {
//				if (xLables.get(i).equals("24")) {// 浅色。这个脑残的做法，用来区别柱子颜色。
//					if (!showOneDay) {
//						color = "dark";
//					}
//				}
			}
			colors.add(color);
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

		bar11.setXlables(xLables);
	}

	private void viewPagerAddContent() {
		view1 = (LinearLayout) inflater.inflate(
				R.layout.view_all_device_day_data, null);
		views.add(view1);
		viewPager.setAdapter(new DataAdapter());
		/***************************************************/
		// 标题
		titlePower1 = (TextView) view1.findViewById(R.id.titlePower);
		/***************************************************/
		// 设置柱状图标题
		setTitles();
		/***************************************************/
		// 第一个view里的控件
		bar11 = (AllCustomBarChartViewHour) view1.findViewById(R.id.bar1);
	}

	private void setTitles() {
		// 设置柱状图标题
		titlePower1.setText(getResources().getString(
				R.string.all_device_last_24_hours));
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

	@Override
	protected void initBarDatas(List<Double> datas) {
		// 数据改变柱子颜色和设置数据
		Double max = ModbusCalUtil.countBiggestShowItsUpper(datas);
		bar11.setBarColorAndData(colors, datas, max, list);
	}

	@Override
	protected void initArea(List<Double> datas) {
		// 全部数据都乘以1000
		// 把上面存入的连贯数据作处理，变成前后数据都是0的折线图形式
		List<Double> lineDatas = new ArrayList<>();
		// TODO
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

	@Override
	public void onClick(View v) {
		if (v == llSelectTime) {
			// 出现年月日时间选择器
			picker.setRange(2016, Integer.valueOf(year));
			Calendar calendar = Calendar.getInstance();
			picker.setSelectedItem(calendar.get(Calendar.YEAR),
					calendar.get(Calendar.MONTH) + 1,
					calendar.get(Calendar.DAY_OF_MONTH));
			picker.setOnDatePickListener(new OnYearMonthDayPickListener() {

				@Override
				public void onDatePicked(String year, String month, String day) {
					// TODO 选择了时间后，把时间发送到服务器
					LogUtilNIU.value(year + month + day);
					LogUtilNIU.value(isFuture(Integer.valueOf(year + month
							+ day)));
					// if (isFuture(Integer.valueOf(year + month + day))) {
					// ToastUtils.shortToast(getContext(), "请选择昨天或以前的日期");
					// } else {
					// TODO
					// List<String> allID = new ArrayList<>();//该用户下所有设备的id
					// checkAllDeviceHisDataFromeService(allID
					// ,"year_month_day",year+month+day);
					getData(year + month + day + "");
					tvShowSelectTime.setText(year + "." + month + "." + day);
					llShowHistory.setVisibility(View.VISIBLE);
					lllowcolor.setVisibility(View.GONE);
					lluptime.setVisibility(View.GONE);
					// }
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
	public void onDestroy() {
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

	/**
	 * 查询数据的方法
	 */
	@Override
	public void checkData() {
		LogUtilNIU.value("全部设备日数据查询");
		// 发UDP货服务器数据查询设备信息
		// 下面的查询方法已经带dialog显示
		// checkOnlineDevicesDatasAndShow(4,5);
		currentTime = System.currentTimeMillis();
		getData(dateFormat.format(currentTime));
	}

	@Override
	protected String showDeviceNumber(int quantity) {
		onlineDeviceNumbers = quantity;
		setTitles();
		return String.valueOf(quantity);
	}

	/**
	 * 从服务器获取数据
	 */
	private void getData(final String strYearMonthDay) {
		showDialog();
		if (NetworkUtils.isNetworkConnected(getActivity())) {
			new Thread() {
				public void run() {
					Message msg = new Message();
					msg.what = Constant.MSG_FAILURE;
					msg.obj = getString(R.string.cannot_connection_server);
					try {
						HttpUtils.TOKIN = "";
						Map map = new HashMap();
						map.put("strYearMonthDay", strYearMonthDay);
						String result = WebServiceClient.CallWebService(
								"GetUserDayTotalPowerData", map,getActivity());
						Log.d("resultGetUserDayTotalPowerData", result);
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
										data.setAvgPowerIsNull("1");
									} else {
										data.setAvgPowerData(object
												.getString("avgPowerData"));
										data.setAvgPowerIsNull("0");
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
										data.setPowerIsNull("1");
									} else {
										data.setPowerData(object
												.getString("powerData"));
										data.setPowerIsNull("0");
									}
									list.add(data);
								}
								msg.what = Constant.MSG_SUCCESS;
							} else if(statusCode == 2){
								msg.what = TOKEN_TIME_OUT;
							}else {
								Bundle b = new Bundle();
								b.putString("date", strYearMonthDay);
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
			if (p != null) {
				p.dismiss();
				p = null;
			}
			if(!isAdded()){
				return;
			}
			switch (msg.what) {
			case Constant.MSG_SUCCESS:
				dataView();
				break;
			case Constant.MSG_FAILURE:
				// 没有数据20170215
				initDay();
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
			case TOKEN_TIME_OUT:
				toast(getString(R.string.login_expired));
				BApplication.instance.clearThisUserFlashDatasOfApplication();// 清空application此用户的共用数据
				getActivity().finish();
				Intent intent = new Intent(getActivity().getApplicationContext(), LoginActivity.class);
				startActivity(intent);
				break;
			}
		};
	};

	private void toast(String text) {
		Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
	}

	// 数据处理
	private void dataView() {
		if (list.size() > 0) {
			List<Double> datas = new ArrayList<>();
			for (int i = 0; i < list.size(); i++) {
				SingleDeviceMonth data = list.get(i);
				datas.add(Double.parseDouble(data.getPowerData()));
			}
			initBarDatas(datas);
		}
	}

	private void showDialog() {
		// 显示正在查询progress
		p = new ProgressDialog(getActivity());
		p.setMessage(getResources().getString(R.string.loading_popwer_data));
		p.setCanceledOnTouchOutside(false);
		p.show();
	}
}
