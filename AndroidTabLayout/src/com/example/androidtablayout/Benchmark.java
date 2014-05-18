package com.example.androidtablayout;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.StringTokenizer;



import it.sauronsoftware.ftp4j.*;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


class Comp implements Comparator<TelStat>{

	public int compare(TelStat o1, TelStat o2) {
		
		if(o1.cpuMS < o2.cpuMS)
			return -1;
		else if(o1.cpuMS > o2.cpuMS)
			return 1;
		
		return 0;
	}
	
}

class TelStat{
	public String name;
	public long cpuMS, memoryMS;
	
	TelStat(String name, long cpuMS, long memoryMS)
	{
		this.name = name;
		this.cpuMS = cpuMS;
		this.memoryMS = memoryMS;
	}
	
	public String addToString(String string)
	{
		return string + name + ";" + cpuMS + ";" + memoryMS + "\n";		
	}
	
	public static TelStat getFromString(String string)
	{
		String name;
		long cpuMS, memoryMS;
		StringTokenizer st2 = new StringTokenizer(string, ";");
		
		name = st2.nextToken();
		cpuMS = Long.parseLong(st2.nextToken());
		memoryMS = Long.parseLong(st2.nextToken());
		
		return new TelStat(name, cpuMS, memoryMS);
	}
}

class BenchmarkStats {
    static final String FTP_HOST= "camera-chirnogi.zapto.org";
    static final String FTP_USER = "pi";
    static final String FTP_PASS = "Sorin";
    static final String FTP_FILE = "fisier.txt";
    static final String FTP_PATH = "/hdd/android";
    static final int maxStats = 100;
	ArrayList<TelStat> benchStats;
	
	public BenchmarkStats()
	{
		benchStats = new ArrayList<TelStat>();
	}
	
	public void addTelStat(String name, long cpuMS, long memoryMS)
	{
		TelStat telStat = new TelStat(name, cpuMS, memoryMS);
		benchStats.add(telStat);
		
		while(benchStats.size() > maxStats) {
			benchStats.remove(maxStats);
		}
	}
	
