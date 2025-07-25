package org.voovan.test.tools.aop;

import junit.framework.TestCase;
import org.voovan.tools.weave.aop.CutPointInfo;
import org.voovan.tools.json.JSON;

/**
 * 类文字命名
 *
 * @author: helyho
 * DBase Framework.
 * WebSite: https://github.com/helyho/DBase
 * Licence: Apache v2 License
 */
public class CutPointInfoUnit extends TestCase {
	public void test1() {
		CutPointInfo cutPointInfo = CutPointInfo.parse("* org.voovan.test.tools.aop.aopUnit@testMethod()");
		System.out.println(JSON.toJSON(cutPointInfo));

		cutPointInfo = CutPointInfo.parse("* org.voovan.test.tools.aop.aopUnit@testMethod(java.lang.String, int)");
		System.out.println(JSON.toJSON(cutPointInfo));
	}
}
