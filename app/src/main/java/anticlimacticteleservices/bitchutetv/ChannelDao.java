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
public interface ChannelDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insert(Channel... channel);
    @Update
    public void update(Channel... channel);
    @Delete
    public void delete(Channel channel);

   @Query("SELECT * FROM channel")
   LiveData<List<Channel>>  getChannels();

    @Query("SELECT * FROM channel")
    List <Channel> getDeadChannels();

    @Query("Select * FROM channel WHERE source_id = :sid")
    List<Channel> getChannelsBySourceID(String sid);

//    @Query("SELECT * FROM channel WHERE ID = :id")
//    public Channel getChannelById(Long id);

//    @Query("SELECT * FROM channel WHERE bitchute_id= :id OR youtube_id = :id")
//    List<Channel> getChannelsBySourceID(String id);

 //   @Query("SELECT COUNT(*) from channel")
//    int countChannels();

//    @Insert
//    void insertAll(Channel... channel);
}
