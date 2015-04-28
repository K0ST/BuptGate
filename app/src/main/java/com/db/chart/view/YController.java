/*
 * Copyright 2014 Diogo Bernardino
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.db.chart.view;

import java.util.ArrayList;

import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint.Align;

import com.db.chart.model.ChartSet;

import pro.kost.bupt.R;

/**
 * Class responsible to control vertical measures, positions, yadda yadda. 
 * If the drawing is requested it will also take care of it.
 */
public class YController{
	
	
	public static enum LabelPosition {
		NONE, OUTSIDE, INSIDE
    }

	
	/** Default step between labels */
	private static final int DEFAULT_STEP = 1;
	
	
	/** Distance between axis Y and label Y */
	private int mDistLabelToAxis;
	
	
	/** ChartView object */
	private ChartView mChartView;

	
	/** Vertical labels */
	private ArrayList<Integer> mLabels;
	
	
	/** Screen step between labels */
	private float mScreenStep;
	

	/** Starting X point of the axis */
	private float mAxisHorPosition;

	
	/** Spacing for top label */
	protected float topSpacing;
	

	/** Range of Y labels from minLabelValue to maxLabelValue */
	protected int maxLabelValue;
	protected int minLabelValue;
	
	
	/** Max value that needs to be displayed */ 
	private float maxValue;
	
	
	/** Min value that nees to be displayed */
	private float minValue;
	
	/** Labels position */
	protected ArrayList<Float> labelsPos;

	
	/** Step between labels */
	protected int step;

	
	/** Whether the chart has Y Axis or not */
	protected boolean hasAxis;
	
	
	/** none/inside/outside */
	protected LabelPosition labelsPositioning;
	
	
	/** Labels Metric to draw together with labels */
	protected String labelMetric;

	
	private int mLabelHeight;
	
	public YController(ChartView chartView) {
		
		mChartView = chartView;
		
		//Set defaults
		step = DEFAULT_STEP;
		topSpacing = mChartView.getResources()
										.getDimension(R.dimen.axis_top_spacing);
		mAxisHorPosition = 0;
		minLabelValue = 0;
		maxLabelValue = 0;
		labelsPositioning = LabelPosition.OUTSIDE;
		hasAxis = true;
		labelMetric = "";
		mLabelHeight = -1;
	}
	
	
	public YController(ChartView chartView, TypedArray attrs) {
		this(chartView);
		
		topSpacing = attrs.getDimension(
								R.styleable.ChartAttrs_chart_axisTopSpacing, 
									topSpacing);
	}


	

