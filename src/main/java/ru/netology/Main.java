package ru.netology;

import java.io.*;

public class Main {
  public static void main(String[] args) {
    try {
      new Server().start(9990);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}


