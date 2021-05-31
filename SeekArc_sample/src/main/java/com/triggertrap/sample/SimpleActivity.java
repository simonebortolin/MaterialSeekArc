package com.triggertrap.sample;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.triggertrap.seekarc.SeekArc;

public class SimpleActivity extends Activity {

	protected int getLayoutFile(){
		return R.layout.holo_sample;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayoutFile());

		SeekArc mSeekArc = findViewById(R.id.seekArc);
		TextView mSeekArcProgress = findViewById(R.id.seekArcProgress);
		SeekBar mRotation = findViewById(R.id.rotation);
		SeekBar mStartAngle = findViewById(R.id.startAngle);
		SeekBar mSweepAngle = findViewById(R.id.sweepAngle);
		SeekBar mArcWidth = findViewById(R.id.arcWidth);
		SeekBar mProgressWidth = findViewById(R.id.progressWidth);
		CheckBox mRoundedEdges = findViewById(R.id.roundedEdges);
		CheckBox mTouchInside = findViewById(R.id.touchInside);
		CheckBox mClockwise = findViewById(R.id.clockwise);
		CheckBox mEnabled = findViewById(R.id.enabled);
		Button mButton = findViewById(R.id.button);
		Button mCancel = findViewById(R.id.cancel);

		mButton.setOnClickListener(v -> mSeekArc.setProgress(10000,true));
		mCancel.setOnClickListener(v -> mSeekArc.stopProgress());


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
				mSeekArc.setActiveWidth(progress);
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
