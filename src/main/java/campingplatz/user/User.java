package campingplatz.user;



import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import static org.salespointframework.core.Currencies.*;
import org.javamoney.moneta.Money;
import org.jboss.aerogear.security.otp.api.Base32;
import org.salespointframework.order.CartItem;
import org.salespointframework.useraccount.UserAccount;
import org.springframework.format.annotation.DateTimeFormat;

// (｡◕‿◕｡)
// Salespoint bietet nur eine UserAccount Verwaltung, für weitere Attribute sollte eine extra
// Klasse geschrieben werden. Unser Kunde hat noch eine Adresse, das bietet der UserAccount nicht.
// Um den Customer in die Datenbank zu bekommen, schreiben wir ein CustomerRepository.

@Entity
public class User{

	private @Id @GeneratedValue long id;
	private String mail;
	private String username;
	private String emailId;
	private String resetToken;
	private LocalDateTime tokenDate;
	private String accountResetToken;
	private LocalDateTime accountTokenDate;
	private boolean isUsing2FA;
    private String secret;
	private int triedLogins;
	private LocalDateTime pwChecked;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate birthDate;
	private String street;
	private String city;
	private String plz;

	@ElementCollection
	private List<Date> dates = new ArrayList<Date>();

	// (｡◕‿◕｡)
	// Jedem Customer ist genau ein UserAccount zugeordnet, um später über den UserAccount an den
	// Customer zu kommen, speichern wir den hier
	@OneToOne //
	private UserAccount userAccount;

	@SuppressWarnings("unused")
	private User() {
		this.secret = Base32.random();
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	

	public User(UserAccount userAccount, String firstname, String lastname, LocalDate birthDate, String street, String city, String plz) {
		this.userAccount = userAccount;
		username = userAccount.getUsername();
		this.mail = userAccount.getUsername();
		this.userAccount.setFirstname(firstname);
		this.userAccount.setLastname(lastname);
		this.birthDate = birthDate;
		this.street = street;
		this.city  = city;
		this.plz = plz;
	}
	public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
	public void setPwChecked(LocalDateTime pwChecked) {
		this.pwChecked = pwChecked;
	}
	public LocalDateTime getPwChecked() {
		return pwChecked;
	}

	public boolean ispwCheckedOK(){
		if(pwChecked == null) return false;
		return LocalDateTime.now().isBefore(pwChecked.plusMinutes(2));
	}
	
	public Long getId() {
		return id;
	}

	public String getMail() {
		return mail;
	}
	public String getPassword(){
		return userAccount.getPassword().toString();
	}
	public void setName(String firstname, String lastname){
		this.userAccount.setFirstname(firstname);
		this.userAccount.setLastname(lastname);
	}
	public String getName(){
		String first = this.userAccount.getFirstname();
		String last = this.userAccount.getLastname();
		return first + " " + last;
	}

	public String getFirstName(){
		return this.userAccount.getFirstname();
	}
	

	public void setFirstname(String firstname){
		this.userAccount.setFirstname(firstname);
	}
	public String getLastName(){
		return this.userAccount.getLastname();
	}
	public void setLastname(String lastname){
		this.userAccount.setLastname(lastname);
	}
	public void setMail(String mail) {
		this.mail = mail;
		this.userAccount.setEmail(mail);
	}

	public UserAccount getUserAccount() {
		return userAccount;
	}
	public LocalDate getBirthDate() {
		return this.birthDate;
	}

	public void setBirthDate(LocalDate birthDate) {
		this.birthDate = birthDate;
	}

	public String getStreet() {
		return this.street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getPlz() {
		return plz;
	}

	public void setPlz(String plz) {
		this.plz = plz;
	}

	public String getCity() {
		return this.city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public void setDate(Date date){

		dates.add(date);

	}

	public LocalDate getStart(CartItem item){
		LocalDate temp = null;
		for(Date date : dates){
			if (date.getItem() == item.getId()){
				temp = date.getStart();
			}
		 }
		
		 if(temp == null){
			return LocalDate.now();
		} 
		return temp;
	}

	public LocalDate getEnd(CartItem item){
		LocalDate temp = null;
		
		for(Date date : dates){
			if (date.getItem().equals(item.getId())){
				temp = date.getEnd();
			}
		}
		
		if(temp == null){
			return LocalDate.now();
		} 
		return temp;
		
	}

	public Date getDate(CartItem item){
		Date temp = null;
		
		for(Date date : dates){
			if (date.getItem().equals(item.getId())){
				temp = date;
			}
		}
		return temp;
		
	}

	public Money getCompleteCosts(){
		Money temp = Money.of(0,EURO);
		for (Date date : dates) {
			temp = temp.add(date.getCompleteCosts());
		}
		return temp;
	}

	public List<Date> getDates(){
		return dates;
	}

	public void clearDates(){
		System.out.println("clearing dates");
		dates.clear();
	}


	public boolean isUsing2FA(){
		return isUsing2FA;
	}

	public void setIsUsig2FA(boolean isUsing2FA){
		if(!isUsing2FA)	this.secret = null;
		this.isUsing2FA = isUsing2FA;
	}
	public String getSecret() {
		return secret;
	}
	public void setSecret() {
		this.secret = Base32.random();
	}

	public String getResetToken() {
		return resetToken;
	}

	public void setResetToken(String resetToken) {
		this.resetToken = resetToken;
	}
	public LocalDateTime getTokenDate() {
		return tokenDate;
	}
	public void setTokenDate(LocalDateTime tokenDate) {
		this.tokenDate = tokenDate;
	}

	public void incTriedLogins() {
		triedLogins++;
	}
	public void resetTriedLogins() {
		triedLogins = 0;
	}
	public int getTriedLogins() {
		return triedLogins;
	}
	public boolean toManyTries(){
		return triedLogins <= 2;
	}
	public String getAccountResetToken() {
		return accountResetToken;
	}
	public LocalDateTime getAccountTokenDate() {
		return accountTokenDate;
	}
	public void setAccountResetToken(String accountResetToken) {
		this.accountResetToken = accountResetToken;
	}
	public void setAccountTokenDate(LocalDateTime accountTokenDate) {
		this.accountTokenDate = accountTokenDate;
	}

}