package org.lxh.demo;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;


import zigbeeNet.DeviceInfo;
import zigbeeNet.NodeInfo;
import zigbeeNet.NwkDesp;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;



/**
 * NIO TCP 客户端
 * 
 * @date 2010-2-3
 * @time 下午03:33:26
 * @version 1.00
 */
public class TCPClient implements Serializable{
	
	public static long prev = 0;
	
	// 信道选择器
	private Selector selector;

	// 与服务器通信的信道
	SocketChannel socketChannel;

	// 要连接的服务器Ip地址
	private String hostIp;

	// 要连接的远程服务器在监听的端口
	private int hostListenningPort;
	
	//android stuff
	private static final long serialVersionUID = -7060210544600464481L;
	private Handler UIhandler;
	private Context c;
	
	public boolean reading = false;
	public boolean FLAG_READ_COMPLETE = false;

	private byte[] rx;
	
	/*below is zigbee stuff*/
	public long coorstate;
	/**
	 * 构造函数
	 * 
	 * @param HostIp
	 * @param HostListenningPort
	 * @throws IOException
	 */
	public TCPClient(String HostIp, int HostListenningPort,Handler handler) throws IOException {
		this.hostIp = HostIp;
		this.hostListenningPort = HostListenningPort;
		this.UIhandler = handler;
		initialize();
		//rx = new byte[1024];
	}

	/**
	 * 初始化
	 * 
	 * @throws IOException
	 */
	
