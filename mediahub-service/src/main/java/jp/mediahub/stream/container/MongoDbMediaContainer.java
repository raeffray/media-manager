package jp.mediahub.stream.container;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import java.io.OutputStream;
import java.util.Map;
import java.util.function.Function;
import jp.mediahub.repository.impl.DefaultMediaRepository;
import jp.mediahub.messages.MediaChunk;
import org.bson.Document;

/**
 * MongoDB's implementation of {@link MediaContainer}
 *
 * */
public class MongoDbMediaContainer implements MediaContainer {

  public MongoDbMediaContainer(GridFSBucket gridFSBucket) {
    this.gridFSBucket = gridFSBucket;
  }

  private Function<Map<String, String>, GridFSUploadOptions> createMetadata = documentMap ->
      new GridFSUploadOptions().metadata(new Document(documentMap));

  private GridFSBucket gridFSBucket;

  @Override
  public OutputStream openStreamForUpload(MediaChunk media) {
    return gridFSBucket.openUploadStream(media.getOriginalName(),
        createMetadata.apply(Map.of(DefaultMediaRepository.CONTENT_HASH_KEY, media.getHash())));
  }

  @Override
  public DownloadStream openStreamForDownload(String mediaName) {
    final GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(mediaName);
    return new DownloadStream(gridFSDownloadStream, gridFSDownloadStream.getGridFSFile().getLength());
  }

}
