package com.example.demo;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.WriteContext;
import net.minidev.json.JSONArray;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * Created by enokj on 21/08/2018
 */
public class JsonProcess {

    public String setValueIntoJsonPath(Object value, String jsonTarget, String path) {
        List<String> pathNodes = new ArrayList<>(Arrays.asList(path.split("\\.")));

        if (!CollectionUtils.isEmpty(pathNodes)) {
            WriteContext writer = JsonPath.parse(jsonTarget);
            Object rootNode = JsonPath.read(jsonTarget, pathNodes.get(0));
            String currentPath = "$";
            removeDolarSign(pathNodes);

            Iterator<String> iterator = pathNodes.iterator();
            while (iterator.hasNext()) {
                String currentNode = iterator.next();

                if (rootNode instanceof LinkedHashMap) {
                    LinkedHashMap innerNode = (LinkedHashMap) rootNode;
                    Object innerObject = innerNode.get(currentNode);

                    if (nodeDoesNotExist(innerObject)) {
                        if (nodeIsLeaf(iterator)) {
                            innerNode.put(currentNode, value);
                        }
                        else {
                            innerNode.put(currentNode, new LinkedHashMap());
                            rootNode = innerNode.get(currentNode);
                        }
                        jsonTarget = writer.set(currentPath, innerNode).jsonString();
                    }
                    else {
                        if (innerObject instanceof LinkedHashMap) {
                            rootNode = innerObject;
                        }
                        else if (innerObject instanceof JSONArray) {
                        }
                        else if (innerObject instanceof Object) {
                            innerNode.put(currentNode, value);
                            jsonTarget = writer.set(currentPath, innerNode).jsonString();
                        }
                    }
                }
                currentPath += "." + currentNode;
            }
        }

        return JsonPath.parse(jsonTarget).jsonString();
    }

    private void removeDolarSign(List<String> pathNodes) {
        pathNodes.remove(0);
    }

    private boolean nodeDoesNotExist(Object nodeObject) {
        return nodeObject == null;
    }

    private boolean nodeIsLeaf(Iterator<String> iterator) {
        return !iterator.hasNext();
    }
}
