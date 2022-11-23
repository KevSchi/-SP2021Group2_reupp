package campingplatz.auth;

import java.time.LocalDateTime;

import org.jboss.aerogear.security.otp.Totp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import campingplatz.user.User;
import campingplatz.user.UserRepository;
@Controller
public class AuthController {
	@Autowired
	private UserRepository users;

  private final PasswordEncoder passwordEncoder;
  private final String userNotFoundEncodedPassword;

  public AuthController(PasswordEncoder passwordEncoder) {
    this.passwordEncoder = passwordEncoder;
    this.userNotFoundEncodedPassword = this.passwordEncoder
        .encode("userNotFoundPassword");
  }

  
  
  @PostMapping("/login-secret")
  public String loginsecret(@RequestParam("name")String name, @RequestParam("code")String code) {
   User user = users.findByUsername(name);
  
   if (user != null) {
      if (user.ispwCheckedOK()) {
            String secret = user.getSecret();
            Totp totp = new Totp(secret);

            if (totp.verify(code)) {
              user.setPwChecked(null);
              UserDetail detail = new UserDetail(user);
              users.save(user);
              UserAuthentication userAuthentication = new UserAuthentication(detail);
              SecurityContextHolder.getContext().setAuthentication(userAuthentication);
              return "redirect:/";
            }
      }
   }

    return "redirect:/login";
  }


    @PostMapping("/signin")
  public String login(@RequestParam String username,
      @RequestParam String password, Model model) {
        User user = null;
    try {
      user = users.findByUsername(username);

    } catch (Exception e) {
      System.out.println("AuthController.login() Nutzer existiert nicht");
    }
    if (user != null) {
      if(user.toManyTries()){
      
          boolean pwMatches = passwordEncoder.matches(password,
              user.getUserAccount().getPassword().toString());
          if (pwMatches) {

            UserDetail detail = new UserDetail(user);
            UserAuthentication userAuthentication = new UserAuthentication(detail);
            if (user.isUsing2FA()) {
              user.setPwChecked(LocalDateTime.now());
              users.save(user);
              model.addAttribute("name", userAuthentication.getName());
              return "loginsecret";
          }
          SecurityContextHolder.getContext().setAuthentication(userAuthentication);
            return "redirect:/";
        }else{
          user.incTriedLogins();
          users.save(user);
        }
      }else{return "/locked";}
    }else {
      this.passwordEncoder.matches(password, this.userNotFoundEncodedPassword);
    }

    return "redirect:/login";
  }

}
