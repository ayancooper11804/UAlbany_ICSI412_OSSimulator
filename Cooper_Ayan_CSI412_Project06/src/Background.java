// Test program that infinitely prints "Bottom Priority" to test Background priority
public class Background extends UserlandProcess{

    public void main() throws InterruptedException {
        while (true) {
            System.out.println("Bottom priority");
            cooperate();
            try {
                Thread.sleep(50); // sleep for 50 ms
            } catch (Exception e) { }
        }
    }

    // Used to understand process is Background
    public String toString () {
        return "Background";
    }

}
