package main.java.log;

import java.io.*;
import java.util.Collection;
import java.util.Properties;
import java.util.logging.*;

import main.java.configs.RemotePeerInfo;

public class FSLogger {
    private static final String CONF = "/logger.properties";
    private static final FSLogger fsLogger = new FSLogger (Logger.getLogger("Computer Network"));
	private Logger myLogger;

    static {
        try{
        	InputStream stream = FSLogger.class.getResourceAsStream(CONF);
            LogManager.getLogManager().readConfiguration(stream);
            stream.close();
        } catch (Exception e) {}
    }

    /* Constructor */

    private FSLogger (Logger log) {
        myLogger = log;
    }

    public static FSLogger getLogger () {
        return fsLogger;
    }

    public static void configure(int id) throws IOException {

        Properties properties = new Properties();
        properties.load(FSLogger.class.getResourceAsStream(CONF));

        Handler handler = new FileHandler("peer_" + id + ".log");
        handler.setFormatter(new SimpleFormatter());
        handler.setLevel(Level.parse(properties.getProperty("java.util.logging.FileHandler.level")));

        fsLogger.myLogger.addHandler(handler);
    }


    public static String peersToString (Collection<RemotePeerInfo> peers) {
        StringBuilder sb = new StringBuilder ("");
        boolean isFirst = true;
        for (RemotePeerInfo peer : peers) {

            if (isFirst) {
                isFirst = false;
            }
            else {
                sb.append(", ");
            }
            sb.append(peer.peerID);
        }
        return sb.toString();
    }

    /* MESSAGE */

    public synchronized void conf (String msg) { myLogger.log(Level.CONFIG, msg); }

    public synchronized void debug (String msg) { myLogger.log(Level.INFO, msg); }

    public synchronized void info (String msg) { myLogger.log (Level.INFO, msg); }

    public synchronized void severe (String msg) { myLogger.log(Level.SEVERE, msg); }

    public synchronized void warning (String msg) { myLogger.log(Level.WARNING, msg); }

    /* ERROR */
	public static String getStackTraceString (Throwable t) {
        final Writer sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    public synchronized void severe (Throwable e) { myLogger.log(Level.SEVERE, getStackTraceString (e)); }

    public synchronized void warning (Throwable e) { myLogger.log(Level.WARNING, getStackTraceString (e)); }

}
