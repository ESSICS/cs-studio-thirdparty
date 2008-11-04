/*
 * Copyright (c) 2007 Stiftung Deutsches Elektronen-Synchrotron,
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
/*
 * $Id$
 */
package org.csstudio.utility.adlconverter.utility.widgetparts;

import java.util.HashMap;
import java.util.Map;

import org.csstudio.platform.logging.CentralLogger;
import org.csstudio.sds.model.AbstractWidgetModel;
import org.csstudio.sds.model.properties.ActionData;
import org.csstudio.sds.model.properties.ActionType;
import org.csstudio.sds.model.properties.actions.CommitValueActionModel;
import org.csstudio.sds.model.properties.actions.CommitValueActionModelFactory;
import org.csstudio.sds.model.properties.actions.OpenDisplayActionModel;
import org.csstudio.sds.model.properties.actions.OpenDisplayActionModelFactory;
import org.csstudio.utility.adlconverter.internationalization.Messages;
import org.csstudio.utility.adlconverter.utility.ADLWidget;
import org.csstudio.utility.adlconverter.utility.FileLine;
import org.csstudio.utility.adlconverter.utility.WrongADLFormatException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * @author hrickens
 * @author $Author$
 * @version $Revision$
 * @since 21.09.2007
 */
public class ADLMenuItem extends WidgetPart{

    /**
     * The displayed text and description of the Action.
     */
    private String _label;
    /**
     * The type of Action.
     */
    private String _type;
    /**
     * The relative path to faceplate, script, ...
     */
    private String _command;
    /**
     * The arguments. Normal a Channel names.
     */
    private String _args;
    /**
     * The root path for Widget.
     */
    private String _path; 

    /**
     * The default constructor.
     * 
     * @param menuItem An ADLWidget that correspond a ADL Menu Item. 
     * @param parentWidgetModel The Widget that set the parameter from ADLWidget.
     * @throws WrongADLFormatException Wrong ADL format or untreated parameter found.
     */
    public ADLMenuItem(final ADLWidget menuItem, final AbstractWidgetModel parentWidgetModel) throws WrongADLFormatException {
        super(menuItem, parentWidgetModel);
    }
    
    /**
     * {@inheritDoc}
     */
    final void init(){
        _path = "/CSS/SDS"; //"C:/Helge_Projekte/CSS 3.3 Workspace/SDS/"; //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    final void parseWidgetPart(final ADLWidget menuItem) throws WrongADLFormatException {
        assert !menuItem.isType("menuItem") :  Messages.ADLMenuItem_AssertError_Begin+menuItem.getType()+Messages.ADLMenuItem_AssertError_End; //$NON-NLS-1$

        for (FileLine fileLine : menuItem.getBody()) {
            String parameter = fileLine.getLine();
            if(parameter.trim().startsWith("//")){ //$NON-NLS-1$
                continue;
            }
            String[] row = parameter.split("="); //$NON-NLS-1$
//            if(row.length!=2){
//                throw new Exception("This "+parameter+" is a wrong ADL Menu Item");
//            }
            if(row[0].trim().toLowerCase().equals("label")){ //$NON-NLS-1$
                _label=row[1].trim();
            }else if(row[0].trim().toLowerCase().equals("type")){ //$NON-NLS-1$
                _type=row[1].trim();
            }else if(row[0].trim().toLowerCase().equals("command")){ //$NON-NLS-1$
                _command=row[1].trim();
            }else if(row[0].trim().toLowerCase().equals("args")){ //$NON-NLS-1$
                _args=parameter.substring(parameter.indexOf("=")+1).replaceAll("\"","").trim(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }else {
                throw new WrongADLFormatException(Messages.ADLMenuItem_WrongADLFormatException_Begin+parameter+Messages.ADLMenuItem_WrongADLFormatException_end);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    final void generateElements(){
        if(_type.equals("\"New Display\"")){ //$NON-NLS-1$
            ActionData actionData = _widgetModel.getActionData();
            if(actionData==null){
                actionData = new ActionData();
            }
            
            // new Open Shell Action
            OpenDisplayActionModelFactory factory = new OpenDisplayActionModelFactory();
            OpenDisplayActionModel action = (OpenDisplayActionModel) factory.createWidgetAction(); 
            action.setEnabled(true);
            
            if(_label!=null){
                action.getProperty(OpenDisplayActionModel.PROP_DESCRIPTION)
                .setPropertyValue(_label.replaceAll("\"","")); //$NON-NLS-1$ //$NON-NLS-2$
            }

            // Set the Resource
            if(_path!=null){
                IPath path = new Path(_path);
                path = path.append(_command.replaceAll("\"", "").replace(".adl", ".css-sds")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                action.getProperty(OpenDisplayActionModel.PROP_RESOURCE)
                .setPropertyValue(path);//TODO: set the correct Path 
                actionData.addAction(action);
            }

            if(_args!=null){  
                Map<String, String> map = new HashMap<String, String>();            
                String[] params = _args.split(","); // TODO: es werde teilweise mehrere Argumente �ber geben.  Momenatan wird aber nur das erste ausgewertet. //$NON-NLS-1$
                // copierte art
                for(int i=0;i<params.length;i++){
                    String[] param = params[i].split("=");//$NON-NLS-1$
                    if(param.length==2){
                        map.put(param[0].trim(), param[1].trim());
                    }else{
                        if(params[i].trim().length()>0){
                            CentralLogger.getInstance().warn(this, Messages.RelatedDisplayItem_Parameter_Error+params[i]);
                        }
                    }
                }
                
                action.getProperty(OpenDisplayActionModel.PROP_ALIASES)
                .setPropertyValue(map);
            }
            actionData.addAction(action);
            _widgetModel.setPropertyValue(AbstractWidgetModel.PROP_ACTIONDATA, actionData);            
        }else if(_type.equals("\"System script\"")){ //$NON-NLS-1$
            ActionData actionData = _widgetModel.getActionData();
            if(actionData==null){
                actionData = new ActionData();
            }
            CommitValueActionModelFactory factory = new CommitValueActionModelFactory(); 
            CommitValueActionModel action = (CommitValueActionModel) factory.createWidgetAction();

            action.getProperty(CommitValueActionModel.PROP_VALUE)
            .setPropertyValue(new Path(_label.replaceAll("\"",""))); //$NON-NLS-1$ //$NON-NLS-2$
            actionData.addAction(action);

            action.getProperty(CommitValueActionModel.PROP_DESCRIPTION)
            .setPropertyValue(new Path(_label.replaceAll("\"",""))); //$NON-NLS-1$ //$NON-NLS-2$
            actionData.addAction(action);
            _widgetModel.setPropertyValue(AbstractWidgetModel.PROP_ACTIONDATA, actionData);
        }
        
        
    }
}
