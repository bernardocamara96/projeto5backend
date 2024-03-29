package aor.paj.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Task is a core entity representing a task in the system, including details such as title, description, priority,
 * status, and associated username. It also includes start and end dates, with logic to handle date parsing exceptions.
 * The class is designed with data validation annotations to ensure that essential fields are not null. The username and
 * ID are immutable post-creation, reflecting task ownership and identity.
 */

@XmlRootElement
public class TaskDto {
    @XmlElement
    private LocalDate startDate;
    @XmlElement
    private LocalDate endDate;
    @XmlElement
    private int id;
    @XmlElement
    private String title;
    @XmlElement
    private String description;
    @XmlElement
    private int priority;
    @XmlElement
    private int status;
    @XmlElement
    private String username_author;
    @XmlElement
    private String category_type;
    @XmlElement
    private boolean deleted;

    public TaskDto() {
    }


    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getCategory_type() {
        return category_type;
    }

    public void setCategory_type(String category_type) {
        this.category_type = category_type;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
    public void setEndDate(LocalDate endDate) {
        try{
            this.endDate = endDate;
        }catch (DateTimeParseException e){
            this.endDate = null;
        }
    }
    public LocalDate getStartDate() {
        return startDate;
    }
    public void setStartDate(LocalDate startDate) {
        try{
            this.startDate = startDate;
        }catch (DateTimeParseException e){
            this.startDate = null;
        }
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

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

    public int getPriority() {
        return priority;
    }
    public void setPriority(int priority) {
        this.priority = priority;
    }

    public @NotNull int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }

    public String getUsername_author() {
        return username_author;
    }

    public void setUsername_author(String username_author) {
        this.username_author = username_author;
    }

    @Override
    public String toString() {
        return "TaskDto{" +
                "startDate=" + startDate +
                ", endDate=" + endDate +
                ", id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", priority=" + priority +
                ", status=" + status +
                ", username_author='" + username_author + '\'' +
                ", category_type='" + category_type + '\'' +
                '}';
    }
}