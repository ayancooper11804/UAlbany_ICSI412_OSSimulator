// Class that handles memory management
public class Hardware {

    public static byte[] memory = new byte[1024 * 1024]; // Static array of 1,048,576 bytes (1MB)
    public static int[][] TLB = new int[2][2]; // TLB which holds 2 virtual addresses and 2 physical addresses

    // Clear the TLB (used when switching processes)
    public static void clearTLB() {
        // Iterate over both rows in the TLB
        for (int i = 0; i < TLB.length; i++) {
            for (int j = 0; j < TLB[i].length; j++) {
                TLB[i][j] = -1; // Clear each entry
            }
        }
    }

    // Read a byte from memory
    public static byte Read(int address) throws InterruptedException {
        int virtualPage = address / 1024; // Calculate virtual page
        int pageOffset = address % 1024; // Calculate offset within page
        int physicalPage = getPhysicalPage(virtualPage); // Find physical page
        int physicalAddress = (physicalPage * 1024) + pageOffset; // Compute physical address
        return memory[physicalAddress];
    }

    // Write a byte to memory
    public static void Write(int address, byte value) throws InterruptedException {
        int virtualPage = address / 1024; // Calculate virtual page
        int pageOffset = address % 1024; // Calculate offset within page
        int physicalPage = getPhysicalPage(virtualPage); // Find physical page
        int physicalAddress = (physicalPage * 1024) + pageOffset; // Compute physical address
        memory[physicalAddress] = value;
    }

    // Find the physical page for a virtual page using the TLB
    public static int getPhysicalPage(int virtualPage) throws InterruptedException {
        // Search TLB for virtual-to-physical page mapping
        for (int i = 0; i < TLB.length; i++) {
            if (TLB[i][0] == virtualPage) { // Check if current TLB entry matches the virtual page
                return TLB[i][1]; // Return the associated physical page if mapping is found
            }
        }
        // If mapping isn't found in the TLB, call GetMapping to load it
        // This is expected to update the TLB with the requested mapping
        OS.GetMapping(virtualPage);
        // Retry the TLB search after GetMapping has updated the TLB
        for (int i = 0; i < TLB.length; i++) {
            if (TLB[i][0] == virtualPage) { // Check again for the virtual-to-physical page mapping
                return TLB[i][1]; // Return the physical page if the mapping is found on the second attempt
            }
        }
        // Throw an error if the mapping still isn't found
        throw new RuntimeException("TLB mapping failed after GetMapping");
    }

}
