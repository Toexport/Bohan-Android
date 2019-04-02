package cn.mioto.bohan.view;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.xclcharts.chart.BarChart;
import org.xclcharts.chart.BarData;
import org.xclcharts.chart.CustomLineData;
import org.xclcharts.common.IFormatterDoubleCallBack;
import org.xclcharts.common.IFormatterTextCallBack;
import org.xclcharts.renderer.XEnum;
import org.xclcharts.renderer.XEnum.Direction;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import cn.mioto.bohan.Constant;
import cn.mioto.bohan.R;

/** 
 * 类说明：自定义柱状图类
 * 用于显示年数据
 * 数据节点为月份。所以这个类命名为CustomBarChartViewMonth
 * 
 */
public class AllCustomBarChartViewDay extends BaseChartView implements Runnable{

	private String TAG = "CustomBarChartView";
	private BarChart chart = new BarChart();
	//轴数据源
	/*
	 * 装x标签数据的容器
	 */
	private List<String> chartLabels = new LinkedList<String>();//使用link可以随意地实现删除

	private List<BarData> chartData = new LinkedList<BarData>();
	private List<CustomLineData> mCustomLineDataset = new LinkedList<CustomLineData>();
	private List<Double> dataSeriesA = new ArrayList<>();//数据的集合
	private List<Integer> dataColorA = new ArrayList<>();//颜色的集合

	public AllCustomBarChartViewDay(Context context) {
		super(context);
		//		initView();
	}

	public AllCustomBarChartViewDay(Context context, AttributeSet attrs){   
		super(context, attrs);   
		//		initView();
	}

	public AllCustomBarChartViewDay(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		//		initView();
	}
	// -------------以下是我重写的，供外部调用的方法-----------------------------------------------------------------

	/***************************************************/
	/**
	 * @Title: setXlables 
	 * @Description:对外提供的方法，这是横坐标标签
	 * @param xLables 标签集合，为String字符串集合
	 * @return void    
	 * @throws
	 */
	public void setXlables(List<String> xLables){
		chartLabels.clear();//清空数据
		for(String l : xLables){
			chartLabels.add(l); 
		}
		this.invalidate();
	}
	/***************************************************/
	/**
	 * @Title: setBarColorAndData 
	 * @Description:设置标签颜色和数据
	 * @param xLables 标签集合
	 * @return void    
	 * @throws
	 */
	private List<Double>powerDatas = new ArrayList<>();

	public void setBarColorAndData(List<String> color,List<Double> datas,Double max){
		dataColorA.clear();
		int barColor=getResources().getColor(R.color.chartColor);
		for(int i = 0; i<color.size();i++){
			if(color.get(i).equals("dark")){//当下标为12的时候
				barColor=getResources().getColor(R.color.chartColor2);//显示为第二种颜色
			}else if (color.get(i).equals("3rdColor")){//设置第三种颜色
				barColor=getResources().getColor(R.color.chartColor3);//显示为第三种颜色
			}
			dataColorA.add(barColor);
		}
		powerDatas.clear();
		powerDatas.addAll(datas);
		chartData.clear();
		chartData.add(new BarData("",powerDatas ,dataColorA ,Color.rgb(53, 169, 239)));
		chartRender(max);
		//綁定手势滑动事件 TODO
		this.bindTouch(this,chart);
		new Thread(this).start();
	}


