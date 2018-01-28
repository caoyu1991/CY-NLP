package com.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.constants.ModelConstant;
import com.constants.SearchConstant;
import com.constants.SearchConstant.Property;
import com.constants.SearchConstant.TargetModel;
import com.dao.ModelDao;

/**
 * Created by cao_y on 2018/1/22.
 */
public class SearchService {

    private final Logger logger = LogManager.getLogger();

    private final ModelDao modelDao;

    public SearchService() {
        this.modelDao = new ModelDao(generateSatasetNamesMap());
    }

    public Map<Object, Object> findEnityByGivenName(TargetModel targetModel, String id) {
        StringBuffer queryString = new StringBuffer();
        queryString.append(targetModel.getPrefix()).append("select ?s ?p ?o where { ").append(targetModel.getName())
                .append(":").append(id).append("_sanguozhi ?p ?o }");
        ResultSet resultSet = modelDao.queryModel(targetModel.getModelName().get(), queryString.toString());
        Map<Object, Object> result = new HashMap<>();
        while(resultSet.hasNext()) {
            QuerySolution qs = resultSet.next();
            RDFNode p = qs.get("p");
            RDFNode o = qs.get("o");
            Object key = null;
            Object value = null;
            if (p.isLiteral()) {
                key = p.toString();
            } else {
                key = p.asResource();
            }
            if (o.isLiteral()) {
                value = o.toString();
            } else {
                value = o.asResource();
            }
            result.put(key, value);
        }
        return result;
    }

    public List<String> findGivenPropertyContainGivenName(TargetModel targetModel, String id, Property property) {
        if (targetModel == null || id == null || property == null) {
            return null;
        }
        StringBuffer queryString = new StringBuffer();
        queryString.append(targetModel.getPrefix()).append("select ?s ?p ?o where { ")
                .append("?s ").append(targetModel.getName()).append(":").append(property.getProperty()).append(" ?o ")
                .append("FILTER regex(str(?s), \"").append(id).append("\")}");
        ResultSet resultSet = modelDao.queryModel(targetModel.getModelName().get(), queryString.toString());
        List<String> result = new ArrayList<>();
        while(resultSet.hasNext()) {
            QuerySolution qs = resultSet.next();
            RDFNode o = qs.get("o");
            if (o.isLiteral()) {
                result.add(o.asLiteral().getLexicalForm());
            } else {
                result.add(findLabelByGivenId(Property.getTargetModelByProperty(property), o.asResource().getURI()));
            }
        }
        return result;
    }

    private String findLabelByGivenId(TargetModel targetModel, String id) {
        StringBuffer queryString = new StringBuffer();
        queryString.append(SearchConstant.RDFS).append("select ?s ?p ?o where { <").append(id).append("> rdfs:label ?o}");
        ResultSet resultSet = modelDao.queryModel(targetModel.getModelName().get(), queryString.toString());
        String result = StringUtils.EMPTY;
        while(resultSet.hasNext()) {
            QuerySolution qs = resultSet.next();
            RDFNode o = qs.get("o");
            result = o.asLiteral().getLexicalForm();
        }
        return result;
    }

    private Map<String, String> generateSatasetNamesMap() {
        Map<String, String> datasetNamesMap = new HashMap<>();
        datasetNamesMap.put(ModelConstant.ModelNames.SANGUO_EVENT.get(), ModelConstant.DatasetNames.SANGUO_EVENT.get());
        datasetNamesMap.put(ModelConstant.ModelNames.SANGUO_FIGURE.get(), ModelConstant.DatasetNames.SANGUO_FIGURE.get());
        datasetNamesMap.put(ModelConstant.ModelNames.SANGUO_LOCATION.get(), ModelConstant.DatasetNames.SANGUO_LOCATION.get());
        datasetNamesMap.put(ModelConstant.ModelNames.SANGUO_TIME.get(), ModelConstant.DatasetNames.SANGUO_TIME.get());
        return datasetNamesMap;
    }

}