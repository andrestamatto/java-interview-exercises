package com.example.shutdown;

import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.Expect;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.Z_Result;

@JCStressTest
@Outcome(id = "true", expect = Expect.ACCEPTABLE, desc = "The reader observed the request.")
@Outcome(id = "false", expect = Expect.ACCEPTABLE, desc = "The reader raced before the request.")
@State
public class VolatileShutdownSignalStress {
  private final ShutdownSignal signal = new ShutdownSignal();

  @Actor
  public void requestStop() {
    signal.requestStop();
  }

  @Actor
  public void observe(Z_Result result) {
    result.r1 = signal.isStopRequested();
  }
}
