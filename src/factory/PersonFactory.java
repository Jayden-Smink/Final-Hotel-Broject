package factory;

import model.Guest;
import model.PersonType;

public class PersonFactory {

    public static Object createGuest(PersonType type, int id, int x, int y) {
        switch (type) {
            case GUEST:
                return new Guest(id, x, y);

            default:
                throw new IllegalArgumentException("Unknown person type: " + type);
        }
    }
}