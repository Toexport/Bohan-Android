/**
 * Copyright 2014  XCL-Charts
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 	
 * @Project XCL-Charts 
 * @Description Android图表基类库
 * @author XiongChuanLiang<br/>(xcl_168@aliyun.com)
 * @Copyright Copyright (c) 2014 XCL-Charts (www.xclcharts.com)
 * @license http://www.apache.org/licenses/  Apache v2 License
 * @version 1.0
 */
package cn.mioto.bohan.view;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.xclcharts.chart.BarChart;
import org.xclcharts.chart.BarData;
import org.xclcharts.chart.CustomLineData;
import org.xclcharts.common.DensityUtil;
import org.xclcharts.common.IFormatterDoubleCallBack;
import org.xclcharts.common.IFormatterTextCallBack;
import org.xclcharts.event.click.BarPosition;
import org.xclcharts.renderer.XEnum;

import cn.mioto.bohan.Constant;
import cn.mioto.bohan.R;
import cn.mioto.bohan.utils.LogUtilNIU;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

/**
 * @ClassName BarChart02View
 * @Description 柱形图例子(横向)
 * @author XiongChuanLiang<br/>
 *         (xcl_168@aliyun.com)
 */
public class BarChart02View extends BaseChartView implements Runnable {

	private static final String TAG = "BarChart02View";
	private BarChart chart = new BarChart();
	// 轴数据源
	/*
	 * 装x标签数据的容器
	 */
	private List<String> chartLabels = new LinkedList<String>();// 使用link可以随意地实现删除
	private List<BarData> chartData = new LinkedList<BarData>();
	private List<CustomLineData> mCustomLineDataset = new LinkedList<CustomLineData>();
	private List<Double> dataSeriesA = new ArrayList<>();// 数据的集合
	private List<Double> powerDatas = new LinkedList<>();// 数据的集合
	private List<Integer> dataColorA = new LinkedList<>();// 颜色的集合

	public BarChart02View(Context context) {
		super(context);
	}

