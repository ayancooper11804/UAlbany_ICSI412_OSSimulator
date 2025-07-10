// Class to track information for virtual memmory
public class VirtualToPhysicalMapping {

    public int physicalPageNumber; // Track the physical page number
    public int diskPageNumber; // Track the on disk page number

    // Initialize the physical and disk page numbers to -1
    public VirtualToPhysicalMapping() {
        this.physicalPageNumber = -1;
        this.diskPageNumber = -1;
    }

}
