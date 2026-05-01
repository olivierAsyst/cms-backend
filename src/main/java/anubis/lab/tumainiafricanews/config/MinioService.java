package anubis.lab.tumainiafricanews.config;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioService {

    private final MinioClient minioClient;
    private final String bucketName = "tumaini-news";   // configure dans application.yml

    public String uploadImage(MultipartFile file, String folder) throws Exception {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String objectName = folder + "/" + fileName;

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );

        // Retourne l'URL publique ou presigned
        return "/files/" + objectName;   // ou générer une URL presigned
    }

    public void deleteImage(String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("Erreur suppression MinIO", e);
        }
    }
}
