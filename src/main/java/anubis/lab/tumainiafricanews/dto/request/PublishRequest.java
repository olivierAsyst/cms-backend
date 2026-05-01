package anubis.lab.tumainiafricanews.dto.request;

import java.time.LocalDateTime;

public record PublishRequest(
        LocalDateTime publishedAt
) {}
