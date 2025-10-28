package com.example.fordev.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.fordev.model.Todo;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {
}
