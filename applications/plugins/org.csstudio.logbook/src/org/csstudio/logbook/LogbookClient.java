package org.csstudio.logbook;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

/**
 * The interface for the logbook client.
 * 
 * @author shroffk
 *
 */
public interface LogbookClient {
	
	/**
	 * @return
	 */
	public Collection<Logbook> listLogbooks();
	
	/**
	 * @return
	 */
	public Collection<Tag> listTags();
	
	/**
	 * @return
	 */
	public Collection<Property> listProperties();
	
	/**
	 * 
	 * @param logId
	 * @return
	 */
	public Collection<Attachment> listAttachments(Object logId);
	
	/**
	 * 
	 * @param logId
	 * @param attachment
	 * @return
	 */
	public InputStream getAttachment(Object logId, String attachmentFileName);
	
	/**
	 * @param logId
	 * @return
	 * @throws Exception 
	 */
	public LogEntry findLogEntry(Object logId) throws Exception;
	
	/**
	 * @return
	 * @throws Exception 
	 */
	public Collection<LogEntry> findLogEntries(Map<String,String> findAttributeMap) throws Exception;
	
	/**
	 * @param logEntry
	 * @return 
	 * @throws Exception 
	 */
	public LogEntry createLogEntry(LogEntry logEntry) throws Exception;
	
	/**
	 * @param logEntry
	 * @return 
	 * @throws Exception
	 */
	public LogEntry updateLogEntry(LogEntry logEntry) throws Exception;
	
	/**
	 * @param logEntires
	 */
	public void updateLogEntries(Collection<LogEntry> logEntires);

	/**
	 * Attach the file to the log
	 * 
	 * @param logId
	 * @param attachment
	 * @return
	 */
	public Attachment addAttachment(Object logId, InputStream file);
}
