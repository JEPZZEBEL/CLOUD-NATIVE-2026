package cl.duoc.tickets.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class S3UploadResponse {
    private String ticketId;
    private String bucket;
    private String s3Key;
}
