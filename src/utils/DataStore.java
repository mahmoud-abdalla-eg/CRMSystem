package utils;

import models.Customer;
import models.Interaction;

import java.util.*;
import java.util.stream.Collectors;

public class DataStore {
    private static DataStore instance;
    private Map<String, Customer> customers;
    private Map<String, List<Interaction>> interactions;

    private DataStore() {
        customers = new LinkedHashMap<>();
        interactions = new LinkedHashMap<>();
        loadSampleData();
    }

    public static DataStore getInstance() {
        if (instance == null) instance = new DataStore();
        return instance;
    }

    private void loadSampleData() {
        Customer c1 = new Customer("Ahmed Hassan", "ahmed@techcorp.com", "+1234567890", "TechCorp Inc.");
        c1.setStatus(Customer.CustomerStatus.ACTIVE);
        customers.put(c1.getId(), c1);

        Customer c2 = new Customer("Sarah Johnson", "sarah@innovate.io", "+1987654321", "Innovate LLC");
        c2.setStatus(Customer.CustomerStatus.PROSPECT);
        customers.put(c2.getId(), c2);

        Customer c3 = new Customer("Mohamed Ali", "mohamed@global.com", "+1122334455", "Global Systems");
        c3.setStatus(Customer.CustomerStatus.LEAD);
        customers.put(c3.getId(), c3);
    }

    public void addCustomer(Customer customer) {
        customers.put(customer.getId(), customer);
    }

    public void updateCustomer(Customer customer) {
        customer.setUpdatedAt(java.time.LocalDateTime.now());
        customers.put(customer.getId(), customer);
    }

    public void deleteCustomer(String id) {
        customers.remove(id);
        interactions.remove(id);
    }

    public Customer getCustomer(String id) {
        return customers.get(id);
    }

    public List<Customer> getAllCustomers() {
        return new ArrayList<>(customers.values());
    }

    public List<Customer> searchCustomers(String query) {
        if (query == null || query.trim().isEmpty()) return getAllCustomers();
        String lower = query.toLowerCase();
        return customers.values().stream()
            .filter(c -> containsIgnoreCase(c.getName(), lower)
                    || containsIgnoreCase(c.getEmail(), lower)
                    || containsIgnoreCase(c.getCompany(), lower))
            .collect(Collectors.toList());
    }

    private boolean containsIgnoreCase(String value, String query) {
        return value != null && value.toLowerCase().contains(query);
    }

    public List<Customer> filterByStatus(Customer.CustomerStatus status) {
        if (status == null) return getAllCustomers();
        return customers.values().stream()
            .filter(c -> c.getStatus() == status)
            .collect(Collectors.toList());
    }

    public void addInteraction(Interaction interaction) {
        interactions.computeIfAbsent(interaction.getCustomerId(), k -> new ArrayList<>()).add(interaction);
    }

    public List<Interaction> getInteractions(String customerId) {
        return interactions.getOrDefault(customerId, new ArrayList<>());
    }
}
