package org.lxh.demo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

import zigbee.NwkDesp;

import android.util.Log;

/**
 * NIO TCP 客户端
 * 
 * @date 2010-2-3
 * @time 下午03:33:26
 * @version 1.00
 */
public class TCPClient {
	// 信道选择器
	private Selector selector;

	// 与服务器通信的信道
	SocketChannel socketChannel;

	// 要连接的服务器Ip地址
	private String hostIp;

	// 要连接的远程服务器在监听的端口
	private int hostListenningPort;
	
	public boolean reading = false;
	public boolean FLAG_READ_COMPLETE = false;

	private byte[] rx;
	/**
	 * 构造函数
	 * 
	 * @param HostIp
	 * @param HostListenningPort
	 * @throws IOException
	 */
	public TCPClient(String HostIp, int HostListenningPort) throws IOException {
		this.hostIp = HostIp;
		this.hostListenningPort = HostListenningPort;

		initialize();
		//rx = new byte[1024];
	}

	/**
	 * 初始化
	 * 
	 * @throws IOException
	 */
	private void initialize() throws IOException {
		// 打开监听信道并设置为非阻塞模式
		socketChannel = SocketChannel.open(new InetSocketAddress(hostIp,
				hostListenningPort));
		socketChannel.configureBlocking(false);

		// 打开并注册选择器到信道
		selector = Selector.open();
		socketChannel.register(selector, SelectionKey.OP_READ);

		// 启动读取线程
		new TCPClientReadThread(selector);
	}

	/**
	 * 发送字符串到服务器
	 * 
	 * @param message
	 * @throws IOException
	 */
	public void sendMsg(String message) throws IOException {
		ByteBuffer writeBuffer = ByteBuffer
				.wrap(message.getBytes(/* "UTF-16" */));
		socketChannel.write(writeBuffer);
	}

	public void write(byte[] tx,int offset,int len) throws IOException {
		// TODO Auto-generated method stub
		ByteBuffer writeBuffer = ByteBuffer
				.wrap(tx);
		socketChannel.write(writeBuffer);
	}
	/**
	 * 
	 * @param txLong need init
	 * @throws IOException 
	 */
	public void writeLong(long[] txLong) throws IOException{
		byte tx[],tmp[];
		long rxArr[] = new long[256];
		//String rxStrBuf = null;
		tmp = new byte[4];
		tx  = new byte[txLong.length*4];
		MyClientDemo.getLineNumber(new Exception());
		for(int i = 0 ; i < txLong.length;i++){
			tmp = HelpUtils.longToBytes(txLong[i]);
			System.arraycopy(tmp, 0, tx, i*4, 4);
		}
		this.write(tx, 0, tx.length);
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
	
	
	public void close() {
		try {
			socketChannel.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean isConnected() {
		return socketChannel.isConnected();
	}

	public boolean isClosed() {
		return !socketChannel.isOpen();
	}

	public static void main(String[] args) throws IOException {
		TCPClient client = new TCPClient("192.168.0.1", 1978);

		client.sendMsg("你好!Nio!醉里挑灯看剑,梦回吹角连营");
	}
	
	public class TCPClientReadThread implements Runnable {
		private Selector selector;
		private byte[] rxTmp = new byte[4];
		private long [] rxLongs = null;
		public TCPClientReadThread(Selector selector) {
			this.selector = selector;

			new Thread(this).start();
		}

		public void run() {
			try {
				while (selector.select() > 0) {
					// 遍历每个有可用IO操作Channel对应的SelectionKey
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

							// 将字节转化为为UTF-16的字符串
							String receivedString = Charset.forName("US-ASCII")
									.newDecoder().decode(buffer).toString();
							
							rx = buffer.array();
							System.out.println("client # len :"+ buffer.arrayOffset() +" rx "+ rx.length);
							int len = buffer.getInt(0);
							buffer.position();
							System.out.println("int :"+ len+ "position :" + buffer.position()+"limit:"+buffer.limit());
							System.out.println("getint : "+ buffer.getInt());
							rxLongs = new long[buffer.limit()];
							for(int i=0;i<buffer.limit()/4;i++){/*
								System.arraycopy(rx, i*4, rxTmp, 0, 4);
								rxLongs[i] = HelpUtils.StrToLong(rxTmp); 
								System.out.println("long rx :" + rxLongs[i]);*/
								rxLongs[i] = buffer.getLong(i);
							}
							clientHandleRecvLongs(rxLongs);
							// 控制台打印出来
							System.out.println("接收到来自服务器"
									+ sc.socket().getRemoteSocketAddress()
									+ "的信息:" + receivedString);

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
                switch(temp)
                {
                case 0x01:

                        //printf("COMMAND:-------TOPOINFO--------:\n");
                        //Cliect_ZigBeeNwkTopo_Process(rxLong);

                        break;
                case 0x02:

                        //printf("COMMAND:-------GetZigBeeNwkInfo--------:\n");
                        //Cliect_ZigBeeNwkInfo_Process(rxLong);
                	NwkDesp pNwkDesp2 = new NwkDesp();
                    pNwkDesp2.panid=rxLong[0];
                    pNwkDesp2.channel=rxLong[1];
                    //qDebug("pNwkDesp2.channel=%ld\n",pNwkDesp2.channel);
                    //qDebug("pNwkDesp2->channel=%d %d\n",*(nwkinfo+2),(*(nwkinfo+1)));
                    pNwkDesp2.maxchild=rxLong[2];
                    pNwkDesp2.maxdepth=rxLong[3];
                    pNwkDesp2.maxrouter=rxLong[4];
                    ZigbeeStuff.pNwkDesp2 = pNwkDesp2;
                        break;

                case 0x04:

                        //printf("COMMAND:-------GetRfidId--------:\n");
                        //Cliect_RfidId_Process(rxLong);
                        break;
                case 0x05:

                        //printf("COMMAND:-------GetTempHum--------:\n");
                        //Cliect_TempHum_Process(rxLong);
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
                	//Client_CtrlLights_Callback(rxLong);
                case 0x09:
                	//Client_GetRealPic(rxLong);//Ready to get Real Pic.
                default:
                        System.out.println(("error COMMAND\n"));
                        break;

                }
            }
            else
            {
                printf("other protrol.\n");
            }
		}
	}

}
/*
 * 
 * //客户端读取线程代码： import java.io.IOException; import java.nio.ByteBuffer; import
 * java.nio.channels.SelectionKey; import java.nio.channels.Selector; import
 * java.nio.channels.SocketChannel; import java.nio.charset.Charset;
 * 
 * 
 * }
 */
