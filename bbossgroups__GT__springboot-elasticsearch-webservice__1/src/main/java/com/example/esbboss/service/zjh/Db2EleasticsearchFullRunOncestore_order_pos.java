package com.example.esbboss.service.zjh;

/**
 * Copyright 2008 biaoping.yin
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

import com.frameworkset.util.SimpleStringUtil;
import org.frameworkset.spi.geoip.IpInfo;
import org.frameworkset.tran.DataRefactor;
import org.frameworkset.tran.DataStream;
import org.frameworkset.tran.ExportResultHandler;
import org.frameworkset.tran.config.ImportBuilder;
import org.frameworkset.tran.context.Context;
import org.frameworkset.tran.metrics.TaskMetrics;
import org.frameworkset.tran.plugin.db.input.DBInputConfig;
import org.frameworkset.tran.plugin.es.output.ElasticsearchOutputConfig;
import org.frameworkset.tran.task.TaskCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * <p>Description: 基于数字类型db-es增量同步案例，同步处理程序，如需调试同步功能，直接运行main方法即可
 * <p></p>
 * <p>Copyright (c) 2018</p>
 *
 * @author biaoping.yin
 * @version 1.0
 * @Date 2018/9/27 20:38
 */
public class Db2EleasticsearchFullRunOncestore_order_pos {
	private static Logger logger = LoggerFactory.getLogger(Db2EleasticsearchFullRunOncestore_order_pos.class);

	public static void main(String args[]) {
		Db2EleasticsearchFullRunOncestore_order_pos db2EleasticsearchDemo = new Db2EleasticsearchFullRunOncestore_order_pos();
		db2EleasticsearchDemo.importDataRunOnce(false);
	}

