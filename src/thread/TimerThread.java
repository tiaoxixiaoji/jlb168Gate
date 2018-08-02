package thread;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;

import net.sf.json.JSONObject;
import socket.model.Log;
import socket.util.Common;
import socket.util.SyncUtil;

public class TimerThread extends Thread{

	public TimerThread() {
		start();
		Log.log("����ͬ���߳��������� ");
	}
	
	public void run() {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null, logNo=null;
		JSONObject json = new JSONObject();
		int successSyncCount = 0, failSyncCount = 0, updateSyncCount = 0, updateLocalCount = 0, sum=1;
		String syncLogNos = "-1", localLogNos = "-1";
		int nowHour = 0;
		while(true){
			try{
				sum = 0;
				successSyncCount = 0;
				failSyncCount = 0;
				updateSyncCount = 0;
				conn = Common.connectionPool.getConnection();
//				conn.setAutoCommit(false);
				sql = "select log_no, global_id, card_type, channel, mainboard_code, door_code, scan_date "
						+ "from nfrc.gate_scan_log_bk where is_sync<>1 and is_local<>1";
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				while(rs.next()&&sum++<999){
					json.clear();
					logNo = rs.getString("log_no");
					json.put("logNo", logNo);
					json.put("globalId", rs.getString("global_id"));
					json.put("cardType", rs.getString("card_type"));
					json.put("channel", rs.getString("channel"));
					json.put("mainboardCode", rs.getString("mainboard_code"));
					json.put("doorCode", rs.getString("door_code"));
					json.put("scanDate", rs.getTimestamp("scan_date").getTime());
					int result = SyncUtil.Sync(json);
					if(result==1 || result==4){//ͬ���ɹ������Ѿ�ͬ������
						successSyncCount++;
						syncLogNos += ","+logNo;
					}
					else if(result==5){//δ��ѯ����ӦglobalId
						localLogNos += ","+logNo;
					}
					else{
						failSyncCount++;
					}
				}
				rs.close();
				pstmt.close();
				
				if(successSyncCount>0){
					sql = "update nfrc.gate_scan_log_bk set is_sync=1 where log_no in ("+syncLogNos+")";
					pstmt = conn.prepareStatement(sql);
					updateSyncCount = pstmt.executeUpdate();
					pstmt.close();
				}
				if(!localLogNos.equals("-1")&&Common.isOnLineUploadUrl){//���ز��Բ�����
					sql = "update nfrc.gate_scan_log_bk set is_local=1 where log_no in ("+localLogNos+")";
					pstmt = conn.prepareStatement(sql);
					updateLocalCount = pstmt.executeUpdate();
					pstmt.close();
				}
			}
			catch(Exception e){
				e.printStackTrace();
				Log.log(Log.ERROR+"socket.thread.SyncThread.run��"+e.getMessage());
			}
			finally{
				if(conn!=null) Common.connectionPool.returnConnection(conn);
				Log.log(Log.SUCCESS+"ͬ��ɨ�����ݣ���successSyncCount��"+successSyncCount+"����failSyncCount��"+failSyncCount+"����updateSyncCount��"+updateSyncCount+"����updateLocalCount��"+updateLocalCount+"��");
			}
			/* ���Ӱ������������999 ORA-01795: �б��е������ʽ��Ϊ 1000*/
			if(sum<999){
				try {
					Thread.sleep(1000*60*10);
					nowHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
					while(nowHour<7 || nowHour>16){
						Thread.sleep(1000*60*10);
						//����7�㵽����17��ִ��
						nowHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
