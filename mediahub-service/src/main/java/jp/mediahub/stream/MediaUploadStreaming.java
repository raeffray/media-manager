package jp.mediahub.stream;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;
import jp.mediahub.exception.ResourceAlreadyExistException;
import jp.mediahub.repository.MediaRepository;
import jp.mediahub.messages.CreateMediaResponse;
import jp.mediahub.messages.Media;
import jp.mediahub.messages.MediaChunk;
import jp.mediahub.stream.container.MediaContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class that handles the streaming of media chunks and uploads them to MongoDB GridFS.
 * <p>
 * Implements the StreamObserver interface and overrides its methods onNext, onError and onCompleted.
 * <p>
 * In the onNext method, the class detects the content type of the media chunk and opens an upload stream to GridFS. The
 * media chunk is then written to the stream.
 * <p>
 * In the onError method, the class sets an error status with the error message and
 * <p>
 * in the onCompleted method, the class closes the stream and sends a successful response to the observer.
 *
 * @author Renato Raeffray
 */
public class MediaUploadStreaming implements StreamObserver<MediaChunk> {

  private static final Logger LOGGER = LoggerFactory.getLogger(MediaUploadStreaming.class);

  private final MediaContainer container;

  private OutputStream uploadStream;

  private final StreamObserver<CreateMediaResponse> observer;

  private String fileName;

  @Autowired
  private final MediaRepository mediaRepository;

//  private Function<Map<String, String>, GridFSUploadOptions> createMetadata = documentMap ->
//      new GridFSUploadOptions().metadata(new Document(documentMap));

  public MediaUploadStreaming(StreamObserver<CreateMediaResponse> observer, MediaContainer container,
      MediaRepository mediaRepository) {
    this.container = container;
    this.observer = observer;
    this.mediaRepository = mediaRepository;
  }

  @Override
  public void onNext(MediaChunk chunk) {
    try {
      // check and setup on the first chunk
      if (uploadStream == null) {
        final String originalName = chunk.getOriginalName();

        // Verify whether media already exists
        final Optional<Media> maybeMedia = mediaRepository.findMedia(originalName);
        maybeMedia.ifPresent(media -> {
          {
            throw new ResourceAlreadyExistException(
                String.format("Media already exists: fileName: [%s], hash: [%s]", media.getOriginalName(),
                    media.getHash()));
          }
        });
        this.fileName = originalName;
        this.uploadStream = container.openStreamForUpload(chunk);
      }

      this.uploadStream.write(chunk.getContent().toByteArray());

    } catch (ResourceAlreadyExistException e) {
      observer.onError(Status.ALREADY_EXISTS.withDescription(e.getMessage()).asRuntimeException());
    } catch (Exception e) {
      observer.onError(Status.UNKNOWN.asRuntimeException());
    }
  }

  @Override
  public void onError(Throwable t) {
    this.observer.onError(Status.UNKNOWN.withDescription(t.getMessage()).asRuntimeException());
  }

  @Override
  public void onCompleted() {
    LOGGER.info("File [{}] Received", this.fileName);
    if (this.uploadStream != null) {
      final Optional<Media> media = this.mediaRepository.findMedia(fileName);
      media.ifPresentOrElse(m -> {
        final String message = String.format(
            "A media was with same name was saved in middle of upload process: hash [%s]. Current file discharged",
            m.getHash());

        LOGGER.info(message);
        observer.onError(Status.ALREADY_EXISTS.withDescription(message).asRuntimeException());

      }, () -> {
        try {
          this.uploadStream.flush();
          this.uploadStream.close();

          observer.onNext(CreateMediaResponse.newBuilder().setSuccess(true).build());
          observer.onCompleted();

        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });
    }
  }
}
