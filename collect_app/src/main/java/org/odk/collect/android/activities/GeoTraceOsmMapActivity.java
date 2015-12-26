package org.odk.collect.android.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.spatial.MBTileProvider;
import org.odk.collect.android.spatial.MapHelper;
import org.odk.collect.android.widgets.GeoTraceWidget;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Marker.OnMarkerClickListener;
import org.osmdroid.bonuspack.overlays.Marker.OnMarkerDragListener;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;




public class GeoTraceOsmMapActivity extends Activity implements IRegisterReceiver {
	private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	private ScheduledFuture schedulerHandler;
	public int zoom_level = 3;
	public Boolean gpsStatus = true;
	private Boolean play_check = false;
	private MapView mapView;
	private SharedPreferences sharedPreferences;
	public DefaultResourceProxyImpl resource_proxy;
	private ITileSource baseTiles;
	public MyLocationNewOverlay mMyLocationOverlay;
	private ImageButton play_button;
	private ImageButton save_button;
	public ImageButton polygon_button;
	public ImageButton layers_button;
	public ImageButton clear_button;
	private Button manual_button;
	private ImageButton pause_button;
	private ProgressDialog progress;
	public AlertDialog.Builder builder;
	public AlertDialog.Builder p_builder;

	public LayoutInflater inflater;
	private AlertDialog alert;
	private AlertDialog p_alert;
	private View traceSettingsView;
	private View polygonPolylineView;
	private PathOverlay pathOverlay;
	private ArrayList<Marker> map_markers = new ArrayList<Marker>();
	private String final_return_string;
	private Integer TRACE_MODE; // 0 manual, 1 is automatic
	private Boolean inital_location_found = false;
	private	EditText time_number;
	private Spinner time_units;
	private Spinner time_delay;
	private Button polygon_save;
	private Button polyline_save;
	private Boolean beenPaused;
	public Boolean layerStatus = false;
	private String[] OffilineOverlays;
	private int selected_layer= -1;
	private TilesOverlay mbTileOverlay;
	private MBTileProvider mbprovider;
	private AlertDialog mAlertDialog;
	private MapHelper mHelper;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.geotrace_osm_layout);
		setTitle(getString(R.string.geotrace_title)); // Setting title of the action
		mHelper = new MapHelper(this);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		resource_proxy = new DefaultResourceProxyImpl(getApplicationContext());
		mapView = (MapView)findViewById(R.id.geotrace_mapview);
		mapView.setMultiTouchControls(true);
		mapView.setBuiltInZoomControls(true);
		mapView.getController().setZoom(zoom_level);
		mMyLocationOverlay = new MyLocationNewOverlay(this, mapView);
		mMyLocationOverlay.runOnFirstFix(centerAroundFix);
		inflater = this.getLayoutInflater();
		traceSettingsView = inflater.inflate(R.layout.geotrace_dialog, null);
		polygonPolylineView = inflater.inflate(R.layout.polygon_polyline_dialog, null);
		time_delay = (Spinner) traceSettingsView.findViewById(R.id.trace_delay);
		time_delay.setSelection(3);
		time_units = (Spinner) traceSettingsView.findViewById(R.id.trace_scale);
		layers_button = (ImageButton) findViewById(R.id.geoTrace_layers_button);
		layers_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showLayersDialog();

			}
		});

		progress = new ProgressDialog(this);
		progress.setTitle(getString(R.string.getting_location));
		progress.setMessage(getString(R.string.please_wait_long));
		progress.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				play_button.setImageResource(R.drawable.ic_menu_mylocation);
			}
		});


		clear_button= (ImageButton) findViewById(R.id.geotrace_clear_button);
		clear_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				clearAndReturnEmpty();
			}

		});

		polygon_button = (ImageButton) findViewById(R.id.geotrace_polygon_button);
		polygon_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
