
package manufacturingsystem;
import java.util.*;

// ─────────────────────────────────────────────
//  ProductionJob – data model
// ─────────────────────────────────────────────
class ProductionJob {
    String jobId;
    String customerName;
    String productType;
    int    quantity;
    String urgencyLevel;   // Normal | Rush | Emergency
    String currentStage;
    int    cuttingTime;
    int    assemblyTime;
    int    qualityCheckTime;
    int    packagingTime;
    String status;

    ProductionJob(String jobId, String customerName, String productType,
                  int quantity, String urgencyLevel,
                  int cuttingTime, int assemblyTime,
                  int qualityCheckTime, int packagingTime) {
        this.jobId            = jobId;
        this.customerName     = customerName;
        this.productType      = productType;
        this.quantity         = quantity;
        this.urgencyLevel     = urgencyLevel;
        this.currentStage     = "Cutting";
        this.cuttingTime      = cuttingTime;
        this.assemblyTime     = assemblyTime;
        this.qualityCheckTime = qualityCheckTime;
        this.packagingTime    = packagingTime;
        this.status           = "Pending";
    }

    /** Remaining production time based on current stage. */
    int remainingTime() {
        return switch (currentStage) {
            case "Cutting"       -> cuttingTime + assemblyTime + qualityCheckTime + packagingTime;
            case "Assembly"      -> assemblyTime + qualityCheckTime + packagingTime;
            case "Quality Check" -> qualityCheckTime + packagingTime;
            case "Packaging"     -> packagingTime;
            default              -> 0;
        };
    }

    void print() {
        System.out.println("  Job ID             : " + jobId);
        System.out.println("  Customer Name      : " + customerName);
        System.out.println("  Product Type       : " + productType);
        System.out.println("  Quantity           : " + quantity);
        System.out.println("  Urgency Level      : " + urgencyLevel);
        System.out.println("  Current Stage      : " + currentStage);
        System.out.println("  Est. Cutting Time  : " + cuttingTime  + " min");
        System.out.println("  Est. Assembly Time : " + assemblyTime + " min");
        System.out.println("  Est. QC Time       : " + qualityCheckTime + " min");
        System.out.println("  Est. Packaging Time: " + packagingTime    + " min");
        System.out.println("  Status             : " + status);
    }

    /** One-line summary used in queue listings. */
    String summary() {
        return jobId + " - " + productType + " - " + urgencyLevel;
    }
}

// ─────────────────────────────────────────────
//  Main system
// ─────────────────────────────────────────────
public class ManufacturingSystem {

    private static final Queue<ProductionJob>   cuttingQueue      = new LinkedList<>();
    private static final Queue<ProductionJob>   assemblyQueue     = new LinkedList<>();
    private static final Queue<ProductionJob>   qualityCheckQueue = new LinkedList<>();
    private static final Queue<ProductionJob>   packagingQueue    = new LinkedList<>();
    private static final ArrayList<ProductionJob> completedJobs   = new ArrayList<>();

    private static final Scanner sc = new Scanner(System.in);

    // ── Helpers ──────────────────────────────
    private static void line()  { System.out.println("─".repeat(55)); }
    private static void blank() { System.out.println(); }

    private static String prompt(String label) {
        System.out.print("  " + label + ": ");
        return sc.nextLine().trim();
    }

    private static int promptInt(String label) {
        while (true) {
            try {
                return Integer.parseInt(prompt(label));
            } catch (NumberFormatException e) {
                System.out.println("  [!] Please enter a valid integer.");
            }
        }
    }

    private static String promptChoice(String label, String... choices) {
        while (true) {
            String v = prompt(label).toLowerCase();
            for (String c : choices) {
                if (c.toLowerCase().equals(v)) return c;
            }
            System.out.println("  [!] Invalid input. Accepted: " + Arrays.toString(choices));
        }
    }

    // ─────────────────────────────────────────
    //  1. Add new production job
    // ─────────────────────────────────────────
    private static void addJob() {
        line();
        System.out.println("  ADD NEW PRODUCTION JOB");
        line();

        String jobId = prompt("Job ID");
        // Duplicate check across all collections
        if (findJob(jobId) != null) {
            System.out.println("  [!] A job with ID '" + jobId + "' already exists.");
            return;
        }

        String customerName     = prompt("Customer Name");
        String productType      = prompt("Product Type");
        int    quantity         = promptInt("Quantity");
        String urgencyLevel     = promptChoice("Urgency Level (Normal/Rush/Emergency)",
                                               "Normal", "Rush", "Emergency");
        int    cuttingTime      = promptInt("Est. Cutting Time (min)");
        int    assemblyTime     = promptInt("Est. Assembly Time (min)");
        int    qualityCheckTime = promptInt("Est. Quality Check Time (min)");
        int    packagingTime    = promptInt("Est. Packaging Time (min)");

        ProductionJob job = new ProductionJob(jobId, customerName, productType,
                quantity, urgencyLevel,
                cuttingTime, assemblyTime, qualityCheckTime, packagingTime);

        cuttingQueue.add(job);
        blank();
        System.out.println("  [✔] Job " + jobId + " added to Cutting Queue.");
    }

