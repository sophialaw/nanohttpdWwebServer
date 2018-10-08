package sophialaw.nanohttpd;

import java.util.logging.Level;
import java.util.logging.Logger;
import android.media.MediaPlayer;

public class VideoPlayer
{
    private static MediaPlayer mPlayer;
    private static Logger appLog;

    public VideoPlayer(Logger appLog)
    {
        this.appLog = appLog;
    }

    public void playAudio(String playFlie)
    {
        try
        {
            mPlayer = new MediaPlayer();
            mPlayer.setDataSource(playFlie);
            mPlayer.prepare();

            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
            {
                @Override
                public void onCompletion(MediaPlayer mp)
                {
                    stopAudio();
                }
            });

            mPlayer.start();
        }
        catch (Exception ex)
        {
            appLog.log(Level.INFO, "playAudio err " + ex.toString() + " file may be in wrong format");
        }
    }

    public void stopAudio()
    {
        if (mPlayer != null)
        {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }
}
