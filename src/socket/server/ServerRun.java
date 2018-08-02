package socket.server;

/**
 * @Description
 * @function 执行该类的main函数可启动Socket服务端
 * @author 凌虚风
 * @Time 2016-12-31 21:04:01
 */
public class ServerRun {

	public static void main(String[] args) throws Exception {
		new SocketServer().run(args);
	}
}
