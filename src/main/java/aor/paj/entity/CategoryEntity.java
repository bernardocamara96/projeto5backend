package aor.paj.entity;

import aor.paj.dto.User;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Set;

@Entity
@Table(name="category")
@NamedQuery(name = "Category.findCategoryByType", query = "SELECT c FROM CategoryEntity c WHERE c.type = :type")
@NamedQuery(name = "Category.findCategoryById", query = "SELECT c FROM CategoryEntity c WHERE c.id = :id")
@NamedQuery(name="Category.getAllCategories",query="SELECT c FROM CategoryEntity c")
@NamedQuery(name="Category.getAllCategoriesByAuthor", query="SELECT c FROM CategoryEntity c WHERE c.author=:author")
@NamedQuery(name="Category.deleteCategoryByType", query="DELETE FROM CategoryEntity c WHERE c.type = :type")

public class CategoryEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue (strategy =  GenerationType.IDENTITY)
    @Column(name="id", nullable = false,unique = true,updatable = false)
    private int id;

    @Column(name="type", nullable = false,unique = true,updatable = true)
    private String type;


    @ManyToOne
   // @Column(name="category_owner", nullable = false,unique = false,updatable = false)
    private UserEntity author;

    @OneToMany (mappedBy = "category")
    private Set<TaskEntity> tasks;

    public CategoryEntity() {}
    public CategoryEntity(String type, UserEntity author) {
        this.type = type;
        this.author=author;
    }

    public Set<TaskEntity> getTasks() {
        return tasks;
    }

    public void setTasks(Set<TaskEntity> tasks) {
        this.tasks = tasks;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public UserEntity getAuthor() {
        return author;
    }

    public void setAuthor(UserEntity author) {
        this.author = author;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "CategoryEntity{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", author=" + author +
                ", tasks=" + tasks +
                '}';
    }
}
