package urlshortener.repository;

import urlshortener.domain.ShortURL;

import java.util.List;

public interface ShortURLRepository {

  /**
   * Find and return ShortUrl object with id [id]
   *
   * @param id id to find
   * @return ShortUrl object with id [id] from database iff exists. Else return null (not exists in database)
   */
  ShortURL findByKey(String id);

  /**
   * Find and return a list of ShortUrl objects with target = [target]
   *
   * @param target target uri to find
   * @return List of ShortUrl objects that shortUrl[i].target = [target]
   */
  List<ShortURL> findByTarget(String target);

  /**
   * Method that stores [su] object in database.
   *
   * @param su object to store.
   * @return [su] iff store doesn't fail or DuplicateKeyException was thrown. Else return null.
   */
  ShortURL save(ShortURL su);

  /**
   * Method that update database to set safe column to [safeness] where hash = [su.hash]
   *
   * @param urlSafe  object to update
   * @param safeness to update
   * @return a copy of [su] object if update doesn't fail. Else return null (Exception).
   */
  ShortURL mark(ShortURL urlSafe, boolean safeness);

  /**
   * Method that updates ShortUrl([su.hash]) object in database with [su] object fields.
   *
   * @param su object to take fields.
   */
  void update(ShortURL su);

  /**
   * Method that deletes shortUrl([hash]) object from database.
   *
   * @param id to delete.
   */
  void delete(String id);

  /**
   * Method that returns number of ShortUrl objects in database
   *
   * @return number of ShortUrl objects in database
   */
  Long count();

  /**
   * Method that paginates and return a list with ShortUrl objects stored in database.
   *
   * @param limit  maximum length of returned list.
   * @param offset offset to start list.
   * @return list of ShortUrl objects from [offset] with [limit] elements.
   */
  List<ShortURL> list(Long limit, Long offset);

}
