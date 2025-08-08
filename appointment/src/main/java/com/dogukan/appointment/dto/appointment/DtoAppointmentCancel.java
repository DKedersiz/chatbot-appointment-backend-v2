package com.dogukan.appointment.dto.appointment;

import com.dogukan.appointment.enums.CancelledBy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DtoAppointmentCancel {

    @NotBlank
    private String cancelReason;

    @NotNull
    private CancelledBy cancelledBy;
}
