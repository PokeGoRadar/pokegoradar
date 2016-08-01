package radar.pokemons.com.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import POGOProtos.Networking.Envelopes.RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo;

public class AuthBundle implements Serializable {
  List<AuthInfo> authInfoList = new ArrayList<>();

  public List<AuthInfo> getAuthInfoList() {
    return authInfoList;
  }

  public void setAuthInfoList(List<AuthInfo> authInfoList) {
    this.authInfoList = authInfoList;
  }
}
