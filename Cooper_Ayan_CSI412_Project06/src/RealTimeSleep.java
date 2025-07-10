// Test program that infinitely prints "Top Priority and sleep" to test RealTime priority and sleep method
public class RealTimeSleep extends UserlandProcess{

    public void main() throws InterruptedException {
        while (true) {
            System.out.println("Top priority and sleep");
            OS.Sleep(100);
            cooperate();
            try {
                Thread.sleep(50); // sleep for 50 ms
            } catch (Exception e) { }
        }
    }

    // Used to understand process is RealTime and sleeping
    public String toString () {
        return "RealTime Sleep";
    }

}
