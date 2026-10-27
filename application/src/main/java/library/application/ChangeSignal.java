package library.application;

/**
 * Tells whoever is listening that the library changed. A driven port with one method
 * and no arguments: the five command use cases call it after every command that
 * completed, and it says that something changed, not what. The adapter decides what to
 * do with that, and the usual answer is to run the listing use cases again. This is the
 * pull form of the Observer pattern, and what Fowler calls an event notification: the
 * smallest message that can cross, carrying no state for the two sides to disagree
 * about. Whether anyone listens, and how many, is the configurator's business; a driver
 * that does not care plugs in a lambda that does nothing. It is declared in this ring
 * because the use cases are its only callers; the two rings inside do not know it
 * exists.
 */
public interface ChangeSignal {

    void changed();
}
