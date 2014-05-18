package com.example.androidtablayout;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

@SuppressWarnings("deprecation")
public class AndroidTabLayoutActivity extends TabActivity {
    
	private static AndroidTabLayoutActivity instance_;	
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	   	    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        instance_ = this;        
        
        TabHost tabHost = getTabHost();
        
        // Phone Stats tab
        TabSpec phoneStats = tabHost.newTabSpec("Phone Stats");
        phoneStats.setIndicator("Phone Stats", getResources().getDrawable(R.drawable.icon_phone));
        Intent newTabIntent = new Intent(this, PhoneStats.class);
        phoneStats.setContent(newTabIntent);
        
        // Packages Stats Tab
        TabSpec packages = tabHost.newTabSpec("Packages");
        packages.setIndicator("Packages", getResources().getDrawable(R.drawable.icon_package));
        Intent packagesIndent = new Intent(this, PackageStats.class);
        packages.setContent(packagesIndent);
        
        // Resources App Tab
        TabSpec appRes = tabHost.newTabSpec("Application");
        // setting Title and Icon for the Tab
        appRes.setIndicator("Application", getResources().getDrawable(R.drawable.icon_appres));
        Intent appResIntent = new Intent(this, ResourceApp.class);
        appRes.setContent(appResIntent);
        
        // Tables tab
        TabSpec statistics = tabHost.newTabSpec("Statistics");
        // setting Title and Icon for the Tab
        statistics.setIndicator("Statistics", getResources().getDrawable(R.drawable.icon_statistics));
        Intent statisticsIndent = new Intent(this, Statistics.class);
        statistics.setContent(statisticsIndent);
        
     // Tables tab
        TabSpec chart = tabHost.newTabSpec("Charts");
        // setting Title and Icon for the Tab
        chart.setIndicator("Charts", getResources().getDrawable(R.drawable.icon_charts));
        Intent chartIndent = new Intent(this, Charts.class);
        chart.setContent(chartIndent);
        
        // Benchmark
        TabSpec benchmark = tabHost.newTabSpec("Benchmark");
        //setting Title and Icon for the Tab
        benchmark.setIndicator("Benchmark", getResources().getDrawable(R.drawable.icon_benchmark));
        Intent benchmarkIntent = new Intent(this, Benchmark.class);
        benchmark.setContent(benchmarkIntent);
        
        // Adding all TabSpec to TabHost
        tabHost.addTab(phoneStats);
        tabHost.addTab(packages); 
        tabHost.addTab(appRes);
        tabHost.addTab(statistics);
        tabHost.addTab(chart);
        tabHost.addTab(benchmark);       
    }
    
    @Override
	protected void onPause() { 
		super.onPause();		
	}

	@Override
	protected void onResume() { 
		super.onResume();
	}
    
    public static AndroidTabLayoutActivity getInstance() {
		return instance_;
	}
}