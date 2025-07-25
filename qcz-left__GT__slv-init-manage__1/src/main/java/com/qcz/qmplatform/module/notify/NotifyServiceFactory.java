package com.qcz.qmplatform.module.notify;

import com.qcz.qmplatform.common.exception.CommonException;
import com.qcz.qmplatform.common.utils.SmsUtils;
import com.qcz.qmplatform.module.notify.bean.SmsConfig;
import com.qcz.qmplatform.module.notify.bean.SmsProvider;
import com.qcz.qmplatform.module.notify.service.INotifyService;

public class NotifyServiceFactory {

	public static INotifyService build(Class<? extends INotifyService> clazz, SmsConfig smsConfig) {
		INotifyService notifyService = null;
		try {
			int smsProvider = smsConfig.getSmsProvider();
			Class<? extends INotifyService> notifyServiceClass;
			if (clazz == null) {
				if (smsProvider <= 0) {
					throw new CommonException("必须指定一个短信提供商！");
				}
				notifyServiceClass = SmsUtils.getNotifyServiceClass(SmsProvider.valueOf(smsProvider));
			} else {
				notifyServiceClass = clazz;
			}
			notifyService = notifyServiceClass.newInstance();
			notifyService.setSmsConfig(smsConfig);
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return notifyService;
	}

	public static INotifyService build(SmsConfig smsConfig) {
		return build(null, smsConfig);
	}

}
