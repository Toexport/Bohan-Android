package cn.mioto.bohan.entity;

import java.io.Serializable;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/** 
 * 类说明:  每台设备实体类
 * 插座和开关，都拥有不一样的属性，但是我们都统一放在这里。
 */
public class SingleDevice implements Serializable{
	/*
	 * 设备状态 （00设备已断电01设备正在正常用电 02设备正在待机）
	 * 插座独有属性
	 * 
	 */
	private String devicePowerStatus = "00";
	
	public String getDevicePowerStatus() {
		return devicePowerStatus;
	}

	public void setDevicePowerStatus(String devicePowerStatus) {
		this.devicePowerStatus = devicePowerStatus;
	}
	
	//开关的继电器1，2，3。开关独有属性
	private Boolean jidianqi1Open = false;
	private Boolean jidianqi2Open = false;
	private Boolean jidianqi3Open = false;
	
	public Boolean getJidianqi1Open() {
		return jidianqi1Open;
	}

	public void setJidianqi1Open(Boolean jidianqi1Open) {
		this.jidianqi1Open = jidianqi1Open;
	}

	public Boolean getJidianqi2Open() {
		return jidianqi2Open;
	}

	public void setJidianqi2Open(Boolean jidianqi2Open) {
		this.jidianqi2Open = jidianqi2Open;
	}

	public Boolean getJidianqi3Open() {
		return jidianqi3Open;
	}

	public void setJidianqi3Open(Boolean jidianqi3Open) {
		this.jidianqi3Open = jidianqi3Open;
	}

	/*
	 * 设备的用电类型
	 */
	private String deviceAppType;

	public String getDeviceAppType() {
		return deviceAppType;
	}

	public void setDeviceAppType(String deviceAppType) {
		this.deviceAppType = deviceAppType;
	}

	//设备所在局域网的路由器的mac地址
	private String deviceWIFIBSSID;

	public String getDeviceWIFIBSSID() {
		return deviceWIFIBSSID;
	}

	public void setDeviceWIFIBSSID(String deviceWIFIBSSID) {
		this.deviceWIFIBSSID = deviceWIFIBSSID;
	}

	/*
	 * 设备id
	 */
	private String deviceID;
	/*
	 * 设备名称
	 */
	private String deviceName;
	/*
	 * 设备类型
	 */
	private String deviceType;
	/*
	 * 设备位置
	 */
	private String deviceLocation;
	/*
	 * 设备品牌
	 */
	private String deviceBrand;
	/*
	 * 设备物理地址
	 */
	private String mACAddress;
	/*
	 * 设备ip地址
	 */
	private String deviceIp;
	/*
	 * 是否在线 默认为不在线
	 */
	private Boolean isOnline=false;
	/*
	 * 插座类型的
	 * 继电器是否开 默认为关闭
	 */
	private Boolean isOpened=false;
	/*
	 * 设备名称首字母
	 */
	private String deviceName1st;
	/*
	 * 设备位置首字母
	 */
	private String deviceLocation1st;
	/*
	 * 设备类型首字母
	 */
	private String deviceType1st;
	/*
	 * 设备名称拼音
	 */
	private String deviceNamePinyin;
	/*
	 * 设备位置拼音
	 */
	private String deviceLocationPinyin;
	/*
	 * 设备类型拼音
	 */
	private String deviceTypePinyin;

	public String getDeviceNamePinyin() {
		deviceNamePinyin = getPinyin(deviceName);
		return deviceNamePinyin;
	}

	public String getDeviceLocationPinyin() {
		deviceLocationPinyin = getPinyin(deviceLocation);
		return deviceLocationPinyin;
	}

	public String getDeviceTypePinyin() {
		deviceTypePinyin=getPinyin(deviceType);
		return deviceTypePinyin;
	}

	public String getDeviceName1st() {
		deviceName1st = getFirstPinyin(this.deviceName.charAt(0));
		return deviceName1st;
	}

