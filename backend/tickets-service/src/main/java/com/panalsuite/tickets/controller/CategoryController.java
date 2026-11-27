package com.panalsuite.tickets.controller;

import com.panalsuite.tickets.dto.NameRequest;
import com.panalsuite.tickets.model.Category;
import com.panalsuite.tickets.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    @GetMapping
    public List<Category> list() {
        return service.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Category create(@Valid @RequestBody NameRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public Category rename(@PathVariable Long id, @Valid @RequestBody NameRequest request) {
        return service.rename(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
