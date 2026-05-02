package com.algaworks.post_service.post.service.domain.service;

import com.algaworks.post_service.post.service.api.model.input.PostInput;
import com.algaworks.post_service.post.service.api.model.input.PostProcessingResultInput;
import com.algaworks.post_service.post.service.api.model.output.PostOutput;
import com.algaworks.post_service.post.service.api.model.output.PostProcessingOutput;
import com.algaworks.post_service.post.service.api.model.output.PostSummaryOutput;
import com.algaworks.post_service.post.service.domain.model.Post;
import com.algaworks.post_service.post.service.domain.repository.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class PostService {
    private final PostRepository postRepository;
    private final RabbitTemplate rabbitTemplate;
    public static final String FANOUT_EXCHANGE_POST_PROCESSING_RECEIVED = "text-processor-service.post-processing-received.v1.e";

    public Page<PostSummaryOutput> search(Pageable pageable) {
        Page<Post> posts = postRepository.findAll(pageable);
        return posts.map(this::convertToSummaryModel);
    }

    public PostOutput findById(UUID postId) {
        Post post = returnPost(postId);

        return PostOutput.builder()
                .id(post.getId())
                .title(post.getTitle())
                .body(post.getBody())
                .author(post.getAuthor())
                .wordCount(post.getWordCount())
                .calculatedValue(post.getCalculatedValue())
                .build();
    }

    @Transactional
    public PostOutput create(PostInput postInput) {
        Post post = Post.builder()
                .title(postInput.getTitle())
                .body(postInput.getBody())
                .author(postInput.getAuthor())
                .build();
        postRepository.saveAndFlush(post);

        PostProcessingOutput postProcessingOutput = PostProcessingOutput.builder()
                .id(post.getId())
                .postBody(post.getBody())
                .build();

        MessagePostProcessor messagePostProcessor = message -> {
            message.getMessageProperties().setHeader("postId", postProcessingOutput.getId());
            return message;
        };

        rabbitTemplate.convertAndSend(FANOUT_EXCHANGE_POST_PROCESSING_RECEIVED, "", postProcessingOutput, messagePostProcessor);

        return convertToOutputModel(post);
    }

    @Transactional
    public void postProcessingResultReading(PostProcessingResultInput postProcessingResultInput) {
        Post post = returnPost(postProcessingResultInput.getId());

        handlePostProcessingResult(postProcessingResultInput, post);
    }

    private void handlePostProcessingResult(PostProcessingResultInput postProcessingResultInput, Post post) {
        post.setWordCount(postProcessingResultInput.getWordCount());
        post.setCalculatedValue(postProcessingResultInput.getCalculatedValue());

        postRepository.saveAndFlush(post);
    }

    private PostSummaryOutput convertToSummaryModel(Post post) {
        return PostSummaryOutput.builder()
                .id(post.getId())
                .title(post.getTitle())
                .summary(getFirstThreeLines(post.getBody()))
                .author(post.getAuthor())
                .build();
    }

    private PostOutput convertToOutputModel(Post post) {
        return PostOutput.builder()
                .id(post.getId())
                .title(post.getTitle())
                .body(post.getBody())
                .author(post.getAuthor())
                .wordCount(post.getWordCount())
                .calculatedValue(post.getCalculatedValue())
                .build();
    }

    private Post returnPost(UUID postId) {
        return postRepository.findById(postId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public String getFirstThreeLines(String body) {
        return body.lines()
                .limit(3)
                .collect(Collectors.joining("\n"));
    }
}
