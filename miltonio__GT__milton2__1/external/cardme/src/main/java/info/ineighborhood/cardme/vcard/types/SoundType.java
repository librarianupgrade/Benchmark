/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package info.ineighborhood.cardme.vcard.types;

import info.ineighborhood.cardme.util.StringUtil;
import info.ineighborhood.cardme.util.Util;
import info.ineighborhood.cardme.vcard.EncodingType;
import info.ineighborhood.cardme.vcard.VCardType;
import info.ineighborhood.cardme.vcard.features.SoundFeature;
import info.ineighborhood.cardme.vcard.types.media.AudioMediaType;
import info.ineighborhood.cardme.vcard.types.parameters.ParameterTypeStyle;
import info.ineighborhood.cardme.vcard.types.parameters.SoundParameterType;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

/**
 * Copyright (c) 2004, Neighborhood Technologies
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * Neither the name of Neighborhood Technologies nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * 
 * @author George El-Haddad
 * <br/>
 * Feb 4, 2010
 *
 */
public class SoundType extends Type implements SoundFeature {

	private byte[] soundBytes = null;
	private URI soundUri = null;
	private SoundParameterType soundParameterType = null;
	private AudioMediaType audioMediaType = null;
	private boolean isSetCompression = false;

	public SoundType() {

	}

	public SoundType(URI soundUri, EncodingType encodingType, SoundParameterType soundParameterType) {
		super(encodingType, ParameterTypeStyle.PARAMETER_VALUE_LIST);
		setSoundURI(soundUri);
		setSoundParameterType(soundParameterType);
	}

	public SoundType(byte[] soundBytes, EncodingType encodingType, SoundParameterType soundParameterType) {
		super(encodingType, ParameterTypeStyle.PARAMETER_VALUE_LIST);
		setSound(soundBytes);
		setSoundParameterType(soundParameterType);
	}

	/**
	 * {@inheritDoc}
	 */
	public byte[] getSound() {
		return soundBytes;
	}

	/**
	 * {@inheritDoc}
	 */
	public URI getSoundURI() {
		return soundUri;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasSound() {
		return soundBytes != null || soundUri != null;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isURI() {
		return soundUri != null;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isInline() {
		return soundBytes != null;
	}

	/**
	 * {@inheritDoc}
	 */
	public SoundParameterType getSoundParameterType() {
		return soundParameterType;
	}

	/**
	 * {@inheritDoc}
	 */
	public AudioMediaType getAudioMediaType() {
		return audioMediaType;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setSoundParameterType(SoundParameterType soundParameterType) {
		this.soundParameterType = soundParameterType;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAudioMediaType(AudioMediaType audioMediaType) {
		this.audioMediaType = audioMediaType;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setSound(byte[] soundBytes) {
		this.soundBytes = soundBytes;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setSoundURI(URI soundUri) {
		this.soundUri = soundUri;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasSoundParameterType() {
		return soundParameterType != null;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasAudioMediaType() {
		return audioMediaType != null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setCompression(boolean compression) {
		isSetCompression = compression;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isSetCompression() {
		return isSetCompression;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getTypeString() {
		return VCardType.SOUND.getType();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj != null) {
			if (obj instanceof SoundType) {
				if (this == obj || ((SoundType) obj).hashCode() == this.hashCode()) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Util.generateHashCode(toString());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getName());
		sb.append("[ ");
		if (encodingType != null) {
			sb.append(encodingType.getType());
			sb.append(",");
		}

		if (soundBytes != null) {
			sb.append(StringUtil.toHexString(soundBytes));
			sb.append(",");
		}

		if (soundUri != null) {
			sb.append(soundUri.getPath());
			sb.append(",");
		}

		if (soundParameterType != null) {
			sb.append(soundParameterType.getType());
			sb.append(",");
		}

		if (super.id != null) {
			sb.append(super.id);
			sb.append(",");
		}

		sb.deleteCharAt(sb.length() - 1); //Remove last comma.
		sb.append(" ]");
		return sb.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SoundFeature clone() {
		SoundType cloned = new SoundType();

		if (soundBytes != null) {
			byte[] clonedBytes = Arrays.copyOf(soundBytes, soundBytes.length);
			cloned.setSound(clonedBytes);
		}

		if (soundUri != null) {
			try {
				cloned.setSoundURI(new URI(soundUri.getPath()));
			} catch (URISyntaxException e) {
				cloned.setSoundURI(null);
			}
		}

		if (soundParameterType != null) {
			cloned.setSoundParameterType(soundParameterType);
		}

		if (audioMediaType != null) {
			cloned.setAudioMediaType(audioMediaType);
		}

		cloned.setCompression(isSetCompression);
		cloned.setParameterTypeStyle(getParameterTypeStyle());
		cloned.setEncodingType(getEncodingType());
		cloned.setID(getID());
		return cloned;
	}
}
