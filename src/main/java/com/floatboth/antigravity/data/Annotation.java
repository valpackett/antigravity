package com.floatboth.antigravity.data;

import java.io.Serializable;

public final class Annotation<T> implements Serializable {
  public String type;
  public T value;
}
