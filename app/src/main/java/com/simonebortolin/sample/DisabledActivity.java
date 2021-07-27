package com.simonebortolin.sample;

import android.app.Activity;
import android.os.Bundle;

public class DisabledActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_disabled);

		//SeekArc seekArcComplete = (SeekArc) findViewById(R.id.seekArcComplete);
		//SeekArc seekArcWarning = (SeekArc) findViewById(R.id.seekArcWarning);

		//seekArcComplete.setActiveColor(Color.parseColor("#22FF22"));
		//seekArcComplete.setProgress(99);

		//seekArcWarning.setActiveColor(Color.parseColor("#FF2222"));
		//seekArcWarning.setInactiveColor(Color.parseColor("#c2c2c2"));
		//seekArcWarning.setProgress(33);

	}
	
}
