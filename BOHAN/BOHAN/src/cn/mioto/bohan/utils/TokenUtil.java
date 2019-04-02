package cn.mioto.bohan.utils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import steed.framework.android.client.SimpleTokenEngine;
import steed.framework.android.client.Token;
import steed.framework.android.util.LogUtil;
import steed.util.system.TaskUtil;

/**
 * access_token工具类，若想获取access_token必须使用该类的getAccessToken方法，
 * 不允许另写方法。
 * @author 战马
 *
 */
@SuppressLint("NewApi")
public class TokenUtil {
	private static Token token = null;
//	private static final Map<String, AccessToken> accessTokenMap = new HashMap<String, AccessToken>();
	
	private static TokenEngine tokenEngine = (TokenEngine) new SimpleTokenEngine();
//	private static long lastRefreshTime = 0L;
	
	public static TokenEngine getTokenEngine() {
		return tokenEngine;
	}
	public static void setTokenEngine(TokenEngine tokenEngine) {
		TokenUtil.tokenEngine = tokenEngine;
	}
	
	public static void init(final Runnable callBack){
		refreshToken();
		new Thread(new Runnable() {
			@Override
			public void run() {
				long time = new Date().getTime();
				while (!isAccessTokenValid() && new Date().getTime() - time < 1000*3) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if (!isAccessTokenValid()) {
					LogUtil.w("刷新token超时!");
				}
				callBack.run();
			}
		}).start();
		startRefreshTask();
	}
	private static void startRefreshTask() {
		TaskUtil.startTask(new Runnable() {
			@Override
			public void run() {
				refreshToken();
				TaskUtil.startTask(this, 1, TimeUnit.HOURS);
			}
		}, 1, TimeUnit.HOURS);
	}
	
	public static void setToken(Token token) {
		TokenUtil.token = token;
	}
	/**
	 * 刷新token
	 */
	public static void refreshToken() {
	/*	long time2 = new Date().getTime();
		if (time2 < 1000  + lastRefreshTime ) {
			LogUtil.d("token刷新频繁,跳过");
			return;
		}
		lastRefreshTime = time2;*/
		tokenEngine.getToken();
	}

	/**
	 * access_token是否还有效
	 * @return access_token是否还有效
	 */
	private static boolean isAccessTokenValid(Token data){
		if (data == null) {
			return false;
		}
		if (data.getExpires_in() <= 0) {
			return false;
		}else {
			long timePastAfterGetAccessToken = new Date().getTime() - data.getAccess_token_getTime();
			/**
			 * 这里允许有15分钟的误差
			 */
			if (timePastAfterGetAccessToken < (data.getExpires_in()-60*15)*1000) {
				return true;
			}else {
				return false;
			}
		}
	}
	public static boolean isAccessTokenValid(){
		return isAccessTokenValid(getToken());
	}
	

	/**
	 * 获取未过期的access_token
	 * @return 未过期的access_token,如失败则AccessToken.access_token值为null
	 */
	public static Token getToken() {
	/*	while (token == null) {
			refreshToken();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}*/
		/*if (!isAccessTokenValid(token)) {
			lastRefreshTime = 0L;
			refreshToken();
		}*/
		return token;
	}
	
	/**
	 * 获取未过期的access_token,本工具类已经实现了access_token缓存，一般不用改方法，请用getAccessToken()
	 * @param data 之前的AccessToken，为null时将重新获取AccessToken.access_token值为null也会重新获取
	 * @return 未过期的access_token,如失败则AccessToken.access_token值为null
	 */
/*	public static AccessToken getToken(Token data){
		if (data == null) {
			data = new Token("", 0, 0);
		}
		if (!isAccessTokenValid(data) || StringUtil.isStringEmpty(data.getAccess_token())) {
			logger.debug("access_token过期，重新获取");
			return WechatInterfaceInvokeUtil.getAccessToken();
		}
		return data;
	}*/
	
	public interface TokenEngine{
		public void getToken();
	}
	
	
}
