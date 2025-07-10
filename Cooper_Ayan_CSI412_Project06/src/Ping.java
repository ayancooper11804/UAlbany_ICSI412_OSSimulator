public class Ping extends UserlandProcess{

    public void main() throws InterruptedException {
        KernelMessage km = null; // Make a KernelMessage to be properly initialized later
        int pongPid = OS.GetPidByName("Pong"); // Get Pong's PID
        System.out.println("Ping got Pong's PID: " + pongPid);
        int message = 0; // Message count

        while (true) {
            OS.Sleep(10); // Used to minimize mixed-up statement order as much as possible. Message functionality still works.
            int pingPid = OS.GetPid(); // Get Ping's PID

            // Send a message to Pong
            if (km == null) {
                // Initialize km with proper sender and receiver
                km = new KernelMessage(pingPid, pongPid, message, new byte[0]);
                // Send the message
                OS.SendMessage(km);
                System.out.println("PING: from: " + pingPid + " to: " + pongPid + " what: " + message);
                // Increment the message count
                message++;
            }

            // Wait for a message from Pong
            km = OS.WaitForMessage();

            cooperate(); // Ensures that OS will switch processes
            // Sleeps to make output less intimidating
            try {
                Thread.sleep(50); // sleep for 50 ms
            } catch (Exception e) { }
        }
    }
}
