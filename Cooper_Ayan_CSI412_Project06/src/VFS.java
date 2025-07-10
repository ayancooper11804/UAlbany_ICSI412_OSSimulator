import java.util.Arrays;

// Virtual File System will map calls to certain devices
public class VFS implements Device{

    // Arrays to store up to 20 devices and corresponding IDs
    Device[] devices;
    int[] ids;

    public VFS() {
        // Initialize the devices and ids arrays with a size of 20
        devices = new Device[20];
        ids = new int[20];
        // Initialize all IDs to -1, indicating no devices are open
        for (int i = 0; i < 20; i++) {
            ids[i] = -1;
        }
    }

    // Get the first word from the string to be the device, and the rest of the string as parameters
    @Override
    public int Open(String s) throws InterruptedException {
        // Split the string to get the device and its parameters
        String[] input = s.split(" ");
        String remainingParams = String.join(" ", Arrays.copyOfRange(input, 1, input.length));
        // Iterate over the devices array to find an empty slot
        for (int i = 0; i < 20; i++) {
            // If a slot is empty, open the device based on the input
            if (devices[i] == null) {
                // If the input is "random", create and open a RandomDevice
                if (input[0].equals("random")) {
                    System.out.println("Random opened");
                    devices[i] = new RandomDevice();
                    int openRandom = devices[i].Open(remainingParams); // Open the RandomDevice
                    ids[i] = openRandom; // Store the device ID
                    return i; // Return the index of the opened device
                }
                // If the input is "file", create and open a FakeFileSystem
                else if (input[0].equals("file")) {
                    System.out.println("File opened");
                    devices[i] = new FakeFileSystem();
                    int openFile = devices[i].Open(remainingParams); // Open the FakeFileSystem
                    ids[i] = openFile; // Store the device ID
                    return i; // Return the index of the opened device
                }
            }
        }
        // Return -1 if there is no available slot or if the device couldn't be opened
        return -1;
    }

    // Remove the device and id entries
    @Override
    public void Close(int id) {
        // Check if the ID is valid and the device exists at the specified index
        if (id != -1) {
            if (devices[id] != null && ids[id] != -1) {
                Device device = devices[id]; // Get the device from the array
                int deviceId = ids[id]; // Get the device ID
                System.out.println("VFS Close called with device: " + device);
                device.Close(deviceId); // Call the close method on the device
                devices[id] = null; // Reset the device slot
                ids[id] = -1; // Reset the ID slot
            }
        }
    }

    // Pass through Read to the appropriate device
    @Override
    public byte[] Read(int id, int size) {
        // Check if the device exists and is open at the specified index
        if (devices[id] != null && ids[id] != -1) {
            Device device = devices[id]; // Get the device from the array
            int deviceId = ids[id]; // Get the device ID
            System.out.println("VFS Read called with device: " + device);
            return device.Read(deviceId, size); // Call the Read method on the device
        }
        return null; // Return null if the device is not available
    }

    // Pass through Write to the appropriate device
    @Override
    public int Write(int id, byte[] data) {
        // Check if the device exists and is open at the specified index
        if (devices[id] != null && ids[id] != -1) {
            Device device = devices[id]; // Get the device from the array
            int deviceId = ids[id]; // Get the device ID
            return device.Write(deviceId, data); // Call the Write method on the device
        }
        return -1; // Return -1 if the device is not available
    }

    // Pass through Seek to the appropriate device
    @Override
    public void Seek(int id, int to) {
        // Check if the device exists and is open at the specified index
        if (devices[id] != null && ids[id] != -1) {
            Device device = devices[id]; // Get the device from the array
            int deviceId = ids[id]; // Get the device ID
            System.out.println("VFS Seek called with device: " + device);
            device.Seek(deviceId, to); // Call the Seek method on the device
        }
    }
}