	public String getDeviceLocation1st() {
		deviceLocation1st = getFirstPinyin(this.deviceLocation.charAt(0));
		return deviceLocation1st;
	}

	public String getDeviceType1st() {
		deviceType1st = getFirstPinyin(this.deviceType.charAt(0));
		return deviceType1st;
	}

	public void setDeviceType1st(String deviceType1st) {
		this.deviceType1st = deviceType1st;
	}
	public Boolean getIsOnline() {
		return isOnline;
	}
	public void setIsOnline(Boolean isOnline) {
		this.isOnline = isOnline;
	}
	//获得插座的继电器的状态
	public Boolean getIsOpened() {
		return isOpened;
	}
	//设置插座的继电器的状态
	public void setIsOpened(Boolean isOpened) {
		this.isOpened = isOpened;
	}
	public String getDeviceBrand() {
		return deviceBrand;
	}
	public void setDeviceBrand(String deviceBrand) {
		this.deviceBrand = deviceBrand;
	}
	public String getDeviceID() {
		return deviceID;
	}
	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}
	public String getDeviceName() {
		return deviceName;
	}
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
	public String getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}
	public String getDeviceLocation() {
		return deviceLocation;
	}
	public void setDeviceLocation(String deviceLocation) {
		this.deviceLocation = deviceLocation;
	}
	public String getmACAddress() {
		return mACAddress;
	}
	public void setmACAddress(String mACAddress) {
		this.mACAddress = mACAddress;
	}
	public String getDeviceIp() {
		return deviceIp;
	}
	public void setDeviceIp(String deviceIp) {
		this.deviceIp = deviceIp;
	}

	public String getIpById(String deviceID){
		return deviceIp;
	}

	public String getIdById(String deviceIp){
		return deviceID;
	}

	@Override
	public String toString() {
		return "SingleDevice [deviceWIFIBSSID=" + deviceWIFIBSSID + ", deviceID="
				+ deviceID + ", deviceName=" + deviceName + ", deviceType=" + deviceType + ", deviceLocation="
				+ deviceLocation + ", deviceBrand=" + deviceBrand + ", deviceIp="
				+ deviceIp + ", isOnline=" + isOnline + ", isOpened=" + isOpened +
				"]";
	}

	public String getFirstPinyin(char cha){
		HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();  
		// UPPERCASE：大写  (ZHONG)  
		// LOWERCASE：小写  (zhong)  
		format.setCaseType(HanyuPinyinCaseType.LOWERCASE);  
		// WITHOUT_TONE：无音标  (zhong)  
		// WITH_TONE_NUMBER：1-4数字表示英标  (zhong4)  
		// WITH_TONE_MARK：直接用音标符（必须WITH_U_UNICODE否则异常）  (zhòng)  
		format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);  
		// WITH_V：用v表示ü  (nv)  
		// WITH_U_AND_COLON：用"u:"表示ü  (nu:)  
		// WITH_U_UNICODE：直接用ü (nü)  
		format.setVCharType(HanyuPinyinVCharType.WITH_V);  
		//		LogUtil.d("获取拼音字符:"+cha);
		try {
			String[] pinyin = PinyinHelper.toHanyuPinyinStringArray(cha, format);
			if (pinyin == null) {
				return cha+"";
			}
			return pinyin[0];
		} catch (BadHanyuPinyinOutputFormatCombination e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getPinyin(String str){
		StringBuffer sb = new StringBuffer();
		String temp;
		char[] chars = str.toCharArray();
		for (int i = 0;i <chars.length;i++) {
			if ((temp = getFirstPinyin(chars[i]))!= null) {
				sb.append(temp);
			}
		}
		return sb.toString();
	}

	/**
	 * 同设备ID获得设备对象
	 * 
	 */
	public SingleDevice getItemByItemId(String id){
		if(this.getDeviceID().equals(id)){
			return this;
		}
		return null;
	}

}