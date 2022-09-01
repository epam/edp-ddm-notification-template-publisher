package com.epam.digital.data.platform.notification.utils;

import com.epam.digital.data.platform.notification.exceptions.NoFilesFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;

@ExtendWith(SpringExtension.class)
public class IOUtilsTest {

  @Test
  void testIOUtils() throws FileNotFoundException {
    File file = ResourceUtils.getFile("classpath:notifications");
    File[] fileList = IOUtils.getFileList(file);
    Assertions.assertNotNull(fileList);
  }

  @Test
  void expectExceptionTest() {
    Assertions.assertThrows(NoFilesFoundException.class, () -> {
      IOUtils.getFileList(new File("classpath:notifications/folder"));
    });
  }
}
