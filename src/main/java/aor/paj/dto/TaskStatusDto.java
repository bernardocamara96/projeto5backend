package aor.paj.dto;

import com.google.gson.Gson;
import jakarta.xml.bind.annotation.XmlElement;

public class TaskStatusDto {
    @XmlElement
    private int id;
    @XmlElement
    private int status;

    public TaskStatusDto(int id, int status) {
        this.id = id;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

}
