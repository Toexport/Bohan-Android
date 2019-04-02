package cn.mioto.bohan.entity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;


/**
 * shared preference 字段名
 *
 * @author
 */
public class Config {
    public static final String FILE = "config";

    public static final String ACCOUNT = "account";
    public static final String PASSWORD = "password";
    public static final String NICKNAME = "nickname";
    public static final String USERID = "userId";
    public static final String HEADIMG = "headimg";
    public static final String RECENT_ACCOUNT = "recent_account";
    public static final String REVIEW = "review";
    public static final String HEIGHT = "height";
    public static final String LOGIN_TYPE = "login_type";
    public static final String BINDED = "binded";
    public static final String FRIST_RUNING = "frist_runing";
    public static final String MOBILE = "mobile";
    public static final String USERNAME = "username";
    public static final String TOKEN = "token";
    public static final String EQU_PWS = "equ_password";
    public static final String LONGITUDE = "longitude";
    public static final String LATITUDE = "latitude";
    public static final String ADDRESS = "address";
    public static final String POLE_HIGHT = "pole_hight";
    public static final String CAR_MODEL_ID = "car_model_id";
    public static final String OUTTRADENO = "out_trade_no";
    public static final String USERBALANCE = "UserBalance";
    public static final String CHARGESTARTTIME = "charge_start_time";
    public static final String WIFIPWD = "wifi_pwd";
    public static final String WIFINAME = "wifi_name";
    public static final String LANGUAGE ="zh"; 

    /**
     * 声音提醒是否打开
     */
    public static final String VOICE_ALERT = "voice_alert";
    /**
     * 振动提醒是否打开
     */
    public static final String VIBRATE_ALERT = "vibrate_alert";
    /**
     * 声音类型
     */
    public static final String VOICE_TYPE = "voice_type";


    /**
     * 审核状态
     */
    public static final int REVIEW_NONE = 0;
    public static final int REVIEW_DOING = 1;
    public static final int REVIEW_PASS = 2;
    public static final int REVIEW_NOT_PASS = 3;
    public static final int REVIEW_MODITY = 4;

    /**
     * 登录类型
     */
    public static final int LOGIN_YT = 0;
    public static final int LOGIN_QQ = 1;
    public static final int LOGIN_WB = 2;


    /**
     * 判断是否已登陆
     *
     * @param context
     * @return
     */
    public static boolean isLogined(Context context) {
        boolean isLogin = false;
        SharedPreferences sharedPref = context.getSharedPreferences(Config.FILE, Context.MODE_PRIVATE);
        String userId = sharedPref.getString(USERID, "");
        if (!TextUtils.isEmpty(userId)) {
            isLogin = true;
        }
        return isLogin;
    }

    public static String getUserId(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(Config.FILE, Context.MODE_PRIVATE);
        return sharedPref.getString(USERID, "");
    }

    public static String getToken(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(Config.FILE, Context.MODE_PRIVATE);
        return sharedPref.getString(TOKEN, "");
    }

    public static String getHeadimg(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(Config.FILE, Context.MODE_PRIVATE);
        return sharedPref.getString(HEADIMG, "");
    }

    public static String getNickname(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(Config.FILE, Context.MODE_PRIVATE);
        return sharedPref.getString(HEADIMG, "");
    }

    /**
     * 经度
     *
     * @param context
     * @return
     */
    public static String getLongitude(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(Config.FILE, Context.MODE_PRIVATE);
        return sharedPref.getString(LONGITUDE, "0");
    }

    /**
     * 纬度
     *
     * @param context
     * @return
     */
    public static String getLatitude(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(Config.FILE, Context.MODE_PRIVATE);
        return sharedPref.getString(LATITUDE, "0");
    }

    /**
     * 保存上一次位置
     *
     * @param longitude
     * @param latitude
     */
    public static void saveCoordinate(Context context, String longitude, String latitude,String address) {
        SharedPreferences sharedPref = context.getSharedPreferences(Config.FILE, Context.MODE_PRIVATE);
        Editor editor = sharedPref.edit();
        editor.putString(LONGITUDE, longitude);
        editor.putString(LATITUDE, latitude);
        editor.putString(ADDRESS,address);
        editor.commit();
    }


    /**
     * 获取上次定位地址信息
     * @param context
     * @return
     */
    public static String getLastAddress(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(Config.FILE, Context.MODE_PRIVATE);
        return sharedPref.getString(ADDRESS, "");
    }


    public static void logout(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(Config.FILE, Context.MODE_PRIVATE);
        Editor editor = sharedPref.edit();
        editor.remove(Config.ACCOUNT);
        editor.remove(Config.PASSWORD);
        editor.remove(Config.USERID);
        editor.remove(Config.USERNAME);
        editor.remove(Config.HEADIMG);
        editor.remove(Config.NICKNAME);
        editor.remove(Config.MOBILE);
        editor.remove(Config.POLE_HIGHT);
        editor.commit();
    }
}
