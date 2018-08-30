package com.json;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.WriteContext;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import java.util.*;

/**
 * Created by enokj on 21/08/2018
 */
public class JsonProcess {

    public static String setValueIntoJsonPath(String jsonTarget, String path, Object value) {
        List<String> pathNodes = new ArrayList<>(Arrays.asList(path.split("\\.")));
        WriteContext writer = JsonPath.parse(jsonTarget);

        if (!isEmpty(pathNodes)) {
            Object rootNode = JsonPath.read(jsonTarget, pathNodes.get(0));
            String previousFullPath = "$";
            removeDolarSign(pathNodes);

            Iterator<String> iterator = pathNodes.iterator();
            while (iterator.hasNext()) {
                String currentNodePath = iterator.next();

                if (rootNode == null) {
                    processCurrentNodeAsNewValue(previousFullPath, currentNodePath, value, writer);
                }
                else if (rootNode instanceof LinkedHashMap) {
                    rootNode = processCurrentNodeAsLinkedHashMap(previousFullPath, currentNodePath, value, rootNode, writer, iterator);
                }
                else if (rootNode instanceof List) {
                    rootNode = processCurrentNodeAsList(previousFullPath, currentNodePath, value, rootNode, writer, iterator);
                }
                previousFullPath += "." + currentNodePath;
            }
        }

        return JsonPath.parse(writer.jsonString()).jsonString();
    }

    private static void processCurrentNodeAsNewValue(String previousFullPath, String currentNodePath, Object value, WriteContext writer) {
        LinkedHashMap addedNode = addValueIntoLinkedHashMap(currentNodePath, value, new LinkedHashMap());

        String lastElement = getLastElementFromPath(previousFullPath);

        if (isList(lastElement)) {
            JSONArray jsonArray = addNewJSONArrayIntoLinkedHashMap(addedNode);
            writer.set(removeBrackets(previousFullPath), jsonArray);
        }
        else {
            writer.set(removeBrackets(previousFullPath), addedNode);
        }
    }

    private static Object processCurrentNodeAsLinkedHashMap(String previousFullPath, String currentNodePath, Object value, Object rootNode, WriteContext writer, Iterator<String> iterator) {
        LinkedHashMap rootNodeLinkedHashMap = (LinkedHashMap) rootNode;
        Object childNode = rootNodeLinkedHashMap.get(removeBrackets(currentNodePath));

        if (nodeDoesNotExist(childNode)) {
            rootNode = processChildNodeAsNew(previousFullPath, currentNodePath, value, rootNode, writer, iterator, rootNodeLinkedHashMap);
        }
        else {
            rootNode = processChildNodeAsExistent(previousFullPath, currentNodePath, value, rootNode, writer, rootNodeLinkedHashMap, childNode);
        }
        return rootNode;
    }

    private static Object processChildNodeAsNew(String previousFullPath, String currentNodePath, Object value, Object rootNode, WriteContext writer, Iterator<String> iterator, LinkedHashMap rootNodeLinkedHashMap) {
        if (nodeIsLeaf(iterator)) {
            processChildNodeAsNewAndLeaf(currentNodePath, value, rootNodeLinkedHashMap);
        }
        else {
            rootNode = processChildNodeAsNewAndBranch(previousFullPath, currentNodePath, writer, rootNodeLinkedHashMap);
        }
        if (currentNodePathIsNotAList(currentNodePath)) {
            writer.set(previousFullPath, rootNodeLinkedHashMap);
        }
        return rootNode;
    }

    private static void processChildNodeAsNewAndLeaf(String currentNodePath, Object value, LinkedHashMap rootNodeLinkedHashMap) {
        rootNodeLinkedHashMap.put(currentNodePath, value);
    }

    private static Object processChildNodeAsNewAndBranch(String previousFullPath, String currentNodePath, WriteContext writer, LinkedHashMap rootNodeLinkedHashMap) {
        if (isList(currentNodePath)) {
            List jsonList = addListIntoLinkedHashMap(currentNodePath, rootNodeLinkedHashMap, new ArrayList());
            writer.put(previousFullPath, removeBrackets(currentNodePath), jsonList);
        }
        else {
            rootNodeLinkedHashMap.put(currentNodePath, new LinkedHashMap());
        }
        return rootNodeLinkedHashMap.get(removeBrackets(currentNodePath));
    }

