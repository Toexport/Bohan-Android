package cn.mioto.bohan.view;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.xclcharts.chart.AreaChart;
import org.xclcharts.chart.AreaData;
import org.xclcharts.common.IFormatterDoubleCallBack;
import org.xclcharts.common.IFormatterTextCallBack;
import org.xclcharts.event.click.PointPosition;
import org.xclcharts.renderer.XEnum;
import org.xclcharts.renderer.info.AnchorDataPoint;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import cn.mioto.bohan.R;

/** 
 * 类说明：区域图
 */
public class AreaChart01View extends BaseChartView implements Runnable{

	private String TAG = "AreaChart01View";

	private AreaChart chart = new AreaChart();	
	//标签集合
	private LinkedList<String> mLabels = new LinkedList<String>();
	//数据集合
	private LinkedList<AreaData> mDataset = new LinkedList<AreaData>();

	private Paint mPaintTooltips = new Paint(Paint.ANTI_ALIAS_FLAG);

	public AreaChart01View(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		initView();
	}	 

	public AreaChart01View(Context context, AttributeSet attrs){   
		super(context, attrs);   
		initView();
	}

	public AreaChart01View(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView();
	}

	private void initView()
	{
		chartLabels();
		chartDataSet();		
		chartRender();

		new Thread(this).start();

		//綁定手势滑动事件
//		this.bindTouch(this,chart);
	}

	/**********************************************************/
	/**
	 * @Title: initChartData 
	 * @Description:对我提供的设置图的方法
	 * x轴字符数组
	 * 数据
	 * 标题
	 * 单位
	 * 
	 * @return void    
	 * @throws
	 */
	public void initChartData(List<String> lables,List<Double> datas,String title,String xUnit){
		// TODO
		initXLables(lables);//初始化X轴的lable
		initChartDataAndColors(datas);//初始化轴的数据内容
		initChartRender(title, xUnit);
		new Thread(this).start();
		//綁定手势滑动事件
		this.bindTouch(this,chart);
	}


	private void initChartRender(String title, String xUnit) {


	}

	private void initChartDataAndColors(List<Double> datas) {

		//对于的数据值
		List<Double> dataSeriesA= new LinkedList<Double>();	
		for(Double d : datas){
			dataSeriesA.add(d); 
		}

		//设置线的颜色



		//			chartData.clear();//把上次的数据删除
		//			chartData.add(new BarData("",dataSeriesA,dataColorA,Color.rgb(53, 169, 239)));


	}

	private void initXLables(List<String> lables) {
		// 初始化x轴下标签
		for(int i = 0 ; i < lables.size(); i ++){
			mLabels.add(lables.get(i)); 
		}
	}


	// ------------------------------------------------------------------------------

	@Override  
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {  
		super.onSizeChanged(w, h, oldw, oldh);  
		//图所占范围大小
		chart.setChartRange(w,h);
	}  

