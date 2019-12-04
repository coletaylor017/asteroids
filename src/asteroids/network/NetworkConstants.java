package asteroids.network;

/*
 * A lot of these are abbreviations for operation codes. In the near future, an op code will simply be a string instead of an
 * object, and the code will only be one letter. These variables make those short, arbitrary abbreviations more coder-friendly.
 */

public class NetworkConstants
{
    public final static String NEWPLAYER = "P";
    public final static String SHIPMOVE = "M";
    public final static String SHIPFIRE = "F";
    public final static String SHIPDIE = "S";
    public final static String ASTEROIDSPAWN = "A";
    public final static String ASTEROIDDIE = "D";
    public final static String NEXTLEVEL = "N";
    public final static String NEWGAME = "G";
    public final static String RESTARTLEVEL = "R";
    public final static String CONNECTIONESTABLISHED = "C";
    public final static String GETPLAYERS = "G";
}
