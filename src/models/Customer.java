package models;

import java.time.LocalDateTime;
import java.util.UUID;

public class Customer {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String company;
    private String notes;
    private CustomerStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum CustomerStatus {
        LEAD, PROSPECT, ACTIVE, INACTIVE
    }

    public Customer() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = CustomerStatus.LEAD;
    }

    public Customer(String name, String email, String phone, String company) {
        this();
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.company = company;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public CustomerStatus getStatus() { return status; }
    public void setStatus(CustomerStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return name + " | " + email + " | " + status;
    }
}
