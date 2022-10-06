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
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion.VersionFlag;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ResourceLoader;

import java.io.File;

@Slf4j
public class JsonSchemaFileValidator {

  private static final VersionFlag JSON_SCHEMA_VERSION = VersionFlag.V4;

  private final JsonSchema schema;
  private final ObjectMapper yamlMapper;

  public JsonSchemaFileValidator(String jsonSchemaLocation, ResourceLoader resourceLoader, ObjectMapper yamlMapper) {
    this.schema = loadSchema(jsonSchemaLocation, resourceLoader);
    this.yamlMapper = yamlMapper;
  }

  @SneakyThrows
  public void validate(File file) {
    var validationMessages = schema.validate(yamlMapper.readTree(file));
    if (!validationMessages.isEmpty()) {
      throw new JsonSchemaValidationException(
          "Failed validation of file " + file.getPath() + ", errors: " + validationMessages);
    }
  }

  @SneakyThrows
  private JsonSchema loadSchema(String jsonSchemaLocation, ResourceLoader resourceLoader) {
    var resource = resourceLoader.getResource(jsonSchemaLocation);
    var factory = JsonSchemaFactory
        .builder(JsonSchemaFactory.getInstance(JSON_SCHEMA_VERSION))
        .objectMapper(new ObjectMapper())
        .build();
    return factory.getSchema(resource.getInputStream());
  }
}
