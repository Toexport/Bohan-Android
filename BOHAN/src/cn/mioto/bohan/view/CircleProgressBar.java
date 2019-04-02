package cn.mioto.bohan.view;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;

/**
 * 绘制时根据自身高度形成一个正方形,在正方形中绘制圆  可以根据高度自适应大小
 * 
 * canvas.drawCircle() 第三个参数为绘制时圆的半径
 * 
 * 圆的路径宽度/边框宽度 是指  圆形的外部与内部 宽度相加之和
 */
/**
 * 环形的ProgressBar
 * @author liling
 *
 */
public class CircleProgressBar extends View {
	//[start]---参数-----------------------------------------------------
	private float progress = 0;
	private int max = 0;//为了使动画更加流畅  动画是由(progress+1)形成

	/**
	 *  环/画笔 的路径宽度
	 */
	private int pathWidth = 8;
	/**
	 *  默认圆的半径
	 */
	private int radius = 60;

	private int width;
	private int height;

	/**
	 *  梯度渐变的填充颜色
	 */
	private int[] arcColors = new int[] {0xFF3CB0F5,0xFF3EABF5,0xFF3DADF6,0xFF3BB2F6,0xFF33C7F3,0xFF2ED2F3,0xFF29DDF3,0xFF29E1F5,0xFF27E2F1,0xFF29E1F3,0xFF3CB0F5
	};
	/**
	 * 阴影颜色组
	 */
	private int[] shadowsColors = new int[] { 0xFF111111, 0x00AAAAAA,0x00AAAAAA };
	// 轨迹绘制点
	private Paint pathPaint = null;
	// 绘制填充
	private Paint fillArcPaint = null;
	private RectF oval;
	// 灰色轨迹
	private int pathColor =  0xFFCCEBFF;//0xFFF0EEDF 圆环底色
	private int pathBorderColor = 0xFFFFFFFF;//原本灰色的小环颜色

	// 指定了光源的方向和环境光强度来添加浮雕效果	
	private EmbossMaskFilter emboss = null;
	// 设置光源的方向
	float[] direction = new float[] { 1, 1, 1 };
	// 设置环境光亮度
	float light = 0.4f;
	// 选择要应用的反射等级
	float specular = 6;
	// 向 mask应用一定级别的模糊
	float blur = 3.5f;
	// 指定了一个模糊的样式和半径来处理 Paint 的边缘
	private BlurMaskFilter mBlur = null;
	// view重绘的标记
	private boolean reset = false;
	//[end]---参数-----------------------------------------------------	
	public CircleProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		pathPaint = new Paint();//用来话路径的画笔
		pathPaint.setAntiAlias(true);//是否抗锯齿
		pathPaint.setFlags(Paint.ANTI_ALIAS_FLAG);// 帮助消除锯齿
		pathPaint.setStyle(Paint.Style.STROKE);// 设置中空的样式
		pathPaint.setDither(true);
		pathPaint.setStrokeJoin(Paint.Join.ROUND);
		fillArcPaint = new Paint();//填充画笔
		fillArcPaint.setAntiAlias(true);// 设置是否抗锯齿
		fillArcPaint.setFlags(Paint.ANTI_ALIAS_FLAG);// 帮助消除锯齿
		fillArcPaint.setStyle(Paint.Style.STROKE);//中空的样式
		fillArcPaint.setDither(true);
		fillArcPaint.setStrokeJoin(Paint.Join.ROUND);