	public BarChart02View(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public BarChart02View(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	// -------------以下是我重写的，供外部调用的方法-----------------------------------------------------------------
	/**
	 * @Title: setXlables
	 * @Description:对外提供的方法，这是横坐标标签
	 * @param xLables
	 *            标签集合，为String字符串集合
	 * @return void
	 * @throws
	 */
	public void setXlables(List<String> xLables) {
		chartLabels.clear();// 清空数据
		for (String l : xLables) {
			chartLabels.add(l);
		}
		this.invalidate();
	}

	/**
	 * @Title: setBarColor
	 * @Description:设置标签颜色和数据
	 * @param xLables
	 *            标签集合
	 * @return void
	 * @throws
	 */
	public void setBarColorAndData(List<String> color, List<Double> datas,
			Double max) {
		// 初始化柱子颜色
		dataColorA.clear();
		int barColor = getResources().getColor(R.color.chartColor);
		for (int i = 0; i < color.size(); i++) {
			if (color.get(i).equals("dark")) {// 当下标为12的时候
				barColor = getResources().getColor(R.color.chartColor2);// 显示为第二种颜色
			}
			dataColorA.add(barColor);
		}
		/***************************************************/
		// 初始化数据
		powerDatas.clear();
		powerDatas.addAll(datas);
		chartData.clear();
		chartData.add(new BarData("", powerDatas, dataColorA, Color.rgb(53,
				169, 239)));

		chartRender(max);
		// 綁定手势滑动事件
		// this.bindTouch(this,chart);
		new Thread(this).start();
	}

	private void chartRender(Double max) {
		try {
			// 设置绘图区默认缩进px值,留置空间显示Axis,Axistitle....
			int[] ltrb = getBarLnDefaultSpadding();// 基类设定的值
			chart.setPadding(ltrb[0], ltrb[1], ltrb[2], ltrb[3]);// 加入到设定
			chart.getBar().getItemLabelPaint()
					.setTextSize(Constant.CHART_DATA_TEXT_SIZE);
			chart.getCategoryAxis().getTickLabelPaint()
					.setTextSize(Constant.CHART_X_TEXT_SIZE);
			chart.getDataAxis().getTickLabelPaint()
					.setTextSize(Constant.CHART_Y_TEXT_SIZE);

			// 标题
			// chart.setTitle(getResources().getString(R.string.all_device_last_12_hours)+"(kWh)");
			// 数据源
			// chart.setDataSource(chartData);
			chart.setCategories(chartLabels);
			chart.setCustomLines(mCustomLineDataset);

			// 图例
			chart.getAxisTitle().setTitleStyle(XEnum.AxisTitleStyle.ENDPOINT);// 右下角
			/******************************************* TODO 先去掉下单位 ********/
			// 设置右下
			// chart.getAxisTitle().setLowerTitle(getResources().getString(R.string.hour));
			// 数据轴取值范围
			chart.getDataAxis().setAxisMax(max);
			LogUtilNIU.value("内部最大范围" + max);
			chart.getDataAxis().setAxisMin(0);
			chart.getDataAxis().setAxisSteps(max / 5);// 步长，需要四舍五入
			// 指隔多少个轴刻度(即细刻度)后为主刻度
			// chart.getDataAxis().setDetailModeSteps(0.05);
			// 背景网格
			chart.getPlotGrid().showHorizontalLines();
			chart.getPlotGrid()
					.getHorizontalLinePaint()
					.setColor(
							getResources().getColor(
									R.color.chart_grid_line_color));

			// 定义数据轴标签显示格式
			chart.getDataAxis().setLabelFormatter(new IFormatterTextCallBack() {

				@Override
				public String textFormatter(String value) {
					// TODO Auto-generated method stub
					Double tmp = Double.parseDouble(value);
					DecimalFormat df = new DecimalFormat("#0.00");// 显示数据保留的位数
					String label = df.format(tmp).toString();
					return (label);
				}
			});

			chart.getBar().setItemLabelVisible(true);// 在柱形顶部显示值
			chart.getBar().setBarStyle(XEnum.BarStyle.OUTLINE);
			chart.getBar().setBorderWidth(2);
			chart.getBar().setOutlineAlpha(500);// 设置柱子颜色透明度

			// chart.getBar().setBarRoundRadius(5);
			// chart.getBar().setBarStyle(XEnum.BarStyle.ROUNDBAR);
			// TODO 设置为可以左右拉
			chart.setPlotPanMode(XEnum.PanMode.HORIZONTAL);
			chart.disableScale();// 不可以缩放
			// chart.disablePanMode();

			// 设定格式
			chart.setItemLabelFormatter(new IFormatterDoubleCallBack() {
				@Override
				public String doubleFormatter(Double value) {
					DecimalFormat df = new DecimalFormat("#0.00");
					String label = df.format(value).toString();
					return label;
				}
			});

			// 隐藏Key
			chart.getPlotLegend().hide();
			chart.getClipExt().setExtTop(150.f);
			// 柱形和标签居中方式
			chart.setBarCenterStyle(XEnum.BarCenterStyle.TICKMARKS);
			chart.getPlotTitle().getTitlePaint().setTextSize(22);
			// 隐藏纵坐标轴
			chart.getDataAxis().hideAxisLine();
			// 隐藏横坐标轴
			chart.getCategoryAxis().hideAxisLine();
			// 隐藏刻度线
			chart.getDataAxis().hideTickMarks();
			chart.getCategoryAxis().hideTickMarks();
			// 设置横坐标标签和X轴间距
			int margin = 20;
			chart.getCategoryAxis().setTickLabelMargin(margin);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void initView() {
		chartLabels();
		chartDataSet();
		chartRender();

		// 綁定手势滑动事件
		this.bindTouch(this, chart);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		// 图所占范围大小
		if (null != chart)
			chart.setChartRange(w, h);
	}

	private void chartRender() {
		try {
			// 设置绘图区默认缩进px值,留置空间显示Axis,Axistitle....
			int[] ltrb = getBarLnDefaultSpadding();
			chart.setPadding(DensityUtil.dip2px(getContext(), 50), ltrb[1],
					ltrb[2], ltrb[3]);

			chart.setTitle("每日收益情况");
			chart.addSubtitle("(XCL-Charts Demo)");
			chart.setTitleVerticalAlign(XEnum.VerticalAlign.MIDDLE);
			chart.setTitleAlign(XEnum.HorizontalAlign.LEFT);

			// 数据源
			// chart.setDataSource(chartData);
			chart.setCategories(chartLabels);
			chart.setCustomLines(mCustomLineDataset);
			// 轴标题
			chart.getAxisTitle().setLeftTitle("所售商品");
			chart.getAxisTitle().setLowerTitle("纯利润(天)");
			chart.getAxisTitle().setRightTitle("生意兴隆通四海,财源茂盛达三江。");

			// 数据轴
			chart.getDataAxis()
					.setAxisMax(Constant.CHART_BART_HOUR_DEFAULT_MAX);
			chart.getDataAxis().setAxisMin(0);
			chart.getDataAxis().setAxisSteps(
					Constant.CHART_BART_HOUR_DEFAULT_MAX / 5);

			chart.getDataAxis().getTickLabelPaint()
					.setColor(Color.rgb(199, 88, 122));

			chart.getDataAxis().setLabelFormatter(new IFormatterTextCallBack() {

				@Override
				public String textFormatter(String value) {
					// TODO Auto-generated method stub
					String tmp = value + "万元";
					return tmp;
				}

			});

			// 网格
			chart.getPlotGrid().showHorizontalLines();
			chart.getPlotGrid().showVerticalLines();
			chart.getPlotGrid().showEvenRowBgColor();

			// 标签轴文字旋转-45度
			chart.getCategoryAxis().setTickLabelRotateAngle(-45f);
			// 横向显示柱形
			chart.setChartDirection(XEnum.Direction.HORIZONTAL);
			// 在柱形顶部显示值
			chart.getBar().setItemLabelVisible(true);// 在柱形顶部显示值
			chart.getBar().setBarStyle(XEnum.BarStyle.OUTLINE);
			chart.getBar().setBorderWidth(2);
			chart.getBar().setOutlineAlpha(400);// 设置柱子颜色透明度
			chart.disableScale();// 不可以缩放
			chart.getPlotTitle().getTitlePaint().setTextSize(22);
			chart.setItemLabelFormatter(new IFormatterDoubleCallBack() {
				@Override
				public String doubleFormatter(Double value) {
					// TODO Auto-generated method stub
					DecimalFormat df = new DecimalFormat("#0.00");
					String label = df.format(value).toString();
					return label;
				}
			});

			// 激活点击监听
			chart.ActiveListenItemClick();
			chart.showClikedFocus();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e(TAG, e.toString());
		}

	}

	private void chartDataSet() {
		dataSeriesA = new LinkedList<Double>();
		dataSeriesA.add(0.00);
		dataSeriesA.add(0.00);
		dataSeriesA.add(0.00);
		dataSeriesA.add(0.00);
		dataSeriesA.add(0.00);
		dataSeriesA.add(0.00);

		// 依数据值确定对应的柱形颜色.
		dataColorA = new LinkedList<Integer>();
		dataColorA.add(getResources().getColor(R.color.chartColor));
		dataColorA.add(getResources().getColor(R.color.chartColor));
		dataColorA.add(getResources().getColor(R.color.chartColor));
		dataColorA.add(getResources().getColor(R.color.chartColor));
		dataColorA.add(getResources().getColor(R.color.chartColor));
		dataColorA.add(getResources().getColor(R.color.chartColor));
		chartData.clear();
		chartData.add(new BarData("", dataSeriesA, dataColorA, Color.rgb(53,
				169, 239)));
	}

	private void chartLabels() {
		chartLabels.add("");
		chartLabels.add("");
		chartLabels.add("");
		chartLabels.add("");
		chartLabels.add("");
	}

	@Override
	public void render(Canvas canvas) {
		try {
			chart.render(canvas);
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		super.onTouchEvent(event);
		if (event.getAction() == MotionEvent.ACTION_UP) {
			triggerClick(event.getX(), event.getY());
		}
		return true;
	}

	// 触发监听
	private void triggerClick(float x, float y) {
		BarPosition record = chart.getPositionRecord(x, y);
		if (null == record)
			return;

		BarData bData = chartData.get(record.getDataID());
		Double bValue = bData.getDataSet().get(record.getDataChildID());

		Toast.makeText(
				this.getContext(),
				"info:" + record.getRectInfo() + " Key:" + bData.getKey()
						+ " Current Value:" + Double.toString(bValue),
				Toast.LENGTH_SHORT).show();

		chart.showFocusRectF(record.getRectF());
		chart.getFocusPaint().setStyle(Style.STROKE);
		chart.getFocusPaint().setStrokeWidth(3);
		chart.getFocusPaint().setColor(Color.GREEN);
		this.invalidate();
	}

	@Override
	public void run() {
		try {
			chartAnimation();
		} catch (Exception e) {
			Thread.currentThread().interrupt();
		}
	}

	private void chartAnimation() {
		try {

			for (int i = 0; i < chartData.size(); i++) {
				BarData barData = chartData.get(i);
				for (int j = 0; j < barData.getDataSet().size(); j++) {
					Thread.sleep(100);
					List<BarData> animationData = new LinkedList<BarData>();
					List<Double> dataSeries = new LinkedList<Double>();
					List<Integer> dataColorA = new LinkedList<Integer>();
					for (int k = 0; k <= j; k++) {
						dataSeries.add(barData.getDataSet().get(k));
						dataColorA.add(barData.getDataColor().get(k));
					}

					BarData animationBarData = new BarData("", dataSeries,
							dataColorA, Color.rgb(53, 169, 239));
					animationData.add(animationBarData);
					chart.setDataSource(animationData);
					postInvalidate();
				}
			}

		} catch (Exception e) {
			Thread.currentThread().interrupt();
		}
	}
}
