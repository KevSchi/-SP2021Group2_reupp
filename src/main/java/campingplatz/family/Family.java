package campingplatz.family;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

import javax.persistence.Embeddable;

import org.springframework.lang.Nullable;


@Embeddable
public class Family implements Serializable{
    
    private UUID id;
    private ArrayList<String> otherPeople; 
	@Nullable
    private Boolean isInGroup = false; 

    public Family(){
        //this.contatPerson = UserAc
    }

	public  Family(ArrayList<String> otherPeople, Boolean isInGroup){
		
        this.id = UUID.randomUUID();
		this.otherPeople = otherPeople;
		this.isInGroup = isInGroup;
	}
	
	public UUID getId() {
		return this.id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public Boolean isIsInGroup() {
		return this.isInGroup;
	}

	public void setIsInGroup(Boolean isInGroup) {
		this.isInGroup = isInGroup;
	}

	public ArrayList<String> getOtherPeople() {
		return otherPeople;
	}
}
