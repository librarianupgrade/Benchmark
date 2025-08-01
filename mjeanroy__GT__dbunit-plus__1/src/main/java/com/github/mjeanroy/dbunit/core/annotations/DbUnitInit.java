/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2021 Mickael Jeanroy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.mjeanroy.dbunit.core.annotations;

import com.github.mjeanroy.dbunit.core.sql.SqlScriptParserConfiguration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * DbUnit initialization: allow user to run SQL scripts before
 * loading DbUnit data set.
 *
 * This annotation can be used on:
 * <ul>
 *   <li>Class (i.e test class).</li>
 *   <li>Package (i.e package where test classes belongs)</li>
 * </ul>
 *
 * For example:
 *
 * <pre><code>
 *
 *   &#64;DbUnitInit(sql = "/sql/schema.sql")
 *   &#64;DbUnitDataSet("/dataset/xml")
 *   public class TestClass {
 *     &#64;Rule
 *     public DbUnitRule rule = new DbUnitRule(connectionFactory);
 *
 *     &#64;Test
 *     public void test1() {
 *     }
 *   }
 *
 * </code></pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Target({ ElementType.TYPE, })
public @interface DbUnitInit {

	/**
	 * Set of SQL scripts to load
	 *
	 * @return SQL scripts file to load.
	 */
	String[] sql() default {};

	/**
	 * Delimiter for these set of SQL scripts.
	 *
	 * @return Delimiter.
	 */
	char delimiter() default SqlScriptParserConfiguration.DEFAULT_DELIMITER;
}
