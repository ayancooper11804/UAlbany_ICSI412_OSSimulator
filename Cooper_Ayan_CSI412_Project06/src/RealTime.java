// Test program that infinitely prints "Top Priority" to test RealTime priority
public class RealTime extends UserlandProcess{

    public void main() throws InterruptedException {


        while (true) {
            System.out.println("Top priority");

            // Open a random and file device
            int openRandom = OS.Open("random 100");
            int openFile = OS.Open("file data.dat");
            System.out.println(openRandom);
            System.out.println(openFile);

            // Read into the random device
            byte[] dataRandom = OS.Read(openRandom, 50);
            // Print an error if there was an issue with reading from the random device
            if (dataRandom == null) {
                System.out.println("Read failed, no data returned");
            }
            // Print out the bytes read into the random device
            else {
                StringBuilder sbRandom = new StringBuilder();
                for (byte b : dataRandom) {
                    sbRandom.append(String.format("%02X ", b));
                }
                System.out.println(sbRandom);
            }

            // Read into the file device
            byte[] dataFile = OS.Read(openFile, 50);
            // Print an error if there was an issue with reading from the file device
            if (dataFile == null) {
                System.out.println("Read failed, no data returned");
            }
            // Print out the bytes read into the file device
            else {
                StringBuilder sbFile = new StringBuilder();
                for (byte b : dataFile) {
                    sbFile.append(String.format("%02X ", b));
                }
                System.out.println(sbFile);
            }

            // Write bytes into the file device
            byte[] writeFile = "Hello World".getBytes();
            OS.Write(openFile, writeFile);

            // Seek through the random and file devices
            OS.Seek(openRandom, 20);
            OS.Seek(openFile, 20);

            // Close the devices
            OS.Close(openRandom);
            OS.Close(openFile);

            cooperate();
            try {
                Thread.sleep(50); // sleep for 50 ms
            } catch (Exception e) { }
        }
    }

    // Used to understand process is RealTime
    public String toString () {
        return "RealTime";
    }

}