	/**
	 * @Title: chartRender 
	 * 参数 max 为传入的list集合通过一个算法算出来的数，用户控制纵坐标上线
	 * @Description:用来设置图显示上限等要素的方法，初始化时
	 * @return void    
	 * @throws
	 */
	private void chartRender(Double max)
	{
		try {
			chart.setChartDirection(Direction.HORIZONTAL);
			//设置绘图区默认缩进px值,留置空间显示Axis,Axistitle....		
			int [] ltrb = getBarLnDefaultSpadding();//基类设定的值
			chart.setPadding(ltrb[0], ltrb[1], ltrb[2], ltrb[3]);//加入到设定
			chart.getBar().getItemLabelPaint().setTextSize(Constant.CHART_DATA_TEXT_SIZE);
			chart.getCategoryAxis().getTickLabelPaint().setTextSize(Constant.CHART_X_TEXT_SIZE);
			chart.getDataAxis().getTickLabelPaint().setTextSize(Constant.CHART_Y_TEXT_SIZE);
			//标题
			//chart.setTitle(getResources().getString(R.string.all_device_last_12_hours)+"(kWh)");
			//数据源
			//chart.setDataSource(chartData);
			chart.setCategories(chartLabels);	
			chart.setCustomLines(mCustomLineDataset);
			//图例
			chart.getAxisTitle().setTitleStyle(XEnum.AxisTitleStyle.ENDPOINT);//右下角
			/******************************************* 先去掉下单位********/
			//设置右下
			//			chart.getAxisTitle().setLowerTitle(getResources().getString(R.string.hour));

			//数据轴取值范围
			chart.getDataAxis().setAxisMax(max);
			chart.getDataAxis().setAxisMin(0);
			chart.getDataAxis().setAxisSteps(max/5);	

			//指隔多少个轴刻度(即细刻度)后为主刻度
			//chart.getDataAxis().setDetailModeSteps(0.05);

			//背景网格
			chart.getPlotGrid().showHorizontalLines();
			chart.getPlotGrid().getHorizontalLinePaint().setColor(getResources().getColor(R.color.chart_grid_line_color));

			//定义数据轴标签显示格式
			chart.getDataAxis().setLabelFormatter(new IFormatterTextCallBack(){

				@Override
				public String textFormatter(String value) {
					Double tmp = Double.parseDouble(value);
					DecimalFormat df=new DecimalFormat("#0.00");//显示数据保留的位数
					String label = df.format(tmp).toString();				
					return (label);
				}				
			});			
			chart.getBar().setItemLabelVisible(true);//在柱形顶部显示值
			chart.getBar().setBarStyle(XEnum.BarStyle.OUTLINE);
			chart.getBar().setBorderWidth(2);
			chart.getBar().setOutlineAlpha(500);//设置柱子颜色透明度
			//chart.getBar().setBarRoundRadius(5);
			//chart.getBar().setBarStyle(XEnum.BarStyle.ROUNDBAR);
			//			chart.setPlotPanMode(XEnum.PanMode.HORIZONTAL);	

			//设定格式
			chart.setItemLabelFormatter(new IFormatterDoubleCallBack() {
				@Override
				public String doubleFormatter(Double value) {
					DecimalFormat df=new DecimalFormat("#0.00");					 
					String label = df.format(value).toString();
					return label;
				}});

			//隐藏Key
			chart.getPlotLegend().hide();           
			chart.getClipExt().setExtTop(150.f);
			//柱形和标签居中方式
			chart.setBarCenterStyle(XEnum.BarCenterStyle.TICKMARKS);
			chart.getPlotTitle().getTitlePaint().setTextSize(22);
			//激活点击监听  TODO
			chart.ActiveListenItemClick();
			chart.showClikedFocus();
			chart.extPointClickRange(5);
//			chart.disableScale(); //不可以放大
			chart.disablePanMode();//不可以左右滑动
			//隐藏纵坐标轴
			chart.getDataAxis().hideAxisLine();	
			//隐藏横坐标轴
			chart.getCategoryAxis().hideAxisLine();
			//隐藏刻度线
			chart.getDataAxis().hideTickMarks();	
			chart.getCategoryAxis().hideTickMarks();	
			int margin = 20;
			chart.getCategoryAxis().setTickLabelMargin(margin );
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// -------------以下是通用的方法，通常用户初始化时-----------------------------------------------------------------

	/**
	 * 大小改变
	 */
	@Override  
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {  
		super.onSizeChanged(w, h, oldw, oldh);  
		//图所占范围大小
		chart.setChartRange(w,h);
	} 

	//	/**
	//	 * @Title: countUpNumber 
	//	 * @Description:用于计算传过来的数的图表上限的方法
	//	 * 
	//	 * 1100 上限 1200
	//	 * @return int   向上取整结果
	//	 * @throws
	//	 */
	//	public int countUpNumberSmall(int number){
	//		String n=String.valueOf(number);
	//		//位数
	//		int length=n.length();
	//		if(length<2){//如果小于2位数，向上取整结果为10
	//			return 10;
	//		}else if (length>=2){//大于等于2位数时
	//			
	//		}
	//	}

	/**
	 * @Title: chartRender 
	 * @Description:用来设置图显示上限等要素的方法，初始化时
	 * @return void    
	 * @throws
	 */
	private void chartRender()
	{
		try {
			//设置绘图区默认缩进px值,留置空间显示Axis,Axistitle....		
			int [] ltrb = getBarLnDefaultSpadding();//基类设定的值
			chart.setPadding(ltrb[0], ltrb[1], ltrb[2], ltrb[3]);//加入到设定
			//标题
			//			chart.setTitle(getResources().getString(R.string.all_device_last_12_hours)+"(kWh)");
			//数据源
			//chart.setDataSource(chartData);
			chart.setCategories(chartLabels);	
			chart.setCustomLines(mCustomLineDataset);
			//图例
			chart.getAxisTitle().setTitleStyle(XEnum.AxisTitleStyle.ENDPOINT);//右下角
			/*******************************************先去掉下单位********/
			//设置右下
			//			chart.getAxisTitle().setLowerTitle(getResources().getString(R.string.hour));

			//数据轴取值范围
			chart.getDataAxis().setAxisMax(Constant.CHART_BAR_DAY_DEFAULT_MAX);
			chart.getDataAxis().setAxisMin(0);
			chart.getDataAxis().setAxisSteps(Constant.CHART_BAR_DAY_DEFAULT_MAX/5);	

			//指隔多少个轴刻度(即细刻度)后为主刻度
			//chart.getDataAxis().setDetailModeSteps(0.05);

			//背景网格
			chart.getPlotGrid().showHorizontalLines();

			//定义数据轴标签显示格式
			chart.getDataAxis().setLabelFormatter(new IFormatterTextCallBack(){

				@Override
				public String textFormatter(String value) {
					Double tmp = Double.parseDouble(value);
					DecimalFormat df=new DecimalFormat("#0.00");//显示数据保留的位数
					String label = df.format(tmp).toString();				
					return (label);
				}				
			});			

			chart.getBar().setItemLabelVisible(true);//在柱形顶部显示值
			chart.getBar().setBarStyle(XEnum.BarStyle.OUTLINE);
			chart.getBar().setBorderWidth(2);
			chart.getBar().setOutlineAlpha(400);//设置柱子颜色透明度

			//chart.getBar().setBarRoundRadius(5);
			//chart.getBar().setBarStyle(XEnum.BarStyle.ROUNDBAR);
			chart.setPlotPanMode(XEnum.PanMode.HORIZONTAL);	
			chart.disableScale();//不可以缩放
			//			chart.disablePanMode();

			//设定格式
			chart.setItemLabelFormatter(new IFormatterDoubleCallBack() {
				@Override
				public String doubleFormatter(Double value) {
					DecimalFormat df=new DecimalFormat("#0.00");					 
					String label = df.format(value).toString();
					return label;
				}});

			//隐藏Key
			chart.getPlotLegend().hide();           
			chart.getClipExt().setExtTop(150.f);
			//柱形和标签居中方式
			chart.setBarCenterStyle(XEnum.BarCenterStyle.TICKMARKS);
			chart.setChartDirection(Direction.HORIZONTAL);
			chart.getPlotTitle().getTitlePaint().setTextSize(22);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @Title: chartDataSet 
	 * @Description:初始化，什么代码都没有写时，图表就是这个值
	 * @return void    
	 * @throws
	 */
	private void chartDataSet()//初始化数据，这里的数据为图标新建时初始的数据
	{
		dataSeriesA= new LinkedList<Double>();	
		dataSeriesA.add(0.00); 
		dataSeriesA.add(0.00); 
		dataSeriesA.add(0.00); 
		dataSeriesA.add(0.00); 
		dataSeriesA.add(0.00); 
		dataSeriesA.add(0.00); 
		dataSeriesA.add(0.00); 
		dataSeriesA.add(0.00); 
		dataSeriesA.add(0.00); 
		dataSeriesA.add(0.00); 

		//依数据值确定对应的柱形颜色.
		dataColorA= new LinkedList<Integer>();
		dataColorA.add(getResources().getColor(R.color.chartColor));	
		dataColorA.add(getResources().getColor(R.color.chartColor));	
		dataColorA.add(getResources().getColor(R.color.chartColor));	
		dataColorA.add(getResources().getColor(R.color.chartColor));	
		dataColorA.add(getResources().getColor(R.color.chartColor));	
		dataColorA.add(getResources().getColor(R.color.chartColor));	
		dataColorA.add(getResources().getColor(R.color.chartColor));	
		dataColorA.add(getResources().getColor(R.color.chartColor));	
		dataColorA.add(getResources().getColor(R.color.chartColor));	
		dataColorA.add(getResources().getColor(R.color.chartColor));	
		chartData.clear();
		chartData.add(new BarData("",dataSeriesA,dataColorA,Color.rgb(53, 169, 239)));
	}


	private void initView()//作者原始的构造方法。初始化时没有数据
	{
		chartLabels();
		chartDataSet();
		//chartCustomLines(); 不需要分界线
		chartRender();
		//綁定手势滑动事件
		//		this.bindTouch(this,chart);
		new Thread(this).start();
	}

	private void chartLabels()//初始化时，显示12个月
	{
		chartLabels.add(""); 
		chartLabels.add(""); 
		chartLabels.add(""); 
		chartLabels.add(""); 
		chartLabels.add(""); 
		chartLabels.add(""); 
		chartLabels.add(""); 
		chartLabels.add(""); 
		chartLabels.add(""); 
		chartLabels.add(""); 
	}	

	@Override
	public void render(Canvas canvas) {
		try{
			chart.render(canvas);
		} catch (Exception e){
			Log.e(TAG, e.toString());
		}
	}

	/***************************************************/
	/**
	 * 数据出现的动画  通用 chartAnimation()
	 */
	@Override
	public void run() {
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

			for(int i=0;i< chartData.size() ;i++)
			{           		       		          		          		          	
				BarData barData = chartData.get(i); 
				for(int j=0;j<barData.getDataSet().size();j++)
				{     
					Thread.sleep(100);
					List<BarData> animationData = new LinkedList<BarData>();
					List<Double> dataSeries= new LinkedList<Double>();	
					List<Integer> dataColorA= new LinkedList<Integer>();
					for(int k=0;k<=j;k++)
					{          				
						dataSeries.add(barData.getDataSet().get(k));  
						dataColorA.add(barData.getDataColor().get(k));  
					}

					BarData animationBarData = new BarData("",dataSeries,dataColorA,Color.rgb(53, 169, 239));
					animationData.add(animationBarData);
					chart.setDataSource(animationData);
					postInvalidate(); 
				}          		          		   
			}

		}
		catch(Exception e) {
			Thread.currentThread().interrupt();
		}            
	}
}
