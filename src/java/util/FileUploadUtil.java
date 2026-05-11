
package util;

import javax.servlet.http.Part;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * File upload utility using Part API.
 */
public class FileUploadUtil {
    
    public static String saveUploadedFile(Part filePart, String uploadDir, String fileName) throws IOException {
        String filePath = uploadDir + '/' + fileName;
        Files.createDirectories(Paths.get(uploadDir));
        filePart.write(filePath);
        return filePath;
    }
    
    public static String getFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] tokens = contentDisp.split(";");
        for (String token : tokens) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf('=') + 2, token.length() - 1);
            }
        }
        return "";
    }
}
