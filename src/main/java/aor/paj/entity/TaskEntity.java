package aor.paj.entity;


import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table (name="task")
@NamedQuery(name="Task.findTaskById", query="SELECT t FROM TaskEntity t WHERE t.id=:id")
@NamedQuery(name="Task.getAllTasks",query="SELECT t FROM TaskEntity t")
@NamedQuery(name="Task.findTasksByUser", query="SELECT t FROM TaskEntity t WHERE t.user=:user ORDER BY t.priority DESC,CASE WHEN t.startDate IS NULL THEN 1 ELSE 0 END, t.startDate ASC,CASE WHEN t.endDate IS NULL THEN 1 ELSE 0 END, t.endDate ASC")
@NamedQuery(name="Task.findTasksByCategory", query="SELECT t FROM TaskEntity t WHERE t.category=:category ORDER BY t.priority DESC,CASE WHEN t.startDate IS NULL THEN 1 ELSE 0 END, t.startDate ASC,CASE WHEN t.endDate IS NULL THEN 1 ELSE 0 END, t.endDate ASC")
@NamedQuery(name="Task.deleteTasksBId", query="DELETE FROM TaskEntity t WHERE t.id = :id")
@NamedQuery(name="Task.findTasksByDeleted", query="SELECT t FROM TaskEntity t WHERE t.deleted=:deleted ORDER BY t.priority DESC,CASE WHEN t.startDate IS NULL THEN 1 ELSE 0 END, t.startDate ASC,CASE WHEN t.endDate IS NULL THEN 1 ELSE 0 END, t.endDate ASC")
@NamedQuery(name="Task.findTasksByCategoryAndUser", query="SELECT t FROM TaskEntity t WHERE t.category = :category AND t.user = :user ORDER BY t.priority DESC,CASE WHEN t.startDate IS NULL THEN 1 ELSE 0 END, t.startDate ASC,CASE WHEN t.endDate IS NULL THEN 1 ELSE 0 END, t.endDate ASC")
@NamedQuery(name="Task.findTasksByStatusAndUser", query="SELECT t FROM TaskEntity t WHERE t.status=:status AND t.user=:user AND t.deleted=:deleted")
@NamedQuery(name="Task.findTasksByUserAndDeleted", query="SELECT t FROM TaskEntity t WHERE t.user=:user AND t.deleted=:deleted")
@NamedQuery(name = "Task.countTasksByUser", query = "SELECT COUNT(t) FROM TaskEntity t WHERE t.user=:user")
@NamedQuery(name = "Task.countTasksByStatus", query = "SELECT COUNT(t) FROM TaskEntity t WHERE t.status=:status AND t.deleted=false")
@NamedQuery(name = "Task.countTasksByCategory", query = "SELECT COUNT(t) FROM TaskEntity t WHERE t.category=:category")
@NamedQuery(name = "Task.calculateAverageConclusionTime", query = "SELECT AVG(CAST(FUNCTION('TIME_TO_SEC', FUNCTION('TIMEDIFF', t.lastDoneDate,t.creationDate )) AS double)) /3600 FROM TaskEntity t WHERE t.lastDoneDate IS NOT NULL")
@NamedQuery(name = "Task.findAllLastDoneDates", query = "SELECT t.lastDoneDate FROM TaskEntity t WHERE t.lastDoneDate IS NOT NULL")

public class TaskEntity implements Serializable {

    private static final long longSerialVersionID=1L;

    @Id
    @GeneratedValue (strategy =  GenerationType.IDENTITY)
    @Column(name="id", nullable = false,unique = true,updatable = false)
    private int id;
    @Column (name="title", nullable = false,unique = false,updatable = true)
    private String title;
    @Column (name="description", nullable = false,unique = false,updatable = true)
    private String description;

    @ManyToOne
    //@Column (name="username_author", nullable = false, unique = false,updatable = false)
    private UserEntity user;
    @Column (name="priority", nullable = false,unique = false,updatable = true)
    private int priority;
    @Column (name="status", nullable = false, unique = false, updatable = true)
    private int status;
    @Column (name="start_date", nullable = true, unique = false,updatable = true)
    private LocalDate startDate;
    @Column (name="end_date", nullable = true, unique = false,updatable = true)
    private LocalDate endDate;
    @Column (name="creation_date", nullable = false, unique = false,updatable = false)
    private LocalDateTime creationDate;
    @Column (name="lastDone_date", nullable = true, unique = false,updatable = true)
    private LocalDateTime lastDoneDate;

    @ManyToOne
    //@Column (name="category", nullable = false, unique = false, updatable = true)
    private CategoryEntity category;
    @Column(name="deleted", nullable = false,unique = false,updatable = true)
    private boolean deleted;

    public TaskEntity() {}

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public CategoryEntity getCategory() {
        return category;
    }

    public void setCategory(CategoryEntity category) {
        this.category = category;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
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

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getLastDoneDate() {
        return lastDoneDate;
    }

    public void setLastDoneDate(LocalDateTime lastDoneDate) {
        this.lastDoneDate = lastDoneDate;
    }

    @Override
    public String toString() {
        return "TaskEntity{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", user=" + user +
                ", priority=" + priority +
                ", status=" + status +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", category=" + category +
                ", deleted=" + deleted +
                '}';
    }
}
