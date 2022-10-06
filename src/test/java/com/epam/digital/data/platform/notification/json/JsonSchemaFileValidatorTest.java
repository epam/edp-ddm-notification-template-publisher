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

package com.epam.digital.data.platform.notification.json;

import com.epam.digital.data.platform.notification.exceptions.JsonSchemaValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassRelativeResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ResourceUtils;

import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonSchemaFileValidatorTest {

  private final ResourceLoader resourceLoader = new ClassRelativeResourceLoader(getClass());
  private final ObjectMapper yamlMapper = new YAMLMapper();

  private JsonSchemaFileValidator validator;

  @BeforeEach
  void beforeEach() {
  }

  @Test
  void expectEmailValidationPassed() throws FileNotFoundException {
    validator =
            new JsonSchemaFileValidator(
                    "classpath:schema/email-notification-metadata-schema.json", resourceLoader, yamlMapper);

    var metadataFile = ResourceUtils.getFile(
            "classpath:notifications/email/SentEmailNotificationWithMetadata/notification2.yml");
    assertDoesNotThrow(() -> validator.validate(metadataFile));
  }

  @Test
  void expectEmailValidationErrorForAttributes() throws FileNotFoundException {
    validator =
        new JsonSchemaFileValidator(
            "classpath:schema/email-notification-metadata-schema.json", resourceLoader, yamlMapper);

    var metadataFile = ResourceUtils.getFile(
            "classpath:notifications/email/SentEmailNotificationWithMetadata/notification.yml");
    assertThrows(JsonSchemaValidationException.class, () -> validator.validate(metadataFile));
  }
}
