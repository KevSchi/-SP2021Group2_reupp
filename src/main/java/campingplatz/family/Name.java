package campingplatz.family;

public class Name {
    String firstName;
    String lastName;

    public Name(String firstName, String lastName){
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String get() {
		return this.firstName;
	}

    public void set(String firstName) {
		this.firstName = firstName;
	}

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    } 

    
}
