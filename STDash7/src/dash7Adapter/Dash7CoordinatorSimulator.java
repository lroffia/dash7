package dash7Adapter;

/** 
 * 1) > socat -d -d pty,raw,echo=0 pty,raw,echo=0
 * 2) > sudo ln -s /dev/ttys00x /usr/commap/com1 (used by simulator)
 * 3) > sudo ln -s /dev/ttys00x /usr/commap/com0 (used by OTCom)
 * 4) Run OTCom

**/
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Dash7CoordinatorSimulator {
	private static SerialReaderThread mainThread = null;
	
	public static void main(String[] args) throws FileNotFoundException, InterruptedException {
        System.out.println("Dash7 Simulator is running...press <ctrl + c> to exit");
		mainThread = new SerialReaderThread("/usr/commap/com1");
		mainThread.start();
	}
	
	static class SerialReaderThread extends Thread {
    	private byte[] inBuffer = null;
    	private FileOutputStream out = null;
    	private FileInputStream in = null;
        
    	private byte temperature = 0;
        private byte temperatureDecimal = 0;
        
        public synchronized void updateTemp() {
        	//Update simulated temperature
			if (temperatureDecimal == 3) {
				temperatureDecimal = 0;
				temperature++;
			}
			else temperatureDecimal++;	
        }
        
    	class NodeSimulator extends Thread {
    		private byte[] request;
    		private byte[] dash7Reply = null;
    		private byte[] node_id = null;
    		private byte[] payload = null;
    		private byte cmd = 0;
    		private byte alpCmdReply = 0x00;
    		private long delay = 0;
    		private double delayR = 0;
    		private int sequence = 0;
        	
    		public NodeSimulator(byte cmd,byte[] paylaod,int sequence) {
    			this.payload = paylaod;
    			this.cmd = cmd;
    			this.sequence = sequence;
    		}
    		
    		public void run() {
    			switch(cmd) {
    				case STDash7Coordinator.ALP_CMD_READ_TEMP_CMD:
    					//Simulate delay...
    					delayR = Math.random();
        				delay = 1000 + Math.round(delayR*4000);
        				
    					//Node id
    					int addrLen = payload[0] & 0xFF;
    					node_id = new byte[addrLen];
    					for (int i=0; i < addrLen; i++) node_id[i] = payload[i + 1];
    					
    					String output = "Node[0x";
    					for (int i=0; i < node_id.length ; i ++) output += String.format("%02X",node_id[i]);
    					output += String.format("] reading temperature in %d ms...\n", delay);
    					System.out.println(output);
    					
    					
    					//Payload (nod_id len + node_id + + data len + temperature)
    					payload = new byte[node_id.length + 4];
    					payload[0] = (byte)(addrLen & 0xFF);
    					payload[payload.length-3]= 2;
    					payload[payload.length-2]= temperature;
    					payload[payload.length-1]= (byte)(temperatureDecimal << 6);
    					for (int i=0; i < addrLen; i++) payload[i + 1] = node_id[i];
    					updateTemp();
    					
    					//Response command
    					alpCmdReply = STDash7Coordinator.ALP_TEMP_REPLY;

    					break;
    					
    				case STDash7Coordinator.ALP_CMD_SET_VALVE_CMD:
    					//Check value range (0-100)
    					int percent = payload[payload.length-1] & 0xFF;
    					if (percent > 100) {
    						System.out.printf("    |--> ERROR: Value range is 0-100. Received %d reponse in ",percent);
    						delay = 0;
    					}
    					else {
    						//Simulate delay...
	        				delayR = Math.random();
	        				delay = 2000 + Math.round(delayR*10000);
	        				
    						output = "Node[0x";
    						for (int i=0; i < payload[0] ; i ++) output += String.format("%02X",payload[i + 1] & 0xFF);
    						output += String.format("] opening valve at 0x%02X in %d ms",payload[payload.length-1] & 0xFF,delay);
    						System.out.println(output);	        				
    					}
    								
    					//Payload
    					if (percent > 100) payload[payload.length-1]= STDash7Coordinator.ST_DASH7_NACK;
    					else payload[payload.length-1]= STDash7Coordinator.ST_DASH7_ACK;
    					
    					//Response command
    					alpCmdReply = STDash7Coordinator.ALP_VALVE_REPLY;
    					
    					break;
    				default:
    					System.out.printf("ERROR: Command unknown (%02X) response in ",request[5] & 0xFF);
    					
    					delay = 0;
    					
    					//Payload
    					payload = null;
    					
    					//Response command
    					alpCmdReply = STDash7Coordinator.ALP_UNKNOWN_CMD_REPLY;
    					
    					break;
    				}
    		
					try 
					{
						Thread.sleep(delay);
					} 
					catch (InterruptedException e) {
						e.printStackTrace();
						notifyAll();
					}
					
    				//Simulated response
    				dash7Reply = OTComSocket.Dash7Packet(STDash7Coordinator.ALP_ID,alpCmdReply,payload,(char)(sequence & 0xFFFF));
    				
    				//Send response over UART
    				reply(dash7Reply);
        		}
    	}
    	
        public SerialReaderThread(String comPort) throws FileNotFoundException {
    		out = new FileOutputStream(comPort);
    		in = new FileInputStream(comPort);
            inBuffer = new byte[1024];
        }
        
        private synchronized void reply(byte[] dash7Packet){
        	System.out.printf("<-- Dash7 Packet[%d]: [ ",dash7Packet.length);
			for(int i=0; i < dash7Packet.length; i++) {
				System.out.printf("0x%02X ", dash7Packet[i]);
				if (i == OTComSocket.NDEF_HEADER_SIZE - 1) System.out.printf("] [ ");
				if (i == dash7Packet.length - 5) System.out.printf("] [ ");
			}
			int sequence = (dash7Packet[dash7Packet.length-3] & 0xFF) + ((dash7Packet[dash7Packet.length-4] << 8) & 0xFF00) ;
			System.out.printf("] Sequence: %d\n",sequence);
			
        	try 
        	{
				out.write(dash7Packet);
			} 
        	catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
        public void run() {
        	int messageLength = 0;
        	int dash7PayloadLen = 0;
        	int dash7PacketOffset = 0;
        	int dash7PacketLen = 0;
        	
        	try {
        		
            while(true) {
            	System.out.println("\n***Listening for NDEF Dash7 Mode2 packets***");
				while(in.available() == 0) Thread.sleep(100);
				System.out.println("***New bytes available...parsing for DASH7 packets***");
				
				//Multiple dash7 packets can be read
				messageLength = 0;
				messageLength = in.read(inBuffer);
				
				//Sync byte
				if (inBuffer[0] != OTComSocket.NDEF_FLAGS) {
					System.out.println("ERROR: NO NDEF FLASG");
					continue;
				}
				
				//Dash7 header+footer (10 bytes)
    			if (messageLength < OTComSocket.NDEF_HEADER_SIZE + OTComSocket.NDEF_FOOTER_SIZE) {
    				String output = "ERROR: Not a valid Dash7 Packet: [ ";
    				for (int i=0; i < messageLength; i++) output += String.format("%02X ", inBuffer[i]);
    				output += String.format("]");
    				System.out.println(output);
    				continue;
    			}
    			
    			if (messageLength < 3) continue;
    			
    			if (messageLength < inBuffer[2] + OTComSocket.NDEF_HEADER_SIZE + OTComSocket.NDEF_FOOTER_SIZE) {
    				String output = String.format("ERROR: Wrong message size %d [", messageLength);
					for (int i=0; i < messageLength; i++) output += String.format("%02X ", inBuffer[i]);
					output += String.format("]\n");
					System.out.println(output);
    				continue;
				}
    			
    			//Parse Dash7 message
        		if (inBuffer[4] != STDash7Coordinator.ALP_ID || (inBuffer[5] != STDash7Coordinator.ALP_CMD_READ_TEMP_CMD && inBuffer[5] != STDash7Coordinator.ALP_CMD_SET_VALVE_CMD)) continue;
					
	        	dash7PayloadLen = 0;
	        	dash7PacketOffset = 0;
	        	dash7PacketLen = 0;
	        	
    			do{
					dash7PayloadLen = inBuffer[2+dash7PacketOffset];
					dash7PacketLen = OTComSocket.NDEF_HEADER_SIZE + OTComSocket.NDEF_FOOTER_SIZE + dash7PayloadLen;

        			//Sequence
    				int sequence = (inBuffer[dash7PacketOffset+OTComSocket.NDEF_HEADER_SIZE+dash7PayloadLen+1] & 0xFF) + ((inBuffer[dash7PacketOffset+OTComSocket.NDEF_HEADER_SIZE+dash7PayloadLen] << 8) & 0xFF00) ;   				
    				
    				String output = String.format("--> Dash7 Packet[%d]: [ ",OTComSocket.NDEF_HEADER_SIZE+dash7PayloadLen+OTComSocket.NDEF_FOOTER_SIZE);
    				for(int i=0; i < dash7PacketLen; i++) {
    					output += String.format("0x%02X ", inBuffer[dash7PacketOffset+i]);
    					if (i == OTComSocket.NDEF_HEADER_SIZE - 1) output += String.format("] [ ");
    					if (i == dash7PacketLen - OTComSocket.NDEF_FOOTER_SIZE-1) output += String.format("] [ ");
    				}
    				output += String.format("] Sequence: %d",sequence);
    				System.out.println(output);

    				//Send message to Dash7 node
    				byte[] payload = new byte[dash7PayloadLen];
    				for (int i=0; i < payload.length ; i++) payload[i] = inBuffer[dash7PacketOffset+OTComSocket.NDEF_HEADER_SIZE + i];
            		
    				//Start new node simulator...
    				new NodeSimulator(inBuffer[dash7PacketOffset+5],payload,sequence).start();
    				
    				dash7PacketOffset += dash7PacketLen;
    				
    				System.out.printf("Bytes read %d, processed %d\n",messageLength,dash7PacketOffset);
				
				} while(dash7PacketOffset < messageLength);
        		
    			System.out.println("***All the requests have been sent to the nodes***");
            }
			}
	    	catch (IOException | InterruptedException e) {
				e.printStackTrace();
				notifyAll();
			}
        }
	}
}
