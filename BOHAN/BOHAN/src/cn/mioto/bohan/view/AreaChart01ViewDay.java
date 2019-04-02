package cn.mioto.bohan.view;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.xclcharts.chart.AreaData;
import org.xclcharts.chart.LineChart;
import org.xclcharts.chart.LineData;
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
import cn.mioto.bohan.Constant;
import cn.mioto.bohan.R;

/** 
 * 类说明：区域图
 */
public class AreaChart01ViewDay extends BaseChartView implements Runnable{

	private String TAG = "AreaChart01View";

	private LineChart chart = new LineChart();	
	//标签集合
	private LinkedList<String> mLabels = new LinkedList<String>();
	//数据集合
	private LinkedList<LineData> mDataset = new LinkedList<LineData>();

	private Paint mPaintTooltips = new Paint(Paint.ANTI_ALIAS_FLAG);

	public AreaChart01ViewDay(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		//		initView();
	}	 

	public AreaChart01ViewDay(Context context, AttributeSet attrs){   
		super(context, attrs);   
		//		initView();
	}

	public AreaChart01ViewDay(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		//		initView();
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
	 * @Description:对外提供的设置图的方法
	 * x轴字符数组
	 * 数据
	 * 标题
	 * 单位
	 * 
	 * @return void    
	 * @throws
	 */
	private void initChartData(List<String> lables,List<Double> datas,String title,String xUnit){
		// TODO
		initXLables(lables);//初始化X轴的lable
		initChartDataAndColors(datas);//初始化轴的数据内容
		initChartRender(title, xUnit);
		new Thread(this).start();
		//綁定手势滑动事件
		this.bindTouch(this,chart);
	}

	/***************************************************/
	/**
	 * @Title: setXlables 
	 * @Description:对外提供的方法，这是横坐标标签
	 * @param xLables 标签集合，为String字符串集合
	 * @return void    
	 * @throws
	 */
	public void setXLables(List<String> xLables){
		mLabels.clear();//清空之前的数据
		for(String l : xLables){
			mLabels.add(l); 
		}
	}

	/***************************************************/
	/** TODO
	 * @Title: setBarColorAndData 
	 * @Description:设置标签颜色和数据
	 * @param xLables 标签集合
	 * @return void    
	 * @throws
	 */
	List<Double> rateDatas = new LinkedList<Double>();
	public void setBarColorAndData(List<Double> datas,Double max){
		rateDatas.clear();
		rateDatas.addAll(datas);
		//直接改为折线图,此处为新建折线图，分别是折线图所显示的说明，折线图数据源，折线图线条颜色
		LineData ratesLine = new LineData("",rateDatas,
				getResources().getColor(R.color.chartlineColor));
		ratesLine.getDotLabelPaint().setTextSize(Constant.CHART_DATA_TEXT_SIZE);
		//设置线上每点对应标签的颜色
		ratesLine.getDotLabelPaint().setColor(Color.BLACK);
		ratesLine.setDotStyle(XEnum.DotStyle.RING);	//点空心
		//设置外环深蓝色，和线的颜色一样
		ratesLine.getDotPaint().setColor(getResources().getColor(R.color.chartlineColor));
		//线上的点的颜色
		ratesLine.getPlotLine().getPlotDot().setRingInnerColor(getResources().getColor(R.color.red));
		//设置点标签
		ratesLine.setLabelVisible(true);
		ratesLine.getLabelOptions().setLabelBoxStyle(XEnum.LabelBoxStyle.TEXT);
		//		line2.setAreaBeginColor(Color.WHITE);
		ratesLine.getLabelOptions().getBox().getBackgroundPaint().setColor(Color.WHITE);
		mDataset.clear();
		mDataset.add(ratesLine);
		chartRender(max);
		new Thread(this).start();
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

//			chart.setCrurveLineStyle(XEnum.CrurveLineStyle.BEELINE);

			//数据轴最大值
			chart.getDataAxis().setAxisMax(Constant.CHART_AREA_DAY_DEFAULT_MAX);
			//数据轴刻度间隔
			chart.getDataAxis().setAxisSteps(Constant.CHART_AREA_DAY_DEFAULT_MAX/5);

			//网格
			chart.getPlotGrid().showHorizontalLines();
//			chart.getPlotGrid().showVerticalLines();	
			//把顶轴和右轴隐藏
			//chart.hideTopAxis();
			//chart.hideRightAxis();
			//把轴线和刻度线给隐藏起来
			chart.getDataAxis().hideAxisLine();
			chart.getDataAxis().hideTickMarks();	
			//			chart.getDataAxis().hideAxisLabels();
			//			chart.getCategoryAxis().hideAxisLine();
			//			chart.getCategoryAxis().hideTickMarks();	
			//			chart.getCategoryAxis().setHorizontalTickAlign(Align.CENTER);// TODO
			//			chart.getCategoryAxis().setAxisSteps(10);// TODO 设置步长
			/*
				//标题
				chart.setTitle("区域图(Area Chart)");
				chart.addSubtitle("(XCL-Charts Demo)");	
				//轴标题
				chart.getAxisTitle().setLowerAxisTitle("(年份)");
			 */
			//透明度
			chart.disablePanMode();	//设置为不可移动		
			chart.getDataAxis().setLabelFormatter(new IFormatterTextCallBack(){

				@Override
				public String textFormatter(String value) {
					// TODO Auto-generated method stub		
					Double tmp = Double.parseDouble(value);
					DecimalFormat df=new DecimalFormat("#0.00");
					String label = df.format(tmp).toString();				
					return (label);
				}

			});

			//设定交叉点标签显示格式
			chart.setItemLabelFormatter(new IFormatterDoubleCallBack() {

				@Override
				public String doubleFormatter(Double value) {
					DecimalFormat df=new DecimalFormat("#0.0000");					 
					String label = df.format(value).toString();
					return label;
				}});

		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, e.toString());
		}
	}

	// ------------------------------------------------------------------------------

	@Override  
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {  
		super.onSizeChanged(w, h, oldw, oldh);  
		//图所占范围大小
		chart.setChartRange(w,h);
	}  

	private void chartRender(Double max)
	{
		try{												 
			//设置绘图区默认缩进px值,留置空间显示Axis,Axistitle....		
			int [] ltrb = getBarLnDefaultSpadding();
			chart.setPadding(ltrb[0], ltrb[1], ltrb[2], ltrb[3]);
			//设置左边轴字体大小
			chart.getCategoryAxis().getTickLabelPaint().setTextSize(Constant.CHART_X_TEXT_SIZE);
			chart.getDataAxis().getTickLabelPaint().setTextSize(Constant.CHART_Y_TEXT_SIZE);
			//轴数据源						
			//标签轴显示的数据源
			chart.setCategories(mLabels);
			//数据轴显示的数据源
			chart.setDataSource(mDataset);
			//			chart.setCrurveLineStyle(XEnum.CrurveLineStyle.BEELINE);
			//纵数据轴最大值
			chart.getDataAxis().setAxisMax(max);
			//数据轴刻度间隔
			chart.getDataAxis().setAxisSteps(max/5);
			//网格
			chart.getPlotGrid().showHorizontalLines();
			chart.getPlotGrid().getHorizontalLinePaint().setColor(getResources().getColor(R.color.chart_grid_line_color));
			//把顶轴和右轴隐藏
			//chart.hideTopAxis();
			//chart.hideRightAxis();
			//把轴线和刻度线给隐藏起来
			chart.getDataAxis().hideAxisLine();
			chart.getDataAxis().hideTickMarks();			
			//隐藏横坐标横线
			chart.getCategoryAxis().hideAxisLine();
			//隐藏横坐标tick
			chart.getCategoryAxis().hideTickMarks();				
			chart.getCategoryAxis().setTickLabelMargin(20);	
			//设置当值与底轴的最小值相等时，线是否与轴连结显示. 默认为连接(true)
			chart.setLineAxisIntersectVisible(false);
			//动态线									
			chart.showDyLine();

			//不封闭
			chart.setAxesClosed(false);
			/*
				//标题
				chart.setTitle("区域图(Area Chart)");
				chart.addSubtitle("(XCL-Charts Demo)");	
				//轴标题
				chart.getAxisTitle().setLowerAxisTitle("(年份)");
			 */
			//透明度
			chart.disablePanMode();	//设置为不可移动		
			chart.getDataAxis().setLabelFormatter(new IFormatterTextCallBack(){

				@Override
				public String textFormatter(String value) {
					Double tmp = Double.parseDouble(value);
					DecimalFormat df=new DecimalFormat("#0.00");
					String label = df.format(tmp).toString();				
					return (label);
				}
			});

			//设定交叉点标签显示格式
			chart.setItemLabelFormatter(new IFormatterDoubleCallBack() {
				@Override
				public String doubleFormatter(Double value) {
					DecimalFormat df=new DecimalFormat("#0.0");					 
					String label = df.format(value).toString();
					return label;
				}});

		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, e.toString());
		}
	}

	private void chartDataSet()
	{//数据
		List<Double> dataSeries2 = new LinkedList<Double>();			

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
		mLabels.add("1");
		mLabels.add("2");
		mLabels.add("3");
		mLabels.add("4");
		mLabels.add("5");
		mLabels.add("6");
		mLabels.add("7");
		mLabels.add("8");
		mLabels.add("9");
		mLabels.add("10");
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

		LineData lData = mDataset.get(record.getDataID());
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
