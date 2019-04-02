package cn.mioto.bohan.entity;

import java.io.Serializable;
import java.util.List;

/** 
 * 类说明：用户实体类
 * 作者：  jiemai liangminhua 
 * 创建时间：2016年5月30日 上午10:51:05 
 */
public class User implements Serializable{
	//用户名
	private String UserName;
	//用户密码
	private String password;
	//用户拥有的所有设备
	private List<SingleDevice> devicesList;
	
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

	public String getUserName() {
		return UserName;
	}
	public void setUserName(String userName) {
		UserName = userName;
	}
	public List<SingleDevice> getDevicesList() {
		return devicesList;
	}
	public void setDevicesList(List<SingleDevice> devicesList) {
		this.devicesList = devicesList;
	}


}
