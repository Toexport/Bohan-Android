package cn.mioto.bohan.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/** 
 * 类说明：自定义不可左右滑动的ViewPager
 * 作者：  jiemai liangminhua 
 * 创建时间：2016年5月26日 下午4:40:12 
 */
public class CustomViewPager extends ViewPager {
	//控制是否能滑动
	private boolean isCanScroll = false;  
	public CustomViewPager(Context context) {
		super(context);
	}

	public CustomViewPager(Context context, AttributeSet attrs) {  
        super(context, attrs);  
    } 
	public void setScanScroll(boolean isCanScroll) {  
        this.isCanScroll = isCanScroll;  
    } 
	
	@Override  
    public void scrollTo(int x, int y) {  
            super.scrollTo(x, y);  
    }  
	
	@Override  
    public boolean onTouchEvent(MotionEvent arg0) {  
        return false;
    }
	@Override  
    public void setCurrentItem(int item, boolean smoothScroll) {  
        super.setCurrentItem(item, smoothScroll);  
    }  
	
	@Override  
    public void setCurrentItem(int item) {  
        super.setCurrentItem(item);  
    }  
}
