package campingplatz.user;


import java.util.Optional;

import org.salespointframework.useraccount.UserAccount;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Repository;

@Repository("userRepository")
public interface UserRepository extends CrudRepository<User, Long> {
	@Override
	Streamable<User> findAll();
	User findByUsername(String username);
	User findByUserAccount(UserAccount userAccount);

	User findByMail(String mail);
	Optional<User> findByAccountResetToken(String accountResetToken);

	Optional<User> findByResetToken(String resetToken);
}