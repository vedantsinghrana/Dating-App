package com.app.dating.profile;

import com.app.dating.common.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Saves uploaded photos to local disk under app.uploads.dir. Swap for S3-compatible
 * storage later; callers only depend on the returned URL, not how it's stored.
 */
@Service
public class PhotoStorageService {

	private final Path uploadsDir;

	public PhotoStorageService(@Value("${app.uploads.dir}") String uploadsDir) {
		this.uploadsDir = Path.of(uploadsDir).toAbsolutePath().normalize();
	}

	public String store(MultipartFile file) {
		if (file.isEmpty()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "File is empty");
		}
		String contentType = file.getContentType();
		if (contentType == null || !contentType.startsWith("image/")) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Only image uploads are supported");
		}

		try {
			Files.createDirectories(uploadsDir);
			String extension = extensionOf(file.getOriginalFilename());
			String filename = UUID.randomUUID() + extension;
			Path target = uploadsDir.resolve(filename).normalize();
			if (!target.startsWith(uploadsDir)) {
				throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid file name");
			}
			Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
			return "/uploads/" + filename;
		} catch (IOException ex) {
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store file");
		}
	}

	private String extensionOf(String originalFilename) {
		if (originalFilename == null) {
			return "";
		}
		int dotIndex = originalFilename.lastIndexOf('.');
		if (dotIndex < 0 || dotIndex == originalFilename.length() - 1) {
			return "";
		}
		return originalFilename.substring(dotIndex).toLowerCase();
	}

}