	/**
	 * elasticsearch地址和数据库地址都从外部配置文件application.properties中获取，加载数据源配置和es配置
	 * 从配置文件application.properties中获取参数值方法
	 * boolean dropIndice = PropertiesUtil.getPropertiesContainer().getBooleanSystemEnvProperty("dropIndice",true);
	 * int threadCount = PropertiesUtil.getPropertiesContainer().getIntSystemEnvProperty("log.threadCount",2);
	 */
	public void importDataRunOnce(boolean dropIndice) {

		ImportBuilder importBuilder = new ImportBuilder();
		//在任务数据抽取之前做一些初始化处理，例如：通过删表来做初始化操作

		String jdbcUrl = "jdbc:mysql://192.168.88.10:3306/middle_platform?rewriteBatchedStatements=true&useServerPrepStmts=false&useCompression=true&useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull&allowMultiQueries=true&serverTimezone=Asia/Shanghai";

		DBInputConfig dbInputConfig = new DBInputConfig();
		//指定导入数据的sql语句，必填项，可以设置自己的提取逻辑，
		// 设置增量变量log_id，增量变量名称#[log_id]可以多次出现在sql语句的不同位置中，例如：
		// select * from td_sm_log where log_id > #[log_id] and parent_id = #[log_id]
		// 需要设置setLastValueColumn信息log_id，
		// 通过setLastValueType方法告诉工具增量字段的类型，默认是数字类型

		//		importBuilder.setSql("select * from td_sm_log where LOG_OPERTIME > #[LOG_OPERTIME]");
		dbInputConfig.setSql("select * from store_order_pos where create_time").setDbName("middle_platform");
		importBuilder.setInputConfig(dbInputConfig);

		//		importBuilder.addFieldMapping("LOG_CONTENT","message");
		//		importBuilder.addIgnoreFieldMapping("remark1");
		//		importBuilder.setSql("select * from td_sm_log ");
		ElasticsearchOutputConfig elasticsearchOutputConfig = new ElasticsearchOutputConfig();
		elasticsearchOutputConfig.setTargetElasticsearch("hemiao_es").setIndex("es_store_order_pos")
				.setEsIdField("order_offline_id")//设置文档主键，不设置，则自动产生文档id
				.setDebugResponse(false)//设置是否将每次处理的reponse打印到日志文件中，默认false
				.setDiscardBulkResponse(false);//设置是否需要批量处理的响应报文，不需要设置为false，true为需要，默认false
		/**
		 elasticsearchOutputConfig.setEsIdGenerator(new EsIdGenerator() {
		 //如果指定EsIdGenerator，则根据下面的方法生成文档id，
		 // 否则根据setEsIdField方法设置的字段值作为文档id，
		 // 如果默认没有配置EsIdField和如果指定EsIdGenerator，则由es自动生成文档id
		
		 @Override public Object genId(Context context) throws Exception {
		 return SimpleStringUtil.getUUID();//返回null，则由es自动生成文档id
		 }
		 });
		 */
		//				.setIndexType("dbdemo") ;//es 7以后的版本不需要设置indexType，es7以前的版本必需设置indexType;
		//				.setRefreshOption("refresh")//可选项，null表示不实时刷新，importBuilder.setRefreshOption("refresh");表示实时刷新
		/**
		 * es相关配置
		 */
		//		elasticsearchOutputConfig.setTargetElasticsearch("default,test");//同步数据到两个es集群

		importBuilder.setOutputConfig(elasticsearchOutputConfig);

		/**
		 * 设置IP地址信息库
		 */
		importBuilder.setGeoipDatabase("/opt/ip_data/GeoLite2-City.mmdb");
		importBuilder.setGeoipAsnDatabase("/opt/ip_data/GeoLite2-ASN.mmdb");
		importBuilder.setGeoip2regionDatabase("/opt/ip_data/ip2region.db");

		importBuilder
				//
				.setUseJavaName(true) //可选项,将数据库字段名称转换为java驼峰规范的名称，true转换，false不转换，默认false，例如:doc_id -> docId
				.setPrintTaskLog(true) //可选项，true 打印任务执行日志（耗时，处理记录数） false 不打印，默认值false
				.setBatchSize(10); //可选项,批量导入es的记录数，默认为-1，逐条处理，> 0时批量处理

		//映射和转换配置开始
		//		/**
		//		 * db-es mapping 表字段名称到es 文档字段的映射：比如document_id -> docId
		//		 * 可以配置mapping，也可以不配置，默认基于java 驼峰规则进行db field-es field的映射和转换
		//		 */
		//		importBuilder.addFieldMapping("document_id","docId")
		//				.addFieldMapping("docwtime","docwTime")
		//				.addIgnoreFieldMapping("channel_id");//添加忽略字段
		//
		//
		//		/**
		//		 * 为每条记录添加额外的字段和值
		//		 * 可以为基本数据类型，也可以是复杂的对象
		//		 */
		//		importBuilder.addFieldValue("testF1","f1value");
		//		importBuilder.addFieldValue("testInt",0);
		//		importBuilder.addFieldValue("testDate",new Date());
		//		importBuilder.addFieldValue("testFormateDate","yyyy-MM-dd HH",new Date());
		//		TestObject testObject = new TestObject();
		//		testObject.setId("testid");
		//		testObject.setName("jackson");
		//		importBuilder.addFieldValue("testObject",testObject);
		//
		/**
		 * 重新设置es数据结构
		 */
		importBuilder.setDataRefactor(new DataRefactor() {
			public void refactor(Context context) throws Exception {
				//				Date date = context.getDateValue("LOG_OPERTIME");
				context.addFieldValue("collecttime", new Date());
				IpInfo ipInfo = context.getIpInfoByIp("219.133.80.136");
				if (ipInfo != null)
					context.addFieldValue("ipInfo", SimpleStringUtil.object2json(ipInfo));
			}
		});
		//映射和转换配置结束

		/**
		 * 内置线程池配置，实现多线程并行数据导入功能，作业完成退出时自动关闭该线程池
		 */
		importBuilder.setParallel(true);//设置为多线程并行批量导入,false串行
		importBuilder.setQueue(10);//设置批量导入线程池等待队列长度
		importBuilder.setThreadCount(50);//设置批量导入线程池工作线程数量
		importBuilder.setContinueOnError(true);//任务出现异常，是否继续执行作业：true（默认值）继续执行 false 中断作业执行

		importBuilder.setExportResultHandler(new ExportResultHandler<String, String>() {
			@Override
			public void success(TaskCommand<String, String> taskCommand, String result) {
				TaskMetrics taskMetrics = taskCommand.getTaskMetrics();
				logger.info(taskMetrics.toString());
				logger.debug(result);
			}

			@Override
			public void error(TaskCommand<String, String> taskCommand, String result) {
				TaskMetrics taskMetrics = taskCommand.getTaskMetrics();
				logger.info(taskMetrics.toString());
				logger.debug(result);
			}

			@Override
			public void exception(TaskCommand<String, String> taskCommand, Throwable exception) {
				TaskMetrics taskMetrics = taskCommand.getTaskMetrics();
				logger.debug(taskMetrics.toString());
			}

		});

		/**
		 * 执行数据库表数据导入es操作
		 */
		DataStream dataStream = importBuilder.builder();
		dataStream.execute();//执行导入操作
		//		dataStream.destroy();//释放资源

	}

}
