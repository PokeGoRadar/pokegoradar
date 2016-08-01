package radar.pokemons.com;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import radar.pokemons.com.model.Account;
import radar.pokemons.com.model.Pokemon;

public class Utils {

  public static Map<String, Pokemon> pokemonMap = new HashMap<>();

  static byte[] encodeParameters(Map<String, String> params, String paramsEncoding) {
    StringBuilder encodedParams = new StringBuilder();
    try {
      for (Map.Entry<String, String> entry : params.entrySet()) {
        encodedParams.append(URLEncoder.encode(entry.getKey(), paramsEncoding));
        encodedParams.append('=');
        encodedParams.append(URLEncoder.encode(entry.getValue(), paramsEncoding));
        encodedParams.append('&');
      }
      return encodedParams.toString().getBytes(paramsEncoding);
    } catch (UnsupportedEncodingException uee) {
      throw new RuntimeException("Encoding not supported: " + paramsEncoding, uee);
    }
  }

  public static boolean checkPermission(Context context) {
    return ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
  }

  public static List<LatLng> generateScanPoints(double currentLat, double currentLon) {
    double dist = 00.002000;
    List<LatLng> scanPoints = new ArrayList<>();
    double lat = currentLat + (dist * -2);
    double lon = currentLon + (dist * -2);
    //this is the GPS offset we're going to use

    for (int i = 0; i < 5; i++) {
      for (int j = 0; j < 5; j++) {
        double newLat = (lat + (dist * i));
        double newLon = (lon + (dist * j));
        LatLng temp = new LatLng(newLat, newLon);
        scanPoints.add(temp);
      }
    }
    return scanPoints;
  }

  public static Bitmap getBitmapFromName(Context context, int id) {
    try {
      int resourceID = context.getResources().getIdentifier("pokemon_" + id, "drawable", context.getPackageName());
      return BitmapFactory.decodeResource(context.getResources(), resourceID);
    } catch (Exception e) {
      return null;
    }
  }

  public static void generatePokemonMap(Context context) {
    if (pokemonMap.isEmpty()) {
      int id = 1;
      for (String name : context.getResources().getStringArray(R.array.pokemons)) {
        Pokemon pokemon = new Pokemon(id, name, false);
        Bitmap icon = Utils.getBitmapFromName(context, Integer.valueOf(id));
        pokemon.setAvatar(icon);
        pokemon.setBitmapDescriptor(BitmapDescriptorFactory.fromBitmap(icon));
        pokemonMap.put(String.valueOf(id), pokemon);
        id++;
      }
    }
  }
}
