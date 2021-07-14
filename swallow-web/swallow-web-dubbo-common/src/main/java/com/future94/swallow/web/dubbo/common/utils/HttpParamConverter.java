/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.future94.swallow.web.dubbo.common.utils;

import com.google.gson.Gson;
import lombok.SneakyThrows;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.Part;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The type Http param converter.
 */
public final class HttpParamConverter {

    private static final Gson gson = new Gson();

    private static final Pattern PATTERN = Pattern.compile("([^&=]+)(=?)([^&]+)?");

    /**
     * of.
     *
     * @param supplier supplier
     * @return String string
     */
    public static String ofString(final String supplier) {
        return gson.toJson(initQueryParams(supplier));
    }

    public static String ofFormData(final MultiValueMap<String, String> supplier) {
        return gson.toJson(supplier.toSingleValueMap());
    }

    public static String ofMultipartData(final MultiValueMap<String, Part> supplier) {
        LinkedMultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        for (Map.Entry<String, List<Part>> entry : supplier.entrySet()) {
            entry.getValue().get(0).content().subscribe(dataBuffer -> {
                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(bytes);
                DataBufferUtils.release(dataBuffer);
                multiValueMap.put(entry.getKey(), Collections.singletonList(new String(bytes, StandardCharsets.UTF_8)));
            });
        }
        return ofFormData(multiValueMap);
    }

    /**
     * Init query params map.
     *
     * @param query the query
     * @return the map
     */
    public static Map<String, String> initQueryParams(final String query) {
        final Map<String, String> queryParams = new LinkedHashMap<>();
        if (StringUtils.hasText(query)) {
            final Matcher matcher = PATTERN.matcher(query);
            while (matcher.find()) {
                String name = decodeQueryParam(matcher.group(1));
                String eq = matcher.group(2);
                String value = matcher.group(3);
                value = StringUtils.hasText(value) ? decodeQueryParam(value) : (StringUtils.hasLength(eq) ? "" : null);
                queryParams.put(name, value);
            }
        }
        return queryParams;
    }

    /**
     * Decode query param string.
     *
     * @param value the value
     * @return the string
     */
    @SneakyThrows
    public static String decodeQueryParam(final String value) {
        return URLDecoder.decode(value, "UTF-8");
    }
}
