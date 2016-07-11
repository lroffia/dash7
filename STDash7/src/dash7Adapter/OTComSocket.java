package dash7Adapter;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Observable;
import java.util.Observer;

public class OTComSocket extends Observable implements Observer {	
	// *****************
	// NDEF Dash7 Mode 2
	// *****************
	// HEADER
	//  Record	Flags	|	Type	Length	|	Payload Length 	|	ID Length	|	ID		|	Payload
	//  	1 Byte 		|  		1 Byte 		|       1 Byte 		|    1 Byte 	|  2 Bytes 	|	N Bytes
	//		(bitfield) 	|     (always 0) 	|    N (integer) 	|    (always 2) | (See ALP) |	Data 
	
	// FOOTER
	//    SEQ LSB		|	SEQ	MSB			|	CRC16	LSB		|	CRC16	MSB
	//		1 byte		!	1 byte			|	1 byte			|		1 byte
	
	public static final byte NDEF_FLAGS = (byte) 0xDD; // no chunking
	static final byte NDEF_TYPE_LEN = 0;
	static final byte NDEF_ID_LEN = 2;
	
	public static final byte NDEF_FOOTER_SIZE = 4;
	public static final byte NDEF_HEADER_SIZE = 6;
	
	// ----------------------------------
	//
	// CRC table (CRC16 CCITT value)
	//
	// ----------------------------------

	final static char crcLut[] = 
	{
	    0x0000,    0x1021,    0x2042,    0x3063,    0x4084,    0x50A5,    0x60C6,    0x70E7,
	    0x8108,    0x9129,    0xA14A,    0xB16B,    0xC18C,    0xD1AD,    0xE1CE,    0xF1EF,
	    0x1231,    0x0210,    0x3273,    0x2252,    0x52B5,    0x4294,    0x72F7,    0x62D6,
	    0x9339,    0x8318,    0xB37B,    0xA35A,    0xD3BD,    0xC39C,    0xF3FF,    0xE3DE,
	    0x2462,    0x3443,    0x0420,    0x1401,    0x64E6,    0x74C7,    0x44A4,    0x5485,
	    0xA56A,    0xB54B,    0x8528,    0x9509,    0xE5EE,    0xF5CF,    0xC5AC,    0xD58D,
	    0x3653,    0x2672,    0x1611,    0x0630,    0x76D7,    0x66F6,    0x5695,    0x46B4,
	    0xB75B,    0xA77A,    0x9719,    0x8738,    0xF7DF,    0xE7FE,    0xD79D,    0xC7BC,
	    0x48C4,    0x58E5,    0x6886,    0x78A7,    0x0840,    0x1861,    0x2802,    0x3823,
	    0xC9CC,    0xD9ED,    0xE98E,    0xF9AF,    0x8948,    0x9969,    0xA90A,    0xB92B,
	    0x5AF5,    0x4AD4,    0x7AB7,    0x6A96,    0x1A71,    0x0A50,    0x3A33,    0x2A12,
	    0xDBFD,    0xCBDC,    0xFBBF,    0xEB9E,    0x9B79,    0x8B58,    0xBB3B,    0xAB1A,
	    0x6CA6,    0x7C87,    0x4CE4,    0x5CC5,    0x2C22,    0x3C03,    0x0C60,    0x1C41,
	    0xEDAE,    0xFD8F,    0xCDEC,    0xDDCD,    0xAD2A,    0xBD0B,    0x8D68,    0x9D49,
	    0x7E97,    0x6EB6,    0x5ED5,    0x4EF4,    0x3E13,    0x2E32,    0x1E51,    0x0E70,
	    0xFF9F,    0xEFBE,    0xDFDD,    0xCFFC,    0xBF1B,    0xAF3A,    0x9F59,    0x8F78,
	    0x9188,    0x81A9,    0xB1CA,    0xA1EB,    0xD10C,    0xC12D,    0xF14E,    0xE16F,
	    0x1080,    0x00A1,    0x30C2,    0x20E3,    0x5004,    0x4025,    0x7046,    0x6067,
	    0x83B9,    0x9398,    0xA3FB,    0xB3DA,    0xC33D,    0xD31C,    0xE37F,    0xF35E,
	    0x02B1,    0x1290,    0x22F3,    0x32D2,    0x4235,    0x5214,    0x6277,    0x7256,
	    0xB5EA,    0xA5CB,    0x95A8,    0x8589,    0xF56E,    0xE54F,    0xD52C,    0xC50D,
	    0x34E2,    0x24C3,    0x14A0,    0x0481,    0x7466,    0x6447,    0x5424,    0x4405,
	    0xA7DB,    0xB7FA,    0x8799,    0x97B8,    0xE75F,    0xF77E,    0xC71D,    0xD73C,
	    0x26D3,    0x36F2,    0x0691,    0x16B0,    0x6657,    0x7676,    0x4615,    0x5634,
	    0xD94C,    0xC96D,    0xF90E,    0xE92F,    0x99C8,    0x89E9,    0xB98A,    0xA9AB,
	    0x5844,    0x4865,    0x7806,    0x6827,    0x18C0,    0x08E1,    0x3882,    0x28A3,
	    0xCB7D,    0xDB5C,    0xEB3F,    0xFB1E,    0x8BF9,    0x9BD8,    0xABBB,    0xBB9A,
	    0x4A75,    0x5A54,    0x6A37,    0x7A16,    0x0AF1,    0x1AD0,    0x2AB3,    0x3A92,
	    0xFD2E,    0xED0F,    0xDD6C,    0xCD4D,    0xBDAA,    0xAD8B,    0x9DE8,    0x8DC9,
	    0x7C26,    0x6C07,    0x5C64,    0x4C45,    0x3CA2,    0x2C83,    0x1CE0,    0x0CC1,
	    0xEF1F,    0xFF3E,    0xCF5D,    0xDF7C,    0xAF9B,    0xBFBA,    0x8FD9,    0x9FF8,
	    0x6E17,    0x7E36,    0x4E55,    0x5E74,    0x2E93,    0x3EB2,    0x0ED1,    0x1EF0
	};
	
