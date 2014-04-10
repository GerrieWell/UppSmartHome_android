package org.lxh.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

public class RealPicActivity extends Activity {
	
	public static final String REALPIC_ACTION_UNEXPECTED = "unexp";
	PopupWindow monitorPicWin;
	//View monitorPicView;
	ImageView monitor_pic=null;
	Button refresh_button;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.monitor_pic);					// 调用布局
		monitor_pic=(ImageView) findViewById(R.id.imageView1);
		refresh_button=(Button) findViewById(R.id.refresh_button_monitor);
		//刷新实时图片
		refresh_button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				/*Bitmap bitmap=AlarmListenerService.getPicByTCP(AlarmListenerService.client);
				if(bitmap!=null){
					monitor_pic.setImageBitmap(bitmap);
				}*/
				System.out.println("do nothing");
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
				path=AlarmListenerService.dir.getAbsolutePath()+"/temp"+AlarmListenerService.imgShowNum+".jpg";
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
		String temp=getIntent().getAction();
		if(temp!=null&&temp.equals(REALPIC_ACTION_UNEXPECTED)){
			//对话框是否要获取图像
			//Dialog.
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("收到警告,要查看实时图片吗?")
			.setCancelable(false)
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					if(AlarmListenerService.client!=null&&AlarmListenerService.client.isConnected()&&!AlarmListenerService.client.isClosed()){
						MyClientDemo.getLineNumber(new Exception());
						AlarmListenerService.getPicByTCP(AlarmListenerService.client);
					}
				}
			})
			.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			}).show();
			return;
		}
		if(AlarmListenerService.isConnectedTCP()){
			//AlarmListenerService.getPicByTCP(AlarmListenerService.client);
			;
		}else{
			MyClientDemo.toastShow(RealPicActivity.this, "未连接");
		}
		
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
}
