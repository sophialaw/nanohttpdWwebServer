package sophialaw.nanohttpd;

import android.hardware.Camera;
import android.media.MediaRecorder;
import android.media.CamcorderProfile;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VideoRecorder
{
    private static MainActivity mainActivity;
    private static Logger applog;

    private static MediaRecorder mediaRecorder;
    private static Camera camera;
    private static ImageSurfaceView mImageSurfaceView;

    private boolean recording = false;

    public VideoRecorder(final MainActivity mainActivity)
    {
        this.mainActivity = mainActivity;
        this.applog = mainActivity.getApplog();

        mainActivity.runOnUiThread(new Runnable() {
            public void run() {
                mImageSurfaceView = new ImageSurfaceView(mainActivity, camera, applog);
                mainActivity.addImageSurfaceView(mImageSurfaceView);
            }
        });
        //mainActivity.addImageSurfaceView(mImageSurfaceView);

        applog.log(Level.INFO, "VideoRecorder");
    }

    public void resumeRecorder()
    {
        applog.log(Level.INFO, "resumeRecorder");
        if (camera == null)
        {
            // if the front facing camera does not exist
            camera = Camera.open(mainActivity.getCameraId());
            Camera.Parameters parameters = camera.getParameters();
            parameters.set("orientation", "portrait"); // no use
            camera.setParameters(parameters);

            mImageSurfaceView.setCameraPreview(camera);
        }
    }

    public void startRecord()
    {
        applog.log(Level.INFO, "all permission granted for take video");
        mediaRecorder = new MediaRecorder();
        camera.unlock();
        mediaRecorder.setCamera(camera);

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setProfile(CamcorderProfile.get(mainActivity.getCameraId(), CamcorderProfile.QUALITY_720P));

        //File sdDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
        String date = dateFormat.format(new Date());
        //String videoFile = sdDir.getAbsolutePath() + "/zZVideo_" + date + ".mp4";

        //filesNameFormat = ViedoPath + "stream@@count@@.mp4";
        String videoFile = mainActivity.filesNameFormat.replace("@@count@@", "_" + date);

        applog.log(Level.INFO, "videoFile=" + videoFile);

        mediaRecorder.setOutputFile(videoFile);
        mediaRecorder.setMaxDuration(600000); // Set max duration 60 sec.
        mediaRecorder.setMaxFileSize(50000000); // Set max file size 50M

        //mediaRecorder.setPreviewDisplay(mImageSurfaceView.getHolder().getSurface()); no need

        try
        {
            mediaRecorder.prepare();
            mainActivity.runOnUiThread(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        mediaRecorder.start();
                    }
                    catch (final Exception ex)
                    {
                        applog.log(Level.SEVERE, "start error: " + ex.toString());
                    }
                }
            });
            recording = true;

            applog.log(Level.INFO, "now is recording");
        }
        catch (IOException e)
        {
            applog.log(Level.SEVERE, "prepare error" + e.toString());
        }
    }

    public void stopRecord()
    {
        applog.log(Level.INFO, "stopRecording");
        if (recording)
        {
            if (mediaRecorder != null)
            {
                mediaRecorder.stop();
                mediaRecorder.reset();
                mediaRecorder.release();
                mediaRecorder = null;
            }
            if (camera != null)
            {
                camera.stopPreview();
                camera.lock();
            }
            recording = false;
        }
    }

    public void pauseRecorder()
    {
        stopRecord();
        if (camera != null)
        {
            camera.release();
            camera = null;
        }
    }
}
