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

import android.os.AsyncTask;
import android.util.Log;

class RetrieveHTMLTask extends AsyncTask<String, Void, String> {
	private Exception exception;

    protected String doInBackground(String... urls) {
        try {
    		String line = null;
            URL url= new URL(urls[0]);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	        while (rd.readLine() != null) {
	            line += rd.readLine();
	        }
	        
	        rd.close();
	        
	        return line;
        } catch (Exception e) {
            this.exception = e;
            return null;
        }
    }

    protected void onPostExecute(String html) {
        if (html == null) {
        	//handle error somehow
        } else {
        	Document doc = Jsoup.parse(html);
        	ArrayList<String> occs = new ArrayList<String>();
        	ArrayList<String> labs = new ArrayList<String>();
        	ArrayList<String> locs = new ArrayList<String>();
        	
        	Elements occsDivs = doc.getElementsByClass("stations");
        	Elements labsDivs = doc.getElementsByClass("labname");
        	Elements locsDivs = doc.getElementsByClass("location");
        	
        	for (Element occ : occsDivs) {
        		occs.add(occ.html());
        	}
        	
        	for (Element lab : labsDivs) {
        		labs.add(lab.html());
        	}
        	
        	for (Element loc : locsDivs) {
        		locs.add(loc.html());
        	}
        	
        	Log.d("getpage", html);
        	//Log.d("getpage", occs.toString());
        	//Log.d("getpage", labs.toString());
        	//Log.d("getpage", locs.toString());
        }
    }
}
