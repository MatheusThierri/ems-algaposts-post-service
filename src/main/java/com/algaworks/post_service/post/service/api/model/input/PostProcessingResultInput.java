package com.algaworks.post_service.post.service.api.model.input;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostProcessingResultInput {
    private UUID id;
    private Long wordCount;
    private BigDecimal calculatedValue;
}
