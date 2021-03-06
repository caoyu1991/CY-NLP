package com.dao;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import com.constants.SearchConstant;

public class ModelDao {

    private final Logger logger = LogManager.getLogger();

    private final Map<String, String> datasetNameMap;

    public ModelDao(Map<String, String> datasetNameMap) {
        this.datasetNameMap = datasetNameMap;
    }

    public boolean testModel(String modelName) {
        try {
            Dataset dataset = TDBFactory.createDataset(datasetNameMap.get(modelName));
        } catch (Exception e) {
            logger.error("Cannot read the data file = {}, modelName = {}", datasetNameMap.get(modelName), modelName);
            return false;
        }
        return true;
    }

    // Save model obtained from owl file into local TDB files
    public boolean saveModel(Model model, String modelName, boolean isOverride) {
        Dataset dataset = TDBFactory.createDataset(datasetNameMap.get(modelName));
        Model dataModel = null;
        dataset.begin(ReadWrite.WRITE);
        boolean success = true;
        try {
            if (dataset.containsNamedModel(modelName) && !isOverride) {
                logger.info("Model has existed and will not be overridden.");
            } else {
                if (dataset.containsNamedModel(modelName) && isOverride) {
                        logger.info("Override an existed model = {}", modelName);
                } else {
                    logger.info("Create a new model = {}", modelName);
                }
                dataset.removeNamedModel(modelName);
                dataModel = dataset.getNamedModel(modelName);
                dataModel.add(model);
                logger.info("Save model successfully.");
            }
            dataset.commit();
        } catch (Exception e) {
            logger.error(e.toString());
            success = false;
        } finally {
            dataset.end();
        }
        return success;
    }

    //get all attribute from a given id of a specific type of data (e.g. event, figure...)
    public Map<Resource, Object> queryById(String modelName, String Id) {
        //todo
        return null;
    }

    //actual execute the query process and return the result
    public ResultSet queryModel(String modelName, String queryString) {
        Dataset dataset = TDBFactory.createDataset(datasetNameMap.get(modelName));
        Model dataModel = dataset.getNamedModel(modelName);
        QueryExecution query = QueryExecutionFactory.create(queryString, dataModel);
        dataset.begin(ReadWrite.READ);
        ResultSet result = query.execSelect();
        dataset.close();
        return result;
    }
}
