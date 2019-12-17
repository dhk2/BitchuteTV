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
        List<Video> vdog = videoDao.getVideos();
        System.out.println("size of retireved allVideos videos "+vdog.size());
        ArrayList<Video> vcat = (ArrayList) vdog;
        System.out.println("calling from loaddb");
        MainActivity.data.setAllVideos(vcat);
        */
        return null;
    }
}
