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
package org.jclouds.ec2.xml;

import static org.jclouds.util.SaxUtils.currentOrNull;
import static org.jclouds.util.SaxUtils.equalsOrSuffix;

import javax.annotation.Resource;
import javax.inject.Inject;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.jclouds.aws.util.AWSUtils;
import org.jclouds.ec2.domain.Hypervisor;
import org.jclouds.ec2.domain.Image;
import org.jclouds.ec2.domain.Image.Architecture;
import org.jclouds.ec2.domain.Image.EbsBlockDevice;
import org.jclouds.ec2.domain.Image.ImageState;
import org.jclouds.ec2.domain.Image.ImageType;
import org.jclouds.ec2.domain.RootDeviceType;
import org.jclouds.ec2.domain.VirtualizationType;
import org.jclouds.http.functions.ParseSax;
import org.jclouds.location.Region;
import org.jclouds.logging.Logger;
import org.xml.sax.Attributes;

/**
 * Parses the following XML document:
 * <p/>
 * DescribeImagesResponse xmlns="http://ec2.amazonaws.com/doc/2010-08-31/"
 * 
 * @see <a href="http://docs.amazonwebservices.com/AWSEC2/latest/APIReference/ApiReference-query-DescribeImages.html"
 *      />
 */
public class DescribeImagesResponseHandler extends ParseSax.HandlerForGeneratedRequestWithResult<Set<Image>> {

	@Inject
	public DescribeImagesResponseHandler(@Region Supplier<String> defaultRegion, TagSetHandler tagSetHandler) {
		this.defaultRegion = defaultRegion;
		this.tagSetHandler = tagSetHandler;
	}

	@Resource
	protected Logger logger = Logger.NULL;

	protected Set<Image> contents = Sets.newLinkedHashSet();
	private StringBuilder currentText = new StringBuilder();
	private final Supplier<String> defaultRegion;
	private final TagSetHandler tagSetHandler;

	private Architecture architecture;
	private String name;
	private String description;
	private String imageId;
	private String imageLocation;
	private String imageOwnerId;
	private ImageState imageState;
	private String rawState;
	private ImageType imageType;
	private boolean isPublic;
	private String kernelId;
	private String platform;
	private Set<String> productCodes = Sets.newHashSet();
	private String ramdiskId;
	private boolean inProductCodes;
	private boolean inBlockDeviceMapping;
	private boolean inTagSet;
	private RootDeviceType rootDeviceType = RootDeviceType.INSTANCE_STORE;
	private Map<String, EbsBlockDevice> ebsBlockDevices = Maps.newHashMap();
	private Map<String, String> tags = Maps.newLinkedHashMap();
	private String deviceName;
	private String snapshotId;
	private VirtualizationType virtualizationType = VirtualizationType.PARAVIRTUAL;
	private Hypervisor hypervisor = Hypervisor.XEN;

	private int volumeSize;
	private boolean deleteOnTermination = true; // correct default is true.
	private boolean encrypted = false;
	private String volumeType;
	private Integer iops;
	private String rootDeviceName;

	public Set<Image> getResult() {
		return contents;
	}

	public void startElement(String uri, String name, String qName, Attributes attrs) {
		if (qName.equals("productCodes")) {
			inProductCodes = true;
		} else if (qName.equals("blockDeviceMapping")) {
			inBlockDeviceMapping = true;
		} else if (equalsOrSuffix(qName, "tagSet")) {
			inTagSet = true;
		}
		if (inTagSet) {
			tagSetHandler.startElement(uri, name, qName, attrs);
		}
	}

