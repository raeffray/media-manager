package jp.mediahub.service;

import io.grpc.Status;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import java.util.List;
import java.util.Optional;
import jp.mediahub.timing.annotation.LogExecutionTime;
import jp.mediahub.messages.DeleteMediaRequest;
import jp.mediahub.messages.DeleteMediaResponse;
import jp.mediahub.repository.MediaRepository;
import jp.mediahub.messages.CreateMediaResponse;
import jp.mediahub.messages.GetMediaRequest;
import jp.mediahub.messages.ListMediaRequest;
import jp.mediahub.messages.ListMediaResponse;
import jp.mediahub.messages.Media;
import jp.mediahub.messages.MediaChunk;
import jp.mediahub.services.MediaServiceGrpc.MediaServiceImplBase;
import jp.mediahub.stream.MediaDownloadStreaming;
import jp.mediahub.stream.MediaUploadStreaming;
import jp.mediahub.stream.container.MediaContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * A gRPC service implementation class for handling Media requests. This class extends {@link MediaServiceImplBase}
 * which is the generated class by protoc compiler from the Media.proto file. Annotated with {@link Component} with a
 * value "mediaServiceApi" to indicate that this class is a Spring component and it can be used to auto-wire in other
 * classes.
 *
 * @author Renato Raeffray
 */
@Component("mediaServiceApi")
public class GRPCMediaServiceAPI extends MediaServiceImplBase {

  private static final Logger LOGGER = LoggerFactory.getLogger(GRPCMediaServiceAPI.class);

  @Autowired
  private MediaContainer mediaContainer;

  @Value("${app.media.buffer.size}")
  private int mediaBufferSize;

  @Autowired
  private MediaRepository mediaRepository;

  @Override
  @LogExecutionTime
  public void listMedias(ListMediaRequest request, StreamObserver<ListMediaResponse> responseObserver) {

    final Optional<List<Media>> maybeAllMedias = mediaRepository.getAllMedias();

    maybeAllMedias.ifPresentOrElse(medias -> {
      responseObserver.onNext(
          ListMediaResponse
              .newBuilder()
              .addAllMedias(medias)
              .build());
      responseObserver.onCompleted();
    }, () -> {
      responseObserver.onError(Status.NOT_FOUND.asRuntimeException());
    });
  }

  @Override
  public StreamObserver<MediaChunk> createStreamMedia(StreamObserver<CreateMediaResponse> responseObserver) {
    return new MediaUploadStreaming(responseObserver, mediaContainer, mediaRepository);
  }

  @Override
  @LogExecutionTime
  public void getStreamMedia(GetMediaRequest request, StreamObserver<MediaChunk> responseObserver) {
    final MediaDownloadStreaming streamObserver = new MediaDownloadStreaming((ServerCallStreamObserver)responseObserver,
        mediaContainer, mediaBufferSize);
    try {
      LOGGER.debug("Media Buffer Size [{}]", mediaBufferSize);
      streamObserver.read(request.getOriginalName());
    } catch (Exception e) {
      if(((ServerCallStreamObserver<MediaChunk>) responseObserver).isCancelled()){
        responseObserver
            .onError(Status.CANCELLED.withDescription(e.getMessage()).asRuntimeException());
      } else {
        responseObserver
            .onError(Status.UNKNOWN.withDescription(e.getMessage()).asRuntimeException());
      }
    }
  }

  @Override
  @LogExecutionTime
  public void deleteMedia(DeleteMediaRequest request, StreamObserver<DeleteMediaResponse> responseObserver) {

    final Optional<Media> maybeMedia = mediaRepository.findMedia(request.getOriginalName());

    maybeMedia.ifPresentOrElse(media -> {
      mediaRepository.deleteMedia(media.getOriginalName());
      responseObserver.onNext(DeleteMediaResponse.newBuilder().setSuccess(true).build());
      responseObserver.onCompleted();
    }, () -> responseObserver.onError(Status.NOT_FOUND.asRuntimeException()));

  }
}
