package com.github.pdimitrov97.thread_coursework2;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Worker<T, G> implements Runnable
{
	private ServerSocket workerSocket;
	private Socket socket;
	private int port;

	public Worker(int port) 
	{
		this.port = port;
	}
	
	@Override
	public void run()
	{
		startWorker();		
	}
	
	public void startWorker()
	{
		try
		{
			setUpWorkerSocket();
			receiveServerConnection();
			
			while (true)
			{
				Map result = null;

				try
				{
					DataWrapper input = receiveWorkFromServer();
					result = workResult(input);
					System.out.println("Worker " + this.port + ": Work finished! Result: " + result.toString());
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				sendResultToServer(result);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				socket.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	private void setUpWorkerSocket() throws IOException
	{
		workerSocket = new ServerSocket(this.port);
		System.out.println("Worker " + this.port + ": Listening on port " + this.port);
	}

	private void receiveServerConnection() throws IOException
	{
		System.out.println("Worker " + this.port + ": Waiting for connection with Server...");
		socket = workerSocket.accept();
		System.out.println("Worker " + this.port + ": Server connected!");
	}

	private DataWrapper receiveWorkFromServer() throws IOException, ClassNotFoundException, InterruptedException
	{
		System.out.println("Worker " + this.port + ": Receiving work from Server...");
		InputStream is = socket.getInputStream();
		ObjectInputStream ois = new ObjectInputStream(is);
		DataWrapper input = (DataWrapper) ois.readObject();
		System.out.println("Worker " + this.port + ": Work received from Server!");
		System.out.println("Worker " + this.port + ": Work is: " + input.getList().toString());

		return input;
	}

	private void sendResultToServer(Map result) throws IOException, InterruptedException
	{
		System.out.println("Worker " + this.port + ": Sending result back to Server...");
		OutputStream os = socket.getOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(os);
		oos.writeObject(result);
		oos.flush();
		System.out.println("Worker " + this.port + ": Result sent to Server!");
	}

	private HashMap<Integer, List<G>> workResult(DataWrapper<T, G> work)
	{
		List<G> res = new LinkedList<>();
		
		for (T item : work.getList())
			res.add(work.getFx().apply(item));

		HashMap result = new HashMap();
		result.put(work.getSegIndex(), res);

		return result;
	}
}