/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.action.ingest;

import org.elasticsearch.common.io.stream.BytesStreamOutput;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.ingest.RandomDocumentPicks;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.test.ESTestCase;

import java.io.IOException;

import static org.elasticsearch.ingest.IngestDocumentMatcher.assertIngestDocument;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class SimulateProcessorResultTests extends ESTestCase {

    public void testSerialization() throws IOException {
        String processorTag = randomAlphaOfLengthBetween(1, 10);
        boolean isSuccessful = randomBoolean();
        boolean isIgnoredException = randomBoolean();
        SimulateProcessorResult simulateProcessorResult;
        if (isSuccessful) {
            IngestDocument ingestDocument = RandomDocumentPicks.randomIngestDocument(random());
            if (isIgnoredException) {
                simulateProcessorResult = new SimulateProcessorResult(processorTag, ingestDocument, new IllegalArgumentException("test"));
            } else {
                simulateProcessorResult = new SimulateProcessorResult(processorTag, ingestDocument);
            }
        } else {
            simulateProcessorResult = new SimulateProcessorResult(processorTag, new IllegalArgumentException("test"));
        }

        BytesStreamOutput out = new BytesStreamOutput();
        simulateProcessorResult.writeTo(out);
        StreamInput streamInput = out.bytes().streamInput();
        SimulateProcessorResult otherSimulateProcessorResult = new SimulateProcessorResult(streamInput);
        assertThat(otherSimulateProcessorResult.getProcessorTag(), equalTo(simulateProcessorResult.getProcessorTag()));
        if (isSuccessful) {
            assertIngestDocument(otherSimulateProcessorResult.getIngestDocument(), simulateProcessorResult.getIngestDocument());
            if (isIgnoredException) {
                assertThat(otherSimulateProcessorResult.getFailure(), instanceOf(IllegalArgumentException.class));
                IllegalArgumentException e = (IllegalArgumentException) otherSimulateProcessorResult.getFailure();
                assertThat(e.getMessage(), equalTo("test"));
            } else {
                assertThat(otherSimulateProcessorResult.getFailure(), nullValue());
            }
        } else {
            assertThat(otherSimulateProcessorResult.getIngestDocument(), is(nullValue()));
            assertThat(otherSimulateProcessorResult.getFailure(), instanceOf(IllegalArgumentException.class));
            IllegalArgumentException e = (IllegalArgumentException) otherSimulateProcessorResult.getFailure();
            assertThat(e.getMessage(), equalTo("test"));
        }
    }
}
