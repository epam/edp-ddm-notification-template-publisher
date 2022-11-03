package com.epam.digital.data.platform.notification.service;

public enum NotificationChannel {

  EMAIL("email"),
  DIIA("diia"),
  INBOX("inbox");

  private final String channelName;

  NotificationChannel(String channelName) {
    this.channelName = channelName;
  }

  public String getChannelName() {
    return channelName;
  }
}
