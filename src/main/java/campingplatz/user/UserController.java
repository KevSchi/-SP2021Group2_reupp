package campingplatz.user;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.net.URLEncoder;
import com.amdelamar.jotp.OTP;
import com.amdelamar.jotp.type.Type;
import org.jboss.aerogear.security.otp.Totp;
import org.salespointframework.useraccount.Password;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.Password.UnencryptedPassword;
import org.salespointframework.useraccount.web.LoggedIn;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.email.EmailBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import java.io.UnsupportedEncodingException;
import campingplatz.captcha.ICaptchaService;
import campingplatz.mail.Mail;


@Controller
class UserController {
	private final PasswordEncoder passwordEncoder;
	private final UserManagement userManagement;

	@Autowired
	private UserRepository users;
	@Autowired
	private ICaptchaService captchaService;

	UserController(UserManagement userManagement, PasswordEncoder passwordEncoder) {

		Assert.notNull(userManagement, "CustomerManagement must not be null!");
		this.passwordEncoder = passwordEncoder;
		this.userManagement = userManagement;
	}

	public User getUser(UserAccount userAccount) {
		return users.findByUserAccount(userAccount);
	}

	// (｡◕‿◕｡)
	// Über @Valid können wir die Eingaben automagisch prüfen lassen, ob es Fehler
	// gab steht im BindingResult,
	// dies muss direkt nach dem @Valid Parameter folgen.
	// Siehe außerdem videoshop.model.validation.RegistrationForm
	// Lektüre:
	// http://docs.spring.io/spring/docs/current/spring-framework-reference/html/validation.html

	@PostMapping("/createEmployee")
	@PreAuthorize("hasRole('BOSS')")
	String createNew(@Valid RegistrationForm form, Errors result) {

		if (result.hasErrors()) {
			return "employee";
		}
		userManagement.createEmployee(form);

		return "redirect:/";
	}
	// @PostMapping("/reserve")
	// public String reserve(@ModelAttribute Cart cart, @LoggedIn
	// Optional<UserAccount> userAccount) {

	// return userAccount.map(account -> {
	// Reservation test = new Reservation(userAccount.get().getId(),
	// LocalDate.of(2021, 04, 14), LocalDate.of(2021, 04, 21));
	// System.out.println(test.getId());
	// reservationCatalog.save(test);

	// cart.clear();
	// return "redirect:/";
	// }).orElse("redirect:/cart");

	// }
	@PreAuthorize("hasRole('BOSS')" + "|| hasRole('EMPLOYEE')")
	@GetMapping("/createReservation")
	String createReservation(Model model, RegistrationForm form) {
		return "createReservation";
	}

	@GetMapping("/changeUserData")
	String changeUserData(Model model, @LoggedIn UserAccount userAccount, RegistrationForm form) {
		User user = users.findByUserAccount(userAccount);
		model.addAttribute("user", user);
		return "changeUserData";
	}

	@PostMapping("/userdata/change/")
	String changeUserData(Model model, @LoggedIn UserAccount userAccount, @RequestParam("firstname") String firstname,
			@RequestParam("lastname") String lastname,
			@DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam("birthday") LocalDate birthday,
			@RequestParam("street") String street, @RequestParam("city") String city, @RequestParam("plz") String plz,
			@RequestParam(value = "using2FA", defaultValue = "false") boolean using2FA,
			@RequestParam(value = "password", defaultValue = "") String password,
			@RequestParam(value = "password_w", defaultValue = "") String password_w,
			@RequestParam(value = "oldPassword", defaultValue = "") String oldPassword)
			throws UnsupportedEncodingException {
		// String mailregex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
		if (firstname == "" || lastname == "" || street == "" || city == "" || plz == "") {
			return "redirect:/profile";
		}
		if (((!password.equals("") || !password_w.equals("") || !oldPassword.equals(""))
				&& (password_w.equals("") || password.equals("") || oldPassword.equals("")))) {
			return "redirect:/profile";
		}
		User user = users.findByUserAccount(userAccount);
		if (!password.equals("")) {
			if (password.equals(password_w)
					&& passwordEncoder.matches(oldPassword, user.getUserAccount().getPassword().toString())) {
				userManagement.setPassword(userAccount, Password.UnencryptedPassword.of(password));
			} else {
				model.addAttribute("passwordError", "Passwort ist nicht korrekt!");
				return "redirect:/changeUserData";
			}
		}
		if (birthday.plusYears(18).isBefore(LocalDate.now())) {
			user.setBirthDate(birthday);
		} else {
			model.addAttribute("ageError", "Sie müssen mindestens 18 Jahre alt sein");
			return "redirect:/profile";
		}
		user.setFirstname(firstname);
		user.setLastname(lastname);
		user.setStreet(street);
		user.setCity(city);
		user.setPlz(plz);
		users.save(user);

		if (user.isUsing2FA() != using2FA) {

			if (!using2FA)
				user.setIsUsig2FA(using2FA);
				users.save(user);

			if (using2FA)
				return "redirect:/qrcode/";
		}
		return "redirect:/profile";
	}

