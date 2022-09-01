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

import com.epam.digital.data.platform.notification.exceptions.NotificationBuildingException;
import com.epam.digital.data.platform.notification.model.NotificationTemplate;
import com.epam.digital.data.platform.notification.repository.NotificationTemplateRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;

@Service
public class NotificationService {
  private final Logger log = LoggerFactory.getLogger(NotificationService.class);

  private final NotificationTemplateRepository notificationTemplateRepository;

  public NotificationService(NotificationTemplateRepository notificationTemplateRepository) {
    this.notificationTemplateRepository = notificationTemplateRepository;
  }

  public void loadDir(File dir) {
    var indexFile = Path.of(dir.getPath(), "notification.ftlh").toFile();
    try {
      var htmlString = FileUtils.readFileToString(indexFile, StandardCharsets.UTF_8);
      var document = Jsoup.parse(htmlString);
      document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);

      embedImagesToHtml(document, dir);
      embedStyleToHtml(document, dir);

      String channel = dir.getParentFile().getName();
      var template = new NotificationTemplate();
      template.setContent(document.toString());
      template.setName(dir.getName());
      template.setChannel(channel);
      template.setChecksum(DigestUtils.sha256Hex(document.toString()));

      save(template);
    } catch (Exception e) {
      log.error("Failded processing template {}. Error message: {}", dir.getName(), e.getMessage());
    }
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
      if(styleName != null && !styleName.isEmpty()) {
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

  private void save(NotificationTemplate newTemplate) {
    var saved = notificationTemplateRepository.findByNameAndChannel(newTemplate.getName(), newTemplate.getChannel());
    if (saved == null) {
      notificationTemplateRepository.save(newTemplate);
      return;
    }

    if (saved.getChecksum().equals(newTemplate.getChecksum())) {
      return;
    }

    saved.update(newTemplate);
    notificationTemplateRepository.save(saved);
  }
}
