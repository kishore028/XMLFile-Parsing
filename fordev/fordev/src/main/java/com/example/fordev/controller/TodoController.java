package com.example.fordev.controller;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.fordev.model.Todo;
import com.example.fordev.service.TodoService;

@Controller
public class TodoController {

    @Autowired
    private TodoService todoService;

    @GetMapping("/")
    public String viewHome(Model model) {
        model.addAttribute("todos", todoService.getAllTodos());
        return "index";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("todo", new Todo());
        return "form";
    }

    @PostMapping("/save")
    public String saveTodo(@ModelAttribute Todo todo) {
        todoService.save(todo);
        return "redirect:/";
    }

    @GetMapping("/delete/{id}")
    public String deleteTodo(@PathVariable Long id) {
        todoService.deleteById(id);
        return "redirect:/";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("todo", todoService.getById(id));
        return "form";
    }
}
