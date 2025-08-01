package io.zeebe.monitor.rest;

public class MessageSubscriptionDto {

	private String messageName;
	private String correlationKey;

	private String activityId = "";
	private long activityInstanceKey;
	private long workflowInstanceKey;

	private String state;
	private String timestamp = "";

	private boolean isOpen;

	public String getMessageName() {
		return messageName;
	}

	public void setMessageName(String messageName) {
		this.messageName = messageName;
	}

	public String getCorrelationKey() {
		return correlationKey;
	}

	public void setCorrelationKey(String correlationKey) {
		this.correlationKey = correlationKey;
	}

	public String getActivityId() {
		return activityId;
	}

	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}

	public long getActivityInstanceKey() {
		return activityInstanceKey;
	}

	public void setActivityInstanceKey(long activityInstanceKey) {
		this.activityInstanceKey = activityInstanceKey;
	}

	public long getWorkflowInstanceKey() {
		return workflowInstanceKey;
	}

	public void setWorkflowInstanceKey(long workflowInstanceKey) {
		this.workflowInstanceKey = workflowInstanceKey;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public boolean isOpen() {
		return isOpen;
	}

	public void setOpen(boolean isOpen) {
		this.isOpen = isOpen;
	}
}
