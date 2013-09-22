package fang;

import static fang.ErrorConsole.addError;
import static fang.ErrorConsole.fixHTML;
import static fang.ErrorConsole.fixedWidth;
import static fang.ErrorConsole.getErrorFile;
import static fang.ErrorConsole.getErrorLine;
import static fang.ErrorConsole.getErrorLineNumber;
import static fang.ErrorConsole.indent;
import static fang.ErrorConsole.subHeading;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.LinkedList;

/**
 * Displays runtime errors in a meaningful way.
 * @author Jam Jenkins
 */
public class ErrorConsole extends JDialog implements ActionListener
{
    /**
     * used for serialization versioning
     */
    private static final long serialVersionUID = 1L;
    
    /**the stylesheet used to display the html*/
    private static final URL STYLE_SHEET=
        ErrorConsole.class.getResource("resources/stylesheet.css");

    /** where the html is displayed */
    private JTextPane message;

    /** default size of the window */
    private static final Dimension DEFAULT_SIZE = new Dimension(600, 600);

    /** close/next button */
    private JButton closeButton;

    /** only one error console is needed, this is the only one constructed*/
    private static final ErrorConsole single=new ErrorConsole();
    
    /** list of all of errors that have occurred*/
    private LinkedList<String> errors=new LinkedList<String>();
    
    /**
     * makes the error window, but does not set it visible
     */
    private ErrorConsole()
    {
        super();
        setTitle("Runtime Errors");
        makeComponents();
        makeLayout();
        setSize(DEFAULT_SIZE);
    }

    /**advances to the next error and makes this gui invisible
     * when there are no more errors
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
    	if(errors.size()==0)
    		setVisible(false);
    	else if(errors.size()==1)
    	{
    		errors.removeFirst();
            setVisible(false);    		
    	}
    	else if(errors.size()>1)
    	{
    		errors.removeFirst();
    		message.setText(errors.getFirst());
    		message.setCaretPosition(0);
    		if(errors.size()==1)
    			closeButton.setText("Close Error Console");
    	}
    }
        
    /** make the message pane and set its contents */
    private void makeComponents()
    {
        message = new JTextPane();
        closeButton = new FunButton("Close Error Console", DEFAULT_SIZE);
        closeButton.addActionListener(this);
        HTMLEditorKit kit=new HTMLEditorKit();
        StyleSheet style=new StyleSheet();
        style.importStyleSheet(STYLE_SHEET);
        kit.setStyleSheet(style);
        message.setEditorKit(kit);
        message.setEditable(false);
    }

    /** place the message pane in the window */
    private void makeLayout()
    {
        Container container = getContentPane();
        container.setLayout(new BorderLayout());
        container.add(new JScrollPane(message), BorderLayout.CENTER);
        container.add(closeButton, BorderLayout.SOUTH);
    }
    
    /**
     * gets the line of the file where the error is.
     * This reads from the file until a semicolon is found.
     * @param fileName the name of the file to read from
     * @param lineNumber the line to return the contents of
     * @return the statement starting at the given line
     */
    public static String getLine(String fileName, int lineNumber)
    {
    	try
		{
			BufferedReader reader=new BufferedReader(new FileReader(fileName));
			String line=reader.readLine();
			int currentLineNumber=1;
			while(currentLineNumber!=lineNumber)
			{
				line=reader.readLine();
				currentLineNumber++;
			}
			while(line.indexOf(";")<0)
			{
				line+="\n"+reader.readLine();
			}
			return line;
		}
    	catch (FileNotFoundException e)
    	{
    		e.printStackTrace();
    	} 
    	catch (IOException e)
    	{	
    		e.printStackTrace();
    	}
    	return null;
    }
    
    /**
     * replaces the symbols less than, greater than, quotes,
     * new lines, spaces, and tabs with the corresponding
     * html to display these properly.
     * @param text the text to convert, usually Java code
     * @return the html for displaying the text properly
     */
    public static String fixHTML(String text)
    {
    	return text.replaceAll("<", "&lt;")
    		.replaceAll(">", "&gt;")
    		.replaceAll("\"", "\\\"")
    		.replace("\n", "<br>")
    		.replace(" ", "&nbsp;")
    		.replace("\t", "&nbsp;&nbsp;&nbsp;");
    }
    
