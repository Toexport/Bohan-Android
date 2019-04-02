package cn.mioto.bohan.fragment;

import java.util.ArrayList;
import java.util.List;

import cn.mioto.bohan.R;
import cn.mioto.bohan.utils.LogUtilNIU;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 类说明：设备所有用电数据查询
 */
public class MainInfoFragment3 extends BaseFragment implements OnClickListener {
	/********** DECLARES *************/
	private List<TextView> tvList = new ArrayList<TextView>();
	private LinearLayout tag_ll;
	private TextView tvDayData;
	private TextView tvMonthData;
	private TextView tvYearData;
	private ImageView ivCursor;
	int currentFragmentIndex = 0;
	private int clickIndex = 0;

	public int getClickIndex() {
		return clickIndex;
	}

	private TextView toolbarTitle;
	private TextView toolbarMenu;
	private TextView menu_front;

	int offset; // 下标的偏移量
	int currIndex = 0;// 当前页卡编号
	int ivWide;// 下标宽度
	int linerLayoutW;
	Handler handler = new Handler();
	View view;
	// 判断用户名下有没有设备

	FragmentTabHost tabHost;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_main_info3, null);
		/********** 初始化控件 *************/
		tag_ll = (LinearLayout) view.findViewById(R.id.tag_ll);
		tvDayData = (TextView) view.findViewById(R.id.tvDayData);
		tvMonthData = (TextView) view.findViewById(R.id.tvMonthData);
		tvYearData = (TextView) view.findViewById(R.id.tvYearData);
		ivCursor = (ImageView) view.findViewById(R.id.ivCursor);
		tvList.add(tvDayData);
		tvList.add(tvMonthData);
		tvList.add(tvYearData);
		//initImageView();// 初始化游标的位置
		toolbarTitle = (TextView) view.findViewById(R.id.toolbar_title);
		toolbarTitle.setText(getString(R.string.all_info));
		tabHost = (FragmentTabHost) view.findViewById(android.R.id.tabhost);
		tabHost.setup(getActivity(), getChildFragmentManager(),
				R.id.realtabcontent);
		tabHost.addTab(
				tabHost.newTabSpec("0").setIndicator(
						getIndicator2(getString(R.string.day))),
				AllDeviceDayDataFragment2.class, null);
		tabHost.addTab(
				tabHost.newTabSpec("1").setIndicator(
						getIndicator2(getString(R.string.month))),
				AllDeviceMonthDataFragment2.class, null);
		tabHost.addTab(
				tabHost.newTabSpec("2").setIndicator(
						getIndicator2(getString(R.string.year))),
				AllDeviceYearDataFragment2.class, null);
		tabHost.setCurrentTab(0);
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB){
			tabHost.getTabWidget().setDividerDrawable(R.color.white);
		}
		return view;
	}

	private View getIndicator2(String name) {
		View v = getActivity().getLayoutInflater().inflate(
				R.layout.tab_indicator, null);
		TextView tv = (TextView) v.findViewById(R.id.textView);
		TextView tv2 = (TextView) v.findViewById(R.id.textView2);
		tv2.setVisibility(View.GONE);
		tv.setText(name);
		return v;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// 让主线程代码延迟
//		handler.postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				setListener();// 由于事件监听的参数有view控件大小等需要延迟获得的数据，所以监听需要延迟执行
//			}
//		}, 100);
	}

	/**
	 * 
	 * @Description:供外部调用的刷新图表的方法 Parameters: return:void
	 */
//	public void refreshBar() {
//		if (clickIndex == 0) {
//			LogUtilNIU.value("更新1");
//			allDeviceDayDataFragment.checkDataAndUpdateCharts();
//		} else if (clickIndex == 1) {
//			LogUtilNIU.value("更新2");
//			allDeviceMonthDataFragment.checkDataAndUpdateCharts();
//		} else if (clickIndex == 2) {
//			LogUtilNIU.value("更新3");
//			allDeviceYearDataFragment.checkDataAndUpdateCharts();
//		}
//	}

	/*
	 * 初始化游标位置 初始游标的长度，最初位置
	 */
	private void initImageView() {
		ViewTreeObserver vto = ivCursor.getViewTreeObserver();
		/**
		 * 这个回调接口使view的计算推迟到view被加载完毕 在view改变后回调
		 */
		vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				ivCursor.getViewTreeObserver().removeGlobalOnLayoutListener(
						this);
				ivWide = ivCursor.getWidth();// 游标长度
				linerLayoutW = tag_ll.getWidth();// 外围ll宽度
				offset = (linerLayoutW / 3 - ivWide) / 2;// 获取图片偏移量
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
						ivCursor.getLayoutParams());
				margin.setMargins(initX, margin.topMargin, margin.rightMargin,
						margin.bottomMargin);
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
						margin);
				ivCursor.setLayoutParams(params);
			}
		});

	}

	private void setListener() {
		tvDayData.setOnClickListener(this);
		tvMonthData.setOnClickListener(this);
		tvYearData.setOnClickListener(this);
	}

	/*
	 * 设置tag被选中的蓝色字体效果
	 */
	private void textViewChangeColor(int i) {
		switch (i) {
		case 0:
			tvDayData.setTextColor(getResources().getColor(R.color.darkBlue));
			tvMonthData.setTextColor(getResources()
					.getColor(R.color.middleGray));
			tvYearData
					.setTextColor(getResources().getColor(R.color.middleGray));
			break;
		case 1:
			tvDayData.setTextColor(getResources().getColor(R.color.middleGray));
			tvMonthData.setTextColor(getResources().getColor(R.color.darkBlue));
			tvYearData
					.setTextColor(getResources().getColor(R.color.middleGray));
			break;
		case 2:
			tvDayData.setTextColor(getResources().getColor(R.color.middleGray));
			tvMonthData.setTextColor(getResources()
					.getColor(R.color.middleGray));
			tvYearData.setTextColor(getResources().getColor(R.color.darkBlue));
			break;
		}

	}

	/**
	 * 
	 * @Description:供外部调用的发UDP刷新数据的方法 Parameters: return:void
	 */
	public void sendUDPToRefreshData() {
		// 先判断现在显示的是哪个index
		if (currIndex == 0) {
			// 发送日数据

		} else if (currIndex == 1) {
			// 发送月数据

		} else if (currIndex == 2) {
			// 发送年数据

		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

}