	public void endElement(String uri, String name, String qName) {
		if (equalsOrSuffix(qName, "tagSet")) {
			inTagSet = false;
			tags = tagSetHandler.getResult();
		} else if (inTagSet) {
			tagSetHandler.endElement(uri, name, qName);
		}
		if (qName.equals("architecture")) {
			architecture = Architecture.fromValue(currentText.toString().trim());
			// Nova Diablo uses the wrong name for this field
		} else if (qName.equals("name") || qName.equals("displayName")) {
			this.name = currentText.toString().trim();
		} else if (qName.equals("description")) {
			description = currentText.toString().trim();
		} else if (qName.equals("imageId")) {
			imageId = currentText.toString().trim();
		} else if (qName.equals("deviceName")) {
			deviceName = currentText.toString().trim();
		} else if (qName.equals("imageLocation")) {
			imageLocation = currentText.toString().trim();
		} else if (qName.equals("imageOwnerId")) {
			imageOwnerId = currentText.toString().trim();
		} else if (qName.equals("imageState")) {
			rawState = currentOrNull(currentText);
			imageState = ImageState.fromValue(rawState);
		} else if (qName.equals("imageType")) {
			imageType = ImageType.fromValue(currentText.toString().trim());
		} else if (qName.equals("isPublic")) {
			isPublic = Boolean.parseBoolean(currentText.toString().trim());
		} else if (qName.equals("kernelId")) {
			kernelId = currentText.toString().trim();
		} else if (qName.equals("platform")) {
			platform = currentText.toString().trim();
		} else if (qName.equals("productCode")) {
			productCodes.add(currentText.toString().trim());
		} else if (qName.equals("productCodes")) {
			inProductCodes = false;
		} else if (qName.equals("blockDeviceMapping")) {
			inBlockDeviceMapping = false;
		} else if (qName.equals("snapshotId")) {
			snapshotId = currentText.toString().trim();
		} else if (qName.equals("volumeSize")) {
			volumeSize = Integer.parseInt(currentText.toString().trim());
		} else if (qName.equals("deleteOnTermination")) {
			deleteOnTermination = Boolean.parseBoolean(currentText.toString().trim());
		} else if (qName.equals("encrypted")) {
			encrypted = Boolean.parseBoolean(currentText.toString().trim());
		} else if (qName.equals("iops")) {
			iops = Integer.valueOf(currentText.toString().trim());
		} else if (qName.equals("volumeType")) {
			volumeType = currentText.toString().trim();
		} else if (qName.equals("ramdiskId")) {
			ramdiskId = currentText.toString().trim();
		} else if (qName.equals("rootDeviceType")) {
			rootDeviceType = RootDeviceType.fromValue(currentText.toString().trim());
		} else if (qName.equals("rootDeviceName")) {
			rootDeviceName = currentText.toString().trim();
		} else if (qName.equals("virtualizationType")) {
			virtualizationType = VirtualizationType.fromValue(currentText.toString().trim());
		} else if (qName.equals("hypervisor")) {
			hypervisor = Hypervisor.fromValue(currentText.toString().trim());
		} else if (qName.equals("item")) {
			if (inBlockDeviceMapping) {
				ebsBlockDevices.put(deviceName, new Image.EbsBlockDevice(snapshotId, volumeSize, deleteOnTermination,
						volumeType, iops, encrypted));
				this.deviceName = null;
				this.snapshotId = null;
				this.volumeSize = 0;
				this.deleteOnTermination = true;
				this.encrypted = false;
				this.volumeType = null;
				this.iops = null;
			} else if (!inTagSet && !inProductCodes) {
				try {
					String region = getRequest() != null ? AWSUtils.findRegionInArgsOrNull(getRequest()) : null;
					if (region == null)
						region = defaultRegion.get();
					contents.add(new Image(region, architecture, this.name, description, imageId, imageLocation,
							imageOwnerId, imageState, rawState, imageType, isPublic, productCodes, kernelId, platform,
							ramdiskId, rootDeviceType, rootDeviceName, ebsBlockDevices, tags, virtualizationType,
							hypervisor));
				} catch (NullPointerException e) {
					logger.warn(e, "malformed image: %s", imageId);
				}
				this.name = null;
				this.description = null;
				this.architecture = null;
				this.imageId = null;
				this.imageLocation = null;
				this.imageOwnerId = null;
				this.imageState = null;
				this.rawState = null;
				this.imageType = null;
				this.isPublic = false;
				this.kernelId = null;
				this.platform = null;
				this.productCodes = Sets.newHashSet();
				this.ramdiskId = null;
				this.rootDeviceType = RootDeviceType.INSTANCE_STORE;
				this.rootDeviceName = null;
				this.ebsBlockDevices = Maps.newHashMap();
				this.virtualizationType = VirtualizationType.PARAVIRTUAL;
				this.hypervisor = Hypervisor.XEN;
			}

		}
		currentText.setLength(0);
	}

	public void characters(char ch[], int start, int length) {
		if (inTagSet) {
			tagSetHandler.characters(ch, start, length);
		} else {
			currentText.append(ch, start, length);
		}
	}
}
