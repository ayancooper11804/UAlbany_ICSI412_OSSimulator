import java.util.ArrayList;

// Gateway between the Userland and Kernel
public class OS {

    // One and only static instance of the Kernel
    private static Kernel kernel;

    // An enum of what function to call, including device calls
    public enum CallType {
        CREATE_PROCESS, SWITCH_PROCESS, SLEEP, EXIT,
        OPEN, CLOSE, READ, WRITE, SEEK,
        GET_PID, GET_PID_BY_NAME, SEND_MESSAGE, WAIT_FOR_MESSAGE,
        GET_MAPPING, ALLOCATE_MEMORY, FREE_MEMORY,
    }

    // An enum of the priority for a process (ordered from highest to lowest priority)
    public enum Priority {
        REAL_TIME, INTERACTIVE, BACKGROUND
    }

    // A static instance of the enum
    public static CallType currentCall;

    // A static instance of the priority enum
    public static Priority currentPriority;

    // A static ArrayList of parameters to the function; we don't know what they will be, so we will
    // make it an ArrayList of Object
    public static ArrayList<Object> params = new ArrayList<Object>();

    // The return value. In a similar way, we don't know what the return type will be, so
    // make it a static object
    public static Object returnValue;

    private static int swapFileDescriptor; // Track the descriptor of the file
    private static int nextPageSwap = 0; // Track the page number to write out to

    // Accessor for the one instance of Kernel
    public static Kernel getKernel() {
        return kernel;
    }

    // Accessor for incrementing nextPageSwap
    public static int getNextPageSwap() {
        return nextPageSwap++;
    }

    // Prepares the necessary data for the kernel to process a new Userland process and its priority
    public static int CreateProcess(UserlandProcess up, Priority priority) throws InterruptedException {
        // Reset the parameters
        params.clear();
        // Add the UserlandProcess to the parameters list
        params.add(up);
        // Set the priority
        currentPriority = priority;
        // Add the priority to the parameters list
        params.add(priority);
        // Set the currentCall
        currentCall = CallType.CREATE_PROCESS;
        // Switch to the Kernel by starting the Kernel
        kernel.start();
        // If there is no current PCB running, create a loop that calls Thread.sleep(10)
        // until the return value is set by the kernel. This will wait for init() to complete.
        if (kernel.getCurrentlyRunning() == null) {
            while (returnValue == null) {
                Thread.sleep(10);
            }
        }
        // If the scheduler has a currentlyRunning PCB, call stop() on it
        else {
            PCB current = kernel.getCurrentlyRunning();
            current.stop();
        }
        // Cast and return the returnValue
        return (int) returnValue;
    }

    // Default version of CreateProcess, setting the priority to Interactive
    public static int CreateProcess(UserlandProcess up) throws InterruptedException {
        return CreateProcess(up, Priority.INTERACTIVE);
    }

    // Creates the Kernel() and calls CreateProcess twice – once for “init” and
    // once for the idle process. As of assignment 6, we will now open files to handle virtual memory
    public static void Startup(UserlandProcess init) throws InterruptedException {
        kernel = new Kernel();
        CreateProcess(init);
        CreateProcess(new IdleProcess());
        // Open the swap file
        swapFileDescriptor = Open("file swapfile.dat");
        if (swapFileDescriptor == -1) {
            throw new RuntimeException("Failed to open swap file");
        }
    }

    // Prepares the necessary data for the kernel to switch processes
    public static void SwitchProcess() throws InterruptedException {
        // Reset the parameters
        params.clear();
        // Set the currentCall
        currentCall = CallType.SWITCH_PROCESS;
        // Switch to the Kernel by starting the Kernel
        kernel.start();
        // If the scheduler has a currentlyRunning, call stop() on it
        PCB current = kernel.getCurrentlyRunning();
        current.stop();
    }

