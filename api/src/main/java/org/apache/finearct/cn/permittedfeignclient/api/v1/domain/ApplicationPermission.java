/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.finearct.cn.permittedfeignclient.api.v1.domain;

import java.util.Objects;
import javax.annotation.Nullable;
import javax.validation.Valid;
import org.apache.fineract.cn.identity.api.v1.domain.Permission;
import org.apache.fineract.cn.lang.validation.constraints.ValidIdentifier;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings("unused")
public class ApplicationPermission {
  @ValidIdentifier
  private String endpointSetIdentifier;
  @Valid
  private Permission permission;

  public ApplicationPermission() {
  }

  public ApplicationPermission(@Nullable String endpointSetIdentifier, Permission permission) {
    this.endpointSetIdentifier = endpointSetIdentifier;
    this.permission = permission;
  }

  public String getEndpointSetIdentifier() {
    return endpointSetIdentifier;
  }

  public void setEndpointSetIdentifier(String endpointSetIdentifier) {
    this.endpointSetIdentifier = endpointSetIdentifier;
  }

  public Permission getPermission() {
    return permission;
  }

  public void setPermission(Permission permission) {
    this.permission = permission;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ApplicationPermission that = (ApplicationPermission) o;
    return Objects.equals(endpointSetIdentifier, that.endpointSetIdentifier) &&
            Objects.equals(permission, that.permission);
  }

  @Override
  public int hashCode() {
    return Objects.hash(endpointSetIdentifier, permission);
  }

  @Override
  public String toString() {
    return "ApplicationPermission{" +
            "endpointSetIdentifier='" + endpointSetIdentifier + '\'' +
            ", permission=" + permission +
            '}';
  }
}