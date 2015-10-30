package co.edu.eafit.mrblock.Controladores;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


import java.util.ArrayList;

import co.edu.eafit.mrblock.Entidades.SimpleGeofence;
import co.edu.eafit.mrblock.Entidades.Ubicacion;
import co.edu.eafit.mrblock.Helper.UbicationHelper;
import co.edu.eafit.mrblock.R;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mapa; // Might be null if Google Play services APK is not available.
    public GeofencingEvent EVENT ;
    private ArrayList<Ubicacion> array;
    private UbicationHelper ubicationHelper;
    public static LatLng storeubic;
    private Geofence geobuild;
    private ArrayList<Geofence> mGeofenceList;
    private PendingIntent mGeofencePendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        mapa.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                storeubic = latLng;
                Intent i = new Intent(getApplicationContext(), LockBlockActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call  once when {@link #mapa} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mapa == null) {
            // Try to obtain the map from the SupportMapFragment.
            mapa = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
        }
        if (mapa != null) {
            // El objeto GoogleMap ha sido referenciado correctamente
            //ahora podemos manipular sus propiedades
            //Seteamos el tipo de mapa
            mapa.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            //Activamos la capa o layer MyLocation
            mapa.setMyLocationEnabled(true);
            ubicationHelper = new UbicationHelper(getApplicationContext());
            array = ubicationHelper.getAllUbication();
            if(!array.isEmpty()){
                for(int i = 0 ; i< array.size();i++){
                    Ubicacion ubicacion = new Ubicacion();
                    ubicacion = array.get(i);
                    Toast.makeText(getApplicationContext(),"Latitud = "+ubicacion.getLatitud()+"  Longitud = "+ubicacion.getLongitud()+"  Creando GeoFence",Toast.LENGTH_LONG).show();
                    double rad = ubicacion.getRadio();
                    float radio = (float)rad;
                    SimpleGeofence geofence =new SimpleGeofence(ubicacion.getName(),ubicacion.getLatitud(),ubicacion.getLongitud(),radio);
                    addMarkerForFence(geofence);
                    /**
                     * Falta tomar las ubicaciones y crear los fence ademas de usar el metodo crearFence para hacer las zonas en el mapa
                     * Recordatorio poner un TOAST para ver el tipo de transicion 2 = EXIT 1 = ENTER
                     */
                }
            }
        }
    }

    public void addMarkerForFence(SimpleGeofence fence){
        if(fence == null){
            // display en error message and return
            return;
        }
        mapa.addMarker(new MarkerOptions()
                .position(new LatLng(fence.getLatitude(), fence.getLongitude()))
                .title("Fence " + fence.getId())
                .snippet("Radius: " + fence.getRadius())).showInfoWindow();

        //Instantiates a new CircleOptions object +  center/radius
        CircleOptions circleOptions = new CircleOptions()
                .center( new LatLng(fence.getLatitude(), fence.getLongitude()) )
                .radius( fence.getRadius() )
                .fillColor(0x40ff0000)
                .strokeColor(Color.TRANSPARENT)
                .strokeWidth(2);

        // Get back the mutable Circle
        Circle circle = mapa.addCircle(circleOptions);
        // more operations on the circle...

    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }
}