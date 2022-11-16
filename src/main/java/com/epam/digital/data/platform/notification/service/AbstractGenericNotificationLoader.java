/*
 * Copyright 2022 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.epam.digital.data.platform.notification.service;

import com.epam.digital.data.platform.notification.client.NotificationTemplateRestClient;
import com.epam.digital.data.platform.notification.dto.NotificationDto;
import com.epam.digital.data.platform.notification.dto.SaveNotificationTemplateInputDto;
import com.epam.digital.data.platform.notification.mapper.NotificationMetadataMapper;
import com.epam.digital.data.platform.notification.model.NotificationYamlObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractGenericNotificationLoader implements NotificationDirectoryLoader {

  private final NotificationTemplateRestClient templateRestClient;
  private final ObjectMapper yamlMapper;

  @SneakyThrows
  @Override
  public void loadDir(File dir) {
    try {
      var notificationDto = getNotificationDto(dir);

      SaveNotificationTemplateInputDto inputDto =
          getSaveNotificationTemplateInputDto(notificationDto);

      templateRestClient.saveTemplate(notificationDto.getChannel(), dir.getName(), inputDto);
    } catch (Exception e) {
      log.error("Failed processing template {}. Error: {}", dir.getName(), e);
      throw e;
    }

  }

  protected SaveNotificationTemplateInputDto getSaveNotificationTemplateInputDto(
      NotificationDto notificationDto) throws IOException {
    File templateMetadataFile = notificationDto.getTemplateMetadataFile();
    var templateMetadata = getNotificationAttributes(templateMetadataFile);
    var inputDto = NotificationMetadataMapper.toSaveNotificationTemplateInputDto(templateMetadata);
    inputDto.setContent(notificationDto.getContent());
    return inputDto;
  }

  public abstract NotificationDto getNotificationDto(File dir) throws IOException;

  private NotificationYamlObject getNotificationAttributes(File file) throws IOException {
    try {
      return yamlMapper.readValue(file, NotificationYamlObject.class);
    } catch (FileNotFoundException e) {
      log.info("Attributes file {} not existing", file.getName());
      return new NotificationYamlObject();
    }
  }
}
