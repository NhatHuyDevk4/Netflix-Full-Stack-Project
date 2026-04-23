package com.nextflix.clone.util;

public class FileHandleUtils {

    private FileHandleUtils() {

    }

    public static String extractFileExtension(String originalFilename) {
        String fileExtension = "";
        if(fileExtension != null && originalFilename.contains(".")) {
            // Lấy phần mở rộng của tệp từ tên gốc tức là phần sau dấu chấm cuối cùng
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return  fileExtension;
    }
}
