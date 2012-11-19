package com.android.xbmc.offline.content.scanner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class MainActivity extends Activity {

	ArrayList<String> arraylist = new ArrayList<String>();
	ArrayAdapter<String> adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final Button butttonSave = (Button) findViewById(R.id.buttonScan);
		final ListView listview = (ListView) findViewById(R.id.listScannedItems);

		// set up list view
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, arraylist);
		listview.setAdapter(adapter);

		// set up button
		Button.OnClickListener mScan = new Button.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(
						"com.google.zxing.client.android.SCAN");
				intent.putExtra("SCAN_MODE", "ONE_D_MODE");
				startActivityForResult(intent, 0);
			}
		};
		butttonSave.setOnClickListener(mScan);

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				String contents = intent.getStringExtra("SCAN_RESULT");
				String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
				// Handle successful scan
				// addnewEntry(contents);
				getInfoFromProvider(contents);
			} else if (resultCode == RESULT_CANCELED) {
				// Handle cancel
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void getInfoFromProvider(String upc) {
		final String googleDeveloperkey = "AIzaSyBiLtxFVVyKuxiyriVNAWtSn5_T-g34eh0";
		final String url = "https://www.googleapis.com/shopping/search/v1/public/products?key="
				+ googleDeveloperkey + "&country=uk&maxResults=1&q=" + upc;
		String googleResponse = "";
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(url);
			HttpResponse res = client.execute(request);

			if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				googleResponse = EntityUtils.toString(res.getEntity(), "UTF-8");
			}

		} catch (Exception e) {
			System.out.println("Exp=" + e);
		}

		JSONObject jObject;
		try {
			jObject = new JSONObject(googleResponse);
			JSONArray itemsArray = jObject.getJSONArray("items");
			JSONObject itemObject = itemsArray.getJSONObject(0);
			JSONObject productObject = itemObject.getJSONObject("product");
			String productTitle = productObject.getString("title");
			addnewEntry(productTitle);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void addnewEntry(String entry) {
		arraylist.add(0, entry);
		createFolderStructure(entry);
		adapter.notifyDataSetChanged();
	}

	public void createFolderStructure(final String title) {
		final File root = Environment.getExternalStorageDirectory();
		final String baseXBMCFolder = "XBMC";
		
		final File localFolderPath = new File(root.getPath() + "/"
				+ baseXBMCFolder + "/" + smbifyPath(title));

		final File localFilePath = new File(root.getPath() + "/"
				+ baseXBMCFolder + "/" + smbifyPath(title) + "/" + smbifyPath(title) + ".disc" );


		if (!localFolderPath.exists()) {
			localFolderPath.mkdirs();
			try {
				localFilePath.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	
	private String smbifyPath(final String input) {
		String returnVal = input;

		// TODO replace with proper library
		if (returnVal.charAt(0) != '/') {
			returnVal = '/' + returnVal;
		}
//		final int length = returnVal.length() - 1;
//		if (returnVal.charAt(length) != '/') {
//			returnVal = returnVal + '/';
//		}

		if (returnVal.contains(" ")) {
			returnVal.replaceAll(" ", "\\ ");
		}
		if (returnVal.contains("//")) {
			returnVal.replaceAll("/*", "/");
		}
		return returnVal;
	}

}
