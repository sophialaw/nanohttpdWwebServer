package sophialaw.nanohttpd;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.*;
import java.io.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity
{
    public final static int serverPort = 8080;
    private WebServer server;
    private TextView txtInfo = null;
    private TextView txtStatus = null;
    private FrameLayout cameraPreviewLayout = null;
    //private ImageSurfaceView mImageSurfaceView = null;

    private final static Logger applog = Logger.getLogger(MainActivity.class.getName());
    private final boolean logToFile = true;
    private final boolean showProgress = false;

    private final static String sdDir = Environment.getExternalStorageDirectory().getAbsolutePath();
    private final static String appFolder = sdDir + "/WebServerRecorder/";
    public final static String logPath = appFolder + "log/";
    public final static String logFilesNameFormat = logPath + "@@count@@.txt";
    public final static String ViedoPath = appFolder + "recorded/";
    public final static String filesNameFormat = ViedoPath + "stream@@count@@.mp4";

    public final static String AudioPath = appFolder + "uploaded/";
    public final static String audioFilesNameFormat = AudioPath + "stream@@count@@.mp3";

    private static final int REQUEST_PERMISSION = 1;
    private boolean gaint_CAMARA_PERMISSION = false;
    private boolean gaint_AUDIO_PERMISSION = false;
    private boolean gaint_SD_READ_PERMISSION = false;
    private boolean gaint_SD_WRITE_PERMISSION = false;

    public int a = 0;

    //private Camera camera;
    private int cameraId = -1;

    //private MediaRecorder mediaRecorder;
    //private boolean recording = false;
    //private boolean prepared = false;
    //private String mFileName = "";

    public final static int videoPerSecond = 5;
    //public final static int frameNumber = 12 * 2; // 2 min per each streaming totally
    public final static int frameNumber = 1;

    public Logger getApplog()
    {
        return applog;
    }

    public int getCameraId()
    {
        return cameraId;
    }
/*
    public FrameLayout getcameraPreviewLayout()
    {
        return cameraPreviewLayout;
    }

    public TextView gettxtStatus()
    {
        return txtStatus;
    }
*/

    private void prepare()
    {
        // with required features and permission
        // start server
        applog.log(Level.INFO, "prepare()");
        if (hasPermission())
        {
            if (server == null)
            {
                applog.log(Level.INFO, "to new a server ");
                //txtStatus.setText(txtStatus.getText() + " to new a server");
                server = new WebServer(this);
            }

            boolean serverError = false;
            try
            {
                applog.log(Level.INFO, "to start server ");
                //txtStatus.setText(txtStatus.getText() + " to start server");
                server.start();
            }
            catch (IOException e)
            {
                serverError = true;
                txtStatus.setText("Sever start error " + e.toString());
            }

            if (!serverError)
            {
                txtStatus.setText(txtStatus.getText() + " Web server initialized.");
            }
        }
    }

    private int findFrontFacingCamera()
    {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++)
        {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
            {
                applog.log(Level.INFO, "Camera found");
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    private boolean hasPermission()
    {
        return gaint_CAMARA_PERMISSION && gaint_AUDIO_PERMISSION && gaint_SD_READ_PERMISSION && gaint_SD_WRITE_PERMISSION;
    }

    @Override
    protected void onPause()
    {
        applog.log(Level.INFO, "onPause");
        // stop videoRecorder and WebServer

        if (server != null)
            server.stop();

        super.onPause();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        applog.log(Level.INFO, "onResume");
        prepare();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtInfo = (TextView)findViewById(R.id.txtInfo);
        txtStatus = (TextView)findViewById(R.id.txtStatus);
        cameraPreviewLayout = (FrameLayout)findViewById(R.id.camera_preview);
        applog.log(Level.INFO, "oncreate");

        int iApp = Utility.createFolderIfNotExist(appFolder, applog);
        int iLog = Utility.createFolderIfNotExist(logPath, applog);
        int iV = Utility.createFolderIfNotExist(ViedoPath, applog);
        int iA = Utility.createFolderIfNotExist(AudioPath, applog);

        if (logToFile)
        {
            try
            {
                FileHandler files = new FileHandler(logPath + "%g.txt", 2048, 10, true);
                applog.addHandler(files);
            }
            catch (Exception ex)
            {
                Toast.makeText(getBaseContext(),"fail to assign handler to file for logging: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        applog.log(Level.INFO, "appFolder=" + appFolder);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA))
        {
            Toast.makeText(this, "No camera on this device", Toast.LENGTH_LONG).show();
            applog.log(Level.INFO, "No camera on this device");
        }
        else
        {
            cameraId = findFrontFacingCamera();
            if (cameraId < 0)
            {
                Toast.makeText(this, "No front facing camera found.",Toast.LENGTH_LONG).show();
                applog.log(Level.INFO, "No front facing camera found");
            }
            else
            {
                boolean[] gaint_PERMISSION = Utility.hasPermission(new String[] {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, this, REQUEST_PERMISSION, applog);
                gaint_CAMARA_PERMISSION = gaint_PERMISSION[0];
                gaint_AUDIO_PERMISSION = gaint_PERMISSION[1];
                gaint_SD_READ_PERMISSION = gaint_PERMISSION[2];
                gaint_SD_WRITE_PERMISSION = gaint_PERMISSION[3];
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        applog.log(Level.INFO, "onRequestPermissionsResult");

        switch (requestCode)
        {
            case REQUEST_PERMISSION:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0)
                {
                    for (int i=0; i<permissions.length; i++)
                    {
                        if (permissions[i].equals(Manifest.permission.CAMERA))
                        {
                            if (grantResults[i] == PackageManager.PERMISSION_GRANTED)
                            {
                                gaint_CAMARA_PERMISSION = true;
                                applog.log(Level.INFO, "gaint_CAMARA_PERMISSION = true");
                            }
                        }
                        else if (permissions[i].equals(Manifest.permission.RECORD_AUDIO))
                        {
                            if (grantResults[i] == PackageManager.PERMISSION_GRANTED)
                            {
                                gaint_AUDIO_PERMISSION = true;
                                applog.log(Level.INFO, "gaint_AUDIO_PERMISSION = true");
                            }
                        }
                        else if (permissions[i].equals(Manifest.permission.READ_EXTERNAL_STORAGE))
                        {
                            if (grantResults[i] == PackageManager.PERMISSION_GRANTED)
                            {
                                gaint_SD_READ_PERMISSION = true;
                                applog.log(Level.INFO, "gaint_SD_READ_PERMISSION = true");

                            }
                        }
                        else if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                        {
                            if (grantResults[i] == PackageManager.PERMISSION_GRANTED)
                            {
                                gaint_SD_WRITE_PERMISSION = true;
                                applog.log(Level.INFO, "gaint_SD_WRITE_PERMISSION = true");
                            }
                        }

                        applog.log(Level.INFO, "android permission name " + Manifest.permission.RECORD_AUDIO + " " + Manifest.permission.READ_EXTERNAL_STORAGE + " " + Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        applog.log(Level.INFO, "on request result for permission " + permissions[i] + " result =" + String.valueOf(grantResults[i]) + " " + String.valueOf(PackageManager.PERMISSION_GRANTED));
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED)
                            applog.log(Level.INFO, "granted");
                        else
                            applog.log(Level.INFO, "not granted");
                    }
                }
                break;
            }
        }

        prepare();
    }

    public void showHelp(View v)
    {
        applog.log(Level.INFO, "show help");
        if (server == null)
            applog.log(Level.INFO, "server is null");
        else
            txtInfo.setText("External IP: " + server.getExternal_IP() + "\nInternal Ip: " + server.getLocal_IP() + "\nPort: " + String.valueOf(serverPort) + "\nActions:\n" + server.getAvailableAction());
    }

    public void addImageSurfaceView(final ImageSurfaceView mImageSurfaceView)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                applog.log(Level.INFO, "addImageSurfaceView thread");
                cameraPreviewLayout.addView(mImageSurfaceView);
            }
        });
    }

    public void updateStatus(final String msg)
    {
        if (showProgress)
        {
            //runOnUiThread(runUpdateStatus);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txtStatus.setText(txtStatus.getText() + "\n" + msg);
                }
            });
        };
    }

    /*
    private Runnable runUpdateStatus = new Runnable()
    {
        @Override
        public void run()
        {
            mySTR = absolutePath+path+"\t"+event;
            txtStatus.setText(txtStatus.getText() + "\n" + mySTR);
        }
    };*/
}
