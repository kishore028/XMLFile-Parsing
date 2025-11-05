package com.example.fordev.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.fordev.model.Todo;
import com.example.fordev.repository.TodoRepository;

import java.util.List;

@Service
public class TodoService {
    @Autowired
    private TodoRepository todoRepository;

    public List<Todo> getAllTodos() {
        return todoRepository.findAll();
    }

    public void save(Todo todo) {
        todoRepository.save(todo);
    }

    public void deleteById(Long id) {
        todoRepository.deleteById(id);
    }

    public Todo getById(Long id) {
        return todoRepository.findById(id).orElse(null);
    }
}
