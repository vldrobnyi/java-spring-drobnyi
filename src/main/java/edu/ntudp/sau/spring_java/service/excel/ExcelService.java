package edu.ntudp.sau.spring_java.service.excel;

import edu.ntudp.sau.spring_java.model.dto.product.ProductParsingDto;
import edu.ntudp.sau.spring_java.model.dto.product.ProductResponseDto;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class ExcelService implements ExcelReportGenerator {

    private CellStyle headerStyle;
    private CellStyle borderedStyle;
    private CellStyle linkStyle;

    @Override
    public byte[] generateSearchReport(String search, List<ProductParsingDto> productParsingDtos) {
        Workbook workbook = new XSSFWorkbook();

        borderedStyle = createBorderedCellStyle(workbook);
        headerStyle = createHeaderCellStyle(workbook);
        linkStyle = createHyperlinkCellStyle(workbook);

        Sheet sheet = workbook.createSheet("Search results");

        Row searchRow = sheet.createRow(0);
        createCell(searchRow, 0, "Search request:", headerStyle);
        createCell(searchRow, 1, search, borderedStyle);

        Row dateRow = sheet.createRow(1);
        createCell(dateRow, 0, "Date of search:", headerStyle);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        createCell(dateRow, 1, formatter.format(new Date()), borderedStyle);

        Row headerRow = sheet.createRow(3);
        sheet.createFreezePane(0, 4);
        createCell(headerRow, 0, "ID", headerStyle);
        createCell(headerRow, 1, "Name", headerStyle);
        createCell(headerRow, 2, "Price", headerStyle);
        createCell(headerRow, 3, "Stock", headerStyle);
        createCell(headerRow, 4, "Link", headerStyle);

        for (int i = 0; i < productParsingDtos.size(); i++) {
            Row dataRow = sheet.createRow(i + 4);
            addProductData(workbook, dataRow, productParsingDtos.get(i));
        }

        for (int i = 0; i < 5; i++) {
            sheet.autoSizeColumn(i);
            int width = sheet.getColumnWidth(i);
            sheet.setColumnWidth(i, width + 256);
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public byte[] generateDatabaseReport(List<ProductResponseDto> productReposnseDtos) {
        Workbook workbook = new XSSFWorkbook();

        borderedStyle = createBorderedCellStyle(workbook);
        headerStyle = createHeaderCellStyle(workbook);
        linkStyle = createHyperlinkCellStyle(workbook);

        Sheet sheet = workbook.createSheet("Product Report");

        Row headerRow = sheet.createRow(0);
        sheet.createFreezePane(0, 1);
        createCell(headerRow, 0, "ID", headerStyle);
        createCell(headerRow, 1, "Name", headerStyle);
        createCell(headerRow, 2, "Price UAH", headerStyle);
        createCell(headerRow, 3, "Price USD", headerStyle);
        createCell(headerRow, 4, "Price EUR", headerStyle);
        createCell(headerRow, 5, "Stock Status", headerStyle);
        createCell(headerRow, 6, "Link", headerStyle);

        for (int i = 0; i < productReposnseDtos.size(); i++) {
            Row dataRow = sheet.createRow(i + 1);
            addProductData(workbook, dataRow, productReposnseDtos.get(i));
        }

        for (int i = 0; i < 9; i++) {
            sheet.autoSizeColumn(i);
            int width = sheet.getColumnWidth(i);
            sheet.setColumnWidth(i, width + 256);
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void createCell(Row row, int columnIndex, String value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void addProductData(Workbook workbook, Row row, ProductParsingDto productParsingDto) {
        createCell(row, 0, String.valueOf(productParsingDto.getId()), borderedStyle);
        createCell(row, 1, productParsingDto.getName(), borderedStyle);
        createCell(row, 2, String.valueOf(productParsingDto.getPrice()), borderedStyle);
        createCell(row, 3, productParsingDto.getStockStatus(), borderedStyle);

        Cell linkCell = row.createCell(4);
        Hyperlink hyperlink = workbook.getCreationHelper().createHyperlink(HyperlinkType.URL);
        hyperlink.setAddress(productParsingDto.getLink());

        XSSFRichTextString richText = new XSSFRichTextString("Link");
        linkCell.setCellValue(richText);
        linkCell.setHyperlink(hyperlink);
        linkCell.setCellStyle(linkStyle);
    }

    private void addProductData(Workbook workbook, Row row, ProductResponseDto productResponseDto) {
        createCell(row, 0, String.valueOf(productResponseDto.getId()), borderedStyle);
        createCell(row, 1, productResponseDto.getName(), borderedStyle);
        createCell(row, 2, String.valueOf(productResponseDto.getPriceUah()), borderedStyle);
        createCell(row, 3, String.valueOf(productResponseDto.getPriceUsd()), borderedStyle);
        createCell(row, 4, String.valueOf(productResponseDto.getPriceEur()), borderedStyle);
        createCell(row, 5, productResponseDto.getStockStatus(), borderedStyle);

        Cell linkCell = row.createCell(6);
        Hyperlink hyperlink = workbook.getCreationHelper().createHyperlink(HyperlinkType.URL);
        hyperlink.setAddress(productResponseDto.getLink());
        linkCell.setCellValue("Link");
        linkCell.setHyperlink(hyperlink);
        linkCell.setCellStyle(linkStyle);
    }

    private static CellStyle createHeaderCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);

        return style;
    }

    private static CellStyle createBorderedCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static CellStyle createHyperlinkCellStyle(Workbook workbook) {
        CellStyle style = createBorderedCellStyle(workbook);

        Font hyperlinkFont = workbook.createFont();
        hyperlinkFont.setColor(IndexedColors.BLUE.getIndex());
        hyperlinkFont.setUnderline(Font.U_SINGLE);

        style.setFont(hyperlinkFont);
        return style;
    }
}
