package cn.mioto.bohan.utils;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import cn.mioto.bohan.R;

/** 
* 类说明：toolbar的设置类
* 靠！自己写出来的，过了两个月竟然不记得
* setToolbarTitle这个方法小心使用，有可能导致title不改变，原因是这个title在这里现在是全局的
* 所以会供用，在activity切换时，失效。
* AnyWay，一旦出现设置失效情况，在activity声明这个title在全局就好，也就是findViewById
* 
* 文本框的工具类
*/
public class ViewUtil {
	public static Toolbar toolbar;
	public static TextView titleTextView;
	/**
	* 
	* @Title: initToolbar 
	* @Description: TODO(设置toolbar标题内容，设置是否带返回键) 
	* @param @param activity
	* @param @param titleString
	* @param @param hasBackButton
	* @param @param hasMenu
	* @param @param menuTitleString
	* @param @return    设定文件 
	* @return Toolbar    返回类型 
	* @throws
	 */
    public static Toolbar initToolbar(final AppCompatActivity activity,String titleString, Boolean hasBackButton, Boolean hasMenu,int menuTitleStringId) {
        toolbar = (Toolbar) activity.findViewById(R.id.toolbar);
        titleTextView = (TextView) activity.findViewById(R.id.toolbar_title);
        titleTextView.setText(titleString);
        if (hasMenu){
            TextView menuTextView = (TextView) activity.findViewById(R.id.menu_tv);
            menuTextView.setText(menuTitleStringId);
        }else if(!hasMenu){//如果没有菜单
        	TextView menuTextView = (TextView) activity.findViewById(R.id.menu_tv);
        	menuTextView.setVisibility(View.GONE);//不可见
        }
        activity.setSupportActionBar(toolbar);
        if (hasBackButton) {
            android.support.v7.app.ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowTitleEnabled(false);
                toolbar.setNavigationIcon(R.drawable.back);
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        activity.finish();
                    }
                });
            }
        } else {
            android.support.v7.app.ActionBar actionBar = activity.getSupportActionBar();
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
        return toolbar;
    }
	
	
	/**
	* 
	* @Title: initToolbar 
	* @Description: TODO(设置toolbar标题内容，设置是否带返回键) 
	* @param @param activity
	* @param @param titleString
	* @param @param hasBackButton
	* @param @param hasMenu
	* @param @param menuTitleString
	* @param @return    设定文件 
	* @return Toolbar    返回类型 
	* @throws
	 */
    public static Toolbar initToolbar(final AppCompatActivity activity,int titleString, Boolean hasBackButton, Boolean hasMenu,int menuTitleStringId) {
        toolbar = (Toolbar) activity.findViewById(R.id.toolbar);
        titleTextView = (TextView) activity.findViewById(R.id.toolbar_title);
        titleTextView.setText(titleString);
        if (hasMenu){
            TextView menuTextView = (TextView) activity.findViewById(R.id.menu_tv);
            menuTextView.setText(menuTitleStringId);
        }else if(!hasMenu){//如果没有菜单
        	TextView menuTextView = (TextView) activity.findViewById(R.id.menu_tv);
        	menuTextView.setVisibility(View.GONE);//不可见
        }
        activity.setSupportActionBar(toolbar);
        if (hasBackButton) {
            android.support.v7.app.ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowTitleEnabled(false);
                toolbar.setNavigationIcon(R.drawable.back);
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        activity.finish();
                    }
                });
            }
        } else {
            android.support.v7.app.ActionBar actionBar = activity.getSupportActionBar();
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
        return toolbar;
    }
    
    
    /**
	* 
	* @Title: initToolbar 
	* @Description: TODO(设置toolbar标题内容，设置是否带返回键) 
	* @param @param activity
	* @param @param titleString
	* @param @param hasBackButton
	* @param @param hasMenu
	* @param @param menuTitleString
	* @param @return    设定文件 
	* @return Toolbar    返回类型 
	* @throws
	 */
    public static Toolbar initToolbar(final AppCompatActivity activity,int titleString, Boolean hasBackButton, Boolean hasMenu,int menuDrawable,int mode) {
        toolbar = (Toolbar) activity.findViewById(R.id.toolbar);
        titleTextView = (TextView) activity.findViewById(R.id.toolbar_title);
        titleTextView.setText(titleString);
        if (hasMenu){
            TextView menuTextView = (TextView) activity.findViewById(R.id.menu_tv);
            menuTextView.setBackgroundResource(menuDrawable);
        }else if(!hasMenu){//如果没有菜单
        	TextView menuTextView = (TextView) activity.findViewById(R.id.menu_tv);
        	menuTextView.setVisibility(View.GONE);//不可见
        }
        activity.setSupportActionBar(toolbar);
        if (hasBackButton) {
            android.support.v7.app.ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowTitleEnabled(false);
                toolbar.setNavigationIcon(R.drawable.back);
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        activity.finish();
                    }
                });
            }
        } else {
            android.support.v7.app.ActionBar actionBar = activity.getSupportActionBar();
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
        return toolbar;
    }

	/**
	* 
	* @Title: initToolbar 
	* @Description: TODO(设置toolbar标题内容，设置是否带返回键) 
	* @param @param activity
	* @param @param id
	* @param @param titleId
	* @param @param titleString
	* @param @param hasBackButton
	* @param @param hasMenu
	* @param @param menuID
	* @param @param menuTitleString
	* @param @return    设定文件 
	* @return Toolbar    返回类型 
	* @throws
	 */
    public static Toolbar initToolbar(final AppCompatActivity activity, int id, int titleId, int titleString, Boolean hasBackButton, Boolean hasMenu,int menuID,int menuTitleString) {
        toolbar = (Toolbar) activity.findViewById(id);
        titleTextView = (TextView) activity.findViewById(titleId);
        titleTextView.setText(titleString);
        if (hasMenu){
            TextView menuTextView = (TextView) activity.findViewById(menuID);
            menuTextView.setText(menuTitleString);
        }else if(!hasMenu){//如果没有菜单
        	TextView menuTextView = (TextView) activity.findViewById(R.id.menu_tv);
        	menuTextView.setVisibility(View.GONE);//不可见
        }
        activity.setSupportActionBar(toolbar);
        if (hasBackButton) {
            android.support.v7.app.ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowTitleEnabled(false);
                toolbar.setNavigationIcon(R.drawable.back);
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        activity.finish();
                    }
                });
            }
        } else {
            android.support.v7.app.ActionBar actionBar = activity.getSupportActionBar();
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
        return toolbar;
    }
    
    public static void setToolbarTitle(String string){
    	titleTextView.setText(string);
    	LogUtilNIU.value("设置的标题为"+string+"titleTextView的ID是"+titleTextView.getId());
    }

