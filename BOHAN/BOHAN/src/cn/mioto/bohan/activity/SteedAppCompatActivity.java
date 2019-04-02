package cn.mioto.bohan.activity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import steed.framework.android.annotation.NoClick;
import steed.framework.android.core.ActivityStack;
import steed.framework.android.core.ViewsHoldAble;
import steed.framework.android.core.activity.ImageGetedListener;
import steed.framework.android.util.AndroidReflectUtil;
import steed.framework.android.util.InjectUtil;
import steed.framework.android.util.LogUtil;
import steed.util.base.StringUtil;
import steed.util.other.ClassUtil;
import steed.util.reflect.ReflectUtil;

/** 
 * 类说明：Activity的AppCompatActivity版本
 */
public class SteedAppCompatActivity extends AppCompatActivity implements OnClickListener,ViewsHoldAble{
	protected View goBack;
	protected TextView activityTitle;
	protected static int requestCode_getImageFromAlbum = 233;
	protected static int requestCode_getImageFromCamera = 235;
	protected ImageView steedAlbumCache;
	private ImageGetedListener imageGetedListener;
	public static final String steedTitleKey = "steedTitle";
	public static final String dataKey = "data";

	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		/*if (ContextUtil.getApplicationContext() == null) {
			ContextUtil.setApplicationContext(this.getApplicationContext());
		}*/
		super.onCreate(savedInstanceState);
		View contentView  = getContentView();
		if (contentView == null) {
			Integer rvalue = getLayoutId();
			if (rvalue != null) {
				setContentView(rvalue);
			}
		}
		InjectUtil.findView(this);
		findViews();
		setListener();
		String stringExtra = getIntent().getStringExtra(steedTitleKey);
		if (activityTitle != null && !StringUtil.isStringEmpty(stringExtra)) {
			activityTitle.setText(stringExtra);
		}
		ActivityStack.pushActivity(this);
	}

	/**
	 * 从相机选择图片,选取图片后会调用onImageGeted,如果imageGetedListener不为空将会调用他的onImageGeted方法
	 * @see #onImageGeted
	 * @see #imageGetedListener
	 */
	public void getImageFromCamera() {
		startActivityForResult(new Intent("android.media.action.IMAGE_CAPTURE"), requestCode_getImageFromCamera);
	}
	public void getImageFromCamera(ImageGetedListener imageGetedListener) {
		getImageFromCamera();
		this.imageGetedListener = imageGetedListener;
	}

	protected Integer getLayoutId() {
		return AndroidReflectUtil.getRvalue("layout", "activity_"+ClassUtil.getClassSimpleName(this.getClass()).toLowerCase());
	}

	public void startActivity(Class<? extends Activity> activityClass){
		startActivity(activityClass,null);
	}
	public void startActivity(Class<? extends Activity> activityClass,String title){
		Intent intent = new Intent(this,activityClass);
		intent.putExtra(steedTitleKey, title);
		startActivity(intent);
	}

	public View getContentView(){
		return ((ViewGroup)getWindow().getDecorView().findViewById(android.R.id.content)).getChildAt(0);
	}

	@Override
	protected void onDestroy() {
		ActivityStack.popActivity(this);
		super.onDestroy();
	}

	/**
	 * 从相机选择图片,选取图片后会调用onImageGeted,如果imageGetedListener不为空将会调用他的onImageGeted方法
	 * @see #onImageGeted
	 * @see #imageGetedListener
	 */
	public void getImageFromAlbum() {  
		Intent intent;
		intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(intent, requestCode_getImageFromAlbum);
	}
	/**
	 * 从相机选择图片,选取图片后会调用onImageGeted,如果imageGetedListener不为空将会调用他的onImageGeted方法
	 * @see #onImageGeted
	 * @see #imageGetedListener
	 */
	public void getImageFromAlbum(ImageGetedListener imageGetedListener) {  
		this.imageGetedListener = imageGetedListener;
		getImageFromAlbum();
	}

	protected void onAlbumImageGeted(Bitmap image){
		if (steedAlbumCache != null) {
			steedAlbumCache.setImageBitmap(image);
		}
		if (imageGetedListener != null) {
			imageGetedListener.onImageGeted(image);
		}
		imageGetedListener = null;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == requestCode_getImageFromAlbum && -1 == resultCode) {
			ContentResolver resolver = this.getContentResolver();
			Uri uri = data.getData();
			try {
				onAlbumImageGeted(MediaStore.Images.Media.getBitmap(resolver, uri));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else if(requestCode == requestCode_getImageFromCamera && RESULT_OK == resultCode){
			Bitmap bm = (Bitmap) data.getExtras().get("data");
			if (bm != null) {
				onAlbumImageGeted(bm);
			}
		}
	}

	protected void setListener() {
		List<Field> allFields = ReflectUtil.getAllFields(this);
		for (Field temp:allFields) {
			if (View.class.isAssignableFrom(temp.getType())) {
				temp.setAccessible(true);
				try {
					View findViewById = (View) temp.get(this);
					if (findViewById != null) {
						if (temp.getAnnotation(NoClick.class) == null && this instanceof OnClickListener && !(findViewById instanceof AdapterView)) {
							findViewById.setOnClickListener((OnClickListener) this);
						}
					}
				} catch (IllegalAccessException e) {
					e.printStackTrace();
					LogUtil.d(e);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					LogUtil.d(e);
				}
			}
		}
	}
	protected void findViews(){}
	protected <T> T findView(int id){
		return (T) findViewById(id);
	}

	protected String getData(String key) {
		SharedPreferences sh = getSharedPreferences(dataKey, MODE_PRIVATE);
		return sh.getString(key, null);
	}
	protected void setData(String key,String value) {
		SharedPreferences sh = getSharedPreferences(dataKey, MODE_PRIVATE);
		sh.edit().putString(key, value).commit();
	}

	@Override
	public void onClick(View v) {
		if (v == goBack) {
			finish();
		}
	}
}
