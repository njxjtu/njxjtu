package fang;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URL;

/**
 * Displays an html file in
 * a JDialog box.
 * @author Jam Jenkins
 */
public class HTMLDisplay extends JDialog implements ActionListener
{
    /**
     * used for serialization versioning
     */
    private static final long serialVersionUID = 1L;
    
    /**the stylesheet used to display the html*/
    private static final URL STYLE_SHEET=
        HTMLDisplay.class.getResource("resources/stylesheet.css");

    /** where the html is displayed */
    private JTextPane message;

    /** the name of the html file with respect to this directory */
    private URL filename;

    /** default size of the window */
    private static final Dimension DEFAULT_SIZE = new Dimension(300, 300);

    /** close button */
    private JButton closeButton;

    /**
     * makes the window, but does not set it visible
     * 
     * @param title
     *            name in the title bar of the window
     * @param filename
     *            the URL to load.  URL is necessary instead
     *            of String filename because this could be
     *            loaded in applets.
     */
    public HTMLDisplay(String title, URL filename)
    {
        super();
        setTitle(title);
        this.filename = filename;
        makeComponents();
        makeLayout();
        setSize(DEFAULT_SIZE);
    }

    /**makes this gui invisible
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        setVisible(false);
    }

    /**sets the stylesheet used to display
     * this html
     * @param styleURL the location of the
     * stylesheet to use
     */
    public void setStyleSheet(URL styleURL)
    {
        HTMLEditorKit kit=(HTMLEditorKit)message.getEditorKit();
        StyleSheet style=new StyleSheet();
        style.importStyleSheet(styleURL);
        kit.setStyleSheet(style);
        message.setEditorKit(kit);
        try
        {
            message.setPage(filename);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        message.setEditable(false);        
    }
    
    /** make the message pane and set its contents */
    private void makeComponents()
    {
        message = new JTextPane();
        closeButton = new FunButton("Close Help Window", DEFAULT_SIZE);
        closeButton.addActionListener(this);
        try
        {
            HTMLEditorKit kit=new HTMLEditorKit();
            StyleSheet style=new StyleSheet();
            style.importStyleSheet(STYLE_SHEET);
            kit.setStyleSheet(style);
            message.setEditorKit(kit);
            message.setPage(filename);
            message.setEditable(false);
        } catch (Exception e)
        {
            System.err.println("Is " + filename
                    + " in the html directory above tipgame?");
            e.printStackTrace();
        }
    }

    /** place the message pane in the window */
    private void makeLayout()
    {
        Container container = getContentPane();
        container.setLayout(new BorderLayout());
        container.add(new JScrollPane(message), BorderLayout.CENTER);
        container.add(closeButton, BorderLayout.SOUTH);
    }
    
    /**sets the content of the window displaying
     * the help screen
     * @param title the title of the JDialog box
     * @param content the content of the help
     */
    public void setContent(String title, URL content)
    {
        setTitle(title);
        try
        {
            message.setPage(content);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
