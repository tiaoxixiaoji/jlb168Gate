package socket.util;

import net.sf.json.JSONObject;
import socket.model.Log;

/**
 * @author lth
 * ͬ��ɨ�����ݵ��������ݿ�
 */
public class SyncUtil {

	/**
	 * ͬ��ɨ�����ݵ��������ݿ�
	 * @param query
	 * @return 1���ɹ�  2�����ݲ���ʧ��  3��У��ʧ�� 4���Ѿ�ͬ������ 5��δ��ѯ��globalId 99���쳣
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
//			Log.log(Log.ERROR+"socket.util.SyncUtil.syc��"+json.toString());
			Log.log(Log.ERROR+"socket.util.SyncUtil.syc��"+e.getMessage());
		}
		if(result==9) Log.log(json.getString("result"));
		return result;
	}

}
