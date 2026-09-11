package com.example.bulkhead;

@FunctionalInterface
public interface DownstreamClient {
  String fetch(String request) throws InterruptedException;
}
