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

package com.epam.digital.data.platform.notification;

import com.epam.digital.data.platform.notification.properties.AppProperties;
import com.epam.digital.data.platform.notification.service.NotificationDirectoryLoader;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@SpringBootApplication
public class NotificationTemplatePublisherApplication implements ApplicationRunner {

  private final Logger log = LoggerFactory.getLogger(NotificationTemplatePublisherApplication.class);

  private final AppProperties appProperties;

  private final Map<String, NotificationDirectoryLoader> templateDirLoaders;

  public NotificationTemplatePublisherApplication(
      AppProperties appProperties,
      @Qualifier("templateDirLoaders")
          Map<String, NotificationDirectoryLoader> templateDirLoaders) {
    this.appProperties = appProperties;
    this.templateDirLoaders = templateDirLoaders;
  }

  public static void main(String[] args) {
    SpringApplication.run(NotificationTemplatePublisherApplication.class, args);
  }


  @Override
  public void run(ApplicationArguments args) {
    if (args.containsOption("notification_templates")) {
      getHandleNotifications();
    }
  }

  private void getHandleNotifications() {
    var channelDirs = getChannelDirectories();
    for (File channelDir: channelDirs) {
      log.info("Processing of directory {}", channelDir);
      processChannelTemplates(channelDir);
    }
  }

  private List<File> getChannelDirectories() {
    var rootDir = FileUtils.getFile(appProperties.getNotificationsDirectoryName());
    return Optional.ofNullable(rootDir.listFiles())
        .map(Arrays::asList)
        .orElseGet(
            () -> {
              log.error("Directory {} does not exist", rootDir);
              return Collections.emptyList();
            });
  }

  private void processChannelTemplates(File channelDir) {
    if (!channelDir.isDirectory()) {
      return;
    }
    var channelName = channelDir.toPath().getFileName().toString();
    var channelTemplateLoader = templateDirLoaders.get(channelName);
    if (channelTemplateLoader == null) {
      log.warn("No template loader for channel {}", channelName);
      return;
    }
    var templateDirectories = Arrays.stream(Optional.ofNullable(channelDir.listFiles())
            .orElse(new File[] {}))
            .filter(File::isDirectory)
            .collect(Collectors.toList());
    for (File templateDir : templateDirectories) {
      channelTemplateLoader.loadDir(templateDir);
    }
  }
}
