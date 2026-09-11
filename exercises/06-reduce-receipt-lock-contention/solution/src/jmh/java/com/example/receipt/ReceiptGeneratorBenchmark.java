package com.example.receipt;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
@Threads(4)
public class ReceiptGeneratorBenchmark {
  private final ReceiptSigner signer = (sequence, document) -> sequence + document;
  private final ReceiptGenerator concurrent = new ReceiptGenerator(signer);
  private final SerialReceiptGenerator serialized = new SerialReceiptGenerator(signer);

  @Benchmark
  public Receipt concurrentSequenceAllocation() throws InterruptedException {
    return concurrent.issue("receipt");
  }

  @Benchmark
  public Receipt serializedSigning() throws InterruptedException {
    return serialized.issue("receipt");
  }

  private static final class SerialReceiptGenerator {
    private final ReceiptSigner signer;
    private final AtomicLong nextSequence = new AtomicLong(1);

    private SerialReceiptGenerator(ReceiptSigner signer) {
      this.signer = signer;
    }

    private synchronized Receipt issue(String document) throws InterruptedException {
      long sequence = nextSequence.getAndIncrement();
      return new Receipt(sequence, signer.sign(sequence, document));
    }
  }
}
