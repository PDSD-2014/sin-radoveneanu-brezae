package com.example.androidtablayout;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

public class Statistics extends Activity{
	
	TextView cpuPow1, cpuPow2, cpuPow3, cpuPow4, cpuPow5;
	TextView cpuTime1, cpuTime2, cpuTime3, cpuTime4, cpuTime5;
	TextView totalTraffic1, totalTraffic2, totalTraffic3;
	TextView totalTraffic4, totalTraffic5, wifiPow1, wifiPow2;
	TextView wifiPow3, wifiPow4, wifiPow5;
	
	ArrayList<AppRes> data;
	static int noOfInstances = 0;
	
	private static Statistics instance_;
	
	Handler handler =new Handler();
	Runnable update = new Runnable() {
			
		public void run() {
			updateValues();
			setValues();
			handler.postDelayed(this, 60000);
		}
	};
	
	public void onCreate(Bundle savedInstanceState) {
	        
			super.onCreate(savedInstanceState);
	        setContentView(R.layout.tables_layout);
	        
	        instance_ = this;
	        
	        
	        	data = new ArrayList<AppRes>(5);
	        	
	        	//Fill with empty data
	        	for(int i=0; i<5; i++)
	        		data.add(new AppRes());
	        
	        cpuPow1 = (TextView) findViewById(R.id.textView5);
	        cpuPow2 = (TextView) findViewById(R.id.textView8);
	        cpuPow3 = (TextView) findViewById(R.id.textView11);
	        cpuPow4 = (TextView) findViewById(R.id.textView14);
	        cpuPow5 = (TextView) findViewById(R.id.textView17);
	        
	        cpuTime1 = (TextView) findViewById(R.id.textView6);
	        cpuTime2 = (TextView) findViewById(R.id.textView9);
	        cpuTime3 = (TextView) findViewById(R.id.textView12);
	        cpuTime4 = (TextView) findViewById(R.id.textView15);
	        cpuTime5 = (TextView) findViewById(R.id.textView18);
	        
	        totalTraffic1 = (TextView) findViewById(R.id.textView23);
	        totalTraffic2 = (TextView) findViewById(R.id.textView26);
	        totalTraffic3 = (TextView) findViewById(R.id.textView29);
	        totalTraffic4 = (TextView) findViewById(R.id.textView32);
	        totalTraffic5 = (TextView) findViewById(R.id.textView35);
	        
	        wifiPow1 = (TextView) findViewById(R.id.textView24);
	        wifiPow2 = (TextView) findViewById(R.id.textView27);
	        wifiPow3 = (TextView) findViewById(R.id.textView30);
	        wifiPow4 = (TextView) findViewById(R.id.textView33);
	        wifiPow5 = (TextView) findViewById(R.id.textView36);
	        
	        updateValues();
	        setValues();        
	        
	        handler.postDelayed(update, 60000);
	}
	
	public void setValues(){
				
		// Show cpu power
		cpuPow1.setText(getCpuPow(data, 0));
		cpuPow2.setText(getCpuPow(data, 1));
		cpuPow3.setText(getCpuPow(data, 2));
		cpuPow4.setText(getCpuPow(data, 3));
		cpuPow5.setText(getCpuPow(data, 4));
		
		// Show cpu time
		cpuTime1.setText(data.get(0).cpuTime.toString()); 
		cpuTime2.setText(data.get(1).cpuTime.toString());
		cpuTime3.setText(data.get(2).cpuTime.toString());
		cpuTime4.setText(data.get(3).cpuTime.toString());
		cpuTime5.setText(data.get(4).cpuTime.toString());
		
		// Show total trafic in kB
		totalTraffic1.setText(getTotalTraffic(data, 0));
		totalTraffic2.setText(getTotalTraffic(data, 1));
		totalTraffic3.setText(getTotalTraffic(data, 2));
		totalTraffic4.setText(getTotalTraffic(data, 3));
		totalTraffic5.setText(getTotalTraffic(data, 4));
		
		//Show WiFi power
		wifiPow1.setText(getWifiPow(data, 0));
		wifiPow2.setText(getWifiPow(data, 1));
		wifiPow3.setText(getWifiPow(data, 2));
		wifiPow4.setText(getWifiPow(data, 3));
		wifiPow5.setText(getWifiPow(data, 4));
	}
	
	public void updateValues()
	{
		if(data.size() > 1)
			data.remove(0);
		
		//Read data
		AndroidPowerCollector apc = new AndroidPowerCollector();
		AppRes resources = apc.processApplicationUsage(1);
		
		if(resources == null)
		{
			resources = new AppRes();
		}
		
		//Store data
		data.add(resources);
	}
	
	public String getTotalTraffic(ArrayList<AppRes> data, int index)
	{
		Long traffic = data.get(index).bytesRecv + data.get(index).bytesSend;
		
		//return total trafic in KB
		Double tmp = traffic.doubleValue()/1024.0;
		
		DecimalFormat df = new DecimalFormat("#.##");
		return df.format(tmp) + " kB";
	}
	
	public String getWifiPow(ArrayList<AppRes> data, int index)
	{
		Double wifipow = data.get(index).wifiPow;
		DecimalFormat df = new DecimalFormat("#.###");
		
		return df.format(wifipow) + " J";
	}
	
	public String getCpuPow(ArrayList<AppRes> data, int index)
	{
		Double cpupow = data.get(index).cpuPow;
		DecimalFormat df = new DecimalFormat("#.####");
		
		return df.format(cpupow);
	}
	
	public static Statistics getInstance()
	{
		return instance_;
	}

}
