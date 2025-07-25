package org.voovan.db;

/**
 * 事务类型枚举
 *
 * @author: helyho
 * DBase Framework.
 * WebSite: https://github.com/helyho/DBase
 * Licence: Apache v2 License
 */
public enum TranscationType {
	//嵌套事务
	NEST,
	//孤立事务
	ALONE,
	//无事务模式
	NONE
}
