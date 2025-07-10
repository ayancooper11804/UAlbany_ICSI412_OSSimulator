// Class that creates and holds information about messages that will be passed
public class KernelMessage {

    public int senderPid; // PID for sending
    public int targetPid; // PID for the target message
    public int purpose; // Indicates "what" this message is; sender uses it ti indicate what it's "for"
    public byte[] data; // Used to store data, will be whatever the application wants it to be

    // Constructor that makes KernelMessage flexible for setting specific values
    public KernelMessage(int senderPid, int targetPid, int purpose, byte[] data) {
        // Initialize all variables
        this.senderPid = senderPid;
        this.targetPid = targetPid;
        this.purpose = purpose;
        if (data != null) {
            this.data = new byte[data.length]; // Create a new array of the same size
            // Copy the contents of the original array
            System.arraycopy(data, 0, this.data, 0, data.length);
        }
        else {
            this.data = null; // Handle case where data is null
        }
    }

    // Deep copy constructor
    public KernelMessage(KernelMessage originalMessage) {
        // Initialize all variables from the original message
        this.senderPid = originalMessage.senderPid;
        this.targetPid = originalMessage.targetPid;
        this.purpose = originalMessage.purpose;
        if (originalMessage.data != null) {
            this.data = new byte[originalMessage.data.length]; // Create a new array of the same size
            // Copy the contents of the original array
            System.arraycopy(originalMessage.data, 0, this.data, 0, originalMessage.data.length);
        }
        else {
            this.data = null; // Handle case where data is null
        }
    }

    // toString method - useful for debugging
    public String toString() {
        // Convert the byte array to a string representation
        String dataString = (data != null) ? new String(data) : "null";

        // Message that outputs all KernelMessage fields
        return "KernelMessage { " +
                "Sender PID: " + senderPid +
                ", Target PID: " + targetPid +
                ", Purpse: " + purpose +
                ", Data: " + data +
                " }";
    }

}
