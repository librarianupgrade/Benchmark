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
package com.comcast.cns.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.amazonaws.services.sns.model.GetSubscriptionAttributesRequest;
import com.amazonaws.services.sns.model.GetSubscriptionAttributesResult;
import com.amazonaws.services.sns.model.SetSubscriptionAttributesRequest;
import com.comcast.cmb.common.controller.AdminServletBase;
import com.comcast.cmb.common.controller.CMBControllerServlet;
import com.comcast.cns.model.CNSRetryPolicy;
import com.comcast.cns.model.CNSSubscriptionDeliveryPolicy;
import com.comcast.cns.model.CNSThrottlePolicy;
import com.comcast.cns.model.CNSRetryPolicy.CnsBackoffFunction;

/**
 * Admin page for editing subscription delivery policy
 * @author tina, aseem, bwolf
 *
 */
public class CNSEditSubscriptionDeliveryPolicyPage extends AdminServletBase {

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(CNSEditSubscriptionDeliveryPolicyPage.class);

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		if (redirectUnauthenticatedUser(request, response)) {
			return;
		}

		CMBControllerServlet.valueAccumulator.initializeAllCounters();
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		String subArn = request.getParameter("subscriptionArn");
		String userId = request.getParameter("userId");
		Map<?, ?> params = request.getParameterMap();

		connect(request);

		out.println("<html>");

		simpleHeader(request, out, "View/Edit Subscription Delivery Policy");

