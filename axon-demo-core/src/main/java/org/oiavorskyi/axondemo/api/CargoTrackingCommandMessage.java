package org.oiavorskyi.axondemo.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

public final class CargoTrackingCommandMessage {

    @NotNull
    public final String commandId;

    @NotNull
    public final String cargoId;

    @NotNull
    public final String correlationId;

    @NotNull
    public final String timestamp;

    @JsonCreator
    public CargoTrackingCommandMessage(
            @JsonProperty( "commandId" ) String commandId,
            @JsonProperty( "cargoId" ) String cargoId,
            @JsonProperty( "correlationId" ) String correlationId,
            @JsonProperty( "timestamp" ) String timestamp
    ) {
        this.commandId = commandId;
        this.cargoId = cargoId;
        this.correlationId = correlationId;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CargoTrackingCommandMessage{");
        sb.append("commandId='").append(commandId).append('\'');
        sb.append(", cargoId='").append(cargoId).append('\'');
        sb.append(", correlationId='").append(correlationId).append('\'');
        sb.append(", timestamp='").append(timestamp).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
