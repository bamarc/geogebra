package geogebra.common.kernel.commands; 
/* 
GeoGebra - Dynamic Mathematics for Schools
Copyright Markus Hohenwarter and GeoGebra Inc.,  http://www.geogebra.org

This file is part of GeoGebra.

This program is free software; you can redistribute it and/or modify it 
under the terms of the GNU General Public License as published by 
the Free Software Foundation.

*/
import geogebra.common.kernel.arithmetic.Command;
import geogebra.common.kernel.geos.GeoElement;
import geogebra.common.kernel.geos.GeoFunctionable;
import geogebra.common.kernel.geos.GeoList;
import geogebra.common.main.MyError;
import geogebra.common.kernel.Kernel;

/** 
 * ResidualPlot[<List of Points>,<Funtion>]
 * 
 * @author G.Sturr
 * @version 2010-9-13
 */
public class CmdResidualPlot extends CommandProcessor{

    public CmdResidualPlot(Kernel kernel) {super(kernel);}
    
    @Override
	public GeoElement[] process(Command c) throws MyError {
        int n=c.getArgumentNumber();
        GeoElement[] arg=resArgs(c);;
        switch(n) {
            case 2:
            	if(  (arg[0].isGeoList() )&& (arg[1].isGeoFunctionable())  ){ 
            		GeoElement[] ret={
            				kernelA.ResidualPlot(c.getLabel(),(GeoList)arg[0],(GeoFunctionable) arg[1]) 
            				};
                    return ret;
            	}else{
            		throw argErr(app,c.getName(),arg[0]);
                }//if args ok

            default :
    			throw argNumErr(app, c.getName(), n);
        }//switch(number of arguments)
    }//process(Command) 
}// class CmdRSquare