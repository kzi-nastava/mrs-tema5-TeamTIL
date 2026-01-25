package rs.ac.uns.ftn.asd.Projekatsiit2023.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Base64;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(@Value("${cloudinary.cloud-name}") String cloudName,
                             @Value("${cloudinary.api-key}") String apiKey,
                             @Value("${cloudinary.api-secret}") String apiSecret) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }

    public String uploadBase64Image(String base64Image, String publicId) {
        try {
            // Remove "data:image/...;base64," prefix
            String base64Data = base64Image.replaceFirst("^data:image/[^;]+;base64,", "");

            byte[] imageBytes = Base64.getDecoder().decode(base64Data);

            Map uploadResult = cloudinary.uploader().upload(imageBytes,
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "folder", "tiltaxi/profiles",
                            "resource_type", "image"
                    )
            );

            return (String) uploadResult.get("secure_url");
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload image: " + e.getMessage());
        }
    }

    public void deleteImage(String publicId) {
        try {
            cloudinary.uploader().destroy("tiltaxi/profiles/" + publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete image: " + e.getMessage());
        }
    }
}