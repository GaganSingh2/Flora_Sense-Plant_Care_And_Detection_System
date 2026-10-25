package org.gagan.florasense.Dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationPreferenceResponse {

    private Boolean pushNotificationEnabled;
    private Boolean emailNotificationEnabled;
}
