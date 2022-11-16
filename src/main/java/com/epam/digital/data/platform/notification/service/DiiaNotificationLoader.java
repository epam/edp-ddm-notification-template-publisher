package com.epam.digital.data.platform.notification.service;

import com.epam.digital.data.platform.notification.client.NotificationTemplateRestClient;
import com.epam.digital.data.platform.notification.dto.NotificationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static com.epam.digital.data.platform.notification.service.NotificationChannel.DIIA;

@Slf4j
public class DiiaNotificationLoader extends AbstractGenericNotificationLoader {

  private static final String TEMPLATE_CONTENT_FILE_NAME = "notification.diia";
  private static final String TEMPLATE_METADATA_FILE_NAME = "notification.yml";

  public DiiaNotificationLoader(NotificationTemplateRestClient templateRestClient, ObjectMapper yamlMapper) {
    super(templateRestClient, yamlMapper);
  }

  @Override
  public NotificationDto getNotificationDto(File dir) throws IOException {
    log.info("Processing diia template {}", dir.getName());
    var indexFile = Path.of(dir.getPath(), TEMPLATE_CONTENT_FILE_NAME).toFile();
    var content = FileUtils.readFileToString(indexFile, StandardCharsets.UTF_8);

    var templateMetadataFile = Path.of(dir.getPath(), TEMPLATE_METADATA_FILE_NAME).toFile();

    return NotificationDto.builder()
            .content(content)
            .templateMetadataFile(templateMetadataFile)
            .channel(DIIA.getChannelName())
            .build();
  }
}
