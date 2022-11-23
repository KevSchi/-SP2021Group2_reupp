package campingplatz.inventory2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class InventoryService {

    private static class Campsite {
        private enum Status {
            OPEN, RESERVED, BOOKED, DEFECTIVE
        }

        private UUID id;
        private String name;
        private String description;
        private Status status = Status.OPEN;

        public Campsite(){
            
        }

        public Campsite(String name, String description) {
            super();
            this.id = UUID.randomUUID();
            this.name = name;
            this.description = description;
        }

        public UUID getId() {
            return this.id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Status getStatus() {
            return this.status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }

        public String getDescription() {
            return this.description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    private List<Campsite> campsites = new ArrayList<>(Arrays.asList());

    public List<Campsite> getAllCampsites() {
        return campsites;
    }

    public Campsite getCampsite(UUID id) {
        return campsites.stream().filter(c -> c.getId().equals(id)).findFirst().get();
    }

    public void addCampsite(Campsite campsite) {
        campsites.add(campsite);
    }

    public void updateCampsite(UUID id, Campsite campsite) {
        for (int i = 0; i < campsites.size(); i++) {
            Campsite c = campsites.get(i);
            if (c.getId().equals(id)) {
                if (campsite.getDescription() == null)
                    campsite.setDescription(c.getDescription());
                if (campsite.getId() == null)
                    campsite.setId(c.getId());
                if (campsite.getName() == null)
                    campsite.setName(c.getName());
                campsites.set(i, campsite);
                return;
            }
        }
    }

    public void deleteCampsite(UUID id) {
        campsites.removeIf(c -> c.getId().equals(id));
    }
}

/**
 * // loop thru each class propertie
 * 
 * Class<?> cls = campsite.getClass(); Field fieldlist[] =
 * cls.getDeclaredFields(); for (Field aFieldlist : fieldlist) {
 * System.out.println(aFieldlist); }
 */