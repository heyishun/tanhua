package com.tanhua.dubbo.api;

import java.util.List;
import java.util.Map;

public interface UsersCounts {

    Map<String,Integer> counts(Long userId);
}
