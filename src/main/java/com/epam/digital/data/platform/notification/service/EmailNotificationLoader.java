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
import com.epam.digital.data.platform.notification.exceptions.NotificationBuildingException;
import com.epam.digital.data.platform.notification.json.JsonSchemaFileValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;

import static com.epam.digital.data.platform.notification.service.NotificationChannel.EMAIL;

@Slf4j
public class EmailNotificationLoader extends AbstractGenericNotificationLoader {

  private static final String TEMPLATE_CONTENT_FILE_NAME = "notification.ftlh";
  private static final String TEMPLATE_METADATA_FILE_NAME = "notification.yml";

  public EmailNotificationLoader(NotificationTemplateRestClient templateRestClient,
      JsonSchemaFileValidator schemaValidator, ObjectMapper yamlMapper) {
    super(templateRestClient, schemaValidator, yamlMapper);
  }

  @Override
  public NotificationDto getNotificationDto(File dir) throws IOException {
    log.info("Processing email template {}", dir.getName());
    var indexFile = Path.of(dir.getPath(), TEMPLATE_CONTENT_FILE_NAME).toFile();
    var htmlString = FileUtils.readFileToString(indexFile, StandardCharsets.UTF_8);
    var document = Jsoup.parse(htmlString);
    document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);

    embedImagesToHtml(document, dir);
    embedStyleToHtml(document, dir);

    var templateMetadataFile = Path.of(dir.getPath(), TEMPLATE_METADATA_FILE_NAME).toFile();

    return NotificationDto.builder()
        .channel(EMAIL.getChannelName())
        .templateMetadataFile(templateMetadataFile)
        .content(document.toString())
        .build();
  }

  @Override
  public SaveNotificationTemplateInputDto getDefaultSaveNotificationTemplateInputDto(
      NotificationDto notificationDto) {

    return SaveNotificationTemplateInputDto.builder().content(notificationDto.getContent())
        .build();
  }

  private void embedImagesToHtml(Document htmlDocument, File dir) {
    for (Element image : htmlDocument.select("img")) {
      var imageFile = Path.of(dir.getPath(), image.attr("src")).toFile();
      try {
        var bytes = FileUtils.readFileToByteArray(imageFile);
        var base64encodedImage = Base64.getEncoder().encodeToString(bytes);
        image.attr("src", "data:image/jpeg;base64," + base64encodedImage);
      } catch (Exception e) {
        throw new NotificationBuildingException(
            String.format("Failed to embed picture \"%s\" into template",
                image.attr("src")), e);
      }
    }
  }

  private void embedStyleToHtml(Document document, File dir) {
    Elements link = document.select("link");
    for (Element element : link) {
      String styleName = element.attr("href");
      if (styleName != null && !styleName.isEmpty()) {
        var styleFile = Path.of(dir.getPath(), "css", styleName).toFile();
        try {
          document.head().select("link").remove();
          document.head().select("style").remove();
          var styleString = FileUtils.readFileToString(styleFile, StandardCharsets.UTF_8);
          document.head().append("<style>" + styleString + "</style>");
        } catch (Exception e) {
          throw new NotificationBuildingException("Failed to embed styles into template", e);
        }
      }
    }
  }
}
