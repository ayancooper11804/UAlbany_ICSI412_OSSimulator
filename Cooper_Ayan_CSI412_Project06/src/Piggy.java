public class Piggy extends UserlandProcess {

    void main() throws InterruptedException {

        while (true) {
            // Set the size for memory allocation
            int requestedSize = 100 * 1024;
            // Allocation for requested size
            int address = OS.AllocateMemory(requestedSize);

            // Check if memory allocation was successful
            if (address != -1) {
                System.out.println("Piggy allocation at address: " + address);

                byte valueToWrite = 50; // Value to write in allocated memory
                // Write the value to each byte of the allocated memory
                for (int i = 0; i < requestedSize; i++) {
                    Hardware.Write(address + i, valueToWrite);
                }

                boolean allocationCorrect = true; // Flag to check if the read/write was successful
                // Verify that the value written matches the value read from memory
                for (int i = 0; i < requestedSize; i++) {
                    byte readValue = Hardware.Read(address + i); // Read value from memory
                    if (readValue != valueToWrite) {
                        allocationCorrect = false; // Mark allocation as incorrect if values don't match
                        System.out.println("Piggy Read failed at address: " + (address + i));
                    }
                }

                // Print success or failure message based on read/write verification
                if (allocationCorrect) {
                    System.out.println("Reading and writing of value 50 successful.");
                } else {
                    System.out.println("Reading and writing of value 50 failed.");
                }

            }
            else {
                // Log if the allocation was unsuccessful
                System.out.println("Piggy allocation failed");
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