		oval = new RectF();
		emboss = new EmbossMaskFilter(direction, light, specular, blur);
		mBlur = new BlurMaskFilter(19, BlurMaskFilter.Blur.NORMAL);
	}
	
	

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (reset) {
			canvas.drawColor(Color.TRANSPARENT);//画布归为空白
			reset = false;
		}
		this.width = getMeasuredWidth();//得到VIEW的大小
		this.height = getMeasuredHeight();
		this.pathWidth =  (getMeasuredHeight() / 2) / 7;//这里设置环形宽度随屏幕大小变化 ****
		this.radius = getMeasuredHeight() / 2 - pathWidth;//默认圆的半径 会自动变化大小*****
		//[start]------渐变圆环白色底环------------------------
		pathPaint.setColor(pathColor);// 画笔
		pathPaint.setStrokeWidth(pathWidth);//圆环边框宽度
		pathPaint.setMaskFilter(emboss);//   浮雕效果
		int outerCircleRadius = (this.height/2)-pathWidth/2;//外环半径
		//[绘制]-渐变圆环底色圆	 在中心的地方画个半径为r的白色圆  半径与渐变圆环相同
		canvas.drawCircle(this.width / 2, this.height / 2, outerCircleRadius-1f, pathPaint);
		pathPaint.setStrokeWidth(0.5f);// 底色圆环边线//宽度
		pathPaint.setColor(pathBorderColor);//
		//[绘制]-渐变圆环底色圆边线-玻璃效果边 外
		canvas.drawCircle(this.width / 2, this.height / 2, height/2-0.5f, pathPaint);
		//[绘制]-渐变圆环底色圆边线-玻璃效果边 内
		canvas.drawCircle(this.width / 2, this.height / 2,(this.height/2)-pathWidth-0.5f, pathPaint);
		//[end]------渐变圆环底环------------------------
		//[start]------[渐变圆环]用来调颜色      定义环形颜色  填充  按照圆心扫描绘制-------------------------
		SweepGradient sweepGradient = new SweepGradient(this.width / 2,
				this.height / 2, arcColors, null);
		fillArcPaint.setShader(sweepGradient);//着色器
		fillArcPaint.setMaskFilter(mBlur);// 模糊效果
		fillArcPaint.setStrokeCap(Paint.Cap.ROUND);// 设置线类型,圆边
		fillArcPaint.setStrokeWidth(pathWidth);//圆环边框宽度
		//定义 渐变圆环 形状与大小    [详见文档-关于drawArc()]
		oval.set(this.width /2 - this.height/2+(pathWidth/2)+0.5f,				  //矩形左方位置	自身总宽度的一半减去自身高度的一半,加上圆环描边的宽度
				0+(pathWidth/2+0.5f),						  				  //矩形上方位置	自身顶部x轴位置减去圆环宽度的一半
				this.width-(this.width /2 - this.height/2)-(pathWidth/2)-1.5f,//矩形右方位置	自身宽度减去(总宽度的一半减去 高的一半即圆环半径)减去圆环宽度一半减去圆环边框总宽度
				this.height-(pathWidth/2)-0.5f);			  				  //矩形下方位置	高度减去圆环宽度一半减去圆环边框
		//[绘制]-渐变圆环  第一个参数为:定义形状和大小，第二个参数为：圆环起始角度/开始的地方，第三个为跨的角度/扫描角度/圆环显示的百分比，第四个为true的时候是实心，false的时候为空心
		canvas.drawArc(oval, -90, progress, false,
				fillArcPaint);
	}

	/**
	 * 
	 * 描述：获取圆的半径
	 * 
	 * @return
	 * @throws
	 */
	public int getRadius() {
		return radius;
	}
	/**
	 * 
	 * 描述：设置圆的半径
	 * 
	 * @param radius
	 * @throws
	 */
	public void setRadius(int radius) {
		this.radius = radius;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}
	/**
	 * 
	 * 描述：设置圆的进度
	 * 
	 * 这里有些问题，progress看能不能话成double更精确
	 * 
	 * @return
	 * @throws
	 */
	public void setProgress(float progress) {
		this.progress = progress;
		this.invalidate();
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int height = View.MeasureSpec.getSize(heightMeasureSpec);
		int width = View.MeasureSpec.getSize(widthMeasureSpec);
		setMeasuredDimension(width, height);
	}
}