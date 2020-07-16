/*===========================================================================
  Copyright (C) 2018 by the Okapi Framework contributors
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.v3.TranslationServiceSettings;
import com.google.cloud.translate.v3.TranslationServiceClient;
import net.sf.okapi.common.exceptions.OkapiException;

public class GoogleAuthentication {

    private GoogleCredentials credential;

    /**
     * Indicates if the credential for the service has been set.
     *
     * @return true if we have credential, false if we need to set it.
     */
    public boolean hasCredential() {
        return (credential != null);
    }

    /**
     * Sets the service credential.
     *
     * @param inputStream the input stream where the credential is.
     * @see #setCredentialFilePath(String)
     */
    public void setCredential(InputStream inputStream) {
        try {
            credential = GoogleCredentials.fromStream(inputStream);
        } catch (IOException e) {
            credential = null;
            throw new RuntimeException(e);
        }
    }

    /**
     * Calls {@link #setCredential(InputStream)} with a file.
     *
     * @param credentialFilePath the path of the file with the credential.
     */
    public void setCredentialFilePath(String credentialFilePath) {
        try {
            try (FileInputStream accountJson = new FileInputStream(new File(credentialFilePath))) {
                setCredential(accountJson);
            }
        } catch (IOException e) {
            credential = null;
            throw new RuntimeException(e);
        }
    }


    public TranslationServiceClient getTranslationServiceSettings() throws IOException {
        try {
            TranslationServiceSettings translationServiceSettings =
                    TranslationServiceSettings.newBuilder()
                            .setCredentialsProvider(FixedCredentialsProvider.create(credential))
                            .build();
            TranslationServiceClient translationServiceClient =
                    TranslationServiceClient.create(translationServiceSettings);
            return translationServiceClient;
        } catch (IOException e) {
            throw new OkapiException("Error creating service client: " + e.getMessage(), e);
        }
    }
}