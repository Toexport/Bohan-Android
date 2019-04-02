package cn.mioto.bohan.activity;

import java.util.ArrayList;
import java.util.List;

import cn.mioto.bohan.R;
import cn.mioto.bohan.entity.SingleDevice;
import cn.mioto.bohan.fragment.LinkAddByHandFragment;
import cn.mioto.bohan.fragment.LinkFastAddFragment;
import cn.mioto.bohan.utils.ViewUtil;

import com.umeng.analytics.MobclickAgent;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/** 
 * 类说明：智能连接界面
 */
public class SmartLinkActivity extends AppCompatActivity implements OnClickListener {
	/**********DECLARES*************/
	private LinearLayout tagll;
	private TextView tvfastAdd;
	private TextView tvHandAdd;
	private ImageView cursorIv;
	private android.support.v4.view.ViewPager mViewPagerSmarkLink;
	/**********设定TAB*************/
	int offset; //下标的偏移量
	int currIndex = 0;// 当前页卡编号
	int ivWide;//下标宽度
	int linerLayoutW;
	private SingleDevice currentDevice;//Application里设置了的当前设备
	Handler handler = new Handler();
	List<Fragment> fragments;
	LinkFastAddFragment fastAddFragment;
	LinkAddByHandFragment addByHandFragment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setTheme(R.style.AppBaseTheme);
		setContentView(R.layout.activity_smartlink);
		bindViews();
		int titleString=R.string.connect_wifi;//初始化toolbar
		ViewUtil.initToolbar(this, R.id.toolbar, R.id.toolbar_title, titleString, true, false, 0,0);
		initImageView();//设置tab下标
		viewPagerAddFragment();
	}

	private void viewPagerAddFragment() {
		fragments = new ArrayList<Fragment>();
		fastAddFragment = new LinkFastAddFragment();
		addByHandFragment = new LinkAddByHandFragment();
		fragments.add(fastAddFragment);
		fragments.add(addByHandFragment);
		mViewPagerSmarkLink.setAdapter(new InnerPagerFragmentAdapter(getSupportFragmentManager()));
		mViewPagerSmarkLink.setCurrentItem(0);
		textViewChangeColor(0);
		//让主线程代码延迟s
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				mViewPagerSmarkLink.setOnPageChangeListener(new MyOnPageChangeListener());
			}
		}, 100);
		setListener();
	}
	protected void setListener() {
		tvfastAdd.setOnClickListener(this);
		tvHandAdd.setOnClickListener(this);
	}
	public class InnerPagerFragmentAdapter extends FragmentPagerAdapter {

		public InnerPagerFragmentAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			return fragments.get(position);
		}

		@Override
		public int getCount() {
			return fragments.size();
		}
	}


	private void textViewChangeColor(int i) {
		switch (i) {
		case 0:
			tvfastAdd.setTextColor(getResources().getColor(R.color.darkBlue));
			tvHandAdd.setTextColor(getResources().getColor(R.color.middleGray));
			break;
		case 1:
			tvfastAdd.setTextColor(getResources().getColor(R.color.middleGray));
			tvHandAdd.setTextColor(getResources().getColor(R.color.darkBlue));
			break;
		}

	}

	private void initImageView() {
		ViewTreeObserver vto = cursorIv.getViewTreeObserver();
		//这个回调接口使view的计算推迟到view被加载完毕

		vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				cursorIv.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				ivWide = cursorIv.getWidth();
				linerLayoutW = tagll.getWidth();
				offset = (linerLayoutW / 2 - ivWide) / 2;// 获取图片偏移量

				/*cursor的初始位置设定：
                思路 屏幕总宽减去LinerLayout宽度再除以2在加上offset就得到初始位置
				 */
				DisplayMetrics dm = new DisplayMetrics();
				getWindowManager().getDefaultDisplay().getMetrics(dm);
				int screenW = dm.widthPixels; // 获取手机屏幕宽度分辨率
				int initX = (screenW - linerLayoutW) / 2 + offset;
				//计算出初始位置后，设置控件的位置
				LinearLayout.MarginLayoutParams margin = new LinearLayout.MarginLayoutParams(cursorIv.getLayoutParams());
				margin.setMargins(initX, margin.topMargin, margin.rightMargin, margin.bottomMargin);
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(margin);
				cursorIv.setLayoutParams(params);
			}
		});
	}

	private void bindViews() {
		tagll = (LinearLayout) findViewById(R.id.tag_ll);
		tvfastAdd = (TextView) findViewById(R.id.tvfastAdd);
		tvHandAdd = (TextView) findViewById(R.id.tvHandAdd);
		cursorIv = (ImageView) findViewById(R.id.cursorIv);
		mViewPagerSmarkLink = (android.support.v4.view.ViewPager) findViewById(R.id.mViewPagerSmarkLink);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tvfastAdd:
			mViewPagerSmarkLink.setCurrentItem(0);
			break;
		case R.id.tvHandAdd:
			mViewPagerSmarkLink.setCurrentItem(1);
			break;
		}
	}

	public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {
		/*  计算出标签的偏移量
             标签偏移量大小为offset * 2 + ivWide
		 */
		private int one = offset * 2 + ivWide;

		@Override
		public void onPageScrollStateChanged(int arg0) {

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
//			DisplayMetrics dm = new DisplayMetrics();
//			getWindowManager().getDefaultDisplay().getMetrics(dm);
//			int screenW = dm.widthPixels; // 获取手机屏幕宽度分辨率
//			Animation animation = new TranslateAnimation(currIndex * one, arg1*screenW, 0, 0);
		}

		// 即将要被显示的页卡的index
		@Override
		public void onPageSelected(int arg0) {
			// 初始化移动的动画（从当前位置，x平移到即将要到的位置）
			//这个动画设定了平移效果，从哪个位置移动到哪个位置，记录了上次的位置，计算出下次的位置
			Animation animation = new TranslateAnimation(currIndex * one, arg0
					* one, 0, 0);
			currIndex = arg0;
			animation.setFillAfter(true); // 动画终止时停留在最后一帧，不然会回到没有执行前的状态
			animation.setDuration(200); // 动画持续时间，0.2秒
			cursorIv.startAnimation(animation); // 是用imageview来显示动画
			int i = currIndex + 1;
			//改变textView的颜色
			textViewChangeColor(arg0);
		}
	}
	
	public void onResume() {
	    super.onResume();
	    MobclickAgent.onResume(this);       //统计时长
	}
	public void onPause() {
	    super.onPause();
	    MobclickAgent.onPause(this);
	}
	
}
