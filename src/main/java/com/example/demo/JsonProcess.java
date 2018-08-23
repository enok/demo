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
                    rootNode = gotToRootNodeLinkedHasMap(value, writer, rootNode, currentPath, iterator, currentNode);
                }
                else if (rootNode instanceof JSONArray) {
                    rootNode = gotToRootNodeJSONArray(value, writer, rootNode, currentPath, iterator, currentNode);
                }
                currentPath += "." + currentNode;
            }
        }

        return JsonPath.parse(writer.jsonString()).jsonString();
    }

    private static Object gotToRootNodeLinkedHasMap(Object value, WriteContext writer, Object rootNode, String currentPath, Iterator<String> iterator, String currentNode) {
        LinkedHashMap innerNode = (LinkedHashMap) rootNode;
        Object innerObject = innerNode.get(removeBrackets(currentNode));

        if (nodeDoesNotExist(innerObject)) {
            rootNode = gotToNonExistingNode(value, writer, rootNode, currentPath, iterator, currentNode, innerNode);
        }
        else {
            rootNode = gotToExistingNode(value, writer, rootNode, currentPath, currentNode, innerNode, innerObject);
        }
        return rootNode;
    }

    private static Object gotToNonExistingNode(Object value, WriteContext writer, Object rootNode, String currentPath, Iterator<String> iterator, String currentNode, LinkedHashMap innerNode) {
        if (nodeIsLeaf(iterator)) {
            innerNode.put(currentNode, value);
        }
        else {
            rootNode = gotToNodeThatIsNotLeaf(writer, currentPath, currentNode, innerNode);
        }
        if (!isArray(currentNode)) {
            writer.set(currentPath, innerNode);
        }
        return rootNode;
    }

    private static Object gotToNodeThatIsNotLeaf(WriteContext writer, String currentPath, String currentNode, LinkedHashMap innerNode) {
        Object rootNode;
        if (isArray(currentNode)) {
            JSONArray jsonArray = new JSONArray();
            innerNode.put(removeBrackets(currentNode), jsonArray);
            writer.put(currentPath, removeBrackets(currentNode), jsonArray);
        }
        else {
            innerNode.put(currentNode, new LinkedHashMap());
        }
        rootNode = innerNode.get(removeBrackets(currentNode));
        return rootNode;
    }

    private static Object gotToExistingNode(Object value, WriteContext writer, Object rootNode, String currentPath, String currentNode, LinkedHashMap innerNode, Object innerObject) {
        if (innerObject instanceof LinkedHashMap) {
            rootNode = gotToExistingNodeLinkedHasMap(innerObject);
        }
        else if (innerObject instanceof JSONArray) {
            rootNode = gotToExistingNodeJSONArray(currentNode, innerObject);
        }
        else if (innerObject instanceof Object) {
            gotToExistingNodeObject(value, writer, currentPath, currentNode, innerNode);
        }
        return rootNode;
    }

    private static Object gotToExistingNodeLinkedHasMap(Object innerObject) {
        return innerObject;
    }

    private static Object gotToExistingNodeJSONArray(String currentNode, Object innerObject) {
        JSONArray jsonArray = (JSONArray) innerObject;
        int index = getIndex(currentNode);
        Object object;
        if (index < jsonArray.size()) {
            object = jsonArray.get(index);
        }
        else {
            object = innerObject;
        }
        return object;
    }

    private static void gotToExistingNodeObject(Object value, WriteContext writer, String currentPath, String currentNode, LinkedHashMap innerNode) {
        innerNode.put(currentNode, value);
        writer.set(currentPath, innerNode);
    }

    private static Object gotToRootNodeJSONArray(Object value, WriteContext writer, Object rootNode, String currentPath, Iterator<String> iterator, String currentNode) {
        JSONArray innerArray = (JSONArray) rootNode;
        if (emptyArray(innerArray)) {
            rootNode = gotToEmptyArray(value, rootNode, iterator, currentNode, innerArray);
        }
        else {
            gotToNonEmptyArray(value, currentNode, innerArray);
        }
        writer.set(removeBrackets(currentPath), innerArray);
        return rootNode;
    }

    private static Object gotToEmptyArray(Object value, Object rootNode, Iterator<String> iterator, String currentNode, JSONArray innerArray) {
        if (!isArray(currentNode)) {
            if (nodeIsLeaf(iterator)) {
                gotToEmptyArrayNodeLeaf(value, currentNode, innerArray);
            }
            else {
                rootNode = gotToEmptyArrayNonNodeLeaf(value, currentNode, innerArray);
            }
        }
        return rootNode;
    }

    private static void gotToEmptyArrayNodeLeaf(Object value, String currentNode, JSONArray innerArray) {
        JSONObject object = new JSONObject();
        object.put(currentNode, value);
        innerArray.add(object);
    }

    private static Object gotToEmptyArrayNonNodeLeaf(Object value, String currentNode, JSONArray innerArray) {
        LinkedHashMap innerNode = new LinkedHashMap();
        innerNode.put(currentNode, value);
        innerArray.add(innerNode);
        return innerArray;
    }

    private static void gotToNonEmptyArray(Object value, String currentNode, JSONArray innerArray) {
        JSONObject object = new JSONObject();
        object.put(currentNode, value);
        innerArray.add(object);
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

    private static boolean emptyArray(JSONArray innerNode) {
        return innerNode.size() == 0;
    }
}