	@GetMapping("/qrcode")
	String qrcode(@LoggedIn UserAccount userAccount, Model model) throws UnsupportedEncodingException {
		User user = users.findByUserAccount(userAccount);
		if (user == null) {
			// this check prevents people browsing straight to this page
			return "redirect:/profile";
		}
		if (user.getSecret() == null) {
			user.setSecret();
			users.save(user);
		}
		String otpUrl = OTP.getURL(user.getSecret(), 6, Type.TOTP, "below-the-sun",
				user.getUserAccount().getUsername());

		String twoFaQrUrl = String.format("https://chart.googleapis.com/chart?cht=qr&chs=200x200&chl=%s",
				URLEncoder.encode(otpUrl, "UTF-8")); // "UTF-8" shouldn't be unsupported but it's a checked exception :(

		model.addAttribute("secret", user.getSecret());
		model.addAttribute("name", userAccount.getUsername());
		model.addAttribute("twoFaQrUrl", twoFaQrUrl);

		return "qrcode";
	}

	@PostMapping("/confirm-secret")
	public String confirmSecret(@RequestParam("username") String username,
			@RequestParam("code") @NotEmpty String code) {

		User user = users.findByUsername(username);
		if (user != null) {
			String secret = user.getSecret();
			Totp totp = new Totp(secret);
			if (totp.verify(code)) {
				user.setIsUsig2FA(true);
				users.save(user);
				return "redirect:/profile";
			}
		}
		return "redirect:/qrcode";
	}

	@PreAuthorize("hasRole('BOSS')")
	@PostMapping("/registerEmployee")
	String registerNewEmployee(Model model, @Valid RegistrationForm form,
			Errors result, final BindingResult bindingResult) {
		if (users.findByMail(form.getMail()) != null) {
			bindingResult.rejectValue("mail", "userData.email",
					"Es existiert bereits ein Account mit dieser Email-Adresse.");
			model.addAttribute("registrationForm", form);
			return "register";
		}
		if (result.hasErrors()) {
			return "register";
		}

		// (｡◕‿◕｡)
		// Falles alles in Ordnung ist legen wir einen Customer an
		userManagement.createEmployee(form);
		return "redirect:/";
	}

	@PreAuthorize("hasRole('BOSS')")
	@PostMapping("/registerCustomer")
	String registerNewCustomer(Model model, @Valid RegistrationForm form,
			Errors result, final BindingResult bindingResult) {
		if (users.findByMail(form.getMail()) != null) {
			bindingResult.rejectValue("mail", "userData.email",
					"Es existiert bereits ein Account mit dieser Email-Adresse.");
			model.addAttribute("registrationForm", form);
			return "register";
		}
		if (result.hasErrors()) {
			return "register";
		}

		// (｡◕‿◕｡)
		// Falles alles in Ordnung ist legen wir einen Customer an
		userManagement.createUser(form);
		return "redirect:/";
	}

	@PostMapping("/register")
	String registerNew(Model model, @Valid RegistrationForm form, @RequestParam("g-recaptcha-response") String captcha,
			Errors result, final BindingResult bindingResult) {
				System.out.println(captcha);
		if (users.findByMail(form.getMail()) != null) {
			bindingResult.rejectValue("mail", "userData.email",
					"Es existiert bereits ein Account mit dieser Email-Adresse.");
			model.addAttribute("registrationForm", form);
			return "register";
		}

		try {
			captchaService.processResponse(captcha);
		} catch (Exception e) {
			return "register";
		}
		if (result.hasErrors()) {
			return "register";
		}

		// (｡◕‿◕｡)
		// Falles alles in Ordnung ist legen wir einen Customer an
		userManagement.createUser(form);
		return "redirect:/";
	}

	@GetMapping("/register")
	String register(Model model, RegistrationForm form) {
		return "register";
	}

	@GetMapping("/profile")
	@PreAuthorize("hasRole('CUSTOMER')" + "|| hasRole('BOSS')" + "|| hasRole('EMPLOYEE')")
	String profile(Model model, @LoggedIn UserAccount userAccount) {

		model.addAttribute("customer", userManagement.findByUserAccount(userAccount));
		return "profile";
	}

