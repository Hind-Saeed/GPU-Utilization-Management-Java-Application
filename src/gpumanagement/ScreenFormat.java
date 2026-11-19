import java.io.*;
import java.util.*;
import javax.swing.*;
import gpumanagement.*;
import java.awt.*;
import java.awt.event.*;

public class ScreenFormat extends JFrame{
	// method to clear the screen and move to the next screen
    public void clearScreen(int rows, int columns) {
        getContentPane().removeAll();
        getContentPane().setBackground(Color.WHITE);
        setLayout(new GridLayout(rows, columns, 10, 10));
    }
    
    // updates the screen with the new changes
    public void refreshScreen() {
        validate();
        repaint();
    }
    
    // method to standardize buttons
    public JButton makeButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(Color.PINK);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        return button;
    }
    // labels formatting
    public JLabel makeLabel(String text, Color background, Color foreground) {
        JLabel label = new JLabel(text, JLabel.CENTER);
        label.setBackground(background);
        label.setForeground(foreground);
        label.setOpaque(true);
        return label;
    }

}
