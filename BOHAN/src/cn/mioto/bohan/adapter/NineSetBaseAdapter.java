package cn.mioto.bohan.adapter;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.mioto.bohan.BApplication;
import cn.mioto.bohan.Constant;
import cn.mioto.bohan.R;
import cn.mioto.bohan.activity.Menu9ONOFFActivity;
import cn.mioto.bohan.activity.NineSetSetTimeActivity;
import cn.mioto.bohan.socket.SocketLong;
import cn.mioto.bohan.utils.LoadDataThreadUtil;
import cn.mioto.bohan.utils.LogUtilNIU;
import cn.mioto.bohan.utils.ModbusCalUtil;
import cn.mioto.bohan.utils.ToastUtils;
import cn.mioto.bohan.view.SwitchView;
import cn.mioto.bohan.view.SwitchView.OnStateChangedListener;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 类说明：9组时段列表
 */
public class NineSetBaseAdapter extends BaseAdapter {
	private Handler handler;
	private Animation animation;
	Context context;
	LayoutInflater inflater;
	private int SET_TIME = 101;
	private String deviceId;
	private String deviceBSSID;
	private int settingPosition;// 正在设置的那个位置
	private View currentSettingView;
	private List<String> oldSettingList = new ArrayList<>();
	private List<String> stringList = new ArrayList<>();
	// 根据每个item的界面状况去设定的，9组定时指令组合
	private Map<Integer, String> mNineSetSetting = new HashMap<>();

	public void setmNineSetSetting(int key, String value) {
		this.mNineSetSetting.put(key, value);
	}

	/**
	 * 把9组的设置设置为初始化状态
	 */
	public void clearSettingMap() {
		for (int i = 0; i < 9; i++) {
			mNineSetSetting.put(i, "none");
		}
	}

