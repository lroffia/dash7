package dash7Adapter;

import java.io.IOException;
import java.io.OutputStream;

import java.net.InetSocketAddress;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Semaphore;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import dash7Adapter.STDash7Coordinator.StatusNotify;
import dash7Adapter.STDash7Coordinator.TemperatureNotify;
import dash7Adapter.STDash7Coordinator.ValveNotify;

public class HTTPDash7Server {

	private static STDash7Coordinator dash7Coordinator = null;
	private static GetTempHandler httpGetTempHandler = null;
	private static SetValveHandler httpSetValveHandler = null;
	private static GetStatusHandler httpGetStatusHandler = null;
	
	private static Semaphore busy = new Semaphore(1,true);
	
	public static void main(String[] args) throws IOException {		
		HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
	    server.createContext("/", new InfoHandler());
	   
	    httpGetTempHandler = new GetTempHandler();
	    httpSetValveHandler = new SetValveHandler();
	    httpGetStatusHandler = new GetStatusHandler();
	    server.createContext("/gettemp", httpGetTempHandler);
	    server.createContext("/setvalve", httpSetValveHandler);
	    server.createContext("/getstatus", httpGetStatusHandler);
	    server.setExecutor(null);
		
		dash7Coordinator = new STDash7Coordinator();
	    dash7Coordinator.startRunning("127.0.0.1", 7700);

	    System.out.println("Connected to OTCom socket");
	    
	    server.start();
	    
	    System.out.println("The HTTP server is running...");
	}

