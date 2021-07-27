package com.simonebortolin.sample;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.simonebortolin.seekarc.MaterialSeekArc;

public class SimpleActivity extends Activity {

	protected int getLayoutFile(){
		return R.layout.holo_sample;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayoutFile());

		MaterialSeekArc mMaterialSeekArc = findViewById(R.id.seekArc);
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

		mButton.setOnClickListener(v -> mMaterialSeekArc.setProgress(10000,true));
		mCancel.setOnClickListener(v -> mMaterialSeekArc.stopProgress());


		mRotation.setProgress(mMaterialSeekArc.getArcRotation());
		mStartAngle.setProgress(mMaterialSeekArc.getStartAngle());
		mSweepAngle.setProgress(mMaterialSeekArc.getSweepAngle());
		mArcWidth.setProgress(mMaterialSeekArc.getInactiveWidth());

		mMaterialSeekArc.setOnSeekArcChangeListener(new MaterialSeekArc.OnSeekProgressChangeListener() {

			@Override
			public void onStopTrackingTouch(MaterialSeekArc materialSeekArc) {
			}

			@Override
			public void onStartTrackingTouch(MaterialSeekArc materialSeekArc) {
			}

			@Override
			public void onProgressChanged(MaterialSeekArc materialSeekArc, int progress,
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
				mMaterialSeekArc.setArcRotation(progress);
				mMaterialSeekArc.invalidate();
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
				mMaterialSeekArc.setStartAngle(progress);
				mMaterialSeekArc.invalidate();
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
				mMaterialSeekArc.setSweepAngle(progress);
				mMaterialSeekArc.invalidate();
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
				mMaterialSeekArc.setInactiveWidth(progress);
				mMaterialSeekArc.invalidate();
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
				mMaterialSeekArc.setActiveWidth(progress);
				mMaterialSeekArc.invalidate();
			}
		});
		
		mRoundedEdges.setOnCheckedChangeListener((buttonView, isChecked) -> {
			mMaterialSeekArc.setRoundedEdges(isChecked);
			mMaterialSeekArc.invalidate();
		});
		
		mTouchInside.setOnCheckedChangeListener((buttonView, isChecked) -> mMaterialSeekArc.setTouchValues(isChecked));
		
		mClockwise.setOnCheckedChangeListener((buttonView, isChecked) -> {
			mMaterialSeekArc.setClockwise(isChecked);
			mMaterialSeekArc.invalidate();
		});

		mEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
			mMaterialSeekArc.setEnabled(isChecked);
			mMaterialSeekArc.invalidate();
		});
		
	}
	
}
