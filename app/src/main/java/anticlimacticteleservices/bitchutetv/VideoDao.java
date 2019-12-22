package anticlimacticteleservices.bitchutetv;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface VideoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insert(WebVideo... feed_item);
    @Update
    public void update(WebVideo... feed_item);
    @Delete
    public void delete(WebVideo feed_item);


    @Query("SELECT * FROM feed_item ORDER BY date DESC")
    LiveData<List<WebVideo>> getVideos();

    @Query("SELECT * FROM feed_item WHERE ID = :id")
    LiveData<WebVideo> getvideoById(Long id);

    @Query("SELECT * FROM feed_item WHERE author_id = :id ORDER BY date DESC")
    List<WebVideo> getVideosByAuthorId(Long id);

    @Query("SELECT COUNT(*) from feed_item")
    int countVideos();

    @Query("Select * FROM feed_item WHERE source_id = :sid ORDER BY date DESC")
    List<WebVideo> getVideosBySourceID(String sid);

    @Insert
    void insertAll(WebVideo... feed_item);

    @Query("SELECT * FROM feed_item WHERE watched=1 ORDER BY date DESC")
    List<WebVideo> getWatchedVideos();

    @Query("SELECT * FROM feed_item WHERE watched=0 ORDER BY date DESC")
    List<WebVideo> getUnWatchedVideos();

    @Query("SELECT * FROM feed_item ORDER BY date DESC")
    List<WebVideo> getDeadVideos();


}
