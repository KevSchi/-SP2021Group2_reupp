package campingplatz.family;

public class Customer {
    
    private String firstName;
    private String lastName;
    private String adress;
    private String mail;
    private String number;

    public Customer(String firstName, String lastName, String adress, String mail, String number){
        this.firstName = firstName; 
        this.lastName = lastName;
        this.adress = adress;
        this.mail = mail;
        this.number = number; 
    }

    public Customer(String firstname){
        this.firstName = firstname;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAdress() {
        return this.adress;
    }

    public void setAdress(String adress) {
        this.adress = adress;
    }

    public String getMail() {
        return this.mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getNumber() {
        return this.number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    
}
