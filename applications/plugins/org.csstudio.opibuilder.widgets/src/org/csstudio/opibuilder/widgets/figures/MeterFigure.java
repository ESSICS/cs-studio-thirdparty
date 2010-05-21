package org.csstudio.opibuilder.widgets.figures;

import org.csstudio.opibuilder.widgets.figureparts.RoundScale;
import org.csstudio.opibuilder.widgets.figureparts.RoundScaledRamp;
import org.csstudio.opibuilder.widgets.util.RotationUtil;
import org.csstudio.platform.ui.util.CustomMediaFactory;
import org.eclipse.draw2d.AbstractLayout;
import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.Polygon;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;


/**
 * The figure of Express Meter
 * @author Xihui Chen
 *
 */
public class MeterFigure extends AbstractRoundRampedFigure {

	private final static Font DEFAULT_LABEL_FONT = CustomMediaFactory.getInstance().getFont(
			new FontData("Arial", 12, SWT.BOLD));	
	private Ellipse needleCenter;
	
	private Needle needle;
	
	private Label valueLabel;
	
	private final static double SPACE_ANGLE = 45;
	
	public static final int NEEDLE_WIDTH = 16;
	
	public final static double ALPHA = SPACE_ANGLE * Math.PI/180;  
	
	public final static double HW_RATIO = (1- Math.sin(ALPHA)/2)/(2*Math.cos(ALPHA));	
	
	public MeterFigure() {
		super();
		transparent = true;
		scale.setScaleLineVisible(false);
		
		((RoundScale)scale).setStartAngle(180-SPACE_ANGLE);
		((RoundScale)scale).setEndAngle(SPACE_ANGLE);
		ramp.setRampWidth(12);
		setLoColor(CustomMediaFactory.COLOR_YELLOW);
		setHiColor(CustomMediaFactory.COLOR_YELLOW);
		
		valueLabel = new Label();		
		valueLabel.setFont(DEFAULT_LABEL_FONT);
		needle = new Needle();
		needle.setFill(true);
		needle.setOutline(false);
		
		needleCenter = new Ellipse();
		needleCenter.setOutline(false);
		
		setLayoutManager(new XMeterLayout());
		add(ramp, XMeterLayout.RAMP);
		add(scale, XMeterLayout.SCALE);			
		add(needle, XMeterLayout.NEEDLE);
		add(needleCenter, XMeterLayout.NEEDLE_CENTER);		
		add(valueLabel, XMeterLayout.VALUE_LABEL);
		
		addFigureListener(new FigureListener() {			
			public void figureMoved(IFigure source) {
				ramp.setDirty(true);
				revalidate();	
			}
		});	
		
	}
	@Override
	public void setBounds(Rectangle rect) {
		
		super.setBounds(rect);
	}
	
	
	@Override
	public void setValue(double value) {
		super.setValue(value);
		valueLabel.setText(scale.format(value));			
	}
	
	/**
	 * @param needleColor the needleColor to set
	 */
	public void setNeedleColor(RGB needleColor) {
		needle.setBackgroundColor(CustomMediaFactory.getInstance().getColor(needleColor));
	}

	class Needle extends Polygon {
		@Override
		protected void fillShape(Graphics g) {
			g.setAntialias(SWT.ON);
			super.fillShape(g);
		}
	}


	

	class XMeterLayout extends AbstractLayout {
		
		private static final int GAP_BTW_NEEDLE_SCALE = -5;
		
			
		
		int M = 0;
		
		/** Used as a constraint for the scale. */
		public static final String SCALE = "scale";   //$NON-NLS-1$
		/** Used as a constraint for the Needle. */
		public static final String NEEDLE = "needle"; //$NON-NLS-1$
		/** Used as a constraint for the Ramp */
		public static final String RAMP = "ramp";      //$NON-NLS-1$
		/** Used as a constraint for the needleCenter */
		public static final String NEEDLE_CENTER = "needleCenter";      //$NON-NLS-1$
		/** Used as a constraint for the value label*/
		public static final String VALUE_LABEL = "valueLabel";      //$NON-NLS-1$
		
		
		
