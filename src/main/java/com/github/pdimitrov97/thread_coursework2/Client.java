package com.github.pdimitrov97.thread_coursework2;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;
import java.util.function.Function;

public class Client implements Runnable
{
	private int id;
	private String serverHost;
	private int serverPort;
	private Socket serverSocket;
	private DataWrapper dataWrapper;

	public Client(int id, String serverHost, int serverPort, DataWrapper dataWrapper)
	{
		this.id = id;
		this.serverHost = serverHost;
		this.serverPort = serverPort;
		this.dataWrapper = dataWrapper;
	}
	
	@Override
	public void run()
	{
		startClient();		
	}

	public void startClient()
	{
		try
		{
			connectToServer();
			sendWorkToServer();
			receiveResultFromServer();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				serverSocket.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	private void connectToServer() throws IOException
	{
		System.out.println("Client " + this.id + ": Connecting to Server...");
		InetAddress address = InetAddress.getByName(serverHost);
		serverSocket = new Socket(address, serverPort);
		System.out.println("Client " + this.id + ": Connected to Server!\n");
	}

	private void sendWorkToServer() throws IOException
	{
		System.out.println("Client " + this.id + ": Work is: " + dataWrapper.getList().toString());

		System.out.println("Client " + this.id + ": Sending work to Server...");
		OutputStream os = serverSocket.getOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(os);
		oos.writeObject(dataWrapper);
		oos.flush();
		System.out.println("Client " + this.id + ": Work sent to Server!\n");
	}

	private void receiveResultFromServer() throws IOException, ClassNotFoundException
	{
		System.out.println("Client " + this.id + ": Waiting for result from Server...");
		InputStream is = serverSocket.getInputStream();
		ObjectInputStream ois = new ObjectInputStream(is);
		List<Object> result = (List<Object>) ois.readObject();
		System.out.println("Client " + this.id + ": Received the result from Server! Result: " + result.toString() + "\n");
	}
}