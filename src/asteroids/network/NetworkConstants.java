package asteroids.network;

/*
 * A lot of these are abbreviations for operation codes. In the near future, an op code will simply be a string instead of an
 * object, and the code will only be one letter. These variables make those short, arbitrary abbreviations more coder-friendly.
 */

public class NetworkConstants
{
    public final static String NEWPLAYER = "NP";
    public final static String SHIPMOVE = "SM";
    public final static String SHIPFIRE = "SF";
    public final static String SHIPDIE = "SD";
    public final static String ASTEROIDSPAWN = "AS";
    public final static String ASTEROIDDIE = "AD";
    public final static String NEXTLEVEL = "LN";
    public final static String RESTARTLEVEL = "LR";
    public final static String CONNECTIONESTABLISHED = "CE";
    public final static String GETPLAYERS = "PG";
}