	protected void init() {
		
		mDistLabelToAxis= (int) mChartView.getResources()
									.getDimension(R.dimen.axis_dist_from_label);
		if(labelsPositioning == LabelPosition.INSIDE)
			mDistLabelToAxis *= -1;
			
		mLabels = calcLabels();
		mAxisHorPosition = calcAxisHorizontalPosition();
		labelsPos = calcLabelsPos(mChartView.data.get(0).size());
	
	}

	
	
	
	/**
	 * Calculates the max Y value.
	 * @return max Y value.
	 */
	private float[]  calcBorderValues() {
		
		float max = Integer.MIN_VALUE;
		float min = Integer.MAX_VALUE;
		ChartSet set;
		
		for(int i = 0; i < mChartView.data.size(); i++){
			
			set = mChartView.data.get(i);
			for(int j = 0; j < set.size(); j++){
				
				if(set.getValue(j) >= max)
					max = set.getValue(j);
				if(set.getValue(j) <= min)
					min = set.getValue(j);
			}
		}

		final float[] result = {min, max};
		return result;
	}
	
	
	
	
	/**
	 * Get labels based on the maximum value displayed
	 * @return result
	 */
	private ArrayList<Integer> calcLabels(){
		
		final float[] borderValues = calcBorderValues();
		minValue = borderValues[0];
		maxValue = borderValues[1];
		
		//If not specified then calculate border labels
		if(minLabelValue == 0 && maxLabelValue == 0){
			
			if(maxValue < 0)
				maxLabelValue = 0;
			else
				maxLabelValue = (int) Math.ceil(maxValue);
			
			if(minValue > 0)
				minLabelValue = 0;
			else
				minLabelValue = (int) Math.floor(minValue);
			
			while((maxLabelValue - minLabelValue) % step != 0)
				maxLabelValue += 1;
		}
		
		final ArrayList<Integer> result = new ArrayList<Integer>();
		int pos = minLabelValue;
		while(pos <= maxLabelValue){
			result.add(pos);
			pos += step;
		}

		//Set max Y axis label in case isn't already there
		if(result.get(result.size()-1) < maxLabelValue)
			result.add(maxLabelValue);
		
		return result;
	}
	
	
	
	
	/**
	 * Get labels position having into account the vertical padding of text size.
	 * @param nLabels
	 */
	private ArrayList<Float> calcLabelsPos(int nLabels) {
		
		final ArrayList<Float> result = new ArrayList<Float>();
		
		final int frameHeight = (int) mChartView.horController.getAxisVerticalPosition() - mChartView.chartTop;
		mScreenStep = (float) (frameHeight - topSpacing) / (mLabels.size() - 1);
		
		float currPos = (float) (mChartView.horController.getAxisVerticalPosition());
		for(int i = 0; i < mLabels.size(); i++){
			result.add(currPos);
			currPos -= mScreenStep;
		}
		
		return result;
	}
	
	
	
	
	/**
	 * Calculates the starting X point of the axis
	 */
	protected float calcAxisHorizontalPosition(){
		
		// In case labels Y needs to be drawn
		if(labelsPositioning == LabelPosition.OUTSIDE){
			
			float maxLenghtLabel = 0;
			float aux = 0;
			for(int i = 0; i < mLabels.size(); i++){

				aux = mChartView.style.labelPaint.measureText(Integer.toString(mLabels.get(i)) + labelMetric);
				if(aux > maxLenghtLabel)
					maxLenghtLabel = aux;
			}
			
			return mChartView.chartLeft + maxLenghtLabel + mDistLabelToAxis;
			
		}else{
			return mChartView.chartLeft;
		}
	}

	
	
	
	/**
	 * Based in a (real) value returns the associated screen point
	 * @param value
	 * @return point
	 */
	protected float parseYPos(double value){
        Integer lable;
        if (mLabels.size() == 1)
            lable = mLabels.get(0);
        else
            lable = mLabels.get(1);
		return (float) ( mChartView.horController.getAxisVerticalPosition() - 
							(((value+Math.abs(minLabelValue)) * mScreenStep) / (lable + Math.abs(minLabelValue))));
	}
	
	
	
	
	
	/**
	 * Method called from onDraw method to draw YController data
	 * @param canvas - Canvas to use while drawing the data.
	 */
	protected void draw(Canvas canvas){
		
		if(hasAxis)
			// Draw axis line
			canvas.drawLine(mAxisHorPosition, 
								mChartView.chartTop, 
									mAxisHorPosition, 
										mChartView.horController.getAxisVerticalPosition() + mChartView.style.axisThickness/2, 
											mChartView.style.chartPaint);
		
		if(labelsPositioning != LabelPosition.NONE){
			
			mChartView.style.labelPaint.setTextAlign(
					(labelsPositioning == LabelPosition.OUTSIDE) 
						? Align.RIGHT : Align.LEFT);
			
			// Draw labels
			for(int i = 0; i < mLabels.size(); i++){
				canvas.drawText(Integer.toString(mLabels.get(i)) + labelMetric, 
									mAxisHorPosition - mChartView.style.axisThickness/2 - mDistLabelToAxis, 
										(float) labelsPos.get(i) + mChartView.style.getTextHeightBounds("0")/2, 
											mChartView.style.labelPaint);
			}
		}
	}

	
	

	
	/**
	 * Differentiates the inner left side of the chart depending 
	 * if axis Y is drawn or not.
	 * If drawing axis give it gives half of the line thickness as margin.  
	 * Inner Chart refers only to the area where chart data will be draw, 
	 * excluding labels, axis, etc.
	 * @return position of the inner left side of the chart
	 */
	public float getInnerChartLeft(){
		
		if(hasAxis)
			return mAxisHorPosition + mChartView.style.axisThickness/2;
		else
			return mAxisHorPosition;
	}
	
	

	
	/**
	 * Inner Chart refers only to the area where chart data will be draw, 
	 * excluding labels, axis, etc.
	 * @return position of the inner left side of the chart
	 */
	public float getInnerChartBottom(){
		return mChartView.horController.getAxisVerticalPosition() - mChartView.style.axisThickness/2;
	}

	
	
	
	protected int getLabelHeight(){
		if(mLabelHeight == -1){
			int result = 0;
			for(int i = 0; i < mChartView.data.get(0).size(); i++){
				result = mChartView.style.getTextHeightBounds(mChartView.data.get(0).getLabel(i));
				if(result != 0)
					break;
			}
			mLabelHeight = result;
		}
			
		return mLabelHeight;
	}
	
}

