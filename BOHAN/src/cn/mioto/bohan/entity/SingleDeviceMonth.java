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
	private String avgPowerData;// 平均用电功率,1为空 0或""不为空
	private String powerIsNull;//标志电量是否为空1为空 0或""不为空
	public String getPowerIsNull() {
		return powerIsNull;
	}
	public void setPowerIsNull(String powerIsNull) {
		this.powerIsNull = powerIsNull;
	}
	public String getAvgPowerIsNull() {
		return avgPowerIsNull;
	}
	public void setAvgPowerIsNull(String avgPowerIsNull) {
		this.avgPowerIsNull = avgPowerIsNull;
	}
	private String avgPowerIsNull;//标志平均功率是否为空
	
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