	public void uploadStats() throws IOException
	{
		File file = new File(Environment.getExternalStorageDirectory().getPath() +
				"/download/fisier.txt");
		FTPClient client = new FTPClient();
        
        
        Collections.sort(benchStats, new Comp());
        
        try {
             
            client.connect(FTP_HOST, 21);
            client.login(FTP_USER, FTP_PASS);
            client.setType(FTPClient.TYPE_BINARY);
            client.changeDirectory(FTP_PATH);
            
           // File file1 = new File("/mnt/sdcard/download/fisier1.txt");
           FileOutputStream fout = new FileOutputStream(file);
           
           for(int i = 0; i < benchStats.size(); i++)
           {
        	   String phone = benchStats.get(i).name + ";" + benchStats.get(i).cpuMS + ";" + benchStats.get(i).memoryMS + "\n";
        	   fout.write(phone.getBytes());
           }
                                
            client.upload(file);
            
            // close resources
            fout.close();
            client.logout();           
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                client.disconnect(true);   
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
	}
	
	public void downloadStats()
	{
		File file = new File(Environment.getExternalStorageDirectory().getPath() + 
				"/download/fisier.txt");
		FTPClient client = new FTPClient();
        
        try {
             
            client.connect(FTP_HOST, 21);
            client.login(FTP_USER, FTP_PASS);
            client.setType(FTPClient.TYPE_BINARY);
            client.changeDirectory(FTP_PATH);
            
            client.download("fisier.txt", file);
            
            // read file
            String line;
            
            BufferedReader br = new BufferedReader(new FileReader(file));
            while((line = br.readLine()) != null)
            {
            	String[] elems = line.split(";");
            	try{
            		addTelStat(elems[0], Long.parseLong(elems[1]), Long.parseLong(elems[2]));              	 	
            	}
            	catch(Exception e)
            	{
            		continue;
            	}
            }
            br.close();
            client.logout();
             
        } catch (Exception e) {
            e.printStackTrace();
            try {
                client.disconnect(true);   
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
	}
}

public class Benchmark extends Activity{
	static int noOfInstances = 0;
	private long cpuMS, memoryMS;
	private BenchmarkStats benchmarkStats;
	private static Benchmark instance_;
	private Handler progressBarbHandler = new Handler();
	TextView msg, rating;
	Button btn;
	Long score = 32000L;
	TelStat phone;
	ProgressDialog progressBar;
	private int progressBarStatus = 0;
	int flag;
		
	public void onCreate(Bundle savedInstanceState) {	        
			super.onCreate(savedInstanceState);
	        setContentView(R.layout.benchmark_layout);
	        
	        instance_ = this;
	        
	        btn = (Button)findViewById(R.id.button1);
	        msg = (TextView)findViewById(R.id.textView1);
	        rating = (TextView)findViewById(R.id.textView2);
	        flag = 0;
	                
    }
	
   public void open(View view){
	   if (flag == 0){
		   rating.setVisibility(View.INVISIBLE);
	   	progressBar = new ProgressDialog(view.getContext());
		progressBar.setCancelable(true);
		progressBar.setMessage("Running benchmark...");
		progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressBar.setProgress(0);
		progressBar.setMax(100);
		progressBar.show();
		
		progressBarStatus = 0;
		
		Thread t = new Thread(new Runnable() {

			public void run() {
				// init BenchmarkStats
			    benchmarkStats = new BenchmarkStats();
			       
				while (progressBarStatus < 100) {

					// process some tasks
					progressBarStatus = runBenchmark(progressBarStatus);

					// sleep 1 second (simulating a time consuming task...)
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					// Update the progress bar
					progressBarbHandler.post(new Runnable() {
						public void run() {
							progressBar.setProgress(progressBarStatus);
						}
					});
				}

				// if the file is downloaded,
				if (progressBarStatus >= 100) {

					// sleep 1 seconds, so that you can see the 100%
					try {
						Thread.sleep(1000);
						flag = 1;
						
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					// and then close the progressbar dialog
					progressBar.dismiss();
				}
				
			}
		});
		
		t.start();
		btn.setText("Show results");
	   }
	   else if (flag == 1)
	   {
		   rating.setVisibility(View.VISIBLE);
		   String str = "You phone score is: " + score + "\n\n\nTop 5 phone rated:\n(Higher is better)\n\n";
		   int iter;
		   if (benchmarkStats.benchStats.size() > 5)
			   iter = 5;
		   else
			   iter = benchmarkStats.benchStats.size();
		   for(int i = 0; i < iter; i++)
		   {
			   long sc = 32000 - benchmarkStats.benchStats.get(i).cpuMS;
			   int pos = i+1;
			   str += pos + ". " + benchmarkStats.benchStats.get(i).name + " - " + sc + "\n";
		   }
		   rating.setText(str);
		   flag = 0;		   
		   btn.setText("Begin Test");
	   }
					
	}
   
   public int runBenchmark(int progress) {

		switch (progress){
			case 0:
				benchmarkStats.downloadStats();
				progress += 25;
				break;
			case 25:
				Bench();
				progress += 25;
				break;
			case 50:
				try {
					Thread.sleep(300); //take a nap
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				for (int i = 0; i < benchmarkStats.benchStats.size(); i++)
				{
					if (benchmarkStats.benchStats.get(i).name.compareTo(getDeviceName()) == 0)
					{
						benchmarkStats.benchStats.remove(i);
					}
				}
				benchmarkStats.addTelStat(getDeviceName(), cpuMS, memoryMS);
				phone = new TelStat(getDeviceName(), cpuMS, memoryMS);
				score =32000 - cpuMS;
				progress += 25;
				break;
			case 75:
				try {
					benchmarkStats.uploadStats();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				progress = 100; // finish work
				break;
			default:
				progress = 100; //exit anyway				
		}

		return progress;

	}
		
	public static Benchmark getInstance()
	{
		return instance_;
	}
	
	public void Bench()
	{
		long start;
		
		start = System.nanoTime();
		Benchmark.cpuIntensiveTest();
		cpuMS = (System.nanoTime() - start) / 1000000;
		memoryMS = 3;
		start = System.nanoTime();
		Benchmark.memoryIntensiveTest();
		memoryMS = (System.nanoTime() - start) / 1000000;
	}
	
	// Read device model
    private String getDeviceName() {
  	  String manufacturer = Build.MANUFACTURER;
  	  String model = Build.MODEL;
  	  if (model.startsWith(manufacturer)) {
  	    return model;
  	  } else {
  	    return manufacturer + " " + model;
  	  }
  	}
	
	private static boolean isPrime(int n)
	{
		for(int i = 2; i < n/2; i++) {
			if(n % i == 0)
				return false;
		}
		return true;
	}
	
	public static void cpuIntensiveTest()
	{
		final int n = 100000; /* Problem size */
		int primes = 0;
		
		for(int i = 2; i < n; i++) {
			if(isPrime(i))
				primes++;
		}
		
		if(primes > 0) /* Suppress warning */
			return;
		return;
	}
	
	public static void memoryIntensiveTest()
	{	
		final int n = 100; /* Problem size */
		int[][] A = new int[n][n];
		int[][] B = new int[n][n];
		int[][] C = new int[n][n];
		
		for(int i = 0; i < n; i++) {
			for(int j = 0; j < n; j++) {
				C[i][j] = 0;
				for(int k = 0; k < n; k++) {
					C[i][j] = A[i][k] + B[k][j];
				}
			}
		}
	}
	
	public class MyTransferListener implements FTPDataTransferListener {
		 
        public void started() {
             
            btn.setVisibility(View.GONE);
            // Transfer started
            Toast.makeText(getBaseContext(), " Upload Started ...", Toast.LENGTH_SHORT).show();
            //System.out.println(" Upload Started ...");
        }
 
        public void transferred(int length) {
             
            // Yet other length bytes has been transferred since the last time this
            // method was called
            Toast.makeText(getBaseContext(), " transferred ..." + length, Toast.LENGTH_SHORT).show();
            //System.out.println(" transferred ..." + length);
        }
 
        public void completed() {
             
            btn.setVisibility(View.VISIBLE);
            // Transfer completed
             
            Toast.makeText(getBaseContext(), " completed ...", Toast.LENGTH_SHORT).show();
            //System.out.println(" completed ..." );
        }
 
        public void aborted() {
             
            btn.setVisibility(View.VISIBLE);
            // Transfer aborted
            Toast.makeText(getBaseContext()," transfer aborted , please try again...", Toast.LENGTH_SHORT).show();
            //System.out.println(" aborted ..." );
        }
 
        public void failed() {
             
            btn.setVisibility(View.VISIBLE);
            // Transfer failed
            System.out.println(" failed ..." );
        }
 
    }
}
