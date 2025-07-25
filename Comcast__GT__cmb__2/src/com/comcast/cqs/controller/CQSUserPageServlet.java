/**
 * Copyright 2012 Comcast Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.comcast.cqs.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.DeleteQueueRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.comcast.cmb.common.controller.AdminServletBase;
import com.comcast.cmb.common.controller.CMBControllerServlet;
import com.comcast.cmb.common.persistence.PersistenceFactory;
import com.comcast.cmb.common.util.CMBProperties;
import com.comcast.cmb.common.util.PersistenceException;
import com.comcast.cmb.common.util.XmlUtil;
import com.comcast.cqs.util.Util;

/**
 * Admin page for cqs users
 * @author bwolf, tina, baosen, vvenkatraman
 *
 */
public class CQSUserPageServlet extends AdminServletBase {

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(CQSUserPageServlet.class);
	private String userId;

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		if (redirectUnauthenticatedUser(request, response)) {
			return;
		}

		CMBControllerServlet.valueAccumulator.initializeAllCounters();
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		Map<?, ?> parameters = request.getParameterMap();
		userId = request.getParameter("userId");
		String queueName = request.getParameter("queueName");
		String queueUrl = request.getParameter("qUrl");

		List<String> queueUrls = new ArrayList<String>();

		boolean showQueueAttributes = false;
		boolean showQueuesWithMessagesOnly = false;
		boolean useQueueNamePrefix = false;

		connect(userId);

		if (parameters.containsKey("Search")) {

			useQueueNamePrefix = true;

			if (request.getParameter("ShowMessagesOnly") != null) {
				showQueuesWithMessagesOnly = true;
			}

			if (request.getParameter("ShowAttributes") != null) {
				showQueueAttributes = true;
			}
		}

		try {

			/*ListQueuesRequest listQueuesRequest = new ListQueuesRequest();
			
			if (queueName != null && !queueName.equals("")) {
				listQueuesRequest.setQueueNamePrefix(queueName);
			}
			
			ListQueuesResult listQueuesResult = sqs.listQueues(listQueuesRequest);
			queueUrls = listQueuesResult.getQueueUrls();*/

			String url = cqsServiceBaseUrl + "?Action=ListQueues&AWSAccessKeyId=" + user.getAccessKey();

			if (useQueueNamePrefix && queueName != null && !queueName.equals("")) {
				url += "&QueueNamePrefix=" + queueName;
			}

			if (showQueuesWithMessagesOnly) {
				url += "&ContainingMessagesOnly=true";
			}

			String apiStateXml = httpGet(url);
			Element root = XmlUtil.buildDoc(apiStateXml);
			List<Element> queueUrlList = XmlUtil.getCurrentLevelChildNodes(root, "ListQueuesResult");

			for (Element urlElement : queueUrlList) {
				queueUrls.add(XmlUtil.getCurrentLevelChildNodes(urlElement, "QueueUrl").get(0).getTextContent().trim());
			}

		} catch (Exception ex) {
			logger.error("event=list_queues user_id= " + userId, ex);
			throw new ServletException(ex);
		}

		if (parameters.containsKey("Create")) {

			try {
				CreateQueueRequest createQueueRequest = new CreateQueueRequest(queueName);
				CreateQueueResult createQueueResult = sqs.createQueue(createQueueRequest);
				queueUrl = createQueueResult.getQueueUrl();
				queueUrls.add(queueUrl);
				logger.debug("event=create_queue queue_url=" + queueUrl + " user_id= " + userId);
			} catch (Exception ex) {
				logger.error("event=create_queue queue_url=" + queueUrl + " user_id= " + userId, ex);
				throw new ServletException(ex);
			}

		} else if (parameters.containsKey("Delete")) {

			try {
				DeleteQueueRequest deleteQueueRequest = new DeleteQueueRequest(queueUrl);
				sqs.deleteQueue(deleteQueueRequest);
				queueUrls.remove(queueUrl);
				logger.debug("event=delete_queue queue_url=" + queueUrl + " user_id= " + userId);
			} catch (Exception ex) {
				logger.error("event=delete_queue queue_url=" + queueUrl + " user_id= " + userId, ex);
				throw new ServletException(ex);
			}

		} else if (parameters.containsKey("DeleteAll")) {

			for (int i = 0; queueUrls != null && i < queueUrls.size(); i++) {

				try {
					DeleteQueueRequest deleteQueueRequest = new DeleteQueueRequest(queueUrls.get(i));
					sqs.deleteQueue(deleteQueueRequest);
					logger.debug("event=delete_queue queue_url=" + queueUrls.get(i) + " user_id= " + userId);
				} catch (Exception ex) {
					logger.error("event=delete_queue queue_url=" + queueUrls.get(i) + " user_id= " + userId, ex);
				}
			}

			queueUrls = new ArrayList<String>();
		}

