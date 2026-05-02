package com.algaworks.post_service.post.service.infrastructure.rabbitmq;

import com.algaworks.post_service.post.service.api.model.input.PostProcessingResultInput;
import com.algaworks.post_service.post.service.domain.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.Duration;

import static com.algaworks.post_service.post.service.infrastructure.rabbitmq.RabbitMQConfig.QUEUE_POST_PROCESSING_RESULT;

@Component
@RequiredArgsConstructor
public class RabbitMQListener {
    private final PostService postService;

    @RabbitListener(queues = QUEUE_POST_PROCESSING_RESULT, concurrency = "2-3")
    @SneakyThrows
    public void handlePostProcessingResult(@Payload PostProcessingResultInput postProcessingResultInput) {
        postService.postProcessingResultReading(postProcessingResultInput);
        Thread.sleep(Duration.ofSeconds(5));
    }
}
