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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ab = getActionBar();
        ab.setTitle("Lab Status");

        listView = (ListView) findViewById(R.id.listView1);

        new RetrieveHTMLTask().execute("http://www.eng.uwaterloo.ca/~eng_comp/enginfo/lab_current.html");

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
                new RetrieveHTMLTask().execute("http://www.eng.uwaterloo.ca/~eng_comp/enginfo/lab_current.html");
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

                ArrayList<LabInfo> labInfos = new ArrayList<LabInfo>();

	        	ArrayList<String> labs = new ArrayList<String>();
	        	ArrayList<String> locs = new ArrayList<String>();
	        	ArrayList<String> occs = new ArrayList<String>();

                Document doc = Jsoup.parse(url, 30000);

                Elements labNames = doc.select("span.labname");
                for (Element element : labNames) {
                    labs.add(element.text().toString());
                    Log.w("AAA", element.text().toString());
                }

                Elements labLocations = doc.select("span.location");
                for (Element element : labLocations) {
                    String location = element.text().toString();
                    //Comes in form of , CPH-3218
                    location = location.substring(2);
                    locs.add(location);
                    Log.w("AAA", element.text().toString());
                }

                Elements labOccupancy = doc.select("td.stations");
                for (Element element : labOccupancy) {
                    occs.add(element.text().toString());
                    Log.w("AAA", element.text().toString());
                }

                for (int i = 0; i < labs.size(); ++i)
                {
                    LabInfo info = new LabInfo(labs.get(i), locs.get(i), occs.get(i));
                    labInfos.add(info);
                    Log.w("AAA",labInfos.get(i).getName());

                }
//	            // Read in html from url
//	            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//		        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//                String line;
//		        while ((line = rd.readLine()) != null) {
//			        // Parse html for fields of interest
//
//		        	/* Currently must parse one line at a time because the whole html
//		        	 * cannot be read into a string at once */
//		        	try {
//				        doc = Jsoup.parse(line);
//
//				        labsDivs = doc.getElementsByClass("labname");
//				        locsDivs = doc.getElementsByClass("location");
//				        occsDivs = doc.getElementsByClass("stations");
//
//				        addToList(labsDivs, labs);
//				        addToList(locsDivs, locs);
//				        addToList(occsDivs, occs);
//
//		        	} catch (Exception e) {
//		        		Log.d("scraping", e.toString());
//		        	}
//		        }
//		        rd.close();
	        	
//		        Log.d("scraping", "creating list");
//		        ArrayList<LabInfo> data = new ArrayList<LabInfo>();
//
//		        for (int i=0; i < labs.size(); i++) {
//		        	Log.d("getPage", labs.get(i));
//		        	Log.d("getPage", locs.get(i));
//		        	Log.d("getPage", occs.get(i));
//
//		        	LabInfo lab = new LabInfo(labs.get(i), locs.get(i), occs.get(i));
//		        	Log.d("getPage", "created lab");
//		        	data.add(lab);
//		        	Log.d("getPage", "added lab");
//		        }
//
	        Log.w("scraping", labInfos.toString());

                for (int i = 0; i < labInfos.size(); ++i)
                {
                    Log.w("LabInfos",labInfos.get(i).getName());
                }
	        	return labInfos;
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

            final ArrayList<LabInfo> datas = data;

	        if (data == null) {
	        	//handle error somehow
	        } else {
                runOnUiThread(new Runnable() {
                    public void run() {
                        for (int i = 0; i < datas.size(); ++i)
                        {
                            Log.w("AAA",datas.get(i).getName());
                        }
                        listView.setAdapter(new LabListAdapter(MainActivity.this, datas));
                    }
                });
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

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
