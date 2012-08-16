/* 
GeoGebra - Dynamic Mathematics for Everyone
http://www.geogebra.org

This file is part of GeoGebra.

This program is free software; you can redistribute it and/or modify it 
under the terms of the GNU General Public License as published by 
the Free Software Foundation.

 */

package geogebra.common.kernel.algos;

import geogebra.common.kernel.Construction;
import geogebra.common.kernel.geos.GeoBoolean;
import geogebra.common.kernel.geos.GeoElement;
import geogebra.common.kernel.geos.GeoList;
import geogebra.common.kernel.geos.GeoNumeric;
import geogebra.common.kernel.geos.GeoPoint;
import geogebra.common.kernel.geos.GeoPolyLine;
import geogebra.common.kernel.kernelND.GeoPointND;
import geogebra.common.kernel.locusequ.EquationElement;
import geogebra.common.kernel.locusequ.EquationScope;

/**
 * Creates a frequency polygon.
 * 
 * Input: inputs are identical to AlgoHistogram
 * 
 * Output: PolyLine created from class borders and frequency heights of the
 * histogram generated by the inputs
 * 
 * @author G.Sturr
 */

public class AlgoFrequencyPolygon extends AlgoElement {

	private GeoList list1, list2; //input
	private GeoBoolean isCumulative, useDensity; //input
	private GeoNumeric density;
	private GeoPolyLine outputPolyLine; //output

	private GeoPointND[] points = null; 
	private AlgoHistogram algoHistogram;

	private boolean right = false;
	/**
	 * Creates a frequency polygon from two data lists.
	 * @param cons construction
	 * @param label label for the histogram
	 * @param list1 list of boundaries
	 * @param list2 list of heights or raw data
	 */
	public AlgoFrequencyPolygon(Construction cons, String label,
			GeoList list1, GeoList list2) {
		this(cons, label, null, list1, list2, null, null);		
	}


	/**
	 * /**
	 * Creates frequency polygon from two data lists with parameter specifications.
	 * 
	 * @param cons
	 * @param label
	 * @param isCumulative
	 * @param list1 
	 * @param list2
	 * @param useDensity
	 * @param density
	 */
	public AlgoFrequencyPolygon(Construction cons, String label,
			GeoBoolean isCumulative,					   
			GeoList list1, 
			GeoList list2, 
			GeoBoolean useDensity, 
			GeoNumeric density) {

		this(cons, isCumulative, list1, list2, useDensity, density);
		outputPolyLine.setLabel(label);
	}

	public AlgoFrequencyPolygon(Construction cons,
			GeoBoolean isCumulative,					   
			GeoList list1, 
			GeoList list2, 
			GeoBoolean useDensity, 
			GeoNumeric density) {
		super(cons);
		this.list1 = list1;
		this.list2 = list2;
		this.isCumulative = isCumulative;
		this.useDensity = useDensity;
		this.density = density;

		outputPolyLine = new GeoPolyLine(cons, points);

		setInputOutput();
		compute();
	}

	@Override
	public Algos getClassName() {
		return Algos.AlgoFrequencyPolygon;
	}

	@Override
	protected void setInputOutput() {
		
		// handle simple case: input is only two lists when default density is used 
		if(useDensity == null) {
			input = new GeoElement[2];
			input[0] = list1;
			input[1] = list2;
			
		// handle other cases	
		} else {
			
			// standard counts, non-cumulative
			if(isCumulative == null) {
				if(density == null){
					input = new GeoElement[3];
					input[0] = list1;		
					input[1] = list2;
					input[2] = useDensity;
				} else {
					input = new GeoElement[4];
					input[0] = list1;		
					input[1] = list2;
					input[2] = useDensity;
					input[3] = density;
				}
				
			// cumulative counts	
			} else {
				if(density == null) {
					input = new GeoElement[4];
					input[0] = isCumulative;
					input[1] = list1;		
					input[2] = list2;
					input[3] = useDensity;
				} else {
					input = new GeoElement[5];
					input[0] = isCumulative;
					input[1] = list1;		
					input[2] = list2;
					input[3] = useDensity;
					input[4] = density;
				}
			}
		}
		
		
		boolean suppressLabelCreation = cons.isSuppressLabelsActive();
		cons.setSuppressLabelCreation(true);

		if(useDensity == null) {
			algoHistogram = new AlgoHistogram(cons, list1, list2,right);
		} else {
			algoHistogram = new AlgoHistogram(cons, isCumulative, list1,
					list2, useDensity, density,right);
		}
		cons.setSuppressLabelCreation(suppressLabelCreation);

		
		setOutput();

		// parent of output
		outputPolyLine.setParentAlgorithm(this);       
		cons.addToAlgorithmList(this); 

		setDependencies(); // done by AlgoElement
	}

	
	private void setOutput() {
		super.setOutputLength(1);           
		super.setOutput(0, outputPolyLine);        
	}


	public GeoPolyLine getResult() {
		return outputPolyLine;
	}
	
	@Override
	public final void compute() {

		// update our histogram to get class borders and y values
		algoHistogram.update();
		
		if(!algoHistogram.getOutput()[0].isDefined()) {
			outputPolyLine.setUndefined();
			return;
		}		
		double[] leftBorder = algoHistogram.getLeftBorder();
		if(leftBorder == null || leftBorder.length < 2) {
			outputPolyLine.setUndefined();
			return;
		}		
		double[] yValue  = algoHistogram.getYValue();
		if(yValue == null || yValue.length < 2) {
			outputPolyLine.setUndefined();
			return;
		}
		
		// if we got this far everything is ok; now define the polyLine 
		outputPolyLine.setDefined();

		// remember old number of points
		int oldPointsLength = points == null ? 0 : points.length;	
		
		// create a new point array
		boolean doCumulative = (isCumulative != null && isCumulative.getBoolean());
		int size = doCumulative ? yValue.length : yValue.length-1;
		points = new GeoPoint[size];
	
		// create points and load the point array  
		boolean suppressLabelCreation = cons.isSuppressLabelsActive();
		cons.setSuppressLabelCreation(true);
		if(doCumulative)
			points[0] = new GeoPoint(cons, null, leftBorder[0], 0.0, 1.0);
		for (int i = 0; i < yValue.length-1; i++) {
			if(doCumulative) {
				points[i+1] = new GeoPoint(cons, null, leftBorder[i+1], yValue[i], 1.0);
			} else {
				points[i] = new GeoPoint(cons, null, (leftBorder[i+1] + leftBorder[i])/2, yValue[i], 1.0);
			}
		}	
		cons.setSuppressLabelCreation(suppressLabelCreation);
		
		// update the polyLine
		outputPolyLine.setPoints(points);
		if (oldPointsLength != points.length)
			setOutput();    	

	}  

	// TODO Consider locusequability

}
