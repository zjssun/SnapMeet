package com.snapmeet.entity.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class PeerConnectionDataDto {
    private String token;
    private String sendUserId;
    private String receiveUserId;
    private String signalType;
    private String signalData;
}
