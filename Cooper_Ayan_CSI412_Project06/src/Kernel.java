// Simulates process management
class Kernel extends Process implements Device {

    // Instance of the Scheduler class
    private Scheduler scheduler;

    VFS vfs; // Reference to the VFS

    public boolean[] inUse; // Array of boolean to track if pages are in use or not

    public Kernel() {
        super();
        // Initialize the scheduler class
        scheduler = new Scheduler();
        // Initialize the VFS
        vfs = new VFS();
        // Initialize the inUse array to a size of 1000 and set every entry as not in use (false)
        inUse = new boolean[1000];
        for (int i = 0; i < inUse.length; i++) {
            inUse[i] = false;
        }
    }

    @Override
    public void main() throws InterruptedException {
        // Run method
        while (true) {
            switch (OS.currentCall) {
                // Call the method to handle creating a process
                case CREATE_PROCESS:
                    // Cast the first member to UserlandProcess from the params list
                    UserlandProcess userProcess = (UserlandProcess) OS.params.getFirst();
                    // Cast the second member to Priority from the params list
                    OS.Priority priority = (OS.Priority) OS.params.get(1);
                    // Set the pid to CreateProcess, essentially making a PCB
                    int pid = scheduler.CreateProcess(userProcess, priority);
                    // Save the process ID as the return value
                    OS.returnValue = pid;
                    break;
                case SWITCH_PROCESS:
                    // Call the method to switch the current process
                    scheduler.SwitchProcess();
                    break;
                case SLEEP:
                    // Call the sleep method on the scheduler with the sleep duration passed in OS.params
                    int sleepDuration = (int) OS.params.getFirst();
                    scheduler.Sleep(sleepDuration);
                    break;
                case EXIT:
                    // Call the method to get the current process to exit
                    scheduler.Exit();
                    break;
                case OPEN:
                    // Cast the first member to String from the params list
                    String s = (String) OS.params.getFirst();
                    // Set an int to calling the Open method
                    int openValue = Open(s);
                    // Store the result of Open
                    OS.returnValue = openValue;
                    break;
                case CLOSE:
                    // Cast the first member to int from the params list
                    int closeId = (int) OS.params.getFirst();
                    // Call the Close method
                    Close(closeId);
                    break;
                case READ:
                    // Cast the first member to int from the params list
                    int readId = (int) OS.params.getFirst();
                    // Cast the second member to int from the params list
                    int readSize = (int) OS.params.get(1);
                    // Set a byte[] to calling the Read method
                    byte[] readValue = Read(readId, readSize);
                    // Store the result of Read
                    OS.returnValue = readValue;
                    break;
                case WRITE:
                    // Cast the first member to int from the params list
                    int writeId = (int) OS.params.getFirst();
                    // Cast the second member to byte[] from the params list
                    byte[] writeData = (byte[]) OS.params.get(1);
                    // Set an int to calling the Write method
                    int writeValue = Write(writeId, writeData);
                    // Store the result of Write
                    OS.returnValue = writeValue;
                    break;
                case SEEK:
                    // Cast the first member to int from the params list
                    int seekId = (int) OS.params.getFirst();
                    // Cast the second member to int from the params list
                    int seekTo = (int) OS.params.get(1);
                    // Call the Seek method
                    Seek(seekId, seekTo);
                    break;
                case GET_PID:
                    // Set an int to calling the method to get a process' PID
                    int processPid = scheduler.GetPid();
                    // Store the result of GetPid
                    OS.returnValue = processPid;
                    break;
                case GET_PID_BY_NAME:
                    // Cast the first member to String from the params list
                    String name = (String) OS.params.getFirst();
                    // Set an int to calling the method to get a process' PID by its name
                    int processName = scheduler.GetPidByName(name);
                    // Store the result of GetPidByName
                    OS.returnValue = processName;
                    break;
                case SEND_MESSAGE:
                    // Cast the first member to KernelMessage from the params list
                    KernelMessage message = (KernelMessage) OS.params.getFirst();
                    // Call the method to send messages
                    scheduler.SendMessage(message);
                    break;
                case WAIT_FOR_MESSAGE:
                    // Set a KernelMessage to calling the method to wait for messages
                    KernelMessage wait = scheduler.WaitForMessage();
                    // Store the result of WaitForMessage
                    OS.returnValue = wait;
                    break;
                case GET_MAPPING:
                    // Cast the first member to int from the params list
                    int virtualPageNumber = (int) OS.params.getFirst();
                    // Call the method to get virtual to physical address mapping
                    scheduler.GetMapping(virtualPageNumber);
                    break;
                case ALLOCATE_MEMORY:
                    // Cast the first member to int from the params list
                    int allocateSize = (int) OS.params.getFirst();
                    // Set an int to calling the method of allocating memory
                    int allocateMemory = scheduler.AllocateMemory(allocateSize);
                    // Store the result of AllocateMemory
                    OS.returnValue = allocateMemory;
                    break;
                case FREE_MEMORY:
                    // Cast the first and second member to int from the params list
                    int freePointer = (int) OS.params.getFirst();
                    int freeSize = (int) OS.params.get(1);
                    // Call the method of freeing memory
                    boolean freeMemory = scheduler.FreeMemory(freePointer, freeSize);
                    // Store the result of FreeMemory
                    OS.returnValue = freeMemory;
                    break;
                default:
                    break;
            }

            // Give the process time to properly perform its methods
            Thread.sleep(10);
            // Call start() on the next process to run
            if (scheduler.currentlyRunning != null) {
                scheduler.currentlyRunning.start();
            }

            // Call stop on itself
            try {
                this.stop();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Accessor for the currentlyRunning PCB
    public PCB getCurrentlyRunning() {
        return scheduler.currentlyRunning;
    }

    // Accessor for nextPageSwap from OS
    public int getNextPageSwap() {
        return OS.getNextPageSwap();
    }

    @Override
    public int Open(String s) throws InterruptedException {
        // Get the data array for checking available slots
        int[] data = scheduler.getCurrentlyRunning().ints;

        // Check if the provided device name is valid (not null or empty)
        if (s != null && !s.isEmpty()) {
            // Iterate through the process data array to find an available slot
            for (int i = 0; i < data.length; i++) {
                // If the slot is available (indicated by -1), attempt to open the device
                if (data[i] == -1) {
                    int vfsOpen = vfs.Open(s); // Call the Open method from VFS
                    // If the device was opened successfully, update the data array and return the index
                    if (vfsOpen != -1) {
                        data[i] = vfsOpen; // Store the VFS id in the process data array
                        return i; // Return the index of the opened device
                    }
                }
            }
            // Return -1 if no available slots were found
            return -1;
        }
        // Return -1 if the device name was invalid
        return -1;
    }

    @Override
    public void Close(int id) {
        // Retrieve the VFS id from the process data array for the specified index
        int vfsId = scheduler.getCurrentlyRunning().ints[id];
        // Close the device in the VFS if the id is valid
        vfs.Close(vfsId);
        // Set the corresponding slot in the process data array to -1 to indicate it is now available
        scheduler.getCurrentlyRunning().ints[id] = -1;
    }

    @Override
    public byte[] Read(int id, int size) {
        // Retrieve the VFS id from the process data array for the specified index
        int vfsId = scheduler.getCurrentlyRunning().ints[id];
        byte[] vfsRead = null;
        // If the id is valid, read data from the VFS
        if (vfsId != -1) {
            vfsRead = vfs.Read(vfsId, size); // Call the read method of VFS
        }
        // Return the data read from the VFS
        return vfsRead;
    }

    @Override
    public int Write(int id, byte[] data) {
        // Retrieve the VFS id from the process data array for the specified index
        int vfsId = scheduler.getCurrentlyRunning().ints[id];
        int vfsWrite = -1;
        // If the id is valid, write data to the VFS
        if (vfsId != -1) {
            vfsWrite = vfs.Write(vfsId, data); // Call the Write method of VFS
        }
        // Return the number of bytes written to the VFS
        return vfsWrite;
    }

    @Override
    public void Seek(int id, int to) {
        // Retrieve the VFS id from the process data array for the specified index
        int vfsId = scheduler.getCurrentlyRunning().ints[id];
        // If the id is valid, seek to the specified position in the VFS
        if (vfsId != -1) {
            vfs.Seek(vfsId, to); // Call the Seek method of VFS
        }
        else {
            System.out.println("Invalid vfsId"); // Print a message if the id is invalid
        }
    }
}
