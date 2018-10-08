package sophialaw.nanohttpd;

import android.hardware.Camera;
import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImageSurfaceView extends SurfaceView implements SurfaceHolder.Callback
{
    private static Camera camera;
    private static SurfaceHolder surfaceHolder;
    private static Logger applog;

    public ImageSurfaceView(Context context, Camera camera, Logger applog)
    {
        super(context);
        applog.log(Level.INFO, "ImageSurfaceView");
        this.camera = camera;
        this.applog = applog;
        applog.log(Level.INFO, "before getHolder");
        this.surfaceHolder = getHolder();
        applog.log(Level.INFO, "after getHolder");
        this.surfaceHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        applog.log(Level.INFO, "surfaceCreated()");
        applog.log(Level.SEVERE, "SurfaceHolder done");

        try
        {
            if (camera != null)
            {
                this.camera.setPreviewDisplay(holder);
                this.camera.startPreview();
            }
        }
        catch (IOException e)
        {
            applog.log(Level.SEVERE, "surfaceCreated er:" + e.toString());
            e.printStackTrace();
        }
    }

    public void setCameraPreview(Camera camera)
    {
        try
        {
            camera.stopPreview();
        }
        catch (Exception e)
        {
            applog.log(Level.SEVERE, "setCameraPreview stopPreview er:" + e.toString());
        }

        try
        {
            this.camera = camera;
            this.camera.setPreviewDisplay(surfaceHolder);
            this.camera.startPreview();
            applog.log(Level.SEVERE, "setCameraPreview done");
        }
        catch (IOException e)
        {
            applog.log(Level.SEVERE, "setCameraPreview surfaceCreated er:" + e.toString());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        //this.camera.stopPreview();
        //this.camera.release();
        applog.log(Level.SEVERE, "surfaceDestroyed");
    }
}