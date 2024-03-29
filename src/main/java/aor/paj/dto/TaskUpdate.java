package aor.paj.dto;

import java.time.LocalDate;

/**
 * TaskUpdate is a DTO (Data Transfer Object) designed for encapsulating modifications to an existing task. It includes
 * fields for updating the task's title, description, priority, status, and dates. Additional boolean fields are provided
 * to explicitly handle the removal of start and end dates, accommodating scenarios where dates might be unset. This class
 * supports the partial update of task properties, allowing users to modify only the attributes they need to change.
 */

public class TaskUpdate {
    private String title;
    private String description;
    private Integer priority;
    private int status;
    private LocalDate startDate;
    private boolean removeStartDate = false;
    private LocalDate endDate;
    private boolean removeEndDate = false;

    // Getters e setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public boolean isRemoveStartDate() {
        return removeStartDate;
    }

    public void setRemoveStartDate(boolean removeStartDate) {
        this.removeStartDate = removeStartDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public boolean isRemoveEndDate() {
        return removeEndDate;
    }

    public void setRemoveEndDate(boolean removeEndDate) {
        this.removeEndDate = removeEndDate;
    }
}
