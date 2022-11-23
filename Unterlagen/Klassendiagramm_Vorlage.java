import java.util.ArrayList;

public class Klassendiagramm_Vorlage {

	public static class Campsite {
		String name;
		int status;
		Family family;
		Booking booking;
		public Campsite(String _name, int _status) {
			this.name = _name;
			this.status = _status;
		}

		public void setFamily(Family _family) {
			this.family = _family;
			this.status = 1;
			if (family.isInGroup) {
				this.booking = new Booking(family.group);
			} else {
				this.booking = new Booking(null);
			}
			this.family.setBooking(this.booking);
		}
	}

	public static class Client {
		String name;
		int id;
		public Client(String _name) {
			this.name = _name;
		}
	}

	public static class Booking {
		int id;
		Group group;
		boolean isInGroup = false;
		int campsiteID;
		public Booking(Group _group) {
			if (group == null) {
				this.group = _group;
				this.isInGroup = true;
			}
			this.campsiteID = (int)((Math.random() * 100));
		}
	}

	public static class Family {
		Client contactPerson;
		Client[] others;
		Booking booking;
		Group group;
		boolean isInGroup = false;
		public Family(Client _contactPerson, Client[] _others) {
			this.contactPerson = _contactPerson;
			this.others = _others;
		}
		public String toString() {
			return "The contact Person is: " + contactPerson.name + " and has the Campsite nr." + this.booking.campsiteID;
		}
		public void setBooking(Booking _booking) {
			this.booking = _booking;
		}
	}

	public static class Group {
		ArrayList < Family > families = new ArrayList < Family > ();
		public Group() {}
		public void addFamily(Family family) {
			family.group = this;
			family.isInGroup = true;
			this.families.add(family);
		}
		public boolean isMoreThanOne() {
			if (families.size() > 1)
				return true;
			return false;
		}
	}

	public static void main(String[] args) throws Exception {


		Client client1 = new Client("Hans");
		Client client2 = new Client("Peter");
		Family family1 = new Family(client1, null);
		Family family2 = new Family(client2, null);
		Group group1 = new Group();
		group1.addFamily(family1);
		group1.addFamily(family2);

		// Link<list 

		Campsite campsite1 = new Campsite("Campsite1", 0);
		Campsite campsite2 = new Campsite("Campsite2", 1);
		Campsite campsite3 = new Campsite("Campsite3", 0);
		Campsite campsite4 = new Campsite("Campsite4", 1);
		Campsite campsite5 = new Campsite("Campsite5", 0);
		Campsite campsite6 = new Campsite("Campsite6", 0);
		Campsite campsite7 = new Campsite("Campsite7", 1);
		Campsite campsite8 = new Campsite("Campsite8", 0);

		ArrayList < Campsite > CampitesAll = new ArrayList < Campsite > ();
		CampitesAll.add(campsite1);
		CampitesAll.add(campsite2);
		CampitesAll.add(campsite3);
		CampitesAll.add(campsite4);
		CampitesAll.add(campsite5);
		CampitesAll.add(campsite6);
		CampitesAll.add(campsite7);
		CampitesAll.add(campsite8);


		int pos = getCampsite(CampitesAll);
		if (pos != -1) {
			CampitesAll.get(pos).setFamily(family1);
			CampitesAll.get(pos + 1).setFamily(family2);

			System.out.println(CampitesAll.get(pos).name);
			System.out.println(CampitesAll.get(pos).booking.group.families.get(1).toString());
		}
	}

	public static int getCampsite(ArrayList < Campsite > allCampsites) {
		for (int i = 0; i < allCampsites.size(); i++) {
			Campsite now = allCampsites.get(i);
			Campsite next = allCampsites.get(i + 1);
			if (now.status == next.status && now.status == 0) {
				return i;
			}
		}
		return -1;
	}
}