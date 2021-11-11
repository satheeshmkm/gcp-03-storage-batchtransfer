package com.sck.gcp.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sck.gcp.service.BigDataService;
import com.sck.gcp.service.CloudStorageService;

import io.swagger.annotations.ApiOperation;

@RestController
public class StorageUploadAndBatchTransferController {
	private static final Logger LOGGER = LoggerFactory.getLogger(StorageUploadAndBatchTransferController.class);


	@Autowired
	private CloudStorageService cloudStorageService;

	@Autowired
	private BigDataService bigDataService;

	@ApiOperation("Upload the file to GCP Cloud Storage and run transfer")
	@PostMapping(value = "/upload", consumes = { "multipart/form-data" })
	public ResponseEntity<?> uploadFile(@RequestParam(value = "file", required = true) MultipartFile uploadfile) {
		if (uploadfile.isEmpty()) {
			LOGGER.error("File is not passed");
			return new ResponseEntity<>(false, HttpStatus.OK);
		}

		try {
			LOGGER.info("Uploading file to cloud");
			String msg = cloudStorageService.writeFile(uploadfile);
			LOGGER.info(msg);
			List<String> runs = bigDataService.runTransfer("61c92f82-0000-2569-995e-883d24f91aa8");
			return new ResponseEntity<>(msg + runs, new HttpHeaders(), HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.error("Exception happened", e);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

}
