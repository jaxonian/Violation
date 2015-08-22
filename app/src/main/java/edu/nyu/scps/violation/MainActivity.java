package edu.nyu.scps.violation;

import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private MapView mapView;
    private Helper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int connectionResult = googleApiAvailability.isGooglePlayServicesAvailable(this);

        if (connectionResult != ConnectionResult.SUCCESS) {
            Dialog dialog = googleApiAvailability.getErrorDialog(this, connectionResult, 0, new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            finish();   //Destroy this activity.
                        }
                    }
            );
            dialog.show();
        }

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this); //eventually calls onMapReady
        helper = new Helper(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Log.d("myTag", "onMapReady");
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);

        //NYU SCPS, 7 East 12th Street, New York, NY  10003.
        //Longitude west of the prime meridian (Greenwich) is negative.
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(40.734457, -73.993886), 18f);
        googleMap.moveCamera(cameraUpdate);

        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(
                "restaurants",
                new String[]{"dba", "violation_description", "latitude", "longitude"},
                null, //selection
                null, //selection arguments
                null, //group by
                null, //having
                null  //order
        );

        final int dbaIndex = cursor.getColumnIndex("dba");
        final int violation_descriptionIndex = cursor.getColumnIndex("violation_description");
        int latitudeIndex = cursor.getColumnIndex("latitude");
        int longitudeIndex = cursor.getColumnIndex("longitude");

        while (cursor.moveToNext()) {
            LatLng latLng = new LatLng(cursor.getDouble(latitudeIndex), cursor.getDouble(longitudeIndex));
            MarkerOptions markerOptions = new MarkerOptions(); //Create an empty MarkerOptions.
            markerOptions.position(latLng);                    //Put some options into it.
            markerOptions.title(cursor.getString(dbaIndex));
            markerOptions.snippet(cursor.getString(violation_descriptionIndex));
            googleMap.addMarker(markerOptions);

            googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                //Put the vertical LinearLayout into the default InfoWindow frame.
                @Override
                public View getInfoWindow(Marker marker) {
                    LayoutInflater layoutInflater = getLayoutInflater();
                    View view = layoutInflater.inflate(R.layout.info_window, null);
                    SQLiteDatabase db = helper.getReadableDatabase();
                    Cursor cursor = db.query(
                            "restaurants",
                            new String[]{"dba", "building", "street", "violation_description", "latitude", "longitude"},
                            "latitude = ? AND longitude = ?",
                            new String[]{
                                    String.valueOf(marker.getPosition().latitude),
                                    String.valueOf(marker.getPosition().longitude),
                            },
                            null, //group by
                            null, //having
                            null  //order
                    );
                    cursor.moveToFirst();
                    TextView textView = (TextView)view.findViewById(R.id.dba);
                    textView.setText(cursor.getString(dbaIndex));

                    textView = (TextView)view.findViewById(R.id.address);
                    textView.setText(
                            cursor.getString(cursor.getColumnIndex("building")) + " " +
                            cursor.getString(cursor.getColumnIndex("street")));

                    textView = (TextView)view.findViewById(R.id.violation_description);
                    textView.setText(cursor.getString(cursor.getColumnIndex("violation_description")));
                    cursor.close();
                    return view;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    return null;
                }
            });
        }

        cursor.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        mapView.onSaveInstanceState(bundle);
        super.onSaveInstanceState(bundle);
    }

    @Override
    public void onLowMemory() {
        mapView.onLowMemory();
        super.onLowMemory();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
