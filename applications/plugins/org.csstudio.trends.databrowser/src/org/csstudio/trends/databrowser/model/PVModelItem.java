package org.csstudio.trends.databrowser.model;

import java.util.ArrayList;

import org.csstudio.apputil.xml.DOMHelper;
import org.csstudio.apputil.xml.XMLHelper;
import org.csstudio.platform.data.IDoubleValue;
import org.csstudio.platform.data.IEnumeratedMetaData;
import org.csstudio.platform.data.IEnumeratedValue;
import org.csstudio.platform.data.ILongValue;
import org.csstudio.platform.data.IMetaData;
import org.csstudio.platform.data.INumericMetaData;
import org.csstudio.platform.data.IStringValue;
import org.csstudio.platform.data.ITimestamp;
import org.csstudio.platform.data.IValue;
import org.csstudio.platform.data.ValueFactory;
import org.csstudio.platform.model.CentralItemFactory;
import org.csstudio.platform.model.IArchiveDataSource;
import org.csstudio.swt.chart.TraceType;
import org.csstudio.trends.databrowser.Plugin;
import org.csstudio.trends.databrowser.preferences.Preferences;
import org.csstudio.utility.pv.PV;
import org.csstudio.utility.pv.PVFactory;
import org.csstudio.utility.pv.PVListener;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.Element;

/** A model item connected to a live and/or archived PV.
 *  @see Model
 *  @author Kay Kasemir
 */
