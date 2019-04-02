package cn.mioto.bohan.fragment;

import java.util.ArrayList;
import java.util.List;

import cn.mioto.bohan.BApplication;
import cn.mioto.bohan.R;
import cn.mioto.bohan.utils.Enterface;
import cn.mioto.bohan.utils.LogUtilNIU;
import cn.mioto.bohan.utils.ModbusCalUtil;
import cn.mioto.bohan.utils.ToastUtils;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import steed.framework.android.client.JsonClientHandler2;

/**
 * 类说明：用电统计的基类 
 */
public abstract class BaseAllDeviceDataFragment extends BaseUDPNoCurrentFragment {
	protected int onlineDeviceNumbers = 0;// 在线设备数量,默认为0个
	// 折线图的横坐标
	protected List<String> xLablesLineChart = new ArrayList<>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	/**
	 * @Description:批量查询历史数据的接口
	 */
	protected void checkAllDeviceHisDataFromeService(List<String> ids, String paramName, String param) {

	}

	// 批量查询所有设备的数据的基类
	protected void checkOnlineDevicesDatasAndShow(final int codeNumber1, final int codeNumber2) {
		// 显示正在查询progress
		final ProgressDialog p = new ProgressDialog(getActivity());
		p.setMessage(getResources().getString(R.string.loading_popwer_data));
		p.setCanceledOnTouchOutside(false);
		p.show();

		LogUtilNIU.value("接口编号" + codeNumber1 + "批量查询用电量");
		new Enterface("batchSendToDevice.act").addParam("content", codeNumber1).doRequest(new JsonClientHandler2() {
			@Override
			public void onInterfaceSuccess(String message, String contentJson) {
				LogUtilNIU.value("接口编号" + codeNumber1 + "得到批量电量Json为---->" + contentJson);
				if (contentJson.equals("")) {
					ToastUtils.shortToast(getContext(), "电量查询失败，不在线或无数据返回");
				} else {
					try {
						contentJson.toUpperCase();
						// 直接把返回的数据计算出平均数并组合成集合并显示数据
						List<List<Double>> listsPower = new ArrayList<>();
						String[] msgs = contentJson.split(",");
						// 设备个数
						int quantity = msgs.length;
						showDeviceNumber(quantity);
						for (int i = 0; i < msgs.length; i++) {
							String msg = msgs[i];// 每条指令
							String effContent = msg.substring(24, msg.length() - 16);
							LogUtilNIU.value("有效数据内容为effContent-->" + effContent);
							// 处理有效的数据内容
							listsPower.add(dealPowerDataForEachID(effContent));
						}
						LogUtilNIU.value("codeNumber1" + codeNumber1);
						if (codeNumber1 == 4) {
							powerAveOfAll(listsPower, 24);
						} else if (codeNumber1 == 6) {
							powerAveOfAll(listsPower, 30);
						} else if (codeNumber1 == 8) {
							powerAveOfAll(listsPower, 12);
						}
					} catch (Exception e) {
						// TODO: handle exception
						ToastUtils.testToast(getContext(), "下标越界了" + "数据为" + contentJson);
						LogUtilNIU.value("下标越界捕捉打印错误信息" + e);
						p.dismiss();
						// checkRates(codeNumber2);
					}
				}
				p.dismiss();
				// 查询功率
				checkRates(codeNumber2);
			}

			@Override
			public void onInterfaceFail(String json) {
				ToastUtils.shortToast(getContext(), "查询用电量失败");
				LogUtilNIU.value("电量接口错误信息" + json);
				p.dismiss();
				// 查询功率
				checkRates(codeNumber2);
			}

			@Override
			public void onFailureConnected(Boolean canConnect) {
				ToastUtils.shortToast(getContext(), "网络异常");
				p.dismiss();
			}
		});

	}

