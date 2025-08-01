/* *********************************************************************** *
 * project: org.matsim.*
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2016 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package org.matsim.pt2matsim.editor;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.population.routes.RouteUtils;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.utils.collections.CollectionUtils;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.pt.transitSchedule.api.*;
import org.matsim.pt2matsim.mapping.networkRouter.ScheduleRouters;
import org.matsim.pt2matsim.tools.NetworkTools;
import org.matsim.pt2matsim.tools.PTMapperTools;
import org.matsim.pt2matsim.tools.ScheduleTools;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of a schedule editor. Provides methods for
 * rerouting and adapting schedules via a csv "command file".
 *
 * @author polettif
 */
public class BasicScheduleEditor implements ScheduleEditor {

	protected static Logger log = LogManager.getLogger(RunScheduleEditor.class);
	// fields
	private final Network network;
	private final TransitSchedule schedule;
	private final TransitScheduleFactory scheduleFactory;
	private final NetworkFactory networkFactory;
	private final ScheduleRouters routers;
	private final ParentStops parentStops;

	public BasicScheduleEditor(TransitSchedule schedule, Network network, ScheduleRouters routers) {
		this.schedule = schedule;
		this.network = network;
		this.scheduleFactory = schedule.getFactory();
		this.networkFactory = network.getFactory();
		this.routers = routers;
		this.parentStops = new ParentStops();
	}

	public BasicScheduleEditor(TransitSchedule schedule, Network network) {
		this.schedule = schedule;
		this.network = network;
		this.scheduleFactory = schedule.getFactory();
		this.networkFactory = network.getFactory();
		this.parentStops = new ParentStops();

		log.info("Guessing routers based on schedule transport modes and used network transport modes.");
		this.routers = NetworkTools.guessRouters(schedule, network).createInstance();
	}

	public Network getNetwork() {
		return network;
	}

	public TransitSchedule getSchedule() {
		return schedule;
	}

