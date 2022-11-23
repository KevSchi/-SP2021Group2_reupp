package campingplatz.auth;

import java.util.Collection;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;

import campingplatz.user.User;


public class UserDetail {

  private final Long userId;

  private final String username;

  private final boolean enabled;

  private final String secret;

  private final Collection<? extends GrantedAuthority> authorities;

  public UserDetail(User user) {
    this.userId = user.getId();
    this.username = user.getUsername();
    this.secret = user.getSecret();
    this.enabled = user.isUsing2FA();
    this.authorities = user.getUserAccount().getRoles() //
					.map(role -> new SimpleGrantedAuthority("ROLE_"+role.toString())) //
					.toList();
  }

  public Long getAppUserId() {
    return this.userId;
  }

  public String getUsername() {
    return this.username;
  }

  public Collection<? extends GrantedAuthority> getAuthorities() {
   return this.authorities;
  }

  public boolean isEnabled() {
    return this.enabled;
  }

  public String getSecret() {
    return this.secret;
  }

  

}