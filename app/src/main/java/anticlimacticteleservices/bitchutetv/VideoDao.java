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
    public void insert(Video... feed_item);
    @Update
    public void update(Video... feed_item);
    @Delete
    public void delete(Video feed_item);


    @Query("SELECT * FROM feed_item ORDER BY date DESC")
    LiveData<List<Video>> getVideos();

    @Query("SELECT * FROM feed_item WHERE ID = :id")
    LiveData<Video> getvideoById(Long id);

    @Query("SELECT * FROM feed_item WHERE author_id = :id ORDER BY date DESC")
    List<Video> getVideosByAuthorId(Long id);

    @Query("SELECT COUNT(*) from feed_item")
    int countVideos();

    @Query("Select * FROM feed_item WHERE source_id = :sid ORDER BY date DESC")
    List<Video> getVideosBySourceID(String sid);

    @Insert
    void insertAll(Video... feed_item);

    @Query("SELECT * FROM feed_item WHERE watched=1 ORDER BY date DESC")
    List<Video> getWatchedVideos();

    @Query("SELECT * FROM feed_item WHERE watched=0 ORDER BY date DESC")
    List<Video> getUnWatchedVideos();

    @Query("SELECT * FROM feed_item ORDER BY date DESC")
    List<Video> getDeadVideos();


}
