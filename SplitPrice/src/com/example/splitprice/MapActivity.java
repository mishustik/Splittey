package com.example.splitprice;

import java.util.ArrayList;
import java.util.HashMap;

import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Marker.OnMarkerClickListener;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.MapQuestRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MapActivity extends Activity{
	
	//Map elements
	private MapView         mMapView;
    private MapController   mMapController;
  	LocationManager locationManager;
  	
  	ArrayList<HashMap<String,GeoPoint>> routes = new ArrayList<HashMap<String,GeoPoint>>();
  	ArrayList<String> prices = new ArrayList<String>();
  		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);
		
		// get action bar   
        ActionBar actionBar = getActionBar();
 
        // Enabling Up / Back navigation
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        actionBar.hide();
        
        
        mMapView = (MapView) findViewById(R.id.mapview);
        mMapView.setTileSource(TileSourceFactory.MAPNIK);
        mMapView.setBuiltInZoomControls(true);
        mMapController = (MapController) mMapView.getController();
        mMapController.setZoom(13);
        GeoPoint gPt = new GeoPoint(51500000, -150000);
        //Centre map near to Hyde Park Corner, London
        mMapController.setCenter(gPt);
      	      	
        routes = (ArrayList<HashMap<String,GeoPoint>>) getIntent().getSerializableExtra("routes");
        prices = (ArrayList<String>) getIntent().getSerializableExtra("prices");
        
        ListView priceLv = (ListView) findViewById(R.id.lvPrice);
        priceLv.setAdapter(new CustomAdapter(this,prices));
        //ArrayAdapter<String> mHistory = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, prices);
        //priceLv.setAdapter(mHistory);
        
        new RouteFinder(mMapView, routes).execute();
        
        
        
	}
	
	//Finds the shortest route between two geopoints and demonstrates it on the map 
		public class RouteFinder extends AsyncTask<Void, Void, Void>{
				
			private final int[] colors = {Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW,  Color.MAGENTA};
			
			private MapView map;
			ArrayList<HashMap<String,GeoPoint>> routes;
			int minLat, maxLat, minLng, maxLng;
			ArrayList<Polyline> roadOverlays;
			ArrayList<ArrayList<Marker>> nodesAllRoutes; 
			//private GeoPoint startPoint, endPoint;
			//ProgressDialog progress;
			
			public class OnMarkerClickListenerCustom implements OnMarkerClickListener{

				int routeId;
				
				public OnMarkerClickListenerCustom(int routeId){
					this.routeId = routeId;
				}
				
				@Override
				public boolean onMarkerClick(Marker arg0, MapView arg1) {
					
					roadOverlays.get(routeId).setColor(colors[routeId]);
					//remove old bitmaps
					map.getOverlays().remove(roadOverlays.get(routeId));
					map.getOverlays().remove(nodesAllRoutes.get(routeId).get(0));
					map.getOverlays().remove(nodesAllRoutes.get(routeId).get(nodesAllRoutes.get(routeId).size() - 1));
					
					//add new bitmaps on top of everything
					map.getOverlays().add(roadOverlays.get(routeId));
					map.getOverlays().add(nodesAllRoutes.get(routeId).get(0));
					map.getOverlays().add(nodesAllRoutes.get(routeId).get(nodesAllRoutes.get(routeId).size() - 1));
					
					((MapView) map).invalidate();
					return false;
				}
						
			}
			
			
			public RouteFinder(MapView map, ArrayList<HashMap<String,GeoPoint>> routes){
			
				this.map = map;
				this.routes = routes;
				minLat = 0;
				maxLat = 0;
				minLng = 0;
				maxLng = 0;
				nodesAllRoutes = new ArrayList<ArrayList<Marker>>();
				roadOverlays = new ArrayList<Polyline>();
				
			}
			
			@Override
			protected void onPreExecute() {
				
				//pd.show();
				
			}
				
			protected Void doInBackground(Void...params){
											
				try{
					//setting road manager, online routing service. For offline -> GraphHopper
					RoadManager roadManager = new MapQuestRoadManager("Fmjtd%7Cluur2q6t2g%2Ca5%3Do5-9a201y");
					roadManager.addRequestOption("routeType=fastest");
					
					//cleaning previously drawn routes
					map.getOverlays().remove(roadOverlays);
					map.getOverlays().removeAll(nodesAllRoutes);
					nodesAllRoutes.clear();
					    	    
					for(int i = 0; i < routes.size(); i++){
						
						//setting up start and end points
						ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
						
						GeoPoint startPoint = routes.get(i).get("startPoint");
						GeoPoint endPoint = routes.get(i).get("endPoint");
						
						waypoints.add(startPoint);
						waypoints.add(endPoint);
						
						//finding minimum and maximum latitudes and longitudes
						if(minLat == 0 || startPoint.getLatitudeE6() < minLat || endPoint.getLatitudeE6() < minLat){
							minLat = Math.min(startPoint.getLatitudeE6(),endPoint.getLatitudeE6());
						}
						if(maxLat == 0 || startPoint.getLatitudeE6() > maxLat || endPoint.getLatitudeE6() > maxLat){
							maxLat = Math.max(startPoint.getLatitudeE6(),endPoint.getLatitudeE6());
						}
						if(minLng == 0 || startPoint.getLongitudeE6() < minLng || endPoint.getLongitudeE6() < minLng){
							minLng = Math.min(startPoint.getLongitudeE6(),endPoint.getLongitudeE6());
						}
						if(maxLng == 0 || startPoint.getLongitudeE6() > maxLng || endPoint.getLongitudeE6() > maxLng){
							maxLng = Math.max(startPoint.getLongitudeE6(),endPoint.getLongitudeE6());
						}
						
						//retrieving the road between points
						Road road = roadManager.getRoad(waypoints);
														    
						//build a polyl`ine with the route shape
						roadOverlays.add(RoadManager.buildRoadOverlay(road, MapActivity.this));
						
						//draw marker for routes
						Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
					            R.drawable.marker).copy(Bitmap.Config.ARGB_8888, true);
						Canvas canvas = new Canvas(bitmap);
						Paint paint = new Paint();
						paint.setColor(colors[i]);
						paint.setStrokeWidth(20);
						paint.setStyle(Style.STROKE.FILL);
						paint.setAntiAlias(true);
						canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 6, 27,
						            paint);
						
						//draw text to the Canvas center
						Rect bounds = new Rect();
						paint.setColor(Color.WHITE);
						paint.setTextSize((int) (14 * 3));
						paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);
						String markerText = String.valueOf(i+1);
						paint.getTextBounds(markerText, 0, markerText.length(), bounds);
						int x = bitmap.getWidth() / 2 - bounds.width() / 2;
						int y = bitmap.getHeight() / 6 + bounds.height()/2;
						canvas.drawText(markerText, x, y, paint);
						
						Drawable nodeIcon = getResources().getDrawable(R.drawable.marker);
						
						//find route nodes
						ArrayList<Marker> routeNodes = new ArrayList<Marker>();
						for (int j = 0; j < road.mNodes.size(); j++){
							if(j == 0 || j == road.mNodes.size() - 1){
								RoadNode node = road.mNodes.get(j);
								Marker nodeMarker = new Marker(map);
								nodeMarker.setPosition(node.mLocation);
								nodeMarker.setIcon(new BitmapDrawable(getResources(), bitmap));
								
								nodeMarker.setTitle("Step "+j);
								nodeMarker.setSnippet(node.mInstructions);
								nodeMarker.setSubDescription(Road.getLengthDurationText(node.mLength, node.mDuration));
								Drawable icon = getResources().getDrawable(R.drawable.ic_launcher);
								nodeMarker.setImage(icon);
								routeNodes.add(nodeMarker);
							}	
						}
						
						nodesAllRoutes.add(routeNodes);
					}
					
				} catch(Exception e){
					e.printStackTrace();
					Thread.interrupted();
				}
				return null;
			}
			
			protected void onPostExecute(Void param) {
	            super.onPostExecute(param);
	            //to add special markers for first and last points of routes
	            ArrayList<Marker> firstLastNodes = new ArrayList<Marker>();
	            
	            //draw route polyline on the map
	            int numbOfRoutesToDraw = Math.min(roadOverlays.size(), 5);
	            for(int i = 0; i < numbOfRoutesToDraw; ++i){
	            	            	
	            	roadOverlays.get(i).setColor(colors[i]);
					map.getOverlays().add(roadOverlays.get(i));
					
					//draw route nodes
					for(ArrayList<Marker> nodesList : nodesAllRoutes){
						for(Marker node : nodesList){
							if(node.equals(nodesList.get(0)) || node.equals(nodesList.get(nodesList.size() - 1))){
								int index = nodesAllRoutes.indexOf(nodesList);
								node.setOnMarkerClickListener(new OnMarkerClickListenerCustom(index));
								firstLastNodes.add(node);
							} else {
								//map.getOverlays().add(node);
							}
						}
					}
					
					//add first and last nodes' markers
					for(Marker node : firstLastNodes){
						map.getOverlays().add(node);
					}
					
				}	
				            
	            //refresh the map
	            ((MapView) map).invalidate();
	            
	         	//zoom to route
				mMapController.zoomToSpan(Math.abs(maxLat - minLat),	Math.abs(maxLng - minLng));
	            mMapController.animateTo(new GeoPoint((maxLat + minLat)/2,(maxLng + minLng)/2));
				
	            //dismiss progress dialog
	            //h.post(r);
	            //tripdatetxt.setVisibility(1);
	                  
	       }
			
		}

}