    /**
     * makes the text monospaced in html
     * @param text the text to make monospaced, typically file names and code
     * @return text surrounded with a tag to make it monospaced
     */
    public static String fixedWidth(String text)
    {
    	return "<span style=\"font-family: monospace; font-weight: bold;color: rgb(255, 255, 0);\">"+
    		text+"</span>";
    }
    
    /**
     * indents the given text 40 pixels
     * @param text the string to indent
     * @return text surrounded with a tag to indent it 40 pixels
     */
    public static String indent(String text)
    {
    	return "<div style=\"margin-left: 40px;\">"+text+"</div>";
    }

    /**
     * makes the text large
     * @param text the heading
     * @return text surrounded with a tag for making it large
     */
    public static String heading(String text)
    {
    	return "<h1 style=\"color: rgb(255, 255, 255);\">"+text+"</h1><br>";
    }

    /**
     * makes the text slightly smaller than the heading
     * @param text the subheading
     * @return text surrounded with a tag for making it large
     */
    public static String subHeading(String text)
    {
    	return "<h2 style=\"color: rgb(255, 255, 255);\">"+text+"</h2>";
    }

    /**
     * gets the text for displaying the error's location
     * @param e the exception that generated the error
     * @return detailed information about where the error occurred
     */
    public static String getLocationSection(Exception e)
    {
    	String message=
    		subHeading("Error Location")+
    		"This error was generated by line "+
    		getErrorLineNumber()+" of the file<br>"+
    		indent(fixedWidth(getErrorFile()))+"<br>"+
    		"This line is <br>"+
    		fixedWidth(fixHTML(getErrorLine()))+"<br>";
    	if(e!=null)
    	{
    		message+="<br>Exception Stack Trace:<br><br>";
    		StringWriter writer=new StringWriter();
    		e.printStackTrace(new PrintWriter(writer));
    		message+=fixedWidth("<pre>"+writer.toString()+"</pre>");
    	}
        return message;
    }
    
    /**
     * gets the text of the line where the error occurred
     * @return the line of the error ending in a semicolon
     */
    public static String getErrorLine()
    {
    	return getLine(getErrorFile(), getErrorLineNumber());
    }
    
    /**
     * gets the line number where the error occurred
     * @return the line number
     */
    public static int getErrorLineNumber()
    {
    	return getErrorElement().getLineNumber();
    }
    
    /**
     * gets the name of the method where the error occurred
     * @return the method name
     */
    public static String getErrorMethod()
    {
    	return getErrorElement().getMethodName();
    }
    
    /**
     * gets the name of the source file where the error occurred
     * @return the file name of the code with the error in it
     */
    public static String getErrorFile()
    {
    	StackTraceElement element=getErrorElement();
        String fileName=element.getClassName();
        fileName=fileName.replace('.', '/');
        fileName=fileName+".java";
        return fileName;    	
    }
    
    /**
     * iterates through the execution stack to find the first
     * element which is outside of the FANG Engine
     * @return the stack trace element of the code with the error
     */
    public static StackTraceElement getErrorElement()
    {
        StackTraceElement[] all=Thread.currentThread().getStackTrace();
        int i;
        for(i=0; i<all.length; i++)
        {
            String packageName=all[i].getClassName();
        	if(!packageName.startsWith("fang") && 
        			!packageName.startsWith("java"))
                break;
        }
        if(i==all.length) return all[i-1];
        return all[i];
    }
    
    /**sets the content of the window displaying
     * the error screen.  If more than one error
     * occurs, this adds the error to the queue.
     * @param title the title of the JDialog box
     * @param content the content of the help
     */
    public static void addError(String diagnosis, String fix, Exception e)
    {
    	String content=
    		heading(e.getClass().getCanonicalName())+
    		subHeading("Diagnosis")+
    		diagnosis+
    		"<br>"+subHeading("Suggested Fix")+
    		fix+
    		getLocationSection(e);
    	single.errors.add(content);
    	if(single.errors.size()==1)
    	{
    		single.message.setText(content);
    		single.setVisible(true);
    	}
    	else
    	{
    		single.closeButton.setText("Next Error Message");
    	}
    }
    
    /**
     * call this method for errors which should not occur.
     * If they do occur, the FANG Engine developers need to
     * know about it.
     * @param e the unexpected exception 
     */
    public static void addUnknownError(Exception e)
    {
    	addError("Strange, this error does not come up often.",
    			"Please make a jar of this game.  Email " +
    			"bug@fangengine.org the jar file along" +
    			" with a description of how to recreate" +
    			" the error.", e);
    }
}