public class PVModelItem 
       extends AbstractModelItem
       implements IPVModelItem, PVListener
{	
    /** <code>"request"</code> */
    private static final String TAG_REQUEST = "request"; //$NON-NLS-1$

    /** The control system PV from which to get new values. */
    private PV pv;
    
    /** Where to get archived data for this item. */
    final private ArrayList<IArchiveDataSource> archives
        = new ArrayList<IArchiveDataSource>();
    
    /** The most recent value we got from the pv.
     *  <p>
     *  Needs lock:
     *  Read from the GUI thread, updated from a PV monitor thread.
     *  But can't use current_value as lock, since it may be null.
     *  So lock on <code>this</code>.
     */
    private volatile IValue current_value;
    
    /** All the samples of this model item. */
    private ModelSamples samples;
    
    /** How to request samples from the archive. */
    private RequestType request_type;
    
    /** Constructor
     *  @param model Model
     *  @param pv_name Name of the PV
     *  @param display_name
     *  @param live_buffer_size Live sample buffer size
     *  @param axis_index The Y axis to use [0, 1, ...]
     *  @param min,
     *  @param max The Y axis range
     *  @param trace_visible
     *  @param axis_visible
     *  @param auto_scale
     *  @param red,
     *  @param green,
     *  @param blue The color to use
     *  @param line_width
     *  @param trace_type
     *  @param log_scale
     *  @param request_type
     *  <p>
     *  There is no <code>dispose()</code>,
     *  but the ChartItem must not be running when released.
     *  @see #stop
     */
    public PVModelItem(final Model model, final String pv_name,
            final String display_name,
            final int live_buffer_size,
            final int axis_index, final double min, final double max,
            final boolean trace_visible, final boolean axis_visible,
            final boolean auto_scale,
            final int red, final int green, final int blue,
            final int line_width,
            final TraceType trace_type,
            final boolean log_scale,
            final RequestType request_type)
    {
        super(model, pv_name, display_name, axis_index, min, max, trace_visible,
              axis_visible, auto_scale,
              red, green , blue, line_width, trace_type, log_scale);
        pv = createPV(pv_name);
        samples = new ModelSamples(live_buffer_size);
        this.request_type = request_type;
    }

    @SuppressWarnings("nls")
    private PV createPV(String pv_name)
    {
        try
        {
            return PVFactory.createPV(pv_name);
        }
        catch (Exception ex)
        {
            Plugin.getLogger().error("Cannot create PV '" + pv_name + "'", ex);
        }
        return null;
    }    
    
    /** Must be called to dispose the color. */
    @Override
    public void dispose()
    {
        if (pv != null && pv.isRunning())
            pv.stop();
        samples.clear();
        archives.clear();
        super.dispose();
    }
    
    /** @see IModelItem#changeName(String) */
    @Override
    public void changeName(String new_name)
    {
        if (new_name == null)
            return;
        new_name = new_name.trim();
        if (new_name.length() <= 0)
            return;
        // Avoid duplicates, do not allow if new name already in model.
        if (model.findItem(new_name) != null)
            return;
        boolean was_running = pv != null  &&  pv.isRunning();
        if (was_running)
            stop();
        // Name change looks like remove/add back in
        model.fireEntryRemoved(this);
        // Now change name
        name = new_name;
        pv = createPV(name);
        // Invalidate the samples we got because it's now a new PV
        samples.clear();
        // and add
        model.fireEntryAdded(this);
        if (was_running)
            start();
    }

    /** @see IModelItem#getSamples() */
    public final IModelSamples getSamples()
    {
        return samples;
    }
    
    /** @see IPVModelItem#getRequestType() */
    public RequestType getRequestType()
    {
        return request_type;
    }
    
    /** @see IPVModelItem#setRequestType() */
    public void setRequestType(RequestType new_request_type)
    {
        // Any change?
        if (request_type == new_request_type)
            return;
        request_type = new_request_type;
        // Need to get new data
        model.fireEntryArchivesChanged(this);
    }
    
    /** @see IModelItem#addSamples() */
    public void addArchiveSamples(final String source, final IValue samples[])
    {
        this.samples.addArchiveSamples(source, samples);
    }
    
    /** @return Returns an XML string for this item.
     *  @see #loadFromDOM(Model, Element, int)
     */
    @SuppressWarnings("nls")
    @Override
    public String getXMLContent()
    {
        final StringBuilder b = new StringBuilder();
        b.append("        <" + TAG_PV + ">\n");
        addCommonXMLConfig(b);
        XMLHelper.XML(b, 3, TAG_REQUEST, Integer.toString(getRequestType().ordinal()));
        if (archives.size() > 0)
        {
            for (IArchiveDataSource archive : archives)
            {
                XMLHelper.indent(b, 3);b.append("<" + TAG_ARCHIVE + ">\n");
                XMLHelper.XML(b, 4, TAG_NAME, archive.getName());
                XMLHelper.XML(b, 4, TAG_URL, archive.getUrl());
                XMLHelper.XML(b, 4, TAG_KEY, Integer.toString(archive.getKey()));
                XMLHelper.indent(b, 3);b.append("</" + TAG_ARCHIVE + ">\n");
            }
        }
        b.append("        </" + TAG_PV + ">\n");
        return b.toString();
    }
    
    /** Decode XML DOM element for "pv ...".
     *  @see #getXMLContent()
     */
    public static PVModelItem loadFromDOM(Model model, Element pv, int ring_size) throws Exception
    {
        // Common PV stuff
        final String name = DOMHelper.getSubelementString(pv, TAG_NAME);
        if (name.length() <= 0)
            return null;
        final String display_name = DOMHelper.getSubelementString(pv, TAG_DISPLAY_NAME);
        final int axis_index = DOMHelper.getSubelementInt(pv, TAG_AXIS, 0);
        final int line_width = DOMHelper.getSubelementInt(pv, TAG_LINEWIDTH, 0);
        final double min = DOMHelper.getSubelementDouble(pv, TAG_MIN, 0.0);
        final double max = DOMHelper.getSubelementDouble(pv, TAG_MAX, 10.0);
        final boolean trace_visible = DOMHelper.getSubelementBoolean(pv, TAG_VISIBLE, true);
        final boolean axis_visible = DOMHelper.getSubelementBoolean(pv, TAG_AXIS_VISIBLE, true);
        final boolean auto_scale = DOMHelper.getSubelementBoolean(pv, TAG_AUTOSCALE);
        final int rgb[] = loadColorFromDOM(pv);
        final boolean log_scale = DOMHelper.getSubelementBoolean(pv, TAG_LOG_SCALE);
        final TraceType trace_type = loadTraceTypeFromDOM(pv);
        final RequestType request_type = loadRequestTypeFromDOM(pv);
        
        final PVModelItem item =
            new PVModelItem(model, name, display_name, ring_size, axis_index,
                          min, max, trace_visible, axis_visible, auto_scale,
                          rgb[0], rgb[1], rgb[2],
                          line_width, trace_type, log_scale,
                          request_type);
        
        if (Preferences.getUseDefaultArchives())
        {   // Use default archives
            final IArchiveDataSource archives[] = Preferences.getArchiveDataSources();
            for (IArchiveDataSource arch : archives)
                item.silentlyAddArchiveDataSource(arch);
        }
        else
        {
            // Get archives from configuration, if there are any
            Element arch = DOMHelper.findFirstElementNode(
                                        pv.getFirstChild(), TAG_ARCHIVE);
            while (arch != null)
            {
                final String arch_name = DOMHelper.getSubelementString(arch, TAG_NAME);
                final String url = DOMHelper.getSubelementString(arch, TAG_URL);
                final int key = DOMHelper.getSubelementInt(arch, TAG_KEY);
                item.silentlyAddArchiveDataSource(
                    CentralItemFactory.createArchiveDataSource(url, key, arch_name));            
                arch = DOMHelper.findNextElementNode(arch, TAG_ARCHIVE);
            }
        }
        return item;
    }
    
    /** @return RequestType obtained from DOM */
    private static RequestType loadRequestTypeFromDOM(Element pv)
    {
        final int ordinal = DOMHelper.getSubelementInt(pv, TAG_REQUEST,
                        RequestType.OPTIMIZED.ordinal());
        return RequestType.fromOrdinal(ordinal);
    }

    /** @param ring_size The ring_size to set.
     *  @throws Exception on out-of-memory error
     */
    public final void setLiveBufferSize(int ring_size) throws Exception
    {
        samples.setLiveCapacity(ring_size);
    }

    /** Start the item (subscribe, ...) */
    public final void start()
    {
        if (pv == null)
            return;
        try
        {
            pv.addListener(this);
            pv.start();
        }
        catch (Exception e)
        {
        	Plugin.getLogger().error(pv.getName(), e);
        }
    }
    
    /** Stop the item (subscribe, ...) */
    public final void stop()
    {
        if (pv == null)
            return;
        pv.removeListener(this);
        pv.stop();
    }

    /** @see org.csstudio.utility.pv.PVListener#pvDisconnected(org.csstudio.utility.pv.PV) */
    public void pvDisconnected(PV pv)
    {
        synchronized (this)
        {
            current_value = null;
        }
    }

    /** PVListener, memorize the most recent value. */
    public void pvValueUpdate(PV pv)
    {
        synchronized (this)
        {
            if (pv != null)
                current_value = pv.getValue();
        }
    }

    /** Add the most recent life value of the PV to the sample sequence. */
    public final void addCurrentValueToSamples(ITimestamp now)
    {
        // Should be called via Scanner/Scroller in UI thread...
        if (Display.getCurrent() == null)
            throw new Error("Called from non-UI thread " //$NON-NLS-1$
                            + Thread.currentThread().getName());
        IValue new_sample;
        synchronized (this)
        {   // pvValueUpdate might also change current_value, so lock.
            new_sample = current_value;
        }
        // When disconnected, add one(!) end sample.
        if (new_sample == null)
        {
            samples.markCurrentlyDisconnected(now);
            return;
        }
        // Add the most recent value after tweaking its
        // time stamp to be 'now'
        final IValue.Quality quality = IValue.Quality.Original;
        if (new_sample instanceof IDoubleValue)
            new_sample = ValueFactory.createDoubleValue(now,
                            new_sample.getSeverity(),
                            new_sample.getStatus(),
                            (INumericMetaData)new_sample.getMetaData(),
                            quality,
                            ((IDoubleValue)new_sample).getValues());
        else if (new_sample instanceof IEnumeratedValue)
            new_sample = ValueFactory.createEnumeratedValue(now,
                            new_sample.getSeverity(),
                            new_sample.getStatus(),
                            (IEnumeratedMetaData)new_sample.getMetaData(),
                            quality,
                            ((IEnumeratedValue)new_sample).getValues());
        else if (new_sample instanceof ILongValue)
            new_sample = ValueFactory.createLongValue(now,
                            new_sample.getSeverity(),
                            new_sample.getStatus(),
                            (INumericMetaData)new_sample.getMetaData(),
                            quality,
                            ((ILongValue)new_sample).getValues());
        else if (new_sample instanceof IStringValue)
            new_sample = ValueFactory.createStringValue(now,
                            new_sample.getSeverity(),
                            new_sample.getStatus(),
                            quality,
                            ((IStringValue)new_sample).getValues());
        else
            Plugin.getLogger().error("ModelItem cannot update timestamp of type " //$NON-NLS-1$
                            + new_sample.getClass().getName());
        samples.addLiveSample(new_sample);
        // See if there are (new) units
        IMetaData meta = new_sample.getMetaData();
        if (meta instanceof INumericMetaData)
        {
            final String new_units =  ((INumericMetaData)meta).getUnits();
            if (! units.equals(new_units))
            {
                units = new_units;
                model.fireEntryMetadataChanged(this);
            }
        }
    }
    
    /** @see IModelItem#getArchiveDataSources() */
    public final IArchiveDataSource[] getArchiveDataSources()
    {
        IArchiveDataSource result[] = new IArchiveDataSource[archives.size()];
        return archives.toArray(result);
    }
    
    /** Add another archive data source. */
    public void addArchiveDataSource(IArchiveDataSource archive)
    {
        if (silentlyAddArchiveDataSource(archive))
        {   // Notify model of this change.
            model.fireEntryArchivesChanged(this);
        }
    }

    /** Add another archive data source without notifying listeners. */
    private boolean silentlyAddArchiveDataSource(IArchiveDataSource archive)
    {
        // Ignore duplicates
        for (IArchiveDataSource arch : archives)
            if (arch.getKey() == archive.getKey() &&
                arch.getUrl().equals(archive.getUrl()))
                return false;
        archives.add(archive);
        return true;
    }

    /** Remove given archive data source. */
    public final void removeArchiveDataSource(IArchiveDataSource archive)
    {
        // Remove all matching entries (should be at most one...)
        for (int i = 0; i < archives.size(); ++i)
        {
            IArchiveDataSource entry = archives.get(i);
            if (entry.getKey() == archive.getKey() &&
                entry.getUrl().equals(archive.getUrl()))
                archives.remove(i); // changes size(), but that's OK
        }
        // Notify model of this change.
        model.fireEntryArchivesChanged(this);
    }
    
    /** Move given archive data source 'up' in the list. */
    public final void moveArchiveDataSourceUp(IArchiveDataSource archive)
    {
        // Move first matching entry, _skipping_ the top one!
        for (int i = 1/*!*/; i < archives.size(); ++i)
        {
            IArchiveDataSource entry = archives.get(i);
            if (entry.getKey() == archive.getKey() &&
                entry.getUrl().equals(archive.getUrl()))
            {
                entry = archives.remove(i);
                archives.add(i-1, entry);
                // Notify model of this change.
                model.fireEntryArchivesChanged(this);
                return;
            }
        }
    }
    
    /** Move given archive data source 'down' in the list. */
    public final void moveArchiveDataSourceDown(IArchiveDataSource archive)
    {
        // Move first matching entry, _skipping_ the last entry!
        for (int i = 0; i < archives.size()-1/*!*/; ++i)
        {
            IArchiveDataSource entry = archives.get(i);
            if (entry.getKey() == archive.getKey() &&
                entry.getUrl().equals(archive.getUrl()))
            {
                entry = archives.remove(i);
                archives.add(i+1, entry);
                // Notify model of this change.
                model.fireEntryArchivesChanged(this);
                return;
            }
        }
    }
    
    /** Replace archive data sources with default */
    public void useDefaultArchiveDataSources()
    {
        archives.clear();
        final IArchiveDataSource archives[] = Preferences.getArchiveDataSources();
        for (IArchiveDataSource arch : archives)
            silentlyAddArchiveDataSource(arch);
        model.fireEntryArchivesChanged(this);
    }

    /** Format as string */
    @SuppressWarnings("nls")
    @Override
    public String toString()
    {
        final StringBuffer b = new StringBuffer();
        b.append("PVModelItem: " + name);
        for (IArchiveDataSource archive : archives)
            b.append("\nArchive '" + archive.getName() + "'");
        return b.toString();
    }
}
