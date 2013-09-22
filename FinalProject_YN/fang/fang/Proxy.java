package fang;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JOptionPane;

/**
 * This class simply forwards messages to 
 * and from a client and web server.
 * 
 * @author Jam Jenkins
 */
public class Proxy extends Thread
{
    /** the web server location */
    String domain;

    /** the port to run the proxy on */
    int port;

    /**
     * sets up the proxy
     * 
     * @param domain
     *            the web server for which this proxy will act as
     * @param port
     *            the port number to run the proxy on
     */
    public Proxy(String domain, int port)
    {
        this.domain = domain;
        this.port = port;
    }

    /**
     * starts the proxy running. While the server 
     * has not closed the connection,
     * this forwards the messages to and from the 
     * client. Upon the web server
     * closing connections, the proxy closes 
     * connections with the client.
     */
    public void run()
    {
        try
        {
            ServerSocket serverSocket = new ServerSocket(port);
            while (true)
            {
                Socket socket = serverSocket.accept();

                InputStream fromClient = socket.getInputStream();
                OutputStream toClient = socket.getOutputStream();
                Socket duke = new Socket(domain, 80);
                InputStream fromDuke = duke.getInputStream();
                OutputStream toDuke = duke.getOutputStream();
                new Forwarder(fromClient, toDuke, true).start();
                new Forwarder(fromDuke, toClient, false).start();
            }
        } catch (Exception e)
        {
            e.printStackTrace();

        }
    }

    /**
     * This class reads from the input and
     * writes to the output.  The only change
     * it makes is to change the proxy domain
     * name to the domain name of the web server.
     * @author Jam Jenkins
     */
    class Forwarder extends Thread
    {
        /**where to read from*/
        InputStream input;
        
        /**whether the output is toward the server
         * or toward the client*/
        boolean towardServer;
        
        /**where to write*/
        OutputStream output;

        /**constructs the forwarder
         * @param in where to read from
         * @param out where to write
         * @param towardServer true if this
         * forwarder is writing toward the web
         * server, false otherwise
         */
        public Forwarder(InputStream in, OutputStream out,
                boolean towardServer)
        {
            input = in;
            output = out;
            this.towardServer=towardServer;
        }

        /**
         * repeatedly reads and writes
         */
        public void run()
        {
            while(true)
            {
                try
                {
                    byte[] buffer=new byte[1000];
                    int read=input.read(buffer);
                    if(read<0)
                    {
                        input.close();
                        output.close();
                        return;
                    }
                    if(towardServer)
                    {
                        String text=new String(buffer, 0, read);
                        String sum="";
                        String[] lines=text.split("\n");
                        for(int i=0; i<lines.length; i++)
                        {     
                            if(lines[i].startsWith("Host"))
                                sum+="Host: "+domain+"\n";
                            else
                                sum+=lines[i]+"\n";
                        }
                        text=sum;
                        System.out.println(text);
                        buffer=text.getBytes();
                        read=buffer.length;
                    }
                    output.write(buffer, 0, read);
                    output.flush();
                } catch (IOException e)
                {
                    return;
                }
            }
        }   
    }

    /**
     * @param args
     *            args[0] is the domain of the web server<br>
     *            args[1] is port on which to run the proxy
     */
    public static void main(String[] args)
    {
        String domain;
        int port;
        if (args.length == 0)
            domain = JOptionPane.showInputDialog(null, "Enter web domain");
        else
            domain = args[0];
        domain=domain.trim();
        if(domain.toLowerCase().startsWith("http://"))
            domain=domain.substring(7);
        if (args.length < 2)
            port = Integer.parseInt(JOptionPane.showInputDialog(null,
                    "Enter proxy port", "8080"));
        else
            port = Integer.parseInt(args[1]);
        new Proxy(domain, port).start();
    }
}
