package com.kc.system.service;

import com.kc.system.entity.GeoReverseRecord;

import java.util.List;

public interface GeoReverseService {
    
    /** 批量解析坐标 */
    void batchReverse(List<GeoReverseRecord> records);
    
    /** 导出解析结果为 Excel */
    byte[] exportToExcel(List<GeoReverseRecord> records);
}
