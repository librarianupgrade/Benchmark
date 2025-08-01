// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.bard.webservice.web.endpoints;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;

import com.yahoo.bard.webservice.application.ObjectMappersSuite;
import com.yahoo.bard.webservice.async.ResponseException;
import com.yahoo.bard.webservice.async.broadcastchannels.BroadcastChannel;
import com.yahoo.bard.webservice.async.jobs.payloads.JobPayloadBuilder;
import com.yahoo.bard.webservice.async.jobs.stores.ApiJobStore;
import com.yahoo.bard.webservice.async.preresponses.stores.PreResponseStore;
import com.yahoo.bard.webservice.data.HttpResponseChannel;
import com.yahoo.bard.webservice.data.HttpResponseMaker;
import com.yahoo.bard.webservice.data.Result;
import com.yahoo.bard.webservice.data.ResultSet;
import com.yahoo.bard.webservice.data.dimension.DimensionDictionary;
import com.yahoo.bard.webservice.logging.RequestLog;
import com.yahoo.bard.webservice.logging.blocks.JobRequest;
import com.yahoo.bard.webservice.util.AllPagesPagination;
import com.yahoo.bard.webservice.util.Pagination;
import com.yahoo.bard.webservice.util.StreamUtils;
import com.yahoo.bard.webservice.util.Utils;
import com.yahoo.bard.webservice.web.ApiRequest;
import com.yahoo.bard.webservice.web.JobNotFoundException;
import com.yahoo.bard.webservice.web.JobsApiRequest;
import com.yahoo.bard.webservice.web.PreResponse;
import com.yahoo.bard.webservice.web.RequestMapper;
import com.yahoo.bard.webservice.web.RequestValidationException;
import com.yahoo.bard.webservice.web.ResponseFormatType;
import com.yahoo.bard.webservice.web.handlers.RequestHandlerUtils;
import com.yahoo.bard.webservice.web.responseprocessors.ResponseContext;
import com.yahoo.bard.webservice.web.responseprocessors.ResponseContextKeys;
import com.yahoo.bard.webservice.web.util.PaginationLink;
import com.yahoo.bard.webservice.web.util.PaginationParameters;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.exceptions.Exceptions;
import rx.observables.ConnectableObservable;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * Resource code for job resource endpoints.
 */
@Path("/jobs")
@Singleton
public class JobsServlet extends EndpointServlet {

	private static final Logger LOG = LoggerFactory.getLogger(JobsServlet.class);

	private final ApiJobStore apiJobStore;
	private final RequestMapper requestMapper;
	private final JobPayloadBuilder jobPayloadBuilder;
	private final PreResponseStore preResponseStore;
	private final BroadcastChannel<String> broadcastChannel;
	private final DimensionDictionary dimensionDictionary;
	private final ObjectWriter writer;

	/**
	 * Constructor.
	 *
	 * @param objectMappers  JSON tools
	 * @param apiJobStore  The ApiJobStore containing job metadata
	 * @param jobPayloadBuilder  The JobRowMapper to be used to map JobRow to the Job returned via the api
	 * @param preResponseStore  The Data store that stores all the PreResponses
	 * @param broadcastChannel  Channel to notify other Bard processes (i.e. long pollers)
	 * @param dimensionDictionary  The dimension dictionary from which to look up dimensions by name
	 * @param requestMapper  Mapper for changing the API request
	 */
	@Inject
	public JobsServlet(ObjectMappersSuite objectMappers, ApiJobStore apiJobStore, JobPayloadBuilder jobPayloadBuilder,
			PreResponseStore preResponseStore, BroadcastChannel<String> broadcastChannel,
			DimensionDictionary dimensionDictionary,
			@Named(JobsApiRequest.REQUEST_MAPPER_NAMESPACE) RequestMapper requestMapper) {
		super(objectMappers);
		this.requestMapper = requestMapper;
		this.apiJobStore = apiJobStore;
		this.jobPayloadBuilder = jobPayloadBuilder;
		this.preResponseStore = preResponseStore;
		this.broadcastChannel = broadcastChannel;
		this.dimensionDictionary = dimensionDictionary;
		this.writer = objectMappers.getMapper().writer();
	}

