package cn.mioto.bohan.entity;
/** 
* 类说明：9组定时设置状态的实体类,此类暂时未被使用 //TODO
* 作者：  jiemai liangminhua 
* 创建时间：2016年6月16日 下午3:21:14 
*/
public class NineSetSetting {

	private String openTime;
	private String endTime;
	private String status;
	private String openWeekDays;
	private Boolean onOff;
	
	public String getOpenTime() {
		return openTime;
	}
	public void setOpenTime(String openTime) {
		this.openTime = openTime;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getOpenWeekDays() {
		return openWeekDays;
	}
	public void setOpenWeekDays(String openWeekDays) {
		this.openWeekDays = openWeekDays;
	}
	public Boolean getOnOff() {
		return onOff;
	}
	public void setOnOff(Boolean onOff) {
		this.onOff = onOff;
	}
	
}
