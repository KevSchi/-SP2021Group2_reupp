package campingplatz.user;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;



// (｡◕‿◕｡)
// Manuelle Validierung ist mühsam, Spring bietet hierfür auch Support.
// Um die Registrierung auf korrekte Eingaben zu checken, schreiben eine POJO-Klasse, welche den
// Inputfeldern der Registrierung entspricht.
// Diese werden annotiert, damit der Validator weiß, worauf geprüft werden soll.
// Via message übergeben wir einen Resourcekey um die Fehlermeldungen auch internationalisierbar
// zu machen. Die ValidationMessages.properties Datei befindet sich in /src/main/resources.
// Lektüre:
// http://docs.oracle.com/javaee/6/tutorial/doc/gircz.html
// http://docs.jboss.org/hibernate/validator/4.2/reference/en-US/html/

public class RegistrationForm {

	@NotEmpty(message = "{RegistrationForm.firstName.NotEmpty}") //
	private final String firstName;

	@NotEmpty(message = "{RegistrationForm.lastName.NotEmpty}") //
	private final String lastName;

	@NotEmpty(message = "{RegistrationForm.password.NotEmpty}") //
	private final String password;
	@Column(unique = true)
	@NotEmpty(message = "{RegistrationForm.mail.NotEmpty}") // s
	private final String mail;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	//@NotEmpty(message = "{RegistrationForm.birthDate.NotEmpty}") // s
	private final LocalDate birthDate;

	@Size(min = 5, max = 5)
	@NotEmpty(message = "{RegistrationForm.plz.NotEmpty}") // s
	private final String plz;
	
	@NotEmpty(message = "{RegistrationForm.street.NotEmpty}") // s
	private final String street;

	@NotEmpty(message = "{RegistrationForm.city.NotEmpty}") // s
	private final String city;

	public RegistrationForm(String firstName, String lastName, String password, String mail, LocalDate birthDate, String street, String city, String plz) {
		
		this.firstName = firstName; 
		this.lastName = lastName;
		this.password = password;
		this.mail = mail;
		this.birthDate = birthDate;
		this.street = street;
		this.city = city;
		this.plz = plz;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getPassword() {
		return password;
	}

	public String getMail() {
		return mail;
	}

	public LocalDate getBirthDate() {
		return birthDate;
	}

	public String getStreet() {
		return street;
	}

	public String getCity() {
		return city;
	}

	public String getPlz() {
		return plz;
	}

	public boolean testPlz() {
		if(plz.length() != 5){
			return false; 
		}else{return true;}
	}


}