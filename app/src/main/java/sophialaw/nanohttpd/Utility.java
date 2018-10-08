package sophialaw.nanohttpd;

import android.app.Activity;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.security.Permission;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import android.support.v4.app.ActivityCompat;
import android.content.pm.PackageManager;
import android.content.Context;

public class Utility
{
    public static boolean saveFS(File f, String savePath, Logger appLog)
    {
        boolean saved = false;
        try
        {
            InputStream fi = new FileInputStream(f);
            byte[] b = new byte[fi.available()];
            fi.read(b);
            appLog.log(Level.INFO, "uploaded file size " + String.valueOf(b.length));

            OutputStream os = new FileOutputStream(savePath);
            os.write(b);

            fi.close();
            os.close();
            saved = true;
        }
        catch (Exception ex)
        {
            appLog.log(Level.SEVERE, "saveFS ex: " + ex.toString());
        }
        return saved;
    }


    public static FileInputStream getLatestFile(String folder, Logger appLog)
    {
        String latestFileName = "";
        File file = new File(folder);
        if (file.exists() && file.isDirectory())
        {
            File[] files = file.listFiles();
            if (files.length > 1)
            {
                String[] fileNames = new String[files.length];
                for (int i=0; i<fileNames.length; i++)
                {
                    fileNames[i] = files[i].getAbsolutePath();
                }
                Arrays.sort(fileNames);
                latestFileName = fileNames[fileNames.length-1];
            }
            else if (files.length == 1)
                latestFileName = files[0].getAbsolutePath();
        }
        return getFS(latestFileName, appLog);
    }

    public static FileInputStream getFS(String file, Logger appLog)
    {
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(file);
        }
        catch (Exception e)
        {
            appLog.log(Level.SEVERE, e.toString());
        }
        return fis;
    }

    public static String getFileName(String filePath, int filesCount)
    {
        //filesNameFormat = ViedoPath + "stream@@count@@.mp4";
        String strCnt = String.valueOf(filesCount).toString();
        if (strCnt.length() == 1)
            strCnt = "00" + strCnt;
        else if (strCnt.length() == 2)
            strCnt = "0" + strCnt;
        return filePath.replace("@@count@@", strCnt);
    }

    public static void cleanUpFolder(String path, Logger appLog)
    {
        appLog.log(Level.INFO, "cleanUpFolder start " + path);
        File file = new File(path);
        if (file.exists() && file.isDirectory())
        {
            File[] files = file.listFiles();
            if (files != null)
            {
                for(File f: files)
                {
                    if (f.isDirectory())
                    {
                        cleanUpFolder(f.getAbsolutePath(), appLog);
                    }
                    f.delete();
                }
            }
        }
        appLog.log(Level.INFO, "cleanUpFolder done");
    }

    public static boolean checkIfFileFolderExist(String path)
    {
        File file = new File(path);
        return file.exists();
    }

    public static int createFolderIfNotExist(String folderPath, Logger log)
    {
        int done = 0;
        File file = new File(folderPath);
        if (!file.exists())
        {
            try
            {
                file.mkdir();
                done = 1;
            }
            catch (Exception ex)
            {
                logMsg(ex.toString(), log, Level.SEVERE);
                done = -1;
            }
        }
        return done;
    }

    private static void logMsg(String msg, Logger log, Level level)
    {
        if (log != null)
        {
            log.log(level, msg);
        }
    }

    public static int tryParseInt(String intStr, Logger log)
    {
        int i = 0;
        try
        {
            i = Integer.parseInt(intStr);
        }
        catch (Exception ex)
        {
            logMsg(ex.toString(), log, Level.SEVERE);
        }
        return i;
    }

    private static boolean[] initBool(int length, boolean val)
    {
        boolean[] OK = new boolean[length];
        for (int i=0; i<OK.length; i++)
        {
            OK[i] = val;
        }
        return OK;
    }

    public static boolean[] hasPermission(String[] permission, Activity activity, int requestPermissionCode, Logger log)
    {
        boolean[] OK = initBool(permission.length, false);
        logMsg("hasPermission", log, Level.INFO);

        if (android.os.Build.VERSION.SDK_INT >= 23)
        {
            String seekPermission = "";
            for (int i = 0; i< permission.length; i++)
            {
                int intPermission = activity.checkSelfPermission(permission[i]);
                logMsg("intPermission =  " + String.valueOf(intPermission), log, Level.INFO);

                if (intPermission == PackageManager.PERMISSION_GRANTED)
                {
                    logMsg("already has permission for " + permission[i], log, Level.INFO);
                    OK[i] = true;
                }
                else
                {
                    logMsg("try to ask permission for " + permission[i], log, Level.INFO);
                    seekPermission += permission[i] + ";";
                }
            }
            if (seekPermission != "")
            {
                logMsg("go request permission " + seekPermission.split(";").toString(), log, Level.INFO);
                activity.requestPermissions(seekPermission.split(";"), requestPermissionCode);
            }
        }
        else
            OK = initBool(permission.length, true);

        return OK;
    }
}
