/**
 * Copyright (c) 2012, md_5. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * The name of the author may not be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
package net.md_5.specialsource.transformer;

import net.md_5.specialsource.JarRemapper;

/**
 * Load a mapping 'chained' through another mapping.
 */
public class ChainingTransformer extends MappingTransformer {

	private final JarRemapper jarRemapper;
	private final MethodDescriptor methodTransformer;

	public ChainingTransformer(JarRemapper jarRemapper) {
		this.jarRemapper = jarRemapper;
		this.methodTransformer = new MethodDescriptor(jarRemapper.jarMapping.packages, jarRemapper.jarMapping.classes);
	}

	@Override
	public String transformClassName(String className) {
		return jarRemapper.map(className);
	}

	@Override
	public String transformFieldName(String className, String fieldName) {
		return jarRemapper.mapFieldName(className, fieldName, null);
	}

	@Override
	public String transformMethodName(String className, String methodName, String methodDescriptor) {
		return jarRemapper.mapMethodName(className, methodName, methodDescriptor);
	}

	@Override
	public String transformMethodDescriptor(String oldDescriptor) {
		return methodTransformer.transform(oldDescriptor);
	}
}
