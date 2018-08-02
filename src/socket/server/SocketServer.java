package socket.server;

import java.io.*;
import java.net.*;

import socket.model.Log;
import socket.util.Common;
import thread.SyncThread;
import net.sf.json.JSONObject;


/**
 * @Description
 * @function Socket服务
 * @author 凌虚风
 * @Time 2016-12-31 21:04:01
 */

public class SocketServer {

	public static final int PORT = 50001;//要监听的端口
    public Socket socket;
    
    public  Socket getSocket() {
		
		return socket;
	}
    
	public void run(String[] args) throws Exception {
		ServerSocket ss = new ServerSocket(PORT);
		InetAddress ia = InetAddress.getByName(null);
		try {
			/*   初始化配置    */
			Common.init(args, ia);
			while (true) {
				Socket s = ss.accept();//listen PORT;
//				System.out.println("已开始监听:"+ss.getLocalPort()+"端口");
				Log.log("已开始监听:"+ss.getLocalPort()+"端口");
				try {
					new ServerOne(s);//创建线程
					this.socket=s;
				} catch (IOException e) {
					s.close();
				}
			}
		} finally {
			ss.close();
			Common.closeInit();
		}
	}
}

class ServerOne extends Thread {
	private Socket s;
	private InputStream in;
	private OutputStream out;  
	private String ip = null;
	byte[] buf = new byte[1024*2];//缓冲区大小
	public ServerOne(Socket s) throws IOException {
		this.s = s;
		in=s.getInputStream(); 
		out = s.getOutputStream();
		ip = s.getInetAddress().getHostAddress();
		Common.mainBoards.put(ip, "1");
//		System.out.println("客户iP:"+ip);
		Log.log("客户iP:"+ip);
		start();
	}

