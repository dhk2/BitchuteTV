package anticlimacticteleservices.bitchutetv;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.util.Log;

public class SicSync extends JobService {
   public static Context context;
    @Override
    public boolean onStartJob(JobParameters params) {
        Log.v("SicSync","woohoo, starting sync in background");
        context = getApplication().getApplicationContext();
        new CheckForUpdates().execute();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }

}