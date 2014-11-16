
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;



public class http {
	
	private boolean built=false;
	private int status=0;  //0:undetermined 1:header  2:content-length  3:chunked  4:complete 
	private ArrayList<Packet> packet=new ArrayList<Packet>();
	
	private head h;
	private string content;
	private ByteBuffer buffer= ByteBuffer.allocate(1000);

	

	public int build(byte[] data) throws Exception{
		///////////////////////////////////////////
		//PSEUDO
		
		
		if status==0:
			h.build_head ,//->  0,1 2 3 4
			if 0:return 
			if 1:buffer.put(data),status=1,return 1
		    if 2:head_length=h.getheadlength,getcontentlength(),content=data[head_lenth:contentlength]
		    		if buffer>contentlength+headlength: status=4;
		    		if buffer<contentlength:status=2;
		    if 3:builder_chunk
		    
		    if 4:headpacket 
			
		if status==1:
			find "\r\n\r\n"
			if exists: buffer.put(data[:"\r\n\r\n"])
			   h.build_head(buffer)
			   		if 2:
			   		if 3:
			   		if 4:
			if not exists:return
		
		if status==2:data+buffer>contentlength:build content
					else return
		if status==3:buildcontent:data+buffer//chunk
		
		if status==4:packet+=1;clear in buffer;status=0;go to start;
		
		
			
			if so:
					status=1;
					h=this,header.build(buffer);
					
					
				
				ELSE RETURN;
		
					
	   if  status==1:
		   if builder.len>3: buffer-3 ->string,if "\r\n\r\n":  
				
		
		builder.put(buffer);
		String str=new String(builder.array());
		
		processing str....   -> set status;
		
		
		if status==1: 是packet｛取出buffer，构建packet，packet.add（）｝
		if status==2:	
				
		if status==4 ，是packet：{取出buffer，构建packet,packet.add();}
		
		
		
		return packet。length
		
		//////////////////////////////////////////
		
		try{
			builder.put(buffer);
			String str=new String(builder.array());
			String subs="";
			String content="";
			//StringReader sr=new StringReader(str);
			//BufferedReader br = new BufferedReader(sr);
		
			
			//header
			if(header_length==-1){
	
				header_length=str.indexOf("\r\n\r\n")+4;
				if(header_length!=-1){
					
					if(str.indexOf("Content_Length:")!=-1){
						subs=str.substring(str.indexOf("Content_Length:")+"Content_Length:".length(),str.length());
						this.Content_Length=Integer.parseInt(subs.substring(0,subs.indexOf("\r\n")).trim());
						
	
					}
					
					if(str.indexOf("Transfer-Encoding:")!=-1){
						subs=str.substring(str.indexOf("Transfer-Encoding:")+"Transfer-Encoding:".length(),str.length());
						this.Transfer_Encoding=subs.substring(0,subs.indexOf("\r\n")).trim();
						
					}
					if(str.indexOf("Host:")!=-1){
						subs=str.substring(str.indexOf("Host:")+"Host:".length(),str.length());
						this.Host=subs.substring(0,subs.indexOf("\r\n")).trim();
						
					}
				}
	
			}
			//end header
			header=str.substring(0,this.header_length);
			String current_content=str.substring(this.header_length,builder.position());
			
			//System.out.println(current_content);
			
			if (this.Content_Length!=-1){
				if(current_content.length()==this.Content_Length){
					content=str.substring(this.header_length,builder.position());
					this.built=true;
					return 1;
				}
			}
			
			else if(this.Transfer_Encoding.equals("chunked")){
				
				for(int i=0;i<=current_content.length();){
					String one_chunk=current_content.substring(i,current_content.length());
					
					
					String chunk_num=one_chunk.substring(0,one_chunk.indexOf("\r\n"));
					int j=Integer.parseInt(chunk_num)+chunk_num.length()+4;
					
					if(j!=5){
					this.chunks.add(current_content.substring(i,i+j-1));
					
					}
					else{
						this.built=true;
						this.chunks.add("0");
						return 1;
					}
					
					i+=j;
				}
	
				
			}
	
			
			return 0;
			}
		catch(Exception e){
			this.built=false;
			return 0;
			}
	}



	public String getchunk(){
			String res=this.chunks.get(0);
			this.chunks.remove(0);
			return res;
	}

	public byte[] getheader(){
		return this.header.getBytes();
	}

	public byte[] getcontent(){
		return this.content.getBytes();
	}
	public void setConnection(String conn){
		this.header=this.header.replaceAll("(Connection:).*?((\\r\\n))","$1"+conn+"$2");
	}
	

	
	public static void main(String args[]) throws Exception{
		String s="Accept:image/webp,*/*;q=0.8\r\nConnection:keep-alive\r\nHost:ww1.sinaimg.cn\r\nTransfer-Encoding:chunked\r\nAccept-Encoding:gzip,deflate,sdch\r\nAccept-Language:en-US,en;q=0.8\r\nCache-Control:max-age=0\r\nConnection:keep-alive\r\nHost:img.t.sinajs.cn\r\nIf-Modified-Since:Wed, 29 Oct 2014 08:58:53 GMT\r\nIf-None-Match:\"5450ac4d-e710\"\r\nReferer:http://weibo.com/u/1710751805/home?topnav=1&wvr=5\r\nUser-Agent:Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.111 Safari/537.36\r\n\r\n4\r\nxxxx\r\n3\r\nxxx\r\n1\r\nx\r\n0\r\n\r\n";
		
		StringReader sr=new StringReader(s);
		BufferedReader br = new BufferedReader(sr);
		
		br.readLine();
		
		
		http h=new http();
		h.build(s.getBytes());
		h.setConnection("alive");
		System.out.println(new String(h.getheader()));
		System.out.println(h.Content_Length);
		System.out.println(h.built);
		System.out.println(h.getchunk());
		System.out.println(h.getchunk());
		System.out.println(h.getchunk());

	}
	
public class Packet{
	private ArrayList<String[]> header_list=new ArrayList<String[]>();
	private List<String> chunks=new ArrayList<String>();
	//private String type=""; //POST GET HTTP.....
	
	


}


}
