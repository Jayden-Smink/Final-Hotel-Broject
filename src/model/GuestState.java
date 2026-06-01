package model;

public enum GuestState {
    IDLE,               // Standaard beginstatus
    WALKING,            // Gebruikt voor normaal lopen naar lift of kamer
    MOVING,             // Eventueel voor andere specifieke bewegingen
    WAITING_FOR_LIFT,   // Wachten bij de schacht
    IN_QUEUE,           // In de rij voor de lift (mover laat ze hier met rust)
    IN_LIFT,            // In de lift
    EXITING_LIFT,       // Uitstappen uit de lift (mover pakt ze hier weer op)
    AT_DESTINATION      // Bestemming bereikt
}