    // Puts the process to sleep for a specified number of milliseconds.
    // Useful for applications that want to “wake up” every so often to do a task
    public static void Sleep(int millis) throws InterruptedException {
        // Reset the parameters
        params.clear();
        // Add the milliseconds to the parameters list
        params.add(millis);
        // Set the currentCall
        currentCall = CallType.SLEEP;
        // Switch to the Kernel by starting the Kernel
        kernel.start();
        // If the scheduler has a currentlyRunning, call stop() on it
        PCB current = kernel.getCurrentlyRunning();
        current.stop();
    }

    // Prepares the necessary data for the kernel to make a process exit
    public static void Exit() throws InterruptedException {
        // Reset the parameters
        params.clear();
        // Set the currentCall
        currentCall = CallType.EXIT;
        // Switch to the Kernel by starting the Kernel
        kernel.start();
        // If the scheduler has a currentlyRunning, call stop() on it
        PCB current = kernel.getCurrentlyRunning();
        current.stop();
    }

    // Prepares the necessary data to Open a device in Kernel
    public static int Open(String s) throws InterruptedException {
        // Reset the parameters
        params.clear();
        // Add the string to the parameters list
        params.add(s);
        // Set the currentCall
        currentCall = CallType.OPEN;
        // Switch to the Kernel by starting the Kernel
        kernel.start();
        // If the scheduler has a currentlyRunning, call stop() on it
        PCB current = kernel.getCurrentlyRunning();
        current.stop();
        // Cast and return the returnValue
        return (int) returnValue;
    }

    // Prepares the necessary data to Close a device in Kernel
    public static void Close(int id) throws InterruptedException {
        // Reset the parameters
        params.clear();
        // Add the id to the parameters list
        params.add(id);
        // Set the currentCall
        currentCall = CallType.CLOSE;
        // Switch to the Kernel by starting the Kernel
        kernel.start();
        // If the scheduler has a currentlyRunning, call stop() on it
        PCB current = kernel.getCurrentlyRunning();
        current.stop();
    }

    // Prepares the necessary data to Read a device in Kernel
    public static byte[] Read(int id,int size) throws InterruptedException {
        // Reset the parameters
        params.clear();
        // Add the id and size to the parameters list
        params.add(id);
        params.add(size);
        // Set the currentCall
        currentCall = CallType.READ;
        // Switch to the Kernel by starting the Kernel
        kernel.start();
        // If the scheduler has a currentlyRunning, call stop() on it
        PCB current = kernel.getCurrentlyRunning();
        current.stop();
        // Cast and return the returnValue
        if (returnValue instanceof byte[]) {
            return (byte[]) returnValue;
        }
        else {
            // Print an error if there was an issue casting the return value to byte[]
            System.err.println("Error: non-byte value returned");
            // Set the return value to null
            return null;
        }
    }

    // Prepares the necessary data to Write into a device in Kernel
    public static int Write(int id, byte[] data) throws InterruptedException {
        // Reset the parameters
        params.clear();
        // Add the id and data to the parameters list
        params.add(id);
        params.add(data);
        // Set the currentCall
        currentCall = CallType.WRITE;
        // Switch to the Kernel by starting the Kernel
        kernel.start();
        // If the scheduler has a currentlyRunning, call stop() on it
        PCB current = kernel.getCurrentlyRunning();
        current.stop();
        // Cast and return the returnValue
        return (int) returnValue;
    }

    // Prepares the necessary data to Seek in a device in Kernel
    public static void Seek(int id,int to) throws InterruptedException {
        // Reset the parameters
        params.clear();
        // Add the id and 'to' to the parameters list
        params.add(id);
        params.add(to);
        // Set the currentCall
        currentCall = CallType.SEEK;
        // Switch to the Kernel by starting the Kernel
        kernel.start();
        // If the scheduler has a currentlyRunning, call stop() on it
        PCB current = kernel.getCurrentlyRunning();
        current.stop();
    }

