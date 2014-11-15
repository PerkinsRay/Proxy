

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
		// ����ʵ�ֱ��롢������ַ�������
       selector = Selector.open();
		// ͨ��open��������һ��δ�󶨵�ServerSocketChannelʵ��
       server = ServerSocketChannel.open();
		// InetSocketAddress isa = new InetSocketAddress(ip, port);
       InetSocketAddress isa = new InetSocketAddress(ip, port);
		// ����ServerSocketChannel�󶨵�ָ��IP��ַ
       server.socket().bind(isa);
		// ����ServerSocket�Է�������ʽ����
       server.configureBlocking(false);
		// ��serverע�ᵽָ��Selector����
       server.register(selector, SelectionKey.OP_ACCEPT);
       
       scmap= new  HashMap<SocketChannel,SocketChannel>();
       csmap= new  HashMap<SocketChannel,SocketChannel>();
       
    }
    

	public void listen() throws IOException { // ���ڼ������Channel״̬��Selector
		while (true) {
			int keys = selector.select();
			// System.out.println("keys:"+keys);
			if (keys > 0) {
				// ���δ���selector�ϵ�ÿ����ѡ���SelectionKey
				// SelectionKey removeReadSk=null;
				try {
					Set<SelectionKey> selectedKeys = selector.selectedKeys();

					Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
					while(keyIterator.hasNext()) {
						
						// ��selector�ϵ���ѡ��Key����ɾ�����ڴ����SelectionKey
						SelectionKey sk= keyIterator.next();
						keyIterator.remove();
						// ���sk��Ӧ��ͨ�������ͻ��˵���������
						
						if (sk.isAcceptable()) {
						
							// ����accept�����������ӣ������������˶�Ӧ��SocketChannel
							
								SocketChannel sc = server.accept();
								sc.socket().setTcpNoDelay(true);
							try{
								SocketChannel RemoteSocket = SocketChannel.open(Remoteaddress);
	
								
								// ���ò��÷�����ģʽ
								sc.configureBlocking(false);
								// ����SocketChannelҲע�ᵽselector
								
			                     RemoteSocket.configureBlocking(false);
			                     RemoteSocket.socket().setTcpNoDelay(true);
			                     
			                     RemoteSocket.register(selector, SelectionKey.OP_READ);
			                     sc.register(selector, SelectionKey.OP_READ);
								// ��sk��Ӧ��Channel���ó�׼��������������
							//	sk.interestOps(SelectionKey.OP_ACCEPT);
								
			                         
			                     scmap.put(RemoteSocket, sc);
			                     csmap.put(sc, RemoteSocket);
							}catch(java.net.ConnectException e){
								sc.close();
							}
						}
						else
						if (sk.isReadable()) {
							
							// ��ȡ��SelectionKey��Ӧ��Channel����Channel���пɶ�������
							SocketChannel ss = null;
							SocketChannel sc = (SocketChannel) sk.channel();
							
							
							
							// ����׼��ִ�ж�ȡ���ݵ�ByteBuffer
							ByteBuffer buff = ByteBuffer.allocate(1024);
							// ��ʼ��ȡ����
							
					
							if(sc.socket().getInetAddress().getHostAddress().compareTo(Remoteaddress.toString().split("/")[1].split(":")[0])==0){
								ss=scmap.get(sc);
							}else{
								ss=csmap.get(sc);
							}
							int len;
							try {
								while((len = sc.read(buff)) > 0) {
									buff.flip();// ���� 2ָ�븴λ ׼���´ζ�ȡ����
									//System.out.println("��ȡ����:" + new String(buff.array()));
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
								System.out.println("�ر�һ���ͻ���");
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
		System.out.println("Nio�����������,host:" + host);
		S.listen();
	}
}
