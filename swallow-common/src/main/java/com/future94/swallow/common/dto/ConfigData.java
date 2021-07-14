package com.future94.swallow.common.dto;

import com.future94.swallow.common.utils.GsonUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author weilai
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ConfigData<T> {

    private volatile String md5;

    private volatile long lastModifyTime;

    private List<T> data;

    @Override
    public String toString() {
        return GsonUtils.getInstance().toJson(this);
    }
}
