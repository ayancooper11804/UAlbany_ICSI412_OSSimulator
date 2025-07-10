import java.time.Clock;
import java.util.*;

// Manage and control the execution of multiple Userland processes
public class Scheduler {

    // Queues of each process priority (Real time, interactive, and background) that the scheduler knows about
    private LinkedList<PCB> realTimeProcesses;
    private LinkedList<PCB> interactiveProcesses;
    private LinkedList<PCB> backgroundProcesses;
    private LinkedList<PCB> sleepingProcesses; // The list of processes that are sleeping

    private Timer timer; // Timer to periodically interrupt the running process
    public PCB currentlyRunning; // Reference to the PCB that holds currently running process and priority

    private Clock clock; // Used to get a clock with millisecond accuracy

    private Random random; // Used to figure out what queue to get the next process

    private HashMap<Integer, PCB> targetMap; // Queue to hold the PCB of a target
    private HashMap<Integer, PCB> waitMap; // Queue to hold the PCB of a waiting message

    public Scheduler() {
        // Initialize priority queues and sleeping queue
        realTimeProcesses =  new LinkedList<>();
        interactiveProcesses =  new LinkedList<>();
        backgroundProcesses =  new LinkedList<>();
        sleepingProcesses = new LinkedList<>();

        // Initialize the timer
        timer = new Timer();
        // Set currentlyRunning to the first PCB in the list (null at the time)
        currentlyRunning = null;

        // Initialize clock to default time zone
        clock = Clock.systemDefaultZone();
        // Initialize the random variable
        random = new Random();

        targetMap = new HashMap<>(); // Initialize the target queue
        waitMap = new HashMap<>(); // Initialize the waiting queue

        // Schedule the interrupt for every 250ms
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                // Call requestStop on the currently running process if there is one (not null)
                if (currentlyRunning != null) {
                    currentlyRunning.queue.requestStop();
                    // Increment the timeout count for the running process
                    currentlyRunning.incrementTimeout();
                }
            }
        }, 0, 250); // This is the quantum
    }

    // Accessor for currentlyRunning
    public PCB getCurrentlyRunning() {
        return currentlyRunning;
    }

    // Add the userland process and priority it to the correct queue and, if nothing else is running,
    // call switchProcess() to get it started.
    public int CreateProcess(UserlandProcess up, OS.Priority priority) {
        // Create a new PCB with the UserlandProcess
        PCB pcb = new PCB(up, priority);
        // Populate the targetMap with the new PCB
        targetMap.put(pcb.pid, pcb);
//        System.out.println("Target map: " + targetMap);
//        System.out.println(pcb.name + " " + pcb.pid);
        // Add the new process to the appropriate queue depending on its priority
        switch (pcb.priority) {
            case REAL_TIME:
                realTimeProcesses.add(pcb);
                break;
            case INTERACTIVE:
                interactiveProcesses.add(pcb);
                break;
            case BACKGROUND:
                backgroundProcesses.add(pcb);
                break;
        }

        // If no process is currently running, switch to the new one
        if (currentlyRunning == null) {
            SwitchProcess();
        }
        // Return process ID in PCB
        return pcb.pid;
    }

    // Default version of CreateProcess that sets the priority to Interactive
    public int CreateProcess(UserlandProcess up) {
        return CreateProcess(up, OS.Priority.INTERACTIVE);
    }

    // Take the currently running PCB and put it at the end of the correct list.
    // Then take the head of the list and run it
    public void SwitchProcess() {
        // Check if there is a currently running process and it has completed its execution
        if (currentlyRunning != null && currentlyRunning.isDone()) {
            System.out.println("I'm DONE");
            Kernel kernel = OS.getKernel(); // Retrieve the Kernel instance from OS
            // Iterate through the currently running process' ints array
            for (int i = 0; i < 10; i++) {
                // Close the device associated with the current index in the ints array
                kernel.Close(currentlyRunning.ints[i]);
            }
        }

        // If there is a current process and it's not done
        else if (currentlyRunning != null && !currentlyRunning.isDone()) {
            // Move the current process to the end of the correct queue
            switch (currentlyRunning.priority) {
                case REAL_TIME:
                    realTimeProcesses.addLast(currentlyRunning);
                    break;
                case INTERACTIVE:
                    interactiveProcesses.addLast(currentlyRunning);
                    break;
                case BACKGROUND:
                    backgroundProcesses.addLast(currentlyRunning);
                    break;
            }
            // Remove the process from the targetMap
            targetMap.remove(currentlyRunning.pid);
            // Free a process's memory
            FreeMemory(currentlyRunning.startAddress, currentlyRunning.allocateMemorySize);
            // Clear the TLB
            Hardware.clearTLB();
        }

        // Poll the first process in the correct priority queue to be the next process to run
        currentlyRunning = selectNextProcess();

        // Check if the process should be demoted based on its timeout and priority
        // NOTE: If an error occurs that says "Cannot invoke 'PCB.shouldBeDemoted()' because 'this.currentlyRunning' is null,"
        // please try again. This may occur once in a while, but otherwise the code works
        if (currentlyRunning.shouldBeDemoted() &&
                (currentlyRunning.priority == OS.Priority.REAL_TIME ||
                        currentlyRunning.priority == OS.Priority.INTERACTIVE)) {
            currentlyRunning.demote();
        }

        // Check if any processes are sleeping and wake them up
        checkSleeping();

        // If no process is left to run, peek the first process in the correct list
        if (currentlyRunning == null) {
            if (!realTimeProcesses.isEmpty()) {
                currentlyRunning = realTimeProcesses.peek();
            }
            else if (!interactiveProcesses.isEmpty()) {
                currentlyRunning = interactiveProcesses.peek();
            }
            else if (!backgroundProcesses.isEmpty()) {
                currentlyRunning = backgroundProcesses.peek();
            }
        }
    }

    // Checks the queues in order of priority with a probabilistic model, returning the next process to run
    private PCB selectNextProcess() {
        // Generates random numbers from 0.0 to 1.0
        double rand = random.nextInt(Integer.MAX_VALUE) / (double) Integer.MAX_VALUE;
        // Check the real time queue first
        if (!realTimeProcesses.isEmpty()) {
            // Real time process runs with 60% probability
            if (rand <= 0.6) {
                return realTimeProcesses.poll(); // Return and remove the first PCB
            }
            // Interactive process runs with 30% probability
            else if ((rand >= 0.6 && rand <= 0.9) && !interactiveProcesses.isEmpty()) {
                return interactiveProcesses.poll(); // Return and remove the first PCB
            }
            // Background process runs with 10% probability
            else {
                // If the background priority queue happens to be empty, poll from the next queue
                if (backgroundProcesses.isEmpty()) {
                    if (!realTimeProcesses.isEmpty()) {
                        return realTimeProcesses.poll();
                    }
                    else {
                        return interactiveProcesses.poll();
                    }
                }
                return backgroundProcesses.poll(); // Return and remove the first PCB
            }
        }
        // Check the interactive queue next
        else if (!interactiveProcesses.isEmpty()) {
            // Interactive process runs with 75% probability
            if (rand < 0.75) {
                return interactiveProcesses.poll(); // Return and remove the first PCB
            }
            // Background process runs with 25% probability
            else {
                // If the background priority queue happens to be empty, poll from the next queue
                if (backgroundProcesses.isEmpty()) {
                    if (!realTimeProcesses.isEmpty()) {
                        return realTimeProcesses.poll();
                    }
                    else {
                        return interactiveProcesses.poll();
                    }
                }
                return backgroundProcesses.poll(); // Return and remove the first PCB
            }
        }
        // The background queue is last, simply run it
        else {
            // If the background priority queue happens to be empty, poll from the next queue
            if (backgroundProcesses.isEmpty()) {
                if (!realTimeProcesses.isEmpty()) {
                    return realTimeProcesses.poll();
                }
                else {
                    return interactiveProcesses.poll();
                }
            }
            return backgroundProcesses.poll(); // Return and remove the first PCB
        }
    }

    // Check if any processes in the sleeping queue should wake up
    private void checkSleeping() {
        long currentTime = clock.millis();
        // Iterate through the sleeping processes
        for (PCB process : sleepingProcesses) {
            // If the wake-up time for the process has passed
            if (currentTime >= process.wakeUpTime) {
                // Add the PCB back to the correct queue based on its priority
                if (process.priority == OS.Priority.REAL_TIME) {
                    realTimeProcesses.addLast(process);
                }
                if (process.priority == OS.Priority.INTERACTIVE) {
                    interactiveProcesses.addLast(process);
                }
                if (process.priority == OS.Priority.BACKGROUND) {
                    backgroundProcesses.addLast(process);
                }
                // Remove the PCB from the sleeping queue
                sleepingProcesses.remove(process);
            }
        }
    }

    // Called when a process wants to sleep
    public void Sleep(int millis) {
        // Calculate the wake-up time by adding the requested time to the current clock value
        currentlyRunning.wakeUpTime = clock.millis() + millis;
        // Move the currently running process to the sleeping queue
        sleepingProcesses.add(currentlyRunning);
        currentlyRunning = null;
        // Switch to another process since the current one is sleeping
        SwitchProcess();
    }

    // Unscheduled the current process so that it never gets to run again
    public void Exit() {
        // Ensure that another process is chosen to run
        PCB pcb = currentlyRunning;
        SwitchProcess();
        // Remove the PCB from priority queues to end it
        realTimeProcesses.remove(pcb);
        interactiveProcesses.remove(pcb);
        backgroundProcesses.remove(pcb);
        // Remove the PCB from the targetMap
        targetMap.remove(pcb.pid);
        // Free all of a process' memory
        if (pcb.allocateMemorySize > 0) {
            FreeMemory(pcb.startAddress, pcb.allocateMemorySize);
        }

        // Access the Kernel's inUse array
        Kernel kernel = OS.getKernel();
        boolean[] inUse = kernel.inUse;

        // Release all physical pages and swap file blocks used by the process
        for (VirtualToPhysicalMapping mapping : pcb.virtualToPhysicalMapping) {
            if (mapping == null) {
                continue; // Skip if no mapping exists
            }

            // Free physical memory if it is allocated
            if (mapping.physicalPageNumber >= 0) {
                inUse[mapping.physicalPageNumber] = false; // Mark physical page as free
                mapping.physicalPageNumber = -1; // Reset the mapping
            }

            // Free swap file block if it is allocated
            if (mapping.diskPageNumber >= 0) {
                inUse[mapping.diskPageNumber] = false; // Mark swap block as free
                mapping.diskPageNumber = -1; // Reset the mapping
            }
        }
    }

    // Return the current process' PID
    public int GetPid() {
        return currentlyRunning.pid;
    }

    // Returns the PID of a process with that name
    public int GetPidByName(String name) {
        // Check RealTime processes
        for (PCB process : realTimeProcesses) {
            if (process.name.equalsIgnoreCase(name)) {
                // Return the process' pid if the names match
                return process.pid;
            }
        }
        // Check Interactive processes
        for (PCB process : interactiveProcesses) {
            if (process.name.equalsIgnoreCase(name)) {
                // Return the process' pid if the names match
                return process.pid;
            }
        }
        // Check Background processes
        for (PCB process : backgroundProcesses) {
            if (process.name.equalsIgnoreCase(name)) {
                // Return the process' pid if the names match
                return process.pid;
            }
        }

        // Just in case, if the target process happens to not be in a priority queue, we will check the target HashMap
        // Check for the target process in the target HashMap
        Object[] keys = targetMap.keySet().toArray();
        for (Object key : keys) {
            // Find the target process in the HashMap
            if (targetMap.containsKey((int) key)) {
                if (targetMap.get((int) key).name.equalsIgnoreCase(name)) {
                    // Return the process' pid if the names match
                    return targetMap.get((int) key).pid;
                }
            }
        }

        // Return -1 if no match is found
        return -1;
    }

    // Sends a message
    public void SendMessage(KernelMessage km) {
        KernelMessage message = new KernelMessage(km); // Make a copy of the original message
        message.senderPid = GetPid(); // Set the sender PID
        PCB targetProcess = targetMap.get(km.targetPid); // Get the target PCB
        if (targetProcess != null) {
            // Add the message to the targetProcess message queue
            targetProcess.messages.add(message);
            // Check if a process is waiting for a message
            if (waitMap.containsKey(targetProcess.pid)) {
                // Remove the process from the waiting queue
                waitMap.remove(targetProcess.pid);
                // Add the process to the appropriate queue based on its priority
                if (targetProcess.priority == OS.Priority.REAL_TIME) {
                    realTimeProcesses.addLast(targetProcess);
                }
                if (targetProcess.priority == OS.Priority.INTERACTIVE) {
                    interactiveProcesses.addLast(targetProcess);
                }
                if (targetProcess.priority == OS.Priority.BACKGROUND) {
                    backgroundProcesses.addLast(targetProcess);
                }
            }
        }
    }

    // Wait for a message
    public KernelMessage WaitForMessage() {
        if (!currentlyRunning.messages.isEmpty()) {
            // If the current process has a message, take it off the messages queue and return it
            KernelMessage message = currentlyRunning.messages.poll();
            return message;
        }
        else {
            // De-schedule the process
            waitMap.put(currentlyRunning.pid, currentlyRunning); // Add the currently running process to the wait queue
            SwitchProcess(); // Switch to another process since the current one is waiting
            return null; // Indicate that the current process is waiting
        }
    }

    // Retrieve a random process from the system that has a valid physical page
    public PCB getRandomProcess() {
        // Combine all processes into one list for simplicity
        List<PCB> allProcesses = new LinkedList<>();
        allProcesses.addAll(realTimeProcesses); // Add real-time processes
        allProcesses.addAll(interactiveProcesses); // Add interactive processes
        allProcesses.addAll(backgroundProcesses); // Add background processes

        // Ensure there are processes to select from
        if (allProcesses.isEmpty()) {
            return null; // Return null if there are no processes
        }

        while (true) {
            // Select a random process from the combined list
            int index = random.nextInt(allProcesses.size());
            PCB selectedProcess = allProcesses.get(index);

            // Look for a page with a valid physical mapping
            for (VirtualToPhysicalMapping mapping : selectedProcess.virtualToPhysicalMapping) {
                if (mapping != null && mapping.physicalPageNumber != -1) {
                    return selectedProcess; // Return process with a valid physical page
                }
            }
        }
    }

    // Update (randomly) one of the two TLB entries
    public void GetMapping(int virtualPageNumber) {
        // Retrieve the mapping array (virtual-to-physical page mappings) for the currently running process
        VirtualToPhysicalMapping[] newMapping = currentlyRunning.virtualToPhysicalMapping;
        // Get the current page's mapping for the specified virtual page number
        VirtualToPhysicalMapping pageMapping = newMapping[virtualPageNumber];

        // If no mapping exists, initialize a new one
        if (pageMapping == null) {
            pageMapping = new VirtualToPhysicalMapping();
            newMapping[virtualPageNumber] = pageMapping;
        }

        // If the page does not have a valid physical page assigned
        if (pageMapping.physicalPageNumber == -1) {
            // Access the Kernel's inUse array
            Kernel kernel = OS.getKernel();
            boolean[] inUse = kernel.inUse;

            // Try to find a free physical page
            int freePhysicalPage = -1;
            for (int i = 0; i < inUse.length; i++) {
                if (!inUse[i]) {
                    freePhysicalPage = i;
                    inUse[i] = true; // Mark the physical page as in use
                    break;
                }
            }

            // If no free physical page is found, perform a page swap
            if (freePhysicalPage == -1) {
                // No free physical pages, perform a page swap
                PCB victimProcess = getRandomProcess(); // Select a random process to swap out
                if (victimProcess == null) throw new RuntimeException("No victim process found for page swap"); // Error if no victim process is found

                // Find a victim page to swap out from the selected process
                VirtualToPhysicalMapping victimMapping = null;
                for (VirtualToPhysicalMapping mapping : victimProcess.virtualToPhysicalMapping) {
                    if (mapping != null && mapping.physicalPageNumber != -1) {
                        victimMapping = mapping; // Select the first valid victim mapping
                        break;
                    }
                }

                // If no valid victim page is found, throw an error
                if (victimMapping == null) throw new RuntimeException("No victim page found for swapping");

                // Write the victim's page to disk if necessary
                if (victimMapping.diskPageNumber == -1) {
                    victimMapping.diskPageNumber = kernel.getNextPageSwap();
                }

                // Calculate the start address of the victim's physical page in memory
                int victimPhysicalStart = victimMapping.physicalPageNumber * 1024;
                // Copy the victim's page data from physical memory into a byte array
                byte[] pageData = Arrays.copyOfRange(Hardware.memory, victimPhysicalStart, victimPhysicalStart + 1024);
                int bytesWritten = kernel.Write(victimMapping.diskPageNumber, pageData); // Write victim page data to disk

                // Ensure the page data is successfully written to disk
                if (bytesWritten != 1024) {
                    throw new RuntimeException("Failed to write victim page to swap file");
                }

                // Reuse the victim's physical page for the current page
                freePhysicalPage = victimMapping.physicalPageNumber;
                victimMapping.physicalPageNumber = -1; // Mark the victim's physical page as unused
            }

            // Assign the newly found or swapped physical page to the current page
            pageMapping.physicalPageNumber = freePhysicalPage;

            // If the page had previously been written to disk, load the data from the disk
            if (pageMapping.diskPageNumber != -1) {
                byte[] pageData = kernel.Read(pageMapping.diskPageNumber, 1024); // Read the victim page from swap
                if (pageData != null && pageData.length == 1024) {
                    System.arraycopy(pageData, 0, Hardware.memory, freePhysicalPage * 1024, 1024); // Load the page into physical memory
                }
                else {
                    throw new RuntimeException("Failed to read page from swap file");
                }
            }
            else {
                // If the page wasn't written to disk, initialize the physical page with zeros
                int physicalStart = freePhysicalPage * 1024;
                Arrays.fill(Hardware.memory, physicalStart, physicalStart + 1024, (byte) 0);
            }
        }

        // Retrieve the TLB from the Hardware class and randomly update one of the two TLB entries with the valid mapping
        int[][] tlb = Hardware.TLB;
        int randomRow = random.nextInt(2); // Randomly select row 1 or row 2
        // Update the TLB entry
        tlb[randomRow][0] = virtualPageNumber;
        tlb[randomRow][1] = pageMapping.physicalPageNumber;
    }

    // Allocate memory, returns the start virtual address
    public int AllocateMemory(int size) {
        int pagesNeeded = size / 1024; // Calculate the number of pages needed
        int falseEntries = 0; // Count of consecutive free pages
        int startIndex = -1; // Track the start of a free block

        // Access the Kernel's inUse array
        Kernel kernel = OS.getKernel();
        boolean[] inUse = kernel.inUse;

        // Search for free pages in the current process's VTPM array
        for (int i = 0; i < currentlyRunning.virtualToPhysicalMapping.length; i++) {
            // Check for free pages
            if (currentlyRunning.virtualToPhysicalMapping[i] == null) {
                // Set the start index of the free block
                if (falseEntries == 0) {
                    startIndex = i;
                    System.out.println("Start index: " + startIndex);
                }
                // Increment the count of free pages
                falseEntries++;

                // Check if we have found enough free pages
                if (falseEntries == pagesNeeded) {
                    System.out.println("Free pages found: " + falseEntries);
                    // Mark pages as allocated by updating the VTPM
                    for (int j = startIndex; j < startIndex + pagesNeeded; j++) {
                        currentlyRunning.virtualToPhysicalMapping[j] = new VirtualToPhysicalMapping(); // Allocate pages
                    }

                    // Update PCb fields
                    currentlyRunning.startAddress = startIndex * 1024; // Set start address
                    currentlyRunning.allocateMemorySize = size; // Set allocated memory size

                    // Return currentlyRunning's start address
                    return currentlyRunning.startAddress;
                }
            }
            else {
                // Reset if a used page interrupts the free block
                falseEntries = 0;
                startIndex = -1;
            }
        }
        // Return -1 if not enough pages are found
        return -1;
    }

    // Free memory, takes the virtual address and the amount to free
    public boolean FreeMemory(int pointer, int size) {
        // Calculate the start and end indices
        int startIndex = pointer / 1024;
        int endIndex = startIndex + (size / 1024);

        // Access the Kernel's inUse array
        Kernel kernel = OS.getKernel();
        boolean[] inUse = kernel.inUse;

        // Check for bounds to avoid IndexOutOfBoundsException
        if (startIndex < 0 || endIndex > currentlyRunning.virtualToPhysicalMapping.length) return false; // Return false if out of bounds

        boolean freedAnyMemory = false; // Track if any memory was freed

        // Search for any pages in use in the current process's VTPM
        for (int i = startIndex; i < endIndex; i++) {
            VirtualToPhysicalMapping mapping = currentlyRunning.virtualToPhysicalMapping[i];
            if (mapping != null) {
                // If the page is in use, free it by resetting its physical and disk page mappings
                if (mapping.physicalPageNumber != -1) {
                    inUse[mapping.physicalPageNumber] = false; // Mark physical page as free
                    mapping.physicalPageNumber = -1; // Clear the physical page mapping
                }
                if (mapping.diskPageNumber != -1) {
                    inUse[mapping.diskPageNumber] = false; // Mark the disk page as free
                    mapping.diskPageNumber = -1; // Clear the disk page mapping
                }
                // Set the VTPM entry to null, effectively freeing the page
                currentlyRunning.virtualToPhysicalMapping[i] = null;
                freedAnyMemory = true; // Mark that memory is freed
            }
        }

        // If any memory was freed, reset the currentlyRunning's memory tracking fields
        if (freedAnyMemory) {
            currentlyRunning.allocateMemorySize = 0; // Reset allocated size
            currentlyRunning.startAddress = -1; // Reset start address
        }

        // Return true if any memory was freed, otherwise false
        return freedAnyMemory;
    }
}
