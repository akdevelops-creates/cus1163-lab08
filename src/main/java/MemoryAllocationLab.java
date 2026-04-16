import java.io.*;
import java.util.*;

public class MemoryAllocationLab {

    static class MemoryBlock {
        int start;
        int size;
        String processName;  // null if free

        public MemoryBlock(int start, int size, String processName) {
            this.start = start;
            this.size = size;
            this.processName = processName;
        }

        public boolean isFree() {
            return processName == null;
        }

        public int getEnd() {
            return start + size - 1;
        }
    }

    static int totalMemory;
    static ArrayList<MemoryBlock> memory;
    static int successfulAllocations = 0;
    static int failedAllocations = 0;

    /**
     * STEPS: 1, 2: Process memory requests from file
     * <p>
     * This method reads the input file and processes each REQUEST and RELEASE.
     * <p>
     * STEP 1: Read and parse the file
     *   - Open the file using BufferedReader
     *   - Read the first line to get total memory size
     *   - Initialize the memory list with one large free block
     *   - Read each subsequent line and parse it
     *   - Call appropriate method based on REQUEST or RELEASE
     * <p>
     * STEP 2: Implement allocation and deallocation
     *   - For REQUEST: implement First-Fit algorithm
     *     * Search memory list for first free block >= requested size
     *     * If found: split the block if necessary and mark as allocated
     *     * If not found: increment failedAllocations
     *   - For RELEASE: find the process's block and mark it as free
     *   - Optionally: merge adjacent free blocks (bonus)
     */
     
     /* MY IRL Memory Allocation Analogy: 
      * I arrive at Watson HQ on a rainy day with my cohort of 15 other fellows, 
      * and we (the processes) need to put our umbrellas away in the storage room (Memory). 
      *
      * First-Fit approach: I (the Computer) volunteer to take everyone's umbrellas and put them in the first bin I see that's big enough to hold them all.
      *  - Pros: speed. it's super fast because I don't have to spend hours searching every corner in the storage room (memory) for space to put the umbrellas away.
      * 
      *  - Cons: messiness/fragmentation. If a felllow only needs a small bin for an umbrella but uses a big bucket, the computer ends up with gaps/"swiss cheese holes" (fragmentation) that other fellows could've used for their other (potentially bigger) supplies (i.e,  a bookbag, shoes, etc,).
      *
      **/
     
