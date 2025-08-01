/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 * 
 *    https://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * 
 */

package org.apache.directory.api.asn1;

/**
 * <p>
 * Provides the highest level of abstraction for Encoders. This is the sister
 * interface of {@link Decoder}. Every
 * implementation of Encoder provides this common generic interface whic allows
 * a user to pass a generic Object to any Encoder implementation in the codec
 * package.
 * </p>
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface Encoder {
	/**
	 * Encodes an "Object" and returns the encoded content as an Object. The
	 * Objects here may just be <code>byte[]</code> or <code>String</code>s
	 * depending on the implementation used.
	 * 
	 * @param object An object to encode
	 * @return An "encoded" Object
	 * @throws EncoderException an encoder exception is thrown if the encoder experiences a
	 * failure condition during the encoding process.
	 */
	Object encode(Object object) throws EncoderException;
}