    // Prepares the necessary data to get a Process' PID
    public static int GetPid() throws InterruptedException {
        // Reset the parameters
        params.clear();
        // Set the currentCall
        currentCall = CallType.GET_PID;
        // Switch to the Kernel by starting the Kernel
        kernel.start();
        // If the scheduler has a currentlyRunning, call stop() on it
        PCB current = kernel.getCurrentlyRunning();
        current.stop();
        // Cast and return the returnValue
        return (int) returnValue;
    }

    // Prepares the necessary data to get a Process' PID by name
    public static int GetPidByName(String name) throws InterruptedException {
        // Reset the parameters
        params.clear();
        // Add the name to the parameters list
        params.add(name);
        // Set the currentCall
        currentCall = CallType.GET_PID_BY_NAME;
        // Switch to the Kernel by starting the Kernel
        kernel.start();
        // If the scheduler has a currentlyRunning, call stop() on it
        PCB current = kernel.getCurrentlyRunning();
        current.stop();
        // Cast and return the returnValue
        return (int) returnValue;
    }

    // Prepares the necessary data to send messages
    public static void SendMessage(KernelMessage km) throws InterruptedException {
        // Reset the parameters
        params.clear();
        // Add the KernelMessage to the parameters list
        params.add(km);
        // Set the currentCall
        currentCall = CallType.SEND_MESSAGE;
        // Switch to the Kernel by starting the Kernel
        kernel.start();
        // If the scheduler has a currentlyRunning, call stop() on it
        PCB current = kernel.getCurrentlyRunning();
        current.stop();
    }

    // Prepares the necessary data to wait for messages
    public static KernelMessage WaitForMessage() throws InterruptedException {
        // Reset the parameters
        params.clear();
        // Set the currentCall
        currentCall = CallType.WAIT_FOR_MESSAGE;
        // Switch to the Kernel by starting the Kernel
        kernel.start();
        // If the scheduler has a currentlyRunning, call stop() on it
        PCB current = kernel.getCurrentlyRunning();
        current.stop();
        // Cast and return the returnValue
        return (KernelMessage) returnValue;
    }

    // Prepares the necessary data for retrieving the mapping of addresses
    public static void GetMapping(int virtualPageNumber) throws InterruptedException {
        // Reset the parameters
        params.clear();
        // Add the virtualPageNumber to the parameters list
        params.add(virtualPageNumber);
        // Set the currentCall
        currentCall = CallType.GET_MAPPING;
        // Switch to the Kernel by starting the Kernel
        kernel.start();
        // If the scheduler has a currentlyRunning, call stop() on it
        PCB current = kernel.getCurrentlyRunning();
        current.stop();
    }

    // Prepares the necessary data to allocate memory
    public static int AllocateMemory(int size) throws InterruptedException {
        // If the size isn't a multiple of 1024, return failure (-1)
        if (size % 1024 != 0) {
            return -1;
        }
        // Reset the parameters
        params.clear();
        // Add the size to the parameters list
        params.add(size);
        // Set the currentCall
        currentCall = CallType.ALLOCATE_MEMORY;
        // Switch to the Kernel by starting the Kernel
        kernel.start();
        // If the scheduler has a currentlyRunning, call stop() on it
        PCB current = kernel.getCurrentlyRunning();
        current.stop();
        // Cast and return the returnValue
        return (int) returnValue;
    }

    // Prepares the necessary data to free memory
    public static boolean FreeMemory(int pointer, int size) throws InterruptedException {
        // If the size and pointers aren't multiples of 1024, return failure (false)
        if ((pointer % 1024 != 0) && (size % 1024 != 0)) {
            return false;
        }
        // Reset the parameters
        params.clear();
        // Add the pointer and size to the parameters list
        params.add(pointer);
        params.add(size);
        // Set the currentCall
        currentCall = CallType.FREE_MEMORY;
        // Switch to the Kernel by starting the Kernel
        kernel.start();
        // If the scheduler has a currentlyRunning, call stop() on it
        PCB current = kernel.getCurrentlyRunning();
        current.stop();
        // Cast and return the returnValue
        return (boolean) returnValue;
    }

}
