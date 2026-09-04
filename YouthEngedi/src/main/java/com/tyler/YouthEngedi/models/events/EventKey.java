package com.tyler.YouthEngedi.models.events;

import com.tyler.YouthEngedi.models.enums.ConnectionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventKey {
    private long eventUserId;
    private ConnectionType connectionType;

    @Override
    public String toString(){
        return eventUserId + ":" + connectionType;
    }
}
