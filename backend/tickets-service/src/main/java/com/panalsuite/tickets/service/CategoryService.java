package com.panalsuite.tickets.service;

import com.panalsuite.tickets.dto.NameRequest;
import com.panalsuite.tickets.exception.ApiException;
import com.panalsuite.tickets.model.Category;
import com.panalsuite.tickets.repository.CategoryRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository repository;
    private final SequenceService sequences;

    public CategoryService(CategoryRepository repository, SequenceService sequences) {
        this.repository = repository;
        this.sequences = sequences;
    }

    public List<Category> findAll() {
        return repository.findAll(Sort.by("idCategory"));
    }

    public Category create(NameRequest req) {
        String name = req.name().trim();
        if (repository.existsByNameIgnoreCase(name)) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe esa categoria");
        }
        return repository.save(new Category(sequences.next("categories"), name));
    }

    public Category rename(Long id, NameRequest req) {
        Category item = find(id);
        String name = req.name().trim();
        repository.findByNameIgnoreCase(name)
                .filter(other -> !other.getIdCategory().equals(id))
                .ifPresent(other -> {
                    throw new ApiException(HttpStatus.CONFLICT, "Ya existe esa categoria");
                });
        item.setName(name);
        return repository.save(item);
    }

    public void delete(Long id) {
        Category item = find(id);
        if (repository.count() <= 1) {
            throw new ApiException(HttpStatus.CONFLICT, "Debe quedar al menos una categoria");
        }
        repository.delete(item);
    }

    private Category find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Categoria no encontrada: " + id));
    }
}
