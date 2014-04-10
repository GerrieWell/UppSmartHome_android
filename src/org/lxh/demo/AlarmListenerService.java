package org.lxh.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

public class AlarmListenerService extends Service {
	/*
	 * 在Service中的变量会永驻内存.
	 * 完善的做法是把状态保存在这里,activity每次启动到这里取值更新控件
	 */
	/*final*/
	public static final String TCP_SERVICE_ACTION_CONNECT="abcd",TCP_SERVICE_ACTION_SEND="abce";
	
	static boolean serviceRan=false;
	public static boolean electric_sw=false;
	public static boolean light_opened=false;
	public static int eltNum=0;
	public static byte lightchar=0;
	public static byte elecAction=0;
	public static String notificationShowStr="test";
	private boolean alarmRecved=false;
	
	/*component*/
/*	public static PrintStream shOut;
	public static BufferedReader shBuf ;
	public static OutputStream os;
	public static InputStream is;*/
	public static TCPClient client;
	
	/*remote info*/
	public static boolean isConnect;
	public static boolean outMode=false;
	public static int str_num=6;
	public static int lightGroupNum=2;
	public static int wir_sws[]={0,1,1,0,1,0,0};
	public static byte lightStates[]=null;
	public static byte WIRING_ACTION_OPEN=1;
	public static int i;
	public static String rxStrBuf = null;
	boolean opened;
	static boolean mutexEnble=true;
	static byte cut_P6=0;
	static boolean checkChangeble=true;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		System.out.println("Service : onStartCommand");
		if(intent.getBooleanExtra("alarmRecved", false)){
			alarmRecved=true;
			electric_sw = intent.getBooleanExtra("electric", false);
			eltNum = intent.getIntExtra("electric_num", 1);
			lightchar=intent.getByteExtra("room_light", (byte)0);
			light_opened=intent.getBooleanExtra("openLight", false);
			System.out.println("Service start 35");
			return super.onStartCommand(intent, flags, startId);
		}
		String temp=intent.getAction();
		if(temp!=null&&temp.equals(TCP_SERVICE_ACTION_CONNECT)){

		}else if(temp!=null&&temp.equals(TCP_SERVICE_ACTION_SEND)){
			
		}
		
		serviceRan=true;
		return super.onStartCommand(intent, flags, startId);
	}
/*	*//**
	 * 
	 * @param tx
	 * @param ack
	 * @return false: cmd return false ||tcp fault
	 * 		   true : cmd return true;
	 *//*
	
	public static boolean sendCmdByTCP(byte[] tx,String ack){
		if(!mutexEnble){
			return false;
		}
		
		String rx_buf=null;
		if(null==client){
			System.out.println("client null");
		}
		if((null==client)||(!client.isConnected())||client.isClosed()){
			return false;
		}
			try {			
				if((null!=client)&&client.isConnected()){
					client.write(tx);
				}
				System.out.print("tx buf:");
				for(int i=0;i<5;i++){
					System.out.print(" "+Integer.toHexString(tx[i]).toUpperCase()); 
				}
				System.out.println(" ");
				mutexEnble=false;
				while(rx_buf==null){
					rx_buf=shBuf.readLine();
				}
				System.out.println(rx_buf);
				if(rx_buf.equals(ack)){
					System.out.println("tag\t    send cmd successfully");
					return true;
				}else if(rx_buf.equals("error")){
					System.out.println("tag\t    send cmd return false");
					return false;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				mutexEnble=true;
			}
			return false; 
	}

	public static void sendCmdByTCP(String tx,String ack){
		if(!mutexEnble){
			return;
		}
		
		String rx_buf=null;
		//String ackStr = null;
		if((null==client)||(!client.isConnected())){
			return ;
		}
			try {			
				if((null!=client)&&client.isConnected()){
					shOut.print(tx);
					//shOut.close();
				}
				mutexEnble=false;
				while(rx_buf==null){
					rx_buf=shBuf.readLine();
				}
				System.out.println(rx_buf);
				if(rx_buf.equals(ack)){
					System.out.println("report    # send cmd successfully");
					mutexEnble=true;
				}
			} catch (IOException e) {
				mutexEnble=true;
				e.printStackTrace();
			}finally{
				mutexEnble=true;
			}
	}

	public static void sendCmdByTCP(byte[] tx,byte[] ack){
		int mutexTry=0;
		while(!mutexEnble&&mutexTry<10){
			if(mutexTry==0)
			mutexTry++;
			if(mutexTry==10)
				return ;
		}
		
		byte[] rx_buf = new byte[64];
		String rxStr = null;
		int read_len=0;
		if((null==client)||(!client.isConnected())){
			return ;
		}
		try {			
			mutexEnble=false;
			if((null!=client)&&client.isConnected()){
				os.write(tx, 0, 5);
			}
			System.out.print("tx buf:");
			for(int i=0;i<5;i++){
				System.out.print(" "+Integer.toHexString(tx[i]).toUpperCase()); 
			}
			System.out.println(" ");
			//请求更新信号不返回
			if(tx[2]==(byte)0xbf && tx[3]==(byte)0){
				return;
			}

			int OverTime=0;
			
			while((read_len)!=6)  //@ 加入超次数判断
			{  				
				OverTime++;
				System.out.println("going to sleep");
				Thread.sleep(300);
				read_len=is.read(rx_buf);
				System.out.println("read_len="+read_len);
				if(read_len==6)
					break;
				if(OverTime==20){
					System.out.println("连接超时!!");
					break;
				}
			}
			byte temp=rx_buf[0];
			if(rx_buf[0]==(byte)0xee){
				System.out.println("right");
			}
			for(int i = 0;i<6;i++){
				System.out.print(" ,"+ Integer.toHexString(rx_buf[i]).toUpperCase());
				ack[i]=rx_buf[i];
			}
			mutexEnble=true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			mutexEnble=true;
		}
	}	*/
