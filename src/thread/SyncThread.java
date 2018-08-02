package thread;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.sf.json.JSONObject;
import socket.model.Log;
import socket.util.Common;
import socket.util.SyncUtil;

public class SyncThread extends Thread{
	
	private JSONObject json = null;

	public SyncThread(JSONObject json) {
		// TODO Auto-generated constructor stub
		this.json = json;
		start();
	}

	public void run() {
		
		Connection conn = null;
		int rowNum = 0;
		try{
			conn = Common.connectionPool.getConnection();
			conn.setAutoCommit(false);
			String sql = "select nfrc.seq_gate_scan_log_bk_no.nextval from dual";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			if(rs.next()){
				json.put("logNo", rs.getString(1));
			}
			rs.close();
			pstmt.close();
			
			//ͬ��������/�������ݿ��ɨ���
			int syncResult = SyncUtil.Sync(json);
			
			//������Ǳ��ز��Բ��Ҳ�ѯ������Ӧ��globalId���򲻰����ݲ��뵽ɨ��bk����
			if(Common.isOnLineUploadUrl && syncResult==5){//�����Ҳ�����Ӧ��globalId�Ͳ�������
				Log.log("����δ��ѯ����Ӧ�ġ�globalId��"+json.getString("globalId")+"��");
				return;
			}
			sql = "insert into nfrc.gate_scan_log_bk(log_no, global_id, card_type, channel, mainboard_code, door_code, scan_date, is_sync, is_local)" +
											 "values(	?, 		?, 			?,		?,			?,				?,		?,			?,		?)";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, json.getString("logNo"));
			pstmt.setString(2, json.getString("globalId"));
			pstmt.setString(3, json.getString("cardType"));
			pstmt.setString(4, json.getString("channel"));
			pstmt.setString(5, json.getString("mainboardCode"));
			pstmt.setString(6, json.getString("doorCode"));
			pstmt.setTimestamp(7, new java.sql.Timestamp(json.getLong("scanDate")));
			pstmt.setInt(8, syncResult);// 1ͬ���ɹ�
			pstmt.setInt(9, Common.isOnLineUploadUrl?0:1);//0����  1����
			rowNum = pstmt.executeUpdate();
			if(rowNum==1) conn.commit();
			pstmt.close();
		}
		catch(Exception e){
			Log.log(Log.ERROR+"socket.thread.SyncThread.run��"+e.getMessage());
		}
		finally{
			if(conn!=null) Common.connectionPool.returnConnection(conn);
		}
	}
}
