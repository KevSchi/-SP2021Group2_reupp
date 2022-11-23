package campingplatz.user;

import java.time.LocalDate;
import java.util.List;

import org.salespointframework.core.DataInitializer;
import org.salespointframework.useraccount.Password.UnencryptedPassword;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
@Order(10)
class CustomerDataInitializer implements DataInitializer {

	private static final Logger LOG = LoggerFactory.getLogger(CustomerDataInitializer.class);

	private final UserAccountManagement userAccountManagement;
	private final UserManagement userManagement;

	CustomerDataInitializer(UserAccountManagement userAccountManagement, UserManagement userManagement) {

		Assert.notNull(userAccountManagement, "UserAccountManagement must not be null!");
		Assert.notNull(userManagement, "CustomerRepository must not be null!");

		this.userAccountManagement = userAccountManagement;
		this.userManagement = userManagement;
	}

	@Override
	public void initialize() {

		// Skip creation if database was already populated
		if (userAccountManagement.findByUsername("boss").isPresent()) {
			return;
		}

		LOG.info("Creating default users and customers.");

		UserAccount temp1 =userAccountManagement.create("boss", UnencryptedPassword.of("boss"), Role.of("BOSS"));
		UserAccount temp2 = userAccountManagement.create("employee1", UnencryptedPassword.of("employee"), Role.of("EMPLOYEE"));
		userManagement.save(new User(temp1, "boss", "boss", LocalDate.of(1212, 12, 12), "street", "city", "34567"));
		userManagement.save(new User(temp2, "Max", "Mustermann", LocalDate.of(1212, 12, 12), "Musterstraße", "Musterstadt", "23456"));

		List.of(//
				
				new RegistrationForm("vorname", "nachname", "a", "a", LocalDate.of(1000, 1, 1), "Strasse 1", "Stadt", "12345")
				//new RegistrationForm("hans", "a", password, "wurst@test.de"),
				//new RegistrationForm("dextermorgan", "a", password, "Miami@test.de"),
				//new RegistrationForm("earlhickey", "a",password, "Camden@test.de"),
				//new RegistrationForm("mclovinfogell", "a",password, "Los@test.de")//
		).forEach(userManagement::createUser);
	}
}
