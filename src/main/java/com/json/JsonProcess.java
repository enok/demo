package com.json;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.WriteContext;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

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
                rootNode = processNode(value, writer, rootNode, currentPath, iterator, currentNode);
                currentPath += "." + currentNode;
            }
        }
        return JsonPath.parse(writer.jsonString()).jsonString();
    }

    private static Object processNode(Object value, WriteContext writer, Object rootNode, String currentPath, Iterator<String> iterator, String currentNode) {
        if (rootNode == null) {
            processNullNode(value, writer, currentPath, currentNode);
        }
        else if (rootNode instanceof LinkedHashMap) {
            rootNode = processNodeAsLinkedHashMap(value, writer, rootNode, currentPath, iterator, currentNode);
        }
        else if (rootNode instanceof List) {
            rootNode = processNodeAsList(value, writer, rootNode, currentPath, iterator, currentNode);
        }
        return rootNode;
    }

    private static void processNullNode(Object value, WriteContext writer, String currentPath, String currentNode) {
        LinkedHashMap innerNode = new LinkedHashMap();
        innerNode.put(currentNode, value);

        String previousNode = getLastNode(currentPath);

        if (isList(previousNode)) {
            setNewNodeIntoJSONArray(writer, currentPath, innerNode);
        }
        else {
            setNewNodeAsLinkedHasMap(writer, currentPath, innerNode);
        }
    }

    private static void setNewNodeIntoJSONArray(WriteContext writer, String currentPath, LinkedHashMap innerNode) {
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(innerNode);
        writer.set(removeBrackets(currentPath), jsonArray);
    }

    private static void setNewNodeAsLinkedHasMap(WriteContext writer, String currentPath, LinkedHashMap innerNode) {
        writer.set(removeBrackets(currentPath), innerNode);
    }

    private static Object processNodeAsLinkedHashMap(Object value, WriteContext writer, Object rootNode, String currentPath, Iterator<String> iterator, String currentNode) {
        LinkedHashMap innerNode = (LinkedHashMap) rootNode;
        Object innerObject = innerNode.get(removeBrackets(currentNode));

        if (nodeDoesNotExist(innerObject)) {
            rootNode = processNotExistingLinkedHashMapNode(value, writer, rootNode, currentPath, iterator, currentNode, innerNode);
        }
        else {
            rootNode = processExistingHashMapNode(value, writer, rootNode, currentPath, currentNode, innerNode, innerObject);
        }
        return rootNode;
    }

    private static Object processNotExistingLinkedHashMapNode(Object value, WriteContext writer, Object rootNode, String currentPath, Iterator<String> iterator, String currentNode, LinkedHashMap innerNode) {
        if (nodeIsLeaf(iterator)) {
            processHashMapNodeAsLeaf(value, currentNode, innerNode);
        }
        else {
            rootNode = processHashMapNodeAsNotLeaf(writer, currentPath, currentNode, innerNode);
        }
        if (!isList(currentNode)) {
            writer.set(currentPath, innerNode);
        }
        return rootNode;
    }

    private static void processHashMapNodeAsLeaf(Object value, String currentNode, LinkedHashMap innerNode) {
        innerNode.put(currentNode, value);
    }

    private static Object processHashMapNodeAsNotLeaf(WriteContext writer, String currentPath, String currentNode, LinkedHashMap innerNode) {
        if (isList(currentNode)) {
            List jsonList = new ArrayList();
            innerNode.put(removeBrackets(currentNode), jsonList);
            putNodeAsList(writer, currentPath, currentNode, jsonList);
        }
        else {
            innerNode.put(currentNode, new LinkedHashMap());
        }
        return innerNode.get(removeBrackets(currentNode));
    }

    private static void putNodeAsList(WriteContext writer, String currentPath, String currentNode, List jsonList) {
        writer.put(currentPath, removeBrackets(currentNode), jsonList);
    }

    private static Object processExistingHashMapNode(Object value, WriteContext writer, Object rootNode, String currentPath, String currentNode, LinkedHashMap innerNode, Object innerObject) {
        if (innerObject instanceof LinkedHashMap) {
            rootNode = processExistingHashMapNodeInnerObjectAsLinkedHasMap(innerObject);
        }
        else if (innerObject instanceof List) {
            rootNode = processExistingHashMapNodeInnerObjectAsList(currentNode, innerObject);
        }
        else if (innerObject instanceof Object) {
            processExistingHashMapNodeInnerObjectAsObject(value, writer, currentPath, currentNode, innerNode);
        }
        return rootNode;
    }

    private static Object processExistingHashMapNodeInnerObjectAsLinkedHasMap(Object innerObject) {
        return innerObject;
    }

    private static Object processExistingHashMapNodeInnerObjectAsList(String currentNode, Object innerObject) {
        Object rootNode;
        List jsonList = (List) innerObject;
        int index = getIndex(currentNode);
        if (index < jsonList.size()) {
            rootNode = jsonList.get(index);
        }
        else {
            rootNode = innerObject;
        }
        return rootNode;
    }

    private static void processExistingHashMapNodeInnerObjectAsObject(Object value, WriteContext writer, String currentPath, String currentNode, LinkedHashMap innerNode) {
        innerNode.put(currentNode, value);
        writer.set(currentPath, innerNode);
    }

    private static Object processNodeAsList(Object value, WriteContext writer, Object rootNode, String currentPath, Iterator<String> iterator, String currentNode) {
        List<Object> innerList = (List) rootNode;
        if (emptyList(innerList)) {
            rootNode = processEmptyList(value, writer, rootNode, currentPath, iterator, currentNode, innerList);
        }
        else {
            rootNode = getObjectFromList(value, writer, currentPath, currentNode, innerList, iterator);
        }
        return rootNode;
    }

    private static Object processEmptyList(Object value, WriteContext writer, Object rootNode, String currentPath, Iterator<String> iterator, String currentNode, List<Object> innerList) {
        if (!isList(currentNode)) {
            if (nodeIsLeaf(iterator)) {
                addObjectIntoList(value, currentNode, innerList);
            }
            else {
                LinkedHashMap innerNode = new LinkedHashMap();
                innerNode.put(currentNode, value);
                innerList.add(innerNode);
                rootNode = innerList;
            }
            writer.set(removeBrackets(currentPath), innerList);
        }
        return rootNode;
    }

    private static void addObjectIntoList(Object value, String currentNode, List<Object> innerList) {
        JSONObject object = new JSONObject();
        object.put(currentNode, value);
        innerList.add(object);
    }

    private static Object getObjectFromList(Object value, WriteContext writer, String currentPath, String currentNode, List<Object> innerList, Iterator<String> iterator) {
        for (Object object : innerList) {
            if (object instanceof LinkedHashMap) {
                LinkedHashMap linkedHashMap = (LinkedHashMap) object;
                if (nodeIsLeaf(iterator)) {
                    processNodeAsLeaf(value, writer, currentPath, currentNode, innerList, linkedHashMap);
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
                addObjectIntoList(value, currentNode, innerList);
                writer.set(removeBrackets(currentPath), innerList);
            }
        }
        return null;
    }

    private static void processNodeAsLeaf(Object value, WriteContext writer, String currentPath, String currentNode, List<Object> innerList, LinkedHashMap linkedHashMap) {
        JSONObject innerObject = new JSONObject();
        innerObject.put(currentNode, value);

        String innerCurrentNode = getLastNode(currentPath);
        if (isList(innerCurrentNode)) {
            processCurrentNodeAslist(value, currentNode, innerList, innerObject, innerCurrentNode);
        }
        else {
            linkedHashMap.put(currentNode, value);
        }

        writer.set(removeBrackets(currentPath), innerList);
    }

    private static void processCurrentNodeAslist(Object value, String currentNode, List<Object> innerList, JSONObject innerObject, String innerCurrentNode) {
        int index = getIndex(innerCurrentNode);
        if (innerCurrentNodeBelongsToInnerList(innerList, index)) {
            updateNodeValue(value, currentNode, innerList, index);
        }
        else {
            addInnerObjectAsNewElementIntoInnerList(innerObject, innerList);
        }
    }

    private static void updateNodeValue(Object value, String currentNode, List<Object> innerList, int index) {
        Object innerObjectFromList = innerList.get(index);
        if (innerObjectFromList != null) {
            if (innerObjectFromList instanceof LinkedHashMap) {
                LinkedHashMap finalLinkedHasMap = (LinkedHashMap) innerObjectFromList;
                finalLinkedHasMap.put(currentNode, value);
            }
        }
    }

    private static void addInnerObjectAsNewElementIntoInnerList(JSONObject innerObject, List<Object> innerList) {
        LinkedHashMap innerLinkedHashMap = (LinkedHashMap) innerList.get(0);
        innerList.remove(0);
        Set<Map.Entry<String, Object>> entries = innerLinkedHashMap.entrySet();

        for (Map.Entry<String, Object> entry : entries) {
            addObjectIntoList(entry.getValue(), entry.getKey(), innerList);
        }
        innerList.add(innerObject);
    }

    private static boolean innerCurrentNodeBelongsToInnerList(List<Object> innerList, int index) {
        return innerList.size() > index;
    }

    private static void removeDolarSign(List<String> pathNodes) {
        pathNodes.remove(0);
    }

    private static boolean nodeDoesNotExist(Object nodeObject) {
        return nodeObject == null;
    }

    private static String removeBrackets(String currentNode) {
        String lastNode = getLastNode(currentNode).replaceAll("\\[\\d\\]", "");
        String innerCurrentNode = removeLastNode(currentNode);
        if (!StringUtils.isEmpty(innerCurrentNode)) {
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

    private static String getLastNode(String path) {
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
}
