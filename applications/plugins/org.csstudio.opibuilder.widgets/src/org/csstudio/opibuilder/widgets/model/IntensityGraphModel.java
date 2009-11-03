package org.csstudio.opibuilder.widgets.model;

import org.csstudio.opibuilder.model.AbstractPVWidgetModel;
import org.csstudio.opibuilder.properties.DoubleProperty;
import org.csstudio.opibuilder.properties.IntegerProperty;
import org.csstudio.opibuilder.properties.WidgetPropertyCategory;

/**The model for intensity graph.
 * @author Xihui Chen
 *
 */
public class IntensityGraphModel extends AbstractPVWidgetModel {

	
	public static final String PROP_MIN = "minimum"; //$NON-NLS-1$		
	
	public static final String PROP_MAX = "maximum"; //$NON-NLS-1$		
	
	public static final String PROP_DATA_WIDTH = "data_width"; //$NON-NLS-1$		
	
	public static final String PROP_DATA_HEIGHT = "data_height"; //$NON-NLS-1$	
	
	
	/** The default value of the minimum property. */
	private static final double DEFAULT_MIN = 0;
	
	/** The default value of the maximum property. */
	private static final double DEFAULT_MAX = 255;	
	
	/**
	 * The ID of this widget model.
	 */
	public static final String ID = "org.csstudio.opibuilder.widgets.intensityGraph"; //$NON-NLS-1$	
	
	@Override
	protected void configureProperties() {
		addProperty(new DoubleProperty(PROP_MIN, "Minimum", 
				WidgetPropertyCategory.Behavior, DEFAULT_MIN));
		
		addProperty(new DoubleProperty(PROP_MAX, "Maximum", 
				WidgetPropertyCategory.Behavior, DEFAULT_MAX));
		
		addProperty(new IntegerProperty(PROP_DATA_WIDTH, "Data Width", 
				WidgetPropertyCategory.Behavior, 0));
		
		addProperty(new IntegerProperty(PROP_DATA_HEIGHT, "Data Height", 
				WidgetPropertyCategory.Behavior, 0));
		removeProperty(PROP_BACKCOLOR_ALARMSENSITIVE);
		removeProperty(PROP_COLOR_BACKGROUND);
		removeProperty(PROP_COLOR_FOREGROUND);
		removeProperty(PROP_FORECOLOR_ALARMSENSITIVE);		
	}

	@Override
	public String getTypeID() {
		return ID;
	}


	/**
	 * @return the maximum value
	 */
	public Double getMaximum() {
		return (Double) getCastedPropertyValue(PROP_MAX);
	}

	/**
	 * @return the minimum value
	 */
	public Double getMinimum() {
		return (Double) getCastedPropertyValue(PROP_MIN);
	}

	/**
	 * @return the data width
	 */
	public Integer getDataWidth() {
		return (Integer) getCastedPropertyValue(PROP_DATA_WIDTH);
	}

	/**
	 * @return the data height
	 */
	public Integer getDataHeight() {
		return (Integer) getCastedPropertyValue(PROP_DATA_HEIGHT);
	}
}
