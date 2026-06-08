package factory;

import model.Cleaner;
import model.Guest;
import model.Person;
import model.PersonType;

public class PersonFactory {

    // Retourneert nu het generieke 'Person' type en heet 'createPerson'
    public static Person createPerson(PersonType type, int id, double x, double y) {
        switch (type) {
            case GUEST:
                return new Guest(id, x, y);

            case CLEANER:
                return new Cleaner(id, x, y);

            default:
                throw new IllegalArgumentException("Unknown person type: " + type);
        }
    }
}