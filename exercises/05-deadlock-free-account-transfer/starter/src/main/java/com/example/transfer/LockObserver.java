package com.example.transfer;

@FunctionalInterface
interface LockObserver {
  LockObserver NONE = () -> {};

  void afterFirstLock() throws InterruptedException;
}