	@GetMapping("/users")
	@PreAuthorize("hasRole('BOSS')" + "|| hasRole('EMPLOYEE')")
	String customers(Model model) {

		model.addAttribute("customerList", userManagement.findAll());
		return "users";
	}

	@GetMapping("/employee")
	@PreAuthorize("hasRole('BOSS')")
	String employee(Model model, RegistrationForm form) {
		return "employee";
	}

	@GetMapping("/usersemployee")
	@PreAuthorize("hasRole('BOSS')")
	String usersemployee(Model model) {

		model.addAttribute("customerList", userManagement.findAll());
		return "usersemployee";
	}

	@GetMapping("/userscustomer")
	@PreAuthorize("hasRole('BOSS')" + "|| hasRole('EMPLOYEE')")
	String userscustomer(Model model) {

		model.addAttribute("customerList", userManagement.findAll());
		return "userscustomer";
	}

	@GetMapping("/deleteAccount/{user}")
	@PreAuthorize("hasRole('BOSS')")
	String deleteAccount(@PathVariable Long user) {

		userManagement.deleteUser(user);

		return "redirect:/users";
	}

	@GetMapping("/chooseAccount")
	@PreAuthorize("hasRole('BOSS')" + "|| hasRole('EMPLOYEE')")
	String chooseAccount(Model model) {
		model.addAttribute("customerList", userManagement.findAll());
		return "chooseAccount";
	}

	@GetMapping("/getAccount")
	@PreAuthorize("hasRole('BOSS')" + "|| hasRole('EMPLOYEE')")
	String getAccount(Model model, @RequestParam("name") String name) {
		List<User> users = new ArrayList<User>();

		for (User user : userManagement.findAll()) {
			users.add(user);
		}

		if (!(name == "")) {
			for (int i = users.size() - 1; i >= 0; i--) {
				if (!users.get(i).getUserAccount().getUsername().equals(name)) {
					users.remove(users.get(i));
				}
			}
		}
		model.addAttribute("customerList", users);
		return "chooseAccount";
	}

	@GetMapping("/changePassword/{user}")
	@PreAuthorize("hasRole('BOSS')")
	String changePassword(@PathVariable UserAccount user, String newPassword) {

		userManagement.setPassword(user, UnencryptedPassword.of(newPassword));

		return "redirect:/";
	}

	// @PostMapping("/mail")
	// String mail(@RequestParam("email") String email) {
	// Mail.sendEMail(email);
	// return("redirect:/");
	// }

	@PostMapping("/forgot-password")
	public ModelAndView forgotPassword(ModelAndView modelAndView, @RequestParam("name") String name) {
		UserAccount existingUser = userManagement.findByName(name);
		User user = users.findByUserAccount(existingUser);
		if (existingUser != null) {
			user.setResetToken(UUID.randomUUID().toString());
			user.setTokenDate(LocalDateTime.now());
			users.save(user);

			try {
				Email mail = EmailBuilder.startingBlank().from("CampingplatzGruppe2@gmail.com")
						.to(existingUser.getUsername()).withSubject("Passwort zurücksetzen")
						.withPlainText("Um das Passwort zurück zusetzen, klicken Sie bitte hier: "
								+ "http://localhost:1337/confirm-reset?token=" + user.getResetToken()
								+ "\noder kopieren den folgen Code:\n" + user.getResetToken())
						.buildEmail();
				Mail.sendEMail(mail);
			} catch (Exception e) {
				System.out.println("UserController.forgotPassword()" + e);
			}

			System.out.println("http://localhost:1337/confirm-reset?token=" + user.getResetToken());
			modelAndView.addObject("message",
					"Wir haben ihre Anfrage erhalten. Bitte überprüfen Sie ihr E-Mail-Postfach für den Reset link.");
			modelAndView.setViewName("/successForgotPassword");

		} else {
			modelAndView.addObject("message", "This email does not exist!");
			modelAndView.setViewName("/error");
		}
		return modelAndView;
	}

	@GetMapping("/confirm-reset")
	public ModelAndView validateResetToken(ModelAndView modelAndView, @RequestParam("token") String confirmationToken) {
		System.out.println("UserController.validateResetToken()");
		User user = users.findByResetToken(confirmationToken).get();
		LocalDateTime comparison = user.getTokenDate().plusDays(1);
		if (user != null && LocalDateTime.now().isBefore(comparison)) {
			modelAndView.addObject("user", user);
			modelAndView.addObject("userAccount", user.getUserAccount());
			modelAndView.addObject("password", user.getUserAccount().getPassword());
			modelAndView.addObject("email", user.getUserAccount().getEmail());
			modelAndView.setViewName("/resetPassword");
		} else {
			modelAndView.addObject("message", "The link is invalid or broken!");
			modelAndView.setViewName("/error");
		}

		return modelAndView;
	}

