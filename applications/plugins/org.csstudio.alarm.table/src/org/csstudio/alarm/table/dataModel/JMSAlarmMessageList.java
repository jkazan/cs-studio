package org.csstudio.alarm.table.dataModel;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.Vector;

import javax.jms.JMSException;
import javax.jms.MapMessage;

import org.csstudio.alarm.table.JmsLogsPlugin;

public class JMSAlarmMessageList extends JMSMessageList {

	public JMSAlarmMessageList(String[] propNames) {
		super(propNames);
	}

	/**
	 * Add a new JMSMessage to the collection of JMSMessages.
	 * 
	 */
	synchronized public void addJMSMessage(MapMessage mm) {
		if (mm == null) {
			return;
		} else {
			String severity = null;
			try {
				severity = mm.getString("SEVERITY");
			} catch (JMSException e) {
				JmsLogsPlugin.logException("No SEVERITY in message", e);
			}
			if (severity != null) {
				if ((deleteEqualMessages(mm))
						|| (severity.equalsIgnoreCase("NO_ALARM")) == false) {
					JMSMessage jmsm = addMessageProperties(mm);
					JMSMessages.add(JMSMessages.size(), jmsm);
					Iterator iterator = changeListeners.iterator();
					while (iterator.hasNext())
						((IJMSMessageViewer) iterator.next())
								.addJMSMessage(jmsm);
				}
			}
		}
	}

	/**
	 * Delete previous messages from the same pv and with the same severity Mark
	 * messages from the same pv and with a different severety that the label
	 * provider can set a brighter color. (It is important to use the
	 * <code>removeMessage</code> method from <code>MessageList</code> that
	 * the changeListeners on the model were actualised.
	 * 
	 * @param mm
	 *            The new MapMessage
	 * @return Is there a previous message in the list with the same pv name
	 */
	private boolean deleteEqualMessages(MapMessage mm) {
		if (mm == null) {
			return false;
		}
		boolean equalPreviousMessage = false;
		Iterator<JMSMessage> it = JMSMessages.listIterator();
		List<JMSMessage> jmsMessagesToRemove = new ArrayList<JMSMessage>();
		List<JMSMessage> jmsMessagesToRemoveAndAdd = new ArrayList<JMSMessage>();
		try {
			String newPVName = mm.getString("NAME");
			String newSeverity = mm.getString("SEVERITY");

			if ((newPVName != null) && (newSeverity != null)) {
				while (it.hasNext()) {
					JMSMessage jmsm = it.next();
					String pvNameFromList = jmsm.getProperty("NAME");
					String severityFromList = jmsm.getProperty("SEVERITY");
					if ((pvNameFromList != null) && (severityFromList != null)) {

						if (newPVName.equalsIgnoreCase(pvNameFromList)) {
							equalPreviousMessage = true;
							if (newSeverity.equalsIgnoreCase(severityFromList)) {
								jmsMessagesToRemove.add(jmsm);
							} else {
								jmsMessagesToRemove.add(jmsm);
								if (severityFromList
										.equalsIgnoreCase("NO_ALARM") == false) {
									jmsMessagesToRemoveAndAdd.add(jmsm);
								}
                                if(jmsm.getProperty("ACK").toUpperCase().equals("TRUE")){
                                    jmsMessagesToRemove.add(jmsm);
                                }else{
                                    jmsm.setBackgroundColorGray(true);
                                }
							}
						}
					}
				}
			}
			it = jmsMessagesToRemove.listIterator();
			while (it.hasNext()) {
				removeJMSMessage(it.next());
			}

			it = jmsMessagesToRemoveAndAdd.listIterator();
			while (it.hasNext()) {
				addJMSMessage(it.next());
			}

			jmsMessagesToRemove.clear();
			jmsMessagesToRemoveAndAdd.clear();
		} catch (JMSException e) {
			JmsLogsPlugin.logException("No SEVERITY in message", e);
		}
		return equalPreviousMessage;
	}
}