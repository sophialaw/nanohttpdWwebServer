package sophialaw.nanohttpd;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.media.CamcorderProfile;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Calendar;

public class VideoRecorderOld
{
    private static int filesCount = 0;

    private static WebServer webServer;
    private static MainActivity mainActivity;
    private static Logger applog;
    //private TextView txtStatus = null;

    private static MediaRecorder mediaRecorder;
    private static Camera camera;

    private static boolean isStreaming = false;
    private static Timer timer;

    public int getFilesCount()
    {
        return filesCount;
    }

    public VideoRecorderOld(MainActivity mainActivity, WebServer webServer)
    {
        // videoRecord is setup for recording 120 mp4 files i.e. totally 2 min, each 5 s, afterward it is released together with camara, mediarecorder
        // as the web streaming is fixed to 2 min only
        // to continue to watch the streaming
        // it is required to submit request to web server again

        this.webServer = webServer;
        this.mainActivity = mainActivity;
        this.applog = mainActivity.getApplog();
        //this.txtStatus = mainActivity.gettxtStatus();

        start();
    }

    public void start()
    {
        applog.log(Level.INFO, "video start");
        mainActivity.updateStatus("VideoRecorder start");
        applog.log(Level.INFO, "video start 2");

        //Utility.cleanUpFolder(mainActivity.ViedoPath, applog);
        applog.log(Level.INFO, "after cleanUpFolder");

        mediaRecorder = new MediaRecorder();
        filesCount = 0;
        timer = new Timer();

        applog.log(Level.INFO, "video started");
        startRecording();
    }

    private class stopRecordingTask extends TimerTask
    {
        @Override
        public void run()
        {
            applog.log(Level.INFO, "timer task stop the recording");
            stopRecording();
        }

        public stopRecordingTask()
        {
            applog.log(Level.INFO, "stopRecordingTask");
            mainActivity.updateStatus("stopRecordingTask");
            this.run();
        }
    }

    private void startRecording()
    {
        if (prepareRecording())
        {
            applog.log(Level.INFO, "start recording count#" + String.valueOf(filesCount));
            mainActivity.updateStatus("start recording count#" + String.valueOf(filesCount));
            mediaRecorder.start();

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.SECOND, mainActivity.videoPerSecond);
            timer.schedule(new stopRecordingTask(), calendar.getTime());

            isStreaming = true;
        }
    }

    private void stopRecording()
    {
        applog.log(Level.INFO, "stopRecording");
        mainActivity.updateStatus("stop recording");
        mediaRecorder.stop();

        filesCount++;

        if (filesCount >= mainActivity.frameNumber)
            releaseRecorder();
        else
            startRecording();
    }

    public boolean isStreamingInProgress()
    {
        return isStreaming;
    }

    public void releaseRecorder()
    {
        applog.log(Level.INFO, "releaseRecorder");

        if (mediaRecorder != null)
        {
            mediaRecorder.release();
            mediaRecorder = null;
        }
        camera.lock();
        camera.stopPreview();
        camera.release();
        camera = null;

        timer.purge();
        timer = null;

        mainActivity.updateStatus("recorder released");
        isStreaming = false;
    }

    private boolean prepareRecording()
    {
        boolean prepared = false;
        applog.log(Level.INFO, "prepare recording for camaraid=" + String.valueOf(mainActivity.getCameraId()));
        mainActivity.updateStatus("prepare recording for camaraid=" + String.valueOf(mainActivity.getCameraId()));

        applog.log(Level.INFO, "video prepareRecording 1a");
        camera = Camera.open(mainActivity.getCameraId());
        applog.log(Level.INFO, "video prepareRecording 1b");

        applog.log(Level.INFO, "video prepareRecording 1");
        //ImageSurfaceView mImageSurfaceView = mainActivity.addImageSurfaceView(camera);
        //ImageSurfaceView mImageSurfaceView = mainActivity.addImageSurfaceView(null);
        mainActivity.addImageSurfaceView(null);

        applog.log(Level.INFO, "video prepareRecording 2");

        Camera.Parameters cameraParameters = camera.getParameters();
        cameraParameters.setRecordingHint(true);
        camera.setParameters(cameraParameters);
        camera.setDisplayOrientation(90);

        camera.unlock();
        applog.log(Level.INFO, "video prepareRecording 3");

        mediaRecorder.setCamera(camera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setProfile(CamcorderProfile.get(mainActivity.getCameraId(), CamcorderProfile.QUALITY_HIGH));

        String prepareFile = Utility.getFileName(mainActivity.filesNameFormat, filesCount);
        mediaRecorder.setOutputFile(prepareFile);
        //mediaRecorder.setPreviewDisplay(mImageSurfaceView.getHolder().getSurface());
        applog.log(Level.INFO, "video prepareRecording 4 " + prepareFile);
        mainActivity.updateStatus("prepareFile: " + prepareFile);

        try
        {
            mediaRecorder.prepare();
            prepared = true;
        }
        catch (IOException e)
        {
            applog.log(Level.SEVERE, "prepare error" + e.toString());
            mainActivity.updateStatus("prepare error" + e.toString());
        }
        return prepared;
    }
}
