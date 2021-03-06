/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.brooklyn.location.jclouds;

import org.apache.brooklyn.core.location.cloud.CloudLocationConfig;
import org.apache.brooklyn.core.location.cloud.names.BasicCloudMachineNamer;
import org.apache.brooklyn.util.core.config.ConfigBag;
import org.apache.brooklyn.util.text.Identifiers;

public class JcloudsMachineNamer extends BasicCloudMachineNamer {

    @Override
    /** returns the max length of a VM name for the cloud specified in setup;
     * this value is typically decremented by 9 to make room for jclouds labels */
    public Integer getCustomMaxNameLength(ConfigBag setup) {
        // otherwise, for some known clouds which only allow a short name, use that length
        if ("vcloud".equals( setup.peek(JcloudsLocationConfig.CLOUD_PROVIDER) ))
            return 24;
        if ("abiquo".equals( setup.peek(JcloudsLocationConfig.CLOUD_PROVIDER) ))
            return 39;
        if ("google-compute-engine".equals( setup.peek(JcloudsLocationConfig.CLOUD_PROVIDER) ))
            return 39;
        if ("softlayer".equals( setup.peek(JcloudsLocationConfig.CLOUD_PROVIDER) ))
            return 55;
        // TODO other cloud max length rules

        return null;
    }

    @Override
    protected String generateNewIdOfLength(ConfigBag setup, int len) {

        // if it's azurecompute-arm it needs a different VM_NAME_ALLOWED_PATTERN
        String pattern = setup.get(CloudLocationConfig.VM_NAME_ALLOWED_CHARACTERS);
        if ((pattern == null || pattern == CloudLocationConfig.VM_NAME_ALLOWED_CHARACTERS.getDefaultValue()) &&
                "azurecompute-arm".equals(setup.peek(JcloudsLocationConfig.CLOUD_PROVIDER))) {
        setup.put(CloudLocationConfig.VM_NAME_ALLOWED_CHARACTERS, Identifiers.UPPER_CASE_ALPHA+Identifiers.LOWER_CASE_ALPHA+Identifiers.NUMERIC);
        }

        return super.generateNewIdOfLength(setup, len);
    }

}
