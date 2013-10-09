package com.example.uwenglabs;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
        new RetrieveHTMLTask().execute("http://www.eng.uwaterloo.ca/~eng_comp/enginfo/lab_current.shtml");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private void onScrapingComplete(ArrayList<ArrayList<String>> data) {
		
	}
	
	class RetrieveHTMLTask extends AsyncTask<String, Void, ArrayList<ArrayList<String>>> {
		private Exception exception;
		private ProgressDialog pd = new ProgressDialog(MainActivity.this);
		
		@Override
		protected void onPreExecute() {
			this.pd.setMessage("Please wait...");
			this.pd.show();
		}

		@Override
	    protected ArrayList<ArrayList<String>> doInBackground(String... urls) {
	        try {
	            URL url= new URL(urls[0]);
	            Document doc;
	        	ArrayList<String> labs = new ArrayList<String>();
	        	ArrayList<String> locs = new ArrayList<String>();
	        	ArrayList<String> occs = new ArrayList<String>();	        	
	        	Elements labsDivs, locsDivs, occsDivs;
	            
	            // Read in html from url
	            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		        while (rd.readLine() != null) {
			        // Parse html for fields of interest
		        	
		        	/* Currently must parse one line at a time because the whole html 
		        	 * cannot be read into a string at once */

		        	doc = Jsoup.parse(rd.readLine());
		        	
		        	labsDivs = doc.getElementsByClass("labname");
		        	locsDivs = doc.getElementsByClass("location");
		        	occsDivs = doc.getElementsByClass("stations");
		        	
		        	addToList(labsDivs, labs);
		        	addToList(locsDivs, locs);
		        	addToList(occsDivs, occs);
		        }
		        rd.close();
	        	
	        	ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
	        	data.add(labs);
	        	data.add(locs);
	        	data.add(occs);
		        
	        	return data;
	        } catch (Exception e) {
	            this.exception = e;
	            return null;
	        }
	    }

		@Override
	    protected void onPostExecute(ArrayList<ArrayList<String>> data) {
			if (pd.isShowing()) {
				pd.dismiss();
			}
			
	        if (data == null) {
	        	//handle error somehow
	        } else {
	        	MainActivity.this.onScrapingComplete(data);
	        }
	    }
		
		private void addToList(Elements els, ArrayList<String> list) {
			if (els.size() > 0) {
				for (Element el : els) {
					list.add(el.html());
				}
			}
		}
	}
}
