package cn.mioto.bohan.utils;

import java.util.Comparator;

import cn.mioto.bohan.entity.SingleDevice;

/** 
 * 类说明：按名字比较的拼音的比较器,用于在线列表
 * 作者：  jiemai liangminhua 
 * 创建时间：2016年7月1日 下午2:55:31 
 */
public class PinyinComparatorName implements Comparator<SingleDevice> {

	@Override
	public int compare(SingleDevice o1, SingleDevice o2) {
		if(o1.getIsOnline()==true&&o2.getIsOnline()==false){//先看是否在线，在线的为1
			return -1;
		}else if(o1.getIsOnline()==false&&o2.getIsOnline()==true){
			return 1;
		}else if(o1.getIsOnline()==true&&o2.getIsOnline()==true){//两个都在线，比较名字的首字母  //如果两个都在线或离线，比较首拼音
			if(o1.getDeviceNamePinyin().compareTo(o2.getDeviceNamePinyin())>0){
				return 1;
			}else if(o1.getDeviceNamePinyin().compareTo(o2.getDeviceNamePinyin())<0){
				return -1;
			}else {
				return 0;
			}
		}else{
			if(o1.getDeviceNamePinyin().compareTo(o2.getDeviceNamePinyin())>0){
				return 1;
			}else if(o1.getDeviceNamePinyin().compareTo(o2.getDeviceNamePinyin())<0){
				return -1;
			}else {
				return 0;
			}
		}
	}
}
