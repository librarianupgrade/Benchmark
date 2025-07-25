package org.weiboad.ragnar.server.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.weiboad.ragnar.server.struct.MetaLog;
import org.weiboad.ragnar.server.statistics.api.APIStatisticTimeSet;
import org.weiboad.ragnar.server.search.IndexService;

import java.util.concurrent.LinkedBlockingQueue;

@Component
@Scope("singleton")
public class MetaLogProcessor {

	private int maxQueueLength = 20000;

	private LinkedBlockingQueue<MetaLog> metaLogQueue = new LinkedBlockingQueue<>();

	//log obj
	private Logger log = LoggerFactory.getLogger(BizLogProcessor.class);

	@Autowired
	APIStatisticTimeSet apiStatisticTimeSet;

	@Autowired
	IndexService indexHelper;

	public Integer getQueueLen() {
		return metaLogQueue.size();
	}

	//main process struct
	public void insertDataQueue(MetaLog data) {
		if (data != null) {
			try {
				this.metaLogQueue.put(data);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public boolean checkAvalible() {
		if (this.metaLogQueue.size() >= this.maxQueueLength / 2) {
			return false;
		}
		return true;
	}

	@Scheduled(fixedRate = 500)
	public void processData() {
		if (metaLogQueue == null) {
			return;
		}
		int totalProcess = 0;
		MetaLog metainfo = metaLogQueue.poll();
		while (metainfo != null) {

			apiStatisticTimeSet.analyzeMetaLog(metainfo);

			totalProcess++;

			if (totalProcess > this.maxQueueLength) {
				break;
			}

			metainfo = metaLogQueue.poll();
		}
	}
}
