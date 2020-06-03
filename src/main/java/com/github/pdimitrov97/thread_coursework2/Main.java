package com.github.pdimitrov97.thread_coursework2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class Main
{
	public static void main(String[] args) throws InterruptedException
	{
		// Initialize Server and Worker variables
		String serverHost = "localhost";
		int serverPort = 9999;
		String workerHost = "localhost";
		int[] workerPorts = {1234, 1235, 1236, 1237};
		
		// Test data and functions
		List<Integer> intNumbers = new LinkedList<>(Arrays.asList(2, 3, 4, 5, 6, 7, 8));
		List<String> stringNumbers = new ArrayList<>(Arrays.asList("2", "3", "4", "5", "6", "7", "8"));
		Function<String, Integer> sqr = (Function<String, Integer> & Serializable) x -> Integer.valueOf(x) * 2;
		Function<Integer, Double> sqrt = (Function<Integer, Double> & Serializable) x -> Math.sqrt(x);
		Function<Integer, String> binary = (Function<Integer, String> & Serializable) x -> Integer.toBinaryString(x);
		DataWrapper work1 = new DataWrapper(stringNumbers, sqr);
		DataWrapper work2 = new DataWrapper(intNumbers, sqrt);
		DataWrapper work3 = new DataWrapper(intNumbers, binary);
		
		// Start 4 workers
		Worker worker1 = new Worker(1234);
		Worker worker2 = new Worker(1235);
		Worker worker3 = new Worker(1236);
		Worker worker4 = new Worker(1237);
		Thread tworker1 = new Thread(worker1);
		Thread tworker2 = new Thread(worker2);
		Thread tworker3 = new Thread(worker3);
		Thread tworker4 = new Thread(worker4);
		tworker1.start();
		tworker2.start();
		tworker3.start();
		tworker4.start();
		
		// Start Server
		Thread.sleep(2000);
		Server server = new Server(serverPort, workerHost, workerPorts);
		Thread tserver = new Thread(server);
		tserver.start();		
		
		// Start 6 Clients
		Thread.sleep(3000);
		Client client1 = new Client(1, serverHost, serverPort, work1);
		Client client2 = new Client(2, serverHost, serverPort, work2);
		Client client3 = new Client(3, serverHost, serverPort, work3);
		Client client5 = new Client(4, serverHost, serverPort, work1);
		Client client4 = new Client(5, serverHost, serverPort, work2);
		Client client6 = new Client(6, serverHost, serverPort, work3);
		Thread tclient1 = new Thread(client1);
		Thread tclient2 = new Thread(client2);
		Thread tclient3 = new Thread(client3);
		Thread tclient4 = new Thread(client4);
		Thread tclient5 = new Thread(client5);
		Thread tclient6 = new Thread(client6);
		tclient1.start();
		tclient2.start();
		tclient3.start();
		tclient4.start();
		tclient5.start();
		tclient6.start();
	}
}