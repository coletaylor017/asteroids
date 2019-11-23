package asteroids.game;

import static asteroids.game.Constants.*;
import java.awt.*;
import javax.swing.*;

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
    private String score = "SCORE: "; // TODO:

    /** String for level label */
    private String level = "LEVEL: "; // TODO:

    /** String for lives label */
    private String remainingLives = "LIVES: "; // TODO:

    /**
     * Creates an empty screen
     */
    public Screen (Controller controller)
    {
        this.controller = controller;
        legend = "";
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

        // Draw the legend across the middle of the panel
        int size = g.getFontMetrics().stringWidth(legend);
        g.drawString(legend, (SIZE - size) / 2, SIZE / 2);

        // Create font size to display score label
        Font scoreFont = new Font("Times New Roman", Font.BOLD, 10);
        g.setFont(scoreFont);
        g.drawString(score, EDGE_OFFSET, EDGE_OFFSET / 2);

        // Create font size to display level label
        Font levelFont = new Font("Times New Roman", Font.BOLD, 10);
        g.setFont(levelFont);
        g.drawString(level, SIZE / 2, EDGE_OFFSET / 2);

        //Create font size to display lives label
        Font livesFont = new Font("Times New Roman", Font.BOLD, 10);
        g.setFont(livesFont);
        g.drawString(remainingLives, SIZE-EDGE_OFFSET, EDGE_OFFSET / 2);
        
        // TODO: g.drawString(score, x, y);
        // TODO: g.drawString(level, x, y);
        // TODO: g.drawString(lives, x, y);

    }

    /** Set lives */
    public void setLives (int s)
    {
        this.remainingLives = "LIVES: " + s;
    }

    /** Set Score */
    public void setScoreLabel (int s)
    {

        this.score = "SCORE: " + s;

    }

    /** Set Level */
    public void setLevelLabel (int s)
    {
        this.level = "LEVEL: " + s;
    }
}
