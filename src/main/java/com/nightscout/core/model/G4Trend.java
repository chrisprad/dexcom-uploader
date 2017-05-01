// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: /Users/klee/Projects/Nightscout/android-uploader/core/src/main/java/com/nightscout/core/model/Download.proto
package com.nightscout.core.model;

import com.squareup.wire.ProtoEnum;

public enum G4Trend
        implements ProtoEnum {
    TREND_NONE(0),
    DOUBLE_UP(1),
    /**
     * More than 3 mg/dL per minute
     */
    SINGLE_UP(2),
    /**
     * +2 to +3 mg/dL per minute
     */
    FORTY_FIVE_UP(3),
    /**
     * +1 to +2 mg/dL per minute
     */
    FLAT(4),
    /**
     * +/- 1 mg/dL per minute
     */
    FORTY_FIVE_DOWN(5),
    /**
     * -1 to -2 mg/dL per minute
     */
    SINGLE_DOWN(6),
    /**
     * -2 to -3 mg/dL per minute
     */
    DOUBLE_DOWN(7),
    /**
     * more than -3 mg/dL per minute
     */
    NOT_COMPUTABLE(8),
    RATE_OUT_OF_RANGE(9);

    private final int value;

    private G4Trend(int value) {
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }
}
