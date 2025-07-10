public class Pong extends UserlandProcess{

    public void main() throws InterruptedException {
        KernelMessage km = null; // Make a KernelMessage to be properly initialized later
        int pingPid = OS.GetPidByName("Ping"); // Get Ping's PID
        System.out.println("Pong got Ping's PID: " + pingPid);
        int message = 0; // Message count

        while (true) {
            OS.Sleep(10); // Used to minimize mixed-up statement order as much as possible. Message functionality still works.
            int pongPid = OS.GetPid(); // Get Pong's PID

            // Send a message to Ping
            if (km == null) {
                // Initialize km with proper sender and receiver
                km = new KernelMessage(pongPid, pingPid, message, new byte[0]);
                // Send the message
                OS.SendMessage(km);
                System.out.println("PONG: from: " + pongPid + " to: " + pingPid + " what: " + message);
                // Increment the message count
                message++;
            }

            // Wait for a message from Ping
            km = OS.WaitForMessage();

            cooperate(); // Ensures that OS will switch processes
            // Sleeps to make output less intimidating
            try {
                Thread.sleep(50); // sleep for 50 ms
            } catch (Exception e) { }
        }
    }
}
