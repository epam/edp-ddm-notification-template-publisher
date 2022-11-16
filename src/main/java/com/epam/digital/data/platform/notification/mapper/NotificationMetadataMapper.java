package com.epam.digital.data.platform.notification.mapper;

import com.epam.digital.data.platform.notification.dto.NotificationTemplateAttributeDto;
import com.epam.digital.data.platform.notification.dto.SaveNotificationTemplateInputDto;
import com.epam.digital.data.platform.notification.model.NotificationYamlObject;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NotificationMetadataMapper {

  private NotificationMetadataMapper() {}

  public static SaveNotificationTemplateInputDto toSaveNotificationTemplateInputDto(NotificationYamlObject src) {
    return SaveNotificationTemplateInputDto.builder()
        .title(src.getTitle())
        .attributes(toNotificationTemplateAttributeDtos(src.getAttributes()))
        .build();
  }

  private static List<NotificationTemplateAttributeDto> toNotificationTemplateAttributeDtos(Map<String, String> attributes) {
    return attributes.entrySet().stream()
        .map(NotificationMetadataMapper::toNotificationTemplateAttributeDto)
        .collect(Collectors.toList());
  }
  private static NotificationTemplateAttributeDto toNotificationTemplateAttributeDto(Map.Entry<String, String> entry) {
    return new NotificationTemplateAttributeDto(entry.getKey(), entry.getValue());
  }

}
