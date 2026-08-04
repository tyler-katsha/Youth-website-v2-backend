package com.tyler.YouthEngedi.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.tyler.YouthEngedi.Exceptions.ImageException;
import com.tyler.YouthEngedi.Exceptions.ResourceNotFoundException;
import com.tyler.YouthEngedi.Repository.UserRepository;
import com.tyler.YouthEngedi.models.User;
import com.tyler.YouthEngedi.models.dtos.FragmentedImage;
import com.tyler.YouthEngedi.models.dtos.InnerFragmentedImage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    private final UserRepository userRepository;

    private static final String TEMP_DIR = "temp/uploads/";
    public String upload(MultipartFile file){
        try{
            var uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("resource_type", "auto"));
            return (String) uploadResult.get("secure_url");
        } catch (Exception e){
            throw new ImageException("Cloudinary upload failed " + e.getMessage());
        }
    }

    public String upload(MultipartFile file,long userId){
        try{

            User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User doesn't exist"));

            var uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("resource_type", "auto","public_id","users/" + userId,"overwrite",true));
            // Returns the secure URL to store in your database

            String url = (String) uploadResult.get("secure_url");

            user.setProfileImageUrl(url);

            userRepository.save(user);
            return url;
        } catch (IOException e){
            return "Upload failed";
        }
    }

    public String generateAltName(MultipartFile file){
        try{
            String fileName = file.getOriginalFilename();

            if(fileName == null){
                return "Uploaded image";
            }

            fileName = fileName.replaceFirst("[.][^.]+$","");

            fileName = fileName.replace("_"," ").replace("-"," ");

            StringBuilder alt = new StringBuilder();

            for(String word:fileName.split("\\s+")){
                if(!word.isEmpty()){
                    alt.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1).toLowerCase()).append(" ");
                }
            }

            return alt.toString().trim();
        } catch (NullPointerException e){
            return file.getOriginalFilename();
        }
    }

    public void processChunk(FragmentedImage fragmentedImage) {

        try{
            Path tempPath = Paths.get(TEMP_DIR, fragmentedImage.getInnerFragmentedImage().getFileId() + "_" + fragmentedImage.getIndex());
            Files.write(tempPath,fragmentedImage.getChunk().getBytes());

            if(fragmentedImage.getIndex() != fragmentedImage.getInnerFragmentedImage().getTotal() - 1){
                mergeChunks(fragmentedImage.getInnerFragmentedImage());
            }
        } catch (IOException e){
            throw new ImageException("Unable to split image");
        }

    }

    private void mergeChunks(InnerFragmentedImage innerFragmentedImage) throws IOException {
        Path finalFile = Paths.get("uploads/",innerFragmentedImage.getFileName());

        try(OutputStream out = new BufferedOutputStream(new FileOutputStream(finalFile.toFile()))){
            for (int i = 0; i < innerFragmentedImage.getTotal(); i++){
                Path chunkPath = Paths.get(TEMP_DIR, innerFragmentedImage.getFileId() + "_" + i);
                Files.copy(chunkPath,out);
                Files.delete(chunkPath);
            }
        }
    }

    public String getFileFormattedSize(MultipartFile file){
        return String.format("%d MB",file.getSize() / (1024 * 1024));
    }

    public boolean deleteImageByUrl(String imageUrl) {
        try {
            String publicId = extractPublicId(imageUrl);

            if (publicId == null || publicId.isEmpty()) {
                throw new IllegalArgumentException("Could not extract public ID from URL");
            }

            var result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());

            return "ok".equals(result.get("result"));

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String extractPublicId(String imageUrl) {
        // Find the start of the path after "/upload/"
        String uploadMarker = "/upload/";
        int uploadIndex = imageUrl.indexOf(uploadMarker);

        if (uploadIndex == -1) {
            return null;
        }

        // Get everything after "/upload/"
        String path = imageUrl.substring(uploadIndex + uploadMarker.length());

        // Remove the version string (e.g., "v1622345678/") if it exists
        if (path.matches("^v\\d+/.*")) {
            path = path.replaceFirst("^v\\d+/", "");
        }

        // Remove the file extension (e.g., ".jpg", ".png")
        int dotIndex = path.lastIndexOf('.');
        if (dotIndex != -1) {
            path = path.substring(0, dotIndex);
        }

        return path;
    }
}
