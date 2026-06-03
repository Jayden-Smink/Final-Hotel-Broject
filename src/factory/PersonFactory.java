package factory;

import model.Guest;
import model.PersonType;

public class PersonFactory {

    // Verander 'Object' naar 'Guest' voor een type-veilige return
    public static Guest createGuest(PersonType type, int id, int x, int y) {
        switch (type) {
            case GUEST:
                return new Guest(id, x, y);

            default:
                throw new IllegalArgumentException("Unknown person type: " + type);
        }
    }
}