import java.util.Random;

public class RandomDevice implements Device {
    // Create an array with a size of 10 random instances
    Random[] randDevices;

    public RandomDevice() {
        // Initialize the randDevices array with a size of 10
        randDevices = new Random[10];
    }

    // Creates a new RandomDevice and puts it in an empty spot in the array
    @Override
    public int Open(String s) {
        // Check if the supplied string is not null/empty
        if (s != null && !s.isEmpty()) {
            // Convert the string into an int
            int seed = Integer.parseInt(s);
            // Make a random device with the seed
            Random device = new Random(seed);
            // Iterate through the randDevices array, adding the random device to a null element
            for (int i = 0; i < randDevices.length; i++) {
                if (randDevices[i] == null) {
                    randDevices[i] = device;
                    // Return the index where the device was added
                    return i;
                }
            }
            // Return an error if the array is full
            return -1;
        }
        // If the seed is null/empty, return an error
        else {
            return -1;
        }
    }

    // Nulls the device entry based on the id
    @Override
    public void Close(int id) {
        System.out.println("Random device " + id + " closed"); // Log message indicating the random device is closed
        randDevices[id] = null;
    }

    // Create/fill an array with random values
    @Override
    public byte[] Read(int id, int size) {
        // Create and initialize a new array of byte
        byte[] data = new byte[size];
        // Ensure that a valid Random instance is available
        if (randDevices[id] == null) {
            // Fill data with random values
            randDevices[id] = new Random();
        }
        randDevices[id].nextBytes(data);
        // Return the newly filled byte array
        return data;
    }

    // Return 0 length and do nothing (since it doesn’t make sense)
    @Override
    public int Write(int id, byte[] data) {
        return 0;
    }

    // Read random bytes but not return them
    @Override
    public void Seek(int id, int to) {
        // Ensure that a valid Random instance is available
        if (randDevices[id] != null) {
            // Log the seek action
            System.out.println("Seeking in Random Device");
            return;
        }
        // If the device doesn't exist, simulate seeking by reading 'to' bytes
        Read(id, to);
    }
}
