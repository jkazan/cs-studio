package org.csstudio.platform.internal.model;

import org.csstudio.platform.model.AbstractControlSystemItem;
import org.csstudio.platform.model.CentralItemFactory;
import org.csstudio.platform.model.IArchiveDataSource;
import org.csstudio.platform.model.IProcessVariableWithArchive;
import org.csstudio.platform.model.IProcessVariableWithSample;

/**
 * Minimal <code>IProcessVariableNameWithArchiveDataSource</code>
 * implementation.
 * <p>
 * The drag-and-drop transfer uses it internally.<br>
 * Applications which need to provide IArchiveDataSource can use this, but can
 * also implement the interface themselves.
 * 
 * @author Jan Hatje und Helge Rickens
 */
public class ProcessVariableWithSample extends AbstractControlSystemItem
		implements IProcessVariableWithSample {

	private double[] sampleValue;
	private double[] timeStamp;
	private int dbrType;
	private String egu;
	private int precision;
	private double low;
	private double high;
	
	/**
	 * @param pvName
	 * @param sampleValue
	 * @param timeStamp
	 * @param dbrType
	 * @param egu
	 * @param precision
	 * @param low
	 * @param high
	 */
	public ProcessVariableWithSample(final String pvName, double[] sampleValue, double[] timeStamp, int dbrType, String egu, int precision, double low, double high) {
		super(pvName);
		this.sampleValue = sampleValue;
		this.timeStamp = timeStamp;
		this.dbrType = dbrType;
		this.egu = egu;
		this.precision = precision;
		this.low = low;
		this.high = high;
	}


	/**
	 * {@inheritDoc}
	 */
	public final String getTypeId() {
		return TYPE_ID;
	}

	public int getDBRTyp() {
		return dbrType;
	}

	public String getEGU() {
		return egu;
	}

	public double getHigh() {
		return high;
	}

	public double getLow() {
		return low;
	}

	public int getPrecision() {
		return precision;
	}

	public double[] getSampleValue() {
		return sampleValue;
	}

	public double[] getTimeStamp() {
		return timeStamp;
	}
}