    // ─────────────────────────────────────────
    //  2. Process Cutting Queue
    // ─────────────────────────────────────────
    private static void processCutting() {
        line();
        System.out.println("  PROCESS NEXT JOB – CUTTING QUEUE");
        line();
        if (cuttingQueue.isEmpty()) {
            System.out.println("  No jobs waiting in the Cutting Queue.");
            return;
        }
        ProductionJob job = cuttingQueue.poll();
        job.currentStage = "Assembly";
        job.status       = "Waiting for Assembly";
        assemblyQueue.add(job);
        System.out.println("  [✔] Job " + job.jobId + " moved: Cutting → Assembly.");
    }

    // ─────────────────────────────────────────
    //  3. Process Assembly Queue
    // ─────────────────────────────────────────
    private static void processAssembly() {
        line();
        System.out.println("  PROCESS NEXT JOB – ASSEMBLY QUEUE");
        line();
        if (assemblyQueue.isEmpty()) {
            System.out.println("  No jobs waiting in the Assembly Queue.");
            return;
        }
        ProductionJob job = assemblyQueue.poll();
        job.currentStage = "Quality Check";
        job.status       = "Waiting for Quality Check";
        qualityCheckQueue.add(job);
        System.out.println("  [✔] Job " + job.jobId + " moved: Assembly → Quality Check.");
    }

    // ─────────────────────────────────────────
    //  4. Process Quality Check Queue
    // ─────────────────────────────────────────
    private static void processQualityCheck() {
        line();
        System.out.println("  PROCESS NEXT JOB – QUALITY CHECK QUEUE");
        line();
        if (qualityCheckQueue.isEmpty()) {
            System.out.println("  No jobs waiting in the Quality Check Queue.");
            return;
        }
        ProductionJob job = qualityCheckQueue.poll();
        System.out.println("  Processing job: " + job.summary());
        String result = promptChoice("  Did job " + job.jobId + " PASS quality check? (yes/no)", "yes", "no");

        if (result.equals("yes")) {
            job.currentStage = "Packaging";
            job.status       = "Waiting for Packaging";
            packagingQueue.add(job);
            System.out.println("  [✔] Job " + job.jobId + " PASSED → moved to Packaging Queue.");
        } else {
            job.currentStage = "Assembly";
            job.status       = "For Rework";
            assemblyQueue.add(job);
            System.out.println("  [✗] Job " + job.jobId + " FAILED → sent back to Assembly Queue for rework.");
        }
    }

    // ─────────────────────────────────────────
    //  5. Process Packaging Queue
    // ─────────────────────────────────────────
    private static void processPackaging() {
        line();
        System.out.println("  PROCESS NEXT JOB – PACKAGING QUEUE");
        line();
        if (packagingQueue.isEmpty()) {
            System.out.println("  No jobs waiting in the Packaging Queue.");
            return;
        }
        ProductionJob job = packagingQueue.poll();
        job.currentStage = "Completed";
        job.status       = "Completed";
        completedJobs.add(job);
        System.out.println("  [✔] Job " + job.jobId + " completed → moved to Completed Jobs list.");
    }

    // ─────────────────────────────────────────
    //  6. View all active queues
    // ─────────────────────────────────────────
    private static void viewQueues() {
        line();
        System.out.println("  ALL ACTIVE QUEUES");
        line();

        printQueue("Cutting Queue",      cuttingQueue);
        printQueue("Assembly Queue",     assemblyQueue);
        printQueue("Quality Check Queue",qualityCheckQueue);
        printQueue("Packaging Queue",    packagingQueue);
    }

    private static void printQueue(String name, Queue<ProductionJob> q) {
        System.out.println("  " + name + ":");
        if (q.isEmpty()) {
            System.out.println("    No jobs waiting.");
        } else {
            for (ProductionJob j : q) {
                System.out.println("    " + j.summary());
            }
        }
        blank();
    }

    // ─────────────────────────────────────────
    //  7. View completed jobs
    // ─────────────────────────────────────────
    private static void viewCompleted() {
        line();
        System.out.println("  COMPLETED JOBS");
        line();
        if (completedJobs.isEmpty()) {
            System.out.println("  No completed jobs yet.");
            return;
        }
        for (int i = 0; i < completedJobs.size(); i++) {
            System.out.println("  [" + (i + 1) + "]");
            completedJobs.get(i).print();
            blank();
        }
    }

