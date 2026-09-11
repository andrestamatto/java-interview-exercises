package com.example.shutdown;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("acceptance")
class ShutdownSignalAcceptanceTest {
  @Test
  void stopRequestIsPublishedWithVolatileSemantics() throws NoSuchFieldException {
    Field stopRequested = ShutdownSignal.class.getDeclaredField("stopRequested");

    assertThat(Modifier.isVolatile(stopRequested.getModifiers())).isTrue();
  }
}
