package com.sck.gcp.service;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.sck.gcp.processor.FileProcessor;

@Service
public class CloudStorageService {
	private static final Logger LOGGER = LoggerFactory.getLogger(CloudStorageService.class);

	@Autowired
	private Storage storage;

	@Value("${com.sck.upload.bucket}")
	private String uploadBucketName;
	
	
	/*
	 * @Value("${com.sck.download.bucket}") private String downloadBucketName;
	 */
	
	@Value("${com.sck.upload.dir:upload}")
	private String uploadFolder;
	
	@Autowired
	private FileProcessor fileProcessor;

	public String writeFile(MultipartFile inputFile) throws IOException {
		LOGGER.info("Inside com.sck.gcp.service.CloudStorageService.writeFIle(String, MultipartFile)");
		String uploadedFile = getFilePath(uploadFolder, inputFile.getOriginalFilename());
		
		String xml = fileProcessor.readFile(inputFile);
		LOGGER.info("readFile() completed");
		String json = fileProcessor.convertToJSONL(xml);
		LOGGER.info("convertToJSON() completed");	
		byte[] arr = json.getBytes();
		
		uploadToCloudStorage(uploadedFile, arr);
		return "File uploaded to bucket " + uploadBucketName + " as " + uploadedFile;
	}

	public void uploadToCloudStorage(String uploadedFile, byte[] arr) {
		BlobId blobId = BlobId.of(uploadBucketName, uploadedFile);
		BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();	
		storage.create(blobInfo, arr);
	}

	public String getFilePath(String uploadFolder, String fileName) {
		StringBuilder uploadFilePath = new StringBuilder();
		if (StringUtils.isNotBlank(uploadFolder)) {
			uploadFilePath.append(uploadFolder).append("/");
		}
		uploadFilePath.append(StringUtils.replaceChars(fileName, '.', '_')).append(".json");
		return  uploadFilePath.toString();
	}
	
	
	/*
	 * public String readFile(String uploadFolder, MultipartFile uploadFile) throws
	 * IOException { LOGGER.
	 * info("Inside com.sck.gcp.service.CloudStorageService.readFile(String, MultipartFile)"
	 * ); String uploadedFile = getFilePath(uploadFolder,
	 * uploadFile.getOriginalFilename()); byte[] arr = uploadFile.getBytes();
	 * uploadToCloudStorage(uploadedFile, arr); return "File uploaded to bucket " +
	 * uploadBucketName + " as " + uploadedFile; }
	 */

}
