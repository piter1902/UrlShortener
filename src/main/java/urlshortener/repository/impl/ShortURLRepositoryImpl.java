package urlshortener.repository.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import urlshortener.domain.ShortURL;
import urlshortener.repository.ShortURLRepository;

import java.util.Collections;
import java.util.List;

@Repository
public class ShortURLRepositoryImpl implements ShortURLRepository {

  private static final Logger log = LoggerFactory
          .getLogger(ShortURLRepositoryImpl.class);

  /**
   * Object to map from database row to ShortUrl object
   */
  private static final RowMapper<ShortURL> rowMapper =
          (rs, rowNum) -> new ShortURL(rs.getString("hash"), rs.getString("target"),
                  null, rs.getString("sponsor"), rs.getString("created"),
                  rs.getString("owner"), rs.getInt("mode"),
                  rs.getBoolean("safe"), rs.getString("ip"),
                  rs.getString("country"), rs.getString("qruri"));

  private final JdbcTemplate jdbc;

  /**
   * Public constructor
   *
   * @param jdbc JDBC template
   */
  public ShortURLRepositoryImpl(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  /**
   * Find and return ShortUrl object with id [id]
   *
   * @param id id to find
   * @return ShortUrl object with id [id] from database iff exists. Else return null (not exists in database)
   */
  @Override
  public ShortURL findByKey(String id) {
    try {
      return jdbc.queryForObject("SELECT * FROM shorturl WHERE hash=?",
              rowMapper, id);
    } catch (Exception e) {
      log.debug("When select for key {}", id, e);
      return null;
    }
  }

  /**
   * Method that stores [su] object in database.
   *
   * @param su object to store.
   * @return [su] iff store doesn't fail or DuplicateKeyException was thrown. Else return null.
   */
  @Override
  public ShortURL save(ShortURL su) {
    try {
      jdbc.update("INSERT INTO shorturl VALUES (?,?,?,?,?,?,?,?,?,?)",
              su.getHash(), su.getTarget(), su.getSponsor(),
              su.getCreated(), su.getOwner(), su.getMode(), su.getSafe(),
              su.getIP(), su.getCountry(), su.getQrCode());
    } catch (DuplicateKeyException e) {
      log.debug("When insert for key {}", su.getHash(), e);
      return su;
    } catch (Exception e) {
      log.debug("When insert", e);
      return null;
    }
    return su;
  }

  /**
   * Method that update database to set safe column to [safeness] where hash = [su.hash]
   *
   * @param su       object to update
   * @param safeness to update
   * @return a copy of [su] object if update doesn't fail. Else return null (Exception).
   */
  @Override
  public ShortURL mark(ShortURL su, boolean safeness) {
    try {
      jdbc.update("UPDATE shorturl SET safe=? WHERE hash=?", safeness,
              su.getHash());
      return new ShortURL(
              su.getHash(), su.getTarget(), su.getUri(), su.getSponsor(),
              su.getCreated(), su.getOwner(), su.getMode(), safeness,
              su.getIP(), su.getCountry(), su.getQrCode()
      );
    } catch (Exception e) {
      log.debug("When update", e);
      return null;
    }
  }

  /**
   * Method that updates ShortUrl([su.hash]) object in database with [su] object fields.
   *
   * @param su object to take fields.
   */
  @Override
  public void update(ShortURL su) {
//    System.err.println("----> Updating su.qrCode = " + su.getQrCode());
    try {
      jdbc.update(
              "update shorturl set target=?, sponsor=?, created=?, owner=?, mode=?, safe=?, ip=?, country=?, qruri=? where hash=?",
              su.getTarget(), su.getSponsor(), su.getCreated(),
              su.getOwner(), su.getMode(), su.getSafe(), su.getIP(),
              su.getCountry(), su.getQrCode(), su.getHash());
    } catch (Exception e) {
      log.debug("When update for hash {}", su.getHash(), e);
    }
  }

  /**
   * Method that deletes shortUrl([hash]) object from database.
   *
   * @param hash to delete.
   */
  @Override
  public void delete(String hash) {
    try {
      jdbc.update("delete from shorturl where hash=?", hash);
    } catch (Exception e) {
      log.debug("When delete for hash {}", hash, e);
    }
  }

  /**
   * Method that returns number of ShortUrl objects in database
   *
   * @return number of ShortUrl objects in database
   */
  @Override
  public Long count() {
    try {
      return jdbc.queryForObject("select count(*) from shorturl",
              Long.class);
    } catch (Exception e) {
      log.debug("When counting", e);
    }
    return -1L;
  }

  /**
   * Method that paginates and return a list with ShortUrl objects stored in database.
   *
   * @param limit  maximum length of returned list.
   * @param offset offset to start list.
   * @return list of ShortUrl objects from [offset] with [limit] elements.
   */
  @Override
  public List<ShortURL> list(Long limit, Long offset) {
    try {
      return jdbc.query("SELECT * FROM shorturl LIMIT ? OFFSET ?",
              new Object[]{limit, offset}, rowMapper);
    } catch (Exception e) {
      log.debug("When select for limit {} and offset {}", limit, offset, e);
      return Collections.emptyList();
    }
  }

  /**
   * Find and return a list of ShortUrl objects with target = [target]
   *
   * @param target target uri to find
   * @return List of ShortUrl objects that shortUrl[i].target = [target]. If no objects found, list will be empty.
   */
  @Override
  public List<ShortURL> findByTarget(String target) {
    try {
      return jdbc.query("SELECT * FROM shorturl WHERE target = ?",
              new Object[]{target}, rowMapper);
    } catch (Exception e) {
      log.debug("When select for target " + target, e);
      return Collections.emptyList();
    }
  }
}
