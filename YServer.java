
import java.net.*;
import java.io.*;
//Standard server class, nothing special
public class YServer 
{
    public static void main(String[] args) throws IOException 
    {
    	ServerSocket sock = null;
    	boolean listen = true;
    	int port = 4646;

    	try
    	{	sock = new ServerSocket(port);
    	} 
    	catch (IOException e) 
    	{	System.err.println("Could not connect on port: "+port);
    	}

    	System.out.println("Yahtzee server started");

        YState Score = new YState();
        int count=1;
        while (listen) 
            {new YThread(sock.accept(),("Player "+count), Score).start();
             count++;
            }

        sock.close();
    }
}



