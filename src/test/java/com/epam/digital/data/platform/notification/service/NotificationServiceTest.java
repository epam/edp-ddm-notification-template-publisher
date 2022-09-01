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

import com.epam.digital.data.platform.notification.model.NotificationTemplate;
import com.epam.digital.data.platform.notification.repository.NotificationTemplateRepository;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class NotificationServiceTest {

  private static File notificationFile;
  private static String expectedResult;
  private static File correctResultFile;

  @Mock
  private NotificationTemplateRepository repository;
  @Mock
  private NotificationTemplate notificationTemplate;
  @Captor
  private ArgumentCaptor<NotificationTemplate> templateCaptor;

  private NotificationService notificationService;

  private static final String expectedChecksum = "6ff965125b12d9aa891b27dd85971e7b652e3d97db08d1d9dee8be7ff6d618ce";

  @BeforeEach
  void init() {
    notificationService = new NotificationService(repository);
  }

  @BeforeAll
  static void setup() throws IOException, URISyntaxException {
    notificationFile = getFile("/notifications/email/SentEmailNotification");
    correctResultFile = getFile("/notifications/email/correctResult.ftlh");
    expectedResult = FileUtils.readFileToString(correctResultFile, StandardCharsets.UTF_8);
  }

  @Test
  void shouldSave() {
    notificationService.loadDir(notificationFile);
    verify(repository).save(templateCaptor.capture());
    var result = templateCaptor.getValue().getContent();
    assertThat(StringUtils.deleteWhitespace(result)).isEqualTo(StringUtils.deleteWhitespace(expectedResult));
  }

  @Test
  void shouldUpdateSavedTemplateIfOldChecksumNotEqualToNewChecksum() {
    when(repository.findByNameAndChannel("SentEmailNotification", "email")).thenReturn(notificationTemplate);
    when(notificationTemplate.getChecksum()).thenReturn("0");

    notificationService.loadDir(notificationFile);

    verify(notificationTemplate).update(templateCaptor.capture());
    var checksum = templateCaptor.getValue().getChecksum();
    var template = templateCaptor.getValue().getContent();

    assertThat(checksum).isEqualTo(expectedChecksum);
    assertThat(StringUtils.deleteWhitespace(template))
            .isEqualTo(StringUtils.deleteWhitespace(expectedResult));

    verify(repository).save(notificationTemplate);
  }

  private static File getFile(String path) throws URISyntaxException {
    return new File(Objects.requireNonNull(NotificationServiceTest.class.getResource(path)).toURI());
  }
}
