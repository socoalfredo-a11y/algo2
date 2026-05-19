
package machinedepreciation;

import java.util.Scanner;

public class Machinedepreciation {

    // ── Machine fields ────────────────────────────────────────────────────────
    private static String machineName;
    private static double initialValue;
    private static double depreciationRate;       // e.g. 0.10 for 10 %
    private static double initialMaintenanceCost;
    private static double maintenanceIncreaseRate; // e.g. 0.08 for 8 %
    private static int    yearsToForecast;
    private static double minAcceptableValue;
    private static double maxAcceptableMaintenance;
    private static boolean dataEntered = false;

    private static final Scanner sc = new Scanner(System.in);

    // ── Formatting helpers ────────────────────────────────────────────────────
    private static void line()  { System.out.println("─".repeat(58)); }
    private static void blank() { System.out.println(); }

    private static String prompt(String label) {
        System.out.print("  " + label + ": ");
        return sc.nextLine().trim();
    }

    private static double promptDouble(String label) {
        while (true) {
            try { return Double.parseDouble(prompt(label)); }
            catch (NumberFormatException e) {
                System.out.println("  [!] Please enter a valid number.");
            }
        }
    }

    private static int promptInt(String label) {
        while (true) {
            try { return Integer.parseInt(prompt(label)); }
            catch (NumberFormatException e) {
                System.out.println("  [!] Please enter a valid integer.");
            }
        }
    }

