package org.csstudio.diag.interconnectionServer.server;

public class PreferenceProperties {
	
	//
	// all of these properties should finally be defined
	// in a preference page of the final implementation
	// in a headles Eclipse plugin
	//
	
	public static String XMPP_USER_NAME = "icserver";				// PP
	
	public static int DATA_PORT_NUMBER = 18324;				// PP
	public static int COMMAND_PORT_NUMBER = 18325;			// PP

	public static int SENT_START_ID	= 5000000;				// PP
	
	//
	// RMI message transfer takes about 300mS
	//	SECONDARY_JMS_URL
	
	// public static String JMS_CONTEXT_FACTORY = "org.apache.activemq.jndi.ActiveMQInitialContextFactory"; // 
	public static String JMS_CONTEXT_FACTORY = "ACTIVEMQ";	// PP
	
	public static long JMS_TIME_TO_LIVE_ALARMS 		= 3600000;  //	60min X 60 sec X 1000 ms	(1hour) PP
	public static long JMS_TIME_TO_LIVE_LOGS 		=  600000;  //	10min X 60 sec X 1000 ms	(10min) PP
	public static long JMS_TIME_TO_LIVE_PUT_LOGS 	= 3600000;  //	60min X 60 sec X 1000 ms	(1hour) PP

	///public static String PRIMARY_JMS_URL = "tcp://elogbook.desy.de:64616";	//TCP
	public static String PRIMARY_JMS_URL = "failover:(tcp://krynfs.desy.de:62616,tcp://krykjmsb.desy.de:64616)?maxReconnectDelay=500,maxReconnectAttempts=50";	//TCP PP
	//public static String PRIMARY_JMS_URL = "failover:(tcp://krynfs.desy.de:62616,tcp://elogbook.desy.de:64616)?maxReconnectDelay=500,maxReconnectAttempts=50";	//TCP
	//public static String SECONDARY_JMS_URL = "rmi://krynfs.desy.de:1099/";
	///public static String SECONDARY_JMS_URL = "tcp://krynfs.desy.de:62616";
	public static String SECONDARY_JMS_URL = "failover:(tcp://krykjmsb.desy.de:64616,tcp://krynfs.desy.de:62616)?maxReconnectDelay=500,maxReconnectAttempts=50";	// PP
	///public static String SECONDARY_JMS_URL = "tcp://elogbook.desy.de:64616";

	
//	public static String SECONDARY_JMS_URL = "rmi://krynfs.desy.de:1099/";	//RMI
//	public static String PRIMARY_JMS_URL = "rmi://krykelog.desy.de:1099/";
	
	
	public static int ERROR_COUNT_BEFORE_SWITCH_JMS_SERVER = 10;
	
	//public static String JMS_URL = "tcp://krykelog.desy.de:3035/";	//TCP
	public static String PROCESS_NAME = "MCL-Test";
	//public static int MAX_NUMBER_OF_CLIENTS = 100;
	public static int MAX_NUMBER_OF_CLIENT_THREADS = 200;
	public static int BUFFER_ZIZE = 65535;
	public static int BEACON_TIMEOUT_PERIOD = 30; //seconds
	public static String DATA_TOKENIZER = ";";
	
	public static String JMS_LOG_CONTEXT = "LOG";
	public static String JMS_ALARM_CONTEXT = "ALARM";
	public static String JMS_PUT_LOG_CONTEXT = "PUT_LOG";
	
	public static String REPLY_IS_OK = "ok";
	public static String REPLY_IS_ERROR = "error";
	public static String REPLY_IS_DONE = "done";
	
	public static String COMMAND_TAKE_OVER 			= "takeOver";
	public static String COMMAND_DISCONNECT 		= "disconnect";
	public static String COMMAND_SEND_ALARM			= "sendAlarm";
	public static String COMMAND_SEND_ALL_ALARMS 	= "sendAllAlarms";
	public static String COMMAND_SEND_STATUS 		= "sendStatus";
	
	public static final int COMMAND_TAKE_OVER_I 			= 0;
	public static final int COMMAND_DISCONNECT_I 			= 1;
	public static final int COMMAND_SEND_ALARM_I			= 2;
	public static final int COMMAND_SEND_ALL_ALARMS_I 	= 3;
	public static final int COMMAND_SEND_STATUS_I 		= 4;
	
	public static String[]	COMMAND_LIST = {COMMAND_TAKE_OVER, COMMAND_DISCONNECT, COMMAND_SEND_ALARM, 
		COMMAND_SEND_ALL_ALARMS, COMMAND_SEND_STATUS };
	
	public static final int	TIME_TO_GET_ANSWER_FROM_IOC_AFTER_COMMAND	= 10000;
	
	public static final int 	TAG_TYPE_LOG_SERVER_REPLY = 1;
	public static final String 	TAG_LOG_SERVER_REPLY = "TEST-KEY";
	public static final int 	TAG_TYPE_IS_TYPE = 2;
	public static final String 	TAG_IS_TYPE = "TYPE";
	public static final int 	TAG_TYPE_IS_ID = 3;
	public static final String 	TAG_IS_ID = "ID";
	public static final int 	TAG_TYPE_IS_REPLY = 4;
	public static final String 	TAG_IS_REPLY = "REPLY";
	public static final int 	TAG_TYPE_IS_COMMAND = 5;
	public static final String 	TAG_IS_COMMAND = "COMMAND";
	
	public static final int BEACON_TIMEOUT = 10;
	
	public static String JMS_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.S";
	
	/*
	 * client request thread properties
	 */
	public static final int CLIENT_REQUEST_THREAD_MAX_NUMBER_DISCONNECT_FROM_IOC	= 500;
	public static final int CLIENT_REQUEST_THREAD_MAX_NUMBER_ALARM_LIMIT			= 50;
	public static final int CLIENT_REQUEST_THREAD_MAX_NUMBER_STOP_LIMIT				= 100;
	public static final int CLIENT_REQUEST_THREAD_UNSUCCESSSFULL_COUNTDOWN			= 25;
	// 2 minutes timout
	public static final int CLIENT_REQUEST_THREAD_TIMEOUT							= 120000; // 2 minutes timout
	

}