    public static void processRequests(String filename) {
        memory = new ArrayList<>();

        // 1. I set up Watson HQ's storage room and read my cohort's requests
        // 2. I use a try-catch block to read the file(s)
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            // 3. Finding out how much total space the storage room has today
            int totalMemory = Integer.parseInt(reader.readLine().trim());
            System.out.println("Reading from: " + filename);
            System.out.println("Total Memory: " + totalMemory + " KB");
            System.out.println("----------------------------------------");
            System.out.println("Processing cohort requests..");

            // 4. I start with one, big empty room: new MemoryBlock(0, totalMemory, null)
            memory.add(new MemoryBlock(0, totalMemory, null));

            // 5. Now, I loop through my list of fellows and their umbrellas, one by one
            String line;
            while ((line = reader.readLine()) != null) {
                // 6. Check if a fellow is entering (REQUEST) or leaving (RELEASE) the room
                String[] parts = line.split(" ");
                if (parts[0].equals("REQUEST")) {
                    allocate(parts[1], Integer.parseInt(parts[2]));
                } else if (parts[0].equals("RELEASE")) {
                    deallocate(parts[1]);
                }
            }
        } catch (IOException e) {
            System.out.println("Attention Watson Fellow, there is an error reading the file" + e.getMessage());
        }
    }

    /** HELPER METHOD 1
     *  methodPurpose: Helping a fellow find the first available bin that fits their supplies.
     */
    private static void allocate(String processName, int size) {
        // 1. Scan the storage room, front to back
        for (int i = 0; i < memory.size(); i++) {
            MemoryBlock block = memory.get(i);
            // 2. Find the first empty bin that's big enough to hold this fellow's supplies
            if (block.isFree() && block.size >= size) {
                // 3. Once found, mark this bin as "Taken" by that fellow
                block.processName = processName;

                // 4. If the bin is bigger than needed, split it to avoid "swiss cheese" holes in memory
                if (block.size > size) {
                    // 5. calculate the remaining space for other fellows' to store their bags, shoes, etc.
                    int remainingSize = block.size - size;
                    block.size = size; // 6. Shrink the taken space to precisely fit the umbrella
                    MemoryBlock leftover = new MemoryBlock(block.start + size, remainingSize, null);
                    // 7. Put that leftover empty space back into my list
                    memory.add(i + 1, leftover);
                }

                // Another successful umbrella put away for my cohort!
                successfulAllocations++;
                System.out.println("REQUEST " + processName + " " + size + " KB → SUCCESS");
                return;
            }
        }
        // If I can't find a bin big enough, the request fails for now
        failedAllocations++;
        System.out.println("REQUEST " + processName + " " + size + " KB → FAILED (Watson HQ Storage Room is too fragmented)");
    }

    /** HELPER METHOD 2
     *  methodPurpose: Moving an umbrella out so another fellow can use the bin.
     */
    private static void deallocate(String processName) {
        for (MemoryBlock block : memory) {
            // 1. Look for the bin with the fellow's name on it
            if (!block.isFree() && block.processName.equals(processName)) {
                block.processName = null; // 2. Make it FREE for the next person to use
                System.out.println("RELEASE " + processName + " → SUCCESS");
                return;
            }
        }
        System.out.println("RELEASE " + processName + " → FAILED (Fellow not found)");
    }

    public static void displayStatistics() {
        System.out.println("\n========================================");
        System.out.println("Final Memory State");
        System.out.println("========================================");

        int blockNum = 1;
        for (MemoryBlock block : memory) {
            String status = block.isFree() ? "FREE" : block.processName;
            String allocated = block.isFree() ? "" : " - ALLOCATED";
            System.out.printf("Block %d: [%d-%d]%s%s (%d KB)%s\n",
                    blockNum++,
                    block.start,
                    block.getEnd(),
                    " ".repeat(Math.max(1, 10 - String.valueOf(block.getEnd()).length())),
                    status,
                    block.size,
                    allocated);
        }

        System.out.println("\n========================================");
        System.out.println("Memory Statistics");
        System.out.println("========================================");

        int allocatedMem = 0;
        int freeMem = 0;
        int numProcesses = 0;
        int numFreeBlocks = 0;
        int largestFree = 0;

        for (MemoryBlock block : memory) {
            if (block.isFree()) {
                freeMem += block.size;
                numFreeBlocks++;
                largestFree = Math.max(largestFree, block.size);
            } else {
                allocatedMem += block.size;
                numProcesses++;
            }
        }

        double allocatedPercent = (allocatedMem * 100.0) / totalMemory;
        double freePercent = (freeMem * 100.0) / totalMemory;
        double fragmentation = freeMem > 0 ?
                ((freeMem - largestFree) * 100.0) / freeMem : 0;

        System.out.printf("Total Memory:           %d KB\n", totalMemory);
        System.out.printf("Allocated Memory:       %d KB (%.2f%%)\n", allocatedMem, allocatedPercent);
        System.out.printf("Free Memory:            %d KB (%.2f%%)\n", freeMem, freePercent);
        System.out.printf("Number of Processes:    %d\n", numProcesses);
        System.out.printf("Number of Free Blocks:  %d\n", numFreeBlocks);
        System.out.printf("Largest Free Block:     %d KB\n", largestFree);
        System.out.printf("External Fragmentation: %.2f%%\n", fragmentation);

        System.out.println("\nSuccessful Allocations: " + successfulAllocations);
        System.out.println("Failed Allocations:     " + failedAllocations);
        System.out.println("========================================");
    }

    /**
     * Main method (FULLY PROVIDED)
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java MemoryAllocationLab <input_file>");
            System.out.println("Example: java MemoryAllocationLab memory_requests.txt");
            return;
        }

        System.out.println("========================================");
        System.out.println("Memory Allocation Simulator (First-Fit)");
        System.out.println("========================================\n");
        System.out.println("Reading from: " + args[0]);

        processRequests(args[0]);
        displayStatistics();
    }
}
