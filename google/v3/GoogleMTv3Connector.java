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

import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.query.QueryResult;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.lib.translation.BaseConnector;
import net.sf.okapi.lib.translation.QueryUtil;
import net.sf.okapi.connectors.google.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class GoogleMTv3Connector extends BaseConnector {
    private String base_url;

    private final Logger LOG = LoggerFactory.getLogger(getClass());
	private GoogleMTv3Parameters params;
	private int failureCount;
    private QueryUtil util;
    private GoogleMTAPI api;

	public GoogleMTv3Connector() {
        params = new GoogleMTv3Parameters();
	    util = new QueryUtil();
        api = new GoogleMTAPIImpl(params);
	}

	public GoogleMTv3Connector(GoogleMTAPI api) {
        params = new GoogleMTv3Parameters();
        util = new QueryUtil();
        this.api = api;
	}

	@Override
	public void setParameters (IParameters params) {
		this.params = (GoogleMTv3Parameters)params;
        api.setParameters(this.params);
	}
	
	@Override
	public GoogleMTv3Parameters getParameters () {
		return params;
	}
	
	@Override
	public void close () {
		// Nothing to do
	}
	@Override
	public String getName () {
		return "Google-MTv3";
	}

	@Override
	public String getSettingsDisplay () {
		return "Server: ";
	}

	@Override
	public void open () {
		failureCount = 0;
	}

	@Override
	public int query (String plainText) {
		return _query(plainText, plainText, new TextQueryResultBuilder(params, getName(), getWeight()));
	}
	
	@Override
	public int query (TextFragment frag) {
	    return _query(util.toCodedHTML(frag), frag, new FragmentQueryResultBuilder(params, getName(), getWeight()));
	}

	private void retryInterval(int retryCount, String operation) {
	    LOG.info("{} - retry {} (waiting {} ms)", operation, retryCount, params.getRetryIntervalMs());
	    try {
            Thread.sleep(params.getRetryIntervalMs());
        } catch (InterruptedException e) {
            throw new OkapiException("Interrupted while trying to contact Google MT");
        }
	}

	protected <T> int _query(String queryText, T originalText, QueryResultBuilder<T> qrBuilder) {

	    current = -1;
        if (queryText.isEmpty()) return 0;
        // Check that we have some json key file available
        if( Util.isEmpty(params.getGoogleCredentials())) {
            throw new OkapiException("This connector requires for a Google service account.");
        }
        // Check that we have some Key available
        if ( Util.isEmpty(params.getProjectId())) {
            throw new OkapiException("You must have a Google project ID to use this connector.");
        }

        List<QueryResult> queryResults = new ArrayList<>();
        GoogleV3QueryBuilder<T> qb = new GoogleV3QueryBuilder<>(queryText, getParameters(), srcCode, trgCode);
        qb.addQuery(queryText, originalText);
        List<TranslationResponse> responses = executeQuery(qb, qrBuilder);
        if (responses != null) {
            queryResults.addAll(qrBuilder.convertResponses(responses, originalText));
        }
        else {
            // Underlying call failed for some reason, probably a timeout
            LOG.error("Received no results for query {}", qb.getQuery());
            // Return the source text as a dummy translation so that we can maintain the correct indexing
            queryResults.add(qrBuilder.createDummyResponse(originalText));
        }
        if (queryResults.size() > 0) {
            current = 0;
            result = queryResults.iterator().next();
            return 1;
        }
        throw new OkapiException("Could not retrieve results from Google after " +
                                 params.getRetryCount() + " attempts.");
	}

    @Override
    public List<List<QueryResult>> batchQueryText(List<String> plainTexts) {
        return _batchQuery(plainTexts, plainTexts, new TextQueryResultBuilder(params, getName(), getWeight()));
    }

    @Override
    public List<List<QueryResult>> batchQuery (List<TextFragment> fragments) {
        return _batchQuery(util.toCodedHTML(fragments), fragments,
                new FragmentQueryResultBuilder(params, getName(), getWeight()));
    }

    protected <T> List<List<QueryResult>> _batchQuery(List<String> texts, List<T> originalTexts,
                                                      QueryResultBuilder<T> qrBuilder) {
         return null;
    }


    protected <T> List<TranslationResponse> executeQuery(GoogleV3QueryBuilder<T> qb, QueryResultBuilder<T> qrBuilder) {
        try {
            for (int tries = 0; tries < params.getRetryCount(); tries++) {
                try {
                    return api.translate(qb);
                }
                catch (GoogleMTErrorException e) {
                    LOG.error("Error {} - {} for query {}", e.getCode(), e.getMessage(), e.getQuery());
                }
                retryInterval(tries + 1, "_batchQuery");
            }
        }
        catch (Throwable e) {
            throw new OkapiException("Error querying the MT server: " + e.getMessage(), e);
        }
        // All retries have failed
        if (( params.getFailuresBeforeAbort() > -1 ) && ( ++failureCount > params.getFailuresBeforeAbort() )) {
        	throw new OkapiException("Too many retry failures while querying the MT server.");
        }
        return null;
    }

    public List<LocaleId> getSupportedLanguages() {
        try {
            for (int tries = 0; tries < params.getRetryCount(); tries++) {
                List<String> codes = api.getLanguages();
                if (codes != null) {
                    List<LocaleId> locales = new ArrayList<>();
                    for (String code : codes) {
                        locales.add(convertGoogleLanguageCode(code));
                    }
                    return locales;
                }
                retryInterval(tries + 1, "getSupportedLanguages");

            }
        }
        catch ( Throwable e) {
            throw new OkapiException("Error querying the MT server: " + e.getMessage(), e);
        }
        throw new OkapiException("Could not retrieve language list from Google after " +
                                 params.getRetryCount() + " attempts.");
    }

    protected LocaleId convertGoogleLanguageCode(String lang) {
        return LocaleId.fromBCP47(lang);
    }

	@Override
	public void leverage (ITextUnit tu) {
		leverageUsingBatchQuery(tu);
	}

	@Override
	public void batchLeverage (List<ITextUnit> tuList) {
		batchLeverageUsingBatchQuery(tuList);
	}

	@Override
	protected String toInternalCode (LocaleId locale) {
		String code = locale.toBCP47();
		if ( !code.startsWith("zh") && ( code.length() > 2 )) {
			code = code.substring(0, 2);
		}
		return code;
	}


}
