/**
 *  Copyright 2014 Red Hat, Inc.
 *
 *  Red Hat licenses this file to you under the Apache License, version
 *  2.0 (the "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */

package io.fabric8.jgroups.zookeeper;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class Constants {
    public static final short WF_ZK_PING_ID = 1001; // wildfly
    public static final short CONFIGURABLE_ZK_PING_ID = 1002;
    public static final short MANAGED_ZK_PING_ID = 1003;

    public static final String ZOOKEEPER_URL = "zookeeper.url";
    public static final String ZOOKEEPER_PASSWORD = "zookeeper.password";
    public static final String ENSEMBLE_ID = "ensemble.id";

    public static final String SESSION_TIMEOUT = "sessionTimeOutMs";
    public static final String CONNECTION_TIMEOUT = "connectionTimeOutMs";

    public static final String RETRY_POLICY_MAX_RETRIES = "retryPolicy.maxRetries";
    public static final String RETRY_POLICY_INTERVAL_MS = "retryPolicy.retryIntervalMs";

    public static final int DEFAULT_CONNECTION_TIMEOUT_MS = 15 * 1000;
    public static final int DEFAULT_SESSION_TIMEOUT_MS = 60 * 1000;
    public static final int MAX_RETRIES_LIMIT = 3;
    public static final int DEFAULT_RETRY_INTERVAL = 500;

}