		private RoundScale scale;
		private RoundScaledRamp ramp;
		private Polygon needle;
		private Ellipse needleCenter;
		private Label valueLabel;
		private PointList needlePoints = new PointList(new int[] {0,0,0,0,0,0});
		
		
		@Override
		public void setConstraint(IFigure child, Object constraint) {
			if(constraint.equals(SCALE))
				scale = (RoundScale)child;
			else if (constraint.equals(RAMP))
				ramp = (RoundScaledRamp) child;
			else if (constraint.equals(NEEDLE))
				needle = (Polygon) child;
			else if (constraint.equals(NEEDLE_CENTER))
				needleCenter = (Ellipse) child;
			else if (constraint.equals(VALUE_LABEL))
				valueLabel = (Label)child;
		}


		@Override
		protected Dimension calculatePreferredSize(IFigure container, int w,
				int h) {
			Insets insets = container.getInsets();
			Dimension d = new Dimension(256, 256);
			d.expand(insets.getWidth(), insets.getHeight());
			return d;
		}


		public void layout(IFigure container) {
			Rectangle area = container.getClientArea();	
			// calculate a virtual area
			
			if(scale != null && scale.isDirty())
				M = Math.max(FigureUtilities.getTextWidth(
						scale.format(scale.getRange().getLower()), scale.getFont()),
						FigureUtilities.getTextWidth(
						scale.format(scale.getRange().getLower()), scale.getFont()))/2;
			
			int h = area.height;
			int w = area.width;			
			if(h > HW_RATIO * (w - 2*M)) 
				h = (int) (HW_RATIO * (w - 2*M));
			else if (w > h/HW_RATIO + 2*M) 
				w = (int) (h/HW_RATIO + 2*M);			
			double r = h/(1- Math.sin(ALPHA)/2);		
			int x = (int) (area.x - r * (1.0 - Math.cos(ALPHA)) + M);
			int y = area.y;
			
			area = new Rectangle(x, y, (int)(2*r), (int)(2*r));			
			Point center = area.getCenter();			
			
			if(scale != null) {				
				scale.setBounds(area);
			}
			
			if(ramp != null && ramp.isVisible()) {
				Rectangle rampBounds = area.getCopy();
				ramp.setBounds(rampBounds.shrink(area.width/4 -ramp.getRampWidth(), 
						area.height/4 - ramp.getRampWidth()));
			}
			
			if(valueLabel != null) {
				Dimension labelSize = valueLabel.getPreferredSize();
				valueLabel.setBounds(new Rectangle(area.x + area.width/2 - labelSize.width/2,
						area.y + area.height/2 - area.height/4
						-(scale.getInnerRadius() - area.height/4)/2 - labelSize.height/2,
						labelSize.width, labelSize.height));
			}
			
			if(needle != null && scale != null) {
				needlePoints.setPoint (
						new Point(center.x + area.width/4, center.y - NEEDLE_WIDTH/2 + 3), 0);
				scale.getScaleTickMarks();
				needlePoints.setPoint(
						new Point(center.x + scale.getInnerRadius() - GAP_BTW_NEEDLE_SCALE, center.y), 1);
				needlePoints.setPoint(
						new Point(center.x + area.width/4, center.y + NEEDLE_WIDTH/2 - 3), 2);
	
				double valuePosition = 360 - scale.getValuePosition(getCoercedValue(), false);
				if(value > maximum)
					valuePosition += 8;
				else if(value < minimum)
					valuePosition -=8;
				needlePoints.setPoint(
						RotationUtil.rotate(needlePoints.getPoint(0),	valuePosition, center), 0);
				needlePoints.setPoint(
						RotationUtil.rotate(needlePoints.getPoint(1), valuePosition, center), 1);
				needlePoints.setPoint(
						RotationUtil.rotate(needlePoints.getPoint(2), valuePosition, center),2);				
				needle.setPoints(needlePoints);			
				
			}
			
			if(needleCenter == null){
				needleCenter.setBounds(new Rectangle(center.x - area.width/4,
						center.y - area.height/4,
						area.width/2, area.height/2));
			}		
						
		}		
	}
	
}
