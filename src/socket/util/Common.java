package socket.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpStatus;

import net.sf.json.JSONObject;
import socket.model.Log;
import thread.TimerThread;

public class Common {
	public static String path = null; 
	public static String uploadUrl = null;
	public static ConnectionPool connectionPool = null;
	public static boolean isOnLineUploadUrl = false;
	
	public static Map<String, String> mainBoards = new HashMap<String, String>();
	

	public Common(){}
	
	//��ʼ��
	public static void init(String[] args, InetAddress ipAddress) throws Exception{
		path = System.getProperty("user.dir").replace("\\", "/");
		/*   ƴ����־·��    */
		new Log();
		Log.log("************************************************************************************");
		Log.log("�����@" + ipAddress + " ������!");
		Log.log("��־·��:"+Log.logPath);
		
		if(args!=null&&args.length>0) uploadUrl = args[0]+"/api/meetingScan/?";
		else uploadUrl = "http://10.88.1.37/api/meetingScan/?";
//		else uploadUrl = "http://www3.job168.com/api/meetingScan/?";
		isOnLineUploadUrl = uploadUrl.contains("www")?true:false;
		Log.log("�ϴ��ӿ�:"+uploadUrl);
		
		/*   ��ʼ�����ݿ����ӳ�    */
		Common.connectionPool = new ConnectionPool();
		Common.connectionPool.createPool();
		/*   ��������ͬ���߳�    */
		new TimerThread();
	}
	
	public static void closeInit() throws SQLException{
		Common.connectionPool.closeConnectionPool();
		Log.log("�������ֹͣ!");
		Log.log("************************************************************************************");
	}
	
	//���ڸ�ʽ�����ַ���
	public static String dateFormat(String format){
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(date);
	}
	
	//��ȡurl����
	public static String readUrlByGet(String url, String chartset ,int timeOutSeconds){
		BufferedReader read = null;
		HttpURLConnection conn = null;
		try{
			URL realurl = new URL(url);  
			//������  
			conn = (HttpURLConnection)realurl.openConnection();
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.84 Safari/537.36");
			conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
			conn.setConnectTimeout(timeOutSeconds);
			int code = conn.getResponseCode();
			if (code!=HttpStatus.SC_INTERNAL_SERVER_ERROR&&code!=HttpStatus.SC_NOT_FOUND) {
				read = new BufferedReader(new InputStreamReader(  
						conn.getInputStream(),chartset));
				StringBuilder sb = new StringBuilder();
				String line = null;
	            while ((line = read.readLine()) != null) {  
	            	sb.append(line);  
	            }
	            return sb.toString();
			}
			else return "{\"responseCode\":"+code+"}";
		}
		catch(Exception e){
			return "{\"errmsg\":\""+String.valueOf(e.getMessage())+"\"}";
//			e.printStackTrace();
		}
		finally{
			try {
				if(read!=null) read.close();
				if(conn!=null) conn.disconnect();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @param query ������
	 * @return int 1У��ɹ� 2������������ 3����ֵ����
	 */
	public static JSONObject validate(String query){
		JSONObject json = new JSONObject();
		json.put("checkedCode", 0);
		try{
			String[] params = query.split(";");
			if(params.length==4){
				String today = dateFormat("yyyy-MM-dd");
				if(params[2].equals("MD5:"+MD5Util.MD5Encode(params[0]+(params[3].equals("CHANNEL:P")?"":";DATE:"+today)+";"+params[1]))){
					json.put("globalId", params[0].split(":")[1]);
					json.put("cardType", params[1].split(":")[1]);
					json.put("channel", params[3].split(":")[1]);
					json.put("scanDate", new Date().getTime());
					json.put("checkedCode", 1);
				}
				else json.put("checkedCode", 3);
			}
			else json.put("checkedCode", 2);;
		}
		catch(Exception e){
			json.put("checkedCode", 99);
			json.put("errmsg", e.getMessage());
		}
		return json;
	}
	
	public static void main(String[] args) throws IOException{
		String query = "GLOBAL_ID:6111746;CARD_TYPE:C;MD5:3797B15464E3BA8FBCC28EE09F362C3F;CHANNEL:P";
		String[] params = query.split(";");
		String today = dateFormat("yyyy-MM-dd");
		System.out.println("MD5:"+MD5Util.MD5Encode(params[0]+(params[3].equals("CHANNEL:P")?"":";DATE:"+today)+";"+params[1]));
		System.out.println(validate(query));
	}
	
	
	/**
	 * @param str  Դ�ַ���
	 * @param length ��ȡ����
	 * @return
	 */
	public static String substring(String str, int length){
		if(str==null || str.equals("")) return "";
		else if(str.length()<=length) return str;
		else return str.substring(0, length);
	}
	
	/* �ж��Ƿ��ǻƿ�ͨ�� */
	public static String[][] yellowPaths = {
				{"3239323238383738", "01"},
				{"3136303131323738", "01"},
				{"3436353434353838", "02"}
			};
	public static boolean isYellowPath(String hexId, String doorCode){
		for(String[] yp: yellowPaths){
			if(yp[0].equals(hexId)&&yp[1].equals(doorCode)) return true;
		}
		return false;
	}
}
