package org.csstudio.opibuilder.widgets.symbol.multistate;

import java.util.ArrayList;
import java.util.List;

import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.widgets.symbol.image.AbstractSymbolImage;
import org.csstudio.opibuilder.widgets.symbol.image.ControlSymbolImage;
import org.csstudio.opibuilder.widgets.symbol.util.SymbolBrowser;
import org.csstudio.swt.widgets.datadefinition.IManualStringValueChangeListener;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;

public class ControlMultiSymbolFigure extends CommonMultiSymbolFigure {
	
	private Composite composite;
	private SymbolBrowser symbolBrowser;
	
	protected boolean showConfirmDialog = false;
	protected boolean displayWidget = false;
	protected boolean figureClicked = false;
	
	protected String password = "";
	protected String confirmTip = "Are you sure you want to do this?";
	
	protected ButtonPresser buttonPresser;
	
	@Override
	protected AbstractSymbolImage createSymbolImage(boolean runMode) {
		ControlSymbolImage csi = new ControlSymbolImage(runMode);
		if (symbolProperties != null) {
			symbolProperties.fillSymbolImage(csi);
		}
		return csi;
	}

	public ControlMultiSymbolFigure(final AbstractBaseEditPart editPart) {
		super(editPart.getExecutionMode() == ExecutionMode.RUN_MODE);
		boolean runMode = editPart.getExecutionMode() == ExecutionMode.RUN_MODE;

		this.composite = (Composite) editPart.getViewer().getControl();
		if (runMode) {
			buttonPresser = new ButtonPresser();
			addMouseListener(buttonPresser);

			// Initialize state chooser widget
			symbolBrowser = new SymbolBrowser(composite, SWT.BORDER);
			symbolBrowser.addSelectionListener(new Listener() {
				public void handleEvent(Event e) {
					String value = symbolBrowser.getSelection();
					if (!showConfirmDialog) {
						fireManualValueChange(value);
					} else if (openConfirmDialog()) {
						symbolBrowser.setVisible(false);
						symbolBrowser.setEnabled(false);
						displayWidget = false;
						fireManualValueChange(value);
					}
				}
			});
			Listener listener = new Listener() {
				@Override
				public void handleEvent(Event event) {
					if (getBounds().contains(event.x, event.y)) {
						return;
					}
					if (figureClicked) {
						figureClicked = false;
						return;
					}
					if (displayWidget) {
						symbolBrowser.setVisible(false);
						symbolBrowser.setEnabled(false);
						displayWidget = false;
					}
				}
			};
			symbolBrowser.getParent().addListener(SWT.MouseDown , listener);
			symbolBrowser.setVisible(false);
			symbolBrowser.setEnabled(false);
		}
	}
	
	@Override
	public void setEnabled(boolean value) {
		super.setEnabled(value);
		if (!isEditMode() && value) 			
			setCursor(Cursors.HAND);	
	}
	
	@Override
	public synchronized void setState(int stateIndex) {
		super.setState(stateIndex);
		symbolBrowser.setCurrentState(currentState);
		symbolBrowser.initCurrentDisplay();
	}
	
	// ************************************************************
	// Control listeners
	// ************************************************************
	
	/**
	 * Listeners that react on manual value change events.
	 */
	private List<IManualStringValueChangeListener> controlListeners = new ArrayList<IManualStringValueChangeListener>();

	/**
	 * Add a control listener which will be executed when pressed or released
	 * 
	 * @param listener
	 *            the listener to add
	 */
	public void addManualValueChangeListener(
			final IManualStringValueChangeListener listener) {
		controlListeners.add(listener);
	}

	public void removeManualValueChangeListener(
			final IManualStringValueChangeListener listener) {
		if (controlListeners.contains(listener))
			controlListeners.remove(listener);
	}

	/**
	 * Inform all control listeners that the manual value has changed.
	 */
	protected void fireManualValueChange(final String newManualState) {
		setState(newManualState);
		if (!isEditMode()) {
			for (IManualStringValueChangeListener l : controlListeners) {
				l.manualValueChanged(newManualState);
			}
		}
	}
	
	// ************************************************************
	// Button presser actions
	// ************************************************************
	
	class ButtonPresser extends MouseListener.Stub {
		public void mousePressed(MouseEvent me) {
			if (me.button != 1)
				return;
			figureClicked = true;
			if (!isEditMode()) {
				if (!displayWidget) {
					initSymbolBrowser();
					if(!symbolBrowser.isEmpty()) {
						symbolBrowser.setVisible(true);
						symbolBrowser.setEnabled(true);
						displayWidget = true;
					}
				} else {
					symbolBrowser.setVisible(false);
					symbolBrowser.setEnabled(false);
					displayWidget = false;
				}
				me.consume();
				repaint();
			}
		}
	}
	
	// ************************************************************
	// SWT figure initialization
	// ************************************************************
	
	private void initSymbolBrowser() {
		// initialize symbol browser with images
		symbolBrowser.clear();
		for (String state : states) {
			if (images.get(state) != null)
				symbolBrowser.addImage(state, images.get(state)
						.getOriginalImageData());
		}
		symbolBrowser.setCurrentState(currentState);
		symbolBrowser.initCurrentDisplay();
		
		Rectangle bounds = getBounds().getCopy();
		translateToAbsolute(bounds);
		
		int xPos = bounds.x + bounds.width;
		int yPos = bounds.y;
		int width = symbolBrowser.getSize().x;
		int height = symbolBrowser.getSize().y;
		symbolBrowser.setBounds(xPos, yPos, width, height);
		symbolBrowser.moveAbove(null);
	}
	
	// ************************************************************
	// Confirm dialog
	// ************************************************************
	
	/**
	 * @return the condition when confirm dialog should be shown.
	 */
	public boolean getShowConfirmDialog() {
		return showConfirmDialog;
	}

	public String getConfirmTip() {
		return confirmTip;
	}

	public String getPassword() {
		return password;
	}

	public void setShowConfirmDialog(boolean showConfirm) {
		this.showConfirmDialog = showConfirm;
	}

	public void setConfirmTip(String confirmTip) {
		this.confirmTip = confirmTip;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	/**
	 * open a confirm dialog.
	 * 
	 * @return false if user canceled, true if user pressed OK or no confirm
	 *         dialog needed.
	 */
	private boolean openConfirmDialog() {
		// confirm & password input dialog
		if (password == null || password.equals("")) {
			MessageBox mb = new MessageBox(Display.getCurrent()
					.getActiveShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO
					| SWT.CANCEL);
			mb.setMessage(confirmTip);
			mb.setText("Confirm Dialog");
			int val = mb.open();
			if (val == SWT.YES)
				return true;
		} else {
			InputDialog dlg = new InputDialog(Display.getCurrent()
					.getActiveShell(), "Password Input Dialog",
					"Please input the password", "", new IInputValidator() {
						public String isValid(String newText) {
							if (newText.equals(password))
								return null;
							else
								return "Password error!";
						}
					}) {
				@Override
				protected int getInputTextStyle() {
					return SWT.SINGLE | SWT.PASSWORD;
				}
			};
			dlg.setBlockOnOpen(true);
			int val = dlg.open();
			if (val == Window.OK)
				return true;
		}
		return false;
	}
	
}
