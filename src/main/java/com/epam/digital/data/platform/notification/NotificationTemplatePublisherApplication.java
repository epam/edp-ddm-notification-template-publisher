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
import com.epam.digital.data.platform.notification.service.NotificationService;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.epam.digital.data.platform.notification.utils.IOUtils.getFileList;
import static java.util.stream.Collectors.toList;

@SpringBootApplication
public class NotificationTemplatePublisherApplication implements ApplicationRunner {

  private final Logger log = LoggerFactory.getLogger(NotificationTemplatePublisherApplication.class);

  private final AppProperties appProperties;
  private final NotificationService notificationService;

  public NotificationTemplatePublisherApplication(AppProperties appProperties, NotificationService notificationService) {
    this.appProperties = appProperties;
    this.notificationService = notificationService;
  }

  public static void main(String[] args) {
    SpringApplication.run(NotificationTemplatePublisherApplication.class, args);
  }


  @Override
  public void run(ApplicationArguments args) throws Exception {
    if (args.containsOption("notification_templates")) {
      getHandleNotifications();
    }
  }

  private void getHandleNotifications() {
    for (File templateDir : getDirectories(appProperties.getNotificationsDirectoryName())) {
      log.info("Processing {} directory", templateDir.getName());
      notificationService.loadDir(templateDir);
    }
  }

  private List<File> getDirectories(String root) {
    List<File> directories = new ArrayList<>();
    File[] obj = FileUtils.getFile(root).listFiles();
    if (obj != null) {
      for (File file : obj) {
        if (!file.exists()) {
          log.error("Directory {} does not exist", root);
          return Collections.emptyList();
        }
        directories.addAll(Arrays.stream(getFileList(file))
                .filter(File::isDirectory)
                .collect(toList()));
      }
    }
    if(directories.isEmpty()) {
      log.error("Not found any inner directory in {} directory. Please check your directory structure", root);
    }
    return directories;
  }
}
