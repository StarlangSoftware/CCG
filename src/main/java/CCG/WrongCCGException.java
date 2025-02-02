package CCG;

public class WrongCCGException extends Exception {

    public WrongCCGException() {
        super();
    }

    public String toString() {
        return "The word's CCG is assigned incorrectly.";
    }
}
