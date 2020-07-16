/*===========================================================================
  Copyright (C) 2011-2017 by the Okapi Framework contributors
-----------------------------------------------------------------------------
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
===========================================================================*/

package net.sf.okapi.connectors.google;

import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.TextInputPart;

public abstract class GoogleMTParameters extends StringParameters implements IEditorDescriptionProvider {

	private static final String PROJECTID = "projectId";
	private static final String MODELID = "modelId";
	private static final String LOCATION = "location";
	private static final String GLOSSARYID = "glossaryId";
	private static final String RETRY_MS = "retryIntervalMs";
	private static final String RETRY_COUNT = "retryCount";
	private static final String FAILURES_BEFORE_ABORT = "failuresBeforeAbort";
	private static final String USE_PBMT = "usePBMT";
	private static final String inputUri = "gs://your-gcs-bucket/path/to/input/file.txt";
	private static final String outputUri = "gs://your-gcs-bucket/path/to/results/";


	public int getRetryIntervalMs () {
	    return getInteger(RETRY_MS);
	}

	public void setRetryIntervalMs (int retryMs) {
	    setInteger(RETRY_MS, retryMs);
	}

	public int getRetryCount () {
	    return getInteger(RETRY_COUNT);
	}

	public void setRetryCount (int retryCount) {
	    setInteger(RETRY_COUNT, retryCount);
	}
	
	public int getFailuresBeforeAbort () {
		return getInteger(FAILURES_BEFORE_ABORT);
	}
	
	// Use -1 for not aborting (backward compatible behavior)
	public void setFailuresBeforeAbort (int failuresBeforeAbort) {
		setInteger(FAILURES_BEFORE_ABORT, failuresBeforeAbort);
	}

	@Override
	public void reset () {
		super.reset();
		// The most likely error we will encounter is the rate limit of 100k
		// characters translated per 100 seconds.  We will retry every 10s
		// up to 10x, which is enough to flush the rate limit.
		setRetryIntervalMs(10 * 1000);
		setRetryCount(10);
		setFailuresBeforeAbort(-1);
	}

	@Override
	public abstract ParametersDescription getParametersDescription ();

	@Override
	public abstract EditorDescription createEditorDescription (ParametersDescription paramsDesc);

}
