
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



public class http {
	
	private boolean built=false;
	private int status=0;  //0:undetermined 1:header  2:content-length  3:chunked  4:complete 
	private ArrayList<Packet> packet_list=new ArrayList<Packet>();
	
	private head head;
	private Packet current_packet;
	private String content;
	private ByteBuffer buffer= ByteBuffer.allocate(1000);
	String http_method[]={"HTTP/1.0","HTTP/1.1","GET","POST","HEAD","PUT","DELETE","TRACE","OPTIONS","PATCH","CONNECT"};
	
	
	
	public Packet get_packet(){
		if(this.packet_list.size()>0){
			Packet re=this.packet_list.get(0);
			this.packet_list.remove(0);
			return re;
		}
		else{
			return null;
		}
	}
	
/**********************************
 * return: 0-uncomplete  1-complete 2-chunked
 * 
 *********************************/
	public int build(byte[] data) throws Exception{
		
		//////////////////////////////////////////
		if(packet_list.size()!=0){
			return 1;
		}
		
		try{
			buffer.put(data);
			String str="";
			String subs="";
			String content="";
			//StringReader sr=new StringReader(str);
			//BufferedReader br = new BufferedReader(sr);
			
			buffer.flip();
			//System.out.println(buffer.limit());
			Charset charset = Charset.forName("UTF-8");  
			CharsetDecoder decoder = charset.newDecoder();  
            // charBuffer = decoder.decode(buffer);//用这个的话，只能输出来一次结果，第二次显示为空  
			CharBuffer charBuffer = decoder.decode(buffer.asReadOnlyBuffer()); 
			str=charBuffer.toString();
			//System.out.println(str);
			
			if(status==0){
				if(str.length()<4){
					this.status=0;
					return 0;
				}
				
				
				
				if(Arrays.asList(this.http_method).contains(str.split(" ")[0])){
					this.status=1;		
				}
				else{
					buffer.clear();
					this.status=0;
					return 0;
				}
				
			}
			
			if(status==1){
				
				if(str.indexOf("\r\n\r\n")!=-1){
					subs=str.substring(0,str.indexOf("\r\n\r\n"));
					this.head=new head(subs);
					
					if(!(this.head.get("content-length").equals("none"))){
						this.current_packet=new Packet(this.head,"");
						this.status=2;
					}
					else if((this.head.get("transfer-encoding").toLowerCase().equals("chunked"))){
						this.current_packet=new Packet(this.head,"");
						this.status=3;
						
					}
					else{
						this.status=4;
						this.current_packet=new Packet(this.head,"");
						this.packet_list.add(this.current_packet);
						buffer.clear();
						return 1;
					}

				}
				else{
					this.status=1;
					return 0;
				}
			}
			
			if(status==2){
				int content_length=Integer.parseInt(this.head.get("content-length").trim());
				
				subs=str.substring(str.indexOf("\r\n\r\n"),str.length());
				buffer.position(str.indexOf("\r\n\r\n"));
				if(subs.length()>=content_length){
					
					this.current_packet=new Packet(this.head,subs.substring(0, content_length-1));
					this.packet_list.add(this.current_packet);
					buffer.position(buffer.position()-content_length);
					return 1;
				}
				
				else{
					buffer.position(buffer.position()-subs.length());
					return 0;
				}
			}
			
			if(status==3){
				//str=new String(buffer.array());
				
				String current_content=str.substring(str.indexOf("\r\n\r\n")+4,str.length());
				String tmp="";
				buffer.position(str.indexOf("\r\n\r\n"));
				
				for(int i=0;i<=current_content.length();){
 				    tmp=current_content.substring(i,current_content.length());
 					String chunk_num=tmp.substring(0,tmp.indexOf("\r\n"));
 					System.out.println(chunk_num);
 					int j=Integer.parseInt(chunk_num.trim())+chunk_num.length()+4;
 					
 					System.out.println(i+" "+j+" "+current_content.length());
 					if((i+j)>current_content.length()){
 						buffer.position(buffer.position()-current_content.length());
 						return 0;}
 					
 					if(Integer.parseInt(chunk_num.trim())!=0){
 						
 						this.current_packet.add_chunk(current_content.substring(i,i+j-1));
 						buffer.position(buffer.position()-i+j-1);
 					}
 					else{
 						status=4;
 						System.out.println("ge");
 						this.current_packet.add_chunk("0");
 						System.out.println("geqw");
 						this.packet_list.add(current_packet);
 						return 1;
 					}
 					
 					i+=j;
 				}
				
				return 2;
 				
 			}
 	
			
			else {
				buffer.clear();
				return 0;
			}
				
			}catch(Exception e){
				System.out.println(e.toString());
				return 0;
			}
	}
			
			
			

	
	public static void main(String args[]) throws Exception{
		String s="HTTP/1.1 200 OK\r\nAccept:image/webp,*/*;q=0.8\r\nConnection:keep-alive\r\nHost:ww1.sinaimg.cn\r\nTransfer-Encoding:chunked\r\ni/537.36\r\n\r\n4\r\nxxxx\r\n3\r\nxxx\r\n1\r\nx\r\n0\r\n\r\n";
		
		StringReader sr=new StringReader(s);
		BufferedReader br = new BufferedReader(sr);
		
		br.readLine();
		
		
		http h=new http();
		h.build(s.getBytes());
		//h.build(s.getBytes());
		//h.build(s.getBytes());
		
		System.out.println(h.get_packet().get_head().toSting());
		
		//System.out.println(h.build(s.getBytes()));
		System.out.println(h.head.get("hoSt"));
		System.out.println(h.head.get("transfer-encoding"));
		
		//h.setConnection("alive");
		

	}
	
public class Packet{
	private head head;
	private ArrayList<String> chunks;
	private String content;
	//private String type=""; //POST GET HTTP.....
	
	public Packet(head head,String content){
		this.head=head;
		this.chunks=new ArrayList<String>();
		this.content=content;	
	}
	
	public void add_chunk(String chunk){
		chunks.add(chunk);	
	}
	
	public head get_head(){
		return this.head;
	}
	
	
}

public class head{
	private ArrayList<String[]> header_list=new ArrayList<String[]>();
	
	public head(String s){
		String[] tmp=s.trim().split("\r\n");
	
		for(int i=0;i<tmp.length;i++){
			this.header_list.add(tmp[i].split(":"));
			
		}	
	}
	
	public String get(String args){
		args=args.trim().toLowerCase();
		for(int i=0;i<header_list.size();i++){
			if(header_list.get(i)[0].trim().toLowerCase().equals(args)){
				return header_list.get(i)[1].trim();
			}
		}
		return "none";
	}
	
	public String toSting(){
		String re="";
		for(int i=0;i<header_list.size();i++){
			if(header_list.get(i).length==1){
				re+=header_list.get(i)[0]+"\r\n";
			}
			else{
				re+=header_list.get(i)[0]+":"+header_list.get(i)[1]+"\r\n";			
		
			}
		}
		return re+"\r\n";
		
	}
}


}
