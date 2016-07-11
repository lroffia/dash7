package dash7Adapter;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Window.Type;

import java.io.BufferedReader;
import java.io.IOException;

import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class STSmartComfortRemoteController {

	private JFrame frmStSmartComfort;
	
	//Node names (DASH7 addresses)
	JLabel lblNode0;
	JLabel lblNode1;
	JLabel lblNode2;
	JLabel lblNode3;
	
	//Temperature values
	JLabel lblReading0;
	JLabel lblReading1;
	JLabel lblReading2;
	JLabel lblReading3;
	
	//Target temperature
	JLabel lblTargetTemp;
	
	//Current valve value
	JProgressBar valve;
	
	//Info box
	JLabel lblInfo;
	
	//Expert mode
	JLabel lbltemp;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					STSmartComfortRemoteController window = new STSmartComfortRemoteController();
					window.frmStSmartComfort.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public STSmartComfortRemoteController() {
		initialize();
		
		loadConfig();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmStSmartComfort = new JFrame();
		frmStSmartComfort.setType(Type.UTILITY);
		frmStSmartComfort.setFont(new Font("Dialog", Font.BOLD, 14));
		frmStSmartComfort.setTitle("ST Smart Comfort Remote Controller");
		frmStSmartComfort.setBounds(100, 100, 505, 580);
		frmStSmartComfort.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{100, 15, 100, 15, 100, 15, 100, 15, 0};
		gridBagLayout.rowHeights = new int[]{0, 35, 0, 0, 0, 47, 51, 30, 23, 81, 41, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 0.0, 1.0, 0.0, 1.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		frmStSmartComfort.getContentPane().setLayout(gridBagLayout);
		
		JLabel lblHttpServerIp = new JLabel("HTTP Server IP");
		GridBagConstraints gbc_lblHttpServerIp = new GridBagConstraints();
		gbc_lblHttpServerIp.anchor = GridBagConstraints.EAST;
		gbc_lblHttpServerIp.gridwidth = 2;
		gbc_lblHttpServerIp.insets = new Insets(0, 0, 5, 5);
		gbc_lblHttpServerIp.gridx = 0;
		gbc_lblHttpServerIp.gridy = 0;
		frmStSmartComfort.getContentPane().add(lblHttpServerIp, gbc_lblHttpServerIp);
		
		textServerIP = new JTextField();
		textServerIP.setText("127.0.0.1");
		GridBagConstraints gbc_textServerIP = new GridBagConstraints();
		gbc_textServerIP.gridwidth = 2;
		gbc_textServerIP.insets = new Insets(0, 0, 5, 5);
		gbc_textServerIP.fill = GridBagConstraints.HORIZONTAL;
		gbc_textServerIP.gridx = 2;
		gbc_textServerIP.gridy = 0;
		frmStSmartComfort.getContentPane().add(textServerIP, gbc_textServerIP);
		textServerIP.setColumns(10);
		
		JLabel lblValveNodeId = new JLabel("Valve node");
		GridBagConstraints gbc_lblValveNodeId = new GridBagConstraints();
		gbc_lblValveNodeId.anchor = GridBagConstraints.EAST;
		gbc_lblValveNodeId.gridwidth = 2;
		gbc_lblValveNodeId.insets = new Insets(0, 0, 5, 5);
		gbc_lblValveNodeId.gridx = 4;
		gbc_lblValveNodeId.gridy = 0;
		frmStSmartComfort.getContentPane().add(lblValveNodeId, gbc_lblValveNodeId);
		
		textNodeID = new JTextField();
		textNodeID.setText("NODO1");
		GridBagConstraints gbc_textNodeID = new GridBagConstraints();
		gbc_textNodeID.insets = new Insets(0, 0, 5, 5);
		gbc_textNodeID.fill = GridBagConstraints.HORIZONTAL;
		gbc_textNodeID.gridx = 6;
		gbc_textNodeID.gridy = 0;
		frmStSmartComfort.getContentPane().add(textNodeID, gbc_textNodeID);
		textNodeID.setColumns(10);
		
		JLabel lblSensorReadings = new JLabel("SENSORS READINGS");
		lblSensorReadings.setFont(new Font("Lucida Grande", Font.BOLD, 16));
		GridBagConstraints gbc_lblSensorReadings = new GridBagConstraints();
		gbc_lblSensorReadings.anchor = GridBagConstraints.SOUTH;
		gbc_lblSensorReadings.gridwidth = 8;
		gbc_lblSensorReadings.insets = new Insets(0, 0, 5, 0);
		gbc_lblSensorReadings.gridx = 0;
		gbc_lblSensorReadings.gridy = 1;
		frmStSmartComfort.getContentPane().add(lblSensorReadings, gbc_lblSensorReadings);
		
		lblNode0 = new JLabel("");
		lblNode0.setForeground(Color.RED);
		lblNode0.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		GridBagConstraints gbc_lblNode = new GridBagConstraints();
		gbc_lblNode.insets = new Insets(0, 0, 5, 5);
		gbc_lblNode.gridx = 0;
		gbc_lblNode.gridy = 2;
		frmStSmartComfort.getContentPane().add(lblNode0, gbc_lblNode);
		
		lblNode1 = new JLabel("");
		lblNode1.setForeground(Color.RED);
		lblNode1.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		GridBagConstraints gbc_lblNode_1 = new GridBagConstraints();
		gbc_lblNode_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNode_1.gridx = 2;
		gbc_lblNode_1.gridy = 2;
		frmStSmartComfort.getContentPane().add(lblNode1, gbc_lblNode_1);
		
		lblNode2 = new JLabel("");
		lblNode2.setForeground(Color.RED);
		lblNode2.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		GridBagConstraints gbc_lblNode_2 = new GridBagConstraints();
		gbc_lblNode_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNode_2.gridx = 4;
		gbc_lblNode_2.gridy = 2;
		frmStSmartComfort.getContentPane().add(lblNode2, gbc_lblNode_2);
		
		lblNode3 = new JLabel("");
		lblNode3.setForeground(Color.RED);
		lblNode3.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		GridBagConstraints gbc_lblNode_3 = new GridBagConstraints();
		gbc_lblNode_3.insets = new Insets(0, 0, 5, 5);
		gbc_lblNode_3.gridx = 6;
		gbc_lblNode_3.gridy = 2;
		frmStSmartComfort.getContentPane().add(lblNode3, gbc_lblNode_3);
		
		lblReading0 = new JLabel("--.--");
		lblReading0.setFont(new Font("Lucida Grande", Font.BOLD, 16));
		GridBagConstraints gbc_lblReading = new GridBagConstraints();
		gbc_lblReading.insets = new Insets(0, 0, 5, 5);
		gbc_lblReading.gridx = 0;
		gbc_lblReading.gridy = 3;
		frmStSmartComfort.getContentPane().add(lblReading0, gbc_lblReading);
		
		JLabel lblc = new JLabel("°C");
		GridBagConstraints gbc_lblc = new GridBagConstraints();
		gbc_lblc.insets = new Insets(0, 0, 5, 5);
		gbc_lblc.gridx = 1;
		gbc_lblc.gridy = 3;
		frmStSmartComfort.getContentPane().add(lblc, gbc_lblc);
		
		lblReading1 = new JLabel("--.--");
		lblReading1.setFont(new Font("Lucida Grande", Font.BOLD, 16));
		GridBagConstraints gbc_lblReading_1 = new GridBagConstraints();
		gbc_lblReading_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblReading_1.gridx = 2;
		gbc_lblReading_1.gridy = 3;
		frmStSmartComfort.getContentPane().add(lblReading1, gbc_lblReading_1);
		
		JLabel lblc_1 = new JLabel("°C");
		GridBagConstraints gbc_lblc_1 = new GridBagConstraints();
		gbc_lblc_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblc_1.gridx = 3;
		gbc_lblc_1.gridy = 3;
		frmStSmartComfort.getContentPane().add(lblc_1, gbc_lblc_1);
		
		lblReading2 = new JLabel("--.--");
		lblReading2.setFont(new Font("Lucida Grande", Font.BOLD, 16));
		GridBagConstraints gbc_lblReading_2 = new GridBagConstraints();
		gbc_lblReading_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblReading_2.gridx = 4;
		gbc_lblReading_2.gridy = 3;
		frmStSmartComfort.getContentPane().add(lblReading2, gbc_lblReading_2);
		
		JLabel lblc_2 = new JLabel("°C");
		GridBagConstraints gbc_lblc_2 = new GridBagConstraints();
		gbc_lblc_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblc_2.gridx = 5;
		gbc_lblc_2.gridy = 3;
		frmStSmartComfort.getContentPane().add(lblc_2, gbc_lblc_2);
		
		lblReading3 = new JLabel("--.--");
		lblReading3.setFont(new Font("Lucida Grande", Font.BOLD, 16));
		GridBagConstraints gbc_lblReading_3 = new GridBagConstraints();
		gbc_lblReading_3.insets = new Insets(0, 0, 5, 5);
		gbc_lblReading_3.gridx = 6;
		gbc_lblReading_3.gridy = 3;
		frmStSmartComfort.getContentPane().add(lblReading3, gbc_lblReading_3);
		
		JLabel lblc_3 = new JLabel("°C");
		GridBagConstraints gbc_lblc_3 = new GridBagConstraints();
		gbc_lblc_3.insets = new Insets(0, 0, 5, 0);
		gbc_lblc_3.gridx = 7;
		gbc_lblc_3.gridy = 3;
		frmStSmartComfort.getContentPane().add(lblc_3, gbc_lblc_3);
		
		JButton btnRefresh = new JButton("Refresh");
		btnRefresh.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				lblInfo.setText("Start sensors reading . . .");
				lblReading0.setText("--.--");
				lblReading1.setText("--.--");
				lblReading2.setText("--.--");
				lblReading3.setText("--.--");
				startSensorReading();
			}
		});
		btnRefresh.setFont(new Font("Lucida Grande", Font.BOLD, 16));
		GridBagConstraints gbc_btnRefresh = new GridBagConstraints();
		gbc_btnRefresh.fill = GridBagConstraints.BOTH;
		gbc_btnRefresh.gridwidth = 5;
		gbc_btnRefresh.insets = new Insets(0, 0, 5, 5);
		gbc_btnRefresh.gridx = 1;
		gbc_btnRefresh.gridy = 4;
		frmStSmartComfort.getContentPane().add(btnRefresh, gbc_btnRefresh);
		
		JLabel lblComfortControl = new JLabel("COMFORT CONTROL");
		lblComfortControl.setFont(new Font("Lucida Grande", Font.BOLD, 16));
		GridBagConstraints gbc_lblComfortControl = new GridBagConstraints();
		gbc_lblComfortControl.anchor = GridBagConstraints.SOUTH;
		gbc_lblComfortControl.insets = new Insets(0, 0, 5, 5);
		gbc_lblComfortControl.gridwidth = 3;
		gbc_lblComfortControl.gridx = 0;
		gbc_lblComfortControl.gridy = 5;
		frmStSmartComfort.getContentPane().add(lblComfortControl, gbc_lblComfortControl);
		
		JButton button = new JButton("-");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				targetTemp -= 0.25f;
				lblTargetTemp.setText(String.format("%.2f", targetTemp));
				smartControl();
			}
		});
		
		JButton btnSet = new JButton("SET");
		btnSet.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				lblInfo.setText(String.format("Setting node %s valve at %d . . .",textNodeID.getText(),valve.getValue()));
				new ThreadRequest().start();
			}
			
			class ThreadRequest extends Thread{
				public void run() {
					HTTPDash7Client httpClient = new HTTPDash7Client(textServerIP.getText());
					String ret = httpClient.SetValve(textNodeID.getText(), valve.getValue());
					String[] ack = ret.split("&");
					if (ack[2].toUpperCase().equals("ACK")) lblInfo.setText("ACK");
					else if (ack[2].toUpperCase().equals("NACK")) lblInfo.setText("NACK");
					else lblInfo.setText("ERROR");	
				}
			}
		});
		
		valve = new JProgressBar();
		valve.setFont(new Font("Lucida Grande", Font.BOLD, 16));
		valve.setStringPainted(true);
		valve.setForeground(Color.RED);
		valve.setBackground(Color.YELLOW);
		valve.setValue(50);
		GridBagConstraints gbc_progressBar = new GridBagConstraints();
		gbc_progressBar.anchor = GridBagConstraints.SOUTH;
		gbc_progressBar.insets = new Insets(0, 0, 5, 0);
		gbc_progressBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_progressBar.gridwidth = 5;
		gbc_progressBar.gridx = 3;
		gbc_progressBar.gridy = 5;
		frmStSmartComfort.getContentPane().add(valve, gbc_progressBar);
		btnSet.setFont(new Font("Lucida Grande", Font.BOLD, 16));
		GridBagConstraints gbc_btnSet = new GridBagConstraints();
		gbc_btnSet.fill = GridBagConstraints.BOTH;
		gbc_btnSet.insets = new Insets(0, 0, 5, 5);
		gbc_btnSet.gridx = 0;
		gbc_btnSet.gridy = 6;
		frmStSmartComfort.getContentPane().add(btnSet, gbc_btnSet);
		button.setForeground(Color.BLUE);
		button.setFont(new Font("Lucida Grande", Font.BOLD, 18));
		GridBagConstraints gbc_button = new GridBagConstraints();
		gbc_button.fill = GridBagConstraints.BOTH;
		gbc_button.insets = new Insets(0, 0, 5, 5);
		gbc_button.gridx = 2;
		gbc_button.gridy = 6;
		frmStSmartComfort.getContentPane().add(button, gbc_button);
		
		JButton button_1 = new JButton("+");
		button_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				targetTemp += 0.25f;
				lblTargetTemp.setText(String.format("%.2f", targetTemp));
				smartControl();
			}
		});
		button_1.setForeground(Color.RED);
		button_1.setFont(new Font("Lucida Grande", Font.BOLD, 18));
		GridBagConstraints gbc_button_1 = new GridBagConstraints();
		gbc_button_1.insets = new Insets(0, 0, 5, 5);
		gbc_button_1.fill = GridBagConstraints.BOTH;
		gbc_button_1.gridx = 4;
		gbc_button_1.gridy = 6;
		frmStSmartComfort.getContentPane().add(button_1, gbc_button_1);
		
		lblTargetTemp = new JLabel("--.--");
		lblTargetTemp.setFont(new Font("Lucida Grande", Font.BOLD, 22));
		GridBagConstraints gbc_lblTargettemp = new GridBagConstraints();
		gbc_lblTargettemp.gridwidth = 2;
		gbc_lblTargettemp.insets = new Insets(0, 0, 5, 5);
		gbc_lblTargettemp.gridx = 5;
		gbc_lblTargettemp.gridy = 6;
		frmStSmartComfort.getContentPane().add(lblTargetTemp, gbc_lblTargettemp);
		
		JLabel lblc_4 = new JLabel("°C");
		lblc_4.setFont(new Font("Lucida Grande", Font.PLAIN, 22));
		GridBagConstraints gbc_lblc_4 = new GridBagConstraints();
		gbc_lblc_4.insets = new Insets(0, 0, 5, 0);
		gbc_lblc_4.gridx = 7;
		gbc_lblc_4.gridy = 6;
		frmStSmartComfort.getContentPane().add(lblc_4, gbc_lblc_4);
		
		JLabel lblExpertMode = new JLabel("EXPERT MODE");
		lblExpertMode.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		GridBagConstraints gbc_lblExpertMode = new GridBagConstraints();
		gbc_lblExpertMode.anchor = GridBagConstraints.SOUTH;
		gbc_lblExpertMode.gridwidth = 8;
		gbc_lblExpertMode.insets = new Insets(0, 0, 5, 0);
		gbc_lblExpertMode.gridx = 0;
		gbc_lblExpertMode.gridy = 8;
		frmStSmartComfort.getContentPane().add(lblExpertMode, gbc_lblExpertMode);
		
		JPanel panel = new JPanel();
		panel.setBorder(new LineBorder(new Color(0, 0, 0), 3));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.anchor = GridBagConstraints.NORTH;
		gbc_panel.gridwidth = 8;
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 9;
		frmStSmartComfort.getContentPane().add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{85, 0, 21, 89, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0};
		gbl_panel.columnWeights = new double[]{1.0, 1.0, 0.0, 0.0, 1.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JLabel lblNodeName = new JLabel("Node name:");
		GridBagConstraints gbc_lblNodeName = new GridBagConstraints();
		gbc_lblNodeName.anchor = GridBagConstraints.EAST;
		gbc_lblNodeName.insets = new Insets(0, 0, 5, 5);
		gbc_lblNodeName.gridx = 0;
		gbc_lblNodeName.gridy = 0;
		panel.add(lblNodeName, gbc_lblNodeName);
		
		txtNodo = new JTextField();
		txtNodo.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_txtNodo = new GridBagConstraints();
		gbc_txtNodo.anchor = GridBagConstraints.WEST;
		gbc_txtNodo.insets = new Insets(0, 0, 5, 5);
		gbc_txtNodo.gridx = 1;
		gbc_txtNodo.gridy = 0;
		panel.add(txtNodo, gbc_txtNodo);
		txtNodo.setText("NODO1");
		txtNodo.setColumns(10);
		
		JButton btnStatus = new JButton("Status");
		btnStatus.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				lblInfo.setText(String.format("Getting valve status of node %s . . .",txtNodo.getText()));	
				new ThreadRequest().start();	
			}
			
			class ThreadRequest extends Thread{
				public void run(){
					HTTPDash7Client httpClient = new HTTPDash7Client(textServerIP.getText());
					
					String response = httpClient.GetStatus(txtNodo.getText());
					
					String[] values = response.split("&");
					lblInfo.setText(values[2]);	
				}
			}
		});
		GridBagConstraints gbc_btnStatus = new GridBagConstraints();
		gbc_btnStatus.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnStatus.insets = new Insets(0, 0, 5, 5);
		gbc_btnStatus.gridx = 3;
		gbc_btnStatus.gridy = 0;
		panel.add(btnStatus, gbc_btnStatus);
		
		JButton btnRead = new JButton("Read");
		GridBagConstraints gbc_btnRead = new GridBagConstraints();
		gbc_btnRead.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnRead.insets = new Insets(0, 0, 0, 5);
		gbc_btnRead.gridx = 0;
		gbc_btnRead.gridy = 1;
		panel.add(btnRead, gbc_btnRead);
		btnRead.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				lblInfo.setText(String.format("Reading temperature of node %s . . .",txtNodo.getText()));
				lbltemp.setText("--.--");	
				new ThreadRequest().start();
			}
			
			class ThreadRequest extends Thread{
				public void run(){
					HTTPDash7Client httpClient = new HTTPDash7Client(textServerIP.getText());
					
					String response = httpClient.GetTemperature(txtNodo.getText());
					
					String[] values = response.split("&");
					lbltemp.setText(values[2]);
					lblInfo.setText(values[2]);
				}
			}
		});
		
		lbltemp = new JLabel("--.--");
		GridBagConstraints gbc_lbltemp = new GridBagConstraints();
		gbc_lbltemp.insets = new Insets(0, 0, 0, 5);
		gbc_lbltemp.gridx = 1;
		gbc_lbltemp.gridy = 1;
		panel.add(lbltemp, gbc_lbltemp);
		
		JLabel lblc_5 = new JLabel("°C");
		GridBagConstraints gbc_lblc_5 = new GridBagConstraints();
		gbc_lblc_5.anchor = GridBagConstraints.WEST;
		gbc_lblc_5.insets = new Insets(0, 0, 0, 5);
		gbc_lblc_5.gridx = 2;
		gbc_lblc_5.gridy = 1;
		panel.add(lblc_5, gbc_lblc_5);
		
		JButton btnSet_1 = new JButton("Set");
		GridBagConstraints gbc_btnSet_1 = new GridBagConstraints();
		gbc_btnSet_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSet_1.insets = new Insets(0, 0, 0, 5);
		gbc_btnSet_1.gridx = 3;
		gbc_btnSet_1.gridy = 1;
		panel.add(btnSet_1, gbc_btnSet_1);
		btnSet_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				lblInfo.setText(String.format("Setting node %s valve at %s . . .",txtNodo.getText(),textValue.getText()));
				new ThreadRequest().start();
			}
			
			class ThreadRequest extends Thread{
				public void run(){
					HTTPDash7Client httpClient = new HTTPDash7Client(textServerIP.getText());
					
					String response = httpClient.SetValve(txtNodo.getText(), Integer.parseInt(textValue.getText()));
					
					String[] values = response.split("&");
					lblInfo.setText(values[2]);
				}
			}
		});
		
		textValue = new JTextField();
		textValue.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_textValue = new GridBagConstraints();
		gbc_textValue.insets = new Insets(0, 0, 0, 5);
		gbc_textValue.gridx = 4;
		gbc_textValue.gridy = 1;
		panel.add(textValue, gbc_textValue);
		textValue.setText("50");
		textValue.setColumns(10);
		
		JLabel label_3 = new JLabel("%");
		GridBagConstraints gbc_label_3 = new GridBagConstraints();
		gbc_label_3.anchor = GridBagConstraints.WEST;
		gbc_label_3.gridx = 5;
		gbc_label_3.gridy = 1;
		panel.add(label_3, gbc_label_3);
		
		lblInfo = new JLabel("Info");
		lblInfo.setFont(new Font("Lucida Grande", Font.BOLD, 14));
		lblInfo.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_lblInfo = new GridBagConstraints();
		gbc_lblInfo.gridwidth = 8;
		gbc_lblInfo.gridx = 0;
		gbc_lblInfo.gridy = 10;
		frmStSmartComfort.getContentPane().add(lblInfo, gbc_lblInfo);
		
		nodeLabels.put(0, lblNode0);
		nodeLabels.put(1, lblNode1);
		nodeLabels.put(2, lblNode2);
		nodeLabels.put(3, lblNode3);
		
		sensorReadingLabels.put(0, lblReading0);
		sensorReadingLabels.put(1, lblReading1);
		sensorReadingLabels.put(2, lblReading2);
		sensorReadingLabels.put(3, lblReading3);
	}
	
	private HashMap<String,Integer> nodeAddresses = new HashMap<String,Integer>();
	private HashMap<Integer,JLabel> nodeLabels = new HashMap<Integer,JLabel>();
	private HashMap<Integer,JLabel> sensorReadingLabels = new HashMap<Integer,JLabel>();
	private HashMap<String,Float> sensorReadings = new HashMap<String,Float>();
	
	private Float defaultTargetTemp = 21.00f;
	private Integer defaultValve = 100;
	private Float targetTemp = 0.0f;
	private Integer deltaTRatio = 10;
	
	//private HTTPDash7Client httpClient = null;
	private JTextField txtNodo;
	private JTextField textValue;
	private JTextField textServerIP;
	private JTextField textNodeID;
	
	public synchronized void newReading(String node,Float value) {
		sensorReadings.put(node, value);
		sensorReadingLabels.get(nodeAddresses.get(node)).setText(String.format("%.2f", value));
	}
	
	private class SensorReadingThread implements Runnable {
		private boolean running = true;
		private Set<String> nodes = null;
		private boolean continuos = false;
		private HTTPDash7Client httpClient = null;
		
		public SensorReadingThread(String url,HashMap<String,Integer> nodes) {
			httpClient = new HTTPDash7Client(url);
			this.nodes = nodes.keySet();
		}
		
		@Override
		public void run() {
			Iterator<String> nodeIterator = nodes.iterator();
			if (!nodeIterator.hasNext()) return;
			
			while(running) {
				try 
				{
					Thread.sleep(1000);
				} 
				catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if (!nodeIterator.hasNext()) {
					nodeIterator = nodes.iterator();
					try 
					{
						smartControl();
						if (!continuos) return;
						lblInfo.setText("Sleeping for 60s . . .");
						Thread.sleep(60000);
					} 
					catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				String node = nodeIterator.next();
				lblInfo.setText(String.format("Reading sensor temperature of %s . . .",node));
				
				String ret = httpClient.GetTemperature(node);					
				
				String[] value = ret.split("&");
				if (!value[2].toUpperCase().equals("TIMEOUT") && !value[2].toUpperCase().equals("ERROR") && !value[2].toUpperCase().equals("BUSY")) {
					sensorReadings.put(value[0], Float.parseFloat(value[2].replace(',', '.')));
					nodeLabels.get(nodeAddresses.get(value[0])).setForeground(Color.BLACK);
				}
				else {
					nodeLabels.get(nodeAddresses.get(value[0])).setForeground(Color.RED);
				}
				sensorReadingLabels.get(nodeAddresses.get(value[0])).setText(value[2]);
				
			}
			lblInfo.setText("Sensors reading completed");	
		}
	}
	
	private void loadConfig(){
		Charset charset = Charset.forName("US-ASCII");
		Path path = FileSystems.getDefault().getPath("comfort.cfg");
		try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
		    String line = null;
		    Integer index = 0;
		    while ((line = reader.readLine()) != null) {
		    	if (line.startsWith("#")) continue;
		    	if (line.toUpperCase().startsWith("NODE=")) {
		    		String node = line.split("=")[1];
		    		nodeAddresses.put(node, index++);
		    	}
		    	if (line.toUpperCase().startsWith("TARGETTEMP=")) {
		    		defaultTargetTemp = Float.parseFloat(line.split("=")[1]);
		    	}
		    	if (line.toUpperCase().startsWith("DEFAULTVALVE=")) {
		    		defaultValve = Integer.decode(line.split("=")[1]);
		    	}
		    	if (line.toUpperCase().startsWith("DELTATRATIO=")) {
		    		deltaTRatio = Integer.decode(line.split("=")[1]);
		    	}
		        System.out.println(line);
		    }
		} catch (IOException x) {
		    System.err.format("IOException: %s%n", x);
		}
		
		Iterator<String> names = nodeAddresses.keySet().iterator();
		while(names.hasNext()) {
			String addr = names.next();
			nodeLabels.get(nodeAddresses.get(addr)).setText(addr);
		}
		
		targetTemp = defaultTargetTemp;
		lblTargetTemp.setText(String.format("%.2f", targetTemp));
		
		valve.setValue(defaultValve);
	}
	
	private void smartControl() {
		//Smart comfort
		if(sensorReadings.isEmpty()) {
			lblInfo.setText("At least one temperature reading is needed");	
			return;
		}
		Iterator<String> nodes = sensorReadings.keySet().iterator();
		Integer n = 0;
		Float meanTemp = 0.0f;
		while (nodes.hasNext()) {
			n++;
			meanTemp += sensorReadings.get(nodes.next());
		}
		if (n > 0) {
			meanTemp = meanTemp / n;
			Float deltaT = targetTemp - meanTemp;
			Integer currentValue = defaultValve + Math.round(deltaTRatio * deltaT);
			if (currentValue < 0) currentValue = 0;
			else if (currentValue > 100) currentValue = 100;
			valve.setValue(currentValue);
			lblInfo.setText(String.format("Average temperature: %.2f DeltaT: %.2f Valve: %d",meanTemp,deltaT,currentValue)+"%");
		}
		else lblInfo.setText("At least one temperature reading is needed");	
	}
	
	private void startSensorReading() {
		SensorReadingThread sensors = new SensorReadingThread(textServerIP.getText(), nodeAddresses);
		Thread th = new Thread(sensors);
		th.start();
	}
}
