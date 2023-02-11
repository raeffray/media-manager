package jp.mediahub.stream;

import com.google.protobuf.ByteString;
import io.grpc.Status;
import io.grpc.stub.ServerCallStreamObserver;
import java.io.IOException;
import jp.mediahub.stream.container.DownloadStream;
import jp.mediahub.messages.MediaChunk;
import jp.mediahub.stream.container.MediaContainer;
import org.apache.tika.Tika;
import org.apache.tika.detect.TypeDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MediaDownloadStreamHandler class is responsible for reading media files from MongoDB gridFSBucket and streaming it to
 * the observer.
 *
 * @author Renato Raeffray
 *
 */
public class MediaDownloadStreaming {

  private static final Logger LOGGER = LoggerFactory.getLogger(MediaDownloadStreaming.class);

  private final int mediaBufferSize;

  private MediaContainer mediaContainer;

  private ServerCallStreamObserver<MediaChunk> observer;

  private final Tika typeDetector = new Tika(new TypeDetector());

  public MediaDownloadStreaming(ServerCallStreamObserver<MediaChunk> observer, MediaContainer mediaContainer,
      int mediaBufferSize) {
    this.mediaContainer = mediaContainer;
    this.observer = observer;
    this.mediaBufferSize = mediaBufferSize;
  }

  /**
   * Reads the media file with the given name from the MongoDB gridFSBucket and streams it to the observer.
   *
   * @param mediaName the name of the media file to be read
   */
  public void read(String mediaName) {

    final DownloadStream stream = mediaContainer.openStreamForDownload(mediaName);
    try {

      long fileLength = stream.getStreamLength();

      byte[] buffer = new byte[mediaBufferSize];

      int bytesRead;
      long totalBytesRead = 0;

      while (totalBytesRead < fileLength) {
        if(!observer.isCancelled()) {
          bytesRead = stream.read(buffer, 0, mediaBufferSize);
          observer.onNext(MediaChunk
              .newBuilder()
              .setContent(ByteString.copyFrom(buffer, 0, bytesRead))
              .setTotalSize(fileLength)
              .build());
          totalBytesRead += bytesRead;
        } else {
          LOGGER.debug("Download was canceled by the client");
          stream.close();
          throw new RuntimeException("Canceled");
        }
      }
      stream.close();
      observer.onCompleted();
    } catch (IOException e) {
      try {
        stream.close();
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      } finally{
        observer.onError(Status.UNKNOWN.asRuntimeException());
      }
    }
  }
}
