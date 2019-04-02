package cn.mioto.bohan.view.timepicker;

import android.app.Activity;

/** 
* 类说明：
* 作者：  jiemai liangminhua 
* 联系方式: elecat@126.com, QQ:349635073
* 创建时间：2016年10月13日 上午11:37:27 
*/
public class NumberPicker extends OptionPicker{

    /**
     * Instantiates a new Number picker.
     *
     * @param activity the activity
     */
    public NumberPicker(Activity activity) {
        super(activity, new String[]{});
    }

    /**
     * Sets range.
     *
     * @param startNumber the start number
     * @param endNumber   the end number
     */
    public void setRange(int startNumber, int endNumber) {
        setRange(startNumber, endNumber, 1);
    }

    /**
     * Sets range.
     *
     * @param startNumber the start number
     * @param endNumber   the end number
     * @param step        the step
     */
    public void setRange(int startNumber, int endNumber, int step) {
        for (int i = startNumber; i <= endNumber; i = i + step) {
            options.add(String.valueOf(i));
        }
    }

    /**
     * Sets selected item.
     *
     * @param number the number
     */
    public void setSelectedItem(int number) {
        setSelectedItem(String.valueOf(number));
    }



}
