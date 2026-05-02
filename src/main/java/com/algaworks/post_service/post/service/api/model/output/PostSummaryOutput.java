package com.algaworks.post_service.post.service.api.model.output;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostSummaryOutput {
    private UUID id;
    private String title;
    private String summary;
    private String author;
}
