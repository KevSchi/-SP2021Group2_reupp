package campingplatz.user;

import org.salespointframework.useraccount.Password.UnencryptedPassword;


import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManagement;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Service
@Transactional
public class UserManagement {
	
	public static final Role CUSTOMER_ROLE = Role.of("CUSTOMER");

	private final UserRepository users;
	private final UserAccountManagement userAccounts;


	UserManagement(UserRepository users, UserAccountManagement userAccounts) {

		Assert.notNull(users, "CustomerRepository must not be null!");
		Assert.notNull(userAccounts, "UserAccountManagement must not be null!");

		this.users = users;
		this.userAccounts = userAccounts;
	}

	public void setPassword(UserAccount userAccount, UnencryptedPassword password){
		userAccounts.changePassword(userAccount, password);
	}

	public User createEmployee(RegistrationForm form) {

		Assert.notNull(form, "Registration form must not be null!");

		var password = UnencryptedPassword.of(form.getPassword());
		var userAccount = userAccounts.create(form.getMail(), password, Role.of("EMPLOYEE"));
		return users.save(new User(userAccount, form.getFirstName(), form.getLastName(), form.getBirthDate(), form.getStreet(), form.getCity(), form.getPlz()));
	}

	public void deleteUser(Long user) {
		users.deleteById(user);
	}


	public User createUser(RegistrationForm form) {
		Assert.notNull(form, "Registration form must not be null!");
		
		//Assert.state(form.testPlz(), "Plz is wrong!");


		var password = UnencryptedPassword.of(form.getPassword());
		
		var userAccount = userAccounts.create(form.getMail(), password, Role.of("CUSTOMER"));
		return users.save(new User(userAccount, form.getFirstName(), form.getLastName(), form.getBirthDate(), form.getStreet(), form.getCity(), form.getPlz()));

	}

	public Streamable<User> findAll() {
		return users.findAll();
	}

	public UserAccount findByName(String name) {
		return userAccounts.findByUsername(name).get();
	}

	public User findUserByID(Long id) {
		return users.findById(id).get();
	}
	public User findByUserAccount(UserAccount userAccount) {
		return users.findByUserAccount(userAccount);
	}

	public void save(User user){
		users.save(user);
	}

}