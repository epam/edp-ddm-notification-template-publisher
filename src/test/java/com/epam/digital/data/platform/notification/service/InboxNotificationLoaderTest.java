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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.epam.digital.data.platform.notification.client.NotificationTemplateRestClient;
import com.epam.digital.data.platform.notification.dto.NotificationTemplateAttributeDto;
import com.epam.digital.data.platform.notification.dto.SaveNotificationTemplateInputDto;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import lombok.SneakyThrows;
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

@ExtendWith(SpringExtension.class)
class InboxNotificationLoaderTest {

  private static String expectedResult;

  private File notificationFile;

  @Mock
  private NotificationTemplateRestClient notificationTemplateRestClient;
  @Captor
  private ArgumentCaptor<SaveNotificationTemplateInputDto> templateCaptor;

  private InboxNotificationLoader inboxNotificationLoader;


  @BeforeEach
  void init() {
    inboxNotificationLoader =
        new InboxNotificationLoader(
            notificationTemplateRestClient, new YAMLMapper());
  }

  @BeforeAll
  static void setup() throws IOException, URISyntaxException {
    File correctResultFile =
        getFile("/notifications/inbox/SendInboxNotificationWithMetadata/notification.ftl");
    expectedResult = FileUtils.readFileToString(correctResultFile, StandardCharsets.UTF_8);
  }

  @Test
  @SneakyThrows
  void shouldSaveWithMetadata() {
    notificationFile = getFile( "/notifications/inbox/SendInboxNotificationWithMetadata");

    inboxNotificationLoader.loadDir(notificationFile);

    verify(notificationTemplateRestClient)
        .saveTemplate(eq("inbox"), eq("SendInboxNotificationWithMetadata"),
            templateCaptor.capture());

    var actualNotificationTemplateDto = templateCaptor.getValue();
    assertThat(actualNotificationTemplateDto.getTitle()).isEqualTo("<Заголовок повідомлення>");
    assertThat(StringUtils.deleteWhitespace(actualNotificationTemplateDto.getContent()))
        .isEqualTo(StringUtils.deleteWhitespace(expectedResult));
    assertThat(actualNotificationTemplateDto.getAttributes())
        .containsExactly(new NotificationTemplateAttributeDto("name", "value"));
  }

  protected static File getFile(String path) throws URISyntaxException {
    return new File(
        Objects.requireNonNull(InboxNotificationLoaderTest.class.getResource(path))
            .toURI());
  }
}