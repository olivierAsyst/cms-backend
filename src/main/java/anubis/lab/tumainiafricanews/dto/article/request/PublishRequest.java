package anubis.lab.tumainiafricanews.dto.article.request;

import java.time.LocalDateTime;

public record PublishRequest(
        LocalDateTime publishedAt
) {}
