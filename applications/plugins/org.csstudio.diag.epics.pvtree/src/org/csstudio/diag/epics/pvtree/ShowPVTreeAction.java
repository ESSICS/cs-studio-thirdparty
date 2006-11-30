package org.csstudio.diag.epics.pvtree;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

/** Action connected to workbench menu action set for showing the view.
 *  @author Kay Kasemir
 */
public class ShowPVTreeAction implements IWorkbenchWindowActionDelegate
{
	public void init(IWorkbenchWindow window)
	{}

	public void selectionChanged(IAction action, ISelection selection)
	{}

	public void run(IAction action)
	{
	    try
	    {
	        IWorkbench workbench = PlatformUI.getWorkbench();
	        IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
	        IWorkbenchPage page = window.getActivePage();
	        page.showView(PVTreeView.ID);
	    }
	    catch (Exception e)
	    {
	        e.printStackTrace();
	    }
	}

	public void dispose()
	{}
}