/*
 * Copyright (c) 2008 Stiftung Deutsches Elektronen-Synchrotron,
 * Member of the Helmholtz Association, (DESY), HAMBURG, GERMANY.
 *
 * THIS SOFTWARE IS PROVIDED UNDER THIS LICENSE ON AN "../AS IS" BASIS.
 * WITHOUT WARRANTY OF ANY KIND, EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR PARTICULAR PURPOSE AND
 * NON-INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 * THE USE OR OTHER DEALINGS IN THE SOFTWARE. SHOULD THE SOFTWARE PROVE DEFECTIVE
 * IN ANY RESPECT, THE USER ASSUMES THE COST OF ANY NECESSARY SERVICING, REPAIR OR
 * CORRECTION. THIS DISCLAIMER OF WARRANTY CONSTITUTES AN ESSENTIAL PART OF THIS LICENSE.
 * NO USE OF ANY SOFTWARE IS AUTHORIZED HEREUNDER EXCEPT UNDER THIS DISCLAIMER.
 * DESY HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS,
 * OR MODIFICATIONS.
 * THE FULL LICENSE SPECIFYING FOR THE SOFTWARE THE REDISTRIBUTION, MODIFICATION,
 * USAGE AND OTHER RIGHTS AND OBLIGATIONS IS INCLUDED WITH THE DISTRIBUTION OF THIS
 * PROJECT IN THE FILE LICENSE.HTML. IF THE LICENSE IS NOT INCLUDED YOU MAY FIND A COPY
 * AT HTTP://WWW.DESY.DE/LEGAL/LICENSE.HTM
 */

package org.csstudio.alarm.treeView.ldap;

import java.net.URL;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;

import org.csstudio.alarm.treeView.model.AbstractAlarmTreeNode;
import org.csstudio.alarm.treeView.model.IAlarmTreeNode;
import org.csstudio.alarm.treeView.model.ObjectClass;
import org.csstudio.alarm.treeView.model.ProcessVariableNode;
import org.csstudio.alarm.treeView.model.SubtreeNode;
import org.csstudio.platform.logging.CentralLogger;
import org.csstudio.utility.ldap.engine.Engine;

/**
 * Editor for the alarm tree in the LDAP directory. The methods of this class
 * update the LDAP directory and also the tree model.
 * 
 * @author Joerg Rathlev
 */
public final class DirectoryEditor {

	/**
	 * The root below which the direcoty is edited.
	 */
	// TODO: refactor (code duplication, see LdapDirectoryReader)
	private static final String ALARM_ROOT = "ou=EpicsAlarmCfg";

	/**
	 * The logger that is used by this class.
	 */
	private static final CentralLogger LOG = CentralLogger.getInstance();

	/**
	 * The directory that is searched.
	 */
	private static DirContext _directory;
	
	
	static {
		_directory = Engine.getInstance().getLdapDirContext();
	}
	
	
	/**
	 * Private constructor.
	 */
	private DirectoryEditor() {
	}
	
	
	/**
	 * Deletes the given node from the directory.
	 * 
	 * @param node
	 *            the node to delete.
	 * @throws DirectoryEditException
	 *             if the node could not be deleted.
	 */
	public static void delete(final IAlarmTreeNode node)
			throws DirectoryEditException {
		String name = fullName(node);
		try {
			LOG.debug(DirectoryEditor.class, "Unbinding " + name);
			_directory.unbind(name);
			node.getParent().remove(node);
		} catch (NamingException e) {
			LOG.error(DirectoryEditor.class,
					"Error unbinding directory entry", e);
			throw new DirectoryEditException(e.getMessage(), e);
		}
	}
	
	
	/**
	 * Modifies the help guidance attribute of the given node in the directory.
	 * 
	 * @param node
	 *            the node.
	 * @param value
	 *            the new value for the attribute. Use <code>null</code> to
	 *            remove the attribute.
	 * @throws DirectoryEditException
	 *             if the attribute could not be modified.
	 */
	public static void modifyHelpGuidance(final AbstractAlarmTreeNode node,
			final String value) throws DirectoryEditException {
		modifyAttribute(node, "epicsHelpGuidance", value);
		node.setHelpGuidance(value);
	}
	
	
	/**
	 * Modifies the help page attribute of the given node in the directory.
	 * 
	 * @param node
	 *            the node.
	 * @param value
	 *            the new value for the attribute. Use <code>null</code> to
	 *            remove the attribute.
	 * @throws DirectoryEditException
	 *             if the attribute could not be modified.
	 */
	public static void modifyHelpPage(final AbstractAlarmTreeNode node,
			final URL value) throws DirectoryEditException {
		modifyAttribute(node, "epicsHelpPage", value.toString());
		node.setHelpPage(value);
	}
	
	
	/**
	 * Modifies the CSS alarm display attribute of the given node in the
	 * directory.
	 * 
	 * @param node
	 *            the node.
	 * @param value
	 *            the new value for the attribute. Use <code>null</code> to
	 *            remove the attribute.
	 * @throws DirectoryEditException
	 *             if the attribute could not be modified.
	 */
	public static void modifyCssAlarmDisplay(final AbstractAlarmTreeNode node,
			final String value) throws DirectoryEditException {
		modifyAttribute(node, "epicsCssAlarmDisplay", value);
		node.setCssAlarmDisplay(value);
	}
	
	
	/**
	 * Modifies the CSS display attribute of the given node in the directory.
	 * 
	 * @param node
	 *            the node.
	 * @param value
	 *            the new value for the attribute. Use <code>null</code> to
	 *            remove the attribute.
	 * @throws DirectoryEditException
	 *             if the attribute could not be modified.
	 */
	public static void modifyCssDisplay(final AbstractAlarmTreeNode node,
			final String value) throws DirectoryEditException {
		modifyAttribute(node, "epicsCssDisplay", value);
		node.setCssDisplay(value);
	}
	
	
	/**
	 * Modifies the CSS strip chart attribute of the given node in the
	 * directory.
	 * 
	 * @param node
	 *            the node.
	 * @param value
	 *            the new value for the attribute. Use <code>null</code> to
	 *            remove the attribute.
	 * @throws DirectoryEditException
	 *             if the attribute could not be modified.
	 */
	public static void modifyCssStripChart(final AbstractAlarmTreeNode node,
			final String value) throws DirectoryEditException {
		modifyAttribute(node, "epicsCssStripChart", value);
		node.setCssStripChart(value);
	}
	
	
	/**
	 * Modifies an attribute of a directory entry.
	 * 
	 * @param node
	 *            the node.
	 * @param attribute
	 *            the id of the attribute to modify.
	 * @param value
	 *            the new attribute value. Use <code>null</code> to remove the
	 *            attribute.
	 * @throws DirectoryEditException
	 *             if the attribute could not be modified.
	 */
	private static void modifyAttribute(final AbstractAlarmTreeNode node,
			final String attribute, final String value)
			throws DirectoryEditException {
		String name = fullName(node);
		Attribute attr = new BasicAttribute(attribute, value);
		int op = value == null
				? DirContext.REMOVE_ATTRIBUTE : DirContext.REPLACE_ATTRIBUTE;
		ModificationItem[] mods = new ModificationItem[] {
				new ModificationItem(op, attr) };
		try {
			LOG.debug(DirectoryEditor.class, name + ": " + mods[0]);
			_directory.modifyAttributes(name, mods);
		} catch (NamingException e) {
			LOG.error(DirectoryEditor.class, "Failed: " + name + ": " + mods[0], e);
			throw new DirectoryEditException(e.getMessage(), e);
		}
	}


