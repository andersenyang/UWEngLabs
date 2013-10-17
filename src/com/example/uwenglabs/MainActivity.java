package com.example.uwenglabs;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {

    ActionBar ab;
    ListView listView;
    LabListAdapter adapter;
    ArrayList<LabInfo> labInfoList;

    private final String SITE_URL = "http://www.eng.uwaterloo.ca/~eng_comp/enginfo/lab_current.html";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ab = getActionBar();
        ab.setTitle("Engineering Labs");
        ab.setDisplayShowHomeEnabled(false);

        listView = (ListView) findViewById(R.id.listView1);

        new RetrieveHTMLTask().execute(SITE_URL);

        labInfoList = new ArrayList<LabInfo>();
        adapter = new LabListAdapter(this, labInfoList);
        listView.setAdapter(adapter);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                new RetrieveHTMLTask().execute(SITE_URL);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class LabListAdapter extends ArrayAdapter<LabInfo> {
        Context context;
        ArrayList<LabInfo> labInfoList;
        LabListAdapter adapter;

        LabListAdapter(Context context, ArrayList<LabInfo> labInfoList) {
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
            labName.setText(labInfoList.get(position).getName());
            labLocation.setText(labInfoList.get(position).getLoc());
            labOccupancy.setText(labInfoList.get(position).getOcc());

            return rowView;
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }
    }
	
	private void onScrapingComplete(ArrayList<LabInfo> data) {
        labInfoList = data;
		adapter.notifyDataSetChanged();
	}
	
	class RetrieveHTMLTask extends AsyncTask<String, Void, ArrayList<LabInfo>> {
		private ProgressDialog pd = new ProgressDialog(MainActivity.this);
		
		@Override
		protected void onPreExecute() {
			this.pd.setMessage("Retrieving data...");
			this.pd.show();
		}

		@Override
	    protected ArrayList<LabInfo> doInBackground(String... urls) {
	        try {
	            URL url= new URL(urls[0]);

                ArrayList<LabInfo> labInfoList = new ArrayList<LabInfo>();

	        	ArrayList<String> labs = new ArrayList<String>();
	        	ArrayList<String> locs = new ArrayList<String>();
	        	ArrayList<String> occs = new ArrayList<String>();

                Document doc = Jsoup.parse(url, 10000);

                Elements labNames = doc.select("span.labname");
                for (Element element : labNames) {
                    labs.add(element.text().toString());
                }

                Elements labLocations = doc.select("span.location");
                for (Element element : labLocations) {
                    String location = element.text().toString();
                    //Comes in form of , CPH-3218
                    location = location.substring(2);
                    locs.add(location);
                }

                Elements labOccupancy = doc.select("td.stations");
                for (Element element : labOccupancy) {
                    occs.add(element.text().toString());
                }

                for (int i = 0; i < labs.size(); ++i)
                {
                    LabInfo info = new LabInfo(labs.get(i), locs.get(i), occs.get(i));
                    labInfoList.add(info);
                }
	        	return labInfoList;
	        } catch (Exception e) {
	            return null;
	        }
	    }

		@Override
	    protected void onPostExecute(ArrayList<LabInfo> data) {
			if (pd.isShowing()) {
				pd.dismiss();
			}

            final ArrayList<LabInfo> dataList = data;

	        if (data == null) {
	        	//handle error somehow
	        } else {
                runOnUiThread(new Runnable() {
                    public void run() {
                        listView.setAdapter(new LabListAdapter(MainActivity.this, dataList));
                    }
                });
	        }
	    }
	}

    @Override
    protected void onPause() {
        super.onPause();
        finish();  //We want to finish because everytime the user comes back, we want to refresh the page
    }
}