	/**
	 * 供外部调用的方法，收集界面上的设置，发送指令
	 */
	public void sendOrderToConfirmSetting() {
		// 判断按钮,如果按钮是关闭的，都设置为0000000000
		// 如果按钮不为关闭，收集开始和结束时间，如果开始结束时间都
		StringBuffer content = new StringBuffer();
		for (int i = 0; i < mNineSetSetting.size(); i++) {
			if (mNineSetSetting.get(i).equals("none")) {
				content.append(stringList.get(i));
				LogUtilNIU.value("第" + i + "组指令内容为" + stringList.get(i));
			} else {
				content.append(mNineSetSetting.get(i));
				LogUtilNIU.value("第" + i + "组指令内容为" + mNineSetSetting.get(i));
			}
		}
		String setTime = content.toString();
		String verCode = ModbusCalUtil.verNumber(deviceId + "0009" + "002D"
				+ setTime);
		String msg = "E7" + deviceId + "0009" + "002D" + setTime + verCode
				+ "0D";
		LogUtilNIU.value("9组定时确认设置发送的指令为" + msg);
		// new LoadDataThreadUtil(msg, handler, deviceBSSID, context).start();

		try {
//			BApplication.instance.socketSend(msg);
			SocketLong.sendMsg(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 用来保存sp列表，所有非00的星期都保存，下次遇到00时使用
	/**
	 * @Title: getOldDataList
	 * @Description:此类内部调用的方法，用来获得用户9组定时的偏好设置（保存或获取） 使得查询继电器数据过来的时候，不受控的星期数据影响。
	 * 
	 * @return List<String>
	 * @throws
	 */
	private List<String> getOldDataList() {
		List<String> oldDatas = new ArrayList<>();
		String old = sp.getString(deviceId + Constant.NINE_SET_SETTING_SP,
				"noData");
		LogUtilNIU.value("old设置信息-->" + old);
		if (!old.equals("noData")) {// 有数据的话，解析成List
			String set0 = old.substring(0, 10);
			String set1 = old.substring(10, 20);
			String set2 = old.substring(20, 30);
			String set3 = old.substring(30, 40);
			String set4 = old.substring(40, 50);
			String set5 = old.substring(50, 60);
			String set6 = old.substring(60, 70);
			String set7 = old.substring(70, 80);
			String set8 = old.substring(80);
			oldDatas.add(set0);
			oldDatas.add(set1);
			oldDatas.add(set2);
			oldDatas.add(set3);
			oldDatas.add(set4);
			oldDatas.add(set5);
			oldDatas.add(set6);
			oldDatas.add(set7);
			oldDatas.add(set8);
		} else {
			// 不添加任何数据，就会让list返回空
		}
		return oldDatas;
	}

	/**
	 * @Title: listToString
	 * @Description:把集合转成数据字符串的方法
	 * @return String
	 * @throws
	 */
	public String listToString(List<String> alist) {
		String result = alist.toString().replace(",", "");//
		result = result.replace("[", "");
		result = result.replace("]", "");
		result = result.replace(" ", "");
		return result;
	}

	/**
	 * @Title: stringBuildList
	 * @Description:把指令字符串化为集合
	 * @return List<String>
	 * @throws
	 */
	public List<String> stringBuildList(String aaa) {
		List<String> datas = new ArrayList<>();
		String set0 = aaa.substring(0, 10);
		String set1 = aaa.substring(10, 20);
		String set2 = aaa.substring(20, 30);
		String set3 = aaa.substring(30, 40);
		String set4 = aaa.substring(40, 50);
		String set5 = aaa.substring(50, 60);
		String set6 = aaa.substring(60, 70);
		String set7 = aaa.substring(70, 80);
		String set8 = aaa.substring(80);
		datas.add(set0);
		datas.add(set1);
		datas.add(set2);
		datas.add(set3);
		datas.add(set4);
		datas.add(set5);
		datas.add(set6);
		datas.add(set7);
		datas.add(set8);
		return datas;
	}

	/**
	 * @Title: buildSpHexWeekFromOld
	 * @Description:从sp得到旧的数据，把旧数据的sp和新数据的sp对比，拿出为非00的星期设置 
	 *                                                     参数：旧sp保存的设置数据，现在查到的9组定时数据
	 * @return List<String>
	 * @throws
	 */
	public List<String> buildSpHexWeekFromOld(List<String> listNow,
			List<String> listOld) {
		for (int i = 0; i < listNow.size(); i++) {
			// 把9组定时中，为非00，也就是开的那些数据拿出来
			String hexWeekNow = listNow.get(i).substring(8);
			if (!hexWeekNow.equals("00")) {// 不为00记下来

			}
		}
		return oldSettingList;
	}

	// public void checkIsOpenSave(List<String> saving,String a, int position){
	// String hexWeekNow = a.substring(8);
	// if(!hexWeekNow.equals("00")){//不为00记下来
	//
	// }
	// }

	/**
	 * @Title: setOKRefleshView
	 * @Description:供外部调用的方法，接收到设置成功的广播后，view改变
	 * @return void
	 * @throws
	 */
	public void setOKRefleshView() {
		SwitchView sv = (SwitchView) currentSettingView
				.findViewById(R.id.switchView);
		// ImageView ivflower = (ImageView)
		// currentSettingView.findViewById(R.id.ivFlower);
		// LinearLayout llFlower = (LinearLayout)
		// currentSettingView.findViewById(R.id.llFlower);
		TextView tvStatus = (TextView) currentSettingView
				.findViewById(R.id.tvStatus);
		// llFlower.setVisibility(View.VISIBLE);
		// ivflower.clearAnimation();//动画要设置停止，这个VIEW才能消失
		// ivflower.setVisibility(View.GONE);
		if (sv.isOpened()) {
			sv.setOpened(false);// 设置为关闭
			tvStatus.setText(context.getString(R.string.did_not_open));
		} else {
			sv.setOpened(true);// 设置为打开
			tvStatus.setText(context.getString(R.string.did_open));
		}
	}

	public NineSetBaseAdapter(Context context, List<String> datas,
			String deviceId, String deviceBSSID, Handler handler) {
		this.handler = handler;
		this.deviceBSSID = deviceBSSID;
		this.context = context;
		this.deviceId = deviceId;
		inflater = LayoutInflater.from(context);
		if (datas != null) {
			this.stringList = datas;
			LogUtilNIU.value("onCreat时的stringList--->" + stringList.toString());
			/*
			 * 此处一大坑，这里会运行，因为datas不为空 stringList赋值后为没有长度，因为此时datas也是没有数据的。
			 * stringList在getView后，屏幕有数据时，才有数据
			 */
		} else {
			datas = new ArrayList<String>();
			this.stringList = datas;
		}
		oldSettingList = getOldDataList();// 程序一起的就从旧数据获取信息
		LogUtilNIU.value("oldSettingList--->" + oldSettingList);
		if (oldSettingList.size() < 1) {// 如果没有数据的话,就是sp数据没有保存过,这个列表就直接等于新来的列表
			oldSettingList = stringList;
		}
		// 保存9组设置的Map初始化
		clearSettingMap();
		// 一开始就从偏好设置里获得之前的9组定时偏好，这个以后可能要考虑做到服务器里。
		// 因为用户有可能更换手机登录 TODO
	}

	@Override
	public int getCount() {
		return this.stringList.size();
	}

	@Override
	public Object getItem(int position) {
		return this.stringList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		LogUtilNIU
				.value(position + "---stringList--->" + stringList.toString());
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_nine_menu_setting,// 获得每个item布局
					null);
			viewHolder = new ViewHolder();
			viewHolder.llOpenTime = (LinearLayout) convertView
					.findViewById(R.id.llOpenTime);
			viewHolder.llCloseTime = (LinearLayout) convertView
					.findViewById(R.id.llCloseTime);
			viewHolder.llChooseTime = (LinearLayout) convertView
					.findViewById(R.id.llChooseTime);
			viewHolder.tvOpenTime = (TextView) convertView
					.findViewById(R.id.tvOpenTime);
			viewHolder.tvCloseTime = (TextView) convertView
					.findViewById(R.id.tvCloseTime);
			viewHolder.tvStatus = (TextView) convertView
					.findViewById(R.id.tvStatus);
			viewHolder.tvWeekDay = (TextView) convertView
					.findViewById(R.id.tvWeekDay);
			viewHolder.switchView = (SwitchView) convertView
					.findViewById(R.id.switchView);
			// viewHolder.ivSwitchButton = (ImageView)
			// convertView.findViewById(R.id.ivSwitchButton);
			// viewHolder.llFlower = (LinearLayout)
			// convertView.findViewById(R.id.llFlower);
			// viewHolder.ivFlower = (ImageView)
			// convertView.findViewById(R.id.ivFlower);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		// /***************************************************/
		// //考虑convertView的复用，判断菊花要不要出现
		// if(BApplication.instance.getNineSetCanSet()){
		// //在可以设置状态的话，不做任何东西
		// }else{//在不可以设置状态，考虑复用问题
		// if(position!=settingPosition){//如果当前position不等于在设置的postion菊花消失
		// viewHolder.llFlower.setVisibility(View.VISIBLE);
		// }
		// }
		//
		String singleSet = stringList.get(position);// 单个字符串 1732 1733 00
		final String openTime = singleSet.substring(0, 4);
		// 显示 开始 时 分 00：00
		viewHolder.tvOpenTime.setText(openTime.substring(0, 2) + ":"
				+ openTime.substring(2));
		final String closeTime = singleSet.substring(4, 8);
		// 显示 结束 时 分 00：00
		viewHolder.tvCloseTime.setText(closeTime.substring(0, 2) + ":"
				+ closeTime.substring(2));
		String status = singleSet.substring(8);// 00
		// LogUtilNIU.e("status---->"+status);
		if (status.equals("FF") || status.equals("00")) {// 除了00代表为开启，其他代表已开启
			status = context.getString(R.string.did_not_open);
			// viewHolder.ivSwitchButton.setBackgroundResource(R.drawable.switch_off_pic);
			if (viewHolder.switchView.isOpened()) {
				viewHolder.switchView.setOpened(false);
			}
			// sp.getString(Constant.NINE_SET_SETTING_SP, "-1");
			// 把查到的00的那组，星期显示为旧星期数据
			// LogUtilNIU.value("oldSettingList长度为------>"+oldSettingList.size()+"判断怎么显示");
//			String temp = context.getString(R.string.mon)
//					+ context.getString(R.string.tue)
//					+ context.getString(R.string.wed)
//					+ context.getString(R.string.thu)
//					+ context.getString(R.string.fri)
//					+ context.getString(R.string.sat)
//					+ context.getString(R.string.sun);
			if (oldSettingList.size() > 0) {
				String hexweekDays = oldSettingList.get(position).substring(8);// 截取后两位
				if (hexweekDays.equals("00")) {// 在还没有设置过星期开的时候，连保存的数据也是00
					viewHolder.tvWeekDay.setText(context.getString(R.string.week_days));// 设置星期显示
				} else {
					String temp2 = ModbusCalUtil
							.hexweekDaysToWeekShow(hexweekDays);
					if (temp2.equals("一")) {
						viewHolder.tvWeekDay.setText(context
								.getString(R.string.mon));// 设置星期显示
					} else if (temp2.equals("二")) {
						viewHolder.tvWeekDay.setText(context
								.getString(R.string.tue));// 设置星期显示
					} else if (temp2.equals("三")) {
						viewHolder.tvWeekDay.setText(context
								.getString(R.string.wed));// 设置星期显示
					} else if (temp2.equals("四")) {
						viewHolder.tvWeekDay.setText(context
								.getString(R.string.thu));// 设置星期显示
					} else if (temp2.equals("五")) {
						viewHolder.tvWeekDay.setText(context
								.getString(R.string.fri));// 设置星期显示
					} else if (temp2.equals("六")) {
						viewHolder.tvWeekDay.setText(context
								.getString(R.string.sat));// 设置星期显示
					} else if (temp2.equals("日")) {
						viewHolder.tvWeekDay.setText(context
								.getString(R.string.sun));// 设置星期显示
					}

				}
			} else {
				viewHolder.tvWeekDay.setText(context.getString(R.string.week_days));// 设置星期显示
			}
		} else {
			status = context.getString(R.string.did_open);
			// viewHolder.ivSwitchButton.setBackgroundResource(R.drawable.switch_on_pic);
			if (!viewHolder.switchView.isOpened()) {
				viewHolder.switchView.setOpened(true);
			}
			/***************************************************/
			String hexweekDays = singleSet.substring(8);// 16进制表示的星期信息
			// LogUtilNIU.value("截取到的字符hexweekDays--"+hexweekDays);
			// 16进制转10进制在转字符串
			int weekDays = Integer.valueOf(hexweekDays, 16);
			// LogUtilNIU.value("16进制字符转10进制weekDays--"+weekDays);
			// 转化为二进制形式 如 101110011
			String binaryWeekDays = Integer.toBinaryString(weekDays);
			// LogUtilNIU.value("10进制转2进制"+binaryWeekDays);
			// 反转这个字符串 011001101
			// LogUtilNIU.value("显示日期8位数为binaryWeekDays"+binaryWeekDays);
			// 向前补0满8位
			binaryWeekDays = ModbusCalUtil.add0fillLength(binaryWeekDays, 8);
			binaryWeekDays = ModbusCalUtil.reverseString(binaryWeekDays);
			// LogUtilNIU.value("翻转2进制数"+binaryWeekDays);
			String weekDaysShow = "一二三四五六日";
			for (int i = 0; i < binaryWeekDays.length() - 1; i++) {// 遍历前7个字符
				char c = binaryWeekDays.charAt(i);
				// LogUtilNIU.value(i+"位置"+c);
				if (c == '1') {// 如果该字符等于1
					// weekDaysShow 的该为char不用变化
					// LogUtilNIU.value("c==1---"+c);
				} else if (c == '0') {
					// LogUtilNIU.value("weekDaysShow.charAt(i)--"+weekDaysShow.charAt(i));
					weekDaysShow = weekDaysShow.replace(weekDaysShow.charAt(i),
							' ');
					// LogUtilNIU.value("c==0---"+c);
					// LogUtilNIU.value("weekDaysShow---"+weekDaysShow);
				}
			}
			// LogUtilNIU.e("weekDaysShow---->"+weekDaysShow);
			weekDaysShow = weekDaysShow.replace(" ", "");
			if (weekDaysShow.equals("一")) {
				viewHolder.tvWeekDay.setText(context
						.getString(R.string.mon));// 设置星期显示
			} else if (weekDaysShow.equals("二")) {
				viewHolder.tvWeekDay.setText(context
						.getString(R.string.tue));// 设置星期显示
			} else if (weekDaysShow.equals("三")) {
				viewHolder.tvWeekDay.setText(context
						.getString(R.string.wed));// 设置星期显示
			} else if (weekDaysShow.equals("四")) {
				viewHolder.tvWeekDay.setText(context
						.getString(R.string.thu));// 设置星期显示
			} else if (weekDaysShow.equals("五")) {
				viewHolder.tvWeekDay.setText(context
						.getString(R.string.fri));// 设置星期显示
			} else if (weekDaysShow.equals("六")) {
				viewHolder.tvWeekDay.setText(context
						.getString(R.string.sat));// 设置星期显示
			} else if (weekDaysShow.equals("日")) {
				viewHolder.tvWeekDay.setText(context
						.getString(R.string.sun));// 设置星期显示
			}
		}

		if (!BApplication.instance.getNineSetWaitConfirm()) {
			viewHolder.tvStatus.setTextColor(context.getResources().getColor(
					R.color.textcolor_darkGray));
			viewHolder.tvStatus.setText(status);// 不在待确认状态下，直接设置
		} else {
			// 在待确认状态，需要判断有没有待确认的内容
			if (!viewHolder.tvStatus.getText().equals(context.getString(R.string.confirmed))) {
				viewHolder.tvStatus.setTextColor(context.getResources()
						.getColor(R.color.textcolor_darkGray));
				viewHolder.tvStatus.setText(status);
			}
		}
		/***************************************************/
		// 设置局部监听,跳转到设置界面
		LinearLayout llItemClick = (LinearLayout) convertView
				.findViewById(R.id.llItemClick);
		llItemClick.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				LogUtilNIU.value("adapter点击区的position是" + position);
				Intent intent = new Intent(((Menu9ONOFFActivity) context),
						NineSetSetTimeActivity.class);
				Bundle b = new Bundle();
				String date = stringList.get(position);// 被点击位置的9组定时时间信息
				b.putInt(Constant.POSITION_NINE_SET_SETTING_ITEM, position);
				b.putString(Constant.DATA_EXISTING_TIME, date);
				intent.putExtras(b);
				((Menu9ONOFFActivity) context).startActivityForResult(intent,
						SET_TIME);
			}
		});
		/***************************************************/
		// // //设置小开关监听
		final TextView tvStatus = viewHolder.tvStatus;
		final SwitchView sv = viewHolder.switchView;
		sv.setOnStateChangedListener(new OnStateChangedListener() {
			// 只要按钮发生变化，activity下面的大按钮就会出现
			@Override
			public void toggleToOn(View view) {
				// 先判断该item小时的设置是否为开始时间和结束时间都是00：00
				// 如果是，就不让用户操作
				if (openTime.equals("0000") && closeTime.equals("0000")) {
					sv.toggleSwitch(false);
					ToastUtils.shortToast(context, context.getString(R.string.please_set_start_end_time));
				} else {
					// 9组定时变为待确认状态
					BApplication.instance.setNineSetWaitConfirm(true);
					if (!sv.isOpened()) {
						sv.toggleSwitch(true);
					}
					tvStatus.setText(context.getString(R.string.confirmed));
					tvStatus.setTextColor(context.getResources().getColor(
							R.color.red));
					// 设置为开启，获得当前开启和关闭信息作为设置指令
					mNineSetSetting.put(position, stringList.get(position));
					// 发广播，大按钮出现
					// Intent intent = new Intent();
					// Bundle bundle = new Bundle();
					// bundle.putInt("switchPosition", position);
					// bundle.putString("action", "on");
					// intent.putExtras(bundle);
					// intent.setAction(Constant.BROCAST_NINE_SET_SHOW_BOTTOM_BTN);
					// context.sendBroadcast(intent);
					handler.sendEmptyMessage(Menu9ONOFFActivity.SET_ON_OFFF);
				}
			}

			@Override
			public void toggleToOff(View view) {
				if (openTime.equals("0000") && closeTime.equals("0000")) {
					sv.toggleSwitch(true);
					ToastUtils.shortToast(context, context.getString(R.string.please_set_start_end_time));
				} else {
					// 9组定时变为待确认状态
					BApplication.instance.setNineSetWaitConfirm(true);
					if (sv.isOpened()) {
						sv.toggleSwitch(false);
					}
					// 设置为关闭，该组指令全部设置为0000000000
					mNineSetSetting.put(position, "0000000000");
					// 当前item的文字显示显示为待确认
					tvStatus.setText(context.getString(R.string.confirmed));
					tvStatus.setTextColor(context.getResources().getColor(
							R.color.red));
					// TODO
					// 发广播，大按钮出现
					// Intent intent = new Intent();
					// Bundle bundle = new Bundle();
					// bundle.putInt("switchPosition", position);
					// bundle.putString("action", "off");
					// intent.putExtras(bundle);
					// intent.setAction(Constant.BROCAST_NINE_SET_SHOW_BOTTOM_BTN);
					// context.sendBroadcast(intent);
					handler.sendEmptyMessage(Menu9ONOFFActivity.SET_ON_OFFF);
				}
			}
		});

