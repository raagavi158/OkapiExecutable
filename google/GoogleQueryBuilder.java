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

package net.sf.okapi.connectors.google;

import net.sf.okapi.common.Util;

import java.util.ArrayList;
import java.util.List;

public abstract class GoogleQueryBuilder<T> {
    // "The URL for GET requests, including parameters, must be less than 2K characters."
    // https://cloud.google.com/translate/docs/translating-text#translating_text_1

    protected String srcCode, tgtCode;
    protected List<String> sourceTexts = new ArrayList<>();
    protected List<T> sources = new ArrayList<>();

    public abstract void reset();

    public abstract String getQuery();

    public abstract void addQuery(String sourceText, T source);

    public List<String> getSourceTexts() {
        return sourceTexts;
    }
    public List<T> getSources() {
        return sources;
    }
    public int getSourceCount() {
        return sourceTexts.size();
    }

    public String getTargetCode() { return tgtCode; }
    public String getSourceCode() { return srcCode; }
}