	@PostMapping("/reset-password")
	public ModelAndView resetUserPassword(ModelAndView modelAndView,
			@RequestParam("userAccount") UserAccount userAccount, @RequestParam("password") String newPassword) {
		System.out.println("UserController.resetUserPassword()");

		userManagement.setPassword(userAccount, UnencryptedPassword.of(newPassword));
		users.findByUserAccount(userAccount).resetTriedLogins();
		if (userAccount != null) {
			User user = users.findByUserAccount(userAccount);
			user.setResetToken(null);
			user.setTokenDate(null);
			users.save(user);

			modelAndView.addObject("message", "Passwort erfoglreich geändert.");
			modelAndView.setViewName("/successResetPassword");
		} else {
			modelAndView.addObject("message", "The link is invalid or broken!");
			modelAndView.setViewName("error");
		}

		return modelAndView;
	}

	@GetMapping("/resetUser")
	@PreAuthorize("hasRole('BOSS')" + "|| hasRole('EMPLOYEE')")
	public String resetUser(@RequestParam("name") String name) {
		System.out.println("UserController.resetUser()");
		User user = users.findByUsername(name);
		try {
			user.resetTriedLogins();
			users.save(user);
		} catch (Exception e) {
			System.out.println("UserController.resetUser()" + e);
		}

		return "redirect:/users";
	}

	@GetMapping("/reset2FA")
	@PreAuthorize("hasRole('BOSS')" + "|| hasRole('EMPLOYEE')")
	public String reset2FA(@RequestParam("name") String name) {
		System.out.println("UserController.resetUser()");
		User user = users.findByUsername(name);
		try {
			user.setIsUsig2FA(false);
			users.save(user);
		} catch (Exception e) {
			System.out.println("UserController.resetUser()" + e);
		}

		return "redirect:/users";
	}

	@PostMapping("/sendReset")
	public ModelAndView sendReset(ModelAndView modelAndView, @RequestParam("username") String name,
			BindingResult bindingResult) {

		User user = null;
		try {
			UserAccount existingUser = userManagement.findByName(name);
			user = users.findByUserAccount(existingUser);
		} catch (Exception e) {
			// TODO: handle exception
		}

		if (user != null) {
			user.setAccountResetToken(UUID.randomUUID().toString());
			user.setAccountTokenDate(LocalDateTime.now());
			users.save(user);

			try {
				Email mail = EmailBuilder.startingBlank().from("CampingplatzGruppe2@gmail.com").to(user.getUsername())
						.withSubject("Account freischalten")
						.withPlainText("Um ihren Account freizuschalten, klicken Sie bitte hier: "
								+ "http://localhost:1337/confirm-AccountReset?token=" + user.getAccountResetToken()
								+ "\noder kopieren den folgen Code:\n" + user.getAccountResetToken())
						.buildEmail();
				Mail.sendEMail(mail);
			} catch (Exception e) {
				System.out.println("UserController.sendReset()" + e);
			}

			System.out.println("http://localhost:1337/confirm-reset?token=" + user.getAccountResetToken());
			modelAndView.addObject("message",
					"Wir haben ihre Anfrage erhalten. Bitte überprüfen Sie ihr E-Mail-Postfach für den Reset link.");
			modelAndView.setViewName("/successresetaccount");

		} else {
			modelAndView.addObject("message", "This email does not exist!");
			modelAndView.setViewName("/error");
		}
		return modelAndView;
	}

	@GetMapping("/confirm-AccountReset")
	public ModelAndView validateAccountResetToken(ModelAndView modelAndView,
			@RequestParam("token") String confirmationToken) {
		System.out.println("UserController.validateResetToken()");
		User user = null;
		LocalDateTime comparison = LocalDateTime.now().minusMinutes(5);
		try {
			user = users.findByAccountResetToken(confirmationToken).get();
			comparison = user.getAccountTokenDate().plusDays(1);
		} catch (Exception e) {
			// TODO: handle exception
		}

		if (user != null && LocalDateTime.now().isBefore(comparison)) {
			modelAndView.addObject("user", user);
			user.resetTriedLogins();
			users.save(user);
			modelAndView.addObject("message", "Ihr Account wurde erfoglreich freigeschaltet.");
			modelAndView.setViewName("/successResetAccount");
		} else {
			modelAndView.addObject("message", "The link is invalid or broken!");
			modelAndView.setViewName("/error");
		}

		return modelAndView;
	}

}
