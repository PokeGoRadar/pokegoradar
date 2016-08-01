package radar.pokemons.com;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.map.Pokemon.CatchablePokemon;
import com.pokegoapi.auth.PTCLogin;
import com.pokegoapi.exceptions.LoginFailedException;

import java.util.List;
import java.util.concurrent.Callable;

import POGOProtos.Networking.Envelopes.RequestEnvelopeOuterClass;
import POGOProtos.Networking.Envelopes.RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo;
import radar.pokemons.com.activity.MapsActivity;
import radar.pokemons.com.model.TaskBundle;

public class FetchPokemonTask implements Callable {
  private double lat;
  private double lon;
  private Handler handler;
  private TaskBundle taskBundle;
  private Context context;

  public FetchPokemonTask(Context context, TaskBundle taskBundle, double lat, double longi, Handler handler) {
    this.lon = longi;
    this.lat = lat;
    this.handler = handler;
    this.taskBundle = taskBundle;
    this.context = context;
  }

  @Override
  public Object call() throws Exception {
    try {
      taskBundle.getLock().lock();
      taskBundle.getPokemonGo().setLatitude(lat);
      taskBundle.getPokemonGo().setLongitude(lon);
      taskBundle.getPokemonGo().setAltitude(0);
      boolean success = false;
      int count = 0;
      // Retry 3 times before giving up.
      while (!success && count < 3) {
        try {
          List<CatchablePokemon> pokemons = taskBundle.getPokemonGo().getMap().getCatchablePokemon();
          Message completeMessage = handler.obtainMessage(MapsActivity.MESSAGE_DISPLAY_POKE, pokemons);
          completeMessage.sendToTarget();
          Thread.sleep(6000);
          return "OK";
        } catch (LoginFailedException e) {
          // Maybe token is expired ? attempt new login
          AuthInfo authInfo = new PTCLogin(taskBundle.getOkHttpClient()).login(taskBundle.getAccount().getUsername(), taskBundle.getAccount().getPassword());
          taskBundle.getAccount().setToken(authInfo.getToken().getContents());
          Settings.updateAccount(context, taskBundle.getAccount());
          taskBundle.setPokemonGo(new PokemonGo(authInfo, taskBundle.getOkHttpClient()));
          Thread.sleep(6000);
        }
        count++;
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      taskBundle.getLock().unlock();
    }
    return "FAILED";
  }
}