	static class InfoHandler implements HttpHandler {
	    public void handle(HttpExchange httpExchange) throws IOException {
	    	String response = "";
	    	response += "<html><body>";
	    	response += "Welcome to the HTTP-DASH7 Server<br>";
	    	response += "- Use /gettemp?id=\"node_id\" to read the temperature of \"node_id\"<br>";
	    	response += "- Use /setvalve?id=\"node_id\"&value=\"0-100\" to open the valve of \"node_id\" to \"1-100\"%<br>";
	    	response += "- Use /getstatus?id=\"node_id\" to retrieve the valve status<br>";
	    	response += "</body></html>";
		    try {
				httpExchange.sendResponseHeaders(200, response.length());
				OutputStream os = httpExchange.getResponseBody();
				os.write(response.getBytes());
				os.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	}

	static class GetStatusHandler implements HttpHandler,Observer {		
		Thread currentThread = null;
		String status = "TIMEOUT";
		int timeout = 15000;
		String nodeID ="";
		
		class Running extends Thread {
			private HttpExchange httpExchange;
			
			public Running(HttpExchange httpExchange) {
				this.httpExchange = httpExchange;
				Map <String,String>parms = HTTPDash7Server.queryToMap(httpExchange.getRequestURI().getQuery());
				nodeID = parms.get("id");
			}
			
			public void run() {
				if (!busy.tryAcquire()) {
					SendResponse(httpExchange,nodeID,"STATUS","BUSY");
					return;
				}
				
				SendCommand(httpExchange);
				
				busy.release();
			}
		}
		
		private void SendCommand(HttpExchange httpExchange) {
			//Map <String,String>parms = HTTPDash7Server.queryToMap(httpExchange.getRequestURI().getQuery());
			//nodeID = parms.get("id");
			
			System.out.printf("> HTTP GetStatus(%s)...\n",nodeID);
			
			dash7Coordinator.addObserver(this);
			
			try 
			{
				dash7Coordinator.SendGetStatusCommand(nodeID.getBytes());
			} 
			catch (IOException e1) {
				System.out.printf("< HTTP GetStatus(%s): %s\n", nodeID, status);
				SendResponse(httpExchange,nodeID,"STATUS",status);
				dash7Coordinator.deleteObserver(this);
				return;
			}
				
			try 
			{
				currentThread = Thread.currentThread();
				Thread.sleep(timeout);
			} 
			catch (InterruptedException e) {
				System.out.printf("< HTTP Status(%s): %s\n", nodeID, status);
				SendResponse(httpExchange,nodeID,"STATUS",status);
				dash7Coordinator.deleteObserver(this);
				return;
			}
			
			System.out.printf("< HTTP GetStatus(%s): TIMEOUT\n",nodeID);
			SendResponse(httpExchange,nodeID,"STATUS","TIMEOUT");
			dash7Coordinator.deleteObserver(this);
		}
		
		public void handle(HttpExchange httpExchange) {
			new Running(httpExchange).start();
		}

		@Override
		public void update(Observable o, Object arg) {
			StatusNotify notify = (StatusNotify) arg;
			if (nodeID.equals(notify.id)){
				status = notify.status;
				currentThread.interrupt();
			}
		}
	}
	
	static class GetTempHandler implements HttpHandler,Observer {
		Thread currentThread = null;
		float temperature = 0.0f;
		int timeout = 15000;
		String nodeID ="";
		
		class Running extends Thread {
			private HttpExchange httpExchange;
			
			public Running(HttpExchange httpExchange) {
				this.httpExchange = httpExchange;
				Map <String,String>parms = HTTPDash7Server.queryToMap(httpExchange.getRequestURI().getQuery());
				nodeID = parms.get("id");
			}
			
			public void run() {
				if (!busy.tryAcquire()) {
					SendResponse(httpExchange,nodeID,"TEMP","BUSY");
					return;
				}
				
				SendCommand(httpExchange);
				
				busy.release();	
			}
		}
		
		private void SendCommand(HttpExchange httpExchange) {
			System.out.printf("> HTTP GetTemp(%s)...\n",nodeID);
			
			dash7Coordinator.addObserver(this);
			try 
			{
				dash7Coordinator.SendReadTempCommand(nodeID.getBytes());
			} 
			catch (IOException e1) {
				System.out.printf("< HTTP GetTemp(%s): TIMEOUT\n", nodeID);
				SendResponse(httpExchange,nodeID,"TEMP","TIMEOUT");
				dash7Coordinator.deleteObserver(this);
				return;
			}
				
			try 
			{
				currentThread = Thread.currentThread();
				Thread.sleep(timeout);
			} 
			catch (InterruptedException e) {
				System.out.printf("< HTTP Temperature(%s): %.2f\n", nodeID, temperature);
				SendResponse(httpExchange,nodeID,"TEMP",String.format("%.2f", temperature));
				dash7Coordinator.deleteObserver(this);
				return;
			}
			
			System.out.printf("< HTTP GetTemp(%s): TIMEOUT\n",nodeID);
			SendResponse(httpExchange,nodeID,"TEMP","TIMEOUT");
			dash7Coordinator.deleteObserver(this);	
		}
		
		public void handle(HttpExchange httpExchange) {
			new Running(httpExchange).start();
		}

		@Override
		public void update(Observable o, Object arg) {
			TemperatureNotify notify = (TemperatureNotify) arg;
			System.out.printf("- HTTP received notification for node %s (waiting temperature for node %s)\n", notify.id,nodeID);
			if (nodeID.equals(notify.id)){
				temperature = notify.temperature;
				currentThread.interrupt();
			}
		}
	}
	
	static class SetValveHandler implements HttpHandler,Observer {		
		Thread currentThread = null;
		boolean ack = false;
		int timeout = 15000;
		String nodeID ="";
		String value = "";
		
		class Running extends Thread {
			private HttpExchange httpExchange;
			
			public Running(HttpExchange httpExchange) {
				this.httpExchange = httpExchange;
				Map <String,String>parms = HTTPDash7Server.queryToMap(httpExchange.getRequestURI().getQuery());
				nodeID = parms.get("id");
				value = parms.get("value");
			}
			
			public void run() {
				if (!busy.tryAcquire()) {
					SendResponse(httpExchange,nodeID,"VALVE","BUSY");
					return;
				}
				
				SendCommand(httpExchange);
				
				busy.release();	
			}
		}
		
		private void SendCommand(HttpExchange httpExchange){
			System.out.printf("> HTTP SetValve(%s,%s)...\n",nodeID,value);
			
			if (value == null) {
				SendResponse(httpExchange,nodeID,"VALVE","NACK");
				return;	
			}
			
			if (Float.parseFloat(value) < 0 || Float.parseFloat(value) > 100) {
				SendResponse(httpExchange,nodeID,"VALVE","NACK");
				return;
			}
			
			dash7Coordinator.addObserver(this);
			
			try 
			{
				dash7Coordinator.SendSetValveCommand(nodeID.getBytes(), (byte)(Integer.parseInt(value) & 0xFF));
			} 
			catch (NumberFormatException e1) {
				System.out.printf("< HTTP SetValve(%s,%s): TIMEOUT\n", nodeID,value);
				SendResponse(httpExchange,nodeID,"VALVE","TIMEOUT");
				dash7Coordinator.deleteObserver(this);
				return;
			} 
			catch (IOException e1) {
				System.out.printf("< HTTP SetValve(%s,%s): TIMEOUT\n", nodeID,value);
				SendResponse(httpExchange,nodeID,"VALVE","TIMEOUT");
				dash7Coordinator.deleteObserver(this);
				return;
			}
				
			try 
			{
				currentThread = Thread.currentThread();
				Thread.sleep(timeout);
			} 
			catch (InterruptedException e) {
				System.out.printf("< HTTP Valve(%s): %s\n", nodeID, (ack ? "ACK" : "NACK"));
				SendResponse(httpExchange,nodeID,"VALVE",(ack ? "ACK" : "NACK"));
				dash7Coordinator.deleteObserver(this);
				return;
			}
			
			System.out.printf("< HTTP SetValve(%s): TIMEOUT\n",nodeID);
			SendResponse(httpExchange,nodeID,"VALVE","TIMEOUT");
			dash7Coordinator.deleteObserver(this);	
		}
		public void handle(HttpExchange httpExchange) {		
			new Running(httpExchange).start();		
		}

		@Override
		public void update(Observable o, Object arg) {
			ValveNotify notify = (ValveNotify) arg;
			if (nodeID.equals(notify.id)){
				ack = notify.ack;
				currentThread.interrupt();
			}
		}
	}
	
	private static void SendResponse(HttpExchange httpExchange,String node,String cmd,String value) {
		if (httpExchange == null) return;

		String response = "";
		response +=String.format("%s&%s&%s",node,cmd,value);
		
		try 
		{
			httpExchange.sendResponseHeaders(200, response.length());
			OutputStream os = httpExchange.getResponseBody();
			os.write(response.getBytes());
			os.close();
		} 
		catch (IOException e) {
			System.out.printf("Send HTTP Response failed...\n");
		}
	}
	
	public static Map<String, String> queryToMap(String query){
		Map<String, String> result = new HashMap<String, String>();
		for (String param : query.split("&")) {
			String pair[] = param.split("=");
			if (pair.length>1) result.put(pair[0], pair[1]);
		    else result.put(pair[0], "");
		}
		return result;
	}
}