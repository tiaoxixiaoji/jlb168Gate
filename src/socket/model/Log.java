package socket.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import socket.util.Common;

public class Log {

	public static final String SUCCESS = "¡¾SUCCESS¡¿";
	public static final String FAIL = "¡¾FAIL¡¿";
	public static final String ERROR = "¡¾ERROR¡¿";
	public static final int LOG_LENGTH = 80;
	
	public static String logPath = null;

	
	public Log(){
		logPath = Common.path + "/log/";
		File file = new File(logPath);
		if(!file.exists()) file.mkdirs();
	}
	
	// ¼ÇÂ¼ÈÕÖ¾
	public static void log(String logContent){
		String time = Common.dateFormat("yyyy-MM-dd HH:mm:ss");
		String filePath = logPath + time.substring(0, 10) + ".txt";
		logContent = time+"£º"+logContent;
		System.out.println(logContent);
		
		FileWriter fileWriter = null;
		BufferedWriter bufferWriter = null;
		try {
			File file = new File(filePath);
			if (!file.exists()) file.createNewFile();

			fileWriter = new FileWriter(filePath, true);
			bufferWriter = new BufferedWriter(fileWriter);

			bufferWriter.write(time+"£º"+logContent);
			bufferWriter.newLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (bufferWriter != null) bufferWriter.close();
				if (fileWriter != null) fileWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