		out.println("<html>");

		header(request, out, "Queues");

		out.println("<body>");

		out.println("<h2>Queues</h2>");

		long numQueues = 0;

		try {
			numQueues = PersistenceFactory.getUserPersistence().getNumUserQueues(userId);
		} catch (PersistenceException ex) {
			logger.warn("event=queue_count_failure", ex);
		}

		if (user != null) {
			out.println("<table><tr><td><b>User Name:</b></td><td>" + user.getUserName() + "</td></tr>");
			out.println("<tr><td><b>User ID:</b></td><td>" + user.getUserId() + "</td></tr>");
			out.println("<tr><td><b>Access Key:</b></td><td>" + user.getAccessKey() + "</td></tr>");
			out.println("<tr><td><b>Access Secret:</b></td><td>" + user.getAccessSecret() + "</td>");
			out.println("<tr><td><b>Queue Count</b></td><td>" + numQueues + "</td></tr></table>");
		}

		out.println("<p><table>");

		out.println("<tr><td>Search queues with name prefix:</td><td></td></tr>");
		out.println("<tr><form action=\"/webui/cqsuser?userId=" + user.getUserId() + "\" method=POST>");
		out.println("<td><input type='text' name='queueName' value='" + (useQueueNamePrefix ? queueName : "")
				+ "'/><input type='hidden' name='userId' value='" + userId + "'/>");
		out.println("<input type='checkbox' " + (showQueueAttributes ? "checked='true' " : "")
				+ "name='ShowAttributes' value='ShowAttributes'>Show Attributes</input>");
		out.println("<input type='checkbox' " + (showQueuesWithMessagesOnly ? "checked='true' " : "")
				+ " name='ShowMessagesOnly' value='ShowMessagesOnly'>Only Queues With Messages</input></td>");
		out.println("<td><input type='submit' value='Search' name='Search' /></td></form></tr>");

		out.println("<tr><td>Create queue with name:</td><td></td></tr>");
		out.println("<tr><form action=\"/webui/cqsuser?userId=" + user.getUserId() + "\" method=POST>");
		out.println("<td><input type='text' name='queueName' /><input type='hidden' name='userId' value='" + userId
				+ "'></td><td><input type='submit' value='Create' name='Create' /></td></form></tr>");

		out.println("<tr><td>Delete all queues:</td><td></td></tr>");
		out.println("<tr><form action=\"/webui/cqsuser?userId=" + user.getUserId() + "\" "
				+ "method=POST><td><input type='hidden' name='userId' value='" + userId + "'/>");
		out.println("<input type='hidden' name='queueName' value='" + (queueName != null ? queueName : "")
				+ "'/></td><td><input type='submit' value='DeleteAll' name='DeleteAll'/></td></form></tr>");

		out.println("</table></p>");

		out.println("<p><hr width='100%' align='left' /></p>");

		out.println("<p><span class='content'><table border='1'>");
		out.println("<tr><th>&nbsp;</th>");
		out.println("<th>Queue Url</th>");
		out.println("<th>Queue Arn</th>");
		out.println("<th>Queue Name</th>");
		out.println("<th>User Id</th>");
		out.println("<th>Region</th>");
		out.println("<th>Visibility TO</th>");
		out.println("<th>Max Msg Size</th>");
		out.println("<th>Msg Rention Period</th>");
		out.println("<th>Delay Seconds</th>");
		out.println("<th>Wait Time Seconds</th>");
		out.println("<th>Num Partitions</th>");
		out.println("<th>Approx Num Msg</th>");
		out.println("<th>&nbsp;</th><th>&nbsp;</th><th>&nbsp;</th><th>&nbsp;</th></tr>");

