package com.wxm.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wxm.entity.ReportResult;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;

public class ExportExcelUtil {
    private String[] excelHeader = { "公司名称", "发起合同数量", "被打回合同数量","存档合同数量","存档/发起比例"};
    public ExportExcelUtil(){

    }
    public ExportExcelUtil(String[] excelHeader){
        this.excelHeader = excelHeader;
    }
    public HSSFWorkbook exportExcel(String name ,List<ReportResult> list) {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet(name);
        HSSFRow row = sheet.createRow((int) 0);
        HSSFCellStyle style = wb.createCellStyle();
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);

        for (int i = 0; i < excelHeader.length; i++) {
            HSSFCell cell = row.createCell(i);
            cell.setCellValue(excelHeader[i]);
            cell.setCellStyle(style);
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, 100 * 100);
        }

        for (int i = 0; i < list.size(); i++) {
            row = sheet.createRow(i + 1);
            ReportResult reportResult = list.get(i);
            row.createCell(0).setCellValue(reportResult.getEnterprise());
            row.createCell(1).setCellValue(reportResult.getTotal());
            row.createCell(2).setCellValue(reportResult.getComplete());
            row.createCell(3).setCellValue(reportResult.getRefuse());
            row.createCell(4).setCellValue(reportResult.getRate());
        }
        return wb;
    }
}
