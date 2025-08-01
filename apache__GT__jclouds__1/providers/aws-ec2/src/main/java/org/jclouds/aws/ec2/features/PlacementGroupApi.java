/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jclouds.aws.ec2.features;

import static org.jclouds.aws.reference.FormParameters.ACTION;

import java.util.Set;

import javax.inject.Named;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.jclouds.Fallbacks.EmptySetOnNotFoundOr404;
import org.jclouds.Fallbacks.VoidOnNotFoundOr404;
import org.jclouds.aws.ec2.domain.PlacementGroup;
import org.jclouds.aws.ec2.xml.DescribePlacementGroupsResponseHandler;
import org.jclouds.aws.filters.FormSigner;
import org.jclouds.ec2.binders.BindFiltersToIndexedFormParams;
import org.jclouds.ec2.binders.BindGroupNamesToIndexedFormParams;
import org.jclouds.javax.annotation.Nullable;
import org.jclouds.location.functions.RegionToEndpointOrProviderIfNull;
import org.jclouds.rest.annotations.BinderParam;
import org.jclouds.rest.annotations.EndpointParam;
import org.jclouds.rest.annotations.Fallback;
import org.jclouds.rest.annotations.FormParams;
import org.jclouds.rest.annotations.RequestFilters;
import org.jclouds.rest.annotations.VirtualHost;
import org.jclouds.rest.annotations.XMLResponseParser;

import com.google.common.collect.Multimap;

/**
 * Provides access to EC2 Placement Groups via their REST API.
 * <p/>
 */
@RequestFilters(FormSigner.class)
@VirtualHost
public interface PlacementGroupApi {

	/**
	* Creates a placement group that you launch cluster compute instances into. You must give the
	* group a name unique within the scope of your account.
	* 
	* @param region
	*           Region to create the placement group in.
	* @param name
	*           The name of the placement group..
	* @param strategy
	*           The placement group strategy.
	* @see #describePlacementGroupsInRegion
	* @see #deletePlacementGroupInRegion
	* 
	* @see <a href="http://docs.amazonwebservices.com/AWSEC2/latest/APIReference/ApiReference-query-CreatePlacementGroup.html"
	*      />
	*/
	@Named("CreatePlacementGroup")
	@POST
	@Path("/")
	@FormParams(keys = ACTION, values = "CreatePlacementGroup")
	void createPlacementGroupInRegion(
			@EndpointParam(parser = RegionToEndpointOrProviderIfNull.class) @Nullable String region,
			@FormParam("GroupName") String name, @FormParam("Strategy") String strategy);

	/**
	* like {@link #createPlacementGroupInRegion(String,String,String) except that the strategy is default: "cluster".
	*/
	@Named("CreatePlacementGroup")
	@POST
	@Path("/")
	@FormParams(keys = { ACTION, "Strategy" }, values = { "CreatePlacementGroup", "cluster" })
	void createPlacementGroupInRegion(
			@EndpointParam(parser = RegionToEndpointOrProviderIfNull.class) @Nullable String region,
			@FormParam("GroupName") String name);

	/**
	* Deletes a placement group from your account. You must terminate all instances in the placement group before deleting it.
	* 
	* @param region
	*           Region to delete the placement from from
	* @param name
	*           Name of the security group to delete.
	* 
	* @see #describePlacementGroupsInRegion
	* @see #createPlacementGroupInRegion
	* 
	* @see <a href="http://docs.amazonwebservices.com/AWSEC2/latest/APIReference/ApiReference-query-DeletePlacementGroup.html"
	*      />
	*/
	@Named("DeletePlacementGroup")
	@POST
	@Path("/")
	@FormParams(keys = ACTION, values = "DeletePlacementGroup")
	@Fallback(VoidOnNotFoundOr404.class)
	void deletePlacementGroupInRegion(
			@EndpointParam(parser = RegionToEndpointOrProviderIfNull.class) @Nullable String region,
			@FormParam("GroupName") String name);

	/**
	* 
	* Returns information about one or more placement groups in your account.
	* 
	* @param region
	*           The bundleTask ID is tied to the Region.
	* @param groupNames
	*           The name of the placement group. You can specify more than one in the request, or
	*           omit the parameter if you want information about all your placement groups. By
	*           default, all placement groups are described
	* 
	* @see #deletePlacementGroupInRegion
	* @see #createPlacementGroupInRegion
	* @see <a href="http://docs.amazonwebservices.com/AWSEC2/latest/APIReference/ApiReference-query-DescribePlacementGroups.html"
	*      />
	*/
	@Named("DescribePlacementGroups")
	@POST
	@Path("/")
	@FormParams(keys = ACTION, values = "DescribePlacementGroups")
	@XMLResponseParser(DescribePlacementGroupsResponseHandler.class)
	@Fallback(EmptySetOnNotFoundOr404.class)
	Set<PlacementGroup> describePlacementGroupsInRegion(
			@EndpointParam(parser = RegionToEndpointOrProviderIfNull.class) @Nullable String region,
			@BinderParam(BindGroupNamesToIndexedFormParams.class) String... placementGroupIds);

	/**
	*
	* Returns information about one or more placement groups in your account.
	*
	* @param region
	*           The bundleTask ID is tied to the Region.
	* @param filter
	*           Multimap of filter key/values
	*
	* @see #deletePlacementGroupInRegion
	* @see #createPlacementGroupInRegion
	* @see <a href="http://docs.amazonwebservices.com/AWSEC2/latest/APIReference/ApiReference-query-DescribePlacementGroups.html"
	*      />
	*/
	@Named("DescribePlacementGroups")
	@POST
	@Path("/")
	@FormParams(keys = ACTION, values = "DescribePlacementGroups")
	@XMLResponseParser(DescribePlacementGroupsResponseHandler.class)
	@Fallback(EmptySetOnNotFoundOr404.class)
	Set<PlacementGroup> describePlacementGroupsInRegionWithFilter(
			@EndpointParam(parser = RegionToEndpointOrProviderIfNull.class) @Nullable String region,
			@BinderParam(BindFiltersToIndexedFormParams.class) Multimap<String, String> filter);
}
