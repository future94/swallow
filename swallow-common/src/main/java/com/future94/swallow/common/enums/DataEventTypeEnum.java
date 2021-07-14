package com.future94.swallow.common.enums;

import com.future94.swallow.common.exception.SwallowException;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author weilai
 */
public enum DataEventTypeEnum {

    /**
     * delete event.
     */
    DELETE,

    /**
     * insert event.
     */
    CREATE,

    /**
     * update event.
     */
    UPDATE,

    /**
     * REFRESH data event type enum.
     */
    REFRESH,

    /**
     * Myself data event type enum.
     */
    MYSELF;

    /**
     * Acquire by name data event type enum.
     *
     * @param name the name
     * @return the data event type enum
     */
    public static DataEventTypeEnum acquireByName(final String name) {
        return Arrays.stream(DataEventTypeEnum.values())
                .filter(e -> Objects.equals(e.name(), name))
                .findFirst().orElseThrow(() -> new SwallowException(String.format(" this DataEventTypeEnum can not support %s", name)));
    }
}
