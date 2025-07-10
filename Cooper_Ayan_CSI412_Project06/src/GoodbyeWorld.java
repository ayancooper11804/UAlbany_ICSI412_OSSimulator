public class GoodbyeWorld extends UserlandProcess {

    public void main() throws InterruptedException {
        KernelMessage km = null; // Make a KernelMessage to be properly initialized later
        int helloPid = OS.GetPidByName("HelloWorld"); // Get HelloWorld's PID
        System.out.println("Goodbye got Hello's PID: " + helloPid);
        int message = 0; // Message count

        while (true) {
            OS.Sleep(10); // Used to minimize mixed-up statement order as much as possible. Message functionality still works.
            int goodbyePid = OS.GetPid(); // Get GoodbyeWorld's PID

            // Send a message to Ping
            if (km == null) {
                // Initialize km with proper sender and receiver
                km = new KernelMessage(goodbyePid, helloPid, message, new byte[0]);
                // Send the message
                OS.SendMessage(km);
                System.out.println("GOODBYE: from: " + goodbyePid + " to: " + helloPid + " what: " + message);
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
