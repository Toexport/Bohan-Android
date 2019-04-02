package cn.mioto.bohan.fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cn.mioto.bohan.Constant;
import cn.mioto.bohan.R;
import cn.mioto.bohan.utils.LogUtilNIU;
import cn.mioto.bohan.utils.ModbusCalUtil;
import cn.mioto.bohan.utils.ToastUtils;
import cn.mioto.bohan.view.AreaChart01ViewDay;
import cn.mioto.bohan.view.CustomBarChartViewDay;
import cn.mioto.bohan.view.timepicker.DatePicker;

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
 * 类说明：所有设备月数据
 */
public class AllDeviceMonthDataFragment extends BaseAllDeviceDataFragment implements OnClickListener {

	private View view;
	private ViewPager viewPager;
	/**********DECLARES*************/
	private TextView tvYear;
	private TextView tvMonth;
	private TextView tvMonth2;
	private TextView tvMonth3;
	private TextView tvYear2;
	private LinearLayout llLast;
	private LinearLayout llLastLast;
	private TextView tvL1;
	private TextView tvL2;
	private TextView tvL3;
	private LinearLayout llLable0;
	private LinearLayout llLable;
	private LinearLayout llLable2;
	private LinearLayout view1;
	private LinearLayout view2;
	private LinearLayout view3;
	private LinearLayout view4;
	private LinearLayout view5;
	private LinearLayout llYear2;
	private LinearLayout llSelectTime;//选择时间的点击区域
	private LayoutInflater inflater;
	private List<LinearLayout> views = new ArrayList<>();
	private String year="", thismonth="",today = "",showingMonth="",lastMonth="";
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
	private TextView titlePower1;
	private TextView titlePower2;
	private TextView titlePower3;
	private TextView titlePower4;
	private TextView titlePower5;
	private TextView titleRate1;
	private TextView titleRate2;
	private TextView titleRate3;
	private TextView titleRate4;
	private TextView titleRate5;
	private Handler handler = new Handler();
	private List<String> colors = new ArrayList<>();
	private LinearLayout lllowcolor;
	private LinearLayout lluptime;
	//现在历史资料后，显示的历史事件区域
	private LinearLayout llShowHistory;
	//历史事件显示
	private TextView tvShowSelectTime;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		this.inflater = inflater;
		view =inflater.inflate(R.layout.fragment_single_device_month_data, null);
		viewPager=(ViewPager) view.findViewById(R.id.viewPager);
		/***************************************************/
		viewPagerAddContent();//初始化viewpager加载的view以及初始图表标题
		bindViews();
		initDay();//初始化日期的显示
//		Intent intent = new Intent();
//		intent.setAction(Constant.BROCAST_CHART_SHOW_DIALOG);//发广播显示加载中
//		getContext().sendBroadcast(intent);
		checkDataAndUpdateCharts();//查询数据
		llSelectTime.setOnClickListener(this);
		return view;
	}
	public void checkDataAndUpdateCharts() {
		initX();
		llShowHistory.setVisibility(View.GONE);
		lllowcolor.setVisibility(View.VISIBLE);
		lluptime.setVisibility(View.VISIBLE);
		checkData();//下载数据
	}
	// ------------------------------------------------------------------------------
	//初始化图表
	private void initX() {
		/***************************************************/
		//暂时不考虑颜色，只考虑下标正确显示
		//获得下标数据
		List<String> xLables  = new ArrayList<>();
		int beginDate=calendar.get(Calendar.DAY_OF_MONTH);//以昨天为起点
		showingMonth=thismonth;//这个月为正在计量显示的月
		String showingColor = "light";
		for(int i =0 ; i < 31; i ++){
			String date = beginDate-i +"";//date为显示的下标
			if(date.equals("0")){//如果为0，则设置为上个月的最后一天，也就算1号之后一天
				//得到上个月总天数
				calendar.set(Calendar.YEAR,Integer.valueOf(year));
				calendar.set(Calendar.MONTH, Integer.valueOf(showingMonth)-1-1);//上个月。减去1为今月，减去2为上个月
				int dateOfMonth = calendar.getActualMaximum(Calendar.DATE);
				showingMonth=Integer.valueOf(showingMonth)-1+"";//正在显示的月-1
				date=dateOfMonth+"";//所要设置的日期由0变为上个月最后1天
				beginDate=Integer.valueOf(dateOfMonth)+i;//又从最后一天开始算起
				if(i!=0){//如果不是第一个月份
					if((Integer.valueOf(showingMonth)+2)==Integer.valueOf(thismonth)){//如果横跨三个月
						if(showingColor.equals("dark")){
							showingColor="3rdColor";//第三种颜色
						}else if(showingColor.equals("light")){
							showingColor="dark";//第三种颜色
						}
						LogUtilNIU.value("显示第三种颜色");
						//设置第三种标签和标题
						//						if(i!=30){//如果是最后一个，就不要显示了
						//							llLable2.setVisibility(view.VISIBLE);
						//						}
						//						tvL3.setText(showingMonth+getResources().getString(R.string.month1));
						//						llLastLast.setVisibility(view.VISIBLE);
						//						tvMonth3.setText(showingMonth);
					}else{
						showingColor="dark";//深色//第二个月份
					}
				}
			}
			colors.add(showingColor);
			xLables.add(date);
		}
		//初始化折线图横坐标标签
		for(int i = 0 ; i < 41 ; i ++){//用一个循环，把所有月份初始到xLables这个集合里
			if(i==0||i%8==0){
				xLablesLineChart.add("");//0位或被8整除的位，都加“”
			}else if (i==7||(i-7)%8==0){
				xLablesLineChart.add("");
			}else if(i<7){
				xLablesLineChart.add(xLables.get(i-1));
			}else if(i<15){
				xLablesLineChart.add(xLables.get(i-3));
			}else if(i<23){
				xLablesLineChart.add(xLables.get(i-5));
			}else if(i<31){
				xLablesLineChart.add(xLables.get(i-7));
			}else if(i<39){
				xLablesLineChart.add(xLables.get(i-9));
			}
		}

		LogUtilNIU.value("月数据下标生成xLables"+xLables);
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

	private void initDay() {
		year=calendar.get(Calendar.YEAR)+"";
		thismonth=calendar.get(Calendar.MONTH) + 1+"";
		lastMonth=calendar.get(Calendar.MONTH) + 1-1+"";
		today=calendar.get(Calendar.DAY_OF_MONTH)+"";
		/*
		 * 月数据要考虑的情况
		 * 
		 * 当前月为1月
		 *    1月1日，显示一个月份，上年12月 okay
		 *    1月N日 okay
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
		 * 
		 */
		if(Integer.valueOf(thismonth)==1){//如果当前月份为1月，则上个月要显示为12月还要显示年
			if(Integer.valueOf(thismonth)==1&&!today.equals("1")){//1月N日
				LogUtilNIU.value("1月N日");
				tvYear.setText(year);//年
				tvMonth.setText(thismonth);//今个月需要加1，因为Java月份是从0开始的
				llYear2.setVisibility(View.VISIBLE);
				tvYear2.setText(Integer.valueOf(year)-1+"");//年份显示为上一年
				tvMonth2.setText("12");//月份设置为12月
				tvL1.setText(year+getResources().getString(R.string.year1)+thismonth+getResources().getString(R.string.month1));//这个月
				tvL2.setText((Integer.valueOf(year)-1)+getResources().getString(R.string.year1)+"12"+getResources().getString(R.string.month1));//上个月
			}else if(Integer.valueOf(thismonth)==1&&today.equals("1")){//1月1日
				LogUtilNIU.value("1月1日");
				llLast.setVisibility(View.GONE);//第二个年份隐藏
				tvYear.setText(Integer.valueOf(year)-1+"");//显示上一年
				tvMonth.setText("12");//显示为12月
				llLable.setVisibility(View.GONE);
				tvL1.setText(Integer.valueOf(year)-1+getResources().getString(R.string.year1)+"12"+getResources().getString(R.string.month1));
			}
		}else {//不为1月
			if(thismonth.equals("3")&&Integer.valueOf(today)==2){//3月2日
				//非闰年3月1日
				//得到上个月总天数
				calendar.set(Calendar.YEAR,Integer.valueOf(year));
				calendar.set(Calendar.MONTH, Integer.valueOf(thismonth)-1-1);//上个月。减去1为今月，减去2为上个月
				int dateOfMonth = calendar.getActualMaximum(Calendar.DATE);
				if(dateOfMonth==28){//上个月总日数为28日，非闰年
					LogUtilNIU.value("非闰年3月2日");//显示3种颜色
					tvYear.setText(year);//年
					tvMonth.setText(thismonth);//上个月
					tvMonth2.setText(Integer.valueOf(thismonth)-1+"");//上上个月
					tvL1.setText(thismonth+getResources().getString(R.string.month1));
					tvL2.setText(lastMonth+getResources().getString(R.string.month1));
					llLable2.setVisibility(View.VISIBLE);
					llLastLast.setVisibility(View.VISIBLE);
					tvMonth3.setText(Integer.valueOf(lastMonth)-1+"");
					tvL3.setText((Integer.valueOf(lastMonth)-1)+getResources().getString(R.string.month1));
				}else if(dateOfMonth==29){//闰年3月2日，显示2种颜色
					LogUtilNIU.value("闰年3月1日");
					tvYear.setText(year);//年
					tvMonth.setText(lastMonth);//上个月
					tvMonth2.setText(Integer.valueOf(lastMonth)-1+"");//上上个月
					tvL1.setText(lastMonth+getResources().getString(R.string.month1));
					tvL2.setText((Integer.valueOf(lastMonth)-1)+getResources().getString(R.string.month1));
					llLable2.setVisibility(View.GONE);
				}
				//如果为1号，则标题不显示这个月，显示上个月和上个月(不是1月1日的情况)
			}else if(Integer.valueOf(today)==1){//N月1日
				LogUtilNIU.value("N月1日");
				tvYear.setText(year);//年
				tvMonth.setText(lastMonth);//上个月
				tvL1.setText(lastMonth+getResources().getString(R.string.month1));
				calendar.set(Calendar.YEAR,Integer.valueOf(year));
				calendar.set(Calendar.MONTH, Integer.valueOf(thismonth)-1-1);//上个月。减去1为今月，减去2为上个月
				int dateOfMonth = calendar.getActualMaximum(Calendar.DATE);
				if(dateOfMonth>=30){//月份天数大于30天
					llLable.setVisibility(View.GONE);
					llLast.setVisibility(View.GONE);
				}else{
					tvMonth2.setText(Integer.valueOf(lastMonth)-1+"");//上上个月
					tvL2.setText((Integer.valueOf(lastMonth)-1)+getResources().getString(R.string.month1));
				}
			}
			else {
				LogUtilNIU.value("N月N日");
				tvYear.setText(year);//年
				tvMonth.setText(thismonth);//今个月需要加1，因为Java月份是从0开始的
				tvMonth2.setText(lastMonth+"");//上个月
				tvL1.setText(thismonth+getResources().getString(R.string.month1));//这个月
				tvL2.setText(lastMonth+getResources().getString(R.string.month1));//上个月
			}
		}
		LogUtilNIU.value("calendar.getActualMaximum(Calendar.DATE)"+calendar.getActualMaximum(Calendar.DATE));
	}

	private void viewPagerAddContent() {
		view1=(LinearLayout) inflater.inflate(R.layout.view_all_device_month_data, null);
		view2=(LinearLayout) inflater.inflate(R.layout.view_all_device_month_data2, null);
		view3=(LinearLayout) inflater.inflate(R.layout.view_all_device_month_data3, null);
		view4=(LinearLayout) inflater.inflate(R.layout.view_all_device_month_data4, null);
		view5=(LinearLayout) inflater.inflate(R.layout.view_all_device_month_data5, null);
		views.add(view1);
		views.add(view2);
		views.add(view3);
		views.add(view4);
		views.add(view5);
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
		tvYear = (TextView) view.findViewById(R.id.tvYear);
		tvMonth = (TextView) view.findViewById(R.id.tvMonth);
		tvYear2 = (TextView) view.findViewById(R.id.tvYear2);
		llLast = (LinearLayout) view.findViewById(R.id.llLast);
		llLastLast = (LinearLayout) view.findViewById(R.id.llLastLast);
		tvMonth2 = (TextView) view.findViewById(R.id.tvMonth2);
		tvMonth3 = (TextView) view.findViewById(R.id.tvMonth3);
		tvL1 = (TextView)view.findViewById(R.id.tvL1);
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
		/***************************************************/
		//标题
		titlePower1=(TextView) view1.findViewById(R.id.titlePower);
		titlePower2=(TextView) view2.findViewById(R.id.titlePower);
		titlePower3=(TextView) view3.findViewById(R.id.titlePower);
		titlePower4=(TextView) view4.findViewById(R.id.titlePower);
		titlePower5=(TextView) view5.findViewById(R.id.titlePower);
		titleRate1=(TextView) view1.findViewById(R.id.titleRate);
		titleRate2=(TextView) view2.findViewById(R.id.titleRate);
		titleRate3=(TextView) view3.findViewById(R.id.titleRate);
		titleRate4=(TextView) view4.findViewById(R.id.titleRate);
		titleRate5=(TextView) view5.findViewById(R.id.titleRate);
		/***************************************************/
		llShowHistory = (LinearLayout)view.findViewById(R.id.llShowHistory);
		tvShowSelectTime = (TextView)view.findViewById(R.id.tvShowSelectTime);
		lllowcolor = (LinearLayout) view.findViewById(R.id.lllowcolor);
		lluptime = (LinearLayout) view.findViewById(R.id.lluptime);
		setTitles();
	
	}

	private void setTitles() {
		titlePower1.setText(onlineDeviceNumbers+getResources().getString(R.string.all_device_last_6_days));
		titleRate1.setText(onlineDeviceNumbers+getResources().getString(R.string.all_device_last_6_days_power));
		titlePower2.setText(onlineDeviceNumbers+getResources().getString(R.string.all_device_last_7_12_days));
		titleRate2.setText(onlineDeviceNumbers+getResources().getString(R.string.all_device_last_7_12_days_power));
		titlePower3.setText(onlineDeviceNumbers+getResources().getString(R.string.all_device_last_13_18_days));
		titleRate3.setText(onlineDeviceNumbers+getResources().getString(R.string.all_device_last_13_18_days_power));
		titlePower4.setText(onlineDeviceNumbers+getResources().getString(R.string.all_device_last_19_24_days));
		titleRate4.setText(onlineDeviceNumbers+getResources().getString(R.string.all_device_last_19_24_days_power));
		titlePower5.setText(onlineDeviceNumbers+getResources().getString(R.string.all_device_last_25_30_days));
		titleRate5.setText(onlineDeviceNumbers+getResources().getString(R.string.all_device_last_25_30_days_power));
	}
	
	protected void initBarDatas(List<Double> datas) {
		//数据改变柱子颜色和设置数据
		Double max = ModbusCalUtil.countBiggestShowItsUpper(datas);
		bar11.setBarColorAndData(colors.subList(0, 6),datas.subList(0, 6),max);
		bar21.setBarColorAndData(colors.subList(7, 12),datas.subList(7, 12),max);
		bar31.setBarColorAndData(colors.subList(13, 18),datas.subList(13, 18),max);
		bar41.setBarColorAndData(colors.subList(19, 24),datas.subList(19, 24),max);
		bar51.setBarColorAndData(colors.subList(25, 30),datas.subList(25, 30),max);
	}

	protected void initArea(List<Double> datas) {
		//全部数据都乘以1000
		//把上面存入的连贯数据作处理，变成前后数据都是0的折线图形式
		List<Double> lineDatas = new ArrayList<>();
		// TODO
		for(int i = 0 ; i < 41 ; i ++){
			if(i==0||i%8==0){
				lineDatas.add(0d);//0位或被8整除的位，都加“”
			}else if (i==7||(i-7)%8==0){
				lineDatas.add(0d);
			}else if(i<7){
				lineDatas.add(datas.get(i-1)*1000);
			}else if(i<15){
				lineDatas.add(datas.get(i-3)*1000);
			}else if(i<23){
				lineDatas.add(datas.get(i-5)*1000);
			}else if(i<31){
				lineDatas.add(datas.get(i-7)*1000);
			}else if(i<39){
				lineDatas.add(datas.get(i-9)*1000);
			}
		}
		Double max = ModbusCalUtil.countBiggestShowItsUpper(lineDatas);
		bar12.setBarColorAndData(lineDatas.subList(0, 8),max);
		bar22.setBarColorAndData(lineDatas.subList(8, 16),max);
		bar32.setBarColorAndData(lineDatas.subList(16, 24),max);
		bar42.setBarColorAndData(lineDatas.subList(24, 32),max);
		bar52.setBarColorAndData(lineDatas.subList(32, 40),max);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
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
	public void onClick(View v) {
		if(v==llSelectTime){
			//出现年月选择器
			DatePicker picker = new DatePicker(getActivity(), DatePicker.YEAR_MONTH);
			picker.setRange(2016, 2050);
			picker.setOnDatePickListener(new DatePicker.OnYearMonthPickListener() {
				@Override
				public void onDatePicked(String year, String month) {
					LogUtilNIU.value(year+month);
					//显示历史事件的区域出现
					if(isFuture(Integer.valueOf(year+month))){
						ToastUtils.shortToast(getContext(), "请选择之前的月份");
					}else{
						tvShowSelectTime.setText(year+"."+month);
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
		int now=Integer.valueOf(year+ModbusCalUtil.add0fillLength(thismonth, 2));
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
		checkOnlineDevicesDatasAndShow(6,7);//发UDP货服务器数据查询设备信息
	}
	
	@Override
	protected String showDeviceNumber(int quantity) {
		onlineDeviceNumbers = quantity;
		setTitles();
		return String.valueOf(quantity);
	}

}
