

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.Iterator;
import java.util.Set;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class NioServer {
	String RemoteAddr;
    int RemotePort;
    Selector selector;
    ServerSocketChannel server;
    SocketAddress Remoteaddress ;
    
    Map<SocketChannel,SocketChannel> scmap;
    Map<SocketChannel,SocketChannel> csmap;
    
    
    public NioServer(String ip, int port) throws IOException{

       RemoteAddr="cache.sjtu.edu.cn";
       RemotePort= 8080;
       
       Remoteaddress = new InetSocketAddress(RemoteAddr, RemotePort);
       
       selector = null;
		// 定义实现编码、解码的字符集对象
       selector = Selector.open();
		// 通过open方法来打开一个未绑定的ServerSocketChannel实例
       server = ServerSocketChannel.open();
		// InetSocketAddress isa = new InetSocketAddress(ip, port);
       InetSocketAddress isa = new InetSocketAddress(ip, port);
		// 将该ServerSocketChannel绑定到指定IP地址
       server.socket().bind(isa);
		// 设置ServerSocket以非阻塞方式工作
       server.configureBlocking(false);
		// 将server注册到指定Selector对象
       server.register(selector, SelectionKey.OP_ACCEPT);
       
       scmap= new  HashMap<SocketChannel,SocketChannel>();
       csmap= new  HashMap<SocketChannel,SocketChannel>();
       
    }
    

	public void listen() throws IOException { // 用于检测所有Channel状态的Selector
		while (true) {
			int keys = selector.select();
			// System.out.println("keys:"+keys);
			if (keys > 0) {
				// 依次处理selector上的每个已选择的SelectionKey
				// SelectionKey removeReadSk=null;
				try {
					Set<SelectionKey> selectedKeys = selector.selectedKeys();

					Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
					while(keyIterator.hasNext()) {
						
						// 从selector上的已选择Key集中删除正在处理的SelectionKey
						SelectionKey sk= keyIterator.next();
						keyIterator.remove();
						// 如果sk对应的通道包含客户端的连接请求
						
						if (sk.isAcceptable()) {
						
							// 调用accept方法接受连接，产生服务器端对应的SocketChannel
							
								SocketChannel sc = server.accept();
								sc.socket().setTcpNoDelay(true);
							try{
								SocketChannel RemoteSocket = SocketChannel.open(Remoteaddress);
	
								
								// 设置采用非阻塞模式
								sc.configureBlocking(false);
								// 将该SocketChannel也注册到selector
								
			                     RemoteSocket.configureBlocking(false);
			                     RemoteSocket.socket().setTcpNoDelay(true);
			                     
			                     RemoteSocket.register(selector, SelectionKey.OP_READ);
			                     sc.register(selector, SelectionKey.OP_READ);
								// 将sk对应的Channel设置成准备接受其他请求
							//	sk.interestOps(SelectionKey.OP_ACCEPT);
								
			                         
			                     scmap.put(RemoteSocket, sc);
			                     csmap.put(sc, RemoteSocket);
							}catch(java.net.ConnectException e){
								sc.close();
							}
						}
						else
						if (sk.isReadable()) {
							
							// 获取该SelectionKey对应的Channel，该Channel中有可读的数据
							SocketChannel ss = null;
							SocketChannel sc = (SocketChannel) sk.channel();
							
							
							
							// 定义准备执行读取数据的ByteBuffer
							ByteBuffer buff = ByteBuffer.allocate(1024);
							// 开始读取数据
							
					
							if(sc.socket().getInetAddress().getHostAddress().compareTo(Remoteaddress.toString().split("/")[1].split(":")[0])==0){
								ss=scmap.get(sc);
							}else{
								ss=csmap.get(sc);
							}
							int len;
							try {
								while((len = sc.read(buff)) > 0) {
									buff.flip();// 缓存 2指针复位 准备下次读取数据
									//System.out.println("读取数据:" + new String(buff.array()));
									int nsend=0;
									while((nsend+=ss.write(buff))<len)
									{
						
										
									}
									buff.clear();
									//sk.interestOps(SelectionKey.OP_READ);
								}
							}
							catch (IOException ex) {
								ex.printStackTrace();
								
								System.out.println("sc"+sc);
								System.out.println("ss"+ss);
								
								
								sk.cancel();
								if(ss.isConnected()&&ss!=null){
									ss.close();
								}
								if(sc.isConnected()&&ss!=null){
									sc.close();
								}
								if(scmap.containsKey(sc)){
									scmap.remove(sc);
								}
								if(csmap.containsKey(sc)){
									csmap.remove(sc);
								}
								System.out.println("关闭一个客户端");
							}
						}	
					}
					
				} catch (Exception e) {
					e.printStackTrace();

				}
			}
		}
	}

	public static void main(String[] args) throws IOException {
		// System.out.println(InetAddress.getLocalHost().getHostAddress());
		String host = InetAddress.getLocalHost().getHostAddress();
		int port = 8087;
		NioServer S=new NioServer("127.0.0.1",port);
		// new NServer().init(InetAddress.getLocalHost().getHostAddress(),
		// 30000);
		System.out.println("Nio服务端启动了,host:" + host);
		S.listen();
	}
}
