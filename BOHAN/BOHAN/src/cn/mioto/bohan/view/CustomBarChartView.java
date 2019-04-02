package cn.mioto.bohan.view;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

import org.xclcharts.chart.BarChart;
import org.xclcharts.chart.BarData;
import org.xclcharts.chart.CustomLineData;
import org.xclcharts.common.IFormatterDoubleCallBack;
import org.xclcharts.common.IFormatterTextCallBack;
import org.xclcharts.renderer.XEnum;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import cn.mioto.bohan.R;

/** 
 * 类说明：自定义柱状图类
 * 作者：  jiemai liangminhua 
 * 创建时间：2016年6月25日 下午7:49:03 
 */
public class CustomBarChartView extends BaseChartView implements Runnable{

	private String TAG = "CustomBarChartView";
	private BarChart chart = new BarChart();
	//轴数据源
	private List<String> chartLabels = new LinkedList<String>();//使用link可以随意地实现删除
	private List<BarData> chartData = new LinkedList<BarData>();
	private List<CustomLineData> mCustomLineDataset = new LinkedList<CustomLineData>();

	public CustomBarChartView(Context context) {
		super(context);
		initView();
	}

	public CustomBarChartView(Context context, AttributeSet attrs){   
		super(context, attrs);   
		initView();
	}

	public CustomBarChartView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView();
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

	/**********************************************************/
	/**
	 * @Title: initChartData 
	 * @Description:对我提供的设置图的方法
	 * x轴字符
	 * 数据集合
	 * 顶标题
	 * 右下角显示的时间单位
	 * @return void    
	 * @throws
	 */
	public void initChartData(List<String> lables,List<Double> datas,String title,String xUnit){
		// TODO
		initXLables(lables);//初始化X轴的lable，这步是重点。
		//需要把lable传过来。但是因为页面不同，lable也不同。lable还要根据当前的时间计算
		initChartDataAndColors(datas);//初始化轴的数据内容
		initChartRender(title, xUnit);
		//綁定手势滑动事件
		//		this.bindTouch(this,chart);
		new Thread(this).start();
	}


