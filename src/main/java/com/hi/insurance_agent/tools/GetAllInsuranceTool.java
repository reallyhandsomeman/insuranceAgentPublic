package com.hi.insurance_agent.tools;

import com.hi.insurance_agent.constant.DatasetConstant;
import com.hi.insurance_agent.util.CsvReader;
import org.springframework.ai.tool.annotation.Tool;

import java.util.Map;

public class GetAllInsuranceTool {
    
    @Tool(description = "Get all insurance service name")
    public String getAllInsurance() {
        Map<String, String> map = CsvReader.readCsvToMap(DatasetConstant.CSV_PATH);
        return String.join(",", map.values());
    }
}