		for (int i = 0; queueUrls != null && i < queueUrls.size(); i++) {

			Map<String, String> attributes = new HashMap<String, String>();

			if (showQueueAttributes) {
				try {

					GetQueueAttributesRequest getQueueAttributesRequest = new GetQueueAttributesRequest(
							queueUrls.get(i));
					getQueueAttributesRequest.setAttributeNames(Arrays.asList("VisibilityTimeout", "MaximumMessageSize",
							"MessageRetentionPeriod", "DelaySeconds", "ApproximateNumberOfMessages",
							"ReceiveMessageWaitTimeSeconds", "NumberOfPartitions"));
					GetQueueAttributesResult getQueueAttributesResult = sqs
							.getQueueAttributes(getQueueAttributesRequest);
					attributes = getQueueAttributesResult.getAttributes();

				} catch (Exception ex) {
					logger.error("event=get_queue_attributes url=" + queueUrls.get(i));
				}
			}

			out.println("<form action=\"/webui/cqsuser?userId=" + user.getUserId() + "\" method=POST>");
			out.println("<tr>");
			out.println("<td>" + i + "</td>");
			out.println("<td>" + queueUrls.get(i) + "<input type='hidden' name='qUrl' value=" + queueUrls.get(i)
					+ "></td>");
			out.println(
					"<td>" + Util.getArnForAbsoluteQueueUrl(queueUrls.get(i)) + "<input type='hidden' name='arn' value="
							+ Util.getArnForAbsoluteQueueUrl(queueUrls.get(i)) + "></td>");
			out.println("<td>" + Util.getNameForAbsoluteQueueUrl(queueUrls.get(i)) + "</td>");
			out.println("<td>" + user.getUserId() + "<input type='hidden' name='userId' value=" + user.getUserId()
					+ "></td>");
			out.println("<td>" + CMBProperties.getInstance().getRegion() + "</td>");

			out.println(
					"<td>" + (attributes.get("VisibilityTimeout") != null ? attributes.get("VisibilityTimeout") : "")
							+ "</td>");
			out.println(
					"<td>" + (attributes.get("MaximumMessageSize") != null ? attributes.get("MaximumMessageSize") : "")
							+ "</td>");
			out.println("<td>"
					+ (attributes.get("MessageRetentionPeriod") != null ? attributes.get("MessageRetentionPeriod") : "")
					+ "</td>");
			out.println(
					"<td>" + (attributes.get("DelaySeconds") != null ? attributes.get("DelaySeconds") : "") + "</td>");
			out.println("<td>" + (attributes.get("ReceiveMessageWaitTimeSeconds") != null
					? attributes.get("ReceiveMessageWaitTimeSeconds")
					: "") + "</td>");
			out.println(
					"<td>" + (attributes.get("NumberOfPartitions") != null ? attributes.get("NumberOfPartitions") : "")
							+ "</td>");
			out.println("<td>" + (attributes.get("ApproximateNumberOfMessages") != null
					? attributes.get("ApproximateNumberOfMessages")
					: "") + "</td>");

			out.println("<td><a href='/webui/cqsuser/message?userId=" + user.getUserId() + "&queueName="
					+ Util.getNameForAbsoluteQueueUrl(queueUrls.get(i)) + "'>Messages</a></td>");
			out.println("<td><a href='/webui/cqsuser/permissions?userId=" + user.getUserId() + "&queueName="
					+ Util.getNameForAbsoluteQueueUrl(queueUrls.get(i)) + "'>Permissions</a></td>");
			out.println("<td><a href='' onclick=\"window.open('/webui/cqsuser/editqueueattributes?queueName="
					+ Util.getNameForAbsoluteQueueUrl(queueUrls.get(i)) + "&userId=" + userId
					+ "', 'EditQueueAttributes', 'height=630,width=580,toolbar=no')\">Attributes</a></td>");

			out.println("<td><input type='submit' value='Delete' name='Delete'/></td></tr></form>");
		}

		out.println("</table></span></p>");
		out.println("<h5 style='text-align:center;'><a href='/webui'>ADMIN HOME</a></h5>");
		out.println("</body></html>");

		CMBControllerServlet.valueAccumulator.deleteAllCounters();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		doGet(request, response);
	}
}
