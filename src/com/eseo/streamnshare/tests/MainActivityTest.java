package com.eseo.streamnshare.tests;

import android.test.ActivityInstrumentationTestCase2;

import com.eseo.streamnshare.activities.main.MainActivity;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

	MainActivity mActivity;

	public MainActivityTest() {
		super(MainActivity.class);
	} // end of MainActivityTest constructor definition

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		setActivityInitialTouchMode(false);

		mActivity = getActivity();

	} // end of setUp() method definition

	public void testPreConditions() {
		/*
		    assertTrue(mSpinner.getOnItemSelectedListener() != null);
		    assertTrue(mPlanetData != null);
		    assertEquals(mPlanetData.getCount(),ADAPTER_COUNT);
		 */
	} // end of testPreConditions() method definition

}
