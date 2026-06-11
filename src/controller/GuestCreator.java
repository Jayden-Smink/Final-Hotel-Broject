package controller;

import factory.PersonFactory;
import model.Guest;
import model.PersonType;

public class GuestCreator {

    public Guest create(int guestId) {
        return (Guest) PersonFactory.createPerson(
                PersonType.GUEST,
                guestId,
                0,
                0
        );
    }
}