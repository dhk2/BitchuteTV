package anticlimacticteleservices.bitchutetv;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;


@Database(entities = {Channel.class}, version = 6, exportSchema = false)
public abstract class ChannelDatabase extends RoomDatabase {
    private static anticlimacticteleservices.bitchutetv.ChannelDatabase INSTANCE;

    public abstract ChannelDao ChannelDao();

    public static anticlimacticteleservices.bitchutetv.ChannelDatabase getChannelDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(context.getApplicationContext(),ChannelDatabase.class, "channel")
        // allow queries on the main thread.
                            // Don't do this on a real app! See PersistenceBasicSample for an example.
                           .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}

