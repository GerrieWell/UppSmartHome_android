package org.lxh.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.sax.StartElementListener;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
	/*receiver在一个新的堆栈,MyClientDemo的静态client不能用?*/

	private boolean alarmRecved=false;
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub

		Toast.makeText(context, "时间到!", Toast.LENGTH_SHORT).show();
		if(AlarmListenerService.client==null)
			System.out.println("receiver\t# null");

		context.startService(intent);
		alarmRecved=true;
		System.out.println("broadcast get :hex:"+Integer.toHexString(intent.getByteExtra("room_light", (byte)0)));
		intent.setClass(context,AlarmListenerService.class);
		intent.putExtra("alarmRecved", alarmRecved);
		context.startService(intent);
		//startService(new Intent(MyClientDemo.this,AlarmListenerService.class));
		//Intent i=new Intent(context,MyClientDemo.class);
		
	}

}
