package com.kc.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kc.system.entity.GeoReverseRecord;
import com.kc.system.mapper.GeoReverseRecordMapper;
import com.kc.system.service.GeoReverseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Controller
@RequestMapping("/geo")
@RequiredArgsConstructor
public class GeoReverseController {

    private final GeoReverseService geoReverseService;
    private final GeoReverseRecordMapper recordMapper;

    /** 页面 */
    @GetMapping("/reverse")
    public String reversePage(Model model) {
        List<GeoReverseRecord> records = recordMapper.selectList(
            new LambdaQueryWrapper<GeoReverseRecord>()
                .orderByDesc(GeoReverseRecord::getCreateTime)
                .last("LIMIT 500")
        );
        model.addAttribute("records", records);
        return "geo/reverse";
    }

    /** 刷新记录（AJAX） */
    @GetMapping("/reverse/records")
    @ResponseBody
    public List<GeoReverseRecord> refreshRecords() {
        return recordMapper.selectList(
            new LambdaQueryWrapper<GeoReverseRecord>()
                .orderByDesc(GeoReverseRecord::getCreateTime)
                .last("LIMIT 500")
        );
    }

    /** 单个录入 */
    @PostMapping("/reverse/single")
    public String reverseSingle(@RequestParam BigDecimal longitude,
                                @RequestParam BigDecimal latitude,
                                RedirectAttributes ra) {
        String batchNo = generateBatchNo();
        GeoReverseRecord record = new GeoReverseRecord();
        record.setBatchNo(batchNo);
        record.setLongitude(longitude);
        record.setLatitude(latitude);
        record.setStatus(0);
        recordMapper.insert(record);

        // 异步解析
        CompletableFuture.runAsync(() ->
            geoReverseService.batchReverse(Collections.singletonList(record))
        );

        ra.addFlashAttribute("success", "坐标已提交，正在解析... 批次号：" + batchNo);
        return "redirect:/geo/reverse";
    }

    /** 批量导入 */
    @PostMapping("/reverse/import")
    public String reverseImport(@RequestParam("file") MultipartFile file,
                                RedirectAttributes ra) {
        try {
            String batchNo = generateBatchNo();
            List<GeoReverseRecord> records = parseExcel(file, batchNo);
            if (records.isEmpty()) {
                ra.addFlashAttribute("error", "未解析到有效坐标数据");
                return "redirect:/geo/reverse";
            }
            
            for (GeoReverseRecord record : records) {
                recordMapper.insert(record);
            }

            // 异步批量解析
            CompletableFuture.runAsync(() ->
                geoReverseService.batchReverse(records)
            );

            ra.addFlashAttribute("success", "成功导入 " + records.size() + " 条坐标，正在后台解析... 批次号：" + batchNo);
        } catch (Exception e) {
            log.error("导入失败", e);
            ra.addFlashAttribute("error", "导入失败: " + e.getMessage());
        }
        return "redirect:/geo/reverse";
    }

    /** 导出 Excel */
    @GetMapping("/reverse/export")
    public void exportExcel(@RequestParam(required = false) String batchNo,
                           HttpServletResponse response) throws IOException {
        List<GeoReverseRecord> records;
        if (batchNo != null && !batchNo.isEmpty()) {
            records = recordMapper.selectList(
                new LambdaQueryWrapper<GeoReverseRecord>()
                    .eq(GeoReverseRecord::getBatchNo, batchNo)
                    .orderByDesc(GeoReverseRecord::getCreateTime)
            );
        } else {
            records = recordMapper.selectList(
                new LambdaQueryWrapper<GeoReverseRecord>()
                    .orderByDesc(GeoReverseRecord::getCreateTime)
            );
        }
        
        byte[] excelData = geoReverseService.exportToExcel(records);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
            "attachment; filename=geo_reverse_" + (batchNo != null ? batchNo : "all") + "_" + System.currentTimeMillis() + ".xlsx");
        response.getOutputStream().write(excelData);
    }

    /** 解析导入的 Excel */
    private List<GeoReverseRecord> parseExcel(MultipartFile file, String batchNo) throws IOException {
        List<GeoReverseRecord> records = new ArrayList<>();
        
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    log.debug("跳过空行 {}", i + 1);
                    continue;
                }

                try {
                    // 检查单元格是否为空
                    if (row.getCell(0) == null || row.getCell(1) == null) {
                        log.debug("跳过空单元格行 {}", i + 1);
                        continue;
                    }

                    // 尝试读取数值
                    BigDecimal longitude = new BigDecimal(row.getCell(0).getNumericCellValue());
                    BigDecimal latitude = new BigDecimal(row.getCell(1).getNumericCellValue());

                    GeoReverseRecord record = new GeoReverseRecord();
                    record.setBatchNo(batchNo);
                    record.setLongitude(longitude);
                    record.setLatitude(latitude);
                    record.setStatus(0);
                    records.add(record);
                } catch (Exception e) {
                    log.warn("跳过无效行 {}: 经度={}, 纬度={}, 原因={}", 
                        i + 1, 
                        row.getCell(0) != null ? row.getCell(0).toString() : "null",
                        row.getCell(1) != null ? row.getCell(1).toString() : "null",
                        e.getMessage());
                }
            }
        }

        return records;
    }

    /** 生成批次号 */
    private String generateBatchNo() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMddHHmmss");
        return "BATCH_" + sdf.format(new java.util.Date()) + "_" + 
               String.format("%04d", (int)(Math.random() * 10000));
    }
}
