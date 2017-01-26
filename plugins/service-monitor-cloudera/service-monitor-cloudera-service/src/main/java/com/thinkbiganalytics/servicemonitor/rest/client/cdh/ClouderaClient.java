package com.thinkbiganalytics.servicemonitor.rest.client.cdh;

/*-
 * #%L
 * thinkbig-service-monitor-cloudera
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.cloudera.api.ApiRootResource;
import com.cloudera.api.ClouderaManagerClientBuilder;
import com.thinkbiganalytics.servicemonitor.rest.client.RestClientConfig;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;

/**
 * Created by sr186054 on 10/1/15.
 */
@Component
public class ClouderaClient {

  private static final Logger LOG = LoggerFactory.getLogger(ClouderaClient.class);

  @Autowired
  @Qualifier("clouderaRestClientConfig")
  private RestClientConfig clientConfig;

  private ClouderaManagerClientBuilder clouderaManagerClientBuilder;

  private ClouderaRootResource clouderaRootResource;

  private AtomicBoolean creatingResource = new AtomicBoolean(false);

 private Integer clientAttempts = 0;


  public ClouderaClient() {

  }

  public ClouderaClient(RestClientConfig clientConfig) {

    this.clientConfig = clientConfig;
  }

  @PostConstruct
  public void setClouderaManagerClientBuilder() {
    String host = clientConfig.getServerUrl();
    String portString = clientConfig.getPort();
    if (StringUtils.isBlank(portString)) {
      portString = "7180";
    }
    Integer port = new Integer(portString);
    String username = clientConfig.getUsername();
    String password = clientConfig.getPassword();
    LOG.info("Created New Cloudera Client for Host [" + host + "], user: [" + username + "]");
    this.clouderaManagerClientBuilder =
        new ClouderaManagerClientBuilder().withHost(host).withPort(port).withUsernamePassword(username, password);

  }


  public ClouderaRootResource getClouderaResource() {
    if(clouderaRootResource == null){
      this.clientAttempts++;
    }
    if(clouderaRootResource == null && !creatingResource.get()) {
      creatingResource.set(true);
      try {
        ApiRootResource rootResource = this.clouderaManagerClientBuilder.build();
        clouderaRootResource = ClouderaRootResourceManager.getRootResource(rootResource);
        LOG.info("Successfully Created Cloudera Client");
      }catch (Exception e){
        creatingResource.set(false);
      }
    }
    if(clientAttempts >=3){
      //rest
      clientAttempts = 0;
      creatingResource.set(false);
    }
    return  clouderaRootResource;
  }

  public void setClientConfig(RestClientConfig clientConfig) {
    this.clientConfig = clientConfig;
  }


}