	private void checkRates(final int codeNumber2) {
		// 显示正在查询progress
		final ProgressDialog p = new ProgressDialog(getActivity());
		p.setMessage(getResources().getString(R.string.loading_popwer_rate_data));
		p.setCanceledOnTouchOutside(false);
		p.show();
		LogUtilNIU.value("接口编号" + codeNumber2 + "批量查询功率");
		new Enterface("batchSendToDevice.act").addParam("content", codeNumber2).doRequest(new JsonClientHandler2() {
			@Override
			public void onInterfaceSuccess(String message, String contentJson) {
				p.dismiss();
				LogUtilNIU.value("接口编号" + codeNumber2 + "得到批量功率的Json为---->" + contentJson);
				// 3a6816080500940000110048000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000780d,3a6816062500500000110048002975002975002975000350000350000350000343000293007743007743007743007743007725007662007637007581007468007393007393007393007393000000006962004068660d
				if (contentJson.equals("")) {
					ToastUtils.shortToast(getContext(), "功率查询失败，不在线或无数据返回");
				} else {
					try {
						contentJson.toUpperCase();
						// 直接把返回的数据计算出平均数并组合成集合并显示数据
						// 把所有单个设备的数据集合添加到一个总集合里
						String[] msgs = contentJson.split(",");
						int quantity = msgs.length;
						showDeviceNumber(quantity);
						List<List<Double>> listsRate = new ArrayList<>();
						for (int i = 0; i < msgs.length; i++) {
							String msg = msgs[i];// 每条指令
							String effContent = msg.substring(24, msg.length() - 4);
							// 处理有效的数据内容
							listsRate.add(dealPowerRateForEachID(effContent));
							// 计算平均
						}

						LogUtilNIU.value("codeNumber2" + codeNumber2);
						if (codeNumber2 == 5) {
							rateAveOfAll(listsRate, 24);
						} else if (codeNumber2 == 7) {
							rateAveOfAll(listsRate, 30);
						} else if (codeNumber2 == 9) {
							rateAveOfAll(listsRate, 12);
						}
					} catch (Exception e) {
						ToastUtils.testToast(getContext(), "下标越界" + "数据为" + contentJson);
						LogUtilNIU.value("下标越界捕捉打印错误信息" + e);
						p.dismiss();
					}
				}
			}

			@Override
			public void onInterfaceFail(String json) {
				ToastUtils.shortToast(getContext(), "查询用功率失败");
				LogUtilNIU.value("功率接口错误信息" + json);
				p.dismiss();
			}

			@Override
			public void onFailureConnected(Boolean canConnect) {
				ToastUtils.shortToast(getContext(), "网络异常");
				p.dismiss();
			}
		});
	}

	// 通过服务器查询数据
	public abstract void checkData();

	// protected abstract List<Double> dealPowerDataForEachID(String content);
	/**
	 * 把每条用电量化为小数集合的方法
	 */
	protected List<Double> dealPowerDataForEachID(String content) {
		List<String> hoursPowerList = new ArrayList<>();// 每小时用电量原始数据
		List<Double> powerDatas = new ArrayList<>();// 每小时用电量处理后得出的浮点数集合
		// 截取为12分的list集合
		for (int i = 0; i < content.length(); i += 8) {
			hoursPowerList.add(content.substring(i, i + 8));// 把每条数据截取为一个一个，放到此集合
		}
		for (int i = 0; i < hoursPowerList.size(); i++) {// 处理每个数据，把它化为浮点数的形式
			String data = hoursPowerList.get(i);
			data = ModbusCalUtil.addDotDel0(data, 6);
			Double d = Double.valueOf(data);
			powerDatas.add(d);
		}
		return powerDatas;
	}

	/**
	 * 把每条功率化为小数集合的方法
	 */
	protected List<Double> dealPowerRateForEachID(String content) {
		List<Double> powerRates = new ArrayList<>();// 每小时用电量处理后得出的浮点数集合
		List<String> listsRateList = new ArrayList<>();// 每小时功率原始数据
		// 截取为12分的list集合
		for (int i = 0; i < content.length(); i += 6) {
			listsRateList.add(content.substring(i, i + 6));// 天才！！！！！有木有！！！
		}
		// LogUtilNIU.value("功率数据处理后截取为listsRateList"+listsRateList.toString());
		// 把次数据做小数处理
		for (int i = 0; i < listsRateList.size(); i++) {
			String data = listsRateList.get(i);
			data = ModbusCalUtil.addDotDel0(data, 2);
			Double d = Double.valueOf(data);
			powerRates.add(d);
		}
		return powerRates;
	}

	/***************************************************/
	/**
	 * 求power的平均值 放进一个Power结果集合里
	 */
	protected void powerAveOfAll(List<List<Double>> listsPower, int type) {// 所有设备用电量的平均数集合
		LogUtilNIU.value("需要计算平均用电量的数组为" + listsPower.toString());
		List<Double> resultPowerAve = new ArrayList<>();
		for (int j = 0; j < type; j++) {
			Double sum = 0d;
			for (int i = 0; i < listsPower.size(); i++) {
				sum += listsPower.get(i).get(j);
			}
			resultPowerAve.add(sum);
		}
		// 把最后算得的平均数据显示
		LogUtilNIU.value("电量平均值" + resultPowerAve.toString());
		initBarDatas(resultPowerAve);
	}

	/***************************************************/
	/**
	 * 求rate的平均值 放进一个Power结果集合里
	 */
	protected void rateAveOfAll(List<List<Double>> listsRate, int type) {// 所有设备用电量的平均数集合
		LogUtilNIU.value("需要计算平均功率的数组为" + listsRate.toString());
		List<Double> resultRateAve = new ArrayList<>();
		for (int j = 0; j < type; j++) {
			Double sum = 0d;
			for (int i = 0; i < listsRate.size(); i++) {
				sum += listsRate.get(i).get(j);
			}
			resultRateAve.add(sum);
		}
		LogUtilNIU.value("功率平均值" + resultRateAve.toString());
		// 把最后算得的平均功率数据显示
		initArea(resultRateAve);
	}

	// 处理数据的抽象方法
	protected abstract void initArea(List<Double> resultRateAve);

	protected abstract void initBarDatas(List<Double> resultPowerAve);

	protected abstract String showDeviceNumber(int quantity);

}
