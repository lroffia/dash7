package dash7Adapter;

import java.io.IOException;
import java.util.Observable;

public class STDash7Coordinator extends OTComSocket {
	
	char sequence = 0x0000;
	
	// ST Protocol Payload format
	// Node Address Len (1 Byte) | Node Address (1-32 bytes) | Payload len (1 byte) | Payload (0-221 bytes)
	public static final byte ALP_ID = (byte) 0xF8;
	
	public static final byte ALP_GET_TEMPERATURE_REQUEST = 0x08;
	public static final byte ALP_TEMPERATURE_REPLY = (byte) 0x18;
	
	public static final byte ALP_SET_VALVE_REQUEST = 0x28;
	public static final byte ALP_VALVE_REPLY = (byte) 0x38;
	
	public static final byte ALP_GET_VALVE_STATUS_REQUEST = 0x48;
	public static final byte ALP_STATUS_REPLY = 0x58;
	
	public static final byte ALP_GET_BATTERY_STATUS_REQUEST = 0x68;
	public static final byte ALP_BATTERY_REPLY = 0x78;
	
	public static final byte ALP_BEACON = (byte) 0x10;
	
	public static final byte ALP_UNKNOWN_CMD_REPLY= (byte) 0xFE;
	
	public static final byte ST_DASH7_NACK = (byte) 0xFF;
	public static final byte ST_DASH7_ACK = (byte) 0x00;
	public static final byte ST_STATUS_RUNNING = (byte) 0x01;
	public static final byte ST_STATUS_STOPPED = (byte) 0x00;
	
	public static boolean isCommandImplemented(byte cmd) {
		return (cmd == ALP_GET_TEMPERATURE_REQUEST ||
				cmd == ALP_SET_VALVE_REQUEST ||
				cmd == ALP_GET_VALVE_STATUS_REQUEST || 
				cmd == ALP_GET_BATTERY_STATUS_REQUEST);
	}
	
	public class Notify {
		public String id;
	}
	public class StatusNotify extends Notify {
		public String status;
	}
	public class TemperatureNotify extends Notify {
		public float temperature;
	}
	public class ValveNotify extends Notify {
		public boolean ack;
	}
	public class BatteryNotify extends Notify {
		public float vBatt;
	}
	public class BeaconNotify extends Notify {
		public String status;
	}
	
	//Notify DASH7 payload
	@Override
	public void update(Observable o, Object arg) {
		byte[] packet = (byte[])arg;
		
		if (packet[4] != ALP_ID) return;
		
		if (packet[5] != ALP_TEMPERATURE_REPLY && 
			packet[5] != ALP_VALVE_REPLY &&
			packet[5] != ALP_STATUS_REPLY &&
			packet[5] != ALP_BATTERY_REPLY && 
			packet[5] != ALP_BEACON
			) return;
		
		byte[] payload = OTComSocket.GetDash7Payload(packet);
		
		Notify notify = null;
		
		switch(packet[5]){
			case ALP_TEMPERATURE_REPLY:
				notify = new TemperatureNotify();
				((TemperatureNotify)notify).temperature = GetTemp(payload);
				break;
			case ALP_VALVE_REPLY:
				notify = new ValveNotify();
				((ValveNotify)notify).ack = GetValveAck(payload);
				break;
			case ALP_STATUS_REPLY:
				notify = new StatusNotify();
				((StatusNotify)notify).status = GetStatus(payload);
				break;
			case ALP_BATTERY_REPLY:
				notify = new BatteryNotify();
				((BatteryNotify)notify).vBatt = GetBatteryStatus(payload);
				break;
			case ALP_BEACON:
				notify = new BeaconNotify();
				((BeaconNotify)notify).status = getBeaconStatus(payload);
				break;
		}
		notify.id = GetID(payload);
		
		setChanged();
		notifyObservers(notify);
	}
	
	private String getBeaconStatus(byte[] payload) {
		//Status code
		//0x00 0x00 ==> heart beat
		//0xAA 0XAA ==> water
		switch(payload[payload.length-1]) {
			case 0x00:
				switch(payload[payload.length-2]) {
					case 0x00: return "Alive";
				} 	
				break;
			case (byte) 0xAA:
				switch(payload[payload.length-2]) {
					case (byte) 0xAA: return "Water";
				} 	
				break;
		}
		
		return "Unknown";
	}
	
