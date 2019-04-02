package cn.mioto.bohan.view;

import org.xclcharts.common.DensityUtil;
import org.xclcharts.view.ChartView;

import android.content.Context;
import android.util.AttributeSet;
import cn.mioto.bohan.R;

/** 
* 类说明：ChartView基类，用于设定大小
* 说明：由于不同的手机屏幕对于这个自定义柱状图显示不同。
* 针对800*480做了一套尺寸
* 
*/
public class BaseChartView extends ChartView{

	public BaseChartView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	 public BaseChartView(Context context, AttributeSet attrs){   
	        super(context, attrs);   
	 }
	 
	 public BaseChartView(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
		
	 }
	
	//Demo中bar chart所使用的默认偏移值。
	//偏移出来的空间用于显示tick,axistitle....
	protected int[] getBarLnDefaultSpadding()
	{
		int [] ltrb = new int[4];
		ltrb[0] = DensityUtil.dip2px(getContext(), getResources().getDimension(R.dimen.chartBarMarginLeft)); //left	
		ltrb[1] = DensityUtil.dip2px(getContext(), getResources().getDimension(R.dimen.chartBarMarginTop)); //top
		ltrb[2] = DensityUtil.dip2px(getContext(), getResources().getDimension(R.dimen.chartBarMarginRight)); //right	
		ltrb[3] = DensityUtil.dip2px(getContext(), getResources().getDimension(R.dimen.chartBarMarginBottom)); //bottom						
		return ltrb;
	}
//	
//	protected int[] getPieDefaultSpadding()
//	{
//		int [] ltrb = new int[4];
//		ltrb[0] = DensityUtil.dip2px(getContext(), 26); //left	
//		ltrb[1] = DensityUtil.dip2px(getContext(), 65); //top	
//		ltrb[2] = DensityUtil.dip2px(getContext(), 26); //right		
//		ltrb[3] = DensityUtil.dip2px(getContext(), 20); //bottom						
//		return ltrb;
//	}
		
	@Override  
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {  
        super.onSizeChanged(w, h, oldw, oldh);  
    }  

}
