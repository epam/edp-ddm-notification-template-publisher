/*
 * Copyright 2022 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.notification.config;

import com.epam.digital.data.platform.notification.client.NotificationTemplateRestClient;
import com.epam.digital.data.platform.notification.json.JsonSchemaFileValidator;
import com.epam.digital.data.platform.notification.service.EmailNotificationLoader;
import com.epam.digital.data.platform.notification.service.NotificationDirectoryLoader;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ResourceLoader;

import java.util.Map;

@Configuration
@EnableFeignClients(clients = NotificationTemplateRestClient.class)
public class TemplateLoaderConfig {

  @Bean
  public Map<String, NotificationDirectoryLoader> templateDirLoaders(
      NotificationTemplateRestClient restClient,
      ResourceLoader resourceLoader,
      @Qualifier("yamlMapper") ObjectMapper yamlMapper) {
    return Map.of(
        "email",
        new EmailNotificationLoader(
            restClient,
            new JsonSchemaFileValidator(
                "classpath:schema/email-notification-metadata-schema.json",
                resourceLoader,
                yamlMapper),
            yamlMapper));
  }

  @Bean
  @Primary
  public ObjectMapper objectMapper() {
    var mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return mapper;
  }

  @Bean
  public ObjectMapper yamlMapper() {
    return new YAMLMapper();
  }
}
