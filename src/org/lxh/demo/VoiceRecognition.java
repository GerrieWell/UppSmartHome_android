package org.lxh.demo;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Sample code that invokes the speech recognition intent API.
 */
public class VoiceRecognition extends Activity implements OnClickListener {
    
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
    
    private ListView mList;
    private HashMap<String, String> siringIndex[]=new HashMap[7];
    public String number[]={"一","二","三","四","五","六","七"};
    public TCPClient client;
    
    /**
     * Called with the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate our UI from its XML layout description.
        setContentView(R.layout.voice_recognition);

        // Get display items for later interaction
        Button speakButton = (Button) findViewById(R.id.btn_speak);
        mList = (ListView) findViewById(R.id.list);
        
        //siringIndex[0]=new HashMap<String, String>();
        for(int i=0;i<7;i++){
        	siringIndex[i]=new HashMap<String, String>();
        	siringIndex[i].put(number[i],MyClientDemo.wir_str_temp[i]);
        }
        
        // Check to see if a recognition activity is present
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() != 0) {
            speakButton.setOnClickListener(this);
        } else {
            speakButton.setEnabled(false);
            speakButton.setText("Recognizer not present");
        }
        //get Clinet
        client = (TCPClient)getIntent().getSerializableExtra(MyClientDemo.SER_KEY);
    }

    /**
     * Handle the click on the start recognition button.
     */
    public void onClick(View v) {
        if (v.getId() == R.id.btn_speak) {
            startVoiceRecognitionActivity();
        }
    }

    /**
     * Fire an intent to start the speech recognition activity.
     */
    private void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech recognition demo");
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }

    //String str[2][6]=;
    
    /**
     * Handle the results from the recognition activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            // Fill the list view with the strings the recognizer thought it could have heard
            ArrayList<String> matches = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            mList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                    matches));
            for(String tmp:matches){
            	byte cmd[] = {MyClientDemo.CATE_EF,0x05,(byte) 0xa4,0,0};
            	boolean swbegin=tmp.startsWith("打开电器");
            	boolean swend=tmp.startsWith("关闭电器");
            	System.out.println("tmp: "+ tmp);
            	if(tmp.contains("开灯")){
//            	if(tmp.startsWith("开灯")){
            	System.out.println("onActivityResult open lights");
            		if(client!=null){
            			
            			MyClientDemo.state = 0xff;
            			client.clientSendCommand(TCPClient.CLIENT_COMMAND_SETSENSOR);
            		}
            	}else if(tmp.equals("关灯")){
            			//MyClientDemo.sendCmdByTCP(VoiceRecognition.this,cmd,"OK");
            		MyClientDemo.state = 0;
            		if(client!=null)
            			client.clientSendCommand(TCPClient.CLIENT_COMMAND_SETSENSOR);
            	}else{
            		Toast.makeText(VoiceRecognition.this, "未识别指令", Toast.LENGTH_SHORT).show();
            	}
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
    
/*	public boolean sendCmdByTCPInVR(byte tx[], String ack) {

		String rx_buf = null;
		// String ackStr = null;

		try {
			if((null==MyClientDemo.client)||(MyClientDemo.client.isClosed())){
				
			}

				shOut.print(tx);
			
			while (rx_buf == null) {
				rx_buf = shBuf.readLine();
				System.out.println(rx_buf);
			}
			if (rx_buf.equals(ack)) {
				System.out.println("tag\tsend cmd successfully");
				return true;
			}else
				return false;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;

	}*/
}

