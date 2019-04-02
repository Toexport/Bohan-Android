package cn.mioto.bohan.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cn.mioto.bohan.R;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class CustomShareDialog extends Dialog {

	public CustomShareDialog(Context context, int theme) {
		super(context, theme);
	}

	public CustomShareDialog(Context context) {
		super(context);
	}

	public static class Builder {
		private String TAG = "AddPileDialog";
		private static String no;
		private static String x;
		private static String y;
		private Context context;
		private String title;
		private String message;
		private String positiveButtonText;
		private String negativeButtonText;
		private View contentView;

		
		private DialogInterface.OnClickListener positiveButtonClickListener,
				negativeButtonClickListener;

		public Builder(Context context) {
			this.context = context;
		}

		public Builder(Context context,String x,String y) {
			this.context = context;
			this.x=x;
			this.y=y;
		}
		
		
		/**
		 * 获取系统语言
		 * 
		 * @return
		 */
		private boolean isZh() {
			Locale locale = context.getResources().getConfiguration().locale;
			String language = locale.getLanguage();
			if (language.endsWith("zh")) {
				return true;
			} else {
				return false;
			}

		}

		/**
		 * Set the Dialog message from String
		 * 
		 * @param title
		 * @return
		 */
		public Builder setMessage(String message) {
			this.message = message;
			return this;
		}
		
		

		public static String getX() {
			return x;
		}


		public static void setX(String x) {
			Builder.x = x;
		}


		public static String getY() {
			return y;
		}


		public static void setY(String y) {
			Builder.y = y;
		}




		/**
		 * Set the Dialog message from resource
		 * 
		 * @param title
		 * @return
		 */
		public Builder setMessage(int message) {
			this.message = (String) context.getText(message);
			return this;
		}

		/**
		 * Set the Dialog title from resource
		 * 
		 * @param title
		 * @return
		 */
		public Builder setTitle(int title) {
			this.title = (String) context.getText(title);
			return this;
		}

		/**
		 * Set the Dialog title from String
		 * 
		 * @param title
		 * @return
		 */
		public Builder setTitle(String title) {
			this.title = title;
			return this;
		}

		/**
		 * Set a custom content view for the Dialog. If a message is set, the
		 * contentView is not added to the Dialog...
		 * 
		 * @param v
		 * @return
		 */
		public Builder setContentView(View v) {
			this.contentView = v;
			return this;
		}

		/**
		 * Set the positive button resource and it's listener
		 * 
		 * @param positiveButtonText
		 * @param listener
		 * @return
		 */
		public Builder setPositiveButton(int positiveButtonText,
				DialogInterface.OnClickListener listener) {
			this.positiveButtonText = (String) context
					.getText(positiveButtonText);
			this.positiveButtonClickListener = listener;
			return this;
		}

		/**
		 * Set the positive button text and it's listener
		 * 
		 * @param positiveButtonText
		 * @param listener
		 * @return
		 */
		public Builder setPositiveButton(String positiveButtonText,
				DialogInterface.OnClickListener listener) {
			this.positiveButtonText = positiveButtonText;
			this.positiveButtonClickListener = listener;
			return this;
		}

		/**
		 * Set the negative button resource and it's listener
		 * 
		 * @param negativeButtonText
		 * @param listener
		 * @return
		 */
		public Builder setNegativeButton(int negativeButtonText,
				DialogInterface.OnClickListener listener) {
			this.negativeButtonText = (String) context
					.getText(negativeButtonText);
			this.negativeButtonClickListener = listener;
			return this;
		}

		/**
		 * Set the negative button text and it's listener
		 * 
		 * @param negativeButtonText
		 * @param listener
		 * @return
		 */
		public Builder setNegativeButton(String negativeButtonText,
				DialogInterface.OnClickListener listener) {
			this.negativeButtonText = negativeButtonText;
			this.negativeButtonClickListener = listener;
			return this;
		}

		/**
		 * Create the custom dialog
		 */
		public CustomShareDialog create() {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final CustomShareDialog dialog = new CustomShareDialog(context,
					R.style.CustomDialog);
			View layout = inflater.inflate(R.layout.dialog_add_point, null);
			dialog.addContentView(layout, new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			((TextView) layout.findViewById(R.id.title_textView))
					.setText(title);
			if(!TextUtils.isEmpty(x)){
				((EditText) layout.findViewById(R.id.et_x)).setText(x);
				((EditText) layout.findViewById(R.id.et_x)).setFocusable(false);
			}
			if(!TextUtils.isEmpty(y)){
				((EditText) layout.findViewById(R.id.et_no)).setText(y);
				((EditText) layout.findViewById(R.id.et_no)).setFocusable(false);
			}
			if(!TextUtils.isEmpty(no)){
				((EditText) layout.findViewById(R.id.et_no_en)).setText(no);
				((EditText) layout.findViewById(R.id.et_no_en)).setFocusable(false);
			}
			EditText et1 = (EditText) layout.findViewById(R.id.et_no);
			EditText et2 = (EditText) layout.findViewById(R.id.et_no_en);
			if(isZh()){
				et1.setVisibility(View.VISIBLE);
				et2.setVisibility(View.GONE);
			}else{
				et1.setVisibility(View.GONE);
				et2.setVisibility(View.VISIBLE);
			}
			if (positiveButtonText != null) {
				((Button) layout.findViewById(R.id.positive_button))
						.setText(positiveButtonText);
				((Button) layout.findViewById(R.id.positive_button))
						.setOnClickListener(new View.OnClickListener() {
							public void onClick(View v) {
								//dialog.dismiss();
								if (positiveButtonClickListener != null) {
									positiveButtonClickListener.onClick(dialog,
											DialogInterface.BUTTON_POSITIVE);
								}
							}
						});

			} else {
				// if no confirm button just set the visibility to GONE
				layout.findViewById(R.id.buttonLayout_confirm).setVisibility(
						View.GONE);
			}
			// set the cancel button
			if (negativeButtonText != null) {
				((Button) layout.findViewById(R.id.negative_button))
						.setText(negativeButtonText);
				((Button) layout.findViewById(R.id.negative_button))
						.setOnClickListener(new View.OnClickListener() {
							public void onClick(View v) {
								dialog.dismiss();
								if (negativeButtonClickListener != null) {
									negativeButtonClickListener.onClick(dialog,
											DialogInterface.BUTTON_NEGATIVE);
								}
							}
						});

			} else {
				// if no confirm button just set the visibility to GONE
				layout.findViewById(R.id.buttonLayout_cancel).setVisibility(
						View.GONE);
			}
			// set the content message
			if (contentView != null) {
				// if no message set
				// add the contentView to the dialog body
				((LinearLayout) layout.findViewById(R.id.content_layout))
						.removeAllViews();
				((LinearLayout) layout.findViewById(R.id.content_layout))
						.addView(contentView, new LayoutParams(
								LayoutParams.WRAP_CONTENT,
								LayoutParams.WRAP_CONTENT));
				((EditText) layout.findViewById(R.id.et_no)).setFocusable(true);
			}
			dialog.setContentView(layout);
			return dialog;
		}

		public void show() {
			create().show();
		}

	}
	
	 
    public String getInput_pile_no(){
    	return ((EditText) findViewById(R.id.et_no)).getText().toString().trim();
    }
    public String getInput_x(){
    	return ((EditText) findViewById(R.id.et_x)).getText().toString().trim();
    }
    
    public String getInput_no_en(){
    	return ((EditText) findViewById(R.id.et_no_en)).getText().toString().trim();
    }
    
    public EditText getEdit_pile_no(){
    	return ((EditText) findViewById(R.id.et_no));
    }

    
    public EditText getEdit_pile_no_en(){
    	return ((EditText) findViewById(R.id.et_no_en));
    }
    
}
