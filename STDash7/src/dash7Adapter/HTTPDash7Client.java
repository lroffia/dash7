package dash7Adapter;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class HTTPDash7Client {
	private String server = "127.0.0.1";
	private String getRequest = "";
	private String node = "";
	private String cmd = "";
	
	//Return: NODEID&TEMP&VALUE (VALUE can be = "TIMEOUT" or "BUSY")
	public String GetTemperature(String node) {
		this.node = node;
		cmd = "TEMP";
		getRequest = "http://" + server + ":8000/gettemp?id="+node;
		return doRequest();
	}
	//Return: NODEID&VALVE&VALUE (VALUE can be = "TIMEOUT" oe "BUSY")
	public String SetValve(String node,int value){
		this.node = node;
		cmd = "VALVE";
		// Percentage adapted to coordinator logic (100% ==> 0%)
		getRequest = "http://" + server + ":8000/setvalve?id="+node+"&value="+String.format("%d",100-value);
		return doRequest();
	}
	//Return: NODEID&STATUS&VALUE (VALUE can be = "TIMEOUT" or "BUSY")
	public String GetStatus(String node) {
		this.node = node;
		cmd = "STATUS";
		getRequest = "http://" + server + ":8000/getstatus?id="+node;
		return doRequest();
	}
	
	public HTTPDash7Client(String server) {
		this.server = server;
	}
	
	private String doRequest() {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		
        HttpGet httpget = new HttpGet(getRequest);

        System.out.println("> Executing request " + httpget.getRequestLine());

        ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
	        @Override
	        public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
	            int status = response.getStatusLine().getStatusCode();
	            if (status >= 200 && status < 300) 
	            {
	                HttpEntity entity = response.getEntity();
	                return entity != null ? EntityUtils.toString(entity) : node+"&"+cmd+"&ERROR";
	            } 
	            else 
	            {
	            	System.out.println("  ***Unexpected response status: " + status + " ***");
	            	return node+"&"+cmd+"&ERROR";
	            }
	        }
        };
        
        String responseBody ="";
        
        try 
        {
			responseBody = httpclient.execute(httpget, responseHandler);
		} 
        catch (ClientProtocolException e) {
        	System.out.println("  ***ClientProtocolException***");
			return node+"&"+cmd+"&ERROR";
		} 
        catch (IOException e) {
        	System.out.println("  ***IOException (HTTP Server down?)***");
			return node+"&"+cmd+"&ERROR";
		}
        
        try 
        {
			httpclient.close();
		} 
        catch (IOException e) {
        	return node+"&"+cmd+"&ERROR";
		}
        
        System.out.println("< Response " + responseBody);
        
        return responseBody;
	}
}