//				if (map_markers.size()>2){
//					openPolygonDialog();
//				}else{
//					showPolyonErrorDialog();
//				}

			}
		});
		save_button= (ImageButton) findViewById(R.id.geotrace_save);
		save_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				p_alert.show();

			}
		});
		manual_button = (Button)findViewById(R.id.manual_button);
		manual_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				addLocationMarker();

			}
		});
		pause_button =(ImageButton)findViewById(R.id.geotrace_pause_button);
		play_button = (ImageButton)findViewById(R.id.geotrace_play_button);
		beenPaused =false;
		TRACE_MODE = 1;
		play_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				//setGPSStatus();
				if (!play_check){
					if (!inital_location_found){
						mMyLocationOverlay.runOnFirstFix(centerAroundFix);
						progress.show();

					}else{
						if(!beenPaused){
							alert.show();
						}else{
							RadioGroup rb = (RadioGroup) traceSettingsView.findViewById(R.id.radio_group);
							int radioButtonID = rb.getCheckedRadioButtonId();
							View radioButton = rb.findViewById(radioButtonID);
							int idx = rb.indexOfChild(radioButton);
							TRACE_MODE = idx;
							if (TRACE_MODE ==0){
								setupManualMode();
							}else if (TRACE_MODE ==1){
								setupAutomaticMode();
							}else{
								reset_trace_settings();
							}
						}
						play_check=true;
					}
				}else{
					play_check=false;
					startGeoTrace();
				}
			}
		});
		pause_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				play_button.setVisibility(View.VISIBLE);
				save_button.setVisibility(View.VISIBLE);
				pause_button.setVisibility(View.GONE);
				manual_button.setVisibility(View.GONE);
				play_check = true;
				try {
					schedulerHandler.cancel(true);
				} catch (Exception e) {
					// Do nothing
				}


			}
		});

		overlayMapLayerListner();
		buildDialogs();
		Intent intent = getIntent();
		if (intent != null && intent.getExtras() != null) {
			if ( intent.hasExtra(GeoTraceWidget.TRACE_LOCATION) ) {
				String s = intent.getStringExtra(GeoTraceWidget.TRACE_LOCATION);
				play_button.setVisibility(View.GONE);
				clear_button.setVisibility(View.VISIBLE);
				overlayIntentTrace(s);
				zoomToPoints();
			}
		}else{
			setGPSStatus();
			progress.show();

		}

		polygon_save = (Button) polygonPolylineView.findViewById(R.id.polygon_save);
		polygon_save.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (map_markers.size()>2){
					createPolygon();
					p_alert.dismiss();
					saveGeoTrace();
				}else{
					p_alert.dismiss();
					showPolyonErrorDialog();
				}


			}
		});
		polyline_save = (Button) polygonPolylineView.findViewById(R.id.polyline_save);
		polyline_save.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				p_alert.dismiss();
				saveGeoTrace();

			}
		});


		mapView.invalidate();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mHelper.setOsmBasemap(mapView);
		setGPSStatus();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mMyLocationOverlay.enableMyLocation();

	}

	@Override
	protected void onStop() {
		super.onStop();
		disableMyLocation();
	}
	@Override
	public void finish() {
		ViewGroup view = (ViewGroup) getWindow().getDecorView();
		view.removeAllViews();
		super.finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		saveGeoTrace();
	}

	public void setGeoTraceScheuler(long delay, TimeUnit units){
		schedulerHandler = scheduler.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						addLocationMarker();
					}
				});
			}
		},delay, delay, units);

	}



	public void overlayIntentTrace(String str){
		String s = str.replace("; ",";");
		String[] sa = s.split(";");
		for (int i=0;i<(sa.length);i++){
			String[] sp = sa[i].split(" ");
			double gp[] = new double[4];
			String lat = sp[0].replace(" ", "");
			String lng = sp[1].replace(" ", "");
			String altStr = sp[2].replace(" ", "");
			String acu = sp[3].replace(" ", "");
			gp[0] = Double.parseDouble(lat);
			gp[1] = Double.parseDouble(lng);
			Double alt = Double.parseDouble(altStr);
			Marker marker = new Marker(mapView);
			marker.setSubDescription(acu);
			GeoPoint point = new GeoPoint(gp[0], gp[1]);
			point.setAltitude(alt.intValue());
			marker.setPosition(point);
			marker.setOnMarkerClickListener(nullmarkerlistner);
			marker.setDraggable(true);
			marker.setOnMarkerDragListener(draglistner);
			marker.setIcon(getResources().getDrawable(R.drawable.map_marker));
			marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
			map_markers.add(marker);
			pathOverlay.addPoint(marker.getPosition());
			mapView.getOverlays().add(marker);

		}
		mapView.invalidate();

	}
	private void zoomToPoints(){
		mapView.getController().setZoom(15);
		mapView.invalidate();
		Handler handler=new Handler();
		Runnable r = new Runnable(){
			public void run() {
				GeoPoint c_marker = map_markers.get(0).getPosition();
				mapView.getController().setCenter(c_marker);
			}
		};
		handler.post(r);
		mapView.invalidate();

	}

	private void setGPSStatus(){
		upMyLocationOverlayLayers();
		gpsStatus = true;
	}

	private void disableMyLocation(){
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		if (locationManager.isProviderEnabled(locationManager.GPS_PROVIDER)){
			mMyLocationOverlay.setEnabled(false);
			mMyLocationOverlay.disableFollowLocation();
			mMyLocationOverlay.disableMyLocation();
			gpsStatus =false;
		}
	}
	private void upMyLocationOverlayLayers(){
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		if (locationManager.isProviderEnabled(locationManager.GPS_PROVIDER)){
			overlayMyLocationLayers();
		}else{
			showGPSDisabledAlertToUser();
		}

	}
	private void overlayMapLayerListner(){
		pathOverlay= new PathOverlay(Color.RED, this);
		Paint pPaint = pathOverlay.getPaint();
		pPaint.setStrokeWidth(5);
		mapView.getOverlays().add(pathOverlay);
		mapView.invalidate();
	}

	private void overlayMyLocationLayers(){
		mapView.getOverlays().add(mMyLocationOverlay);
		mMyLocationOverlay.setEnabled(true);
		mMyLocationOverlay.enableMyLocation();
		mMyLocationOverlay.enableFollowLocation();


	}
	private Handler mHandler = new Handler(Looper.getMainLooper());

	private Runnable centerAroundFix = new Runnable() {
		public void run() {
			mHandler.post(new Runnable() {
				public void run() {
					zoomToMyLocation();
					progress.dismiss();
					play_button.setImageResource(R.drawable.play_button);
				}
			});
		}
	};


	private void zoomToMyLocation(){
		if (mMyLocationOverlay.getMyLocation()!= null){
			inital_location_found = true;
			if (zoom_level ==3){
				mapView.getController().setZoom(15);
			}else{
				mapView.getController().setZoom(zoom_level);
			}
			mapView.getController().setCenter(mMyLocationOverlay.getMyLocation());
		}else{
			mapView.getController().setZoom(zoom_level);
		}

	}

	private void showGPSDisabledAlertToUser(){
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setMessage(getString(R.string.enable_gps_message))
				.setCancelable(false)
				.setPositiveButton(getString(R.string.enable_gps),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
							}
						});
		alertDialogBuilder.setNegativeButton(getString(R.string.cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = alertDialogBuilder.create();
		alert.show();
	}
	//This happens on click of the play button
	public void setGeoTraceMode(View view){
		boolean checked = ((RadioButton) view).isChecked();
		switch(view.getId()) {
			case R.id.trace_manual:
				if (checked){
					TRACE_MODE = 0;
					time_units.setVisibility(View.GONE);
					time_delay.setVisibility(View.GONE);
					time_delay.invalidate();
					time_units.invalidate();
				}
				break;
			case R.id.trace_automatic:
				if (checked){
					TRACE_MODE = 1;
					time_units.setVisibility(View.VISIBLE);
					time_delay.setVisibility(View.VISIBLE);
					time_delay.invalidate();
					time_units.invalidate();
				}
				break;
		}
	}


	private void buildDialogs(){

		builder = new AlertDialog.Builder(this);

		builder.setTitle(getString(R.string.geotrace_instruction));
		builder.setMessage(getString(R.string.geotrace_instruction_message));
		builder.setView(null);
		builder.setView(traceSettingsView)
				// Add action buttons
				.setPositiveButton(getString(R.string.start), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						startGeoTrace();
						dialog.cancel();
						alert.dismiss();
					}
				})
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
						alert.dismiss();
						reset_trace_settings();
					}
				})
				.setOnCancelListener(new OnCancelListener() {

					@Override
					public void onCancel(DialogInterface dialog) {
						reset_trace_settings();
					}
				});


		alert = builder.create();



		p_builder = new AlertDialog.Builder(this);
		p_builder.setTitle("Select Polygon/Polyline");
