package com.github.pdimitrov97.thread_coursework2;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Server<T, G> implements Runnable
{
	private int port;
	private String workerHost;
	private int[] workerPorts;
	private int initializedWorkers;
	
	private ServerSocket serverSocket;
	private List<Socket> workers;
	private Lock workersLock;
	
	public Server(int port, String workerHost, int[] workerPorts)
	{
		this.port = port;
		this.workerHost = workerHost;
		this.workerPorts = workerPorts;
		this.initializedWorkers = 0;
		this.workersLock = new ReentrantLock();
	}
	
	@Override
	public void run()
	{
		startServer();		
	}
	
	private void startServer()
	{
		try
		{
			setUpServerSocket();
			connectToWorkers();
			serverLoop();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			shutDownWorkers();
			shutDownServer();
		}
	}

	private void setUpServerSocket() throws IOException
	{
		serverSocket = new ServerSocket(this.port);
		System.out.println("Server listening on port " + this.port);
	}
	
	private void connectToWorkers() throws InterruptedException
	{
		workers = new ArrayList<>();

		for (int port : this.workerPorts)
		{
			System.out.println("Server connecting to Worker: " + port);
			
			try
			{
				InetAddress address = InetAddress.getByName(this.workerHost);
				Socket socket = new Socket(address, port);
				workers.add(socket);
				System.out.println("Server connected to Worker: " + port);
				this.initializedWorkers++;
			}
			catch (IOException e)
			{
				System.out.println("Error connecting to worker: " + port);
			}
		}
	}
	
	public void serverLoop()
	{
		try
		{
			while (true)
				receiveClientRequest();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			shutDownWorkers();
			shutDownServer();
		}
	}

	private void receiveClientRequest() throws IOException
	{
		System.out.println("Server: Waiting for connection with Client...");
		Socket client = serverSocket.accept();
		System.out.println("Server: Client \"" + client.getPort() + "\" connected!");
		
		ClientRequestAccepter cra = new ClientRequestAccepter(client);
		Thread tcra = new Thread (cra);
		tcra.start();
	}

	private class ClientRequestAccepter implements Runnable
	{
		private Socket clientSocket;
		private Map workerOutput;

		public ClientRequestAccepter(Socket clientSocket)
		{
			this.clientSocket = clientSocket;
		}

		@Override
		public void run()
		{
			try
			{
				runRequest();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		private void runRequest() throws Exception
		{		
			DataWrapper input = receiveWorkFromClient();

			workerOutput = new HashMap();
			List result = new ArrayList<>();
			
			workersLock.lock();

			try
			{
				List dataChunks = splitData(input);
				int nOfChunks = dataChunks.size();

				sendToWorkers(dataChunks);
				receiveResultFromWorkers();

				result = joinData(workerOutput);
				sendResultsToClient(result);
				clientSocket.close();
			}
			catch (Exception exception)
			{
				exception.printStackTrace();
			}
			finally
			{
				workersLock.unlock();
			}
		}
		
		private DataWrapper receiveWorkFromClient() throws IOException, ClassNotFoundException
		{
			System.out.println("Server: Receiving work from Client " + clientSocket.getPort() + "...");
			InputStream is = clientSocket.getInputStream();
			ObjectInputStream ois = new ObjectInputStream(is);
			DataWrapper input = (DataWrapper) ois.readObject();
			System.out.println("Server: Received work from Client " + clientSocket.getPort() + "!");
			System.out.println("Server: Work is: " + input.getList().toString());
			
			return input;
		}

		private void sendToWorkers(List<DataWrapper> data) throws IOException
		{
			int i = 0;
			int workerIndex = 0;
			Socket worker;
			
			while (i < data.size())
			{
				if (workerIndex >= workers.size())
					workerIndex = 0;

				worker = workers.get(workerIndex);
				DataWrapper chunk = data.get(i);

				try
				{
					System.out.println("Server: Sending work to Worker " + worker.getPort());
					OutputStream os = worker.getOutputStream();
					ObjectOutputStream oos = new ObjectOutputStream(os);
					oos.writeObject(chunk);
					oos.flush();
					System.out.println("Server: Work to Worker " + worker.getPort() + " sent!");
					
					i++;
					workerIndex++;
				}
				catch (Exception e)
				{
					System.out.println("Server: Worker " + worker.getPort() + " is busy!");
					workerIndex++;
				}
			}
		}

		private void receiveResultFromWorkers() throws IOException, ClassNotFoundException, InterruptedException
		{
			for (Socket worker : workers)
			{
				System.out.println("Server: Waiting for result from Worker " + worker.getPort() + "...");
				InputStream is = worker.getInputStream();				
				ObjectInputStream ois = new ObjectInputStream(is);
				
				Map<Integer, List> data = (Map) ois.readObject();

				for (Map.Entry<Integer, List> dataChunk : data.entrySet())
					workerOutput.put(dataChunk.getKey(), dataChunk.getValue());
				
				System.out.println("Server: Result from Worker " + worker.getPort() + " received!");
			}
		}

		private void sendResultsToClient(Object result) throws IOException
		{
			System.out.println("Server: Sending result back to Client " + clientSocket.getPort() + "...");
			OutputStream os = clientSocket.getOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(os);
			oos.writeObject(result);
			oos.flush();
			System.out.println("Server: Result sent to Client " + clientSocket.getPort() + "!");
			System.out.println();
		}

		private List<DataWrapper> splitData(DataWrapper input)
		{			
			List<DataWrapper> dataChunks = new ArrayList<DataWrapper>();
			DataWrapper dataChunk;
			List subList;
			int startIndex = 0;
			int endIndex = 0;
			int chunkSize = (int) Math.ceil((double) input.getList().size() / (double) initializedWorkers);

			while (startIndex <= (input.getList().size() - 1))
			{
				if ((startIndex + chunkSize) >= input.getList().size())
					endIndex = input.getList().size();
				else 
					endIndex = startIndex + chunkSize;

				subList = input.getList().subList(startIndex, endIndex);
				dataChunk = new DataWrapper(startIndex, new ArrayList(subList), input.getFx());
				dataChunks.add(dataChunk);
				startIndex += chunkSize;
			}

			return dataChunks;
		}

		private List<Object> joinData(Map<Integer, List> dataChunks)
		{
			TreeMap<Integer, List> orderedData = new TreeMap(dataChunks);
			List<Object> results = new ArrayList<>();

			for (Map.Entry<Integer, List> dataChunk : orderedData.entrySet())
			{
				for (Object data : dataChunk.getValue())
					results.add(data);
			}
			
			return results;
		}
	}

	private void shutDownServer()
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

	private void shutDownWorkers()
	{
		try
		{
			for (Socket worker : workers)
				worker.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}