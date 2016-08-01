package radar.pokemons.com.model;

import POGOProtos.Networking.Envelopes.RequestEnvelopeOuterClass;
import POGOProtos.Networking.Envelopes.RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo;

public class Account {
  private String username;
  private String password;
  private String token = "";

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Account account = (Account) o;

    return username != null ? username.equals(account.username) : account.username == null;

  }

  @Override
  public int hashCode() {
    return username != null ? username.hashCode() : 0;
  }


  public AuthInfo buildAuth() {
    AuthInfo.Builder auth = AuthInfo.newBuilder();
    auth.setProvider("ptc");
    auth.setToken(AuthInfo.JWT.newBuilder().setContents(getToken()).setUnknown2(59).build());
    return auth.build();
  }
}
