package radar.pokemons.com.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import radar.pokemons.com.R;
import radar.pokemons.com.model.Pokemon;

public class PokemonListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  List<Pokemon> mItems;
  private PokemonAdapterListener pokemonAdapterListener;

  public interface PokemonAdapterListener {
    void onPokemonSelected(Pokemon pokemon);
  }

  public PokemonListAdapter(List<Pokemon> pokemons, PokemonAdapterListener pokemonAdapterListener) {
    this.mItems = pokemons;
    this.pokemonAdapterListener = pokemonAdapterListener;
  }

  class PokemonItemHolder extends RecyclerView.ViewHolder {

    public ImageView avatar;
    public TextView name;
    public CheckBox checkBox;

    public PokemonItemHolder(View itemView) {
      super(itemView);
      name = (TextView) itemView.findViewById(R.id.textView);
      checkBox = (CheckBox) itemView.findViewById(R.id.checkBox);
      avatar = (ImageView) itemView.findViewById(R.id.imageView);
    }
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.pokemon_filter_item, parent, false);
    return new PokemonItemHolder(v);
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    final Pokemon pokemon = mItems.get(position);
    Context context = holder.itemView.getContext();
    PokemonItemHolder itemHolder = (PokemonItemHolder) holder;
    itemHolder.name.setText(pokemon.getName());
    itemHolder.checkBox.setChecked(pokemon.isSelected());
    itemHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        pokemon.setIsSelected(isChecked);
        if (pokemonAdapterListener != null) {
          pokemonAdapterListener.onPokemonSelected(pokemon);
        }
      }
    });

    Drawable drawable = ContextCompat.getDrawable(context, context.getResources()
        .getIdentifier("pokemon_" + pokemon.getId(), "drawable", context.getPackageName()));
    itemHolder.avatar.setImageDrawable(drawable);
  }

  @Override
  public int getItemCount() {
    return mItems.size();
  }

  public List<Pokemon> getSelectedPokemons() {
    List<Pokemon> selectedPokemons = new ArrayList<>();
    for (Pokemon pokemon : mItems) {
      if (pokemon.isSelected()) {
        selectedPokemons.add(pokemon);
      }
    }
    return selectedPokemons;
  }
}
