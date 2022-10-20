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
import com.epam.digital.data.platform.notification.json.JsonSchemaFileValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

@Slf4j
public class InboxNotificationLoader extends AbstractGenericNotificationLoader {

  public InboxNotificationLoader(NotificationTemplateRestClient templateRestClient,
      JsonSchemaFileValidator schemaValidator, ObjectMapper yamlMapper) {
    super(templateRestClient, schemaValidator, yamlMapper);
  }

  @Override
  public NotificationDto getNotificationDto(File dir) throws IOException {
    log.info("Processing inbox template {}", dir.getName());
    var indexFile = Path.of(dir.getPath(), "notification.ftl").toFile();
    var content = FileUtils.readFileToString(indexFile, StandardCharsets.UTF_8);

    var templateMetadataFile = Path.of(dir.getPath(), "notification.yml").toFile();

    return NotificationDto.builder()
        .content(content)
        .templateMetadataFile(templateMetadataFile)
        .channel("inbox")
        .build();
  }
}
