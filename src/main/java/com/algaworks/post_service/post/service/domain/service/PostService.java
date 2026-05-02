package com.algaworks.post_service.post.service.domain.service;

import com.algaworks.post_service.post.service.api.model.input.PostInput;
import com.algaworks.post_service.post.service.api.model.input.PostProcessResultInput;
import com.algaworks.post_service.post.service.api.model.output.PostOutput;
import com.algaworks.post_service.post.service.api.model.output.PostProcessOutput;
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

import static com.algaworks.post_service.post.service.infrastructure.rabbitmq.RabbitMQConfig.FANOUT_EXCHANGE_POST_PROCESSING_RECEIVED;

@RequiredArgsConstructor
@Slf4j
@Service
public class PostService {
    private final PostRepository postRepository;
    private final RabbitTemplate rabbitTemplate;

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
                .calculatedValue(post.getCalculateValue())
                .build();
    }

    public PostOutput create(PostInput postInput) {
        Post post = Post.builder()
                .title(postInput.getTitle())
                .body(postInput.getBody())
                .author(postInput.getAuthor())
                .build();
        postRepository.saveAndFlush(post);

        PostProcessOutput postProcessOutput = PostProcessOutput.builder()
                .id(post.getId())
                .postBody(post.getBody())
                .build();

        String exchange = FANOUT_EXCHANGE_POST_PROCESSING_RECEIVED;
        String routingKey = "";
        Object payload = postProcessOutput;

        MessagePostProcessor messagePostProcessor = message -> {
            message.getMessageProperties().setHeader("postId", postProcessOutput.getId());
            return message;
        };

        rabbitTemplate.convertAndSend(exchange, routingKey, payload, messagePostProcessor);

        return convertToOutputModel(post);
    }

    @Transactional
    public void postProcessResultReading(PostProcessResultInput postProcessingResultInput) {
        postRepository.findById(postProcessingResultInput.getId())
                .ifPresentOrElse(
                        post -> handlePostProcessResult(postProcessingResultInput, post),
                        () -> logIgnoredPost(postProcessingResultInput));
    }

    private void handlePostProcessResult(PostProcessResultInput postProcessingResultInput, Post post) {
        post.setWordCount(postProcessingResultInput.getWordCount());
        post.setCalculateValue(postProcessingResultInput.getCalculatedValue());

        postRepository.saveAndFlush(post);

        log.info("Post update: PostId {} Word Count: {} Calculated Value: {}", post.getId(), post.getWordCount(), post.getCalculateValue());
    }

    private void logIgnoredPost(PostProcessResultInput postProcessingResultInput) {
        log.info("Post Ignored: PostId {} Word Count: {} Calculated Value: {}", postProcessingResultInput.getId(), postProcessingResultInput.getWordCount(), postProcessingResultInput.getCalculatedValue());
    }

    private PostSummaryOutput convertToSummaryModel(Post post) {
        return PostSummaryOutput.builder()
                .id(post.getId())
                .title(post.getTitle())
                .summary(post.getBody())
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
                .calculatedValue(post.getCalculateValue())
                .build();
    }

    private Post returnPost(UUID postId) {
        return postRepository.findById(postId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
