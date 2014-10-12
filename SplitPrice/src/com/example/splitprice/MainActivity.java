package com.example.splitprice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.osmdroid.util.GeoPoint;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Spinner;

public class MainActivity extends Activity {
	
	private static final double RATE = 0.001;
	
	//search request codes
	public static final int REQUEST_LOCATION = 1;
	SearchView searchView;
	private List<String> listOfAddresses = new ArrayList<String>();
	private List<GeoPoint> listOfGeoPoints = new ArrayList<GeoPoint>();
	private ArrayList<HashMap<String,GeoPoint>> routes = new ArrayList<HashMap<String,GeoPoint>>();
	private ArrayList<Double> realDistances = new ArrayList<Double>();
	private ArrayList<String> prices = new ArrayList<String>();
	
	Spinner spinner;
	ListView lView;
	Button calcBtn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		// Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        
        searchView.setOnQueryTextListener(new OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
            	
            	Intent searchIntent = new Intent(MainActivity.this,SearchActivity.class);
    			
    			searchIntent.putExtra(SearchManager.QUERY, query);
    			startActivityForResult(searchIntent, REQUEST_LOCATION);
    			
    			return false;
            }

			@Override
			public boolean onQueryTextChange(String newText) {
				// DO NOTHING
				return false;
			}

        });
        
        lView = (ListView) findViewById(R.id.lvExp);
        
    	//adapter
        List<String> copyList = new ArrayList<String>();
        copyList = listOfAddresses;
        
        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,copyList);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item,copyList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.add("<Select starting point>");
        
        spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setAdapter(adapter);
        spinner.setSelection(adapter.getCount() - 1);
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(position == 0){
                	calcBtn.setTextColor(getResources().getColor(R.color.rb_text_disabled));
                	calcBtn.setShadowLayer(0, 0, 0, getResources().getColor(R.color.rb_text_glow_disabled));
                	calcBtn.setEnabled(false);
                } else {
                	if(listOfAddresses.size() >= 2 && spinner.getSelectedItemId() > 0){
	    				calcBtn.setTextColor(getResources().getColor(R.color.rb_text));
	    				calcBtn.setShadowLayer(8, 0, 0, getResources().getColor(R.color.rb_text_glow));
	    				calcBtn.setEnabled(true);
	    			}
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
        
        calcBtn = (Button) findViewById(R.id.calculate_btn);
        calcBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				
				long startingPointId = spinner.getSelectedItemId();
								
				calculateDistances(startingPointId);
				//new CalculateRoute(listOfAddresses,listOfGeoPoints,startingPointId).execute();
				
				Intent i = new Intent(MainActivity.this, MapActivity.class);
				i.putExtra("routes", routes);
				i.putExtra("prices", prices);
		        startActivity(i);
				
			}});
        		
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Take appropriate action for each action item click
        switch (item.getItemId()) {
        case R.id.action_search:
            // search action
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode) {
	    	case (REQUEST_LOCATION): {
	    		if (resultCode == Activity.RESULT_OK) {
	    			GeoPoint tempGP = (GeoPoint) data.getParcelableExtra("location");
	    			String tempDP = data.getStringExtra("name");
	    			listOfAddresses.add(tempDP);
	    			listOfGeoPoints.add(tempGP);
	    			lView.setAdapter(new CustomAdapter(this,listOfAddresses.subList(1, listOfAddresses.size())));
	    			if(listOfAddresses.size() >= 2 && spinner.getSelectedItemId() > 0){
	    				calcBtn.setTextColor(getResources().getColor(R.color.rb_text));
	    				calcBtn.setShadowLayer(8, 0, 0, getResources().getColor(R.color.rb_text_glow));
	    				calcBtn.setEnabled(true);
	    			}	
	    			
	    		}
	    		break;
	    	}
	    }
	}
	
	/*private class CalculateRoute extends AsyncTask<Object,Void,Object>{
		
		private String url_calculate_route = "http://54.162.26.170/SplitPrice/create_route.php";
		List<String> names = new ArrayList<String>();
		List<GeoPoint> locations = new ArrayList<GeoPoint>();
		long startingPoint;
		
		public CalculateRoute(List<String> names, List<GeoPoint> locations, long startingPoint){
			this.names = names;
			this.locations = locations;
			this.startingPoint = startingPoint;
		}
		
		@Override
		protected Object doInBackground(Object... params) {
			
			try {
		    	// Building Parameters
	        	List<NameValuePair> parameters = new ArrayList<NameValuePair>();
	        	JSONParser jsonParser = new JSONParser();
	        	
	        	for (int i = 0; i < names.size(); i++){
	              
	            	parameters.add(new BasicNameValuePair("name" + i, names.get(i)));
	            	parameters.add(new BasicNameValuePair("location" + i, String.valueOf(locations.get(i))));
	            	
	            }
	        	parameters.add(new BasicNameValuePair("stPoint", String.valueOf(startingPoint)));
	        	
	        	JSONObject json = jsonParser.makeHttpRequest(url_calculate_route,"POST",parameters);
	        	
	        } catch(Exception e){
		    	e.printStackTrace();
	        }
			
			return null;
		}
	
	}*/
	
	public void calculateDistances(long startingPointId){
		
		List<Double> distances = new ArrayList<Double>();
		GeoPoint temp = new GeoPoint(0,0);
		Double totalDistance = 0.0;
		Double totalRealDistance = 0.0;
		
		//starting point
		Location locationStart = new Location("point Start");
		locationStart.setLatitude(listOfGeoPoints.get((int) startingPointId - 1).getLatitudeE6() / 1E6);  
		locationStart.setLongitude(listOfGeoPoints.get((int) startingPointId - 1).getLongitudeE6() / 1E6);
				
		//starting point
		Location locationA = new Location("point A");
		locationA.setLatitude(listOfGeoPoints.get((int) startingPointId - 1).getLatitudeE6() / 1E6);  
		locationA.setLongitude(listOfGeoPoints.get((int) startingPointId - 1).getLongitudeE6() / 1E6);
		listOfGeoPoints.remove((int) startingPointId - 1);
		
		while(listOfGeoPoints.size() > 1){
			for(int i = 0; i < listOfGeoPoints.size(); i++){
				Location locationB = new Location("point B");  
				locationB.setLatitude(listOfGeoPoints.get(i).getLatitudeE6() / 1E6);  
				locationB.setLongitude(listOfGeoPoints.get(i).getLongitudeE6() / 1E6);  
	
				distances.add((double) locationA.distanceTo(locationB));
			}
			
			double min = Collections.min(distances);
			totalDistance += min; 
			int index = distances.indexOf(min);
			
			//convert to geopoints
			int lat1 = (int) (locationA.getLatitude() * 1E6);
		    int lng1 = (int) (locationA.getLongitude() * 1E6);
		    GeoPoint point1 = new GeoPoint(lat1, lng1);
		    int lat2 = (int) (listOfGeoPoints.get(index).getLatitude() * 1E6);
		    int lng2 = (int) (listOfGeoPoints.get(index).getLongitude() * 1E6);
		    GeoPoint point2 = new GeoPoint(lat2, lng2);
			
		    HashMap<String,GeoPoint> route = new HashMap<String,GeoPoint>();
			route.put("startPoint", point1);
			route.put("endPoint", point2);
			
			Location locationCurrent = new Location("point Current");  
			locationCurrent.setLatitude(listOfGeoPoints.get(index).getLatitudeE6() / 1E6);  
			locationCurrent.setLongitude(listOfGeoPoints.get(index).getLongitudeE6() / 1E6);
			realDistances.add((double) locationStart.distanceTo(locationCurrent));
			totalRealDistance += locationStart.distanceTo(locationCurrent);
						
			routes.add(route);
			locationA.setLatitude(listOfGeoPoints.get(index).getLatitude());
			locationA.setLongitude(listOfGeoPoints.get(index).getLongitude());
			listOfGeoPoints.remove(index);
			if(listOfGeoPoints.size() == 1){
				temp = point2;
			}
			
		}
		
		HashMap<String,GeoPoint> route = new HashMap<String,GeoPoint>();
		route.put("startPoint", temp);
		route.put("endPoint", listOfGeoPoints.get(0));
		totalDistance += temp.distanceTo(listOfGeoPoints.get(0));
				
		Location locationCurrent = new Location("point Current");  
		locationCurrent.setLatitude(listOfGeoPoints.get(0).getLatitudeE6() / 1E6);  
		locationCurrent.setLongitude(listOfGeoPoints.get(0).getLongitudeE6() / 1E6);
				realDistances.add((double) locationStart.distanceTo(locationCurrent));
		totalRealDistance += locationStart.distanceTo(locationCurrent);
		
		routes.add(route);
		
		//price calculation
		Double totalPrice = totalDistance * RATE;
		for(int i = 0; i < routes.size(); i++){
			prices.add(String.valueOf(i+1) + ": " + String.valueOf(totalPrice * realDistances.get(i) / totalRealDistance).substring(0, 5) + "$");
		}
		
	}
	
}
