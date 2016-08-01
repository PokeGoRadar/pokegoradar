package radar.pokemons.com.model;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.BitmapDescriptor;

public class Pokemon {

  private String name;
  private boolean isSelected;
  private int id;
  private Bitmap avatar;
  private BitmapDescriptor bitmapDescriptor;
  public Pokemon() {
  }

  public Pokemon(int id, String name, boolean isSelected) {
    this.name = name;
    this.isSelected = isSelected;
    this.id = id;
  }

  public BitmapDescriptor getBitmapDescriptor() {
    return bitmapDescriptor;
  }

  public void setBitmapDescriptor(BitmapDescriptor bitmapDescriptor) {
    this.bitmapDescriptor = bitmapDescriptor;
  }

  public Bitmap getAvatar() {
    return avatar;
  }

  public void setAvatar(Bitmap avatar) {
    this.avatar = avatar;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isSelected() {
    return isSelected;
  }

  public void setIsSelected(boolean isSelected) {
    this.isSelected = isSelected;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }
}