	public void run() {
		try {
			String logContent = null;
			while (true) {
				logContent = null;
			    in.read(buf);//读取数据 
				String hexStr=HexTool.bytesToHexString(buf);//将字节数组转成十六进制字符串
				String signStr = new String(buf);
				
				String commandStr=hexStr.substring(0,8);//截取到命令
//				System.out.println("十六进数据"+hexStr);
				
				
				if("55550601".equals(commandStr)){
					
					System.out.println("|【已收到控制板<服务器开门>结果通知】");
					//55550601:控制板向服务端发送开门结果通知指令
//					System.out.println("字节数据"+new String(buf,"UTF-16LE"));
//					System.out.println("十六进数据"+hexStr);
				}else if("55550701".equals(commandStr)){
					
					//55550601:控制板向服务端发送心跳指令
					
				}else if("55550804".equals(commandStr)){
//					System.out.println("十六进数据"+Common.substring(hexStr, 250));
					//0810:控制板向服务端上报刷身份证指令
					/**
					 * 通过校验位来验证数据是否合法
					 * 可供调用的算法函数XorCheck在HexTool类中
					 * 此处省略校验过程
					 */
					System.out.println(" -----------------------------------------");
					String bodyData=hexStr.substring(12, hexStr.length()-4);//截取到报文中的数据部分
					String doorCode = bodyData.substring(18, 20);//设备端口号
					String hexId = bodyData.substring(0, 16);
					System.out.println("|【解析数据包<设备IP>】"+ip);
					System.out.println("|【解析数据包<设备hex_ID>】"+hexId);
					/*System.out.println("|【解析数据包<设备返回码>】"+bodyData.substring(16, 18));
					System.out.println("|【解析数据包<设备端口号>】"+doorCode);
					System.out.println("|【解析数据包<刷卡时间>】"+bodyData.substring(22, 36));*/
					
					int cardLenngth = HexTool.toIntHex4(bodyData.substring(40, 44)) * 2;

					String cardBuf = bodyData.substring(44, 44 + cardLenngth);

					String queryString =HexTool.toStringHex(cardBuf).trim();
					
					if(queryString.equals("common:job168")){
						logContent = Log.SUCCESS+"【通用】";
					}
					else{
						JSONObject json = Common.validate(queryString);
						/* 第一步：参数校验 */
						if(json.getInt("checkedCode") != 1){
							logContent = Log.FAIL;
						}
						
						/*if(queryString.contains("CARD_TYPE:Y")&&!Common.isYellowPath(hexId, doorCode)){
							logContent = Log.FAIL+"【"+hexId+"】【"+doorCode+"】&checkedCode【"+checkedCode+"】&"+Common.substring(queryString, Log.LOG_LENGTH);
							System.out.println( Common.dateFormat("yyyy-MM-dd HH:mm:ss")+" "+logContent);
							break;
						}*/
						
						if(logContent==null){
							json.put("mainboardCode", hexId);
							json.put("doorCode", doorCode);
							new SyncThread(json);
							logContent = Log.SUCCESS+"【"+hexId+"】【"+doorCode+"】&checkedCode【"+json.getInt("checkedCode")+"】】&"+Common.substring(queryString, Log.LOG_LENGTH);
							if(queryString.contains("CARD_TYPE:Y")){//黄卡不开门，只记录扫码情况
								Log.log(logContent);
								break;
							}
						}
						else{
							logContent += "【"+hexId+"】【"+doorCode+"】&checkedCode【"+json.getInt("checkedCode")+"】&"+Common.substring(queryString, Log.LOG_LENGTH);
							System.out.println( Common.dateFormat("yyyy-MM-dd HH:mm:ss")+" "+logContent);
							break;
						}
						
						
					}
		
					byte[] deviceID=new byte[8];
						
					System.arraycopy(buf, 6, deviceID,0,8);
//						System.out.println("|【解析数据包<设备ID>】"+new String (deviceID));
						
					/**
					 * 组装开门命令
					 * 命令结构=引导码（16进制）+命令（16 进制）+数据长度（short 型）+数据（16 进制）+校验位+结束码
					 * 命令格式=
					 * AAAA+0601+1000+HexTool.bytesToHexString(deviceID)+[继电器控制]
					 * +HexTool.XorCheck(命令+数据长度+数据)+0D
					 * 
					 * 继电器控制说明:
					 * 报文格式：X,Y (X代表延时多少毫秒后开启门,Y代表动作，01->开门 00->维持门原状态)
					 * 只开第1道门,延时14*100毫秒的报文示例：
					 * 门1--->0E 01
					 * 门2--->00 00
					 * 门3--->00 00
					 * 门4--->00 00
					 * 最终拼接的十六进制字符串为:0E01000000000000
					 */
						
					String head="AAAA06011000";//引导码+长度
						
					String A="0000",B="0000",C="0000",D="0000";
					if(doorCode.equals("01")) A = "0001";
					else if(doorCode.equals("02")) B = "0001";
					else if(doorCode.equals("03")) C = "0001";
					else if(doorCode.equals("04")) D = "0001";
						
					String hex_data=HexTool.bytesToHexString(deviceID)+A+B+C+D;

					String checkData="0601"+"1000"+hex_data;//参与校验的数据报文
						
					byte sign=HexTool.XorCheck(HexTool.hexStringToBytes(checkData));
						
					byte[] sginByte={sign};//将字节装入数组
	
					String hexOpendoorCommand=head+hex_data+HexTool.bytesToHexString(sginByte).toUpperCase()+"0D";
						
//						System.out.println("|【组装开门指令<最终的十六进制开门指令串>】"+hexOpendoorCommand);
	
					out.write(HexTool.hexStringToBytes(hexOpendoorCommand));
						
					logContent += "&openCode【"+hexOpendoorCommand+"】";
//					System.out.println( Common.dateFormat("yyyy-MM-dd HH:mm:ss")+" "+logContent);
					Log.log(logContent);
				}
				else if(signStr!=null&&signStr.contains("ITJOB168")){
					System.out.println("**************闸机在线*****************");
					for(String key : Common.mainBoards.keySet()){
						System.out.println(key+"："+Common.mainBoards.get(key) );
					}
					return;
				}
				/*else if(testStr.length()>0){
					new DataService("GLOBAL_ID:4506019;CARD_TYPE:G;MD5:73C6004EF7FADA1589546B7328745988;CHANNEL:A"+";MAINBOARD:41564189561561;DOORCODE:01");
					out.write(buf);
					System.out.println(new String(buf)+"：长度："+testStr.length());
				}*/
			}
			
		} catch (IOException e) {
			Log.log(Log.ERROR+e.getMessage());
		} finally {
			try {
				Common.mainBoards.put(ip, "0");//下线
				System.out.println("ip已下线："+ip);
				s.close();
			} catch (IOException e) {}
		}
	}
}