	private void initChartRender(String title,String xUnit) {
		try {

			//设置绘图区默认缩进px值,留置空间显示Axis,Axistitle....		
			int [] ltrb = getBarLnDefaultSpadding();//基类设定的值
			chart.setPadding(ltrb[0], ltrb[1], ltrb[2], ltrb[3]);//加入到设定

			//标题
			//			chart.setTitle(title+"(kWh)");
			//数据源
			//chart.setDataSource(chartData);
			chart.setCategories(chartLabels);	
			chart.setCustomLines(mCustomLineDataset);

			//图例
			chart.getAxisTitle().setTitleStyle(XEnum.AxisTitleStyle.ENDPOINT);
			//设置右下时间单位
			chart.getAxisTitle().setLowerTitle(xUnit);

			//数据轴
			chart.getDataAxis().setAxisMax(0.25);
			chart.getDataAxis().setAxisMin(0);
			chart.getDataAxis().setAxisSteps(0.05);	

			//背景网格
			chart.getPlotGrid().showHorizontalLines();

			//定义数据轴标签显示格式
			chart.getDataAxis().setLabelFormatter(new IFormatterTextCallBack(){

				@Override
				public String textFormatter(String value) {
					// TODO Auto-generated method stub		
					Double tmp = Double.parseDouble(value);
					DecimalFormat df=new DecimalFormat("#0.00");//显示数据保留的位数
					String label = df.format(tmp).toString();				
					return (label);
				}				
			});			

			//在柱形顶部显示值
			chart.getBar().setItemLabelVisible(true);
			chart.getBar().setBarStyle(XEnum.BarStyle.OUTLINE);
			chart.getBar().setBorderWidth(2);
			chart.getBar().setOutlineAlpha(400);

			chart.disablePanMode();//不可以向右滑动

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

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void initChartDataAndColors(List<Double> datas) {
		//标签对应的柱形数据集
		List<Double> dataSeriesA= new LinkedList<Double>();	
		for(Double d : datas){
			dataSeriesA.add(d);
		}

		//依数据值确定对应的柱形颜色.
		List<Integer> dataColorA= new LinkedList<Integer>();
		for(int i = 0 ; i < datas.size(); i ++){
			dataColorA.add(getResources().getColor(R.color.chartColor));	
		}

		chartData.clear();//把上次的数据删除
		chartData.add(new BarData("",dataSeriesA,dataColorA,Color.rgb(53, 169, 239)));
	}

	private void initXLables(List<String> lables) {
		// 初始化x轴下标签
		for(int i = 0 ; i < lables.size(); i ++){
			chartLabels.add(lables.get(i)); 
		}
	}

	@Override  
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {  
		super.onSizeChanged(w, h, oldw, oldh);  
		//图所占范围大小
		chart.setChartRange(w,h);
	}  

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
			/*******************************************  TODO  先去掉下单位********/
			//设置右下
			//			chart.getAxisTitle().setLowerTitle(getResources().getString(R.string.hour));

			//数据轴
			chart.getDataAxis().setAxisMax(0.25);
			chart.getDataAxis().setAxisMin(0);
			chart.getDataAxis().setAxisSteps(0.05);	

			//指隔多少个轴刻度(即细刻度)后为主刻度
			//chart.getDataAxis().setDetailModeSteps(0.05);

			//背景网格
			chart.getPlotGrid().showHorizontalLines();

			//定义数据轴标签显示格式
			chart.getDataAxis().setLabelFormatter(new IFormatterTextCallBack(){

				@Override
				public String textFormatter(String value) {
					// TODO Auto-generated method stub		
					Double tmp = Double.parseDouble(value);
					DecimalFormat df=new DecimalFormat("#0.00");//显示数据保留的位数
					String label = df.format(tmp).toString();				
					return (label);
				}				
			});			

			chart.getBar().setItemLabelVisible(true);//在柱形顶部显示值
			chart.getBar().setBarStyle(XEnum.BarStyle.OUTLINE);
			chart.getBar().setBorderWidth(2);
			chart.getBar().setOutlineAlpha(400);

			//chart.getBar().setBarRoundRadius(5);
			//chart.getBar().setBarStyle(XEnum.BarStyle.ROUNDBAR);
			// TODO 设置为可以左右拉
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

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void chartDataSet()
	{
		//标签对应的柱形数据集
		List<Double> dataSeriesA= new LinkedList<Double>();	
		dataSeriesA.add(0.20); 
		dataSeriesA.add(0.12); 
		dataSeriesA.add(0.22); 
		dataSeriesA.add(0.17); 
		dataSeriesA.add(0.18); 
		//		dataSeriesA.add(0.00); 
		//		dataSeriesA.add(0.00); 	
		//		dataSeriesA.add(0.00); 	
		//		dataSeriesA.add(0.00); 	
		//		dataSeriesA.add(0.00); 	
		//		dataSeriesA.add(0.00); 	
		//		dataSeriesA.add(0.12); 	
		//		dataSeriesA.add(0.15); 	
		//		dataSeriesA.add(0.23); 	
		//		dataSeriesA.add(0.09); 	

		//依数据值确定对应的柱形颜色.
		List<Integer> dataColorA= new LinkedList<Integer>();
		dataColorA.add(getResources().getColor(R.color.chartColor));	
		dataColorA.add(getResources().getColor(R.color.chartColor));	
		dataColorA.add(getResources().getColor(R.color.chartColor));	
		dataColorA.add(getResources().getColor(R.color.chartColor));	
		dataColorA.add(getResources().getColor(R.color.chartColor));	
		//		dataColorA.add(getResources().getColor(R.color.chartColor));	
		//		dataColorA.add(getResources().getColor(R.color.chartColor));	
		//		dataColorA.add(getResources().getColor(R.color.chartColor));	
		//		dataColorA.add(getResources().getColor(R.color.chartColor));	
		//		dataColorA.add(getResources().getColor(R.color.chartColor));	
		//		dataColorA.add(getResources().getColor(R.color.chartColor));	
		//		BarData BarDataA = new BarData("",dataSeriesA,dataColorA,(int)Color.rgb(53, 169, 239));
		chartData.clear();
		chartData.add(new BarData("",dataSeriesA,dataColorA,Color.rgb(53, 169, 239)));
	}

	private void chartLabels()
	{
		chartLabels.add("11~12"); 
		chartLabels.add("12~13"); 
		chartLabels.add("13~14"); 
		chartLabels.add("14~15"); 
		chartLabels.add("15~16"); 
		//		chartLabels.add(""); 
		//		chartLabels.add(""); 
		//		chartLabels.add("9:00~10:00"); 
		//		chartLabels.add("10:00~11:00"); 
		//		chartLabels.add("11:00~12:00"); 
		//		chartLabels.add("12:00~13:00"); 
	}	

	//	private int calcAvg()
	//	{
	//		return (98+100+95+100)/4;
	//	}

	@Override
	public void render(Canvas canvas) {
		try{
			chart.render(canvas);
		} catch (Exception e){
			Log.e(TAG, e.toString());
		}
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
