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

import net.sf.okapi.connectors.google.GoogleQueryBuilder;

import java.util.ArrayList;
import java.util.List;

public class GoogleV3QueryBuilder<T> extends GoogleQueryBuilder<T> {
    // "The URL for GET requests, including parameters, must be less than 2K characters."
    // https://cloud.google.com/translate/docs/translating-text#translating_text_1

    private String text;
    protected GoogleMTv3Parameters params;

    public GoogleV3QueryBuilder(String text, GoogleMTv3Parameters params, String srcCode, String tgtCode) {
        this.params = params;
        this.srcCode = srcCode;
        this.tgtCode = tgtCode;
        this.text = text;
        reset();
    }

    public void reset() {
        sourceTexts.clear();
        sources.clear();
    }

    public void addQuery(String sourceText, T source) {
        sourceTexts.add(sourceText);
        sources.add(source);
    }
    public String getQuery() {
        return text;
    }

}
