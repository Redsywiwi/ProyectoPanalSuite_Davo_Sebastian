package com.panalsuite.tickets.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "categories")
public class Category {
    @Id
    private Long idCategory;
    private String name;

    public Category() {}

    public Category(Long idCategory, String name) {
        this.idCategory = idCategory;
        this.name = name;
    }

    public Long getIdCategory() { return idCategory; }
    public void setIdCategory(Long idCategory) { this.idCategory = idCategory; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