	// ***************
	// OTCom Protocol
	// ***************
	//------------------------------------------------------------------------------------
    //| sync | size (hi) | size (lo) | packet type | pload (0) | ... | ... | pload (N-1) |
    //------------------------------------------------------------------------------------
	//Packet types
	static final byte OTC_PROTOCOL_BAUDRATE_CHANGE_REQUEST = (byte) 0xA0;
	static final byte OTC_PROTOCOL_RECONNECT_COM_PORT = (byte) 0xA2;
	static final byte OTC_PROTOCOL_KILL_OTC = (byte) 0xB1;
	
	static final byte OTC_PROTOCOL_STATUS = (byte) 0xB3;
	static final byte OTC_PROTOCOL_STATUS_RESULT = (byte) 0x02;
	
	static final byte OTC_PROTOCOL_SEND_AS_IS = (byte) 0xB5;
	static final byte OTC_PROTOCOL_RAW_DATA = (byte) 0xB6;
	
	//Sync byte
	static final byte OTC_PROTOCOL_SYNC = (byte) 0xBB;

	static final byte OTC_HEADER_SIZE = 4;
	
	//Socket
	Socket socket = null;
	SocketReaderThread socketThread = null;
	
	//Listening for Dash7 Packets
	public void startRunning(String ip,int port) throws UnknownHostException, IOException{
		socket = new Socket(ip,port);
		socketThread = new SocketReaderThread(socket,1024);
		socketThread.addObserver(this);
		new Thread(socketThread).start();
	}
	
	public void startRunning() throws UnknownHostException, IOException {
		startRunning("127.0.0.1",7700);
	}
	
	public void stopRunning(){
		if (socketThread != null) socketThread.stopRunning();
	}
	
	@Override
	public void update(Observable o, Object arg) {		
		setChanged();
		notifyObservers(arg);
	}
	
 	protected synchronized void writeOverOTCom(byte[] packet) throws IOException {
		System.out.printf("> DASH7 [");
		for (int i=0; i < NDEF_HEADER_SIZE; i++) System.out.printf(" %02X", packet[i]);
		System.out.printf(" ] [");
		for (int i=NDEF_HEADER_SIZE; i < packet.length - NDEF_FOOTER_SIZE; i++) System.out.printf(" %02X", packet[i]);
		System.out.printf(" ] [");
		for (int i=packet.length - NDEF_FOOTER_SIZE; i < packet.length; i++) System.out.printf(" %02X", packet[i]);
		System.out.print(" ]\n");
		
		//Send over OTCom-DASH7
		byte[] otcom = OTComSocket.OTComPacket(packet);
		if (socket != null) socket.getOutputStream().write(otcom);	
	}
	
	//OTCom NDEF Packets
	static byte[] OTComPacket(byte[] payload) {		
		byte[] otcomPacket = new byte[OTC_HEADER_SIZE + payload.length];
		
		//Header
		otcomPacket[0]= OTC_PROTOCOL_SYNC;
		otcomPacket[1]= 0; 								//MSB Payload Len
		otcomPacket[2]= (byte) (payload.length & 0xFF);	//LSB Payload Len
		otcomPacket[3]= OTC_PROTOCOL_SEND_AS_IS;
										
		//Payload
		for (int i=0; i < payload.length ; i++) otcomPacket[OTC_HEADER_SIZE + i] = payload[i];
		
		//Write on socket
		return otcomPacket;	
	}
	
	static byte[] GetDash7Payload(byte[] dash7Packet) {
		byte[] ret = new byte[dash7Packet.length - NDEF_HEADER_SIZE -NDEF_FOOTER_SIZE];
		for (int i=0; i < ret.length; i++) ret[i] = dash7Packet[NDEF_HEADER_SIZE+i];
		return ret;
	}
	
	static byte[] AddCRC16ToPacket(byte[] packet) {
		//CRC
		int crc = 0xFFFF;
		int MSBcrc;
		int index;
		for (int i = 0; i < packet.length - 2; i++)
		{
			MSBcrc = (crc << 8) & 0xFF00;
			index = ((crc >> 8) & 0xFF) ^ (packet[i] & 0xFF);
			    	
			crc = MSBcrc ^ crcLut[index];
		}
			    
		//Footer (CRC)
		packet[packet.length-1] = (byte) (crc & 0xFF);			//LSB
		packet[packet.length-2] = (byte) ((crc >> 8) & 0xFF);	//MSB
		
		return packet;
	}
	
