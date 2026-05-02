package com.algaworks.post_service.post.service.api.model.output;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostOutput {
    private UUID id;
    private String title;
    private String body;
    private String author;
    private Integer wordCount;
    private BigDecimal calculatedValue;
}
