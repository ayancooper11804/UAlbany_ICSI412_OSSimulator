// Used to run our programs
public class Main {

    public static void main (String[] args) throws InterruptedException {
        // Calls Startup() on Init, the class that manages what happens on startup
        OS.Startup(new Init());
    }
}
