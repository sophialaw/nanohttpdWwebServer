package sophialaw.nanohttpd;

import android.hardware.Camera;
import android.media.MediaRecorder;

import java.util.logging.Level;
import java.util.logging.Logger;

public class test
{
    private static MainActivity mainActivity;
    private static Logger applog;

    private static MediaRecorder mediaRecorder;
    private static Camera camera;
    private static ImageSurfaceView mImageSurfaceView;

    private boolean recording = false;

    public test(final MainActivity mainActivity)
    {
        this.mainActivity = mainActivity;

        mainActivity.runOnUiThread(new Runnable() {
            public void run() {
                mImageSurfaceView = new ImageSurfaceView(mainActivity, camera, null);
            }
        });

        //mImageSurfaceView = new ImageSurfaceView(null, camera, null);
        mainActivity.addImageSurfaceView(mImageSurfaceView);

        //applog.log(Level.INFO, "addView");
    }
}