	/**
	 * Parses a command file (csv) and runs the commands specified
	 */
	@Override
	public void parseCommandCsv(String filePath) throws IOException {
		try (CSVReader reader = new CSVReaderBuilder(new FileReader(filePath))
				.withCSVParser(new CSVParserBuilder().withSeparator(';').build()).build()) {
			String[] line = reader.readNext();
			while (line != null) {
				log.info(CollectionUtils.arrayToString(line));
				executeCmdLine(line);
				line = reader.readNext();
			}
		} catch (CsvValidationException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * executes a command line
	 */
	@Override
	public void executeCmdLine(String[] cmd) {
		/*
		  Reroute TransitRoute via new Link
		  ["rerouteViaLink"] [TransitLineId] [TransitRouteId] [oldLinkId] [newLinkId]
		 */
		if (RR_VIA_LINK.equals(cmd[0])) {
			if (cmd.length == 5) {
				rerouteViaLink(getTransitLine(cmd[1]), getTransitRoute(cmd[1], cmd[2]), Id.createLinkId(cmd[3]),
						Id.createLinkId(cmd[4]));
			} else {
				throw new IllegalArgumentException(
						"Incorrect number of arguments for " + cmd[0] + "! 5 needed, " + cmd.length + " given");
			}
		}

		/*
		  Reroute TransitRoute from a given stop facility
		  ["rerouteFromStop"] [TransitLineId] [TransitRouteId] [fromStopId] [newLinkId]
		 */
		else if (RR_FROM_STOP.equals(cmd[0])) {
			if (cmd.length == 5) {
				rerouteFromStop(getTransitLine(cmd[1]), getTransitRoute(cmd[1], cmd[2]), cmd[3], cmd[4]);
			} else {
				throw new IllegalArgumentException(
						"Incorrect number of arguments for " + cmd[0] + "! 5 needed, " + cmd.length + " given");
			}
		}
		/*
		  Change the referenced link of a stopfacility. Effectively creates a new child stop facility.
		  ["changeRefLink"] [StopFacilityId] [newlinkId]
		  ["changeRefLink"] [TransitLineId] [TransitRouteId] [ParentId] [newlinkId]
		  ["changeRefLink"] ["allTransitRoutesOnLink"] [linkId] [ParentId] [newlinkId]
		 */
		else if (CHANGE_REF_LINK.equals(cmd[0])) {
			if (cmd.length == 3 || "".equals(cmd[3])) {
				changeRefLink(cmd[1], cmd[2]);
			} else if (cmd.length == 5) {
				switch (cmd[1]) {
				case ALL_TRANSIT_ROUTES_ON_LINK:
					Set<Tuple<TransitLine, TransitRoute>> tmpTransitRoutes = getTransitRoutesOnLink(
							Id.createLinkId(cmd[2]));
					for (Tuple<TransitLine, TransitRoute> tpl : tmpTransitRoutes) {
						changeRefLink(tpl.getFirst(), tpl.getSecond(), cmd[3], cmd[4]);
					}
					break;
				default:
					changeRefLink(getTransitLine(cmd[1]), getTransitRoute(cmd[1], cmd[2]), cmd[3], cmd[4]);
				}
			} else {
				throw new IllegalArgumentException(
						"Incorrect number of arguments for " + cmd[0] + "! 3 or 5 needed, " + cmd.length + " given");
			}
		}

		/*
		  Adds a link to the network. Uses the attributes (freespeed, nr of lanes, transportModes)
		  of the attributeLink.
		  [addLink] [linkId] [fromNodeId] [toNodeId] [attributeLinkId]
		 */
		else if (ADD_LINK.equals(cmd[0])) {
			if (cmd.length == 5) {
				addLink(cmd[1], cmd[2], cmd[3], cmd[4]);
				refreshSchedule();
			} else {
				throw new IllegalArgumentException(
						"Incorrect number of arguments for " + cmd[0] + "! 5 needed, " + cmd.length + " given");
			}
		}

		/*
		  Refreshes the given transit route (reroute all paths between referenced stop facility links)
		  [refreshTransitRoute] [transitLineId] [transitRouteId]
		 */
		else if (REFRESH_TRANSIT_ROUTE.equals(cmd[0])) {
			if (cmd.length >= 3) {
				refreshTransitRoute(getTransitLine(cmd[1]), getTransitRoute(cmd[1], cmd[2]));
			} else {
				throw new IllegalArgumentException(
						"Incorrect number of arguments for " + cmd[0] + "! 3 needed, " + cmd.length + " given");
			}
		}

		/*
		  comment
		 */
		else if (COMMENT_START.equals(cmd[0].substring(0, 2))) {
			// comment
		} else {
			throw new IllegalArgumentException("Invalid command \"" + cmd[0] + "\"");
		}
	}

	/**
	 * @return the TransitLine of the schedule
	 */
	private TransitLine getTransitLine(String transitLineStr) {
		TransitLine transitLine = schedule.getTransitLines().get(Id.create(transitLineStr, TransitLine.class));
		if (transitLine == null) {
			throw new IllegalArgumentException("TransitLine " + transitLineStr + " not found!");
		}
		return transitLine;
	}

	/**
	 * @return the TransitRoute of the schedule based on transit line and transit route as strings
	 */
	private TransitRoute getTransitRoute(String transitLineStr, String transitRouteStr) {
		TransitLine transitLine = schedule.getTransitLines().get(Id.create(transitLineStr, TransitLine.class));
		if (transitLine == null) {
			throw new IllegalArgumentException("TransitLine " + transitLineStr + " not found!");
		}
		Id<TransitRoute> transitRouteId = Id.create(transitRouteStr, TransitRoute.class);
		if (!transitLine.getRoutes().containsKey(transitRouteId)) {
			throw new IllegalArgumentException(
					"TransitRoute " + transitRouteStr + " not found in Transitline " + transitLineStr + "!");
		}
		return transitLine.getRoutes().get(transitRouteId);
	}

	/**
	 * Reroutes the section between two stops that passes the oldlink via the new link
	 * @param transitRoute the transit route
	 * @param oldLinkId the section between two route stops where this link appears is rerouted
	 * @param newLinkId the section is routed via this link
	 */
	@Override
	public void rerouteViaLink(TransitLine transitLine, TransitRoute transitRoute, Id<Link> oldLinkId,
			Id<Link> newLinkId) {
		List<TransitRouteStop> stopSequence = transitRoute.getStops();
		List<Id<Link>> linkSequence = transitRoute.getRoute().getLinkIds();

		List<Id<Link>> refLinkIds = stopSequence.stream().map(routeStop -> routeStop.getStopFacility().getLinkId())
				.collect(Collectors.toList());

		if (refLinkIds.contains(oldLinkId)) {
			throw new IllegalArgumentException(
					"Link is referenced to a stop facility, rerouteViaLink cannot be performed. Use changeRefLink instead.");
		} else {
			int i = 0;
			TransitRouteStop fromRouteStop = stopSequence.get(i);
			for (Id<Link> linkId : linkSequence) {
				if (linkId.equals(oldLinkId)) {
					rerouteFromStop(transitLine, transitRoute, fromRouteStop, newLinkId);
					break;
				}
				if (linkId.equals(refLinkIds.get(i))) {
					fromRouteStop = stopSequence.get(i++);
					i++;
				}
			}
		}
	}

	/**
	 *
	 * @param transitRoute  the transit route
	 * @param fromRouteStop the section of the route from this routeStop to the subsequent
	 *                      routeStop is rerouted
	 * @param viaLinkId		the section is routed via this link
	 */
	@Override
	public void rerouteFromStop(TransitLine transitLine, TransitRoute transitRoute, TransitRouteStop fromRouteStop,
			Id<Link> viaLinkId) {
		List<TransitRouteStop> routeStops = transitRoute.getStops();
		TransitRouteStop toRouteStop = routeStops.get(routeStops.indexOf(fromRouteStop) + 1);

		Id<Link> cutFromLinkId = fromRouteStop.getStopFacility().getLinkId();
		Link cutFromLink = network.getLinks().get(cutFromLinkId);
		Id<Link> cutToLinkId = toRouteStop.getStopFacility().getLinkId();
		Link cutToLink = network.getLinks().get(cutToLinkId);
		Link viaLink = network.getLinks().get(viaLinkId);

		NetworkRoute routeBeforeCut = transitRoute.getRoute().getSubRoute(transitRoute.getRoute().getStartLinkId(),
				cutFromLinkId);
		NetworkRoute routeAfterCut = transitRoute.getRoute().getSubRoute(cutToLinkId,
				transitRoute.getRoute().getEndLinkId());

		LeastCostPathCalculator.Path path1 = routers.calcLeastCostPath(cutFromLink.getToNode().getId(),
				viaLink.getFromNode().getId(), transitLine, transitRoute);
		LeastCostPathCalculator.Path path2 = routers.calcLeastCostPath(viaLink.getToNode().getId(),
				cutToLink.getFromNode().getId(), transitLine, transitRoute);

		if (path1 != null && path2 != null) {
			List<Id<Link>> newLinkSequence = new ArrayList<>(routeBeforeCut.getLinkIds());
			newLinkSequence.add(routeBeforeCut.getEndLinkId());
			newLinkSequence.addAll(PTMapperTools.getLinkIdsFromPath(path1));
			newLinkSequence.add(viaLinkId);
			newLinkSequence.addAll(PTMapperTools.getLinkIdsFromPath(path2));
			newLinkSequence.add(routeAfterCut.getStartLinkId());
			newLinkSequence.addAll(routeAfterCut.getLinkIds());
			newLinkSequence.add(routeAfterCut.getEndLinkId());
			transitRoute.setRoute(RouteUtils.createNetworkRoute(newLinkSequence));
		}
	}

	private void rerouteFromStop(TransitLine transitLine, TransitRoute transitRoute, String fromStopFacilityId,
			String viaLinkId) {
		rerouteFromStop(transitLine, transitRoute, getRouteStop(transitRoute, fromStopFacilityId),
				Id.createLinkId(viaLinkId));
	}

	/**
	 * @return the stop facility of a transit route that has the given parentId
	 */
	private TransitStopFacility getChildStopInRoute(TransitRoute transitRoute, String parentId) {
		for (TransitRouteStop routeStop : transitRoute.getStops()) {
			if (parentId.equals(ScheduleTools.createParentStopFacilityId(routeStop.getStopFacility()).toString())) {
				return routeStop.getStopFacility();
			}
		}
		throw new IllegalArgumentException(
				"No child facility for " + parentId + " found in Transit Route " + transitRoute + ".");
	}

	/**
	 * @return the stop facility of a transit route that has the given id
	 */
	private TransitStopFacility getStopFacilityInRoute(TransitRoute transitRoute, String stopFacilityId) {
		return getRouteStop(transitRoute, stopFacilityId).getStopFacility();
	}

	/**
	 * @return the TransitRouteStop with that contain the given stop facility
	 */
	public TransitRouteStop getRouteStop(TransitRoute transitRoute, Id<TransitStopFacility> stopFacilityId) {
		for (TransitRouteStop routeStop : transitRoute.getStops()) {
			if (stopFacilityId.equals(routeStop.getStopFacility().getId())) {
				return routeStop;
			}
		}
		throw new IllegalArgumentException(
				"No child facility for " + stopFacilityId + " found in Transit Route " + transitRoute + ".");
	}

	private TransitRouteStop getRouteStop(TransitRoute transitRoute, String stopFacilityIdStr) {
		return getRouteStop(transitRoute, createStopFacilityId(stopFacilityIdStr));
	}

	/**
	 * Shortcut to create a stop facility id
	 */
	private Id<TransitStopFacility> createStopFacilityId(String stopFacilityIdStr) {
		return Id.create(stopFacilityIdStr, TransitStopFacility.class);
	}

	/**
	 * Changes the reference of a stop facility (for all routes)
	 */
	@Override
	public void changeRefLink(Id<TransitStopFacility> stopFacilityId, Id<Link> newRefLinkId) {
		TransitStopFacility oldStopFacility = schedule.getFacilities().get(stopFacilityId);
		TransitStopFacility newChildStopFacility = parentStops.getChildStopFacility(
				ScheduleTools.createParentStopFacilityId(stopFacilityId.toString()), newRefLinkId.toString());
		replaceStopFacilityInAllRoutes(oldStopFacility, newChildStopFacility);
	}

	private void changeRefLink(String stopFacilityIdStr, String newRefLinkIdStr) {
		TransitStopFacility oldStopFacility = schedule.getFacilities().get(createStopFacilityId(stopFacilityIdStr));
		TransitStopFacility newChildStopFacility = parentStops
				.getChildStopFacility(ScheduleTools.createParentStopFacilityId(stopFacilityIdStr), newRefLinkIdStr);
		replaceStopFacilityInAllRoutes(oldStopFacility, newChildStopFacility);
	}

	/**
	 * changes the child stop in the given route
	 */
	private void changeRefLink(TransitLine transitLine, TransitRoute transitRoute, String childStopFacilityIdStr,
			String newRefLinkIdStr) {
		TransitStopFacility childStopToReplace = schedule.getFacilities()
				.get(Id.create(childStopFacilityIdStr, TransitStopFacility.class));
		TransitStopFacility childStopReplaceWith = parentStops.getChildStopFacility(
				ScheduleTools.createParentStopFacilityId(childStopFacilityIdStr), newRefLinkIdStr);

		replaceStopFacilityInRoute(transitLine, transitRoute, childStopToReplace, childStopReplaceWith);
	}

	/**
	 * creates a new stop facility and adds it to the schedule
	 */
	public TransitStopFacility createStopFacility(Id<TransitStopFacility> facilityId, Coord coord, String name,
			Id<Link> linkId) {
		TransitStopFacility newTransitStopFacility = scheduleFactory.createTransitStopFacility(facilityId, coord,
				false);
		newTransitStopFacility.setName(name);
		newTransitStopFacility.setLinkId(linkId);
		return newTransitStopFacility;
	}

	/**
	 * Adds a link to the network. Uses the attributes (freespeed, nr of lanes, transportModes)
	 * of the attributeLink.
	 */
	@Override
	public void addLink(Id<Link> newLinkId, Id<Node> fromNodeId, Id<Node> toNodeId, Id<Link> attributeLinkId) {
		Node fromNode = network.getNodes().get(fromNodeId);
		Node toNode = network.getNodes().get(toNodeId);

		Link newLink = networkFactory.createLink(newLinkId, fromNode, toNode);

		if (attributeLinkId != null) {
			Link attributeLink = network.getLinks().get(attributeLinkId);

			newLink.setAllowedModes(attributeLink.getAllowedModes());
			newLink.setCapacity(attributeLink.getCapacity());
			newLink.setFreespeed(attributeLink.getFreespeed());
			newLink.setNumberOfLanes(attributeLink.getNumberOfLanes());
		}

		network.addLink(newLink);
	}

	private void addLink(String newLinkIdStr, String fromNodeIdStr, String toNodeIdStr, String attributeLinkIdStr) {
		addLink(Id.createLinkId(newLinkIdStr), Id.createNodeId(fromNodeIdStr), Id.createNodeId(toNodeIdStr),
				Id.createLinkId(attributeLinkIdStr));
	}

	/**
	 * Replaces a stop facility with another one in the given route. Both ids must exist.
	 */
	public void replaceStopFacilityInRoute(TransitLine transitLine, TransitRoute transitRoute,
			Id<TransitStopFacility> toReplaceId, Id<TransitStopFacility> replaceWithId) {
		TransitStopFacility toReplace = schedule.getFacilities().get(toReplaceId);
		TransitStopFacility replaceWith = schedule.getFacilities().get(replaceWithId);

		if (toReplace == null) {
			throw new IllegalArgumentException("StopFacility " + toReplaceId + " not found in schedule!");
		} else if (replaceWith == null) {
			throw new IllegalArgumentException("StopFacility " + replaceWithId + " not found in schedule!");
		}
		replaceStopFacilityInRoute(transitLine, transitRoute, toReplace, replaceWith);
	}

	/**
	 * Replaces a stop facility with another one in the given route. Both facilities must exist.
	 */
	public void replaceStopFacilityInRoute(TransitLine transitLine, TransitRoute transitRoute,
			TransitStopFacility toReplace, TransitStopFacility replaceWith) {
		TransitRouteStop routeStopToReplace = transitRoute.getStop(toReplace);
		if (routeStopToReplace != null) {
			routeStopToReplace.setStopFacility(replaceWith);
			refreshTransitRoute(transitLine, transitRoute);
		} else {
			throw new IllegalArgumentException(
					"StopFacility " + toReplace.getId() + " not found in TransitRoute " + transitRoute.getId());
		}
	}

	/**
	 * Replaces a stop facility with another one the whole schedule. Both must exist.
	 */
	public void replaceStopFacilityInAllRoutes(TransitStopFacility toReplace, TransitStopFacility replaceWith) {
		for (TransitLine line : schedule.getTransitLines().values()) {
			for (TransitRoute route : line.getRoutes().values()) {
				replaceStopFacilityInRoute(line, route, toReplace, replaceWith);
			}
		}
	}

	/**
	 * Gets all transit routes that ar on the given link
	 */
	public Set<Tuple<TransitLine, TransitRoute>> getTransitRoutesOnLink(Id<Link> linkId) {
		Set<Tuple<TransitLine, TransitRoute>> transitRoutesOnLink = new HashSet<>();
		for (TransitLine transitLine : schedule.getTransitLines().values()) {
			for (TransitRoute transitRoute : transitLine.getRoutes().values()) {
				if (ScheduleTools.getTransitRouteLinkIds(transitRoute).contains(linkId)) {
					transitRoutesOnLink.add(new Tuple<>(transitLine, transitRoute));
				}
			}
		}
		return transitRoutesOnLink;
	}

	/**
	 * "Refreshes" the transit route by routing between all referenced links
	 * of the stop facilities.
	 */
	@Override
	public void refreshTransitRoute(TransitLine transitLine, TransitRoute transitRoute) {
		List<TransitRouteStop> routeStops = transitRoute.getStops();
		List<Id<Link>> linkSequence = new ArrayList<>();
		linkSequence.add(routeStops.get(0).getStopFacility().getLinkId());

		// route
		for (int i = 0; i < routeStops.size() - 1; i++) {
			if (routeStops.get(i).getStopFacility().getLinkId() == null) {
				throw new IllegalArgumentException("stop facility " + routeStops.get(i).getStopFacility().getName()
						+ " (" + routeStops.get(i).getStopFacility().getId() + " not referenced!");
			}
			if (routeStops.get(i + 1).getStopFacility().getLinkId() == null) {
				throw new IllegalArgumentException("stop facility " + routeStops.get(i - 1).getStopFacility().getName()
						+ " (" + routeStops.get(i + 1).getStopFacility().getId() + " not referenced!");
			}

			Id<Link> currentLinkId = Id.createLinkId(routeStops.get(i).getStopFacility().getLinkId().toString());

			Link currentLink = network.getLinks().get(currentLinkId);
			Link nextLink = network.getLinks().get(routeStops.get(i + 1).getStopFacility().getLinkId());

			List<Id<Link>> path = PTMapperTools.getLinkIdsFromPath(routers.calcLeastCostPath(
					currentLink.getToNode().getId(), nextLink.getFromNode().getId(), transitLine, transitRoute));

			if (path != null)
				linkSequence.addAll(path);

			linkSequence.add(nextLink.getId());
		}

		// add link sequence to schedule
		transitRoute.setRoute(RouteUtils.createNetworkRoute(linkSequence));
	}

	/**
	 * Refreshes the whole schedule by routing all transit routes.
	 */
	@Override
	public void refreshSchedule() {
		for (TransitLine transitLine : schedule.getTransitLines().values()) {
			for (TransitRoute transitRoute : transitLine.getRoutes().values()) {
				refreshTransitRoute(transitLine, transitRoute);
			}
		}
	}

	/**
	 * Container class for all parent stop facilities
	 */
	private class ParentStops {

		final Map<Id<TransitStopFacility>, ParentStopFacility> fac = new HashMap<>();

		public ParentStops() {
			for (TransitStopFacility stopFacility : schedule.getFacilities().values()) {
				Id<TransitStopFacility> parentId = ScheduleTools.createParentStopFacilityId(stopFacility);
				if (!fac.containsKey(parentId)) {
					fac.put(parentId, new ParentStopFacility(stopFacility));
				} else {
					fac.get(parentId).getChildStopFacility(stopFacility);
				}
			}
		}

		private TransitStopFacility getChildStopFacility(Id<TransitStopFacility> parentId, String newRefLinkIdStr) {
			return fac.get(parentId).getChildStopFacility(newRefLinkIdStr);
		}

	}

	/**
	 * Container class for a parent stop facility (most likely
	 * not actual facilities in the schedule)
	 */
	private class ParentStopFacility {
		final String id;
		final String name;
		final Coord coord;

		final Map<Id<Link>, TransitStopFacility> children = new HashMap<>();

		public ParentStopFacility(String id, String name, Coord coord) {
			this.id = id;
			this.name = name;
			this.coord = coord;
		}

		public ParentStopFacility(TransitStopFacility childStopFacility) {
			this.id = ScheduleTools.createParentStopFacilityId(childStopFacility).toString();
			this.name = childStopFacility.getName();
			this.coord = childStopFacility.getCoord();

			children.put(childStopFacility.getLinkId(), childStopFacility);
		}

		public void getChildStopFacility(TransitStopFacility childStopFacility) {
			children.put(childStopFacility.getLinkId(), childStopFacility);
		}

		/**
		 * Adds a child stop facility for the given refLink, creates
		 * a new one if needed.
		 * @param refLinkId the id of the ref link
		 * @return the childStopFacility
		 */
		public TransitStopFacility getChildStopFacility(Id<Link> refLinkId) {
			Id<TransitStopFacility> newChildStopId = ScheduleTools.createChildStopFacilityId(id, refLinkId.toString());
			TransitStopFacility newChildStopFacilty = schedule.getFacilities().get(newChildStopId);
			if (newChildStopFacilty == null) {
				newChildStopFacilty = createStopFacility(newChildStopId, this.coord, this.name, refLinkId);
				newChildStopFacilty.setLinkId(refLinkId);
				newChildStopFacilty.setStopAreaId(Id.create(this.id, TransitStopArea.class));
				schedule.addStopFacility(newChildStopFacilty);
			}
			children.put(newChildStopFacilty.getLinkId(), newChildStopFacilty);
			return newChildStopFacilty;
		}

		public TransitStopFacility getChildStopFacility(String newRefLinkIdStr) {
			return getChildStopFacility(Id.createLinkId(newRefLinkIdStr));
		}
	}
}
