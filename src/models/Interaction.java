package models;

import java.time.LocalDateTime;
import java.util.UUID;

public class Interaction {
    private String id;
    private String customerId;
    private InteractionType type;
    private String description;
    private LocalDateTime date;

    public enum InteractionType {
        CALL, EMAIL, MEETING, NOTE
    }

    public Interaction() {
        this.id = UUID.randomUUID().toString();
        this.date = LocalDateTime.now();
    }

    public Interaction(String customerId, InteractionType type, String description) {
        this();
        this.customerId = customerId;
        this.type = type;
        this.description = description;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public InteractionType getType() { return type; }
    public void setType(InteractionType type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    @Override
    public String toString() {
        return type + " - " + date.toString();
    }
}
