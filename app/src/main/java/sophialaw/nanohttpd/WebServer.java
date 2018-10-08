package sophialaw.nanohttpd;

import android.content.Context;
import android.os.Environment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import sophialaw.nanohttpd.protocols.http.response.Response;
import sophialaw.nanohttpd.protocols.http.IHTTPSession;
import sophialaw.nanohttpd.protocols.http.request.Method;
import sophialaw.nanohttpd.protocols.http.NanoHTTPD;
import sophialaw.nanohttpd.protocols.http.response.Status;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.net.*;
import java.io.*;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;

public class WebServer extends NanoHTTPD
{
    private final static String urlIPchecker = "http://bot.whatismyipaddress.com";
    private String externalIP = "";

    private static Logger applog;
    private static MainActivity mainActivity;
    private static VideoRecorder videoRecorder;
    private static VideoPlayer videoPlayer;

    public WebServer(MainActivity mainActivity)
    {
        super(mainActivity.serverPort);
        this.mainActivity = mainActivity;
        this.applog = mainActivity.getApplog();

        mainActivity.updateStatus("Web server contructor.\"");

        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                applog.log(Level.INFO, "thread running");
                try
                {
                    try
                    {
                        URL url_name = new URL(urlIPchecker);
                        BufferedReader sc = new BufferedReader(new InputStreamReader(url_name.openStream()));
                        // reads system IPAddress
                        externalIP = sc.readLine().trim();
                        applog.log(Level.INFO, "externalIP = " + externalIP);
                    }
                    catch (Exception e)
                    {
                        externalIP = "Cannot Execute Properly " + e.toString();
                        applog.log(Level.INFO, "error = " + externalIP);
                    }
                }
                catch (Exception e)
                {
                    externalIP = "Cannot Execute Properly2 " + e.toString();
                    applog.log(Level.INFO, "error = " + externalIP);
                }
            }
        });
        thread.start();
    }

    public String getAvailableAction()
    {
        // available browser actions to server
        // 183.111.111.111:8080?action=stream - server record video and send the stream to browser
        // 183.111.111.111:8080?action=log - server send latest log to browser
        // 183.111.111.111:8080?action=time - server send server time to browser
        // 183.111.111.111:8080?action=info - server send the information it displays to browser which include internal and external IP port etc to browser
        // 183.111.111.111:8080?action=voice - server shows and voice upload form to browser and accept voice upload, call audio player to play back
        return "record: record video \n" + "info: get server info\n" + "log: check latest log\n" + "time: get server time\n" + "voice: upload voice to server\n" + "video: get latest video\n" + "clean: to clean up uploaded and recorded files\n";
    }

    public String getExternal_IP()
    {
        return externalIP;
    }

    public String getLocal_IP()
    {
        String currentHostIpAddress = "";
        Enumeration<NetworkInterface> netInterfaces = null;
        try
        {
            netInterfaces = NetworkInterface.getNetworkInterfaces();

            while (netInterfaces.hasMoreElements())
            {
                NetworkInterface ni = netInterfaces.nextElement();
                Enumeration<InetAddress> address = ni.getInetAddresses();
                while (address.hasMoreElements()) {
                    InetAddress addr = address.nextElement();
                    if (!addr.isLoopbackAddress() && addr.isSiteLocalAddress() && !(addr.getHostAddress().indexOf(":") > -1))
                    {
                        currentHostIpAddress = addr.getHostAddress();
                    }
                }
            }
            if (currentHostIpAddress.equals(""))
            {
                currentHostIpAddress = "127.0.0.1";
            }

        }
        catch (SocketException e)
        {
            currentHostIpAddress = "127.0.0.1";
        }
        return currentHostIpAddress;
    }

    @Override
    public void start() throws IOException
    {
        super.start();
        if (videoRecorder != null)
            videoRecorder.resumeRecorder();
    }

    @Override
    public void stop()
    {
        super.stop();
        if (videoRecorder != null)
            videoRecorder.pauseRecorder();
        if (videoPlayer != null)
            videoPlayer.stopAudio();

        mainActivity.updateStatus("Webserver stopped.");
    }

    @Override
    public Response serve(IHTTPSession session)
    {
        mainActivity.updateStatus("Webserver Response serve");
        applog.log(Level.INFO, "Webserver Response serve");

        boolean isResponseText = true;
        boolean isMedia = true;
        FileInputStream fs = null;

        Method method = session.getMethod();
        String uri = session.getUri();
        applog.log(Level.INFO, "http method: " + method + " uri='" + uri + "' ");

        String msg = "<html><body><h1>server " + externalIP + ":" + String.valueOf(mainActivity.serverPort) + "</h1>\n";
        Map<String, String> parms = session.getParms();
        Map<String, String> files = new HashMap<String, String>();

        if (Method.POST.equals(method) || Method.PUT.equals(method))
        {
            try
            {
                session.parseBody(files);
            }
            catch (Exception e)
            {
                applog.log(Level.SEVERE, "error on parseBody " +e.toString());
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
            String date = dateFormat.format(new Date());
            String audioFile = mainActivity.audioFilesNameFormat.replace("@@count@@", "_" + date);

            File file = new File(files.get("file"));
            if (Utility.saveFS(file, audioFile, applog))
                applog.log(Level.INFO, "got upload file " + file.getAbsolutePath());
            else
                applog.log(Level.INFO, "got upload file fail " + file.getAbsolutePath());

            // play music
            if (videoPlayer == null)
            {
                videoPlayer = new VideoPlayer(applog);
                applog.log(Level.INFO, "VideoPlayer");
            }
            videoPlayer.playAudio(audioFile);

            //return Response.newChunkedResponse(Status.OK, "audio/mp3", fs);
            msg += "<p>getAvailable Action = <br>" + getAvailableAction() + "</p> file uploaded and being played";
            return Response.newFixedLengthResponse(msg);
        }
        else
        {
            // get
            String action = parms.get("action");
            applog.log(Level.INFO, "action = " + action);

        /*
        if (parms.get("username") == null)
        {
            msg += "<form action='?' method='get'>\n" + "  <p>Your name: <input type='text' name='username'></p>\n" + "</form>\n";
        }
        else
        {
            msg += "<p>Hello, " + parms.get("username") + "!</p>";
        }*/

            if (parms.get("action") == null)
                msg += "no action found<p>" + getAvailableAction();
            else
            {
                //msg += "<p>action = " + parms.get("action") + "!</p>";
                //msg += "<p>action = " + action + " !</p>";
                msg += "<p>getAvailable Action = <br>" + getAvailableAction() + "</p>";
                switch (action)
                {
                    case "upload":
                        msg += "<form  name=a enctype='multipart/form-data'  method=post ><input type=file name=file /><input type=hidden name=action value=uploadfile /><input type=submit value=upload /></form>";
                        break;
                    case "clean":
                        msg += "clean request recived<p>";
                        Utility.cleanUpFolder(mainActivity.ViedoPath, applog);
                        Utility.cleanUpFolder(mainActivity.AudioPath, applog);
                        break;
                    case "time":
                        msg += "<p>time = " + new java.util.Date() + "!</p>";
                        break;
                    case "info":
                        break;
                    case "log":
                        fs = Utility.getLatestFile(mainActivity.logPath, applog);
                    /*
                    File sdDir1 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                    String file = sdDir1.getAbsolutePath() + "/4.txt";
                    fs = Utility.getFS(file, applog);*/
                        isMedia = false;
                        isResponseText = false;
                        break;
                    case "record":
                        applog.log(Level.INFO, "web server record video");
                        msg += "record video...<p>";
                        applog.log(Level.INFO, "web server record video 1");

                        if (videoRecorder == null)
                        {
                            videoRecorder = new VideoRecorder(mainActivity);
                            applog.log(Level.INFO, "web server record video null 2");
                        }
                        else
                        {
                            applog.log(Level.INFO, "web server record video not null");
                            videoRecorder.resumeRecorder();
                        }

                        applog.log(Level.INFO, "web server record video 2");
                        videoRecorder.startRecord();
                        applog.log(Level.INFO, "web server record video request send");
                        break;
                    case "video":
                        applog.log(Level.INFO, "web server return video");
                        msg += "stop record video<p>";
                        if (videoRecorder != null)
                            videoRecorder.stopRecord();

                        isResponseText = false;
                        //File sdDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                        //String videoFile = sdDir.getAbsolutePath() + "/zZVideo_20181707031757.mp4";
                        //fs = Utility.getFS(videoFile, applog);
                        fs = Utility.getLatestFile(mainActivity.ViedoPath, applog);
                        break;
                }
            }

            Map<String, String> headers = session.getHeaders();
            String clientIp = headers.get("http-client-ip");
            applog.log(Level.INFO, "client " + clientIp + " request action = " + parms.get("action"));
            mainActivity.updateStatus("client: " + clientIp + " request action=: " + parms.get("action"));

            msg += "client " + clientIp + "<br>request action = " + action +"<p>";
            msg += "</body></html>\n";
            applog.log(Level.INFO, "serving msg=" + msg);

            //VideoRecorder v = new VideoRecorder();
            //test t = new test(mainActivity);

            if (isResponseText)
                return Response.newFixedLengthResponse(msg);
            else
            {
                if (isMedia)
                    return Response.newChunkedResponse(Status.OK, "video/mp4", fs);
                else
                    return Response.newChunkedResponse(Status.OK, "text/xml", fs);
            }
        }
    }
}
