import java.util.LinkedList;

// Process Control Block (PCB). Used to schedule processes by having queues of PCBs.
// It manages processes from the Kernel's perspective and is not visible in Userland
public class PCB {

    public UserlandProcess queue; // Reference to UserlandProcess
    // Used to holds a process ID
    public static int nextpid;
    public int pid;

    public OS.Priority priority; // Stores the priority of a process
    public int timeoutCount; // Keeps track of how many times a process has timed out
    public final int MAX_TIMEOUTS = 5; // Process can time out a max of 5 times
    public long wakeUpTime; // Keeps track of when a PCB should be waked up

    public int[] ints; // Array of ints to store device information

    public String name; // Holds the name of a process' PID
    public LinkedList<KernelMessage> messages; // A queue for KernelMessages

    public VirtualToPhysicalMapping[] virtualToPhysicalMapping; // Array of VTPM used for virtual to physical address mapping
    public int allocateMemorySize; // Track the allocated memory size for each process
    public int startAddress; // Track the start address for each process

    public PCB(UserlandProcess up, OS.Priority priority) {
        queue = up; // Initialize queue to UserlandProcess of PCB
        // Set the pid to nextpid and increment it
        this.pid = nextpid;
        nextpid++;
        this.priority = priority; // Initialize the priority
        timeoutCount = 0; // Initialize the time-out count to 0
        wakeUpTime = 0; // Initialize the wakeUpTime to 0

        // Initialize the ints array with a size of 10 and set each entry to empty (-1)
        ints = new int[10];
        for (int i = 0; i < ints.length; i++) {
            ints[i] = -1;
        }

        this.name = up.getClass().getSimpleName(); // Initialize the name of the process
        this.messages = new LinkedList<>(); // Initialize the message queue

        // Initialize the VTPM array with a size of 100.
        virtualToPhysicalMapping = new VirtualToPhysicalMapping[100];

        allocateMemorySize = 0; // Initialize the allocated memory size to 0
        startAddress = -1; // Initialize the start address to -1
    }

    // Calls UserlandProcess' stop(). Loops with Thread.sleep() until ULP.isStopped is true
    public void stop() throws InterruptedException {
        queue.stop();
        while (queue.isStopped() != true) {
            Thread.sleep(50);
        }
    }

    // Calls UserlandProcess' isDone
    public boolean isDone() {
        return queue.isDone();
    }

    // Calls UserlandProcess' start
    public void start() {
        queue.start();
    }

    // Increment the timeoutCount
    public void incrementTimeout() {
        timeoutCount++;
    }

    // Check if the process has timed out too many times
    public boolean shouldBeDemoted() {
        return timeoutCount > MAX_TIMEOUTS;
    }

    // Demote the process' priority
    public void demote() {
        switch (priority) {
            case REAL_TIME:
                // Demote to interactive
                priority = OS.Priority.INTERACTIVE;
                System.out.println(queue.toString() + " DEMOTED TO INTERACTIVE");
                break;
            case INTERACTIVE:
                // Demote to background
                priority = OS.Priority.BACKGROUND;
                System.out.println(queue.toString() + " DEMOTED TO BACKGROUND");
                break;
            default:
                // No demotion if it's already BACKGROUND
                break;
        }
    }

    // Used to see each process and its priority
    public String toString() {
        return queue.toString() + " " + priority;
    }
}