	/**
	 * Endpoint to get metadata of all the Jobs in the ApiJobStore.
	 *
	 * @param perPage  Requested number of rows of data to be displayed on each page of results
	 * @param page  Requested page of results desired
	 * @param format  Requested format
	 * @param filters  Filters to be applied on the JobRows. Expects a URL filter query String that may contain multiple
	 * filter strings separated by comma.  The format of a filter String is :
	 * (JobField name)-(operation)[(value or comma separated values)]?
	 * @param uriInfo  UriInfo of the request
	 * @param containerRequestContext  The context of data provided by the Jersey container for this request
	 * @param asyncResponse  An asyncAfter response that we can use to respond asynchronously
	 */
	@GET
	@Timed
	public void getJobs(@DefaultValue("") @NotNull @QueryParam("perPage") String perPage,
			@DefaultValue("") @NotNull @QueryParam("page") String page, @QueryParam("format") String format,
			@QueryParam("filters") String filters, @Context UriInfo uriInfo,
			@Context ContainerRequestContext containerRequestContext, @Suspended AsyncResponse asyncResponse) {
		try {
			RequestLog.startTiming(this);
			RequestLog.record(new JobRequest("all"));

			JobsApiRequest apiRequest = new JobsApiRequest(format, null, //asyncAfter is null so it behaves like a synchronous request
					perPage, page, filters, uriInfo, jobPayloadBuilder, apiJobStore);

			if (requestMapper != null) {
				apiRequest = (JobsApiRequest) requestMapper.apply(apiRequest, containerRequestContext);
			}

			// apiRequest is not final and cannot be used inside a lambda. Therefore we are assigning apiRequest to
			// jobsApiRequest.
			JobsApiRequest jobsApiRequest = apiRequest;

			Function<Collection<Map<String, String>>, AllPagesPagination<Map<String, String>>> paginationFactory = jobsApiRequest
					.getAllPagesPaginationFactory(
							jobsApiRequest.getPaginationParameters().orElse(jobsApiRequest.getDefaultPagination()));

			apiRequest.getJobViews().toList().map(jobs -> jobsApiRequest.getPage(paginationFactory.apply(jobs)))
					.map(result -> formatResponse(jobsApiRequest, result, "jobs", null))
					.defaultIfEmpty(getResponse("{}")).onErrorReturn(this::getErrorResponse).subscribe(response -> {
						RequestLog.stopTiming(this);
						asyncResponse.resume(response);
					});
		} catch (RequestValidationException e) {
			LOG.debug(e.getMessage(), e);
			RequestLog.stopTiming(this);
			asyncResponse.resume(RequestHandlerUtils.makeErrorResponse(e.getStatus(), e, writer));
		} catch (Error | Exception e) {
			String msg = String.format("Exception processing request: %s", e.getMessage());
			LOG.info(msg, e);
			RequestLog.stopTiming(this);
			asyncResponse.resume(Response.status(INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	/**
	 * Endpoint to get all the metadata about a particular job.
	 *
	 * @param ticket  The ticket that can uniquely identify a Job
	 * @param uriInfo  UriInfo of the request
	 * @param containerRequestContext  The context of data provided by the Jersey container for this request
	 * @param asyncResponse  An async response that we can use to respond asynchronously
	 */
	@GET
	@Timed
	@Path("/{ticket}")
	public void getJobByTicket(@PathParam("ticket") String ticket, @Context UriInfo uriInfo,
			@Context ContainerRequestContext containerRequestContext, @Suspended AsyncResponse asyncResponse) {
		try {
			RequestLog.startTiming(this);
			RequestLog.record(new JobRequest(ticket));
			JobsApiRequest apiRequest = new JobsApiRequest(ResponseFormatType.JSON.toString(), null, "", "", null, //filter string is null
					uriInfo, jobPayloadBuilder, apiJobStore);

			if (requestMapper != null) {
				apiRequest = (JobsApiRequest) requestMapper.apply(apiRequest, containerRequestContext);
			}

			handleJobResponse(ticket, apiRequest, asyncResponse);

		} catch (RequestValidationException e) {
			LOG.debug(e.getMessage(), e);
			RequestLog.stopTiming(this);
			asyncResponse.resume(RequestHandlerUtils.makeErrorResponse(e.getStatus(), e, writer));
		} catch (IOException | IllegalStateException e) {
			LOG.debug("Bad request exception : {}", e);
			RequestLog.stopTiming(this);
			asyncResponse.resume(RequestHandlerUtils.makeErrorResponse(BAD_REQUEST, e, writer));
		}
	}

	/**
	 * Endpoint to get a particular job's result.
	 *
	 * @param ticket  The ticket that can uniquely identify a Job
	 * @param format  Requested format of the response
	 * @param asyncAfter  How long the user is willing to wait for a synchronous request in milliseconds, if null
	 * defaults to the system config {@code default_asyncAfter}
	 * @param perPage  Requested number of rows of data to be displayed on each page of results
	 * @param page  Requested page of results desired
	 * @param uriInfo  UriInfo of the request
	 * @param containerRequestContext  The context of data provided by the Jersey container for this request
	 * @param asyncResponse  An async response that we can use to respond asynchronously
	 */
	@GET
	@Timed
	@Path("/{ticket}/results")
	public void getJobResultsByTicket(@PathParam("ticket") String ticket, @QueryParam("format") String format,
			@QueryParam("asyncAfter") String asyncAfter,
			@DefaultValue("") @NotNull @QueryParam("perPage") String perPage,
			@DefaultValue("") @NotNull @QueryParam("page") String page, @Context UriInfo uriInfo,
			@Context ContainerRequestContext containerRequestContext, @Suspended AsyncResponse asyncResponse) {
		try {
			RequestLog.startTiming(this);
			RequestLog.record(new JobRequest(ticket));

			JobsApiRequest apiRequest = new JobsApiRequest(format, asyncAfter, perPage, page, null, // filter string is null
					uriInfo, jobPayloadBuilder, apiJobStore);

			if (requestMapper != null) {
				apiRequest = (JobsApiRequest) requestMapper.apply(apiRequest, containerRequestContext);
			}

			// apiRequest is not final and cannot be used inside a lambda. Therefore we are assigning apiRequest to
			// jobsApiRequest.
			JobsApiRequest jobsApiRequest = apiRequest;

			Observable<PreResponse> preResponseObservable = getResults(ticket, apiRequest.getAsyncAfter());

			preResponseObservable.isEmpty().subscribe(isEmptyResult -> handlePreResponse(ticket, jobsApiRequest,
					asyncResponse, preResponseObservable, isEmptyResult));

		} catch (RequestValidationException e) {
			LOG.debug(e.getMessage(), e);
			RequestLog.stopTiming(this);
			asyncResponse.resume(RequestHandlerUtils.makeErrorResponse(e.getStatus(), e, writer));
		} catch (Error | Exception e) {
			LOG.debug("Exception processing request", e);
			RequestLog.stopTiming(this);
			asyncResponse.resume(Response.status(INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	/**
	 * If isEmpty is true, call the method to send the job payload to the user else call the method to send the job
	 * result to the user.
	 *
	 * @param ticket  The ticket that can uniquely identify a Job
	 * @param apiRequest  JobsApiRequest object with all the associated info in it
	 * @param asyncResponse  Parameter specifying for how long the request should be asyncAfter
	 * @param preResponseObservable  An Observable wrapping a PreResponse or an empty observable
	 * @param isEmpty  A boolean that indicates if the PreResponse is empty
	 */
	protected void handlePreResponse(String ticket, JobsApiRequest apiRequest, AsyncResponse asyncResponse,
			Observable<PreResponse> preResponseObservable, boolean isEmpty) {
		if (isEmpty) {
			//If we did not get the PreResponse before the sync timeout, send the job payload back to the user.
			handleJobResponse(ticket, apiRequest, asyncResponse);
		} else {
			//We got a PreResponse from the PreResponseStore. Send the query result back to the user.
			handleResultsResponse(preResponseObservable, asyncResponse, apiRequest);
		}
	}

	/**
	 * Get an Observable wrapping a PreResponse. We first connect to the BroadcastChannel to ensure that we do not
	 * miss any notifications. We then check the PreResponseStore for the PreResponse. If no PreResponse is available,
	 * we check to see if we got a notification from the BroadcastChannel before the async timeout. If we get a
	 * notification before timeout, we retrieve the PreResponse from the PreResponseStore else we return an empty
	 * Observable.
	 *
	 * @param ticket  The ticket for which the PreResponse needs to be retrieved.
	 * @param asyncAfter  The minimum duration the request is allowed to last before becoming asynchronous
	 *
	 * @return An Observable wrapping a PreResponse or an empty Observable in case a timeout occurs.
	 */
	protected Observable<PreResponse> getResults(@NotNull String ticket, long asyncAfter) {
		if (asyncAfter == JobsApiRequest.ASYNCHRONOUS_ASYNC_AFTER_VALUE) {
			// If the user specifies that they always want the asynchronous payload, then we need to force the system
			// to behave like the results are not ready in the store, and the asynchronous timeout has expired even
			// if the results are available.
			return Observable.empty();
		} else {
			/*
			 * BroadCastChannel is a hot observable i.e. it emits notification irrespective of whether it has any
			 * subscribers. We use the replay operator so that the preResponseObservable upon connection, will begin
			 * collecting values.
			 * Once a new observer subscribes to the observable, it will have all the collected values replayed to it.
			 */
			ConnectableObservable<String> broadcastChannelNotifications = broadcastChannel.getNotifications()
					.filter(ticket::equals).take(1).replay(1);
			broadcastChannelNotifications.connect();
			/*
			 * In the cases where we may get a synchronous response (asyncAfter is a number, or
			 * ApiRequest.SYNCHRONOUS_ASYNC_AFTER_VALUE ), then we start the timer, and
			 * go to the store and check to see if it has the results. If it doesn't, and 'asyncAfter' is a number
			 * then it starts listening to the broadcast channel, and waiting for the timer to expire.
			 *
			 * What this means is that in the case of `asyncAfter=0`, we have the following semantics:
			 * If the results are already in the response store, then return them to me. Otherwise, very quickly
			 * send back the asynchronous payload.
			 */
			return preResponseStore.get(ticket).switchIfEmpty(
					applyTimeoutIfNeeded(broadcastChannelNotifications, asyncAfter).flatMap(preResponseStore::get));
		}
	}

	/**
	 * Given an observable, returns a new observable with an asyncAfter timeout applied only if {@code asyncAfter} is
	 * not {@code never}.
	 * <p>
	 * If the timeout expires, the current observable is replaced with an empty observable.
	 *
	 * @param primary  The observable that should have a timeout attached to it, if the request's asyncAfter is
	 * a number
	 * @param asyncAfter  The minimum duration the request is allowed to last before becoming asynchronous
	 * @param <T>  The type of the observable's payload
	 *
	 * @return An Observable that may or may not have a timeout attached to it, depending on whether the request is
	 * forced to be synchronous or not
	 */
	private <T> Observable<T> applyTimeoutIfNeeded(Observable<T> primary, long asyncAfter) {
		return asyncAfter == JobsApiRequest.SYNCHRONOUS_ASYNC_AFTER_VALUE ? primary
				: primary.timeout(asyncAfter, TimeUnit.MILLISECONDS, Observable.empty());
	}

	/**
	 * Process a request to get job payload.
	 *
	 * @param ticket  The ticket that can uniquely identify a Job
	 * @param apiRequest  JobsApiRequest object with all the associated info in it
	 * @param asyncResponse  An async response that we can use to respond asynchronously
	 */
	protected void handleJobResponse(String ticket, JobsApiRequest apiRequest, AsyncResponse asyncResponse) {
		apiRequest.getJobViewObservable(ticket)
				//map the job to Json String
				.map(job -> {
					try {
						return objectMappers.getMapper().writeValueAsString(job);
					} catch (JsonProcessingException e) {
						LOG.error(e.getMessage(), e);
						throw Exceptions.propagate(e);
					}
				})
				//map the jsonResponse String to a Response
				.map(this::getResponse).onErrorReturn(this::getErrorResponse).subscribe(asyncResponse::resume);
	}

	/**
	 * Process a request to get job results.
	 *
	 * @param preResponseObservable  An Observable over the PreResponse which will be used to generate the Response
	 * @param asyncResponse  An async response that we can use to respond asynchronously
	 * @param apiRequest  JobsApiRequest object with all the associated info with it
	 */
	protected void handleResultsResponse(Observable<PreResponse> preResponseObservable, AsyncResponse asyncResponse,
			ApiRequest apiRequest) {
		HttpResponseMaker httpResponseMaker = new HttpResponseMaker(objectMappers, dimensionDictionary);

		preResponseObservable
				.flatMap(preResponse -> handlePreResponseWithError(preResponse, apiRequest.getUriInfo(),
						apiRequest.getPaginationParameters()))
				.subscribe(new HttpResponseChannel(asyncResponse, httpResponseMaker, apiRequest.getFormat(),
						apiRequest.getUriInfo()));
	}

	/**
	 * Check whether the PreResponse contains an error and if it does, return an Observable wrapping the error else
	 * return an Observable wrapping the PreResponse as is.
	 *
	 * @param preResponse  The PreResponse to be inspected
	 * @param uriInfo  uriInfo object to get uriBuilder
	 * @param paginationParameters  user's requested pagination parameters
	 *
	 * @return An Observable wrapping the PreResponse or an Observable wrapping a ResponseException
	 */
	protected Observable<PreResponse> handlePreResponseWithError(PreResponse preResponse, UriInfo uriInfo,
			Optional<PaginationParameters> paginationParameters) {
		ResponseContext responseContext = preResponse.getResponseContext();

		if (responseContext.containsKey(ResponseContextKeys.STATUS.getName())) {
			ResponseException responseException = new ResponseException(
					(Integer) responseContext.get(ResponseContextKeys.STATUS.getName()),
					(String) responseContext.get(ResponseContextKeys.ERROR_MESSAGE.getName()),
					(String) responseContext.get(ResponseContextKeys.ERROR_MESSAGE.getName()), null, // Druid query
					null, // cause
					writer);
			return Observable.error(responseException);
		}

		return paginationParameters.map(pageParams -> new AllPagesPagination<>(preResponse.getResultSet(), pageParams))
				.map(page -> new PreResponse(
						new ResultSet(page.getPageOfData(), preResponse.getResultSet().getSchema()),
						addPaginationInfoToResponseContext(responseContext, uriInfo, page)))
				.map(Observable::just).orElse(Observable.just(preResponse));
	}

	/**
	 * Add pagination details to ResponseContext.
	 *
	 * @param responseContext  ResponseContext object contains all the meta info of the resultSet
	 * @param uriInfo  uriInfo object to get uriBuilder
	 * @param pages  Paginated resultSet
	 *
	 * @return Updated ResponseContext contains pagination info
	 */
	protected ResponseContext addPaginationInfoToResponseContext(ResponseContext responseContext, UriInfo uriInfo,
			Pagination<Result> pages) {
		LinkedHashMap<String, URI> bodyLinks = Arrays.stream(PaginationLink.values())
				.map(link -> new ImmutablePair<>(link.getBodyName(), link.getPage(pages)))
				.filter(pair -> pair.getRight().isPresent())
				.map(pair -> Utils.withRight(pair, pair.getRight().getAsInt()))
				.map(pair -> Utils.withRight(pair,
						uriInfo.getRequestUriBuilder().replaceQueryParam("page", pair.getRight())))
				.map(pair -> Utils.withRight(pair, pair.getRight().build()))
				.collect(StreamUtils.toLinkedMap(Pair::getLeft, Pair::getRight));
		responseContext.put(ResponseContextKeys.PAGINATION_LINKS_CONTEXT_KEY.getName(), bodyLinks);
		responseContext.put(ResponseContextKeys.PAGINATION_CONTEXT_KEY.getName(), pages);
		return responseContext;
	}

	/**
	 * Map the given jsonString to a Response object.
	 *
	 * @param jsonResponse  The jsonResponse to be mapped to a Response object
	 *
	 * @return The Response object
	 */
	protected Response getResponse(String jsonResponse) {
		LOG.trace("Jobs endpoint Response: {}", jsonResponse);
		RequestLog.stopTiming(this);
		return Response.status(OK).entity(jsonResponse).build();
	}

	/**
	 * Map the exception thrown while processing the job request to an appropriate http response.
	 *
	 * @param throwable  The exception thrown while processing the request
	 *
	 * @return The http Response to be sent to the user
	 */
	protected Response getErrorResponse(Throwable throwable) {
		//In case the given ticket does not exist in the ApiJobStore
		if (throwable instanceof JobNotFoundException) {
			LOG.debug(throwable.getMessage());
			RequestLog.stopTiming(this);
			return Response.status(NOT_FOUND).entity(throwable.getMessage()).build();
		}

		LOG.error(throwable.getMessage());
		RequestLog.stopTiming(this);
		//In case the job cannot be retrieved from the ApiJobStore or if it cannot be mapped to a Job
		return Response.status(INTERNAL_SERVER_ERROR).entity(throwable.getMessage()).build();
	}
}
