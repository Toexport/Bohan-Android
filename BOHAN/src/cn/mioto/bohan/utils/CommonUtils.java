package cn.mioto.bohan.utils;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

/** 
* 类说明：通用工具类
* 作者：  jiemai liangminhua 
* 创建时间：2016年8月9日 上午10:04:53 
*/
public class CommonUtils {
	 //    隐藏软键盘
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    public static void copy(Context context, TextView tv) {
        ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setText(tv.getText().toString());
    }
}
