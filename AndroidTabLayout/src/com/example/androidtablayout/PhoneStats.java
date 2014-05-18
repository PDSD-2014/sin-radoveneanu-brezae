package com.example.androidtablayout;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.util.Log;
import android.widget.TextView;

public class PhoneStats extends Activity {
	
	TextView deviceModel, androidVer, totalRam, freeRAM;
	TextView totalSpace, freeSpace, cpu, batteryStat;
	
	Handler handler =new Handler();
	Runnable update = new Runnable() {
			
		public void run() {
			updatableResources();
			handler.postDelayed(this, 1000);
		}
	};
	
	// Show all stats about phone
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phone_layout);
        
      //Show device model
        deviceModel = (TextView) findViewById(R.id.textView2);
        deviceModel.setText(getDeviceName());
        
        //Show android version
        androidVer = (TextView) findViewById(R.id.textView4);
        androidVer.setText(android.os.Build.VERSION.RELEASE);
        
        //Show total ram
        totalRam = (TextView) findViewById(R.id.textView6);
        totalRam.setText(getMemory(0));
        
        //Show free ram
        freeRAM = (TextView) findViewById(R.id.textView8);
        
                        
        //Show total disk space
        totalSpace = (TextView) findViewById(R.id.textView10);
        totalSpace.setText(getTotalMemory());
        
        //Show free disk space
        freeSpace = (TextView) findViewById(R.id.textView12);
                
        //Show CPU model
        cpu = (TextView) findViewById(R.id.textView14);
        cpu.setText(getCPU());
        
        // Show battery status
        batteryStat = (TextView) findViewById(R.id.textView22);
       
        // Update free ram, disk space and battery level
        updatableResources();
        
        handler.postDelayed(update, 1000);
    }
	
	public void updatableResources(){
		
		batteryStat.setText(getBatteryLevel());
		freeSpace.setText(getFreeMemory());
		freeRAM.setText(getMemory(1));
	}
	
	// Read device model
    public String getDeviceName() {
  	  String manufacturer = Build.MANUFACTURER;
  	  String model = Build.MODEL;
  	  if (model.startsWith(manufacturer)) {
  	    return model;
  	  } else {
  	    return manufacturer + " " + model;
  	  }
  	}
    
    // Get total or  free memory from /proc/meminfo
    // 0 = total memory; 1 = free memory
    public String getMemory(int i) {  
        String str1 = "/proc/meminfo", str2;        
        String[] arrayOfString;
        long memory = 0;
        try {
        FileReader localFileReader = new FileReader(str1);
        BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
        
        if(i == 0) {
        	str2 = localBufferedReader.readLine();
        }
        else {
        	localBufferedReader.readLine();
        	str2 = localBufferedReader.readLine();
        }
        
        // parse read line to get memory
        arrayOfString = str2.split("\\s+");
        for (String num : arrayOfString) {
        	Log.i(str2, num + "\t");
        }
        
        memory = Integer.valueOf(arrayOfString[1]).intValue();   
        localBufferedReader.close();
        return Long.toString(memory) + " kB";
        } 
        catch (IOException e) {       
            return "Error open file";
        }
     }
    
 // Get total disk space
    static String getTotalMemory() {
        StatFs statFs = new StatFs(Environment.getRootDirectory().getAbsolutePath());   
        @SuppressWarnings("deprecation")
		long Total = (statFs.getBlockCount() * statFs.getBlockSize()) / 1048576;
        return Long.toString(Total) + " MB";
    }

    // Get free disk space
    static String getFreeMemory() {
        StatFs statFs = new StatFs(Environment.getRootDirectory().getAbsolutePath());
        @SuppressWarnings("deprecation")
		long Free  = (statFs.getAvailableBlocks() * statFs.getBlockSize()) / 1048576;
        return Long.toString(Free) + " MB";
    }
    
    // Compute and return battery level
    public String getBatteryLevel() {
    	IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
    	Intent batteryStatus =   registerReceiver(null, ifilter);
    	
    	int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
    	int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

    	float batteryPct = (level / (float)scale) * 100.0f;
    	
    	return Float.toString(batteryPct) + "%";
    }
     
    // Get cpu model from /proc/cpuinfo
    public String getCPU() {
    	String str1 = "/proc/cpuinfo";
        String str2;
        String[] arrayOfString;
        try {
            FileReader localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
            
            str2 = localBufferedReader.readLine();
            arrayOfString = str2.split("\\s+");
            
            localBufferedReader.close();
            
            // return cpu model and revision
            return arrayOfString[2] + " " + arrayOfString[4]+arrayOfString[5];
        }
        catch (IOException e) {       
            return "Error open file";
        }	
    }
}
