/*===========================================================================
  Copyright (C) 2017 by the Okapi Framework contributors
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.Util;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.connectors.google.GoogleMTAPI;
import net.sf.okapi.connectors.google.GoogleResponseParser;
import net.sf.okapi.connectors.google.GoogleQueryBuilder;
import net.sf.okapi.connectors.google.TranslationResponse;

import com.google.cloud.translate.v3.LocationName;
import com.google.cloud.translate.v3.TranslateTextRequest;
import com.google.cloud.translate.v3.TranslateTextResponse;
import com.google.cloud.translate.v3.Translation;
import com.google.cloud.translate.v3.TranslationServiceClient;
import com.google.cloud.translate.v3.GetSupportedLanguagesRequest;
import com.google.cloud.translate.v3.SupportedLanguage;
import com.google.cloud.translate.v3.SupportedLanguages;
import com.google.cloud.translate.v3.GlossaryName;
import com.google.cloud.translate.v3.TranslateTextGlossaryConfig;

public class GoogleMTAPIImpl implements GoogleMTAPI {
    private final Logger LOG = LoggerFactory.getLogger(getClass());
    private GoogleMTv3Parameters params;
    private GoogleResponseParser parser = new GoogleResponseParser();
    private TranslationServiceClient client;
    private GoogleAuthentication auth;

    public GoogleMTAPIImpl(GoogleMTv3Parameters params) {
        this.params = params;
        auth = new GoogleAuthentication();
    }

    @Override
    public List<String> getLanguages() throws IOException, ParseException {
        if (client == null) {
            try {
                auth.setCredentialFilePath(params.getGoogleCredentials());
                client = auth.getTranslationServiceSettings();
            } catch (OkapiException e) {
                throw new OkapiException("Error creating service client: " + e.getMessage(), e);
            }
        }
        LocationName parent = LocationName.of(params.getProjectId(), "global");
        GetSupportedLanguagesRequest request =
                GetSupportedLanguagesRequest.newBuilder().setParent(parent.toString()).build();

        SupportedLanguages response = client.getSupportedLanguages(request);
        List<String> languages = new ArrayList<>();
        for (SupportedLanguage language : response.getLanguagesList()) {
            languages.add(language.getLanguageCode());
        }
        return languages;
    }

    @Override
    public <T> List<TranslationResponse> translate(GoogleQueryBuilder<T> qb) throws IOException, ParseException {
        if (client == null) {
            try {
                auth.setCredentialFilePath(params.getGoogleCredentials());
                client = auth.getTranslationServiceSettings();
            } catch (OkapiException e) {
                throw new OkapiException("Error creating service client: " + e.getMessage(), e);
            }
        }
        String projectCredentials = params.getProjectId();
        String location = params.getLocation();
        LocationName parent = LocationName.of(projectCredentials, location);
        TranslateTextRequest.Builder request =
                TranslateTextRequest.newBuilder()
                        .setParent(parent.toString())
                        .setSourceLanguageCode(qb.getSourceCode())
                        .setTargetLanguageCode(qb.getTargetCode())
                        .addContents(qb.getQuery());
        String modelPath = getModelPath(projectCredentials, location);
        TranslateTextGlossaryConfig glossaryConfig = getGlossaryConfig(projectCredentials, location);
        if (modelPath != null) {
            request.setModel(modelPath);
        }
        if (glossaryConfig != null) {
            request.setGlossaryConfig(glossaryConfig);
        }
        TranslateTextResponse response = client.translateText(request.build());
        List<Translation> responseList = response.getTranslationsList();
        List<TranslationResponse> responses = new ArrayList<>();
        if (qb.getSourceCount() != responseList.size()) {
            LOG.error("Received {} translations for {} sources in query {}", responseList.size(),
                    qb.getSourceCount(), qb.getQuery());
            throw new OkapiException("API returned incorrect number of translations (expected " +
                    qb.getSourceCount() + ", got " + responseList.size());
        }
        for (int i = 0; i < qb.getSourceCount(); i++) {
            responses.add(new TranslationResponse(qb.getSourceTexts().get(i), responseList.get(i).getTranslatedText()));
            System.out.println(responseList.get(i).getTranslatedText());
        }
        return responses;
    }

    private TranslateTextGlossaryConfig getGlossaryConfig(String projectCredentials, String location) {
        if (!Util.isEmpty(params.getGlossaryId())) {
            if (!location.equals("us-central1")) {
                LOG.error("Glossary location set to {}", location);
                throw new OkapiException("Glossary location cannot be " +
                        location + ", must be us-central1.");
            }
            GlossaryName glossaryName = GlossaryName.of(projectCredentials, location, params.getGlossaryId());
            TranslateTextGlossaryConfig glossaryConfig =
                    TranslateTextGlossaryConfig.newBuilder().setGlossary(glossaryName.toString()).build();
            return glossaryConfig;
        }
        return null;
    }

    private String getModelPath(String projectCredentials, String location) {
        if (!Util.isEmpty(params.getModelId())) {
            String modelPath =
                    String.format("projects/%s/locations/%s/models/%s", projectCredentials, location, params.getModelId());
            return modelPath;
        }
        return null;
    }

    public void setParameters(IParameters params) {
        this.params = (GoogleMTv3Parameters) params;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> TranslationResponse translateSingleSegment(GoogleQueryBuilder<T> qb, String sourceText)
            throws IOException, ParseException {
        return null;
    }


}
