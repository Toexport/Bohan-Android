package cn.mioto.bohan.entity;

/**
 * 单个设备月数据
 * 
 * @author STAR
 * 
 */
public class SingleDeviceMonth {
	private int id;
	private String date;// 日期
	private String powerData;// 电量
	private String avgPowerData;// 平均用电功率
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getPowerData() {
		return powerData;
	}
	public void setPowerData(String powerData) {
		this.powerData = powerData;
	}
	public String getAvgPowerData() {
		return avgPowerData;
	}
	public void setAvgPowerData(String avgPowerData) {
		this.avgPowerData = avgPowerData;
	}
}
