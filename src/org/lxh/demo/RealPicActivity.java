package org.lxh.demo;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import org.lxh.demo.MyClientDemo.MessageHandler;

import zigbeeNet.NwkDesp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

public class RealPicActivity extends Activity {
	
	public static final String REALPIC_ACTION_UNEXPECTED = "unexp";
	protected static final int UI_MESG_PIC_VIEW = 0;
	PopupWindow monitorPicWin;
	//View monitorPicView;
	ImageView monitor_pic=null;
	Button refresh_button;
	public static final int PIC_CONNECT = 1;
	public static final int PIC_SENDCMD = 2;
	
	private Handler mPicHandler;
	public Handler realPicUI_Handler;
	public Bitmap bitmap;
	private TCPPicThread tThread;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.monitor_pic);					// 调用布局
		monitor_pic=(ImageView) findViewById(R.id.imageView1);
		refresh_button=(Button) findViewById(R.id.refresh_button_monitor);
		//刷新实时图片
		
		
		refresh_button.setOnClickListener(new OnClickListener() {
			Bitmap bitmap ;
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
/*				new Thread(
						new Runnable() {
							@Override
							public void run() {
								bitmap = AlarmListenerService.getPicByTCP();//,RealPicActivity.this);

							}
					}).start();*/
				Message m = mPicHandler.obtainMessage();
				m.arg1 = PIC_SENDCMD;
				mPicHandler.sendMessage(m);
			}
		});
		((Button)findViewById(R.id.previous_pic)).setOnClickListener(new OnClickListener(
				) {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				AlarmListenerService.imgShowNum--;
				if(AlarmListenerService.imgShowNum<0){
					Toast.makeText(RealPicActivity.this, "已经是第一张", Toast.LENGTH_SHORT).show();
					AlarmListenerService.imgShowNum=0;
					return;
				}
				Bitmap bitmap = null;    
				String path=null;
				path=AlarmListenerService.dir.getAbsolutePath()+"/temp"+AlarmListenerService.imgShowNum+".png";
System.out.println("pervious path is :"+path);
				bitmap=BitmapFactory.decodeFile(path);
				if(!(bitmap==null))
					monitor_pic.setImageBitmap(bitmap);
			}
		});
		((Button)findViewById(R.id.button_pic_cancel)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//monitorPicWin.dismiss();
			}
		});
		//fill_pic_view();
		//handle 
		
		tThread = new TCPPicThread();
		tThread.start();
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Message uiMessage = mPicHandler.obtainMessage();
		uiMessage.arg1 = PIC_CONNECT;
		mPicHandler.sendMessage(uiMessage);
		Looper looper =Looper.getMainLooper();
		realPicUI_Handler = new Handler(){
	        public void handleMessage(Message msg) {
	            //处理收到的消息，把天气信息显示在title上
	        	
	        	switch(msg.arg1){
	        	case UI_MESG_PIC_VIEW:
					if(bitmap!=null){
						monitor_pic.setImageBitmap(bitmap);
					}
					else
						System.out.println("do nothing");
	        		break;
	        	
	        	}
	        }
		};
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		mPicHandler.getLooper().quit();
		super.onDestroy();
	}


	public class TCPPicThread extends Thread {
		private Selector selector;
		private byte[] rxTmp = new byte[4];
		private long [] rxLongs = null;
		private NwkDesp pNwkDesp2;
		private Thread t;
		public TCPPicThread() {
		}
	
		public Thread getT() {
			return t;
		}

		public void run() {
			Looper.prepare();
			mPicHandler = new Handler(){
	            public void handleMessage(Message msg) {
	            	/* 使用两个参数传递 */
	            	if(msg.arg1 == PIC_CONNECT){
	            		AlarmListenerService.connectQtServer();
	            		return;
	            	}else if(msg.arg1 == PIC_SENDCMD){
	            		bitmap = AlarmListenerService.getPicByTCP();
	            		Message mes = realPicUI_Handler.obtainMessage();
	            		mes.arg1 = UI_MESG_PIC_VIEW;
	            		realPicUI_Handler.sendMessage(mes);
	            		//task.execute("PIC_UPDATE");
	            	}
	            }
			};
			Looper.loop();
			AlarmListenerService.closeQtServer();
		}
	}
	
	
/*	public class UITask extends AsyncTask<String, Integer, String> {
        // 可变长的输入参数，与AsyncTask.exucute()对应
        ProgressDialog pdialog;
        InputStreamReader isReader;
        public UITask(Context context){
        	//isReader = new InputStreamReader(is);
        }
		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			//AlarmListenerService.connectQtServer();
			if(params[0].equals("PIC_UPDATE")){
				if(bitmap!=null){
					monitor_pic.setImageBitmap(bitmap);
				}
				else
					System.out.println("do nothing");
			}
			return null;
		}
		
		public int readFuully(InputStream is,byte[] buf,int desiredByteCount) throws IOException{
			int actualByteCount = 0;
			while(actualByteCount < desiredByteCount){
				actualByteCount += is.read(buf,actualByteCount,desiredByteCount - actualByteCount);
			}
			
			return actualByteCount;
		}*/
		
 }
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
/*	public void fill_pic_view(){
		LayoutInflater inflater=LayoutInflater.from(RealPicActivity.this);
		LayoutParams p=new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		monitorPicView=inflater.inflate(R.layout.monitor_pic, null);
		monitorPicWin=new PopupWindow(RealPicActivity.this);
		monitorPicWin.setContentView(monitorPicView);
		monitorPicWin.setWidth(LayoutParams.FILL_PARENT);
		monitorPicWin.setHeight(LayoutParams.WRAP_CONTENT);
		monitorPicWin.setAnimationStyle(android.R.style.Animation_Dialog);

	}*/
	
/*	private class monitorPicListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(monitorPicWin==null)
				return;
			monitorPicWin.setFocusable(true);
			monitorPicWin.showAtLocation(button_lights, Gravity.CENTER, 0, 0);
			if(AlarmListenerService.client.isConnected()&&!AlarmListenerService.client.isClosed()){
	
				AlarmListenerService.getPicByTCP(AlarmListenerService.client);
			}		
			return ;
			
		}
	}*/
/*	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);
		if(pic_foced){
			monitorPicWin.showAtLocation(button_lights, Gravity.CENTER, 0, 0);
			pic_foced=false;
		}
	}*/

