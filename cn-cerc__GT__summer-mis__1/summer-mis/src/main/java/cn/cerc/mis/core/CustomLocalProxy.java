package cn.cerc.mis.core;

import org.springframework.context.ApplicationContext;

import cn.cerc.core.DataSet;
import cn.cerc.db.core.IHandle;

public abstract class CustomLocalProxy extends Handle {
	private String service;
	private String message;

	public CustomLocalProxy(IHandle handle) {
		super(handle);
	}

	protected boolean executeService(Object bean, DataSet dataIn, DataSet dataOut) {
		IStatus status;
		IService ss = (IService) bean;
		try {
			status = ss.execute(dataIn, dataOut);
		} catch (ServiceException e) {
			status = new ServiceStatus(false, e.getMessage());
		}
		this.setMessage(status.getMessage());
		return status.getResult();
	}

	protected Object getServiceObject() {
		if (getSession() == null) {
			this.setMessage("session is null.");
			return null;
		}
		if (getService() == null) {
			this.setMessage("service is null.");
			return null;
		}

		// 读取xml中的配置
		Object bean = null;
		ApplicationContext context = Application.getContext();
		if (context.containsBean(getService())) {
			bean = context.getBean(this.getService());
		} else {
			// 读取注解的配置，并自动将第一个字母改为小写
			String beanId = getService().split("\\.")[0];
			if (!beanId.substring(0, 2).toUpperCase().equals(beanId.substring(0, 2)))
				beanId = beanId.substring(0, 1).toLowerCase() + beanId.substring(1);
			if (context.containsBean(beanId)) {
				bean = context.getBean(beanId);
				// 支持指定函数
				if (bean instanceof IMultiplService) {
					IMultiplService cs = ((IMultiplService) bean);
					cs.setFuncCode(getService().split("\\.")[1]);
				}
			}
		}
		if (bean == null) {
			this.setMessage(String.format("bean %s not find", getService()));
			return null;
		}

		if (bean instanceof IHandle) {
			((IHandle) bean).setSession(this.getSession());
		}
		return bean;
	}

	public String getService() {
		return service;
	}

	public CustomLocalProxy setService(String service) {
		this.service = service;
		return this;
	}

	public String getMessage() {
		if (message != null) {
			return message.replaceAll("'", "\"");
		} else {
			return null;
		}
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
