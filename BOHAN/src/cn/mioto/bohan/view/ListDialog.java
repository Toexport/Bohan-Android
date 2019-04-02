package cn.mioto.bohan.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.mioto.bohan.R;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ListDialog extends Dialog{
	
    public ListDialog(Context context, int theme) {
        super(context, theme);
    }
 
    public ListDialog(Context context) {
        super(context);
    }
	
    public static class Builder {
		private String TAG = "ListDialog";
		
        private Context context;
        private String title;
        private DialogInterface.OnClickListener onClickListener;
        private List<Map<String, Object>> list;
        private int selectPosition = -1;
        
        private String positiveButtonText;
        private String negativeButtonText;
        
        private DialogInterface.OnClickListener 
        positiveButtonClickListener,
        negativeButtonClickListener;
 
        public Builder(Context context) {
        	this.context = context;
        	list = new ArrayList<Map<String, Object>>();
        }
        
        
        
        public void setOnClickListener(DialogInterface.OnClickListener onClickListener) {
			this.onClickListener = onClickListener;
		}



		public void addItem(int img, String name) {
        	 Map<String, Object> map = new HashMap<String, Object>();
        	 map.put("img", img);
        	 map.put("name", name);
        	 list.add(map);
        }
        
        public void addItem(String name) {
	       	 Map<String, Object> map = new HashMap<String, Object>();
	       	 map.put("img", -1);
	       	 map.put("name", name);
	       	 list.add(map);
       }
        
		/**
         * Set the Dialog title from resource
         * @param title
         * @return
         */
        public Builder setTitle(int title) {
            this.title = (String) context.getText(title);
            return this;
        }
 
        /**
         * Set the Dialog title from String
         * @param title
         * @return
         */
        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }
        
        /**
         * Set the positive button resource and it's listener
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
        public ListDialog create() {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final ListDialog dialog = new ListDialog(context, R.style.CustomDialog);
            View layout = inflater.inflate(R.layout.dialog_list, null);
            dialog.addContentView(layout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            ((TextView) layout.findViewById(R.id.title_textView)).setText(title);
            DialogAdapter adapter = new DialogAdapter(context, list);
            ((ListView)layout.findViewById(R.id.listView)).setAdapter(adapter);
            ((ListView)layout.findViewById(R.id.listView)).setOnItemClickListener(new OnItemClickListener() {
    			@Override
    			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
    					long arg3) {
    				if(onClickListener != null){
    					selectPosition = arg2;
    					onClickListener.onClick(dialog, arg2);
    				}
    			}			
    		});
            if (positiveButtonText != null) {
                ((Button) layout.findViewById(R.id.positive_button)).setText(positiveButtonText);
                ((Button) layout.findViewById(R.id.positive_button))
	                    .setOnClickListener(new View.OnClickListener() {
	                        public void onClick(View v) {
	                        	  if (positiveButtonClickListener != null) {
	                        		  positiveButtonClickListener.onClick(
	                            		dialog, selectPosition
	                            		);
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
                ((Button) layout.findViewById(R.id.negative_button)).setText(negativeButtonText);
                ((Button) layout.findViewById(R.id.negative_button))
                	.setOnClickListener(new View.OnClickListener() {
                       public void onClick(View v) {   
                    	    dialog.dismiss();
                           if (negativeButtonClickListener != null) {
                        	   negativeButtonClickListener.onClick(dialog, 
                        			   selectPosition);
                          	}
                        }
                 });
            } else {
                // if no confirm button just set the visibility to GONE
                layout.findViewById(R.id.buttonLayout_cancel).setVisibility(
                        View.GONE);
            }
            dialog.setContentView(layout);
            return dialog;
        }
        
        public void show(){
        	Dialog dialog = create();
        	dialog.show();
        	dialog.setCanceledOnTouchOutside(true);
        }
        
        public class DialogAdapter extends BaseAdapter{
    		
        	private Context context;
        	private List<Map<String, Object>> list;
        	
        	public DialogAdapter(Context context, List<Map<String, Object>> list)  {
        		super();
        		this.context = context;
        		this.list = list;	
        	}

        	@Override
        	public int getCount() {
        		return list.size();
        	}

        	@Override
        	public Object getItem(int position) {
        		return list.get(position);
        	}

        	@Override
        	public long getItemId(int position) {
        		return position;
        	}

        	@Override
        	public View getView(int position, View view, ViewGroup parent) {
        		ViewHolder viewHolder = null;
        		Map<String, Object> data = list.get(position);
        		if (view == null) {
        			viewHolder=new ViewHolder();		
        			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        			view = inflater.inflate(R.layout.item_dialog, null);
        		    viewHolder.img = (ImageView)view.findViewById(R.id.img);
        		    viewHolder.name = (TextView)view.findViewById(R.id.name);
        			view.setTag(viewHolder);
        		} else {
        			viewHolder =  (ViewHolder) view.getTag();
        		}		
        		String name = (String) data.get("name");
        		int img = (Integer) data.get("img");
        		if(img == -1){
        			viewHolder.img.setVisibility(View.GONE);
        		}else{
        			viewHolder.img.setImageResource(img);
        		}
        		viewHolder.name.setText(name);
       		
        	    return view;
        	}
        }         
    }

    
	final static class ViewHolder {
		ImageView img;
		TextView name;
	}	

}
