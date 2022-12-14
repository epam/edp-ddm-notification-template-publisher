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
import com.epam.digital.data.platform.notification.dto.NotificationTemplateAttributeDto;
import com.epam.digital.data.platform.notification.dto.SaveNotificationTemplateInputDto;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class DiiaNotificationLoaderTest {

  @Mock
  private NotificationTemplateRestClient notificationTemplateRestClient;
  @Captor
  private ArgumentCaptor<SaveNotificationTemplateInputDto> templateCaptor;

  private NotificationDirectoryLoader diiaNotificationLoader;


  @BeforeEach
  void init() {
    diiaNotificationLoader =
        new DiiaNotificationLoader(
            notificationTemplateRestClient, new YAMLMapper());
  }

  @Test
  void shouldSave() throws URISyntaxException {
    var notificationFile = getFile("/notifications/diia/SendDiiaNotificationWithMetadata");

    diiaNotificationLoader.loadDir(notificationFile);
    verify(notificationTemplateRestClient)
        .saveTemplate(eq("diia"), eq("SendDiiaNotificationWithMetadata"), templateCaptor.capture());

    var actualNotificationTemplateDto = templateCaptor.getValue();
    assertThat(actualNotificationTemplateDto.getTitle()).isEqualTo("Some test title");
    assertThat(actualNotificationTemplateDto.getContent())
        .isEqualTo("Some test notification template");
    assertThat(actualNotificationTemplateDto.getAttributes())
        .containsExactlyInAnyOrder(new NotificationTemplateAttributeDto("actionType", "message"),
            new NotificationTemplateAttributeDto("templateType", "template_type"),
            new NotificationTemplateAttributeDto("shortText", "Attention message"));

  }

  private static File getFile(String path) throws URISyntaxException {
    return new File(Objects.requireNonNull(DiiaNotificationLoaderTest.class.getResource(path)).toURI());
  }
}
