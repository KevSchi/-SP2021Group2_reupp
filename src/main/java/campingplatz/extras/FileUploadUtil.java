package campingplatz.extras;

import java.io.*;
import java.nio.file.*;
 
import org.springframework.web.multipart.MultipartFile;
 
public class FileUploadUtil {
     
    public static String saveFile(String uploadDir, String fileName,String fileFormat,
        MultipartFile multipartFile) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
      
        int i = 1;
        File f = new File(uploadPath+"/"+fileName + fileFormat);
        String savedName=fileName;
        
        while(f.exists() && !f.isDirectory()) { 
            savedName = fileName.concat((String.valueOf(i++)));
            f = new File(uploadPath+"/"+savedName + fileFormat);
        }
       
        savedName = savedName.concat(fileFormat);
        try (InputStream inputStream = multipartFile.getInputStream()) {
            Path filePath = uploadPath.resolve(savedName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ioe) {        
            throw new IOException("Could not save image file: " + fileName, ioe);
        }return savedName;      
    }
}
