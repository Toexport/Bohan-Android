package cn.mioto.bohan.fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cn.mioto.bohan.Constant;
import cn.mioto.bohan.R;
import cn.mioto.bohan.utils.ExceptionUtil;
import cn.mioto.bohan.utils.LogUtilNIU;
import cn.mioto.bohan.utils.ModbusCalUtil;
import cn.mioto.bohan.utils.ToastUtils;
import cn.mioto.bohan.view.AreaChart01ViewMonth;
import cn.mioto.bohan.view.CustomBarChartViewMonth;
import cn.mioto.bohan.view.timepicker.DatePicker;
import cn.mioto.bohan.view.timepicker.DatePicker.OnYearMonthDayPickListener;
import cn.mioto.bohan.view.timepicker.DatePicker.OnYearPickListener;

import com.umeng.analytics.MobclickAgent;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/** 
 * 类说明：单个设备年数据
 */
public class AllDeviceYearDataFragment extends BaseAllDeviceDataFragment implements OnClickListener {
	//标题
	private TextView titlePower1;
	private TextView titlePower2;
	private TextView titleRate1;
	private TextView titleRate2;
	private View view;
	private ViewPager viewPager;
	/**********DECLARES*************/
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
	private CustomBarChartViewMonth bar11;
	private AreaChart01ViewMonth bar12;
	private CustomBarChartViewMonth bar21;
	private AreaChart01ViewMonth bar22;
	private Calendar calendar = Calendar.getInstance();
	private List<String> xLables = new ArrayList<>();
	private Handler handler = new Handler();
	//显示时间的地方
	private LinearLayout lllowcolor;
	//显示颜色的地方
	private LinearLayout lluptime;
	//现在历史资料后，显示的历史事件区域
	private LinearLayout llShowHistory;
	//历史事件显示
	private TextView tvShowSelectTime;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		this.inflater = inflater;
		view =inflater.inflate(R.layout.fragment_single_device_year_data, null);
		viewPager=(ViewPager) view.findViewById(R.id.viewPager);
		/***************************************************/
		/***************************************************/
		viewPagerAddContent();//初始化viewpager加载的view以及初始图表标题
		bindViews();
		initDay();//初始化日期的显示
		//		Intent intent = new Intent();
		//		intent.setAction(Constant.BROCAST_CHART_SHOW_DIALOG);//发广播显示加载中
		//		getContext().sendBroadcast(intent);
		checkDataAndUpdateCharts();
		llSelectTime.setOnClickListener(this);
		return view;
	}

	public void checkDataAndUpdateCharts() {
		initX();
		llShowHistory.setVisibility(View.GONE);
		lllowcolor.setVisibility(View.VISIBLE);
		lluptime.setVisibility(View.VISIBLE);
		checkData();
	}

	private void checkSDevicePowerAndRateFromUdp(final String deviceId) {
		LogUtilNIU.value("3发送UDP数据*******");
		udpok=false;
		String verCode=ModbusCalUtil.verNumber(deviceId+"000C0000");
		final String msg1="3A"+deviceId+"000C0000"+verCode +"0D";//0010 查询上24小时的用电量
		try {
			app.sendUDPMsg(msg1);
		} catch (IOException e) {
			ExceptionUtil.handleException(e);
		}
		LogUtilNIU.value("查询单个设备30天用电量-->"+msg1+"发送数据的时间为"+System.currentTimeMillis());
		/**********查询平均用电功率*****************************************/
		//日数据中途间隔1秒
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				String verCode2 =ModbusCalUtil.verNumber(deviceId+"000D0000");
				final String msg2="3A"+deviceId+"000D0000"+verCode2  +"0D";//0011 询上24小时的用电平均功率
				try {
					app.sendUDPMsg(msg2);
				} catch (IOException e) {
					ExceptionUtil.handleException(e);
				}
				LogUtilNIU.value("查询单个设备30天用电功率-->"+msg2+"发送数据的时间为"+System.currentTimeMillis());
			}
		}, 1000);
	}

	private void initDay() {
		tvYear.setText(calendar.get(Calendar.YEAR)+"");
		tvL1.setText(calendar.get(Calendar.YEAR)+""+getResources().getString(R.string.year1));
		/*
		 * 考虑两种情况
		 * 1月 okay
		 * 非1月 okay
		 */
		if(calendar.get(Calendar.MONTH)!=0){//只要不为1月份就需要显示2个时间
			tvYear2.setText(calendar.get(Calendar.YEAR)-1+"");
			tvL2.setText(calendar.get(Calendar.YEAR)-1+""+getResources().getString(R.string.year1));
			/***************************************************/
			//初始化图表的下标
		}else{//为一月的时候，显示上年全年，所以需要显示年份-1
			llLast.setVisibility(View.GONE);//年份标题隐藏
			llLable.setVisibility(View.GONE);//小颜色标签隐藏
			tvYear.setText(calendar.get(Calendar.YEAR)-1+"");
			tvL1.setText(calendar.get(Calendar.YEAR)-1+""+getResources().getString(R.string.year1));
			//由于标签默认显示1-12所以不用另外设置了。
		}
	}

	@Override
	protected void initBarDatas(List<Double> datas) {
		//数据改变柱子颜色和设置数据
		Double max = ModbusCalUtil.countBiggestShowItsUpper(datas);
		bar11.setBarColorAndData(xLables.subList(0, 6),datas.subList(0, 6),max);
		bar21.setBarColorAndData(xLables.subList(6, 12),datas.subList(6, 12),max);
	}

	@Override
	protected void initArea(List<Double> datas) {
		//全部数据都乘以1000
		//把上面存入的连贯数据作处理，变成前后数据都是0的折线图形式
		List<Double> lineDatas = new ArrayList<>();
		for(int i = 0 ; i < 17 ; i ++){
			if(i==0||i%8==0){
				lineDatas.add(0d);//0位或被8整除的位，都加“”
			}else if (i==7||(i-7)%8==0){
				lineDatas.add(0d);
			}else if(i<7){
				lineDatas.add(datas.get(i-1)*1000);
			}else if(i<15){
				lineDatas.add(datas.get(i-3)*1000);
			}
		}
		Double max = ModbusCalUtil.countBiggestShowItsUpper(lineDatas);
		bar12.setBarColorAndData(lineDatas.subList(0,6),max);
		bar22.setBarColorAndData(lineDatas.subList(6,12),max);
	}

	// ------------------------------------------------------------------------------
	private void initX() {
		//初始化集合
		int m0=calendar.get(Calendar.MONTH)+1;//当前月份
		for(int i = 0 ; i < 12 ; i ++){//用一个循环，把所有月份初始到xLables这个集合里
			if((m0-i)==0){//当月份等于0
				m0=12+i;
			}
			xLables.add(m0-i+"");//每一个月加到集合里
		}
		//初始化折线图横坐标标签
		for(int i = 0 ; i < 17 ; i ++){//用一个循环，把所有月份初始到xLables这个集合里
			if(i==0||i%8==0){
				xLablesLineChart.add("");//0位或被8整除的位，都加“”
			}else if (i==7||(i-7)%8==0){
				xLablesLineChart.add("");
			}else if(i<7){
				xLablesLineChart.add(xLables.get(i-1));
			}else if(i<15){
				xLablesLineChart.add(xLables.get(i-3));
			}
		}
		//初始化标签
		bar11.setXlables(xLables.subList(0, 6));
		bar12.setXLables(xLablesLineChart.subList(0, 8));
		bar21.setXlables(xLables.subList(6, 12));
		bar22.setXLables(xLablesLineChart.subList(8, 16));
	}

	private void viewPagerAddContent() {
		view1=(LinearLayout) inflater.inflate(R.layout.view_all_device_year_data, null);
		view2=(LinearLayout) inflater.inflate(R.layout.view_all_device_year_data2, null);
		views.add(view1);
		views.add(view2);
		titlePower1=(TextView) view1.findViewById(R.id.titlePower);
		titlePower2=(TextView) view2.findViewById(R.id.titlePower);
		titleRate1=(TextView) view1.findViewById(R.id.titleRate);
		titleRate2=(TextView) view2.findViewById(R.id.titleRate);
		viewPager.setAdapter(new DataAdapter());
	}

	/***************************************************/
	private class DataAdapter extends PagerAdapter{

		@Override
		public int getCount() {
			return views.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return  view == object;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			container.addView(views.get(position));
			return views.get(position);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object)   {
			container.removeView(views.get(position));//删除页卡
		}
	}

	private void bindViews() {
		llSelectTime = (LinearLayout) view.findViewById(R.id.llSelectTime);
		tvYear = (TextView)  view.findViewById(R.id.tvYear);
		llLast = (LinearLayout)  view.findViewById(R.id.llLast);
		tvYear2 = (TextView)  view.findViewById(R.id.tvYear2);
		tvL1 = (TextView)  view.findViewById(R.id.tvL1);
		llLable = (LinearLayout)  view.findViewById(R.id.llLable);
		tvL2 = (TextView)  view.findViewById(R.id.tvL2);
		bar11 = (CustomBarChartViewMonth) view1.findViewById(R.id.bar11);
		bar12 = (AreaChart01ViewMonth) view1.findViewById(R.id.bar12);
		bar21 = (CustomBarChartViewMonth) view2.findViewById(R.id.bar21);
		bar22 = (AreaChart01ViewMonth) view2.findViewById(R.id.bar22);
		llShowHistory = (LinearLayout) view.findViewById(R.id.llShowHistory);
		tvShowSelectTime = (TextView) view.findViewById(R.id.tvShowSelectTime);
		lllowcolor = (LinearLayout) view.findViewById(R.id.lllowcolor);
		lluptime = (LinearLayout) view.findViewById(R.id.lluptime);
		setTitles();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v==llSelectTime){
			//出现年选择器
			DatePicker picker = new DatePicker(getActivity(), DatePicker.YEAR);
			picker.setRange(2015, 2050);
			picker.setSelectedItem(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
			picker.setOnDatePickListener(new OnYearPickListener() {

				@Override
				public void onDatePicked(String year) {
					LogUtilNIU.value(year);
					if(isFuture(Integer.valueOf(year))){
						ToastUtils.shortToast(getContext(), "请选择较前的年份");
					}else{

						tvShowSelectTime.setText(year);
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
		int now=Integer.valueOf(calendar.get(Calendar.YEAR));
		LogUtilNIU.value("value"+value+"now"+now);
		if(now<=value){
			return true;
		}else{
			return false;
		}
	}

	@Override
	public void checkData() {
		LogUtilNIU.value("全部设备月数据查询");
		checkOnlineDevicesDatasAndShow(8,9);//发UDP货服务器数据查询设备信息
	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart(getClass().getName()); //统计页面，"MainScreen"为页面名称，可自定义
	}
	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd(getClass().getName()); 
	}

	@Override
	protected String showDeviceNumber(int quantity) {
		onlineDeviceNumbers = quantity;
		setTitles();
		return String.valueOf(quantity);
	}

	private void setTitles() {
		titlePower1.setText(onlineDeviceNumbers+getResources().getString(R.string.all_device_last_6_months));
		titlePower2.setText(onlineDeviceNumbers+getResources().getString(R.string.all_device_last_712_months));
		titleRate1.setText(onlineDeviceNumbers+getResources().getString(R.string.all_device_last_6_months_power));
		titleRate2.setText(onlineDeviceNumbers+getResources().getString(R.string.all_device_last_7_12_days_power));
	}


}
