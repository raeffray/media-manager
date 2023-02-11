package jp.mediahub.stream.container;

import java.io.IOException;
import java.io.InputStream;

/**
 * A custom InputStream class for handling media file download.
 * Extends the standard {@link InputStream} class and adds a field to store the length of the stream.
 *
 * The {@link #read()} method is overridden to read the underlying input stream.
 * A getter method {@link #getStreamLength()} is provided to retrieve the length of the stream.
 *
 * @author Renato Raeffray
 *
 */

public class DownloadStream extends InputStream {

  public DownloadStream(InputStream stream, Long streamLength) {
    this.stream = stream;
    this.streamLength = streamLength;
  }

  private Long streamLength;

  private InputStream stream;

  @Override
  public int read() throws IOException {
    return stream.read();
  }

  public Long getStreamLength() {
    return this.streamLength;
  }


}
