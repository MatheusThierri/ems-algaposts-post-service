package com.algaworks.post_service.post.service.api.controller;

import com.algaworks.post_service.post.service.api.model.input.PostInput;
import com.algaworks.post_service.post.service.api.model.output.PostOutput;
import com.algaworks.post_service.post.service.api.model.output.PostSummaryOutput;
import com.algaworks.post_service.post.service.domain.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<PostSummaryOutput> search(@PageableDefault Pageable pageable) {
        return postService.search(pageable);
    }

    @GetMapping("{postId}")
    @ResponseStatus(HttpStatus.OK)
    public PostOutput findById(@PathVariable UUID postId) {
        return postService.findById(postId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostOutput create(@Valid @RequestBody PostInput postInput) {
        return postService.create(postInput);
    }
}
