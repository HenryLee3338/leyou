package com.leyou.page.service;

import java.util.Map;

public interface PageService {
    Map buildDataBySPUId(Long id);

    void removeHtml(Long spuId);

    void createHtml(Long spuId);
}
