package fang;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;


public class HttpServerSocket extends ServerSocket
{

	public HttpServerSocket() throws IOException
	{
		super();
	}

	public HttpServerSocket(int port, int backlog, InetAddress 
bindAddr) throws IOException
	{
		super(port, backlog, bindAddr);
	}

	public HttpServerSocket(int port, int backlog) throws 
IOException
	{
		super(port, backlog);
	}

	public HttpServerSocket(int port) throws IOException
	{
		super(port);
	}

	public Socket accept() throws IOException
	{
		Socket socket=super.accept();
		InputStream in=socket.getInputStream();
		int[] last4=new int[4];
		for(int i=0; i<last4.length; i++)
		{
			last4[i]=in.read();
		}
		while(last4[0]!=10 || last4[1]!=13 ||
				last4[2]!=10 || last4[3]!=13)
		{
			for(int i=3; i>0; i--)
			{
				last4[i]=last4[i-1];
			}
			last4[0]=in.read();
		}
		java.lang.System.out.println("Cleared out input.");
		return socket;
	}
}

