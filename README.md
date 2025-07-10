# OS Simulator, Fall 2024
This project was developed as part of my *Operating Systems* course. The goal was to simulate the core behavior of a modern multitasking operating system, including memory management, process scheduling, message passing, and file system support. This simulator helped reinforce fundamental OS concepts through hands-on implementation and modular design.
## Project Overview
This OS simulator replicates essential components of a real-world operating system by implementing:
* Scheduler: Manages process execution across multiple queues, handling process states, sleep timers, and scheduling algorithms for prioritization.
* Virtual Memory Paging: Simulates memory allocation with a page table and a TLB (Translation Lookaside Buffer), incorporating disk swap logic to manage memory-efficiently under constraints.
* Message Passing: Implements inter-process communication via a kernel-level message system, enabling asynchronous send/receive operations.
* File System Management: Supports basic file handling with a virtual file system (VFS) layer and simulated devices such as a fake disk, offering open, read, write, and close operations.
## Testing
The system was tested through a combination of unit tests and simulated userland processes. Test scenarios included:
* Memory allocation and deallocation patterns with fragmentation avoidance
* Multi-process communication via message queues
* Sleep and wake functionality tied to clock ticks
* File reads/writes using multiple devices under the VFS
The modular design allowed for isolated component testing and clear debugging.
## Notes
* This project was built for educational purposes, with an emphasis on learning OS internals through simulation rather than raw performance or OS deployment.
* The simulator uses a class-based architecture, with distinct layers for kernel space and user space logic.
* Features like paging and TLB replacement policies were simplified to aid clarity, though they reflect real OS concepts.