	public TCPClientReadThread tr;
	public TCPClientWriteThread tw;
	private void initialize() throws IOException {
		// 打开监听信道并设置为非阻塞模式
		socketChannel = SocketChannel.open(new InetSocketAddress(hostIp,
				hostListenningPort));
		socketChannel.configureBlocking(false);

		// 打开并注册选择器到信道
		selector = Selector.open();
		socketChannel.register(selector, SelectionKey.OP_READ);
		//MyClientDemo.getLineNumber(new Exception());
		// 启动读取线程
		tr = new TCPClientReadThread(selector);
		tw = new TCPClientWriteThread(this);
		
	}
	public void close() {
		try {
			socketChannel.close();
			//selector.select() > 0 return false read Thread end.
			//todo 
			if(mSendHandler!=null){
				mSendHandler.getLooper().quit();
				System.out.println("thead write "+ tw.isAlive());
			}
			System.out.println("thread is alive " + tr.isAlive() );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 发送字符串到服务器
	 * 
	 * @param message
	 * @throws IOException
	 */
/*	private void sendMsg(String message) throws IOException {
		ByteBuffer writeBuffer = ByteBuffer
				.wrap(message.getBytes( "UTF-16" ));
		socketChannel.write(writeBuffer);
	}*/

	private void write(byte[] tx,int offset,int len) throws IOException {
		// TODO Auto-generated method stub
		ByteBuffer writeBuffer = ByteBuffer
				.wrap(tx);
		socketChannel.write(writeBuffer);
	}

	
	public boolean readBlockWithTime(byte[]rxDest,int sec) throws InterruptedException{
		FLAG_READ_COMPLETE = false;
		if(rxDest.length < rx.length)
			return false;
		while(reading || !FLAG_READ_COMPLETE || --sec ==0)
			Thread.sleep(1000);
		if(sec==0){
			Log.e("Time", "Timeout");
			System.out.println("error: Timeout!");
			return false;
		}else{
			System.arraycopy(rx, 0, rxDest, 0, rx.length);
			return true;
		}
		//return false;
	}
	public boolean readPoll(byte[] rxDest) throws InterruptedException{
		FLAG_READ_COMPLETE = false;
		if(!FLAG_READ_COMPLETE||reading)
			return false;
		else
			System.arraycopy(rx, 0, rxDest, 0, rx.length);
		return false;
	}
	


	public boolean isConnected() {
		return socketChannel.isConnected();
	}

	public boolean isClosed() {
		return !socketChannel.isOpen();
	}

	static int irda_warn_flag=0,irda_warn_flag_temp=0;
	static int int_warn_flag=0,int_warn_flag_temp=0;
	static int smog_warn_flag=0,smog_warn_flag_temp=0;
	
	public final static long CLIENT_COMMAND_SETSENSOR 	= 0x03;
	public final static long CLIENT_COMMAND_GETNWKINFO 	= 0x01;
	public final static long CLIENT_COMMAND_GETTOPO		= 0x02;
	public final static long CLIENT_COMMAND_GETRFID		= 0x04;
	public final static long CLIENT_COMMAND_CLEARINT	= 0x08;
	
	public final static long CLIENT_COMMAND_ALARM_TOGGLE_ON = 0x100;
/*	public boolean Client_GetTempHum( ){
		long tmp[] = {0x15,0x05,0x0a};
		try {
			writeLong(tmp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}*/
	
	public boolean clientSendCommand(long command){
		if(mSendHandler == null || tw.isAlive()=="no")
			return false;
        Message childMsg = mSendHandler.obtainMessage();
        //childMsg.obj = mSendHandler.getLooper().getThread().getName() + " says Hello";
        childMsg.obj = command;
        mSendHandler.sendMessage(childMsg);
        //Log.i(TAG, "Send a message to the child thread - " + (String)childMsg.obj);
        return true;
	}
	public boolean clientSendCommand(long[] command){
		if(mSendHandler == null || tw.isAlive()=="no")
			return false;
        Message childMsg = mSendHandler.obtainMessage();
        childMsg.obj = command;
        childMsg.arg1 = TCPClientWriteThread.CLIENT_COMMAND_MUTI_ARGS;
        mSendHandler.sendMessage(childMsg);
        return true;
	}
	Handler mSendHandler;
	public class TCPClientWriteThread implements Runnable{
		protected static final int CLINET_COMMAND_CATE_SYSTEM = 0x100;
		protected static final int CLIEN_COMMAND_EXIT_THREAD  = 0x101;
		protected static final int CLIENT_COMMAND_MUTI_ARGS	  = 0x102;
		TCPClient clientInner;
		boolean alarmToggle = false;
		private Thread t;
		public TCPClientWriteThread(TCPClient c){
			clientInner = c;
			t = new Thread(this);
			t.start();
		}
		public Thread getT() {
			return t;
		}
		public String isAlive() {
			// TODO Auto-generated method stub
				// TODO Auto-generated method stub
			if(t.isAlive())
				return "yes";
			else
				return "no";
		}
		@Override
		public void run() {
			MyClientDemo.getLineNumber(new Exception());
			Looper.prepare();
			mSendHandler = new Handler(){
	            public void handleMessage(Message msg) {
	            	/* 使用两个参数传递 */
	            	if(msg.arg1 == CLINET_COMMAND_CATE_SYSTEM){
	            		boolean toggle = (msg.arg2 !=0);
	            		alarmToggle = toggle;
	            		return;
	            	}if(msg.arg1 == CLIEN_COMMAND_EXIT_THREAD){
	            		this.getLooper().quit();
	            		return;
	            	}if(msg.arg1 == CLIENT_COMMAND_MUTI_ARGS){
	            		long command[] = (long[]) msg.obj;
	            		/*long num = msg.arg2;
	            		long[] tmp = new long[num+2];
	            		tmp[0]= 0x15L;
	            		tmp[]*/
	            		try {
							writeLong(command);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	            		return ;
	            	}
	            	
	            	long command = ((Long) msg.obj).longValue();
	        		long tmp[] = {0x15,command,0x0a};
	        		if(command == CLIENT_COMMAND_SETSENSOR){
	        			tmp = new long[]{ 0x15,0x03,MyClientDemo.addr,MyClientDemo.state,0x0a};
	        		}
	        		try {
	        			writeLong(tmp);
	        		} catch (IOException e) {
	        			e.printStackTrace();
	        		}
	            }
	        	public void writeLong(long[] txLong) throws IOException{
	        		byte tx[],tmp[];
	        		long rxArr[] = new long[256];
	        		//String rxStrBuf = null;
	        		tmp = new byte[4];
	        		tx  = new byte[txLong.length*4];
	        		for(int i = 0 ; i < txLong.length;i++){
	        			tmp = HelpUtils.longToBytes(txLong[i]);
	        			System.arraycopy(tmp, 0, tx, i*4, 4);
	        		}
	        		clientInner.write(tx, 0, tx.length);
	        	}
			};
			//Run the message queue in this thread. Be sure to call quit() to end the loop.
			//loop 后面的代码不会执行
			Looper.loop();
			MyClientDemo.getLineNumber(new Exception());
		}
		
	}
	public boolean TCP_SUSPEND = false;
	public static NodeInfo newNodeInfo;
	public class TCPClientReadThread implements Runnable {
		private Selector selector;
		private byte[] rxTmp = new byte[4];
		private long [] rxLongs = null;
		private NwkDesp pNwkDesp2;
		private Thread t;
		public TCPClientReadThread(Selector selector) {
			this.selector = selector;
			this.pNwkDesp2 = new NwkDesp();
			t = new Thread(this);
			t.start();
		}
	
		public Thread getT() {
			return t;
		}

		public String isAlive() {
			// TODO Auto-generated method stub
			if(t.isAlive())
				return "yes";
			else
				return "no";
		}

		public void run() {
			boolean alive =true;
			try {
				while (selector.select() > 0 && alive) {
					// 遍历每个有可用IO操作Channel对应的SelectionKey
					if(TCP_SUSPEND)
						continue;
					
					FLAG_READ_COMPLETE = false;
					for (SelectionKey sk : selector.selectedKeys()) {
						// 如果该SelectionKey对应的Channel中有可读的数据
						if (sk.isReadable()) {
							// 使用NIO读取Channel中的数据
							reading = true;
							SocketChannel sc = (SocketChannel) sk.channel();
							ByteBuffer buffer = ByteBuffer.allocate(1024);
							sc.read(buffer);
							buffer.flip();
							rx = buffer.array();
							//System.out.println("client # len :"+ buffer.arrayOffset() +" rx "+ rx.length);
							buffer.position();
							System.out.println( "limit:"+buffer.limit());
							rxLongs = new long[buffer.limit()/4];
							for(int i=0;i<buffer.limit()/4;i++){
								System.arraycopy(rx, i*4, rxTmp, 0, 4);
								rxLongs[i] = HelpUtils.bytesToLong(rxTmp); 
								//System.out.println("long rx :" + rxLongs[i] + "\ti = "+ i);
							}
							// 控制台打印出来
/*							System.out.println("接收到来自服务器"
									+ sc.socket().getRemoteSocketAddress()
									+ "的信息:" + receivedString);*/
							if(rxLongs.length!=0)
								clientHandleRecvLongs(rxLongs);
							else{
								System.out.println("server out");
								alive = false;
								break;
							}
							// 为下一次读取作准备
							sk.interestOps(SelectionKey.OP_READ);
							rx = null;
							reading = false;
							FLAG_READ_COMPLETE = true;
						}

						// 删除正在处理的SelectionKey
						selector.selectedKeys().remove(sk);
					}
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		private boolean clientHandleRecvLongs(long[] rxLong) {
			// TODO Auto-generated method stub
			//int[] rxInt[] = (int [])rxLong;
			int temp;
            if(rxLong[0]==0x26)
            {
                temp=(int)rxLong[1];
                //count=i+1;
                System.arraycopy(rxLong, 2, rxLong, 0, rxLong.length -2);
                switch(temp)
                {
                case 0x01:
/**
 * 注意：Cliect_ZigBeeNwkTopo_Process(&buffer[2],count);  从后两个字节开始
 * */
                        //printf("COMMAND:-------TOPOINFO--------:\n");
                		Cliect_ZigBeeNwkTopo_Process(rxLong);
                        break;
                case 0x02:
                        //printf("COMMAND:-------GetZigBeeNwkInfo--------:\n");
                        Cliect_ZigBeeNwkInfo_Process(rxLong);
                        break;
                case 0x04:
                        //printf("COMMAND:-------GetRfidId--------:\n");
                        //Cliect_RfidId_Process(rxLong);
                        break;
                case 0x05:
                        //printf("COMMAND:-------GetTempHum--------:\n");
                        Cliect_TempHum_Process(rxLong);
                        break;
                case 0x06:
                        //printf("COMMAND:-------GetSendMsg--------:\n");
                        //Cliect_GPRSSend_Process(buffer[2]);
                        break;
                case 0x07:
                        //printf("COMMAND:-------get GPRSSignal--------:\n");
                        //Cliect_GPRSSignal_Process(buffer[2]);
                        break;
                case 0x08:
                case 0x09:
                	//Client_GetRealPic(rxLong);//Ready to get Real Pic.
                default:
                        System.out.println(("error COMMAND\n"));
                        break;

                }
            }
            else
            {
                //printf("other protrol.\n");
            }
			return false;
		}

		public final static long STATE_COOR_ONLINE_NOROUTE = 0x02;////for online,but NULL
		public final static long STATE_COOR_ONLINEWITHROUTE = 0;
		public final static long STATE_COOR_OFFLINE = 0x01;////for offline
		
		public final static long SENSOR_TYPE_SMOG = 0x02;
		public final static long SENSOR_TYPE_INT  = 0x3;
		
		
		public void Cliect_ZigBeeNwkTopo_Process(long[] node) {
			// TODO Auto-generated method stub
			String show ="";
			int count = node.length;
			//System.out.println("count : "+ count);
			ArrayList<NodeInfo> nodeInfos = new ArrayList<NodeInfo>();
			if(count < 5){
				if(node[0] == STATE_COOR_ONLINE_NOROUTE){
					coorstate = STATE_COOR_ONLINE_NOROUTE;
					show = "节点在线无节点";
				}else if(node[0] == STATE_COOR_OFFLINE){
					coorstate = STATE_COOR_OFFLINE;
					show = "节点不在线";
					System.out.println("节点不在线");
				}
			}else{// count > 5   得到链表
				int i =0;
				NodeInfo firstNodeInfo = new NodeInfo();
				DeviceInfo firstDevInfo = new DeviceInfo();
				firstDevInfo.nwkaddr = node[0];
//System.out.println("nwkaddr is "+firstDevInfo.nwkaddr);
				firstDevInfo.macaddr = new long[8];
				for(int j=0;j<=7;j++)
					firstDevInfo.macaddr[j] = node[j+1];
				firstDevInfo.depth = 		node[9];
				firstDevInfo.devtype = 		node[10];
				firstDevInfo.parentnwkaddr = node[11];
				firstDevInfo.sensortype = 	node[12];
				firstDevInfo.sensorvalue = 	node[13];
				firstDevInfo.status = 		node[15];
				firstNodeInfo.row = 		(byte) node[16];
				firstNodeInfo.num =		    (byte) node[17];
//System.out.println("get node : "+ firstNodeInfo.num);
				firstNodeInfo.devinfo	  		= firstDevInfo;
				nodeInfos.add(firstNodeInfo);//firstNodeInfo.next = null;
/*果然错了 处理应该放在接受线程，ui主线程仅仅负责更新ui！ （虽然一个个控件加到handler麻烦了点，但是可以封装一个类出来）*/
				if(firstDevInfo.sensortype == MyClientDemo.SENSORTYPE_RF||firstDevInfo.sensortype == MyClientDemo.SENSORTYPE_SMOG){
					if(firstDevInfo.sensorvalue >= 1)
		System.out.println("clear interrupt");
						TCPClient.this.clientSendCommand(TCPClient.CLIENT_COMMAND_CLEARINT);
				}
				
				//为什么要 -3
				for(i=1;i<(count-3)/18;i++){//2+18  i=
					newNodeInfo = new NodeInfo();
					DeviceInfo devInfo = new DeviceInfo();
					devInfo.nwkaddr = node[18*i];
//System.out.println("numb node: " +node[17+18*i] + "nwk is  : "+ devInfo.nwkaddr);
					devInfo.macaddr = new long[8];
					for(int j=0;j<=7;j++)
						devInfo.macaddr[j] = node[(j+1)+18*i];
					devInfo.depth = 		node[9+18*i];
					devInfo.devtype = 		node[10+18*i];
					devInfo.parentnwkaddr = node[11+18*i];
					devInfo.sensortype = 	node[12+18*i];
					devInfo.sensorvalue = 	node[13+18*i];
System.out.println("sensorvalue is :"+ node[13+18*i]);
					//注意没有14
					devInfo.status = 		node[15+18*i];
					newNodeInfo.row = 			(byte) node[16+18*i];
					newNodeInfo.num =		(byte) node[17+18*i];
//System.out.println("get node : "+ newNodeInfo.num);
					newNodeInfo.devinfo	  = devInfo;
					nodeInfos.add(newNodeInfo);//newNodeInfo.next = null;
					if(devInfo.sensortype == MyClientDemo.SENSORTYPE_RF||devInfo.sensortype == MyClientDemo.SENSORTYPE_SMOG){
						if(devInfo.sensorvalue >= 1)
			System.out.println("clear interrupt");
							TCPClient.this.clientSendCommand(TCPClient.CLIENT_COMMAND_CLEARINT);
					}
				}
			}
			//@wei 这个client随activity初始化。
			//所以测试客户端 默认在ui activity一直存在
			//正确的思路 ： create activity(每次） -->传递handler  --> 通过handler更新ui
			//				onDestory 时 UIhandler =null;
	        if(UIhandler!=null){
		        Message childMsg = UIhandler.obtainMessage();
		        childMsg.arg1 = MyClientDemo.UI_MESG_UPDATE_TOPO;
System.out.println("nodes num is "+ nodeInfos.size());
		        childMsg.obj = nodeInfos;//show;
	        	UIhandler.sendMessage(childMsg);
	        }
		}


		private void Cliect_ZigBeeNwkInfo_Process(long[] nwkinfo)
		{
		    pNwkDesp2.panid=nwkinfo[0];
		    pNwkDesp2.channel=nwkinfo[1];
		    //qDebug("pNwkDesp2.channel=%ld\n",pNwkDesp2.channel);
		    //qDebug("pNwkDesp2->channel=%d %d\n",*(nwkinfo+2),(*(nwkinfo+1)));
		    pNwkDesp2.maxchild=nwkinfo[3];
		    pNwkDesp2.maxdepth=nwkinfo[4];
		    pNwkDesp2.maxrouter=nwkinfo[5];
		}
		
		private void Cliect_TempHum_Process(long[] rxLong) {
			// TODO Auto-generated method stub
			
		}
	}

}
/* be clear ..
 * 
 * //客户端读取线程代码： import java.io.IOException; import java.nio.ByteBuffer; import
 * java.nio.channels.SelectionKey; import java.nio.channels.Selector; import
 * java.nio.channels.SocketChannel; import java.nio.charset.Charset;
 * 
 * 
 * }
 */
