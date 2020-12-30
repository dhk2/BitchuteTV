package anticlimacticteleservices.bitchutetv;

import android.os.AsyncTask;

import androidx.room.Room;

import java.util.ArrayList;
import java.util.List;
public class loadDB extends AsyncTask<Void,Void,Void> {
    private VideoDao videoDao;
    @Override
    protected Void doInBackground(Void... videos) {
        VideoDao videoDao;
        VideoDatabase videoDatabase;
        videoDatabase = Room.databaseBuilder(MainActivity.data.getContext(), VideoDatabase.class, "mydb")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();
        videoDao = videoDatabase.videoDao();
        /*
        List<WebVideo> vdog = videoDao.getVideos();
        ArrayList<WebVideo> vcat = (ArrayList) vdog;
        MainActivity.data.setAllVideos(vcat);
        */
        return null;
    }
}
