package com.example.androidtablayout;

import java.text.DecimalFormat;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

public class ResourceApp extends Activity {
	
	// Resource tab view elements
	TextView aplicationName, cpuPow, cpuTime, gpsPower;
	TextView gpsTime, bytesRecv, bytesSent, wifiPow;
		
	private static ResourceApp instance_;
		
	Handler handler =new Handler();
	Runnable update = new Runnable() {
			
		public void run() {
			showResource();
			handler.postDelayed(this, 60000);
		}
	};
	
	// Show application that use most resources 	
	public void onCreate(Bundle savedInstanceState) {
	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.appres_layout);
        instance_ = this;
                 
        showResource();
        
        handler.postDelayed(update, 60000);        
    }
	
	public static ResourceApp getInstance()
	{
		return instance_;
	}
	
	public void showResource()
	{
		AndroidPowerCollector apc = new AndroidPowerCollector();
		AppRes resources = apc.processApplicationUsage(0);
				
		DecimalFormat df = new DecimalFormat("#.####");
		
		aplicationName = (TextView) findViewById(R.id.textView1);
		aplicationName.setText(resources.appName);
        
		cpuPow = (TextView) findViewById(R.id.textView4);
        cpuPow.setText(df.format(resources.cpuPow));
        
        cpuTime = (TextView) findViewById(R.id.textView6);
        cpuTime.setText(resources.cpuTime.toString());
        
        gpsPower = (TextView) findViewById(R.id.textView8);
        gpsPower.setText(df.format(resources.gpsPow));
        
        gpsTime = (TextView) findViewById(R.id.textView10);
        gpsTime.setText(resources.gpsTime.toString());
                        
        bytesRecv = (TextView) findViewById(R.id.textView12);
        bytesRecv.setText(resources.bytesRecv.toString());
        
        bytesSent = (TextView) findViewById(R.id.textView14);
        bytesSent.setText(resources.bytesSend.toString());
        
        wifiPow = (TextView) findViewById(R.id.textView15);
        wifiPow.setText(df.format(resources.wifiPow)); 
		
	}
	
}