    // ── Guard ─────────────────────────────────────────────────────────────────
    private static boolean checkData() {
        if (!dataEntered) {
            System.out.println("  [!] Please enter machine details first (option 1).");
            return false;
        }
        return true;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  RECURSIVE METHODS
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Recursively computes machine value after N years.
     * Base case : years == 0  → return currentValue
     * Recursive : value × (1 − rate), then call with years − 1
     */
    static double computeMachineValue(double currentValue, double rate, int years) {
        if (years == 0) return currentValue;
        return computeMachineValue(currentValue * (1 - rate), rate, years - 1);
    }

    /**
     * Recursively computes maintenance cost in year N.
     * Year 1 is the initial cost; each subsequent year multiplies by (1 + rate).
     * Base case : year == 1  → return currentCost
     * Recursive : call with cost × (1 + rate), year − 1
     */
    static double computeMaintenanceCostInYear(double currentCost, double rate, int year) {
        if (year == 1) return currentCost;
        return computeMaintenanceCostInYear(currentCost * (1 + rate), rate, year - 1);
    }

    /**
     * Recursively computes TOTAL maintenance cost over N years.
     * Base case : years == 0  → return 0
     * Recursive : cost in year N  +  total of years 1 … N-1
     *
     * We pass the INITIAL cost and accumulate forwards:
     *   totalMaintenance(init, rate, years) = init + init*(1+r) + … (years terms)
     */
    static double computeTotalMaintenance(double currentCost, double rate, int years) {
        if (years == 0) return 0;
        return currentCost + computeTotalMaintenance(currentCost * (1 + rate), rate, years - 1);
    }

    /**
     * Recursively displays the yearly forecast.
     * Base case : year > totalYears
     * Recursive : print year, then call with year + 1
     */
    static void displayYearlyForecast(int year, int totalYears,
                                      double value,   double depRate,
                                      double mCost,   double mRate) {
        if (year > totalYears) return;

        double newValue = value * (1 - depRate);
        System.out.printf("  Year %d%n", year);
        System.out.printf("    Machine Value    : %,.2f%n", newValue);
        System.out.printf("    Maintenance Cost : %,.2f%n", mCost);
        blank();

        displayYearlyForecast(year + 1, totalYears,
                newValue, depRate,
                mCost * (1 + mRate), mRate);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  MENU ACTIONS
    // ═════════════════════════════════════════════════════════════════════════

    // 1. Enter machine details
    private static void enterDetails() {
        line();
        System.out.println("  ENTER MACHINE DETAILS");
        line();
        machineName              = prompt("Machine Name");
        initialValue             = promptDouble("Initial Machine Value");
        depreciationRate         = promptDouble("Annual Depreciation Rate (e.g. 10 for 10%)") / 100.0;
        initialMaintenanceCost   = promptDouble("Initial Maintenance Cost");
        maintenanceIncreaseRate  = promptDouble("Annual Maintenance Increase Rate (e.g. 8 for 8%)") / 100.0;
        yearsToForecast          = promptInt("Number of Years to Forecast");
        minAcceptableValue       = promptDouble("Minimum Acceptable Machine Value");
        maxAcceptableMaintenance = promptDouble("Maximum Acceptable Maintenance Cost");
        dataEntered = true;
        blank();
        System.out.println("  [✔] Machine details saved.");
    }

    // 2. Machine value after N years
    private static void computeValueMenu() {
        if (!checkData()) return;
        double value = computeMachineValue(initialValue, depreciationRate, yearsToForecast);
        blank();
        System.out.printf("  Machine Value after %d year(s): %,.2f%n", yearsToForecast, value);
    }

    // 3. Total depreciation
    private static void computeDepreciation() {
        if (!checkData()) return;
        double value       = computeMachineValue(initialValue, depreciationRate, yearsToForecast);
        double depreciation = initialValue - value;
        blank();
        System.out.printf("  Initial Value           : %,.2f%n", initialValue);
        System.out.printf("  Value After %d Year(s)  : %,.2f%n", yearsToForecast, value);
        System.out.printf("  Total Depreciation      : %,.2f%n", depreciation);
    }

    // 4. Maintenance cost in year N
    private static void computeMaintenanceCostMenu() {
        if (!checkData()) return;
        double cost = computeMaintenanceCostInYear(initialMaintenanceCost,
                maintenanceIncreaseRate, yearsToForecast);
        blank();
        System.out.printf("  Maintenance Cost in Year %d: %,.2f%n", yearsToForecast, cost);
    }

    // 5. Total maintenance cost after N years
    private static void computeTotalMaintenanceMenu() {
        if (!checkData()) return;
        double total = computeTotalMaintenance(initialMaintenanceCost,
                maintenanceIncreaseRate, yearsToForecast);
        blank();
        System.out.printf("  Total Maintenance Cost after %d Year(s): %,.2f%n",
                yearsToForecast, total);
    }

    // 6. Replacement recommendation
    private static void checkReplacement() {
        if (!checkData()) return;
        double finalValue = computeMachineValue(initialValue, depreciationRate, yearsToForecast);
        double finalMaint = computeMaintenanceCostInYear(initialMaintenanceCost,
                maintenanceIncreaseRate, yearsToForecast);
        blank();
        System.out.printf("  Machine Value after %d Year(s)  : %,.2f%n", yearsToForecast, finalValue);
        System.out.printf("  Minimum Acceptable Value         : %,.2f%n", minAcceptableValue);
        System.out.printf("  Maintenance Cost in Year %d      : %,.2f%n", yearsToForecast, finalMaint);
        System.out.printf("  Maximum Acceptable Maintenance   : %,.2f%n", maxAcceptableMaintenance);
        blank();

        boolean replace = finalValue < minAcceptableValue || finalMaint > maxAcceptableMaintenance;
        if (replace) {
            System.out.println("  ⚠  RECOMMENDATION: REPLACE the machine.");
            if (finalValue < minAcceptableValue)
                System.out.println("     Reason: Machine value dropped below minimum acceptable value.");
            if (finalMaint > maxAcceptableMaintenance)
                System.out.println("     Reason: Maintenance cost exceeded maximum acceptable cost.");
        } else {
            System.out.println("  ✔  RECOMMENDATION: KEEP the machine. It is still within acceptable limits.");
        }
    }

    // 7. Yearly forecast (recursive display)
    private static void displayForecast() {
        if (!checkData()) return;
        line();
        System.out.println("  YEARLY FORECAST — " + machineName);
        line();
        displayYearlyForecast(1, yearsToForecast,
                initialValue, depreciationRate,
                initialMaintenanceCost, maintenanceIncreaseRate);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Menu loop
    // ─────────────────────────────────────────────────────────────────────────
    private static void showMenu() {
        blank();
        line();
        System.out.println("  MACHINE DEPRECIATION & MAINTENANCE CALCULATOR");
        line();
        System.out.println("  [1] Enter machine details");
        System.out.println("  [2] Compute machine value after N years");
        System.out.println("  [3] Compute total depreciation after N years");
        System.out.println("  [4] Compute maintenance cost in year N");
        System.out.println("  [5] Compute total maintenance cost after N years");
        System.out.println("  [6] Check replacement recommendation");
        System.out.println("  [7] Display yearly forecast");
        System.out.println("  [8] Exit");
        line();
        System.out.print("  Enter choice: ");
    }

    public static void main(String[] args) {
        while (true) {
            showMenu();
            String input = sc.nextLine().trim();
            blank();
            switch (input) {
                case "1" -> enterDetails();
                case "2" -> computeValueMenu();
                case "3" -> computeDepreciation();
                case "4" -> computeMaintenanceCostMenu();
                case "5" -> computeTotalMaintenanceMenu();
                case "6" -> checkReplacement();
                case "7" -> displayForecast();
                case "8" -> { System.out.println("  Exiting. Goodbye!"); return; }
                default  -> System.out.println("  [!] Invalid choice. Enter 1–8.");
            }
        }
    }
}