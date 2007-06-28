/* 
 * Copyright (c) 2006 Stiftung Deutsches Elektronen-Synchroton, 
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
package org.csstudio.platform.ui.dnd;

import java.util.ArrayList;
import java.util.List;

import org.csstudio.platform.model.rfc.IPVAdressListProvider;
import org.csstudio.platform.model.rfc.IPVAdressProvider;
import org.csstudio.platform.model.rfc.ProcessVariable;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.TextTransfer;

/**
 * This adapter class provides enhanced implementations for the methods
 * described by the <code>DragSourceListener</code> interface.
 * @author Kai Meyer
 */
public class PVDragSourceAdapter extends DragSourceAdapter {
	
	/**
	 * A {@link IPVAdressProvider}
	 */
	private IPVAdressProvider _pvProvider;
	/**
	 * A {@link IPVAdressListProvider}
	 */
	private IPVAdressListProvider _pvListProvider;
	
	/**
	 * Constructs a drag source adapter, which only provides items during DnD,
	 * that are {@link ProcessVariable}s.
	 * 
	 * @param pvProvider
	 * 			  The provider of the {@link ProcessVariable}
	 */
	public PVDragSourceAdapter(IPVAdressProvider pvProvider) {
		_pvProvider = pvProvider;
		_pvListProvider = null;
	}
	
	/**
	 * Constructs a drag source adapter, which only provides items during DnD,
	 * that are {@link ProcessVariable}s.
	 * 
	 * @param pvProvider
	 * 			  The provider of the {@link ProcessVariable}
	 */
	public PVDragSourceAdapter(IPVAdressListProvider pvProvider) {
		_pvProvider = null;
		_pvListProvider = pvProvider;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dragStart(final DragSourceEvent event) {
		super.dragStart(event);
		PVTransfer.getInstance().setSelectedItems(this.getProceesVariables());
	}
	
	/**
	 * Returns a new List of all provided {@link ProcessVariable}
	 * @return List of ProcessVariables
	 * 		A new List of all provided {@link ProcessVariable}
	 */
	private List<ProcessVariable> getProceesVariables() {
		List<ProcessVariable> list = new ArrayList<ProcessVariable>();
		if (_pvProvider!=null) {
			list.add(_pvProvider.getPVAdress());
		} else if (_pvListProvider!=null) {
			for (ProcessVariable pv : _pvListProvider.getPVAdressList()) {
				list.add(pv);
			}
		}
		return list;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dragSetData(final DragSourceEvent event) {
		List<ProcessVariable> items = null;
		List currentSelection = this.getProceesVariables();
		
		if ((currentSelection != null) && (currentSelection.size() > 0)) {
			items = this.getProceesVariables();
		} else {
			items = PVTransfer.getInstance().getSelectedItems();
		}
		if (PVTransfer.getInstance().isSupportedType(
				event.dataType)) {
			event.data = items;
		} else if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
			StringBuffer sb = new StringBuffer();
			// concatenate a String, which contains items line by line
			for (ProcessVariable item : items) {
				String path = item.toFullString();
				sb.append(path);
				sb.append("\n"); //$NON-NLS-1$
			}
			event.data = sb.toString();
		}
	}

}
