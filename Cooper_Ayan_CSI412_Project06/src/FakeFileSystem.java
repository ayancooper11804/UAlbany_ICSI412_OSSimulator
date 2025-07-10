import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FakeFileSystem implements Device{

    // Create an array with a size of 10 RandomAccessFile instances
    RandomAccessFile[] randAccessFiles;

    public FakeFileSystem()  {
        // Initialize the RandomAccessFile array with a size of 10
        randAccessFiles = new RandomAccessFile[10];
    }

    @Override
    public int Open(String filename) {
        // Check if the filename provided is null and throw an exception if so
        if (filename == null) {
            throw new IllegalArgumentException("filename cannot be null");
        }
        try {
            // Open the file with read, write, and seek access
            RandomAccessFile file = new RandomAccessFile(filename, "rws");

            // Find an empty slot in the randAccessFiles array to store the opened file
            for (int i = 0; i < randAccessFiles.length; i++) {
                if (randAccessFiles[i] == null) {
                    randAccessFiles[i] = file; // Store the opened file in the array
                    return i; // Return the index of the opened file
                }
            }
            // Return -1 if the array is full (no available slots)
            return -1;
        } catch (FileNotFoundException e) {
            // Handle the case where the file could not be opened
            throw new RuntimeException(e);
        }
    }

    @Override
    public void Close(int id) {
        // Check if the file at the provided index is not null (i.e., it has been opened)
        if (randAccessFiles[id] != null) {
            try {
                // Close the file to release resources
                randAccessFiles[id].close();
                randAccessFiles[id] = null; // Set the slot to null after closing
                System.out.println("File closed"); // Log message to indicate the file is closed
            } catch (IOException e) {
                // Handle exceptions that may occur during closing the file
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public byte[] Read(int id, int size) {
        // Create a byte array to hold the data read from the file
        byte[] data = new byte[size];

        // Check if the file at the provided index is not null (i.e., it has been opened)
        if (randAccessFiles[id] != null) {
            try {
                // Read data from the file into the byte array
                randAccessFiles[id].read(data);
            } catch (IOException e) {
                // Handle exceptions that may occur during reading the file
                throw new RuntimeException(e);
            }
        }
        // Return the data read from the file
        return data;
    }

    @Override
    public int Write(int id, byte[] data) {
        // Check if the file at the provided index is not null (i.e., it has been opened)
        if (randAccessFiles[id] != null) {
            try {
                // Write the byte array data into the file
                System.out.println("Writing into a file");
                randAccessFiles[id].write(data);
            } catch (IOException e) {
                // Handle exceptions that may occur during writing the file
                throw new RuntimeException(e);
            }
        }
        // Return the length of the data written
        return data.length;
    }

    @Override
    public void Seek(int id, int to) {
        // Check if the file at the provided index is not null (i.e., it has been opened)
        if (randAccessFiles[id] != null) {
            try {
                // Move the file pointer to the specified position
                System.out.println("Seeking in File Device");
                randAccessFiles[id].seek(to);
            } catch (IOException e) {
                // Handle exceptions that may occur during seeking the file
                throw new RuntimeException(e);
            }
        }
    }
}