    private static boolean currentNodePathIsNotAList(String currentNodePath) {
        return !isList(currentNodePath);
    }

    private static Object processChildNodeAsExistent(String previousFullPath, String currentNodePath, Object value, Object rootNode, WriteContext writer, LinkedHashMap rootNodeLinkedHashMap, Object childNode) {
        if (childNode instanceof LinkedHashMap) {
            rootNode = processChildNodeAsExistentAndLinkedHashMap(childNode);
        }
        else if (childNode instanceof List) {
            rootNode = processChildNodeAsExistentAndList(currentNodePath, childNode);
        }
        else if (childNode instanceof Object) {
            processChildNodeAsExistentAndObject(previousFullPath, currentNodePath, value, writer, rootNodeLinkedHashMap);
        }
        return rootNode;
    }

    private static Object processChildNodeAsExistentAndLinkedHashMap(Object childNode) {
        return childNode;
    }

    private static Object processChildNodeAsExistentAndList(String currentNodePath, Object childNode) {
        Object rootNode;
        List jsonList = (List) childNode;
        int index = getIndex(currentNodePath);
        if (indexBelongsToList(index, jsonList)) {
            rootNode = jsonList.get(index);
        }
        else {
            rootNode = childNode;
        }
        return rootNode;
    }

    private static void processChildNodeAsExistentAndObject(String previousFullPath, String currentNodePath, Object value, WriteContext writer, LinkedHashMap rootNodeLinkedHashMap) {
        rootNodeLinkedHashMap.put(currentNodePath, value);
        writer.set(previousFullPath, rootNodeLinkedHashMap);
    }

    private static Object processCurrentNodeAsList(String previousFullPath, String currentNodePath, Object value, Object rootNode, WriteContext writer, Iterator<String> iterator) {
        List<Object> rootNodeList = (List) rootNode;
        if (emptyList(rootNodeList)) {
            rootNode = processRootNodeAsList(previousFullPath, currentNodePath, value, rootNode, writer, iterator, rootNodeList);
        }
        else {
            rootNode = getObjectFromList(value, writer, previousFullPath, currentNodePath, rootNodeList, iterator);
        }
        return rootNode;
    }

    private static Object processRootNodeAsList(String previousFullPath, String currentNodePath, Object value, Object rootNode, WriteContext writer, Iterator<String> iterator, List<Object> rootNodeList) {
        if (!isList(currentNodePath)) {
            if (nodeIsLeaf(iterator)) {
                addNewJSONObjectIntoList(rootNodeList, currentNodePath, value);
            }
            else {
                LinkedHashMap innerNode = addValueIntoLinkedHashMap(currentNodePath, value, new LinkedHashMap());
                rootNodeList.add(innerNode);
                rootNode = rootNodeList;
            }
            writer.set(removeBrackets(previousFullPath), rootNodeList);
        }
        return rootNode;
    }

    private static Object getObjectFromList(Object value, WriteContext writer, String currentPath, String currentNode, List<Object> innerList, Iterator<String> iterator) {
        for (Object object : innerList) {
            if (object instanceof LinkedHashMap) {
                LinkedHashMap linkedHashMap = (LinkedHashMap) object;
                if (nodeIsLeaf(iterator)) {
                    processObjectAsLeaf(value, writer, currentPath, currentNode, innerList, linkedHashMap);
                    break;
                }
                else {
                    return linkedHashMap.get(removeBrackets(currentNode));
                }
            }
            else if (object instanceof List) {
                return getObjectFromList(value, writer, currentPath, currentNode, (List) object, iterator);
            }
            else {
                addNewJSONObjectIntoList(innerList, currentNode, value);
                writer.set(removeBrackets(currentPath), innerList);
            }
        }
        return null;
    }

    private static void processObjectAsLeaf(Object value, WriteContext writer, String currentPath, String currentNode, List<Object> innerList, LinkedHashMap linkedHashMap) {
        JSONObject innerObject = new JSONObject();
        innerObject.put(currentNode, value);

        String innerCurrentNode = getLastElementFromPath(currentPath);
        if (isList(innerCurrentNode)) {
            processObjectAsLeafAndList(value, currentNode, innerList, innerObject, innerCurrentNode);
        }
        else {
            linkedHashMap.put(currentNode, value);
        }

        writer.set(removeBrackets(currentPath), innerList);
    }

