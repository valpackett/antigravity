package com.floatboth.antigravity.data;

import java.io.*;
import retrofit.mime.TypedOutput;
import org.apache.commons.io.IOUtils;

public class TypedContent implements TypedOutput {
  private final String fileName;
  private final long fileSize;
  private final String mimeType;
  private final InputStream stream;

  public TypedContent(String fileName, long fileSize, String mimeType, InputStream stream) {
    this.fileName = fileName;
    this.fileSize = fileSize;
    this.mimeType = mimeType;
    this.stream = stream;
  }

  @Override public String mimeType() {
    return mimeType;
  }

  @Override public String fileName() {
    return fileName;
  }

  @Override public long length() {
    return fileSize;
  }

  @Override public void writeTo(OutputStream out) throws IOException {
    try {
      IOUtils.copyLarge(stream, out);
    } catch (NullPointerException ex) {}
  }
}
