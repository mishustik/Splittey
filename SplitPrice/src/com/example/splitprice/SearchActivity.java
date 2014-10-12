package com.example.splitprice;

import java.io.IOException;
import java.util.List;

import org.osmdroid.util.GeoPoint;

import android.app.Activity;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SearchActivity extends ListActivity{
	
	static GeoPoint location;
	String[] searchResults;
	GeoPoint[] searchGPResults;
		
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_results);
		
		ListView lv = getListView();
      	lv.setOnItemClickListener(new OnItemClickListener(){
      		@Override public void onItemClick(AdapterView<?> arg0, View arg1,int position, long arg3){ 
      	        
      			//resultLocation.setCoordinates(foundGeocode.get(i).getLatitude(), foundGeocode.get(i).getLongitude());
      			Intent resultIntent = new Intent(SearchActivity.this, MainActivity.class);
      			resultIntent.putExtra("name", searchResults[position]);
      			resultIntent.putExtra("location",(Parcelable) searchGPResults[position]);
      			setResult(Activity.RESULT_OK, resultIntent);
      			finish();   			
      		}
      	});
		
		// Perform search for the request in the intent 
		Intent intent = getIntent();
		String query = intent.getStringExtra(SearchManager.QUERY);
		new doMySearch().execute(query);
	}
	
	class doMySearch extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {
        	try {
				List<Address> foundGeocode = new Geocoder(SearchActivity.this).getFromLocationName(params[0], 5);
				searchResults = new String[foundGeocode.size()];
				searchGPResults = new GeoPoint[foundGeocode.size()];
				for(int i = 0;i < foundGeocode.size();++i){
					StringBuilder tempAddress = new StringBuilder();
					if(foundGeocode.get(i).getFeatureName() != null){
						tempAddress.append(foundGeocode.get(i).getFeatureName());
					}
					if(foundGeocode.get(i).getAdminArea() != null){
						tempAddress.append(", " + foundGeocode.get(i).getAdminArea());
					}
					if(foundGeocode.get(i).getSubAdminArea() != null){
						tempAddress.append(", " + foundGeocode.get(i).getSubAdminArea());
					}
					if(foundGeocode.get(i).getThoroughfare() != null){
						tempAddress.append(", " + foundGeocode.get(i).getThoroughfare());
					}
					if(foundGeocode.get(i).getCountryName() != null){
						tempAddress.append(", " + foundGeocode.get(i).getCountryName());
					}
					if(foundGeocode.get(i).getPostalCode() != null){
						tempAddress.append(", " + foundGeocode.get(i).getPostalCode());
					}
					//saving addresses of each item 
					searchResults[i] = tempAddress.toString();
					//saving GPS coordinates of each found item
					int tempLat = (int) (foundGeocode.get(i).getLatitude() * 1e6);
					int tempLng = (int) (foundGeocode.get(i).getLongitude() * 1e6);
					searchGPResults[i] = new GeoPoint(tempLat,tempLng);
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
            return "Loaded";
        }
        @Override
        protected void onPostExecute(String result) {
            if(result.equals("Loaded")) {
                 // You can create and populate an Adapter
                 ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                            SearchActivity.this,
                            android.R.layout.simple_list_item_1, searchResults);
                 setListAdapter(adapter);
            }
        }
    }
	
	@Override
    public void onDestroy() {
		super.onDestroy();
	}

}
