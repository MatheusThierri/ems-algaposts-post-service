package com.algaworks.post_service.post.service.api.model.input;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostInput {
    @NotBlank
    private String title;

    @NotBlank
    private String body;

    @NotBlank
    private String author;
}