		if (params.containsKey("Update")) {

			String numRetries = request.getParameter("numRetries");
			String retriesNoDelay = request.getParameter("retriesNoDelay");
			String minDelay = request.getParameter("minDelay");
			String minDelayRetries = request.getParameter("minDelayRetries");
			String maxDelay = request.getParameter("maxDelay");
			String maxDelayRetries = request.getParameter("maxDelayRetries");
			String maxReceiveRate = request.getParameter("maxReceiveRate");
			String backoffFunc = request.getParameter("backoffFunc");

			CNSSubscriptionDeliveryPolicy effectiveDeliveryPolicy = new CNSSubscriptionDeliveryPolicy();
			CNSRetryPolicy defaultHealthyRetryPolicy = new CNSRetryPolicy();

			if (maxDelay.trim().length() > 0) {
				defaultHealthyRetryPolicy.setMaxDelayTarget(Integer.parseInt(maxDelay));
			}

			if (minDelay.trim().length() > 0) {
				defaultHealthyRetryPolicy.setMinDelayTarget(Integer.parseInt(minDelay));
			}

			if (maxDelayRetries.trim().length() > 0) {
				defaultHealthyRetryPolicy.setNumMaxDelayRetries(Integer.parseInt(maxDelayRetries));
			}

			if (minDelayRetries.trim().length() > 0) {
				defaultHealthyRetryPolicy.setNumMinDelayRetries(Integer.parseInt(minDelayRetries));
			}

			if (retriesNoDelay.trim().length() > 0) {
				defaultHealthyRetryPolicy.setNumNoDelayRetries(Integer.parseInt(retriesNoDelay));
			}

			if (numRetries.trim().length() > 0) {
				defaultHealthyRetryPolicy.setNumRetries(Integer.parseInt(numRetries));
			}

			defaultHealthyRetryPolicy.setBackOffFunction(CnsBackoffFunction.valueOf(backoffFunc));
			effectiveDeliveryPolicy.setHealthyRetryPolicy(defaultHealthyRetryPolicy);
			CNSThrottlePolicy defaultThrottlePolicy = new CNSThrottlePolicy();

			if (maxReceiveRate.trim().length() > 0) {
				defaultThrottlePolicy.setMaxReceivesPerSecond(Integer.parseInt(maxReceiveRate));
			}

			effectiveDeliveryPolicy.setThrottlePolicy(defaultThrottlePolicy);

			try {

				SetSubscriptionAttributesRequest setSubscriptionAttributesRequest = new SetSubscriptionAttributesRequest(
						subArn, "DeliveryPolicy", effectiveDeliveryPolicy.toString());
				sns.setSubscriptionAttributes(setSubscriptionAttributesRequest);

				logger.debug("event=set_delivery_policy sub_arn=" + subArn + " user_id= " + userId);

			} catch (Exception ex) {
				logger.error("event=set_subscription_attribute sub_arn=" + subArn + " user_id= " + userId, ex);
				throw new ServletException(ex);
			}

			out.println("<body onload='javascript:window.opener.location.reload();window.close();'>");

		} else {

			int numRetries = 0, retriesNoDelay = 0, minDelay = 0, minDelayRetries = 0, maxDelay = 0,
					maxDelayRetries = 0, maxReceiveRate = 0;
			String retryBackoff = "linear";

			if (subArn != null) {

				Map<String, String> attributes = null;
				CNSSubscriptionDeliveryPolicy deliveryPolicy = null;

				try {
					GetSubscriptionAttributesRequest getSubscriptionAttributesRequest = new GetSubscriptionAttributesRequest(
							subArn);
					GetSubscriptionAttributesResult getSubscriptionAttributesResult = sns
							.getSubscriptionAttributes(getSubscriptionAttributesRequest);
					attributes = getSubscriptionAttributesResult.getAttributes();
					deliveryPolicy = new CNSSubscriptionDeliveryPolicy(
							new JSONObject(attributes.get("DeliveryPolicy")));
				} catch (Exception ex) {
					logger.error("event=get_subscription_attributes sub_arn=" + subArn + " user_id= " + userId, ex);
					throw new ServletException(ex);
				}

				if (deliveryPolicy != null) {

					CNSRetryPolicy healthyRetryPolicy = deliveryPolicy.getHealthyRetryPolicy();

					if (healthyRetryPolicy != null) {
						numRetries = healthyRetryPolicy.getNumRetries();
						retriesNoDelay = healthyRetryPolicy.getNumNoDelayRetries();
						minDelay = healthyRetryPolicy.getMinDelayTarget();
						minDelayRetries = healthyRetryPolicy.getNumMinDelayRetries();
						maxDelay = healthyRetryPolicy.getMaxDelayTarget();
						maxDelayRetries = healthyRetryPolicy.getNumMaxDelayRetries();
						retryBackoff = healthyRetryPolicy.getBackOffFunction().toString();
					}

					CNSThrottlePolicy throttlePolicy = deliveryPolicy.getThrottlePolicy();

					if (throttlePolicy != null) {
						if (throttlePolicy.getMaxReceivesPerSecond() != null) {
							maxReceiveRate = throttlePolicy.getMaxReceivesPerSecond().intValue();
						}
					}
				}
			}

			out.println("<body>");
			out.println("<h1>View/Edit Subscripton Delivery Policy</h1>");
			out.println("<form action=\"/webui/cnsuser/subscription/editdeliverypolicy?subscriptionArn=" + subArn
					+ "\" method=POST>");
			out.println("<input type='hidden' name='userId' value='" + userId + "'>");
			out.println("<table>");
			out.println("<tr><td colspan=2><b><font color='orange'>Delivery Policy</font></b></td></tr>");
			out.println("<tr><td colspan=2><b>Apply these delivery policies for the subscripton:</b></td></tr>");
			out.println("<tr><td>Number of retries:</td><td><input type='text' name='numRetries' size='50' value='"
					+ numRetries + "'></td></tr>");
			out.println("<tr><td>&nbsp;</td><td><I><font color='grey'>Between 0 - 100</font></I></td></tr>");
			out.println(
					"<tr><td>Retries with no delay:</td><td><input type='text' name='retriesNoDelay' size='50' value='"
							+ retriesNoDelay + "'></td></tr>");
			out.println(
					"<tr><td>&nbsp;</td><td><I><font color='grey'>Between (0 - number of retries)</font></I></td></tr>");
			out.println("<tr><td>Minimum delay:</td><td><input type='text' name='minDelay' size='50' value='" + minDelay
					+ "'></td></tr>");
			out.println(
					"<tr><td>&nbsp;</td><td><I><font color='grey'>In seconds.Between 0 - maximum delay</font></I></td></tr>");
			out.println(
					"<tr><td>Minimum delay retries:</td><td><input type='text' name='minDelayRetries' size='50' value='"
							+ minDelayRetries + "'></td></tr>");
			out.println(
					"<tr><td>&nbsp;</td><td><I><font color='grey'>Between (0 - number of retries)</font></I></td></tr>");
			out.println("<tr><td>Maximum delay:</td><td><input type='text' name='maxDelay' size='50' value='" + maxDelay
					+ "'></td></tr>");
			out.println(
					"<tr><td>&nbsp;</td><td><I><font color='grey'>In seconds. Between minimum delay - 3600</font></I></td></tr>");
			out.println(
					"<tr><td>Maximum delay retries:</td><td><input type='text' name='maxDelayRetries' size='50' value='"
							+ maxDelayRetries + "'></td></tr>");
			out.println(
					"<tr><td>&nbsp;</td><td><I><font color='grey'>Between (0 - number of retries)</font></I></td></tr>");
			out.println(
					"<tr><td>Maximum receive rate:</td><td><input type='text' name='maxReceiveRate' size='50' value='"
							+ maxReceiveRate + "'></td></tr>");
			out.println("<tr><td>&nbsp;</td><td><I><font color='grey'>Receives per second. >= 1</font></I></td></tr>");
			out.println("<tr><td>&nbsp;</td><td>&nbsp;</td></tr>");

			if (retryBackoff.equals("linear")) {
				out.println(
						"<tr><td>Retry backoff function:</td><td><select name='backoffFunc'><option value='linear' selected>Linear</option><option value='arithmetic'>Arithmetic</option><option value='geometric'>Geometric</option><option value='exponential'>Exponential</option></select></td></tr>");
			} else if (retryBackoff.equals("arithmetic")) {
				out.println(
						"<tr><td>Retry backoff function:</td><td><select name='backoffFunc'><option value='linear'>Linear</option><option value='arithmetic' selected>Arithmetic</option><option value='geometric'>Geometric</option><option value='exponential'>Exponential</option></select></td></tr>");
			} else if (retryBackoff.equals("geometric")) {
				out.println(
						"<tr><td>Retry backoff function:</td><td><select name='backoffFunc'><option value='linear'>Linear</option><option value='arithmetic'>Arithmetic</option><option value='geometric' selected>Geometric</option><option value='exponential'>Exponential</option></select></td></tr>");
			} else if (retryBackoff.equals("exponential")) {
				out.println(
						"<tr><td>Retry backoff function:</td><td><select name='backoffFunc'><option value='linear'>Linear</option><option value='arithmetic'>Arithmetic</option><option value='geometric'>Geometric</option><option value='exponential' selected>Exponential</option></select></td></tr>");
			}

			out.println("<tr><td colspan=2><hr/></td></tr>");
			out.println(
					"<tr><td colspan=2 align=right><input type='button' onclick='window.close()' value='Cancel'><input type='submit' name='Update' value='Update'></td></tr></table></form>");
		}

		out.println("</body></html>");

		CMBControllerServlet.valueAccumulator.deleteAllCounters();
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
