package com.example.happyfridge;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.os.Build;

public class ItemList extends ActionBarActivity {

	public static String token = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_item_list);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}

		new ItemPoolTask().execute();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.item_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_item_list,
					container, false);
			return rootView;
		}
	}

	private class ItemPoolTask extends AsyncTask<Void, Void, String> {

		protected String getASCIIContentFromEntity(HttpEntity entity)
				throws IllegalStateException, IOException {
			InputStream in = entity.getContent();

			StringBuffer out = new StringBuffer();
			int n = 1;
			while (n > 0) {
				byte[] b = new byte[4096];
				n = in.read(b);

				if (n > 0)
					out.append(new String(b, 0, n));
			}

			return out.toString();
		}

		@Override
		protected String doInBackground(Void... params) {
			HttpClient httpClient = new DefaultHttpClient();
			HttpContext localContext = new BasicHttpContext();
			HttpGet httpGet = new HttpGet(MainActivity.domain
					+ "/happyfridge/rest/get-item-pool/");

			Header httpHeader = new BasicHeader("Authorization", "Token "
					+ token);
			httpGet.setHeader(httpHeader);

			String text = null;

			try {
				HttpResponse response = httpClient.execute(httpGet,
						localContext);

				HttpEntity entity = response.getEntity();

				text = getASCIIContentFromEntity(entity);

			} catch (Exception e) {
				return e.getLocalizedMessage();
			}

			return text;
		}

		protected void onPostExecute(String results) {
			if (results != null) {

				try {
					JSONArray jsonArray = new JSONArray(results);

					JSONObject json = null;
					String value = "";
					Button btnItem = null;
					LinearLayout layout = (LinearLayout) findViewById(R.id.item_layout);
					
					layout.removeAllViews();
					
					for (int i = 0; i < jsonArray.length(); i++) {

						json = (JSONObject) jsonArray.get(i);
						value = json.getString("name");

						btnItem = new Button(ItemList.this);
						btnItem.setText(value);
						btnItem.setEnabled(json.getString("status").equals(
								"UCHK"));
						btnItem.setOnClickListener(new ItemButtonClick(json.getString("id")) );
						layout.addView(btnItem);

					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
	}

	private class ItemButtonClick implements OnClickListener {

		private String id = "";

		public ItemButtonClick(String id) {

			this.id = id;

		}

		@Override
		public void onClick(View v) {
			
			new ItemCheckTask().execute();
			

		}
		
		private class ItemCheckTask extends AsyncTask<Void, Void, String> {

			@Override
			protected String doInBackground(Void... params) {
				HttpClient httpClient = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost(MainActivity.domain
						+ "/happyfridge/rest/check-item/");
				Header httpHeader = new BasicHeader("Authorization", "Token "
						+ token);
				httpPost.setHeader(httpHeader);
				
				ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();

				postParameters.add(new BasicNameValuePair("item_id", id));
				

				try {
					httpPost.setEntity(new UrlEncodedFormEntity(postParameters));
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					HttpResponse response = httpClient.execute(httpPost);

					if (response.getStatusLine().getStatusCode() == 200) {
						return "success";
					}

				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
				
				return null;
			
			}
			
			
			@Override
			protected void onPostExecute(String result) {
				if (result!=null)
					new ItemPoolTask().execute();
			}
			
		}
	}

}