//		p_builder.setMessage(getString(R.string.polygon_conection_message));
		p_builder.setView(polygonPolylineView)
				// Add action buttons
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();

					}
				})
				.setOnCancelListener(new OnCancelListener() {

					@Override
					public void onCancel(DialogInterface dialog) {
						dialog.cancel();
						alert.dismiss();
					}
				});

		p_alert = p_builder.create();



	}

	private void createPolygon(){
		map_markers.add(map_markers.get(0));
		pathOverlay.addPoint(map_markers.get(0).getPosition());
		mapView.invalidate();
	}


	private void reset_trace_settings(){
		play_button.setImageResource(R.drawable.play_button);
		play_check=false;
	}

	private void startGeoTrace(){
		RadioGroup rb = (RadioGroup) traceSettingsView.findViewById(R.id.radio_group);
		int radioButtonID = rb.getCheckedRadioButtonId();
		View radioButton = rb.findViewById(radioButtonID);
		int idx = rb.indexOfChild(radioButton);
		beenPaused = true;
		TRACE_MODE = idx;
		if (TRACE_MODE ==0){
			setupManualMode();
		}else if (TRACE_MODE ==1){
			setupAutomaticMode();
		}else{
			reset_trace_settings();
		}
		play_button.setVisibility(View.GONE);
		save_button.setVisibility(View.GONE);
		pause_button.setVisibility(View.VISIBLE);


	}
