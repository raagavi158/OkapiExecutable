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

package net.sf.okapi.connectors.google.v3;

import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.TextInputPart;
import net.sf.okapi.connectors.google.GoogleMTParameters;


public class GoogleMTv3Parameters extends GoogleMTParameters {

	private static final String PROJECTID = "projectId";
	private static final String MODELID = "modelId";
	private static final String LOCATION = "location";
	private static final String GLOSSARYID = "glossaryId";
	private static final String RETRY_MS = "retryIntervalMs";
	private static final String RETRY_COUNT = "retryCount";
	private static final String FAILURES_BEFORE_ABORT = "failuresBeforeAbort";
	private static final String GOOGLE_APPLICATION_CREDENTIALS = "googleCredentials";

	public String getProjectId() {
		return getString(PROJECTID).trim();
	}

	public void setProjectId (String projectId) {
		if (projectId != null) {
			projectId = projectId.trim();
		}
		setString(PROJECTID, projectId);
	}

	public String getModelId() {
		return getString(MODELID).trim();
	}

	public void setModelId (String modelId) {
		if (modelId != null) {
			modelId = modelId.trim();
		}
		setString(MODELID, modelId);
	}

	public String getGlossaryId() {
		return getString(GLOSSARYID).trim();
	}

	public void setGlossaryId (String glossaryId) {
		if (glossaryId != null) {
			glossaryId = glossaryId.trim();
		}
		setString(MODELID, glossaryId);
	}

	public String getLocation () {
		return getString(LOCATION).trim();
	}

	public void setLocation (String location) {
		if (location != null) {
			location = location.trim();
		}
		setString(LOCATION, location);
	}

	public String getGoogleCredentials() {
		return getString(GOOGLE_APPLICATION_CREDENTIALS).trim();
	}

	public void setGoogleCredentials (String googleCredentials) {
		if (googleCredentials != null) {
			googleCredentials = googleCredentials.trim();
		}
		setString(GOOGLE_APPLICATION_CREDENTIALS, googleCredentials);
	}

	@Override
	public void reset () {
		super.reset();
		setProjectId("");
		setModelId("");
		setGlossaryId("");
		setLocation("global");
		setGoogleCredentials("");
		// The most likely error we will encounter is the rate limit of 100k
		// characters translated per 100 seconds.  We will retry every 10s
		// up to 10x, which is enough to flush the rate limit.
		setRetryIntervalMs(10 * 1000);
		setRetryCount(10);
		setFailuresBeforeAbort(-1);
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(GOOGLE_APPLICATION_CREDENTIALS,
				"Google Application Credentials",
				"Absolute path to a JSON file containing private key information for a Google service account");
		desc.add(PROJECTID,
			"Google Project ID",
			"The Google Project ID to identify the application/user");
		desc.add(GLOSSARYID,
				"Google Glossary ID",
				"The Google Glossary ID to fetch the glossary to be used");
		desc.add(MODELID,
				"Google Model ID",
				"The Google Model ID to fetch the predicitive model to be used");
		desc.add(LOCATION,
				"Google Cloud Location",
				"The location of the Google Cloud project");
		desc.add(RETRY_COUNT,
	        "Retry Count",
	        "Number of retries to attempt before failing");
		desc.add(RETRY_MS,
		        "Retry Interval (ms)",
		        "Time to wait before retrying a failed query");
		desc.add(FAILURES_BEFORE_ABORT,
		        "Failures before abort",
		        "Number of times we let queries fail (after retries) before aborting the process");
		return desc;
	}

	@Override
	public EditorDescription createEditorDescription (ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("Google Translate v3 Connector Settings", true, false);
		TextInputPart tip = desc.addTextInputPart(paramsDesc.get(PROJECTID));
		tip.setPassword(true);
		desc.addTextInputPart(paramsDesc.get(GOOGLE_APPLICATION_CREDENTIALS));
		desc.addTextInputPart(paramsDesc.get(GLOSSARYID));
		desc.addTextInputPart(paramsDesc.get(MODELID));
		desc.addTextInputPart(paramsDesc.get(LOCATION));
		desc.addTextInputPart(paramsDesc.get(RETRY_COUNT));
		desc.addTextInputPart(paramsDesc.get(RETRY_MS));
		desc.addTextInputPart(paramsDesc.get(FAILURES_BEFORE_ABORT));
		return desc;
	}

}
