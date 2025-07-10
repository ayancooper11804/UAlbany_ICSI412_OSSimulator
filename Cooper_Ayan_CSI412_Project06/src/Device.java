// Interface for devices since they're complex
public interface Device {
    // Opens a device. Takes a string for extensibility
    int Open(String s) throws InterruptedException;
    // Closes a device
    void Close(int id);
    // Reads the data from the device
    byte[] Read(int id,int size);
    // Writes data into the device
    int Write(int id, byte[] data);
    // Move pointer
    void Seek(int id,int to);
}
