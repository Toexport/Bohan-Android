package cn.mioto.bohan.fragment;

import java.util.ArrayList;
import java.util.List;

import cn.mioto.bohan.R;
import cn.mioto.bohan.utils.LogUtilNIU;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
public class MainInfoFragment extends BaseFragment implements OnClickListener {
	/**********DECLARES*************/
	private List<TextView> tvList= new ArrayList<TextView>();
	private LinearLayout tag_ll;
	private TextView tvDayData;
	private TextView tvMonthData;
	private TextView tvYearData;
	private ImageView ivCursor;
	private LinearLayout llContainer;
	private Fragment[] fragments = new Fragment[3];
	private AllDeviceDayDataFragment allDeviceDayDataFragment;
	private AllDeviceMonthDataFragment allDeviceMonthDataFragment;
	private AllDeviceYearDataFragment allDeviceYearDataFragment;
	int currentFragmentIndex = 0;
	private int clickIndex = 0;
	
	public int getClickIndex() {
		return clickIndex;
	}

	int offset; //下标的偏移量
	int currIndex = 0;// 当前页卡编号
	int ivWide;//下标宽度
	int linerLayoutW;
	Handler handler = new Handler();
	View view;
	//判断用户名下有没有设备

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view =inflater.inflate(R.layout.fragment_main_info, null);
		/**********初始化控件*************/
		tag_ll = (LinearLayout) view.findViewById(R.id.tag_ll);
		tvDayData = (TextView) view.findViewById(R.id.tvDayData);
		tvMonthData = (TextView) view.findViewById(R.id.tvMonthData);
		tvYearData = (TextView)view. findViewById(R.id.tvYearData);
		ivCursor = (ImageView) view.findViewById(R.id.ivCursor);
		llContainer = (LinearLayout) view.findViewById(R.id.llContainer);
		tvList.add(tvDayData);
		tvList.add(tvMonthData);
		tvList.add(tvYearData);
		allDeviceDayDataFragment = new AllDeviceDayDataFragment();
		allDeviceMonthDataFragment = new AllDeviceMonthDataFragment();
		allDeviceYearDataFragment = new AllDeviceYearDataFragment();
		fragments[0]=allDeviceDayDataFragment;
		fragments[1]=allDeviceMonthDataFragment;
		fragments[2]=allDeviceYearDataFragment;
		FragmentManager manager = getChildFragmentManager();
		FragmentTransaction transaction=manager.beginTransaction();
		transaction.add(R.id.llContainer, allDeviceDayDataFragment);//设置显示第一个fragment
		transaction.commit();
		textViewChangeColor(0);//设置第一个textView变成蓝色，实现被默认选择的效果。
		initImageView();//初始化游标的位置
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		//让主线程代码延迟
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				setListener();//由于事件监听的参数有view控件大小等需要延迟获得的数据，所以监听需要延迟执行
			}
		}, 100);
	}
	/**
	 * 
	 * @Description:供外部调用的刷新图表的方法
	 * Parameters: 
	 * return:void
	 */
	public void refreshBar(){
		if(clickIndex ==0){
			LogUtilNIU.value("更新1");
			allDeviceDayDataFragment.checkDataAndUpdateCharts();
		}else if(clickIndex ==1){
			LogUtilNIU.value("更新2");
			allDeviceMonthDataFragment.checkDataAndUpdateCharts();
		}else if (clickIndex ==2){
			LogUtilNIU.value("更新3");
			allDeviceYearDataFragment.checkDataAndUpdateCharts();
		}
	}
	/*
	 * 初始化游标位置
	 * 初始游标的长度，最初位置
	 */
	private void initImageView() {
		ViewTreeObserver vto = ivCursor.getViewTreeObserver();
		/**
		 * 这个回调接口使view的计算推迟到view被加载完毕
		 * 在view改变后回调
		 */
		vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				ivCursor.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				ivWide = ivCursor.getWidth();//游标长度
				linerLayoutW = tag_ll.getWidth();//外围ll宽度
				offset = (linerLayoutW / 3 - ivWide) / 2;// 获取图片偏移量
				/*cursor的初始位置设定：
                思路 屏幕总宽减去LinerLayout宽度再除以2在加上offset就得到初始位置
				 */
				DisplayMetrics dm = new DisplayMetrics();
				getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
				int screenW = dm.widthPixels; // 获取手机屏幕宽度分辨率
				int initX = (screenW - linerLayoutW) / 2 + offset;
				//计算出初始位置后，设置控件的位置
				LinearLayout.MarginLayoutParams margin = new LinearLayout.MarginLayoutParams(ivCursor.getLayoutParams());
				margin.setMargins(initX, margin.topMargin, margin.rightMargin, margin.bottomMargin);
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(margin);
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
			tvMonthData.setTextColor(getResources().getColor(R.color.middleGray));
			tvYearData.setTextColor(getResources().getColor(R.color.middleGray));
			break;
		case 1:
			tvDayData.setTextColor(getResources().getColor(R.color.middleGray));
			tvMonthData.setTextColor(getResources().getColor(R.color.darkBlue));
			tvYearData.setTextColor(getResources().getColor(R.color.middleGray));
			break;
		case 2:
			tvDayData.setTextColor(getResources().getColor(R.color.middleGray));
			tvMonthData.setTextColor(getResources().getColor(R.color.middleGray));
			tvYearData.setTextColor(getResources().getColor(R.color.darkBlue));
			break;
		}

	}
	/*
	 * Description: 实现每个tag被点击，下标以动画形式平移。并且显示对应的fragment
	 * @param v 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tvDayData:
			clickIndex = 0;
			break;
		case R.id.tvMonthData:
			clickIndex = 1;
			break;
		case R.id.tvYearData:
			clickIndex = 2;
			break;
		}
		if(clickIndex!=currentFragmentIndex){
			Fragment showFragment=fragments[clickIndex];
			FragmentTransaction transaction=getChildFragmentManager().beginTransaction();
			if(!showFragment.isAdded()){
				transaction.add(R.id.llContainer, showFragment);
			}
			transaction.hide(fragments[currentFragmentIndex]);
			transaction.show(showFragment);
			transaction.commit();
			textViewChangeColor(clickIndex);
			currentFragmentIndex = clickIndex;
		}
		/*  计算出标签的偏移量
            标签偏移量大小为offset * 2 + ivWide
		 */
		int one = offset * 2 + ivWide;
		// 初始化移动的动画（从当前位置，x平移到即将要到的位置）
		//这个动画设定了平移效果，从哪个位置移动到哪个位置，记录了上次的位置，计算出下次的位置
		Animation animation = new TranslateAnimation(currIndex * one, clickIndex
				* one, 0, 0);
		currIndex = clickIndex;
		animation.setFillAfter(true); // 动画终止时停留在最后一帧，不然会回到没有执行前的状态
		animation.setDuration(200); // 动画持续时间，0.2秒
		ivCursor.startAnimation(animation); // 是用imageview来显示动画
	}

	/**
	 * 
	 * @Description:供外部调用的发UDP刷新数据的方法
	 * Parameters: 
	 * return:void
	 */
	public void sendUDPToRefreshData(){
		//先判断现在显示的是哪个index
		if(currIndex ==0){
			//发送日数据

		}else if(currIndex ==1){
			//发送月数据

		}else if(currIndex ==2){
			//发送年数据

		}
	}


}
