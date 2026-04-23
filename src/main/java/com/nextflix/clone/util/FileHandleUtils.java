package com.nextflix.clone.util;

import org.springframework.core.io.Resource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class FileHandleUtils {

    private static final Map<String, String> VIDEO_CONTENT_TYPES = Map.ofEntries(
            Map.entry(".webm", "video/webm"),
            Map.entry(".ogg", "video/ogg"),
            Map.entry(".ogv", "video/ogg"),
            Map.entry(".mp4", "video/mp4"),
            Map.entry(".avi", "video/x-msvideo"),
            Map.entry(".mpeg", "video/mpeg"),
            Map.entry(".mpg", "video/mpeg"),
            Map.entry(".mkv", "video/x-matroska"),
            Map.entry(".flv", "video/x-flv"),
            Map.entry(".mov", "video/quicktime"),
            Map.entry(".wmv", "video/x-ms-wmv"),
            Map.entry(".mp3", "audio/mpeg"),
            Map.entry(".wav", "audio/wav"),
            Map.entry(".aac", "audio/aac"),
            Map.entry(".flac", "audio/flac"),
            Map.entry(".opus", "audio/opus")
    );

    private static final Map<String, String> IMAGE_CONTENT_TYPES = Map.ofEntries(
            Map.entry(".png", "image/png"),
            Map.entry(".jpg", "image/jpeg"),
            Map.entry(".jpeg", "image/jpeg"),
            Map.entry(".gif", "image/gif"),
            Map.entry(".webp", "image/webp")
    );

    private FileHandleUtils() {

    }

    public static String extractFileExtension(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank() || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
    }

    public static Path findFileByUuid(Path directory, String uuid) {
        try {
            return Files.list(directory)
                    .filter(path -> path.getFileName().toString().startsWith(uuid))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("File not found for UUID: " + uuid));
        } catch (Exception e) {
            throw new RuntimeException("Error while searching for file with UUID: " + uuid, e);
        }
    }

    public static String detectVideoContentType(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "video/mp4";
        }
        String lowerFileName = fileName.toLowerCase();
        return VIDEO_CONTENT_TYPES.entrySet().stream()
                .filter(entry -> lowerFileName.endsWith(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse("application/octet-stream");
    }

    public static String detectImageContentType(String fileName) {
        return IMAGE_CONTENT_TYPES.entrySet().stream()
                .filter(entry -> fileName.toLowerCase().endsWith(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse("image/jpeg");
    }

    public static Long[] parseRangeHeader(String rangeHeader, long fileLength) {
        String[] ranges = rangeHeader.replace("bytes=", "").split("-");
        long rangeStart = Long.parseLong(ranges[0]);
        long rangeEnd = ranges.length > 1 && !ranges[1].isBlank() ? Long.parseLong(ranges[1]) : fileLength - 1;
        return new Long[]{rangeStart, rangeEnd};
    }

    public static Resource createResourceFromFile(Path filePath, long rangeStart, long rangeLength) throws IOException {
        RandomAccessFile fileReader = new RandomAccessFile(filePath.toFile(), "r");
        fileReader.seek(rangeStart);

        InputStream partialContentStream = new InputStream() {
            private long totalBytesRead = 0;

            @Override
            public int read() throws IOException {
                if (totalBytesRead >= rangeLength) {
                    fileReader.close();
                    return -1;
                }
                totalBytesRead++;
                return fileReader.read();
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                if (totalBytesRead >= rangeLength) {
                    fileReader.close();
                    return -1;
                }
                long bytesRemaining = rangeLength - totalBytesRead;
                int bytesToRead = (int) Math.min(len, bytesRemaining);
                int bytesRead = fileReader.read(b, off, bytesToRead);

                if (totalBytesRead >= rangeLength) {
                    fileReader.close();
                } else if (bytesRead > 0) {
                    totalBytesRead += bytesRead;
                }
                return bytesRead;
            }

            @Override
            public void close() throws IOException {
                fileReader.close();
            }
        };

        return new InputStreamResource(partialContentStream) {
            @Override
            public long contentLength() {
                return rangeLength;
            }
        };
    }

    public static Resource createFullResource(Path filePath) throws IOException {
        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            throw new RuntimeException("File not found or not readable: " + filePath);
        }
        return resource;
    }
}
