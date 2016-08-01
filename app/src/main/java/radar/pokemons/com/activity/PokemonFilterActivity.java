package radar.pokemons.com.activity;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import radar.pokemons.com.R;
import radar.pokemons.com.adapter.PokemonListAdapter;
import radar.pokemons.com.model.Pokemon;

public class PokemonFilterActivity extends AppCompatActivity {

  public static final String KEY_SHAREDPREF_POK = "KEY_SHAREDPREF_POK";
  RecyclerView mRecyclerView;
  private PokemonListAdapter mAdapter;
  private Toolbar toolbar;
  Button unselectAllBtn;
  Button selectAllButton;
  List<Pokemon> pokemons = new ArrayList<>();
  TextView selectedPokemonCount;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_filter);

    selectedPokemonCount = (TextView) findViewById(R.id.count_selected_pokemon);

    unselectAllBtn = (Button) findViewById(R.id.uncheck_all_btn);
    unselectAllBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        for (Pokemon pokemon : pokemons) {
          pokemon.setIsSelected(false);
          mAdapter.notifyDataSetChanged();
        }
      }
    });
    selectAllButton = (Button) findViewById(R.id.check_all);
    selectAllButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        for (Pokemon pokemon : pokemons) {
          pokemon.setIsSelected(true);
          mAdapter.notifyDataSetChanged();
        }
      }
    });

    toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setTitle(getString(R.string.activity_filter_title));
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    Set<String> selectedPokemons =
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getStringSet(KEY_SHAREDPREF_POK, new HashSet<String>());
    selectedPokemonCount.setText(""+selectedPokemons.size());
    int id = 1;
    for (String name : getResources().getStringArray(R.array.pokemons)) {
      Pokemon pokemon = new Pokemon(id, name, selectedPokemons.contains(String.valueOf(id)));
      pokemons.add(pokemon);
      id++;
    }

    mRecyclerView = (RecyclerView) findViewById(R.id.pokemons_list);

    mRecyclerView.setHasFixedSize(true);

    // use a linear layout manager
    mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

    // create an Object for Adapter
    mAdapter = new PokemonListAdapter(pokemons, new PokemonListAdapter.PokemonAdapterListener() {
      @Override
      public void onPokemonSelected(Pokemon pokemon) {
        selectedPokemonCount.setText(""+mAdapter.getSelectedPokemons().size());
      }
    });

    // set the adapter object to the Recyclerview
    mRecyclerView.setAdapter(mAdapter);

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.save_menu, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch(item.getItemId()) {
      case android.R.id.home:
        finish();
        break;
      case R.id.action_save:
        save();
        finish();
        break;
    }
   return true;
  }

  private void save() {
    Set<String> selectedPokemon = new HashSet<>();
    for (Pokemon pokemon : mAdapter.getSelectedPokemons()) {
      if (pokemon.isSelected()) {
        selectedPokemon.add(String.valueOf(pokemon.getId()));
      }
    }
    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putStringSet(KEY_SHAREDPREF_POK, selectedPokemon).commit();
  }
}
