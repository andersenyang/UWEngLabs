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
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

    ActionBar ab;
    ListView listView;
    LabListAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ab = getActionBar();
        ab.setTitle("Lab Status");

        listView = (ListView) findViewById(R.id.listView1);

        new RetrieveHTMLTask().execute("http://www.eng.uwaterloo.ca/~eng_comp/enginfo/lab_current.shtml");

        ArrayList<String> labInfoList = new ArrayList<String>();
        adapter = new LabListAdapter(this, labInfoList);
        listView.setAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

    class LabListAdapter extends ArrayAdapter<String> {
        Context context;
        ArrayList<String> labInfoList;
        LabListAdapter adapter;

        LabListAdapter(Context context, ArrayList<String> labInfoList) {
            super(context, R.layout.row_list, labInfoList);
            this.context = context;
            this.labInfoList = labInfoList;
            adapter = this;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = LayoutInflater.from(context).inflate(R.layout.row_list, null);

            final TextView labName = (TextView) rowView.findViewById(R.id.tv_lv_LabName);
            TextView labLocation = (TextView) rowView.findViewById(R.id.tv_lv_LabLocation);
            TextView labOccupancy = (TextView) rowView.findViewById(R.id.tv_lv_LabOccupancy);

            //ImageView imageView = (ImageView) rowView.findViewById(R.id.btn_lv_delete);
            labName.setText(labInfoList.get(position));
            labLocation.setText(labInfoList.get(position));
            labOccupancy.setText(labInfoList.get(position));

            return rowView;
        }
    }
	
	private void onScrapingComplete(ArrayList<LabInfo> data) {
		
	}
	
	class RetrieveHTMLTask extends AsyncTask<String, Void, ArrayList<LabInfo>> {
		private Exception exception;
		private ProgressDialog pd = new ProgressDialog(MainActivity.this);
		
		@Override
		protected void onPreExecute() {
			this.pd.setMessage("Please wait...");
			this.pd.show();
		}

		@Override
	    protected ArrayList<LabInfo> doInBackground(String... urls) {
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
		        	Log.d("scraping", labsDivs.toString());
		        }
		        rd.close();
	        	
		        ArrayList<LabInfo> data = new ArrayList<LabInfo>();
		        
		        for (int i=0; i < labs.size(); i++) {
		        	data.add(new LabInfo(labs.get(i), locs.get(i), occs.get(i)));
		        }
		        
		        Log.d("scraping", data.toString());
	        	return data;
	        } catch (Exception e) {
	            this.exception = e;
	            return null;
	        }
	    }

		@Override
	    protected void onPostExecute(ArrayList<LabInfo> data) {
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
