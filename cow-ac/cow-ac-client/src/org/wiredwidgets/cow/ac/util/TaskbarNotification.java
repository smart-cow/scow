package org.wiredwidgets.cow.ac.util;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.Timer;

/**
 * Adopted from Hack #46 from "Swing Hacks" by Marinacci and Adamson
 * TODO: incomplete, needs much improved styling.
 * @author RYANMILLER
 */
public class TaskbarNotification {
    protected static final int ANIMATION_TIME = 500;  // milliseconds
    protected static final float ANIMATION_TIME_F =
            (float) ANIMATION_TIME;
    protected static final int ANIMATION_DELAY = 50;  // milliseconds

    JWindow win;
    JComponent contents;
    Rectangle desktopBounds;
    Dimension tempWinSize;
    Timer animationTimer;
    int showX, startY;
    long animationStart;
    AnimatingSheet animatingSheet;

    public TaskbarNotification() {
        initDesktopBounds();
    }

    public TaskbarNotification(JComponent contents) {
        this();
        setContents(contents);
    }

    protected void initDesktopBounds() {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        desktopBounds = env.getMaximumWindowBounds();
    }

    public void setContents(JComponent contents) {
        this.contents = contents;
        JWindow tempWindow = new JWindow();
        tempWindow.getContentPane().add(contents);
        tempWindow.pack();
        tempWinSize = tempWindow.getSize();
        tempWindow.getContentPane().removeAll();
        win = new JWindow();
        animatingSheet = new AnimatingSheet();
        animatingSheet.setSource(contents);
        win.getContentPane().add(animatingSheet);
    }

    public void showAt(int x) {
        // create a window with an animating sheet
        // copy its contents from the temp window
        // animate it
        // when done, remove animating sheet and add real contents

        showX = x;
        startY = desktopBounds.y + desktopBounds.height;

        ActionListener animationLogic = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                long elapsed =
                        System.currentTimeMillis() - animationStart;
                if (elapsed > ANIMATION_TIME) {
                    // put real contents in the window and show
                    win.getContentPane().removeAll();
                    win.getContentPane().add(contents);
                    win.pack();
                    win.setLocation(showX, startY - win.getSize().height);
                    win.setVisible(true);
                    win.repaint();
                    animationTimer.stop();
                    animationTimer = null;
                } else {
                    // calc % done
                    float progress = (float) elapsed / ANIMATION_TIME_F;
                    // get height to show
                    int animatingHeight = (int) (progress*tempWinSize.getHeight());
                    animatingHeight = Math.max( animatingHeight, 1);
                    //animatingSheet.setAnimatingHeight(animatingHeight);
                    win.pack();
                    win.setLocation(showX, startY - win.getHeight());
                    win.setVisible(true);
                    win.repaint();
                }
            }
        };  // end animationLogic
        animationTimer = new Timer(ANIMATION_DELAY, animationLogic);
        animationStart = System.currentTimeMillis();
        animationTimer.start();
    }  // end showAt()

    class AnimatingSheet extends JPanel {
        Dimension animatingSize = new Dimension (0, 1);
        JComponent source;
        BufferedImage offscreenImage;

        public AnimatingSheet () {
            super();
            setOpaque(true);
        }

        public void setSource (JComponent source) {
            this.source = source;
            animatingSize.width = source.getWidth();
            makeOffscreenImage(source);
        }

        public void setAnimatingHeight (int height) {
            animatingSize.height = height;
            setSize (animatingSize);
        }

        private void makeOffscreenImage(JComponent source) {
            GraphicsEnvironment ge =
                GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsConfiguration gfxConfig =
                ge.getDefaultScreenDevice().getDefaultConfiguration();
            offscreenImage =
                gfxConfig.createCompatibleImage(source.getWidth(),
                                                source.getHeight());
            Graphics2D offscreenGraphics =
                (Graphics2D) offscreenImage.getGraphics();
            // windows workaround
            offscreenGraphics.setColor (source.getBackground());
            offscreenGraphics.fillRect (0, 0,
                                        source.getWidth(), source.getHeight());
            // paint from source to offscreen buffer
            source.paint (offscreenGraphics);
        }

        @Override
        public Dimension getPreferredSize() { return animatingSize; }
        @Override
        public Dimension getMinimumSize() { return animatingSize; }
        @Override
        public Dimension getMaximumSize() { return animatingSize; }
        @Override

        public void update (Graphics g) {
            // override to eliminate flicker from
            // unneccessary clear
            paint (g);
        }

        @Override
        public void paint (Graphics g) {
            // get the top-most n pixels of source and
            // paint them into g, where n is height
            // (different from sheet example, which used bottom-most)
            BufferedImage fragment =
                offscreenImage.getSubimage (0, 0,
                                            source.getWidth(),
                                            animatingSize.height);
            g.drawImage (fragment, 0, 0, this);
        }
    }  // end AnimatingSheet


}
