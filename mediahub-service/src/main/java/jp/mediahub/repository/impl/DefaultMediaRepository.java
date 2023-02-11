package jp.mediahub.repository.impl;

import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import jp.mediahub.repository.MediaRepository;
import jp.mediahub.messages.Media;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;

/**
 *
 * Repository class for managing {@link Media} objects in a MongoDB GridFS. This class provides methods for storing,
 * retrieving, and deleting media files in the GridFS.
 *
 * @author Renato Raeffray
 */
@Component
public class DefaultMediaRepository implements MediaRepository {

  @Autowired
  private GridFsTemplate gridFsTemplate;

  private static Function<GridFSFile, Media> buildMedia = gridfile ->
      Media.newBuilder()
          .setOriginalName(gridfile.getFilename())
          .setHash(gridfile.getMetadata().getString(CONTENT_HASH_KEY))
          .setSize(gridfile.getLength())
          .build();

  /**
   * This class retrieves all the media files stored in the MongoDB GridFS and returns them as a List of {@link Media}
   * objects wrapped in an {@link Optional}.
   *
   * @return an Optional containing a List of Media objects if media files are found, or an empty Optional if no media
   * files are found.
   */
  @Override
  public Optional<List<Media>> getAllMedias() {
    final GridFSFindIterable gridFSFiles = gridFsTemplate.find(new Query());
    List<Media> medias = new ArrayList<>();
    for (GridFSFile file : gridFSFiles) {
      medias.add(buildMedia.apply(file));
    }
    return Optional.of(medias);
  }

  /**
   * This a media file stored in the MongoDB GridFS and returns them as a {@link Media} objects wrapped in an
   * {@link Optional}.
   *
   * @param fileName The name of file to be found
   * @return an Optional containing a  Media object if media file is found, or an empty Optional if no media file is
   * found.
   */
  @Override
  public Optional<Media> findMedia(String fileName) {
    final GridFSFile gridFileFound = gridFsTemplate.findOne(new Query(Criteria.where("filename").is(fileName)));
    if (gridFileFound == null) {
      return Optional.empty();
    }
    return Optional.of(buildMedia.apply(gridFileFound));
  }

  /**
   * Delete a media stored in the MongoDB GridFS
   *
   * @param fileName The name of file to be deleted
   */
  @Override
  public void deleteMedia(String fileName) {
    final Query query = new Query(Criteria.where(FILENAME_KEY).is(fileName));
    gridFsTemplate.delete(query);
  }
}
