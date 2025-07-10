// Prevents busy-waiting
public class IdleProcess extends UserlandProcess{

    public void main() throws InterruptedException {
        // Run an infinite loop of cooperate and Thread.sleep(50)
        while (true) {
            cooperate();
            Thread.sleep(50);
        }
    }

    public String toString () {
        return "Idle";
    }
}
