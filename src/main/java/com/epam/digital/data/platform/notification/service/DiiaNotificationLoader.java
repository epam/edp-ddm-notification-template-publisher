package com.epam.digital.data.platform.notification.service;

import com.epam.digital.data.platform.notification.client.NotificationTemplateRestClient;
import com.epam.digital.data.platform.notification.json.JsonSchemaFileValidator;
import com.epam.digital.data.platform.notification.mapper.NotificationMetadataMapper;
import com.epam.digital.data.platform.notification.model.NotificationYamlObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static com.epam.digital.data.platform.notification.service.NotificationChannel.DIIA;

@Slf4j
@RequiredArgsConstructor
public class DiiaNotificationLoader implements NotificationDirectoryLoader {

  private static final String TEMPLATE_CONTENT_FILE_NAME = "notification.diia";
  private static final String TEMPLATE_METADATA_FILE_NAME = "notification.yml";

  private final NotificationTemplateRestClient templateRestClient;
  private final JsonSchemaFileValidator diiaSchemaValidator;
  private final ObjectMapper yamlMapper;

  @Override
  public void loadDir(File dir) {
    log.info("Processing diia template {}", dir.getName());
    try {
      var contentFile = Path.of(dir.getPath(), TEMPLATE_CONTENT_FILE_NAME).toFile();
      var content = FileUtils.readFileToString(contentFile, StandardCharsets.UTF_8);

      var templateMetadataFile = Path.of(dir.getPath(), TEMPLATE_METADATA_FILE_NAME).toFile();
      diiaSchemaValidator.validate(templateMetadataFile);
      var templateMetadata =
          yamlMapper.readValue(templateMetadataFile, NotificationYamlObject.class);
      var inputDto = NotificationMetadataMapper
          .toSaveNotificationTemplateInputDto(templateMetadata);
      inputDto.setContent(content);
      templateRestClient.saveTemplate(DIIA.getChannelName(), dir.getName(), inputDto);
    } catch (Exception e) {
      log.error("Failed processing template {}. Error: {}", dir.getName(), e);
    }
  }
}
