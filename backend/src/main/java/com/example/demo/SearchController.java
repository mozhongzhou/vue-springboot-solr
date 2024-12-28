package com.example.demo;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class SearchController {

    private static final Logger logger = Logger.getLogger(SearchController.class.getName());

    @Value("${spring.data.solr.host}")
    private String solrHost;

    @Value("${spring.data.solr.core}")
    private String solrCore;

    @GetMapping("/api/test-connection")
    public ResponseEntity<Map<String, Object>> testConnection() {
        Map<String, Object> response = new HashMap<>();
        HttpSolrClient solrClient = null;
        try {
            solrClient = new HttpSolrClient.Builder(solrHost + "/" + solrCore).build();
            SolrPingResponse pingResponse = solrClient.ping();
            int statusCode = pingResponse.getStatus();
            long qTime = pingResponse.getQTime();
            
            response.put("status", "success");
            response.put("message", "成功连接到Solr服务器");
            response.put("statusCode", statusCode);
            response.put("responseTime", qTime);
            response.put("solrUrl", solrHost + "/" + solrCore);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "连接Solr服务器失败: " + e.getMessage());
            response.put("solrUrl", solrHost + "/" + solrCore);
            
            return ResponseEntity.status(500).body(response);
        } finally {
            if (solrClient != null) {
                try {
                    solrClient.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    @GetMapping("/api/search")
    public ResponseEntity<Map<String, Object>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws SolrServerException, IOException {
        
        HttpSolrClient solrClient = new HttpSolrClient.Builder(solrHost + "/" + solrCore).build();
        SolrQuery query = new SolrQuery();
        query.setQuery("mytext:" + q);
        query.setStart(page * size);
        query.setRows(size);
        query.setSort("score", SolrQuery.ORDER.desc);
        query.setHighlight(true);
        query.addHighlightField("mytext");
        query.setHighlightSimplePre("<em>");
        query.setHighlightSimplePost("</em>");

        QueryResponse response = solrClient.query(query);
        SolrDocumentList results = response.getResults();

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("results", results);
        resultMap.put("numFound", results.getNumFound());
        resultMap.put("start", results.getStart());
        resultMap.put("page", page);
        resultMap.put("size", size);

        return ResponseEntity.ok(resultMap);
    }
}