	/**
	 * Creates an entry for a process variable record (eren) in the directory
	 * below the given parent.
	 * 
	 * @param parent
	 *            the parent node.
	 * @param recordName
	 *            the name of the process variable record.
	 * @throws DirectoryEditException if the entry could not be created.
	 */
	public static void createProcessVariableRecord(
			final SubtreeNode parent, final String recordName)
			throws DirectoryEditException {
		createEntry(fullName(parent), recordName, ObjectClass.RECORD);
		new ProcessVariableNode(parent, recordName);
	}
	
	
	/**
	 * Creates an entry for a component (ecom) in the directory below the given
	 * parent.
	 * 
	 * @param parent
	 *            the parent node.
	 * @param componentName
	 *            the name of the component.
	 * @throws DirectoryEditException
	 *             if the entry could not be created.
	 */
	public static void createComponent(final SubtreeNode parent,
			final String componentName) throws DirectoryEditException {
		createEntry(fullName(parent), componentName, ObjectClass.COMPONENT);
		new SubtreeNode(parent, componentName, ObjectClass.COMPONENT);
	}
	
	
	/**
	 * Creates a new directory entry.
	 * 
	 * @param parentName
	 *            the full name of the parent entry.
	 * @param name
	 *            the name of the entry to be created.
	 * @param objectClass
	 *            the object class of the entry to be created.
	 * @throws DirectoryEditException
	 *             if the entry could not be created.
	 */
	private static void createEntry(final String parentName, final String name,
			final ObjectClass objectClass) throws DirectoryEditException {
		String fullName = objectClass.getRdnAttribute() + "=" + name
				+ "," + parentName;
		Attributes attrs = attributesForEntry(objectClass, name);
		try {
			LOG.debug(DirectoryEditor.class,
					"Creating entry " + fullName + " with attributes " + attrs);
			_directory.bind(fullName, null, attrs);
		} catch (NamingException e) {
			LOG.error(DirectoryEditor.class,
					"Error creating directory entry", e);
			throw new DirectoryEditException(e.getMessage(), e);
		}
	}


	/**
	 * Returns the full name of the given node in the directory.
	 * 
	 * @param node
	 *            the node.
	 * @return the full name of the node in the directory.
	 */
	private static String fullName(final IAlarmTreeNode node) {
		StringBuilder sb = new StringBuilder();
		if (node instanceof ProcessVariableNode) {
			sb.append("eren=").append(node.getName()).append(",")
				.append(node.getParent().getDirectoryName());
		} else {
			sb.append(((SubtreeNode) node).getDirectoryName());
		}
		sb.append(",").append(ALARM_ROOT);
		return sb.toString();
	}
	

	/**
	 * Returns the attributes for a new entry with the given object class and
	 * name.
	 * 
	 * @param objectClass
	 *            the object class of the new entry.
	 * @param name
	 *            the name of the new entry.
	 * @return the attributes for the new entry.
	 */
	private static Attributes attributesForEntry(final ObjectClass objectClass,
			final String name) {
		BasicAttributes result = new BasicAttributes();
		result.put("objectClass", objectClass.getObjectClassName());
		result.put(objectClass.getRdnAttribute(), name);
		result.put("epicsCssType", objectClass.getCssType());
		return result;
	}
}