	private String GetID(byte[] payload) {
		String id = "";
		byte idLen = (byte)(payload[0] & 0xFF);
		for(int i=0;i < idLen;i++) id += String.format("%c", (char)payload[i+1]);
		
		return id;
	}
	
	private float GetTemp(byte[] payload) {
		float temp = (float) (payload[payload.length-2] + ((payload[payload.length-1] >> 6) & 0x03) * 0.25f);
		return temp;
	}
	
	private boolean GetValveAck(byte[] payload){
		return (payload[payload.length-1] == STDash7Coordinator.ST_DASH7_ACK);
	}
	
	private String GetStatus(byte[] payload) {
		int value = 0;
		if ((payload[payload.length-5] & 0xFF) == ST_STATUS_RUNNING) {
			value = 100 - (payload[payload.length-4] & 0xFF);
			return " RUNNING to value " + String.format("%d", value) + "%";	
		}
		else {
			value = 100 - (payload[payload.length-4] & 0xFF);
			return " STOPPED at value " + String.format("%d", value) + "%";		
		}
	}
	
	private float GetBatteryStatus(byte[] payload) {
		float FULL_SCALE = 4095;
		float K = 1.9f;
		int vrefIntCal =  ((payload[payload.length-6] << 8) & 0xFF00) + (payload[payload.length-5] & 0x00FF);
		int vrefIntData = ((payload[payload.length-4] << 8) & 0xFF00) + (payload[payload.length-3] & 0x00FF);
		int vrefMonData = ((payload[payload.length-2] << 8) & 0xFF00) + (payload[payload.length-1] & 0x00FF);
		
		float temp = (float) (3*K*vrefIntCal*vrefMonData/(vrefIntData* FULL_SCALE));
		return temp;	
	}
	
	public void SendReadTempCommand(byte[] node_id) throws IOException {
		byte [] payload = new byte[node_id.length + 2];
		
		payload[0] = (byte) (node_id.length & 0xFF); // Address len
		for (int i=0; i < node_id.length; i++) payload[1+i] = node_id[i]; // Address
		payload[payload.length - 1] = 0; // Data len = 0
		
		byte[] dash7 = OTComSocket.Dash7Packet(ALP_ID, ALP_GET_TEMPERATURE_REQUEST,payload,sequence++); 
		
		writeOverOTCom(dash7);
	}
	
	public void SendSetValveCommand(byte[] node_id,byte value) throws IOException {
		byte[] payload = new byte[node_id.length + 3];
		
		payload[0] = (byte) (node_id.length & 0xFF); // Address len
		for (int i=0; i < node_id.length ; i++) payload[1+i] = node_id[i]; // Address
		payload[payload.length - 2 ] = 0x01; // Data len = 1
		payload[payload.length - 1 ] = value; // Data
		
		byte[] dash7 = OTComSocket.Dash7Packet(ALP_ID, ALP_SET_VALVE_REQUEST,payload,sequence++); 
		
		writeOverOTCom(dash7);
	}
	
	public void SendGetStatusCommand(byte[] node_id) throws IOException {
		byte [] payload = new byte[node_id.length + 2];
		
		payload[0] = (byte) (node_id.length & 0xFF); // Address len
		for (int i=0; i < node_id.length; i++) payload[1+i] = node_id[i]; // Address
		payload[payload.length - 1] = 0; // Data len = 0
		
		byte[] dash7 = OTComSocket.Dash7Packet(ALP_ID, ALP_GET_VALVE_STATUS_REQUEST,payload,sequence++); 
		
		writeOverOTCom(dash7);
	}
	
	public void SendGetBatteryStatusCommand(byte[] node_id) throws IOException {
		byte [] payload = new byte[node_id.length + 2];
		
		payload[0] = (byte) (node_id.length & 0xFF); // Address len
		for (int i=0; i < node_id.length; i++) payload[1+i] = node_id[i]; // Address
		payload[payload.length - 1] = 0; // Data len = 0
		
		byte[] dash7 = OTComSocket.Dash7Packet(ALP_ID, ALP_GET_BATTERY_STATUS_REQUEST,payload,sequence++); 
		
		writeOverOTCom(dash7);
	}
}

