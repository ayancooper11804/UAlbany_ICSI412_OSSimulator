// The class that manages what happens on startup
public class Init extends UserlandProcess{

    @Override
    public void main() throws InterruptedException {
        // Calls CreateProcess on Piggy 20 times showcase memory functionality (allocation, writing, reading, and freeing).
        for (int i = 0; i < 20; i++) {
            OS.CreateProcess(new Piggy());
        }
        OS.Exit();
    }

    // Used to understand this is an Init process
    public String toString () {
        return "Init";
    }
}
