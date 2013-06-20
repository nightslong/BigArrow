package uk.ac.brighton.ci360.bigarrow.remove;

import uk.ac.brighton.ci360.bigarrow.PlaceSearch;
import uk.ac.brighton.ci360.bigarrow.PlaceSearchRequester;
import uk.ac.brighton.ci360.bigarrow.R;
import uk.ac.brighton.ci360.bigarrow.places.Place;
import uk.ac.brighton.ci360.bigarrow.places.PlaceDetails;
import uk.ac.brighton.ci360.bigarrow.places.PlacesList;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

public class BigArrowActivity extends Activity implements SurfaceHolder.Callback, 
															LocationListener, PlaceSearchRequester {

	private static final String TAG = "BigArrow";
	private Camera camera;
	private SurfaceView cameraSV;
	private SurfaceHolder cameraSH;
	private OverlayView overlay; 
	private PlaceSearch pSearch;
	private final boolean PLACES_SEARCH_ON = true; 

	/* Activity event handlers */
	// Called when activity is initialised by OS
	@Override
	public void onCreate(Bundle inst) {
		super.onCreate(inst);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_bigarrow);
		initCamera();
		if (PLACES_SEARCH_ON) pSearch = new PlaceSearch(this);
		
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 50, this);
	}

	// Called when activity is closed by OS
	@Override
	public void onDestroy() {
		super.onDestroy();
		// Turn off the camera
		stopCamera();
	}

	/* SurfaceHolder event handlers */
	// Called when the surface is first created
	public void surfaceCreated(SurfaceHolder holder) {
	}

	// Called when surface dimensions etc change
	public void surfaceChanged(SurfaceHolder sh, int format, int width,
			int height) {
		// Start camera preview
		startCamera(sh, width, height);
	}

	// Called when the surface is closed/destroyed
	public void surfaceDestroyed(SurfaceHolder sh) {
		stopCamera();
	}

	private void initCamera() {
		//cameraSV = (SurfaceView) findViewById(R.id.surface_camera);
		cameraSH = cameraSV.getHolder();
		cameraSH.addCallback(this);

		try {
			camera = Camera.open();
		} catch (RuntimeException e) {
			Log.e(TAG, "Failed to connect to camera");
		}

		//overlay = (OverlayView) findViewById(R.id.surface_overlay);
		//overlay.setZOrderMediaOverlay(true);
		overlay.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		overlay.setCamera(camera);
	}

	// Setup camera based on surface parameters

	private void startCamera(SurfaceHolder sh, int width, int height) {
		Camera.Parameters p = camera.getParameters();
		for (Camera.Size s : p.getSupportedPreviewSizes()) { 

			p.setPreviewSize(s.width, s.height);
			overlay.setPreviewSize(s);
			break;
		}
		if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
			// p.set("orientation", "portrait");
			// p.setRotation(90);
			camera.setDisplayOrientation(90);
		} else {
			// p.set("orientation", "landscape");
			// p.setRotation(0);
			camera.setDisplayOrientation(0);
		}
		camera.setParameters(p);

		try {
			camera.setPreviewDisplay(sh);
		} catch (Exception e) { // Log surface setting exceptions

		}
		camera.startPreview();
	}

	// Stop camera when application ends
	private void stopCamera() {
		if (cameraSH != null) {
			cameraSH.removeCallback(this);
			cameraSH = null;
		}
		if (camera != null) {
			camera.stopPreview();
			camera.release();
			camera = null;
		}
	}
	
	private void getNearestPub(Location l) {
		Log.d(TAG, String.format("lat:%s long:%s", l.getLatitude(), l.getLongitude()));
		pSearch.search(l, new SearchEstab[]{SearchEstab.BAR}, SearchType.SINGLE); 
	}
	
	public void updateNearestPub(Place p, Location l, float d) {
		Log.d(TAG, "Nearest pub:"+p.name);
		Log.d(TAG, "Distance:"+d);
		overlay.setNearestPub(p);
		overlay.setDistance(d);
		overlay.setNpLocation(l);
	}

	@Override
	public void onLocationChanged(Location arg0) {
		if (PLACES_SEARCH_ON) getNearestPub(arg0);
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onPause() {
		camera.setPreviewCallback(null); 
		overlay.getHolder().removeCallback(this);
		camera.release();
	}

	@Override
	public void updateNearestPlace(Place place, Location location,
			float distance) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateNearestPlaces(PlacesList places) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updatePlaceDetails(PlaceDetails details) {
		// TODO Auto-generated method stub
		
	}
	
	/*@Override
	public void onResume() {
		initCamera();
	}*/
	
}
