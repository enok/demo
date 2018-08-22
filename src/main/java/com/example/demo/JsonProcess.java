package com.example.demo;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.WriteContext;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * Created by enokj on 21/08/2018
 */
public class JsonProcess {

    public static String setValueIntoJsonPath(String jsonTarget, String path, Object value) {
        List<String> pathNodes = new ArrayList<>(Arrays.asList(path.split("\\.")));
        WriteContext writer = JsonPath.parse(jsonTarget);

        if (!CollectionUtils.isEmpty(pathNodes)) {
            Object rootNode = JsonPath.read(jsonTarget, pathNodes.get(0));
            String currentPath = "$";
            removeDolarSign(pathNodes);

            Iterator<String> iterator = pathNodes.iterator();
            while (iterator.hasNext()) {
                String currentNode = iterator.next();

                if (rootNode instanceof LinkedHashMap) {
                    LinkedHashMap innerNode = (LinkedHashMap) rootNode;
                    Object innerObject = innerNode.get(removeBrackets(currentNode));

                    if (nodeDoesNotExist(innerObject)) {
                        if (nodeIsLeaf(iterator)) {
                            innerNode.put(currentNode, value);
                        }
                        else {
                            if (isArray(currentNode)) {
                                JSONArray jsonArray = new JSONArray();
                                innerNode.put(removeBrackets(currentNode), jsonArray);
                                writer.put(currentPath, removeBrackets(currentNode), jsonArray);
                            }
                            else {
                                innerNode.put(currentNode, new LinkedHashMap());
                            }
                            rootNode = innerNode.get(removeBrackets(currentNode));
                        }
                        if (!isArray(currentNode)) {
                            writer.set(currentPath, innerNode);
                        }
                    }
                    else {
                        if (innerObject instanceof LinkedHashMap) {
                            rootNode = innerObject;
                        }
                        else if (innerObject instanceof JSONArray) {
                            JSONArray jsonArray = (JSONArray) innerObject;
                            Object object = jsonArray.get(getIndex(currentNode));
                            rootNode = object;
                        }
                        else if (innerObject instanceof Object) {
                            innerNode.put(currentNode, value);
                            writer.set(currentPath, innerNode);
                        }
                    }
                }
                else if (rootNode instanceof JSONArray) {
                    JSONArray innerArray = (JSONArray) rootNode;
                    if (emptyArray(innerArray)) {
                        if (!isArray(currentNode)) {
                            if (nodeIsLeaf(iterator)) {
                                JSONObject object = new JSONObject();
                                object.put(currentNode, value);
                                innerArray.add(object);
                            }
                            else {
                                LinkedHashMap innerNode = new LinkedHashMap();
                                innerNode.put(currentNode, value);
                                innerArray.add(innerNode);
                                rootNode = innerArray;
                            }
                            writer.set(removeBrackets(currentPath), innerArray);
                        }
                    }
                }
                currentPath += "." + currentNode;
            }
        }

        return JsonPath.parse(writer.jsonString()).jsonString();
    }

    private static void removeDolarSign(List<String> pathNodes) {
        pathNodes.remove(0);
    }

    private static boolean nodeDoesNotExist(Object nodeObject) {
        return nodeObject == null;
    }

    private static String removeBrackets(String currentNode) {
        return currentNode.replaceAll("\\[\\d\\]", "");
    }

    private static boolean nodeIsLeaf(Iterator<String> iterator) {
        return !iterator.hasNext();
    }

    private static boolean isArray(String node) {
        return node.contains("[");
    }

    private static int getIndex(String currentNode) {
        String numberString = currentNode.replaceAll("[^\\d]", "");
        return Integer.parseInt(numberString);
    }

    private static String getFather(String path) {
        List<String> nodes = Arrays.asList(path.split("\\."));
        return nodes.get(nodes.size() - 1);
    }

    private static boolean emptyArray(JSONArray innerNode) {
        return innerNode.size() == 0;
    }
}