	private void chartRender()
	{
		try{												 
			//设置绘图区默认缩进px值,留置空间显示Axis,Axistitle....		
			int [] ltrb = getBarLnDefaultSpadding();
			chart.setPadding(ltrb[0], ltrb[1], ltrb[2], ltrb[3]);

			//轴数据源						
			//标签轴
			chart.setCategories(mLabels);
			//数据轴
			chart.setDataSource(mDataset);

			chart.setCrurveLineStyle(XEnum.CrurveLineStyle.BEELINE);

			//数据轴最大值
			chart.getDataAxis().setAxisMax(2500);
			//数据轴刻度间隔
			chart.getDataAxis().setAxisSteps(500);

			//网格
			chart.getPlotGrid().showHorizontalLines();
			chart.getPlotGrid().showVerticalLines();	
			//把顶轴和右轴隐藏
			//chart.hideTopAxis();
			//chart.hideRightAxis();
			//把轴线和刻度线给隐藏起来
			chart.getDataAxis().hideAxisLine();
			chart.getDataAxis().hideTickMarks();			
//			chart.getCategoryAxis().hideAxisLine();
//			chart.getCategoryAxis().hideTickMarks();				

			/*
				//标题
				chart.setTitle("区域图(Area Chart)");
				chart.addSubtitle("(XCL-Charts Demo)");	
				//轴标题
				chart.getAxisTitle().setLowerAxisTitle("(年份)");
			 */
			//透明度
			chart.setAreaAlpha(150);
			chart.disablePanMode();	//设置为不可移动		
			chart.getDataAxis().setLabelFormatter(new IFormatterTextCallBack(){

				@Override
				public String textFormatter(String value) {
					// TODO Auto-generated method stub		
					Double tmp = Double.parseDouble(value);
					DecimalFormat df=new DecimalFormat("#0");
					String label = df.format(tmp).toString();				
					return (label);
				}

			});

			//设定交叉点标签显示格式
			chart.setItemLabelFormatter(new IFormatterDoubleCallBack() {
				@Override
				public String doubleFormatter(Double value) {
					DecimalFormat df=new DecimalFormat("#0");					 
					String label = df.format(value).toString();
					return label;
				}});

		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, e.toString());
		}
	}

	private void chartDataSet()
	{
		List<Double> dataSeries2 = new LinkedList<Double>();			
		dataSeries2.add((double)100); 
		dataSeries2.add((double)2200); 
		dataSeries2.add((double)300); 	
		dataSeries2.add((double)1700); 
		dataSeries2.add((double)600);

		AreaData line2 = new AreaData("",dataSeries2,
				getResources().getColor(R.color.lineColor),getResources().getColor(R.color.areaColor));
		//设置线上每点对应标签的颜色
		line2.getDotLabelPaint().setColor(Color.BLACK);
		line2.setDotStyle(XEnum.DotStyle.RING);	//点空心
		//设置点标签
		line2.setLabelVisible(true);
		line2.getLabelOptions().setLabelBoxStyle(XEnum.LabelBoxStyle.CAPRECT);
		//		line2.setAreaBeginColor(Color.WHITE);
		line2.getLabelOptions().getBox().getBackgroundPaint().setColor(Color.WHITE);
		mDataset.add(line2);	
	}

	/*
	 * 下面的标签
	 */
	private void chartLabels()
	{		
		mLabels.add("11~12");
		mLabels.add("11~12");
		mLabels.add("11~12");
		mLabels.add("11~12");
		mLabels.add("11~12");
	}

	@Override
	public void render(Canvas canvas) {
		try{
			chart.render(canvas);
		} catch (Exception e){
			Log.e(TAG, e.toString());
		}
	}


	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub		
		super.onTouchEvent(event);		
		if(event.getAction() == MotionEvent.ACTION_UP) 
		{			
			triggerClick(event.getX(),event.getY());
		}
		return true;
	}


	//触发监听
	private void triggerClick(float x,float y)
	{		
		PointPosition record = chart.getPositionRecord(x,y);			
		if( null == record) return;

		AreaData lData = mDataset.get(record.getDataID());
		Double lValue = lData.getLinePoint().get(record.getDataChildID());	

		/*
		Toast.makeText(this.getContext(), 
				record.getPointInfo() +
				" Key:"+lData.getLineKey() +
				" Label:"+lData.getLabel() +								
				" Current Value:"+Double.toString(lValue), 
				Toast.LENGTH_SHORT).show();	
		 */
		float r = record.getRadius();
		chart.showFocusPointF(record.getPosition(),r + r*0.5f);		
		//chart.getFocusPaint().setStyle(Style.STROKE);
		chart.getFocusPaint().setStrokeWidth(3);		
		chart.getFocusPaint().setColor(Color.RED);
		chart.getFocusPaint().setTextAlign(Align.CENTER);


		//在点击处显示tooltip
		mPaintTooltips.setColor(Color.YELLOW);			
		chart.getToolTip().getBackgroundPaint().setColor(Color.GRAY);
		//chart.getToolTip().setCurrentXY(x,y);
		chart.getToolTip().setCurrentXY(record.getPosition().x,record.getPosition().y); 
		chart.getToolTip().setStyle(XEnum.DyInfoStyle.CAPRECT);	

		chart.getToolTip().addToolTip(" Key:"+lData.getLineKey(),mPaintTooltips);
		chart.getToolTip().addToolTip(" Label:"+lData.getLabel(),mPaintTooltips);		
		chart.getToolTip().addToolTip(" Current Value:" +Double.toString(lValue),mPaintTooltips);
		chart.getToolTip().setAlign(Align.CENTER);

		this.invalidate();

	}


	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {          
			chartAnimation();         	
		}
		catch(Exception e) {
			Thread.currentThread().interrupt();
		}        
	}

	private void chartAnimation()
	{
		try {                            	           		

			chart.getPlotLegend().hide();

			int [] ltrb = getBarLnDefaultSpadding();          	
			for(int i=8; i> 0 ;i--)
			{
				Thread.sleep(100);
				chart.setPadding(ltrb[0],ltrb[1], i *  ltrb[2], ltrb[3]);	    	

				if(1 == i)drawTitle(); 
				postInvalidate();    
			} 

		}
		catch(Exception e) {
			Thread.currentThread().interrupt();
		}            
	}

	private void drawTitle()
	{		
		//标题
		chart.setTitle(getResources().getString(R.string.all_device_month_power)+"(W)");
		chart.getAxisTitle().setTitleStyle(XEnum.AxisTitleStyle.ENDPOINT);
		//设置右下
		/********************* TODO ******************************/
//		chart.getAxisTitle().setLowerTitle(getResources().getString(R.string.hour));
	
		//显示图例
		chart.getPlotLegend().show();
		//激活点击监听
		//		chart.ActiveListenItemClick();
		//为了让触发更灵敏，可以扩大5px的点击监听范围
		//		chart.extPointClickRange(5);		
		//		chart.showClikedFocus();		

		//批注
		List<AnchorDataPoint> mAnchorSet = new ArrayList<AnchorDataPoint>();

		AnchorDataPoint an2 = new AnchorDataPoint(1,3,XEnum.AnchorStyle.CIRCLE);
		an2.setBgColor(Color.GRAY);
		an2.setAnchorStyle(XEnum.AnchorStyle.CIRCLE);


		AnchorDataPoint an3 = new AnchorDataPoint(1,4,XEnum.AnchorStyle.CIRCLE);
		an3.setBgColor(Color.RED);
		an3.setAnchorStyle(XEnum.AnchorStyle.RECT);
		an3.setAreaStyle(XEnum.DataAreaStyle.STROKE);

		//从点到底的标识线
		AnchorDataPoint an4 = new AnchorDataPoint(2,2,XEnum.AnchorStyle.TOBOTTOM);
		an4.setBgColor(Color.YELLOW);
		an4.setLineWidth(15);

		AnchorDataPoint an5 = new AnchorDataPoint(2,1,XEnum.AnchorStyle.TORIGHT);
		an5.setBgColor(Color.WHITE);
		an5.setLineWidth(15);

		//AnchorDataPoint an6 = new AnchorDataPoint(2,1,XEnum.AnchorStyle.HLINE);
		//an6.setBgColor(Color.WHITE);
		//an6.setLineWidth(15);

		mAnchorSet.add(an2);
		mAnchorSet.add(an3);
		mAnchorSet.add(an4);
		mAnchorSet.add(an5);
		//mAnchorSet.add(an6);
		chart.setAnchorDataPoint(mAnchorSet);		


	}


}
