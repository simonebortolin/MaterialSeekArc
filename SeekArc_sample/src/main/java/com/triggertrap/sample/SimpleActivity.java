/*******************************************************************************
 * The MIT License (MIT)
 * 
 * Copyright (c) 2013 Triggertrap Ltd
 * Author Neil Davies
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.triggertrap.sample;

import android.app.Activity;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.triggertrap.seekarc.SeekArc;

/**
 * 
 * SimpleActivity.java
 * @author Neil Davies
 * 
 */
public class SimpleActivity extends Activity {

	private SeekArc mSeekArc;
	private SeekBar mRotation;
	private SeekBar mStartAngle;
	private SeekBar mSweepAngle;
	private SeekBar mArcWidth;
	private SeekBar mProgressWidth;
	private CheckBox mRoundedEdges;
	private CheckBox mTouchInside;
	private CheckBox mClockwise;
	private TextView mSeekArcProgress;
	private CheckBox mEnabled;

	protected int getLayoutFile(){
		return R.layout.holo_sample;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayoutFile());
		
		mSeekArc = findViewById(R.id.seekArc);
		mSeekArcProgress = findViewById(R.id.seekArcProgress);
		mRotation = findViewById(R.id.rotation);
		mStartAngle = findViewById(R.id.startAngle);
		mSweepAngle  = findViewById(R.id.sweepAngle);
		mArcWidth = findViewById(R.id.arcWidth);
		mProgressWidth = findViewById(R.id.progressWidth);
		mRoundedEdges = findViewById(R.id.roundedEdges);
		mTouchInside = findViewById(R.id.touchInside);
		mClockwise = findViewById(R.id.clockwise);
		mEnabled = findViewById(R.id.enabled);

		mRotation.setProgress(mSeekArc.getArcRotation());
		mStartAngle.setProgress(mSeekArc.getStartAngle());
		mSweepAngle.setProgress(mSeekArc.getSweepAngle());
		mArcWidth.setProgress(mSeekArc.getInactiveWidth());

		mSeekArc.setOnSeekArcChangeListener(new SeekArc.OnSeekProgressChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekArc seekArc) {
			}

			@Override
			public void onStartTrackingTouch(SeekArc seekArc) {
			}

			@Override
			public void onProgressChanged(SeekArc seekArc, int progress,
										  boolean fromUser) {
				mSeekArcProgress.setText(String.valueOf(progress));
			}
		});
			
		mRotation.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar arg0) {

			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
			}

			@Override
			public void onProgressChanged(SeekBar view, int progress, boolean fromUser) {
				mSeekArc.setArcRotation(progress);
				mSeekArc.invalidate();
			}
		});
		
		mStartAngle.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar arg0) {

			}		
			@Override
			public void onStartTrackingTouch(SeekBar arg0) {		
			}
			
			@Override
			public void onProgressChanged(SeekBar view, int progress, boolean fromUser) {
				mSeekArc.setStartAngle(progress);
				mSeekArc.invalidate();
			}
		});
		
		mSweepAngle.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar arg0) {

			}		
			@Override
			public void onStartTrackingTouch(SeekBar arg0) {		
			}
			
			@Override
			public void onProgressChanged(SeekBar view, int progress, boolean fromUser) {
				mSeekArc.setSweepAngle(progress);
				mSeekArc.invalidate();
			}
		});
			
		mArcWidth.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar arg0) {

			}		
			@Override
			public void onStartTrackingTouch(SeekBar arg0) {		
			}
			
			@Override
			public void onProgressChanged(SeekBar view, int progress, boolean fromUser) {
				mSeekArc.setInactiveWidth(progress);
				mSeekArc.invalidate();
			}
		});
		
		mProgressWidth.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar arg0) {

			}		
			@Override
			public void onStartTrackingTouch(SeekBar arg0) {		
			}
			
			@Override
			public void onProgressChanged(SeekBar view, int progress, boolean fromUser) {
				mSeekArc.invalidate();
			}
		});
		
		mRoundedEdges.setOnCheckedChangeListener((buttonView, isChecked) -> {
			mSeekArc.setRoundedEdges(isChecked);
			mSeekArc.invalidate();
		});
		
		mTouchInside.setOnCheckedChangeListener((buttonView, isChecked) -> mSeekArc.setTouchInSide(isChecked));
		
		mClockwise.setOnCheckedChangeListener((buttonView, isChecked) -> {
			mSeekArc.setClockwise(isChecked);
			mSeekArc.invalidate();
		});

		mEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
			mSeekArc.setEnabled(isChecked);
			mSeekArc.invalidate();
		});
		
	}
	
}
