import models.Customer;
import models.Interaction;
import utils.DataStore;

public class CRMSystemTest {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        run("addCustomerStoresCustomer", CRMSystemTest::addCustomerStoresCustomer);
        run("searchCustomersFindsByNameEmailOrCompany", CRMSystemTest::searchCustomersFindsByNameEmailOrCompany);
        run("filterByStatusReturnsMatchingCustomers", CRMSystemTest::filterByStatusReturnsMatchingCustomers);
        run("addInteractionStoresCustomerActivity", CRMSystemTest::addInteractionStoresCustomerActivity);

        System.out.println();
        System.out.println("CRM System Test Results");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);

        if (failed > 0) {
            throw new AssertionError("Some CRM tests failed.");
        }
    }

    private static void addCustomerStoresCustomer() {
        DataStore store = DataStore.getInstance();
        Customer customer = new Customer("Test User", "test@example.com", "555-0100", "Test Company");
        store.addCustomer(customer);
        assertTrue(store.getCustomer(customer.getId()) == customer, "Customer should be retrievable by id.");
    }

    private static void searchCustomersFindsByNameEmailOrCompany() {
        DataStore store = DataStore.getInstance();
        Customer customer = new Customer("Alice Chen", "alice@example.com", "555-0101", "Jinan CRM");
        store.addCustomer(customer);
        assertTrue(store.searchCustomers("Alice").contains(customer), "Search should find name.");
        assertTrue(store.searchCustomers("example").contains(customer), "Search should find email.");
        assertTrue(store.searchCustomers("Jinan").contains(customer), "Search should find company.");
    }

    private static void filterByStatusReturnsMatchingCustomers() {
        DataStore store = DataStore.getInstance();
        Customer customer = new Customer("Lead User", "lead@example.com", "555-0102", "Lead Company");
        customer.setStatus(Customer.CustomerStatus.LEAD);
        store.addCustomer(customer);
        assertTrue(store.filterByStatus(Customer.CustomerStatus.LEAD).contains(customer),
            "Status filter should include matching customer.");
    }

    private static void addInteractionStoresCustomerActivity() {
        DataStore store = DataStore.getInstance();
        Customer customer = new Customer("Activity User", "activity@example.com", "555-0103", "Activity Company");
        store.addCustomer(customer);
        Interaction interaction = new Interaction(customer.getId(), Interaction.InteractionType.NOTE, "Follow up next week");
        store.addInteraction(interaction);
        assertTrue(store.getInteractions(customer.getId()).contains(interaction),
            "Interaction should be stored under the customer id.");
    }

    private static void run(String name, Runnable test) {
        try {
            test.run();
            passed++;
            System.out.println("[PASS] " + name);
        } catch (Throwable error) {
            failed++;
            System.out.println("[FAIL] " + name + " - " + error.getMessage());
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
