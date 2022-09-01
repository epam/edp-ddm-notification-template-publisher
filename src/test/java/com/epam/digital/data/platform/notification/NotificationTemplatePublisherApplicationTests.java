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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.boot.ApplicationArguments;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.ResourceUtils;

import java.io.FileNotFoundException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class NotificationTemplatePublisherApplicationTests {

  @Mock
  private ApplicationArguments args;

  @Mock
  private NotificationService notificationService;

  private String notificationDirectoryName;
  private AppProperties appProperties;
  private NotificationTemplatePublisherApplication notificationTemplatePublisherApplication;

  @BeforeEach
  void setup() throws FileNotFoundException {
    notificationDirectoryName = ResourceUtils.getFile("classpath:notifications").getAbsolutePath();
    appProperties = new AppProperties();
    appProperties.setNotificationsDirectoryName(notificationDirectoryName);
    notificationTemplatePublisherApplication = new NotificationTemplatePublisherApplication(appProperties, notificationService);
  }

  @Test
  void shouldCallServiceForEachFolder() throws Exception {
    when(args.containsOption("notification_templates")).thenReturn(true);
    notificationTemplatePublisherApplication.run(args);

    verify(notificationService, times(3)).loadDir(any());
  }

  @Test
  void shouldReturnEmptyListOfFilesWhenNotificationFolderAbsent() throws Exception {
    when(args.containsOption("notification_templates")).thenReturn(true);
    appProperties.setNotificationsDirectoryName(notificationDirectoryName + "a");
    notificationTemplatePublisherApplication.run(args);

    verify(notificationService, never()).loadDir(any());
  }

}
