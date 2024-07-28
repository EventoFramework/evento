package com.evento.common.modeling.messaging.payload;

import com.evento.common.utils.Context;

/**
 * PayloadWithContext is an abstract class that represents a payload object with context information.
 * It extends the Payload interface.
 */
public abstract class TrackablePayload implements Payload {

    private boolean forceTelemetry = false;

    /**
     * Returns the aggregate ID associated with this payload.
     *
     * The aggregate ID is a unique identifier assigned to an aggregation of related objects.
     * It is used to group together objects that should be treated as a single unit in the system.
     *
     * @return the aggregate ID as a string
     *
     * @see TrackablePayload
     */
    public abstract String getAggregateId();

    /**
     * Checks if telemetry is forced for the current payload.
     *
     * Telemetry refers to the collection of data about the operation and performance of a software system.
     * When telemetry is forced, it overrides the default behavior of telemetry data collection.
     *
     * @return true if telemetry is forced, false otherwise
     *
     * @see TrackablePayload
     */
    public boolean isForceTelemetry() {
        return forceTelemetry;
    }

    /**
     * Sets whether telemetry should be forced for the current payload.
     *
     * Telemetry refers to the collection of data about the operation and performance of a software system.
     * When telemetry is forced, it overrides the default behavior of telemetry data collection.
     *
     * @param forceTelemetry true to force telemetry, false otherwise
     * @param <T> the type of the payload with context
     * @return the payload itself with the force telemetry flag set
     */
    @SuppressWarnings("unchecked")
    public <T extends TrackablePayload> T setForceTelemetry(boolean forceTelemetry) {
        this.forceTelemetry = forceTelemetry;
        return (T) this;
    }
}
