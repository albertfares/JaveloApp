package ch.epfl.javelo.gui;

import ch.epfl.javelo.projection.PointCh;

/**
 * Enregistrement qui représente un point de passage.
 *
 * @author Albert Fares (341918)
 * @author Etienne Asselin (340201)
 */
public record Waypoint(PointCh point, int closestNodeId) {}