//    /**
//     * 
//    * @Title: lengthFilter 
//    * @Description: TODO(EditText、文本框、输入框最大输入数提示 都可以调用此方法。在onCreat()中调用) 
//    * @param @param context
//    * @param @param editText
//    * @param @param max_length
//    * @param @param err_msg    设定文件 
//    * @return void    返回类型 
//    * @throws
//     */
//    public static void lengthFilter(final Context context, final EditText editText, final int max_length, final String err_msg) {
//
//        InputFilter[] filters = new InputFilter[1];
//
//        filters[0] = new InputFilter.LengthFilter(max_length) {
//
//            @Override
//            public CharSequence filter(CharSequence source, int start, int end,
//                                       Spanned dest, int dstart, int dend) {
//
//                int destLen = getCharacterNum(dest.toString()); //获取字符个数(一个中文算2个字符)
//                int sourceLen = getCharacterNum(source.toString());
//                if (destLen + sourceLen > max_length) {
//                    Toast.makeText(context, err_msg, Toast.LENGTH_SHORT).show();
//                    return "";
//                }
//                return source;
//            }
//        };
//        editText.setFilters(filters);
//    }

//    /**
//     * @param content
//     * @return
//     * @description 获取一段字符串的字符个数（包含中英文，一个中文算2个字符）
//     */
//    public static int getCharacterNum(final String content) {
//        if (null == content || "".equals(content)) {
//            return 0;
//        } else {
//            return (content.length() + getChineseNum(content));
//        }
//    }

//    /**
//     * @param s
//     * @return
//     * @description 返回字符串里中文字或者全角字符的个数
//     */
//    public static int getChineseNum(String s) {
//
//        int num = 0;
//        char[] myChar = s.toCharArray();
//        for (int i = 0; i < myChar.length; i++) {
//            if ((char) (byte) myChar[i] != myChar[i]) {
//                num++;
//            }
//        }
//        return num;
//    }

	
}
