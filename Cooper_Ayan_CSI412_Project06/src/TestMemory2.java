public class TestMemory2 extends UserlandProcess{

    void main() throws InterruptedException {

        while (true) {
            // Set the size for the first and second memory allocation
            int requestedSize = 3072;
            int nextRequestedSize = 4096;

            // First allocation for requestedSize (1024)
            int address1 = OS.AllocateMemory(requestedSize);
            // Check if first memory allocation was successful
            if (address1 != -1) {
                System.out.println("TestMemory2 first allocation at address: " + address1);

                // Write a byte value of 50 to the first allocated address
                byte valueToWrite1 = 50;
                Hardware.Write(address1, valueToWrite1);
                System.out.println("TestMemory2 Written byte value: " + valueToWrite1 + " at address: " + address1);

                // Read the byte value from the first allocated address
                byte readValue1 = Hardware.Read(address1);
                System.out.println("TestMemory2 Read byte value: " + readValue1 + " from address: " + address1);

            }
            else {
                // Log if the first allocation was unsuccessful
                System.out.println("TestMemory2 first allocation failed");
            }

            // Second allocation for nextRequestedSize (2048)
            int address2 = OS.AllocateMemory(nextRequestedSize);
            // Check if second memory allocation was successful
            if (address2 != -1) {
                System.out.println("TestMemory2 second allocation at address: " + address2);

                // Write a byte value of 100 to the second allocated address
                byte valueToWrite2 = 100;
                Hardware.Write(address2, valueToWrite2);
                System.out.println("TestMemory2 Written byte value: " + valueToWrite2 + " at address: " + address2);

                // Read the byte value from the second allocated address
                byte readValue2 = Hardware.Read(address2);
                System.out.println("TestMemory2 Read byte value: " + readValue2 + " from address: " + address2);
            }
            else {
                // Log if the second allocation was unsuccessful
                System.out.println("TestMemory2 second allocation failed");
            }

            // Free both allocations if they were successful
            if (address1 != -1 && OS.FreeMemory(address1, requestedSize)) {
                // Confirm first memory was freed
                System.out.println("TestMemory2 freed first allocation");
            }
            if (address2 != -1 && OS.FreeMemory(address2, nextRequestedSize)) {
                // Confirm second memory was freed
                System.out.println("TestMemory2 freed second allocation");
            }

            // Breaks used for reading output easier
            System.out.println("---------------");
            System.out.println("---------------");

            cooperate(); // Allow other processes to run
            try {
                Thread.sleep(50); // sleep for 50 ms
            } catch (Exception e) { }
        }

    }
}