/**
 * 
 * @param txLong
 * @param rxLong 
 * @return true specified send successfully and get appropriate ACK;
 */
	public static boolean sendCmdByTCP(long[] txLong,long[] ackArr){
		int mutexTry=0;
		byte tx[],tmp[],rxTmp[],rxTmp2[];
		long rxArr[] = new long[256];
		//String rxStrBuf = null;
		tmp = new byte[4];
		tx  = new byte[txLong.length*4];
		rxTmp2 = new byte[1024];
		MyClientDemo.getLineNumber(new Exception());
		for(int i = 0 ; i < txLong.length;i++){
			tmp = HelpUtils.longToBytes(txLong[i]);
			System.arraycopy(tmp, 0, tx, i*4, 4);
		}
		System.out.println(" txlong :"+txLong[0]+txLong[1]+txLong[2]);
		MyClientDemo.getLineNumber(new Exception());
		while(!mutexEnble&&mutexTry<10){
			if(mutexTry==0)
			mutexTry++;
			if(mutexTry==10)
				return false;
		}
		
		//byte[] rx_buf = new byte[256];
		//String rxStr = null;
		//int read_len=0;
		if((null==client)||(!client.isConnected())){
			return false;
		}
		try {			
			mutexEnble=false;
			if((null!=client)&&client.isConnected()){
				client.write(tx, 0, tx.length);
			}
			System.out.print("tx buf:");
			for(int i=0;i<5;i++){
				System.out.print(" "+Integer.toHexString(tx[i]).toUpperCase()); 
			}
			System.out.println(" ");
			//请求更新信号不返回
			/*if(tx[2]==(byte)0xbf && tx[3]==(byte)0){
				return;
			}*/
/*			while(rxStrBuf==null){
				rxStrBuf = shBuf.readLine();
			}*/
/*			new Thread(
					new Runnable() {
						@Override
						public void run() {
							try {
								rxStrBuf = shBuf.readLine();
								mutexEnble=true;
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
				}).start();*/
			client.readBlockWithTime(rxTmp2, 5);
			while(!mutexEnble)
				Thread.sleep(500);
			if(rxArr.length < rxStrBuf.length()/4)
				return false;
			rxTmp = rxStrBuf.getBytes();
			
			for(int i=0;i<rxStrBuf.length()/4;i++){
				System.arraycopy(rxTmp, i*4, tmp, 0, 4);
				rxArr[i] = HelpUtils.StrToLong(tmp); 
				System.out.println("long rx :" + rxArr[i]);
			}
			mutexEnble=true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			mutexEnble=true;
		}		
		return false;
	}
	

	static int ImgNum=0;
	static int imgShowNum=0;
	static File dir=null;
	public boolean pic_foced=false;
	/**
	 * 
	 */
	public static void TCPConnectFunction(){
		try {
			if(null!=client&&(client.isConnected()&&(!client.isClosed()))){
				isConnect=false;
				System.out.println("function : TCPConnectFunction  :ready to close socket");
				client.close();
				return ;
			}
			System.out.println("readly to connet IP is " +MyClientDemo.IP + "Port is "+MyClientDemo.PORT);
			//client = new Socket(MyClientDemo.IP, MyClientDemo.PORT);				// 指定服务器
			//SocketAddress socAddress = new InetSocketAddress(MyClientDemo.IP,MyClientDemo.PORT); 
			//client.connect(socAddress, 5000);
			
			//smInfoText.setText("create Socket success");
			new Thread(
				new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						try {
							client = new TCPClient(MyClientDemo.IP, MyClientDemo.PORT);
							/*System.out.println("create Socket success");
							os=client.getOutputStream();	
MyClientDemo.getLineNumber(new Exception());
							shOut = new PrintStream(
									os);				// 打印流输出
							is=client.getInputStream();
MyClientDemo.getLineNumber(new Exception());
							shBuf= new BufferedReader(		//输入流
									new InputStreamReader(
											is));		// 缓冲区读取
							if(null!=client&&(client.isConnected()&&(!client.isClosed()))){
								isConnect=true;
							}*/
						} catch (UnknownHostException e) {
							// TODO Auto-generated catch block
							System.out.println("new socket error!!");
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							System.out.println("new socket error!!");
							e.printStackTrace();
						}	
					}
			}).start();

		} catch (Exception e) {
			MyClientDemo.getLineNumber(e);
			System.out.println("new stream error!!");
			e.printStackTrace();
		}
	}
	
	public static boolean isConnectedTCP(){
		return (AlarmListenerService.client!=null&&AlarmListenerService.client.isConnected()&&!AlarmListenerService.client.isClosed());
	}
	/**
	 * 
	 * @param s
	 * @return
	 */
 	public static Bitmap getPicByTCP(TCPClient s){
		 Bitmap bitmap = null;    
		 FileOutputStream fos=null;
		 FileInputStream fis=null;
		 byte buffer[]=new byte[1360];  
		 byte temp[]=new byte[1360];
		 String path;
		 int count;
		 String SDPATH=Environment.getExternalStorageDirectory()+"/";
		 dir=new File(SDPATH+"zigbeeTCP/");
		 if(!dir.exists())
			 dir.mkdir();
		 path=dir.getAbsolutePath()+"/temp"+ImgNum+".jpg";
		 File file=new File(path);
		 imgShowNum=ImgNum;
		 ImgNum++;
			if((null==s)||(!s.isConnected())){
				//Toast.makeText(MyClientDemo.this, "未连接到智能家居系统", Toast.LENGTH_SHORT).show();
				return null;
			}
		 return null;
	}

    public void addNotification(String str) {
		NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
		// 创建一个Notification
		Notification notification = new Notification();
		// 设置显示在手机最上边的状态栏的图标
		notification.icon =android.R.drawable.ic_menu_today;
		// 当当前的notification被放到状态栏上的时候，提示内容
		notification.tickerText = "日程：";
		notification.defaults=Notification.DEFAULT_SOUND;
		notification.audioStreamType= android.media.AudioManager.ADJUST_LOWER;
	// 点击状态栏的图标出现的提示信息设置
		//notification.setLatestEventInfo(this.context, c.getContent()+" ","  ", pendingIntent);
		Intent intent = new Intent(AlarmListenerService.this, MyClientDemo.class);
		PendingIntent pendIntent = PendingIntent.getActivity(AlarmListenerService.this, 0, intent, 0);  
		//notification.contentIntent = notificationIntent;
		notification.setLatestEventInfo(AlarmListenerService.this, "SMARTHOME 提醒您:", str, pendIntent);
		manager.notify(1, notification);
		
	}
}
