package com.example.android.foodtrucktracker;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference databaseRef;
    private DatabaseReference trucksRef;
    private DatabaseReference locationsRef;
    private ValueEventListener trucksLis;
    private ValueEventListener locLis;
    LocationManager locationManager;
    String locationProvider;
    //Marker mCurrLocationMarker;
    ArrayList<Marker> markers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        databaseRef = database.getReference();
        trucksRef = database.getReference("Trucks");
        locationsRef = database.getReference("locations");

        markers = new ArrayList();

        initializeLocationManager();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("called", "Activity --> onResume");
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 200);
            return;
        }
        if(this.locationProvider == null){
            return;
        }
        this.locationManager.requestLocationUpdates(this.locationProvider, 400, 1, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("called", "Activity --> onPause");
        this.locationManager.removeUpdates(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(trucksLis != null){
            trucksRef.removeEventListener(trucksLis);
        }
        if(locLis != null){
            locationsRef.removeEventListener(locLis);
        }

    }

    private void initializeLocationManager() {
        //get the location manager
        this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //define the location manager criteria
        Criteria criteria = new Criteria();
        criteria.setAccuracy( Criteria.ACCURACY_COARSE );
        this.locationProvider = locationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 200);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (mAuth.getCurrentUser() == null) {
            return false;
        }
        getMenuInflater().inflate(R.menu.menu_main, menu);//Menu Resource, Menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item1:
                try {
                    databaseRef.child("Trucks").child(mAuth.getCurrentUser().getUid()).child("online").setValue(false);
                    mAuth.signOut();
                    finish();
                } catch (Exception eِِ) {
                    Toast.makeText(getApplicationContext(), "Sign Out failed", Toast.LENGTH_LONG).show();
                }
                Toast.makeText(getApplicationContext(), "Signed Out", Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 200: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // {Some Code}
                }
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng Saudi = new LatLng(24.6437208, 47.2976813);
        //mMap.addMarker(new MarkerOptions().position(Saudi).title("Marker in Saudi Arabia"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(Saudi));
        Log.i("called", "aftermap");


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 200);
            return;
        }
        mMap.setMyLocationEnabled(true);

        Location location = locationManager.getLastKnownLocation(locationProvider);
        if(location != null) {
            Log.i("called", "loc not null");

            CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(),location.getLongitude()));
            this.mMap.moveCamera(center);
            CameraUpdate zoom=CameraUpdateFactory.zoomTo(14);
            this.mMap.animateCamera(zoom);
        }

        trucksLis = trucksRef.orderByChild("online").equalTo(true).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                removeMarkers();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String id = snapshot.getKey();
                    if (mAuth.getCurrentUser() != null && id.equals(mAuth.getCurrentUser().getUid())){
                        continue;
                    }
                    BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.foodtruckicon);
                    String name = snapshot.child("TruckName").getValue().toString();
                    String title = snapshot.child("TruckDescription").getValue().toString();
                    final Marker m = mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Truck name: " + name +" - Description: "+ title).icon(icon));
                    markers.add(m);
                    locLis = locationsRef.child(id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(!dataSnapshot.exists()){
                                markers.remove(m);
                                m.remove();
                                return;
                            }
                            String lat = dataSnapshot.child("latitude").getValue().toString();
                            String lon = dataSnapshot.child("longitude").getValue().toString();
                            LatLng pos = new LatLng(Double.parseDouble(lat), Double.parseDouble(lon));
                            m.setPosition(pos);
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Failed to read value
                        }
                    });
                }
                //Log.d("read", "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                //Log.w("failed", "Failed to read value.", error.toException());
            }
        });


        /*
        Location location = locationManager.getLastKnownLocation(locationProvider);


        //initialize the location
        if(location != null) {
            Log.i("called", "loc not null");

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(location.getLatitude(),location.getLongitude()));
            markerOptions.title("Me");
            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.my_location_icon);
            markerOptions.icon(icon);
            mCurrLocationMarker = mMap.addMarker(markerOptions);

            CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(),location.getLongitude()));
            this.mMap.moveCamera(center);
            CameraUpdate zoom=CameraUpdateFactory.zoomTo(6);
            this.mMap.animateCamera(zoom);
            onLocationChanged(location);
        }else{
            Log.i("called", "loc null");

        }*/
    }


    @Override
    public void onLocationChanged(Location location) {
        Log.i("called", "onLocationChanged");
        /*
        if(mCurrLocationMarker != null){
            mCurrLocationMarker.setPosition(new LatLng(location.getLatitude(),location.getLongitude()));
        }*/
        if (mAuth.getCurrentUser() != null){
            Log.i("called", String.valueOf(location.getLatitude()));
            DatabaseReference ref = locationsRef.child(mAuth.getCurrentUser().getUid());
            ref.child("latitude").setValue(location.getLatitude());
            ref.child("longitude").setValue(location.getLongitude());
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void removeMarkers(){
        if(locLis != null){
            locationsRef.removeEventListener(locLis);
        }
        for (int i=0; i < markers.size(); i++){
            markers.get(i).remove();
        }
        markers.clear();
    }


}
