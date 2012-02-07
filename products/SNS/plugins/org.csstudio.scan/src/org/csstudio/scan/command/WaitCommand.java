/*******************************************************************************
 * Copyright (c) 2011 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The scan engine idea is based on the "ScanEngine" developed
 * by the Software Services Group (SSG),  Advanced Photon Source,
 * Argonne National Laboratory,
 * Copyright (c) 2011 , UChicago Argonne, LLC.
 *
 * This implementation, however, contains no SSG "ScanEngine" source code
 * and is not endorsed by the SSG authors.
 ******************************************************************************/
package org.csstudio.scan.command;

import java.io.PrintStream;

import org.csstudio.scan.device.DeviceInfo;
import org.w3c.dom.Element;

/** Command that delays the scan until a device reaches a certain value
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class WaitCommand extends ScanCommand
{
    /** Configurable properties of this command */
    final private static ScanCommandProperty[] properties = new ScanCommandProperty[]
    {
        new ScanCommandProperty("device_name", "Device Name", DeviceInfo.class),
        new ScanCommandProperty("comparison", "Comparison", Comparison.class),
        new ScanCommandProperty("desired_value", "Desired Value", Double.class),
        new ScanCommandProperty("tolerance", "Tolerance (for '=')", Double.class),
        new ScanCommandProperty("timeout", "Time out (seconds; 0 to disable)", Double.class),
    };

    private String device_name;
    private Comparison comparison;
    private double desired_value;
    private double tolerance;
    private double timeout;

    /** Initialize empty wait command */
    public WaitCommand()
    {
        this("device", Comparison.EQUALS, 0.0, 0.1, 0.0);
    }

    /** Initialize
     *  @param device_name Name of device to check
     *  @param comparison Comparison to use
     *  @param desired_value Desired value of the device
     *  @param tolerance Numeric tolerance when checking value
     */
	public WaitCommand(final String device_name,
	        final Comparison comparison, final double desired_value,
	        final double tolerance, final double timeout)
    {
        this.device_name = device_name;
        this.desired_value = desired_value;
	    this.comparison = comparison;
	    this.tolerance = tolerance;
	    this.timeout = timeout;
    }

	/** {@inheritDoc} */
    @Override
    public ScanCommandProperty[] getProperties()
    {
        return properties;
    }

	/** @return Device name */
	public String getDeviceName()
    {
        return device_name;
    }

    /** @param device_name Name of device */
    public void setDeviceName(final String device_name)
    {
        this.device_name = device_name;
    }

	/** @return Desired value */
    public double getDesiredValue()
    {
        return desired_value;
    }

    /** @param desired_value Desired value */
    public void setDesiredValue(final Double desired_value)
    {
        this.desired_value = desired_value;
    }

    /** @return Desired comparison */
    public Comparison getComparison()
    {
        return comparison;
    }

    /** @param comparison Desired comparison */
    public void setComparison(final Comparison comparison)
    {
        this.comparison = comparison;
    }

    /** @return Tolerance */
    public double getTolerance()
    {
        return tolerance;
    }

    /** @param tolerance Tolerance */
    public void setTolerance(final Double tolerance)
    {
        this.tolerance = tolerance;
    }

    /** @return Timeout in seconds */
    public double getTimeout()
    {
        return timeout;
    }

    /** @param timeout Time out in seconds */
    public void setTimeout(final Double timeout)
    {
        this.timeout = timeout;
    }

    /** {@inheritDoc} */
    @Override
    public void writeXML(final PrintStream out, final int level)
    {
        writeIndent(out, level);
        out.println("<wait>");
        writeIndent(out, level+1);
        out.println("<device>" + device_name + "</device>");
        writeIndent(out, level+1);
        out.println("<value>" + desired_value + "</value>");
        writeIndent(out, level+1);
        out.println("<comparison>" + comparison.name() + "</comparison>");
        if (tolerance > 0.0)
        {
            writeIndent(out, level+1);
            out.println("<tolerance>" + tolerance + "</tolerance>");
        }
        if (timeout > 0.0)
        {
            writeIndent(out, level+1);
            out.println("<timeout>" + timeout + "</timeout>");
        }
        writeIndent(out, level);
        out.println("</wait>");
    }

    /** {@inheritDoc} */
    @Override
    public void readXML(final SimpleScanCommandFactory factory, final Element element) throws Exception
    {
        setDeviceName(DOMHelper.getSubelementString(element, "device"));
        setDesiredValue(DOMHelper.getSubelementDouble(element, "value"));
        try
        {
            setComparison(Comparison.valueOf(DOMHelper.getSubelementString(element, "comparison")));
        }
        catch (Throwable ex)
        {
            setComparison(Comparison.EQUALS);
        }
        setTolerance(DOMHelper.getSubelementDouble(element, "tolerance"));
        try
        {
            setTimeout(DOMHelper.getSubelementDouble(element, "timeout"));
        }
        catch (Throwable ex)
        {
            setTimeout(0.0);
        }
    }

    /** {@inheritDoc} */
	@Override
	public String toString()
	{
	    final StringBuilder buf = new StringBuilder();
	    buf.append("Wait for '").append(device_name).append("' ").append(comparison).append(" ").append(desired_value);
	    if (comparison == Comparison.EQUALS)
	        buf.append(" (+-").append(tolerance).append(")");
	    if (timeout > 0)
            buf.append(" (").append(timeout).append(" sec timeout)");
        return buf.toString();
	}
}