	public static byte[] Dash7Packet(byte id, byte cmd,byte[] payload,char seq) {
		byte[] packet = null;
		
		if (payload != null) packet = new byte[NDEF_HEADER_SIZE+payload.length+NDEF_FOOTER_SIZE];
		else packet = new byte[NDEF_HEADER_SIZE+NDEF_FOOTER_SIZE];
		
		//Header
		packet[0] = NDEF_FLAGS;
		packet[1] = NDEF_TYPE_LEN;
		packet[2] = (byte) (payload.length & 0xFF);
		packet[3] = NDEF_ID_LEN;
		packet[4] = id;
		packet[5] = cmd;
		
		//Footer
		packet[packet.length-3] = (byte)(seq & 0xFF);			//LSB Seq
		packet[packet.length-4] = (byte)((seq >> 8) & 0xFF);	//MSB Seq
		
		//Create packet payload
		if (payload != null) for(int i=0; i < payload.length; i++) packet[NDEF_HEADER_SIZE + i] = payload[i];
		
		return AddCRC16ToPacket(packet);
	}
	
	static byte[] OTComStatusPacket() {	
		byte[] otcomPacket = new byte[OTC_HEADER_SIZE];
		
		//OTCom packet
		otcomPacket[0] = OTC_PROTOCOL_SYNC;
		otcomPacket[1] = 0;
		otcomPacket[2] = 0;
		otcomPacket[3] = OTC_PROTOCOL_STATUS;
		
		return otcomPacket;
	}
	
	class SocketReaderThread extends Observable implements Runnable {
        private boolean running = false;
        private Socket in = null;
    	private byte[] inBuffer = null;
    	
        public SocketReaderThread(Socket in,int bufferSize) {
            this.in = in;
            running = true;
            inBuffer = new byte[bufferSize];
        }

        public void stopRunning(){running = false;}
        
        public void run() {
        	int multiplePacketOffset = 0;

            while(running) {
            	try {
					while(in.getInputStream().available() == 0)
						try 
						{
							Thread.sleep(100);
							if (!running) return;
						} 
						catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
							return;
						}
				}
            	catch (IOException e1) {
            		// TODO Auto-generated catch block
					e1.printStackTrace();
					return;
				}
            	
            	try 
            	{
            		in.getInputStream().read(inBuffer);
					multiplePacketOffset = 0;
				} 
            	catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
            	
            	//Multiple-DASH7 packets in OTCom packet
            	if (inBuffer[0] == OTC_PROTOCOL_SYNC && inBuffer[3] == OTC_PROTOCOL_RAW_DATA){
	            	int OTComPayloadLength = ((((int) inBuffer[1]) << 8) & 0xFF00) + (((int) inBuffer[2]) & 0xFF);
	            	byte[] OTComPayload = new byte[OTComPayloadLength];
	            	for(int i=0; i < OTComPayloadLength; i++) OTComPayload[i] = inBuffer[i+OTC_HEADER_SIZE];
	            	
	            	System.out.printf("< OTCom [");
	            	for (int i=0; i < OTC_HEADER_SIZE; i++) System.out.printf(" %02X", inBuffer[i]);
	            	System.out.printf(" ] [");
	            	for (int i=OTC_HEADER_SIZE; i < OTComPayloadLength + OTC_HEADER_SIZE; i++) System.out.printf(" %02X", inBuffer[i]);
	            	System.out.printf(" ]\n");
	            	
	            	for(int j=0; j < OTComPayloadLength; j = j + multiplePacketOffset) {
	            		//Dash7 payload length
	            		byte payloadLength = OTComPayload[multiplePacketOffset+2];
	            		byte[] packet = new byte[payloadLength+NDEF_HEADER_SIZE+NDEF_FOOTER_SIZE];
	        			for (int i=0; i < payloadLength+NDEF_HEADER_SIZE+NDEF_FOOTER_SIZE;i++)
	        				packet[i] = OTComPayload[multiplePacketOffset+i];
	            		
	        			if (packet[0] != NDEF_FLAGS) break;
	        			String message = "< DASH7 [";
	            		for (int i=0; i < NDEF_HEADER_SIZE; i++) message += String.format(" %02X", packet[i]);
	            		message += " ] [";
	            		for (int i=NDEF_HEADER_SIZE; i < packet.length-NDEF_FOOTER_SIZE; i++) message += String.format(" %02X", packet[i]);
	            		message += " ] [";
	            		for (int i=packet.length-NDEF_FOOTER_SIZE; i < packet.length; i++) message += String.format(" %02X", packet[i]);
	            		message += " ]";
	            		System.out.println(message);
	            		
	            		setChanged();
	            		notifyObservers(packet);
	            		
	            		multiplePacketOffset += payloadLength+NDEF_HEADER_SIZE+NDEF_FOOTER_SIZE;
	            	}
            	}
            }
        }
	}
}