    private static void processObjectAsLeafAndList(Object value, String currentNode, List<Object> innerList, JSONObject innerObject, String innerCurrentNode) {
        int index = getIndex(innerCurrentNode);
        if (indexBelongsToList(index, innerList)) {
            addValueIntoListNode(currentNode, value, index, innerList);
        }
        else {
            addValueIntoListNodeAsNewElement(innerList, innerObject);
        }
    }

    private static void addValueIntoListNodeAsNewElement(List<Object> innerList, JSONObject innerObject) {
        LinkedHashMap innerLinkedHashMap = (LinkedHashMap) innerList.get(0);
        innerList.remove(0);
        Set<Map.Entry<String, Object>> entries = innerLinkedHashMap.entrySet();

        for (Map.Entry<String, Object> entry : entries) {
            addNewJSONObjectIntoList(innerList, entry.getKey(), entry.getValue());
        }
        innerList.add(innerObject);
    }

    private static void addValueIntoListNode(String currentNode, Object value, int index, List<Object> innerList) {
        Object innerObjectFromList = innerList.get(index);
        if ((innerObjectFromList != null) && (innerObjectFromList instanceof LinkedHashMap)) {
            addValueIntoLinkedHashMap(currentNode, value, (LinkedHashMap) innerObjectFromList);
        }
    }

    private static boolean indexBelongsToList(int index, List<Object> list) {
        return index < list.size();
    }

    private static List addListIntoLinkedHashMap(String currentNode, LinkedHashMap innerNode, List jsonList) {
        innerNode.put(removeBrackets(currentNode), jsonList);
        return jsonList;
    }

    private static void addNewJSONObjectIntoList(List<Object> list, String key, Object value) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(key, value);
        list.add(jsonObject);
    }

    private static LinkedHashMap addValueIntoLinkedHashMap(String path, Object value, LinkedHashMap linkedHashMap) {
        LinkedHashMap finalLinkedHasMap = linkedHashMap;
        finalLinkedHasMap.put(path, value);
        return finalLinkedHasMap;
    }

    private static JSONArray addNewJSONArrayIntoLinkedHashMap(LinkedHashMap linkedHashMap) {
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(linkedHashMap);
        return jsonArray;
    }

    private static void removeDolarSign(List<String> pathNodes) {
        pathNodes.remove(0);
    }

    private static boolean nodeDoesNotExist(Object nodeObject) {
        return nodeObject == null;
    }

    private static String removeBrackets(String path) {
        String lastNode = getLastElementFromPath(path).replaceAll("\\[\\d\\]", "");
        String innerCurrentNode = removeLastNode(path);
        if (!isEmpty(innerCurrentNode)) {
            return innerCurrentNode.concat(".").concat(lastNode);
        }
        return lastNode;
    }

    private static boolean nodeIsLeaf(Iterator<String> iterator) {
        return !iterator.hasNext();
    }

    private static boolean isList(String node) {
        return node.contains("[");
    }

    private static int getIndex(String currentNode) {
        String numberString = currentNode.replaceAll("[^\\d]", "");
        return Integer.parseInt(numberString);
    }

    private static boolean emptyList(List innerNode) {
        int size = innerNode.size();

        if (size == 0) {
            return true;
        }
        else if (size == 1) {
            Object object = innerNode.get(0);
            if (object instanceof List) {
                List list = (List) object;
                return list.size() == 0;
            }
        }

        return false;
    }

    private static String getLastElementFromPath(String path) {
        List<String> list = Arrays.asList(path.split("\\."));
        return list.get(list.size() - 1);
    }

    private static String removeLastNode(String path) {
        int index = path.lastIndexOf(".");
        if (index >= 0) {
            return path.substring(0, index);
        }
        return null;
    }

    private static boolean isEmpty(Collection list) {
        return list == null || list.size() == 0;
    }

    private static boolean isEmpty(String value) {
        return value == null || "".equals(value);
    }
}
