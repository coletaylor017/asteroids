package asteroids.game;

import static asteroids.game.Constants.*;
import java.awt.*;
import javax.swing.*;
import asteroids.participants.Ship;

/**
 * The area of the display in which the game takes place.
 */
@SuppressWarnings("serial")
public class Screen extends JPanel
{
    /** Legend that is displayed across the screen */
    private String legend;

    /** Game controller */
    private Controller controller;

    /** String for score label */
    private String score = "SCORE: ";

    /** String for level label */
    private String level = "LEVEL: ";

    /** String for lives label */
    private String remainingLives = "LIVES: ";

    /**
     * Creates an empty screen
     */
    public Screen (Controller controller)
    {
        this.controller = controller;
        legend = "";
        if (!controller.getGameMode().equals("classic"))
        {
            Constants.SIZE = 1000;
        }
        setPreferredSize(new Dimension(SIZE, SIZE));
        setMinimumSize(new Dimension(SIZE, SIZE));
        setBackground(Color.black);
        setForeground(Color.white);
        setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 120));
        setFocusable(true);
    }

    /**
     * Set the legend
     */
    public void setLegend (String legend)
    {
        this.legend = legend;
    }

    /**
     * Paint the participants onto this panel
     */
    @Override
    public void paintComponent (Graphics graphics)
    {
        // Use better resolution
        Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Do the default painting
        super.paintComponent(g);

        // Draw each participant in its proper place
        for (Participant p : controller)
        {
            p.draw(g);
        }

        g.setColor(Color.WHITE);

        // Draw the legend across the middle of the panel
        int size = g.getFontMetrics().stringWidth(legend);
        g.drawString(legend, (SIZE - size) / 2, SIZE / 2);

        if (controller.getGameMode().equals("classic"))
        {
            // Create font size to display score label
            Font scoreFont = new Font(Font.SANS_SERIF, Font.PLAIN, 30);
            g.setFont(scoreFont);
            g.drawString(score, LABEL_HORIZONTAL_OFFSET + 20 - g.getFontMetrics().stringWidth(score) / 2, LABEL_VERTICAL_OFFSET);

            // Create font size to display level label
            Font levelFont = new Font(Font.SANS_SERIF, Font.PLAIN, 30);
            g.setFont(levelFont);
            g.drawString(level, SIZE - LABEL_HORIZONTAL_OFFSET, LABEL_VERTICAL_OFFSET);
        }
        else // for two or more players, display stats in a list format
        {
            Font font = new Font("Times New Roman", Font.BOLD, 20);
            g.setFont(font);
            g.drawString(level, SIZE - g.getFontMetrics().stringWidth(level) - 5, 25);

            // vert offset makes sure the stats display one below another, not on top of each other
            int vertOffset = 25;

            // repeat for each player
            for (Ship s : controller.getShipList())
            {
                // color of stat will correspond to color of ship
                g.setColor(s.getColor());
                g.drawString("LIVES: " + s.getOwner().getLives(), 15, vertOffset);
                g.drawString("SCORE: " + s.getOwner().getScore(), 115, vertOffset);
                vertOffset += 25;
            }
        }
    }

    /** Set lives */
    public void setLives (String s)
    {
        this.remainingLives = s;
    }

    /** Set Score */
    public void setScoreLabel (String s)
    {

        this.score = s;

    }

    /** Set Level */
    public void setLevelLabel (String s)
    {
        this.level = s;
    }
}
