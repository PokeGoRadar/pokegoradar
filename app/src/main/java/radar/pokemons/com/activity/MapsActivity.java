package radar.pokemons.com.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.pokegoapi.api.map.Pokemon.CatchablePokemon;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import POGOProtos.Networking.Envelopes.RequestEnvelopeOuterClass;
import okhttp3.OkHttpClient;
import radar.pokemons.com.FetchPokemons;
import radar.pokemons.com.R;
import radar.pokemons.com.Settings;
import radar.pokemons.com.Utils;
import radar.pokemons.com.model.Account;

import static com.google.android.gms.location.LocationServices.FusedLocationApi;
import static com.google.android.gms.location.LocationServices.SettingsApi;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, LocationListener {

  private static final int REQUEST_CHECK_SETTINGS = 100;

  public static final String ARG_TOKEN = "ARG_TOKEN";
  public static final int REQUEST_CODE_FILTER = 1;
  public static final int REQUEST_CODE_EXTRA_ACCOUNTS = 2;
  private GoogleMap mMap;
  public static final String ua = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36";
  Location currentLocation;
  OkHttpClient okHttpClient;
  private RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo authInfo;
  private ExecutorService executorService;
  private CompletionService completionService;
  private Button refreshMapBtn;
  private Button clearMapBtn;

  private Map<String, String> pokemonOnMap = new ConcurrentHashMap<>();
  private List<LatLng> scanPoints = new ArrayList<>();
  private ProgressBar progressBar;
  private AdView mAdView;
  private GoogleApiClient mGoogleApiClient;
  private LocationRequest mLocationRequest;
  boolean movedToUserLocation;
  private ProgressDialog progressDialog;
  private Polygon currentPolygon;
  private Handler handler;
  private Toolbar toolbar;
  private Set<String> pokemonToFilter = new HashSet<>();
  private Set<Account> currentActiveAccounts = new HashSet<>();
  public static final int MESSAGE_DISPLAY_POKE = 0;


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.main_menu, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.filter:
        Intent i = new Intent(getApplicationContext(), PokemonFilterActivity.class);
        startActivityForResult(i, REQUEST_CODE_FILTER);
        break;
      case R.id.extra_accounts:
        i = new Intent(getApplicationContext(), AccountsSettingsActivity.class);
        startActivityForResult(i, REQUEST_CODE_EXTRA_ACCOUNTS);
        break;
    }
    return super.onOptionsItemSelected(item);
  }


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Utils.generatePokemonMap(getApplicationContext());

    currentActiveAccounts = Settings.getAccounts(getApplicationContext());

    clearMapBtn = (Button) findViewById(R.id.clear_map);
    refreshMapBtn = (Button) findViewById(R.id.refresh_map);
    toolbar = (Toolbar) findViewById(R.id.toolbar);
    progressBar = (ProgressBar) findViewById(R.id.progress);

    clearMapBtn.setEnabled(false);
    refreshMapBtn.setEnabled(false);

    movedToUserLocation = false;

    toolbar.setTitle("PokemonGO Radar");
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(false);

    okHttpClient = new OkHttpClient();
    handler = new Handler(Looper.getMainLooper()) {
      @Override
      public void handleMessage(Message inputMessage) {
        switch (inputMessage.what) {
          case MESSAGE_DISPLAY_POKE:
            drawPokemonOnMap((List<CatchablePokemon>) inputMessage.obj);
            progressBar.setProgress(progressBar.getProgress() + 1);
            break;
        }
      }
    };

    mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
        .addApi(LocationServices.API)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .build();

    mLocationRequest = LocationRequest.create()
        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        .setInterval(10 * 1000)        // 10 seconds, in milliseconds
        .setFastestInterval(1 * 1000); // 1 second, in milliseconds


    clearMapBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (mMap != null) {
          mMap.clear();
        }
        pokemonOnMap.clear();
      }
    });
    authInfo = (RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo) getIntent().getExtras().getSerializable(ARG_TOKEN);
    executorService = Executors.newFixedThreadPool(4);
    completionService = new ExecutorCompletionService<>(executorService);

    refreshMapBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        try {
          scanPoints = Utils.generateScanPoints(mMap.getCameraPosition().target.latitude, mMap.getCameraPosition().target.longitude);
          createBox();
          refreshCurrentActiveAccounts();
          new FetchPokemons(getApplicationContext(), completionService, handler, okHttpClient, scanPoints, currentActiveAccounts, fetchPokemonsListener).execute(mMap.getCameraPosition().target);
        } catch (Exception e) {
          e.printStackTrace();
          Toast.makeText(getApplicationContext(), getString(R.string.server_down), Toast.LENGTH_LONG).show();
        }
      }
    });
    MobileAds.initialize(getApplicationContext(), "ca-app-pub-9879577400303327~3054725490");
    mAdView = (AdView) findViewById(R.id.adView);
    AdRequest adRequest = new AdRequest.Builder()
        //.addTestDevice("17A4EF47D4DCBE08E3640CA30A42326F")
        .build();
    mAdView.loadAd(adRequest);

  }

  private void refreshCurrentActiveAccounts() {
    Set<Account> updateToDateAccounts = Settings.getAccounts(getApplicationContext());
    Iterator<Account> it = currentActiveAccounts.iterator();
    while (it.hasNext()) {
      Account account = it.next();
      if (!updateToDateAccounts.contains(account)) {
        // Means one of the active account has been deleted
        it.remove();
      }
    }
    currentActiveAccounts.addAll(updateToDateAccounts);
  }

  private FetchPokemons.FetchPokemonsListener fetchPokemonsListener = new FetchPokemons.FetchPokemonsListener() {
    @Override
    public void showProgressBar(boolean show) {
      MapsActivity.this.showProgressbar(show);
    }

    @Override
    public void setProgress(int progress) {
      progressBar.setProgress(progress);
    }

    @Override
    public void setMaxProgress(int max) {
      progressBar.setMax(max);
    }

    @Override
    public void showMessage(String message) {
      Toast.makeText(MapsActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void scanComplete() {

    }
  };

  public void drawPokemonOnMap(List<CatchablePokemon> catchablePokemons) {
    if (catchablePokemons != null) {
      for (CatchablePokemon catchablePokemon : catchablePokemons) {
        drawPokemonOnMap(String.valueOf(catchablePokemon.getEncounterId()), String.valueOf(catchablePokemon.getPokemonId().getNumber()),
            catchablePokemon.getPokemonId().getValueDescriptor().getName(), catchablePokemon.getLatitude(), catchablePokemon.getLongitude(),
            catchablePokemon.getExpirationTimestampMs());
      }
    }
  }

  private void drawPokemonOnMap(String encounterId, String pokemonId, String name, double latitude, double longitude, long expirationTime) {
    if (!pokemonOnMap.containsKey(encounterId) &&
        pokemonToFilter.contains(String.valueOf(pokemonId))) {
      try {
        LatLng pok = new LatLng(latitude, longitude);
        DateTime dateTime = new DateTime(expirationTime);
        MarkerOptions markerOptions = new MarkerOptions();
        BitmapDescriptor bitmapDescriptor = Utils.pokemonMap.get(pokemonId).getBitmapDescriptor();
        if (dateTime.isAfter(new Instant())) {
          Interval interval = new Interval(new Instant(), dateTime);
          markerOptions = markerOptions
              .icon(bitmapDescriptor)
              .position(pok)
              .title(name)
              .snippet("Expires in: " + String.valueOf(interval.toDurationMillis() / 1000) + "s");
        } else {
          markerOptions = markerOptions
              .icon(bitmapDescriptor)
              .position(pok)
              .title(name);
        }

        mMap.addMarker(markerOptions);
        pokemonOnMap.put(encounterId, "");

      } catch (Exception e) {
        e.printStackTrace();
        Toast.makeText(getApplicationContext(), getString(R.string.server_down), Toast.LENGTH_LONG).show();
      }
    }
  }

  public void showProgressbar(final boolean status) {
    runOnUiThread(new Runnable() {
      public void run() {
        if (status) {
          progressBar.setVisibility(View.VISIBLE);
          refreshMapBtn.setVisibility(View.GONE);
        } else {
          progressBar.setVisibility(View.GONE);
          refreshMapBtn.setVisibility(View.VISIBLE);
        }
      }
    });
  }

  @Override
  protected void onStart() {
    super.onStart();
    mGoogleApiClient.connect();
  }

  @Override
  protected void onStop() {
    super.onStop();
    if (mGoogleApiClient.isConnected()) {
      FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
      mGoogleApiClient.disconnect();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    //movedToUserLocation = false;
    pokemonToFilter = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getStringSet(PokemonFilterActivity.KEY_SHAREDPREF_POK, new HashSet<String>());
    if (pokemonToFilter.isEmpty()) {
      for (int id = 1; id <= 151; id++) {
        pokemonToFilter.add(String.valueOf(id));
      }
    }
    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putStringSet(PokemonFilterActivity.KEY_SHAREDPREF_POK, pokemonToFilter).commit();
  }

  @Override
  protected void onPause() {
    super.onPause();
    showProgressDialog(null, false);
  }


  @Override
  protected void onDestroy() {
    executorService.shutdownNow();
    showProgressDialog(null, false);
    super.onDestroy();
  }

  static Intent getIntent(Context context, RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo authInfo) {
    Intent i = new Intent(context, MapsActivity.class);
    i.putExtra(ARG_TOKEN, authInfo);
    return i;
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
    refreshMapBtn.setEnabled(true);
    clearMapBtn.setEnabled(true);
    settingsrequest();
  }

  public void centerCamera() {
    showProgressDialog(null, false);
    if (!movedToUserLocation && currentLocation != null && Utils.checkPermission(this)) {

      LatLng target = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
      //CameraPosition position = this.mMap.getCameraPosition();
      CameraPosition.Builder builder = new CameraPosition.Builder();
      builder.zoom(16);
      builder.target(target);
      this.mMap.animateCamera(CameraUpdateFactory.newCameraPosition(builder.build()));
      movedToUserLocation = true;
    }
  }


  public void settingsrequest() {
    LocationRequest locationRequest = LocationRequest.create();
    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    locationRequest.setInterval(30 * 1000);
    locationRequest.setFastestInterval(5 * 1000);
    LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
        .addLocationRequest(locationRequest);
    builder.setAlwaysShow(true); //this is the key ingredient

    PendingResult<LocationSettingsResult> result =
        SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
    result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
      @Override
      public void onResult(LocationSettingsResult result) {
        final Status status = result.getStatus();
        final LocationSettingsStates state = result.getLocationSettingsStates();
        switch (status.getStatusCode()) {
          case LocationSettingsStatusCodes.SUCCESS:
            // All location settings are satisfied. The client can initialize location
            // requests here.
            getLocationAndCenterCamera();
            break;
          case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
            // Location settings are not satisfied. But could be fixed by showing the user
            // a dialog.
            try {
              // Show the dialog by calling startResolutionForResult(),
              // and check the result in onActivityResult().
              status.startResolutionForResult(MapsActivity.this, REQUEST_CHECK_SETTINGS);
            } catch (IntentSender.SendIntentException e) {
              // Ignore the error.
              Toast.makeText(getApplicationContext(), getString(R.string.gps_not_enabled), Toast.LENGTH_LONG).show();
            }
            break;
          case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
            // Location settings are not satisfied. However, we have no way to fix the
            // settings so we won't show the dialog.
            Toast.makeText(getApplicationContext(), getString(R.string.gps_not_enabled), Toast.LENGTH_LONG).show();
            break;
        }
      }
    });
  }

  private void showProgressDialog(String text, boolean show) {
    if (show) {
      progressDialog = ProgressDialog.show(MapsActivity.this,
          text,
          "Loading...");
    } else {
      if (progressDialog != null) {
        progressDialog.dismiss();
      }
    }
  }

  private void getLocationAndCenterCamera() {
    if (Utils.checkPermission(this)) {
      showProgressDialog(getString(R.string.wait_for_location), true);
      // It can happen that mMap is YET ready, we simply dont do anything
      if (mMap != null) {
        mMap.setMyLocationEnabled(true);
        Location location = FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
          FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } else {
          currentLocation = location;
          centerCamera();
        }
      }
    } else {
      Toast.makeText(getApplicationContext(), getString(R.string.gps_not_enabled), Toast.LENGTH_LONG).show();
      showProgressDialog(null, false);
    }
  }


  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
    // Check for the integer request code originally supplied to startResolutionForResult().
      case REQUEST_CHECK_SETTINGS:
        switch (resultCode) {
          case Activity.RESULT_OK:
            getLocationAndCenterCamera();
            break;
          case Activity.RESULT_CANCELED:
            settingsrequest();//keep asking if imp or do whatever
            break;
        }
        break;

      case REQUEST_CODE_FILTER:
        movedToUserLocation = true;
        break;
      case REQUEST_CODE_EXTRA_ACCOUNTS:
        break;
    }
  }

  @Override
  public void onConnected(Bundle bundle) {
    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
        .findFragmentById(R.id.map);
    mapFragment.getMapAsync(this);
  }

  @Override
  public void onConnectionSuspended(int i) {
    showProgressDialog(null, false);
  }

  @Override
  public void onLocationChanged(Location location) {
    currentLocation = location;
    centerCamera();
  }

  @Override
  public void onConnectionFailed(ConnectionResult connectionResult) {
    showProgressDialog(null, false);
  }

  private void createBox() {
    if (currentPolygon != null) {
      currentPolygon.remove();
    }
    if (scanPoints.size() >= 25) {
      currentPolygon = mMap.addPolygon(new PolygonOptions()
          .add(scanPoints.get(0))
          .add(scanPoints.get(4))
          .add(scanPoints.get(24))
          .add(scanPoints.get(20)).strokeWidth(3));
    }
  }
}
