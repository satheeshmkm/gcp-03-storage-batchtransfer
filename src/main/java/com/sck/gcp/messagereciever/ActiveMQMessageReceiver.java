package com.sck.gcp.messagereciever;

import java.io.IOException;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.TextMessage;

import org.apache.activemq.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.sck.gcp.processor.FileProcessor;
import com.sck.gcp.service.BigDataService;
import com.sck.gcp.service.CloudStorageService;

@Component
public class ActiveMQMessageReceiver {
	private static final Logger LOGGER = LoggerFactory.getLogger(ActiveMQMessageReceiver.class);

	@Autowired
	private FileProcessor fileProcessor;

	@Autowired
	private CloudStorageService cloudStorageService;

	@Autowired
	private BigDataService bigDataService;

	@JmsListener(destination = "${inbound.endpoint}", containerFactory = "jmsListenerContainerFactory")
	public void receiveMessage(Message msg) throws JMSException {
		try {
			String xml = ((TextMessage) msg).getText();
			fileProcessor.convertToJSONL(xml);
			LOGGER.info("readFile() completed");
			String json = fileProcessor.convertToJSONL(xml);
			LOGGER.info("convertToJSON() completed");
			byte[] arr = json.getBytes();
			cloudStorageService.uploadToCloudStorage("product.json", arr);
			LOGGER.info("File uploaded to bucket with name " + "product.json");
			List<String> runs = bigDataService.runTransfer("61c92f82-0000-2569-995e-883d24f91aa8");
			LOGGER.info("Job started " + runs);
		} catch (JMSException e) {
			LOGGER.error("JMSException occured", e);
		} catch (IOException e) {
			LOGGER.error("IOException occured", e);
		}

	}

}
