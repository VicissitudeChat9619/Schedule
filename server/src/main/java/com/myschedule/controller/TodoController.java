package com.myschedule.controller;

import com.myschedule.dto.ApiResponse;
import com.myschedule.dto.TodoRequest;
import com.myschedule.entity.Todo;
import com.myschedule.entity.User;
import com.myschedule.service.TodoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Todo>>> list(Authentication authentication,
                                                         @RequestParam(required = false) String status) {
        User user = (User) authentication.getPrincipal();
        List<Todo> todos = todoService.listByStatus(user.getId(), status);
        return ResponseEntity.ok(ApiResponse.success(todos));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Todo>> create(Authentication authentication,
                                                     @Valid @RequestBody TodoRequest request) {
        User user = (User) authentication.getPrincipal();
        Todo todo = todoService.create(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(todo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Todo>> update(Authentication authentication,
                                                     @PathVariable Long id,
                                                     @Valid @RequestBody TodoRequest request) {
        User user = (User) authentication.getPrincipal();
        try {
            Todo todo = todoService.update(user.getId(), id, request);
            return ResponseEntity.ok(ApiResponse.success(todo));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @PutMapping("/{id}/done")
    public ResponseEntity<ApiResponse<Todo>> markDone(Authentication authentication,
                                                       @PathVariable Long id) {
        User user = (User) authentication.getPrincipal();
        try {
            Todo todo = todoService.markDone(user.getId(), id);
            return ResponseEntity.ok(ApiResponse.success(todo));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @PutMapping("/{id}/undo")
    public ResponseEntity<ApiResponse<Todo>> markUndo(Authentication authentication,
                                                       @PathVariable Long id) {
        User user = (User) authentication.getPrincipal();
        try {
            Todo todo = todoService.markUndo(user.getId(), id);
            return ResponseEntity.ok(ApiResponse.success(todo));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(Authentication authentication,
                                                     @PathVariable Long id) {
        User user = (User) authentication.getPrincipal();
        try {
            todoService.delete(user.getId(), id);
            return ResponseEntity.ok(ApiResponse.success());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Todo>> getById(Authentication authentication,
                                                      @PathVariable Long id) {
        User user = (User) authentication.getPrincipal();
        try {
            Todo todo = todoService.getById(user.getId(), id);
            return ResponseEntity.ok(ApiResponse.success(todo));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }
}