		return convertView;// 太牛了，一次口气写完，运行成功
	}

	private SharedPreferences sp = BApplication.instance.getSp();
	private Editor et = BApplication.instance.getEditor();

	protected void sendUdpToClose(int position) {
		// 在第一次设置关闭的时候，保存设置数据 TODO
		/***************************************************/
		BApplication.instance.setNineSetSettingPosition(position);
		// 关闭前，先把旧的定时数据保存到sp，以便打开时可以取出来。
		// 新数据和旧sp数据对比
		// 把为开00的保存替换
		LogUtilNIU.value("sendUdpToClose的stringList--->"
				+ stringList.toString());
		String nowData = stringList.toString().replace(",", "");// 这个旧数据不对 TODO
		nowData = nowData.replace("[", "");
		nowData = nowData.replace("]", "");
		nowData = nowData.replace(" ", "");
		// 字符串转为list集合
		List<String> nowDatas = new ArrayList<>();
		nowDatas = stringBuildList(nowData);
		// oldData这次的记录和上次的记录对比 oldData oldSettingList
		List<String> saving = new ArrayList<>();
		LogUtilNIU.value("*****nowDatas--->" + nowDatas.toString());
		LogUtilNIU.value("*****oldSettingList--->" + oldSettingList.toString());
		if (oldSettingList.size() > 0) {// 上次存得有数据，就对比，存对比后的数据
			for (int i = 0; i < nowDatas.size(); i++) {
				if (nowDatas.get(i).substring(8).equals("00")) {// 如果这次存的是00，就看看上次的是不是00
					if (oldSettingList.get(i).substring(8).equals("00")) {
						// 如果两次的都为00，就存00
						saving.add(nowDatas.get(i));
						LogUtilNIU
								.value(i + "--saving.add--" + nowDatas.get(i));
					} else if (!oldSettingList.get(i).substring(8).equals("00")) {
						// 如果这次00，上次不是00，就存上次的
						saving.add(nowDatas.get(i).substring(0, 8)
								+ oldSettingList.get(i).substring(8));
						LogUtilNIU.value(i + "--saving.add--"
								+ nowDatas.get(i).substring(0, 8)
								+ oldSettingList.get(i).substring(8));
					}
				} else {// 如果这次不为0，直接存这次的
					saving.add(nowDatas.get(i));
					LogUtilNIU.value(i + "--saving.add--" + nowDatas.get(i));
				}
			}
			String savingString = saving.toString().replace(",", "");// 这个旧数据不对
																		// TODO
			savingString = savingString.replace("[", "");
			savingString = savingString.replace("]", "");
			savingString = savingString.replace(" ", "");
			et.putString(deviceId + Constant.NINE_SET_SETTING_SP, savingString);
			et.commit();
		} else {// 上次没有数据，就直接存新的数据
			et.putString(deviceId + Constant.NINE_SET_SETTING_SP, nowData);
			et.commit();
		}
		oldSettingList.clear();
		oldSettingList = getOldDataList();// 清空旧的旧数据，保存为最新的经过过滤的偏好设置
		LogUtilNIU
				.value("*****oldSettingList是--->" + oldSettingList.toString());
		LogUtilNIU.value("*****saving是--->" + saving.toString());
		/***************************************************/
		StringBuffer sendToClose = new StringBuffer();
		if (stringList.size() < 1) {// 如果没有数据，就添加firstList为数据
			LogUtilNIU.value("stringList无数据，添加oldSettingList" + oldSettingList);
			for (int i = 0; i < oldSettingList.size(); i++) {
				// 把设定关那个position的内容设置为关
				if (i == position) {
					sendToClose.append(oldSettingList.get(i).substring(0, 8)
							+ "00");// 之前的时间+"00"，作为关闭
				} else {
					sendToClose.append(oldSettingList.get(i));// 数据不变
				}
			}
			LogUtilNIU.value("循环后内部关闭指令设置sendToClose----->" + sendToClose);
		} else {// 如果有数据，直接使用
			for (int i = 0; i < stringList.size(); i++) {// 把现在的那条数据变成00，然后发出
				if (i == position) {
					// 保存屏幕显示的星期数据
					// 这里要注意，因为之前是关闭状态，星期方面数据在来的时候会全部显示为默认的00000000
					// adapter 在显示前要判断是否为00000000，如果是就从sp获取，sp默认没有数据时也为00000000
					// 星期数据在直接用关闭按钮关闭时要保存，然后开启时，在sp把之前的偏好找回来
					// 把之前的星期数据保存起来用于adapter显示
					//
					sendToClose
							.append(stringList.get(i).substring(0, 8) + "00");// 之前的时间+"00"，作为关闭
				} else {
					sendToClose.append(stringList.get(i));// 数据不变
				}
			}
		}
		LogUtilNIU.value("最终关闭指令设置sendToClose----->" + sendToClose);
		// setTime为设置关闭的字串，和对象一直
		String setTime = sendToClose.toString();// 把新设置全变为字符串
		LogUtilNIU.value("关闭指令设置的时间为----->" + setTime);
		String verCode = ModbusCalUtil.verNumber(deviceId + "0009" + "002D"
				+ setTime);
		String msg = "3A" + deviceId + "0009" + "002D" + setTime + verCode
				+ "0D";// 发送9组定时指令
		LogUtilNIU.value("设置9组定时关指令--" + msg);
		LogUtilNIU
				.value("查看9组定时设置是-->"
						+ sp.getString(deviceId + Constant.NINE_SET_SETTING_SP,
								"保存失败"));
		try {
//			BApplication.instance.socketSend(msg);
			SocketLong.sendMsg(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// oldSettingList-->[1011101270, 0000000000, 0000000000, 0000000000,
	// 0000000000, 0000000000, 0000000000, 0000000000, 0000000000]

	protected void sendUdpToOpen(int position) {
		StringBuffer sendToOpen = new StringBuffer();
		for (int i = 0; i < stringList.size(); i++) {
			if (i == position) {
				// 这里要注意，因为之前是关闭状态，星期方面会全部显示为默认的日到6
				// TODO 星期数据在直接用关闭按钮关闭时要保存，然后开启时，在sp把之前的偏好找回来
				String hexWeek = oldSettingList.get(i).substring(8);
				if (hexWeek.equals("00")) {
					hexWeek = "7F";
				}
				sendToOpen.append(stringList.get(i).substring(0, 8) + hexWeek);// 把这个指令修改为打开，星期数据按本来屏幕显示
			} else {
				sendToOpen.append(stringList.get(i));
			}
		}
		String setTime = sendToOpen.toString();
		String verCode = ModbusCalUtil.verNumber(deviceId + "0009" + "002D"
				+ setTime);
		String msg = "E7" + deviceId + "0009" + "002D" + setTime + verCode
				+ "0D";
		LogUtilNIU.value("设置9组定时发送的数据为" + msg);
		try {
//			BApplication.instance.socketSend(msg);
			SocketLong.sendMsg(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
		;
	}

	public class ViewHolder {
		LinearLayout llOpenTime;// 开始，用于点击
		LinearLayout llCloseTime;// 结束，用于点击
		LinearLayout llChooseTime;// 结束，用于点击
		TextView tvOpenTime;// 开始时间，用于显示
		TextView tvCloseTime;// 关闭时间，用于显示
		TextView tvStatus;// 状态，用于显示
		TextView tvWeekDay;// 开启的星期，用于显示
		SwitchView switchView;// 滑动按钮
		// ImageView ivSwitchButton;//滑动按钮
		LinearLayout llFlower;// 菊花空白
		ImageView ivFlower;
	}

}
