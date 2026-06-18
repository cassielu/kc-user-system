package com.kc.system.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.kc.system.component.RateLimiter;
import com.kc.system.config.GaodeApiProperties;
import com.kc.system.entity.GeoReverseRecord;
import com.kc.system.mapper.GeoReverseRecordMapper;
import com.kc.system.service.GeoReverseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeoReverseServiceImpl implements GeoReverseService {

    private final GaodeApiProperties gaodeProperties;
    private final RateLimiter rateLimiter;
    private final RestTemplate restTemplate;
    private final GeoReverseRecordMapper recordMapper;

    @Override
    public void batchReverse(List<GeoReverseRecord> records) {
        for (GeoReverseRecord record : records) {
            try {
                // 限频控制
                rateLimiter.acquire();

                String url = gaodeProperties.getReverseGeocodeUrl()
                    + "?key=" + gaodeProperties.getKey()
                    + "&location=" + record.getLongitude() + "," + record.getLatitude()
                    + "&output=json";

                log.info("[GeoReverse] 请求: {}", url);
                String response = restTemplate.getForObject(url, String.class);
                JSONObject json = JSON.parseObject(response);

                if ("1".equals(json.getString("status"))) {
                    JSONObject regeocode = json.getJSONObject("regeocode");
                    JSONObject addressComponent = regeocode.getJSONObject("addressComponent");

                    record.setProvince(addressComponent.getString("province"));
                    record.setCity(addressComponent.getString("city"));
                    record.setDistrict(addressComponent.getString("district"));
                    record.setAddress(regeocode.getString("formatted_address"));
                    record.setStatus(1); // 成功
                    
                    log.info("[GeoReverse] 成功: {},{} -> {}{}{}", 
                        record.getLongitude(), record.getLatitude(),
                        record.getProvince(), record.getCity(), record.getDistrict());
                } else {
                    record.setStatus(2); // 失败
                    record.setErrorMsg(json.getString("info"));
                    log.warn("[GeoReverse] 失败: {}", json.getString("info"));
                }

                recordMapper.updateById(record);
                Thread.sleep(50); // 额外间隔保护

            } catch (Exception e) {
                log.error("[GeoReverse] 坐标解析失败: {},{}", record.getLongitude(), record.getLatitude(), e);
                record.setStatus(2);
                record.setErrorMsg(e.getMessage());
                recordMapper.updateById(record);
            }
        }
    }

    @Override
    public byte[] exportToExcel(List<GeoReverseRecord> records) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("坐标解析结果");

            // 表头
            Row header = sheet.createRow(0);
            String[] headers = {"批次号", "ID", "经度", "纬度", "省", "市", "区", "详细地址", "状态", "错误信息"};
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }

            // 数据行
            for (int i = 0; i < records.size(); i++) {
                GeoReverseRecord r = records.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(r.getBatchNo() != null ? r.getBatchNo() : "");
                row.createCell(1).setCellValue(r.getId());
                row.createCell(2).setCellValue(r.getLongitude().doubleValue());
                row.createCell(3).setCellValue(r.getLatitude().doubleValue());
                row.createCell(4).setCellValue(r.getProvince() != null ? r.getProvince() : "");
                row.createCell(5).setCellValue(r.getCity() != null ? r.getCity() : "");
                row.createCell(6).setCellValue(r.getDistrict() != null ? r.getDistrict() : "");
                row.createCell(7).setCellValue(r.getAddress() != null ? r.getAddress() : "");
                row.createCell(8).setCellValue(r.getStatus() == 1 ? "成功" : (r.getStatus() == 2 ? "失败" : "待解析"));
                row.createCell(9).setCellValue(r.getErrorMsg() != null ? r.getErrorMsg() : "");
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("导出 Excel 失败", e);
        }
    }
}
