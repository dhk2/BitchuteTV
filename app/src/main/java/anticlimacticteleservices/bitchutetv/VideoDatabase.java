package anticlimacticteleservices.bitchutetv;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;


@Database(entities = {Video.class}, version = 8, exportSchema = false)
public abstract class VideoDatabase extends RoomDatabase {
    private static VideoDatabase INSTANCE;

    public abstract VideoDao videoDao();

    public static VideoDatabase getSicDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(context.getApplicationContext(), VideoDatabase.class, "item_-database")
                            // allow queries on the main thread.
                            // Don't do this on a real app! See PersistenceBasicSample for an example.
                            .allowMainThreadQueries()
                            .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}