    // ─────────────────────────────────────────
    //  8. Search by Job ID
    // ─────────────────────────────────────────
    private static ProductionJob findJob(String id) {
        for (ProductionJob j : cuttingQueue)      if (j.jobId.equalsIgnoreCase(id)) return j;
        for (ProductionJob j : assemblyQueue)     if (j.jobId.equalsIgnoreCase(id)) return j;
        for (ProductionJob j : qualityCheckQueue) if (j.jobId.equalsIgnoreCase(id)) return j;
        for (ProductionJob j : packagingQueue)    if (j.jobId.equalsIgnoreCase(id)) return j;
        for (ProductionJob j : completedJobs)     if (j.jobId.equalsIgnoreCase(id)) return j;
        return null;
    }

    private static void searchJob() {
        line();
        System.out.println("  SEARCH JOB BY ID");
        line();
        String id  = prompt("Enter Job ID");
        ProductionJob job = findJob(id);
        if (job == null) {
            System.out.println("  [!] Job '" + id + "' not found in any queue or completed list.");
            return;
        }
        String location = job.currentStage.equals("Completed")
                ? "Completed Jobs"
                : job.currentStage + " Queue";
        System.out.println("  Job found in: " + location);
        blank();
        job.print();
    }

    // ─────────────────────────────────────────
    //  9. Production summary
    // ─────────────────────────────────────────
    private static void productionSummary() {
        line();
        System.out.println("  PRODUCTION SUMMARY");
        line();

        int totalQtyCompleted   = 0;
        int totalQtyInProd      = 0;
        int rushPending         = 0;
        int emergencyPending    = 0;
        int totalRemainingTime  = 0;

        // Completed stats
        for (ProductionJob j : completedJobs) {
            totalQtyCompleted += j.quantity;
        }

        // Active queue stats
        List<Queue<ProductionJob>> active = List.of(
                cuttingQueue, assemblyQueue, qualityCheckQueue, packagingQueue);
        for (Queue<ProductionJob> q : active) {
            for (ProductionJob j : q) {
                totalQtyInProd     += j.quantity;
                totalRemainingTime += j.remainingTime();
                if (j.urgencyLevel.equals("Rush"))      rushPending++;
                if (j.urgencyLevel.equals("Emergency")) emergencyPending++;
            }
        }

        System.out.printf("  %-40s : %d%n", "Jobs in Cutting Queue",       cuttingQueue.size());
        System.out.printf("  %-40s : %d%n", "Jobs in Assembly Queue",       assemblyQueue.size());
        System.out.printf("  %-40s : %d%n", "Jobs in Quality Check Queue",  qualityCheckQueue.size());
        System.out.printf("  %-40s : %d%n", "Jobs in Packaging Queue",      packagingQueue.size());
        System.out.printf("  %-40s : %d%n", "Completed Jobs",               completedJobs.size());
        System.out.printf("  %-40s : %d%n", "Total Quantity Completed",     totalQtyCompleted);
        System.out.printf("  %-40s : %d%n", "Total Quantity In Production", totalQtyInProd);
        System.out.printf("  %-40s : %d%n", "Rush Jobs Still Pending",      rushPending);
        System.out.printf("  %-40s : %d%n", "Emergency Jobs Still Pending", emergencyPending);
        System.out.printf("  %-40s : %d min%n","Total Est. Remaining Time",  totalRemainingTime);
    }

    // ─────────────────────────────────────────
    //  Menu
    // ─────────────────────────────────────────
    private static void showMenu() {
        blank();
        line();
        System.out.println("  MULTI-STAGE MANUFACTURING JOB QUEUE SYSTEM");
        line();
        System.out.println("  [1]  Add new production job");
        System.out.println("  [2]  Process next job in Cutting Queue");
        System.out.println("  [3]  Process next job in Assembly Queue");
        System.out.println("  [4]  Process next job in Quality Check Queue");
        System.out.println("  [5]  Process next job in Packaging Queue");
        System.out.println("  [6]  View all active queues");
        System.out.println("  [7]  View completed jobs");
        System.out.println("  [8]  Search job by Job ID");
        System.out.println("  [9]  Generate production summary");
        System.out.println("  [10] Exit");
        line();
        System.out.print("  Enter choice: ");
    }

    // ─────────────────────────────────────────
    //  Entry point
    // ─────────────────────────────────────────
    public static void main(String[] args) {
        while (true) {
            showMenu();
            String input = sc.nextLine().trim();
            blank();
            switch (input) {
                case "1"  -> addJob();
                case "2"  -> processCutting();
                case "3"  -> processAssembly();
                case "4"  -> processQualityCheck();
                case "5"  -> processPackaging();
                case "6"  -> viewQueues();
                case "7"  -> viewCompleted();
                case "8"  -> searchJob();
                case "9"  -> productionSummary();
                case "10" -> { System.out.println("  Exiting system. Goodbye!"); return; }
                default   -> System.out.println("  [!] Invalid choice. Please enter 1–10.");
            }
        }
    }
}
