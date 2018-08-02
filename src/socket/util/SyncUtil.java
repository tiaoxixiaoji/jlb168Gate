package socket.util;

import net.sf.json.JSONObject;
import socket.model.Log;

/**
 * @author lth
 * 同步扫码数据到线上数据库
 */
public class SyncUtil {

	/**
	 * 同步扫码数据到线上数据库
	 * @param query
	 * @return 1、成功  2、数据插入失败  3、校验失败 4、已经同步过了 5、未查询到globalId 99、异常
	 */
	public static int Sync(JSONObject json) {
		int result = 0;
		try{
			json.put("key", MD5Util.MD5Encode(json.toString()+"job168"));
			json.put("result", Common.readUrlByGet(Common.uploadUrl+java.net.URLEncoder.encode(json.toString(), "utf-8"), "utf-8", 6000));
			//			String backStr = Common.readUrlByGet(Common.uploadUrl+java.net.URLEncoder.encode(json.toString(), "utf-8"), "utf-8", 6000);
			result = json.getJSONObject("result").getInt("code");
		}
		catch(Exception e){
			result = 999;
//			Log.log(Log.ERROR+"socket.util.SyncUtil.syc："+json.toString());
			Log.log(Log.ERROR+"socket.util.SyncUtil.syc："+e.getMessage());
		}
		if(result==9) Log.log(json.getString("result"));
		return result;
	}

}
