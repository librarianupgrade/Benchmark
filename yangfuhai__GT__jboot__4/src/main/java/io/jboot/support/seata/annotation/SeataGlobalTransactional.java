/**
 * Copyright (c) 2015-2021, Michael Yang 杨福海 (fuhai999@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jboot.support.seata.annotation;

import io.seata.tm.api.transaction.TransactionInfo;

import java.lang.annotation.*;

/**
 * The interface Seata Global transactional.
 * @author michael yang
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface SeataGlobalTransactional {

	/**
	 * Global transaction timeoutMills in MILLISECONDS.
	 *
	 * @return timeoutMills in MILLISECONDS.
	 */
	int timeoutMills() default TransactionInfo.DEFAULT_TIME_OUT;

	/**
	 * Given name of the global transaction instance.
	 *
	 * @return Given name.
	 */
	String name() default "";

	/**
	 * roll back for the Class
	 * @return
	 */
	Class<? extends Throwable>[] rollbackFor() default {};

	/**
	 *  roll back for the class name
	 * @return
	 */
	String[] rollbackForClassName() default {};

	/**
	 * not roll back for the Class
	 * @return
	 */
	Class<? extends Throwable>[] noRollbackFor() default {};

	/**
	 * not roll back for the class name
	 * @return
	 */
	String[] noRollbackForClassName() default {};

}
