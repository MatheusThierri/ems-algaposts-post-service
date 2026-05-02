package com.algaworks.post_service.post.service.api.model.input;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostProcessResultInput {
    private UUID id;
    private Integer wordCount;
    private BigDecimal calculatedValue;
}
