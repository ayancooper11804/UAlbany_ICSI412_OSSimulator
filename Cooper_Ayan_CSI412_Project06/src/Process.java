import java.util.concurrent.Semaphore;

// Base class for Userland, Kernel, and every test program
abstract class Process implements Runnable {

    Thread runProgram; // Allows user program to run
    public Semaphore sem; // Stops the program when we need to cooperate
    public boolean quantum = false; // Indicates that our quantum is expired

    // Initializes Thread and Semaphore; starts the Thread
    public Process() {
        runProgram = new Thread(this);
        sem = new Semaphore(0);
        runProgram.start();
    }

    // Represents the "main" of our program
    abstract void main() throws InterruptedException;

    // Sets the boolean (quantum) indicating that this process' quantum has expired
    public void requestStop() {
        quantum = true;
    }

    // Indicates if the semaphore (sem) is 0. If so, return true. Otherwise, return false
    public boolean isStopped() {
        if (sem.availablePermits() == 0) {
            return true;
        }
        return false;
    }

    // Returns true when the Java thread (runProgram) is not alive, otherwise return false
    public boolean isDone() {
        if (!runProgram.isAlive()) {
            return true;
        }
        return false;
    }

    // Releases (increments) the semaphore, allowing this thread to run
    public void start() {
        sem.release();
    }

    // Acquires (decrements) the semaphore, stopping this thread from running
    public void stop() throws InterruptedException {
        sem.acquire();
    }

    // Acquire the semaphore, then call main
    public void run() {
        try {
            sem.acquire();
            main();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // If the boolean (quantum) is true, set the boolean to false and call OS.switchProcess()
    public void cooperate() throws InterruptedException {
        if (quantum == true) {
            quantum = false;
            OS.SwitchProcess();
        }
    }

}
