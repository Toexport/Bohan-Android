package cn.mioto.bohan.utils;

import java.util.Comparator;

import cn.mioto.bohan.entity.SingleDevice;

/** 
* 类说明：
* 作者：  jiemai liangminhua 
* 创建时间：2016年7月15日 下午5:32:38 
*/
public class NineSetComparator implements Comparator<String>{

	@Override
	public int compare(String o1, String o2) {
		//获得是否关闭
		String status1 = o1.substring(8);
		String status2 = o2.substring(8);
		if(status1.equals("00")&&(!status2.equals("00"))){
			return 1;
		}else if(status2.equals("00")&&(!status1.equals("00"))){
			return -1;
		}else{
			return 0;
		}
	}

}
