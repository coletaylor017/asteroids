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

    /** Displays the users remaining lives */
    private String remainingLives;

    /** shows and updates score as asteroids are destroyed */
    private String score;

    /** Level display next to the current level */
    private String level;

    /** Game controller */
    private Controller controller;

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
        // DRAW LIVES LABEL
        Font livesFont = new Font("Times New Roman", Font.BOLD, 20);
        g.setFont(livesFont);
        g.drawString(remainingLives, 500, 50);
        // DRAW SCORE LABEL
        Font scoreFont = new Font("Times New Roman", Font.BOLD, 20);
        g.setFont(scoreFont);
        g.drawString(score, 100, 50);
        // DRAW LEVEL LABEL
        Font levelFont = new Font("Times New Roman", Font.BOLD, 20);
        g.setFont(levelFont);
        g.drawString(level, 300, 50);
    }

    public void setLives (String livesLabel)
    {
        this.remainingLives = livesLabel;
    }

    public void setScoreLabel (String score)
    {
        this.score = score;

    }

    public void setLevelLabel (String level)
    {
        this.level = level;
    }
}
