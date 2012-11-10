package edu.uwec.cs.hurdad.thesaurize;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class TwodeeActivity extends ListActivity {
	ArrayList<Link> links;
	ArrayAdapter<Link> adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        links = new ArrayList<Link>();
		adapter = new ArrayAdapter<Link>(this, android.R.layout.simple_list_item_1, links);
		setListAdapter(adapter);
        
        final OnItemClickListener onItemClickListener = new OnItemClickListener() {

			public void onItemClick(AdapterView<?> adapterView,
									View itemView,
									int position,
									long id) {
				
				Link link = adapter.getItem(position);
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(link.url));
				startActivity(intent);
			}
        	
        };
        
        getListView().setOnItemClickListener(onItemClickListener);

        new DownloadTask().execute();
	}
	
	private ArrayList<Link> thing() {
		
		ArrayList<Link> test = new ArrayList<Link>();
		
		try {
			
			URL url = new URL("http://www.twodee.org/teaching/cs491-mobile/2012C/homework/links.json");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			InputStream in = connection.getInputStream();
			
			String json = slurp(in);
			
			JSONArray array = new JSONArray(json);
			for (int i = 0; i < array.length(); i++) {
				JSONObject object = array.getJSONObject(i);
				test.add(new Link(object.getString("title"), object.getString("link")));
			}
			in.close();
			
		} catch (MalformedURLException e) {
		} catch (IOException e) {
		} catch (JSONException e) {}
		
		return test;
	}
	
	class DownloadTask extends AsyncTask<String, Void, ArrayList<Link>> {

		@Override
		protected ArrayList<Link> doInBackground(String... params) {
			return thing();
		}

		@Override
		protected void onPostExecute(ArrayList<Link> result) {
			super.onPostExecute(result);
			
			links.clear();
			links.addAll(result);
			adapter.notifyDataSetChanged();
		}
		
	}
	
	public static String slurp(InputStream in) throws IOException {
		StringBuilder builder = new StringBuilder();
		byte[] buffer = new byte[1024];
		int bytesRead = 0;
		
		while((bytesRead = in.read(buffer)) >= 0) {
			builder.append(new String(buffer, 0, bytesRead));
		}
		
		return builder.toString();
	}
	
	private class Link {
		private String title;
		private String url;

		public Link(String title, String url) {
			this.title = title;
			this.url = url;
		}

		@Override
		public String toString() {
			return title;
		}
	}

}