//	private void stop_play(){
//		manual_button.setVisibility(View.GONE);
//		play_button.setVisibility(View.GONE);
//		save_button.setVisibility(View.VISIBLE);
//		polygon_button.setVisibility(View.VISIBLE);
//
//	}

	private void setupManualMode(){
		manual_button.setVisibility(View.VISIBLE);

	}
	private void setupAutomaticMode(){
		manual_button.setVisibility(View.VISIBLE);
		String delay = time_delay.getSelectedItem().toString();
		String units = time_units.getSelectedItem().toString();
		Long time_delay;
		TimeUnit time_units_value;
		if (units == getString(R.string.minutes)){
			time_delay = Long.parseLong(delay) * (60); //Convert minutes to seconds
			time_units_value = TimeUnit.SECONDS;
		}else{
			//in Seconds
			time_delay = Long.parseLong(delay);
			time_units_value = TimeUnit.SECONDS;
		}

		setGeoTraceScheuler(time_delay, time_units_value);
	}

	private void addLocationMarker(){
		Marker marker = new Marker(mapView);
		marker.setPosition(mMyLocationOverlay.getMyLocation());
		Float last_know_acuracy = mMyLocationOverlay.getMyLocationProvider().getLastKnownLocation().getAccuracy();
		mMyLocationOverlay.getMyLocationProvider().getLastKnownLocation().getAccuracy();
		marker.setIcon(getResources().getDrawable(R.drawable.map_marker));
		marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
		marker.setDraggable(true);
		marker.setOnMarkerDragListener(draglistner);
		marker.setSubDescription(Float.toString(last_know_acuracy));
		map_markers.add(marker);

		marker.setOnMarkerClickListener(nullmarkerlistner);
		mapView.getOverlays().add(marker);
		pathOverlay.addPoint(marker.getPosition());
		mapView.invalidate();
	}

	private void saveGeoTrace(){
		returnLocation();
		finish();
	}
	private void showPolyonErrorDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.polygon_validator))
				.setPositiveButton(getString(R.string.dialog_continue), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// FIRE ZE MISSILES!
					}
				}).show();

	}


	private String generateReturnString() {
		String temp_string = "";
		for (int i = 0 ; i < map_markers.size();i++){
			String lat = Double.toString(map_markers.get(i).getPosition().getLatitude());
			String lng = Double.toString(map_markers.get(i).getPosition().getLongitude());
			String alt = Integer.toString(map_markers.get(i).getPosition().getAltitude());
			String acu = map_markers.get(i).getSubDescription();
			temp_string = temp_string+lat+" "+lng +" "+alt+" "+acu+";";
		}
		return temp_string;
	}

	private void returnLocation(){
		final_return_string = generateReturnString();
		Intent i = new Intent();
		i.putExtra(
				FormEntryActivity.GEOTRACE_RESULTS,
				final_return_string);
		setResult(RESULT_OK, i);
		finish();
	}
	private OnMarkerClickListener nullmarkerlistner= new Marker.OnMarkerClickListener() {

		@Override
		public boolean onMarkerClick(Marker arg0, MapView arg1) {
			return false;
		}
	};
	private void clearAndReturnEmpty(){
		final_return_string = "";
		Intent i = new Intent();
		i.putExtra(
				FormEntryActivity.GEOTRACE_RESULTS,
				final_return_string);
		setResult(RESULT_OK, i);
		setResult(RESULT_OK, i);
		finish();

	}
	private void update_polygon(){
		pathOverlay.clearPath();
		for (int i =0;i<map_markers.size();i++){
			pathOverlay.addPoint(map_markers.get(i).getPosition());
		}
		mapView.invalidate();
	}



	private OnMarkerDragListener draglistner = new Marker.OnMarkerDragListener(){
		@Override
		public void onMarkerDragStart(Marker marker) {

		}
		@Override
		public void onMarkerDragEnd(Marker arg0) {
			update_polygon();

		}
		@Override
		public void onMarkerDrag(Marker marker) {
			update_polygon();

		}

	} ;
	private String getMBTileFromItem(int item) {
		String foldername = OffilineOverlays[item];
		File dir = new File(Collect.OFFLINE_LAYERS+File.separator+foldername);
		String mbtilePath;
		File[] files = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".mbtiles");
			}
		});
		mbtilePath =Collect.OFFLINE_LAYERS+File.separator+foldername+File.separator+files[0].getName();

		return mbtilePath;
	}
	private String[] getOfflineLayerList() {
		File files = new File(Collect.OFFLINE_LAYERS);
		ArrayList<String> results = new ArrayList<String>();
		results.add("None");
		for(String folder : files.list()){
			results.add(folder);
		}
		String[] finala = new String[results.size()];
		finala = results.toArray(finala);
		return finala;
	}


	private void showLayersDialog() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		alertDialog.setTitle(getString(R.string.select_offline_layer));
		OffilineOverlays = getOfflineLayerList(); // Maybe this should only be done once. Have not decided yet.
		alertDialog.setSingleChoiceItems(OffilineOverlays, selected_layer, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				switch (item) {
					case 0:
						mapView.getOverlays().remove(mbTileOverlay);
						layerStatus = false;
						//updateMapOverLayOrder();
						break;
					default:
						layerStatus = true;
						mapView.getOverlays().remove(mbTileOverlay);
						//String mbTileLocation = getMBTileFromItem(item);
						String mbFilePath = getMBTileFromItem(item);
						//File mbFile = new File(Collect.OFFLINE_LAYERS+"/GlobalLights/control-room.mbtiles");
						File mbFile = new File(mbFilePath);
						mbprovider = new MBTileProvider(GeoTraceOsmMapActivity.this, mbFile);
						mbTileOverlay = new TilesOverlay(mbprovider, GeoTraceOsmMapActivity.this);
						mbTileOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
						//updateMapOverLayOrder();
						mapView.getOverlays().add(mbTileOverlay);
						updateMapOverLayOrder();
						mapView.invalidate();
				}
				//This resets the map and sets the selected Layer
				selected_layer = item;
				dialog.dismiss();
				final Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						mapView.invalidate();
					}
				}, 400);

			}
		});

		alertDialog.show();
	}




	private void createErrorDialog(String errorMsg, final boolean shouldExit) {
		Collect.getInstance().getActivityLogger().logAction(this, "createErrorDialog", "show");
		mAlertDialog = new AlertDialog.Builder(this).create();
		mAlertDialog.setIcon(android.R.drawable.ic_dialog_info);
		mAlertDialog.setMessage(errorMsg);
		DialogInterface.OnClickListener errorListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int i) {
				switch (i) {
					case DialogInterface.BUTTON_POSITIVE:
						Collect.getInstance().getActivityLogger().logAction(this, "createErrorDialog",
								shouldExit ? "exitApplication" : "OK");
						if (shouldExit) {
							finish();
						}
						break;
				}
			}
		};
		mAlertDialog.setCancelable(false);
		mAlertDialog.setButton(getString(R.string.ok), errorListener);
		mAlertDialog.show();
	}
	private void updateMapOverLayOrder(){
		List<Overlay> overlays = mapView.getOverlays();
		if (layerStatus){
			mapView.getOverlays().remove(mbTileOverlay);
			mapView.getOverlays().remove(pathOverlay);
			mapView.getOverlays().remove(mMyLocationOverlay);
			mapView.getOverlays().add(mbTileOverlay);
			mapView.getOverlays().add(pathOverlay);
			mapView.getOverlays().add(mMyLocationOverlay);

		}
		for (Overlay overlay : overlays){
			//Class x = overlay.getClass();
			final Overlay o = overlay;
			if (overlay.getClass() == Marker.class){
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					public void run() {
						mapView.getOverlays().remove(o);
						mapView.invalidate();
					}
				}, 100);
				handler.postDelayed(new Runnable() {
					public void run() {
						mapView.getOverlays().add(o);
						mapView.invalidate();
					}
				}, 100);


			}
		}
		mapView.invalidate();

	}
}