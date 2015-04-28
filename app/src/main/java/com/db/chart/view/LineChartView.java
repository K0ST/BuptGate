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

import com.db.chart.model.ChartEntry;
import com.db.chart.model.ChartSet;
import com.db.chart.model.LineSet;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.util.AttributeSet;
import android.graphics.Shader;

import pro.kost.bupt.R;

/**
 * Implements a line chart extending {@link ChartView}
 */
public class LineChartView extends ChartView {

	
	/** Radius clickable region */
	private static float sRegionRadius;
	
	
	/** Style applied to line chart */
	private Style mStyle;

	
	
	
	public LineChartView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mStyle = new Style(context.getTheme()
				.obtainStyledAttributes(attrs, R.styleable.ChartAttrs, 0, 0));
		sRegionRadius = (float) getResources()
									.getDimension(R.dimen.dot_region_radius);
	}
	
	
	public LineChartView(Context context) {
		super(context);
		mStyle = new Style();
	}
	
	
	
	
	@Override
	public void onAttachedToWindow(){
		super.onAttachedToWindow();
		mStyle.init();
	}
	
	
	
	@Override
	public void onDetachedFromWindow(){
		super.onDetachedFromWindow();
		mStyle.clean();
	}
	
	
	
	
	/**
	 * Method responsible to draw a line with the parsed screen points.
	 * @param canvas
	 *   The canvas to draw on.
	 * @param screenPoints
	 *   The parsed screen points ready to be used/drawn.
	 */
	@Override
	public void onDrawChart(Canvas canvas, ArrayList<ChartSet> data) {
		
		LineSet lineSet;
		
		for(int i = 0; i < data.size(); i++){
			
			lineSet = (LineSet) data.get(i);
			mStyle.mLinePaint.setColor(lineSet.getLineColor());
			mStyle.mLinePaint.setStrokeWidth(lineSet.getLineThickness());
			handleAlpha(mStyle.mLinePaint, lineSet.getAlpha());
			
			
			if(lineSet.isDashed())
				mStyle.mLinePaint
					.setPathEffect(new DashPathEffect(new float[] {10,10}, 0));
			else
				mStyle.mLinePaint.setPathEffect(null);
			
			//Draw line
			if (!lineSet.isSmooth())
				drawLine(canvas, lineSet);
			else
				drawSmoothLine(canvas, lineSet);
			
			//Draw points
			if(lineSet.hasDots())
				drawPoints(canvas, lineSet);
		}

	}

	
	
	
	/**
	 * Responsible for drawing points
	 */
	private void drawPoints(Canvas canvas, LineSet set) {
		
		mStyle.mDotsPaint.setColor(set.getDotsColor());
		handleAlpha(mStyle.mDotsPaint, set.getAlpha());
		mStyle.mDotsStrokePaint.setStrokeWidth(set.getDotsStrokeThickness());
		mStyle.mDotsStrokePaint.setColor(set.getDotsStrokeColor());
		handleAlpha(mStyle.mDotsStrokePaint, set.getAlpha());
		
		
		
		final Path path = new Path();
		for(ChartEntry e : set.getEntries())
			path.addCircle(e.getX(), e.getY(), 
					set.getDotsRadius(), Path.Direction.CW);
		
		//Draw dots fill
		canvas.drawPath(path, mStyle.mDotsPaint);
		
		//Draw dots stroke
		if(set.hasDotsStroke())
			canvas.drawPath(path, mStyle.mDotsStrokePaint);
	}



	
	/**
	 * Responsible for drawing a (non smooth) line
	 */
	public void drawLine(Canvas canvas, LineSet set) {
		
		float minY = this.getInnerChartBottom();
		
		final Path path = new Path();
		final Path bgPath = new Path();
		for (int i = 0; i < set.size(); i++) {
			
			// Get minimum display Y to optimize gradient
			if(set.getEntry(i).getY() < minY)
				minY = set.getEntry(i).getY();
			
			if (i == 0){
				//Defining outline
				path.moveTo(set.getEntry(i).getX(), set.getEntry(i).getY());
				//Defining background
				bgPath.moveTo(set.getEntry(i).getX(), set.getEntry(i).getY());	
			}else{
				//Defining outline
				path.lineTo(set.getEntry(i).getX(), set.getEntry(i).getY());
				//Defining background
				bgPath.lineTo(set.getEntry(i).getX(), set.getEntry(i).getY());	
			}
		}
		
		//Draw background
		if(set.hasFill() || set.hasGradientFill())
			drawBackground(canvas, bgPath, set, minY);
		
		//Draw line
		canvas.drawPath(path, mStyle.mLinePaint);
	}

	


	/**
	 * Credits: http://www.jayway.com/author/andersericsson/
	 * Method responsible to draw a smooth line with the parsed screen points.
	 */
	private void drawSmoothLine(Canvas canvas, LineSet set) {
		
		float minY = this.getInnerChartBottom();
		
		float thisPointX;
		float thisPointY;
		float nextPointX;
		float nextPointY;
		float startdiffX;
		float startdiffY;
		float endDiffX;
		float endDiffY;
		float firstControlX;
		float firstControlY;
		float secondControlX;
		float secondControlY;
		
		final Path path = new Path();
		path.moveTo(set.getEntry(0).getX(),set.getEntry(0).getY());
		
		final Path bgPath= new Path();
		bgPath.moveTo(set.getEntry(0).getX(), set.getEntry(0).getY());
			
		for (int i = 0; i < set.size() - 1; i++) {
            
			// Get minimum display Y to optimize gradient
			if(set.getEntry(i).getY() < minY)
				minY = set.getEntry(i).getY();
			
			thisPointX = set.getEntry(i).getX();
            thisPointY = set.getEntry(i).getY();
            
            nextPointX = set.getEntry(i+1).getX();
            nextPointY = set.getEntry(i+1).getY();
	
            startdiffX = (nextPointX - set.getEntry(
            								si(set.size(), i - 1)).getX());
            startdiffY = (nextPointY - set.getEntry(
            								si(set.size(), i - 1)).getY());
	            
            endDiffX = (set.getEntry(si(set.size(), i + 2)).getX() - thisPointX);
            endDiffY = (set.getEntry(si(set.size(), i + 2)).getY() - thisPointY);
	
            firstControlX = thisPointX + (0.15f * startdiffX);
            firstControlY = thisPointY + (0.15f * startdiffY);
	            
            secondControlX = nextPointX - (0.15f * endDiffX);
            secondControlY = nextPointY - (0.15f * endDiffY);
            
            //Define outline
            path.cubicTo(firstControlX, firstControlY, 
            				secondControlX, secondControlY, 
            					nextPointX, nextPointY);
	        
            //Define background
            bgPath.cubicTo(firstControlX, firstControlY, 
            				secondControlX, secondControlY, 
            					nextPointX, nextPointY);
				
		}
		
		//Draw background
		if(set.hasFill() || set.hasGradientFill())
			drawBackground(canvas, bgPath, set, minY);
		
		//Draw outline
		canvas.drawPath(path, mStyle.mLinePaint);

	}
	
	
	
	
	/**
	 * Responsible for drawing line background
	 */
	private void drawBackground(Canvas canvas, Path path, LineSet set, float minDisplayY){
		
		mStyle.mFillPaint.setAlpha((int)(set.getAlpha() * 255));
		
		if(set.hasFill())
			mStyle.mFillPaint.setColor(set.getFillColor());
		if(set.hasGradientFill())
			mStyle.mFillPaint.setShader(
					new LinearGradient(
							super.getInnerChartLeft(), 
								minDisplayY, 
									super.getInnerChartLeft(), 
										super.getInnerChartBottom(), 
											set.getGradientColors(), 
												set.getGradientPositions(), 
													Shader.TileMode.MIRROR));
		
		path.lineTo(set.getEntry(set.size()-1).getX(), this.getInnerChartBottom());
		path.lineTo(set.getEntry(0).getX(), this.getInnerChartBottom());
		path.close();
		canvas.drawPath(path, mStyle.mFillPaint);
	}
	
	
	
	@Override
	public ArrayList<ArrayList<Region>> defineRegions(ArrayList<ChartSet> data){
		
		final ArrayList<ArrayList<Region>> result = new ArrayList<ArrayList<Region>>();
		ArrayList<Region> regionSet;
		
		for(ChartSet set : data){
			regionSet = new ArrayList<Region>(set.size());
			
			for(ChartEntry e : set.getEntries())
				regionSet.add(new Region((int)(e.getX() - sRegionRadius), 
										(int)(e.getY() - sRegionRadius), 
											(int)(e.getX() + sRegionRadius), 
												(int)(e.getY() + sRegionRadius)));
			result.add(regionSet);
		}
		
		return result;
	}
	
	
	
	
    private void handleAlpha(Paint paint, float alpha){
    	
		paint.setAlpha((int)(alpha * 255));
		paint.setShadowLayer(
				mStyle.mShadowRadius, 
					mStyle.mShadowDx, 
						mStyle.mShadowDy, 
							Color.argb(((int)(alpha * 255) < mStyle.mAlpha) 
							? (int)(alpha * 255) 
							: mStyle.mAlpha, 
								mStyle.mRed, 
									mStyle.mGreen, 
										mStyle.mBlue));	
    }
	
	
	
	/**
     * Credits: http://www.jayway.com/author/andersericsson/
     * Given an index in points, it will make sure the the returned index is
     * within the array.
     */
    private static int si(int setSize, int i) {
    	
        if (i > setSize - 1)
            return setSize - 1;
        else if (i < 0)
            return 0;
        return i;
    }
    
    
    
    
    
	/**
	 * Class responsible to style the LineChart!
	 * Can be instantiated with or without attributes.
	 */
	class Style {
		
		
		/** Paint variables */
		private Paint mDotsPaint;
		private Paint mDotsStrokePaint;
		private Paint mLinePaint;
		private Paint mFillPaint;
		
		
		/** Shadow variables */
		private final int mShadowColor;
		private final float mShadowRadius;
		private final float mShadowDx;
		private final float mShadowDy;
		
		/** Shadow color */
		private int mAlpha;
		private int mRed;
		private int mBlue;
		private int mGreen;

		
		protected Style() {
			
			mShadowRadius = 0;
	    	mShadowDx = 0;
	    	mShadowDy = 0;
			mShadowColor = 0;
		}
		
		
		protected Style(TypedArray attrs) {
			
			mShadowRadius = attrs.getDimension(
					R.styleable.ChartAttrs_chart_shadowRadius, 0);
	    	mShadowDx = attrs.getDimension(
	    			R.styleable.ChartAttrs_chart_shadowDx, 0);
	    	mShadowDy = attrs.getDimension(
	    			R.styleable.ChartAttrs_chart_shadowDy, 0);
			mShadowColor = attrs.getColor(
					R.styleable.ChartAttrs_chart_shadowColor, 0);
	    }
		
		
		
		private void init(){
	    	
			mAlpha = Color.alpha(mShadowColor);
			mRed = Color.red(mShadowColor);
			mBlue = Color.blue(mShadowColor);
			mGreen = Color.green(mShadowColor);
			
			mDotsPaint = new Paint();
			mDotsPaint.setStyle(Paint.Style.FILL_AND_STROKE);
			mDotsPaint.setAntiAlias(true);

			
			mDotsStrokePaint = new Paint();
			mDotsStrokePaint.setStyle(Paint.Style.STROKE);
			mDotsStrokePaint.setAntiAlias(true);
			
			mLinePaint = new Paint();
			mLinePaint.setStyle(Paint.Style.STROKE);
			mLinePaint.setAntiAlias(true);
			
			mFillPaint = new Paint();
			mFillPaint.setStyle(Paint.Style.FILL);
	    }

		
		
	    private void clean(){
	    	
	    	mLinePaint = null;
	    	mFillPaint = null;
	    	mDotsPaint = null;
	    }
	    
	    
	}
    
}
