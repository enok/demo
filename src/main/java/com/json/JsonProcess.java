package com.json;

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

                if (rootNode == null) {
                    LinkedHashMap innerNode = new LinkedHashMap();
                    innerNode.put(currentNode, value);

                    String previousNode = getLastNode(currentPath);

                    if (isList(previousNode)) {
                        JSONArray jsonArray = new JSONArray();
                        jsonArray.add(innerNode);
                        writer.set(removeBrackets(currentPath), jsonArray);
                    }
                    else {
                        writer.set(removeBrackets(currentPath), innerNode);
                    }
                }
                else if (rootNode instanceof LinkedHashMap) {
                    LinkedHashMap innerNode = (LinkedHashMap) rootNode;
                    Object innerObject = innerNode.get(removeBrackets(currentNode));

                    if (nodeDoesNotExist(innerObject)) {
                        if (nodeIsLeaf(iterator)) {
                            innerNode.put(currentNode, value);
                        }
                        else {
                            if (isList(currentNode)) {
                                List jsonList = new ArrayList();
                                innerNode.put(removeBrackets(currentNode), jsonList);
                                writer.put(currentPath, removeBrackets(currentNode), jsonList);
                            }
                            else {
                                innerNode.put(currentNode, new LinkedHashMap());
                            }
                            rootNode = innerNode.get(removeBrackets(currentNode));
                        }
                        if (!isList(currentNode)) {
                            writer.set(currentPath, innerNode);
                        }
                    }
                    else {
                        if (innerObject instanceof LinkedHashMap) {
                            rootNode = innerObject;
                        }
                        else if (innerObject instanceof List) {
                            List jsonList = (List) innerObject;
                            int index = getIndex(currentNode);
                            if (index < jsonList.size()) {
                                rootNode = jsonList.get(index);
                            }
                            else {
                                rootNode = innerObject;
                            }
                        }
                        else if (innerObject instanceof Object) {
                            innerNode.put(currentNode, value);
                            writer.set(currentPath, innerNode);
                        }
                    }
                }
                else if (rootNode instanceof List) {
                    List<Object> innerList = (List) rootNode;
                    if (emptyList(innerList)) {
                        if (!isList(currentNode)) {
                            if (nodeIsLeaf(iterator)) {
                                JSONObject object = new JSONObject();
                                object.put(currentNode, value);
                                innerList.add(object);
                            }
                            else {
                                LinkedHashMap innerNode = new LinkedHashMap();
                                innerNode.put(currentNode, value);
                                innerList.add(innerNode);
                                rootNode = innerList;
                            }
                            writer.set(removeBrackets(currentPath), innerList);
                        }
                    }
                    else {
                        rootNode = getObjectFromList(value, writer, currentPath, currentNode, innerList, iterator);
                    }
                }
                currentPath += "." + currentNode;
            }
        }

        return JsonPath.parse(writer.jsonString()).jsonString();
    }

    private static Object getObjectFromList(Object value, WriteContext writer, String currentPath, String currentNode, List<Object> innerList, Iterator<String> iterator) {
        for (Object object : innerList) {
            if (object instanceof LinkedHashMap) {
                LinkedHashMap linkedHashMap = (LinkedHashMap) object;
                if (nodeIsLeaf(iterator)) {
                    JSONObject innerObject = new JSONObject();
                    innerObject.put(currentNode, value);

                    String innerCurrentNode = getLastNode(currentPath);
                    if (isList(innerCurrentNode)) {
                        int index = getIndex(innerCurrentNode);
                        if (index < innerList.size()) {
                            Object innerObjectFromList = innerList.get(index);
                            if (innerObjectFromList != null) {
                                if (innerObjectFromList instanceof LinkedHashMap) {
                                    LinkedHashMap finalLinkedHasMap = (LinkedHashMap) innerObjectFromList;
                                    finalLinkedHasMap.put(currentNode, value);
                                }
                            }
                        } else {
                            LinkedHashMap innerLinkedHashMap = (LinkedHashMap) innerList.get(0);
                            innerList.remove(0);
                            Set<Map.Entry<String,Object>> entries = innerLinkedHashMap.entrySet();

                            for (Map.Entry<String,Object> entry : entries) {
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put(entry.getKey(), entry.getValue());
                                innerList.add(jsonObject);
                            }
                            innerList.add(innerObject);
                        }
                    }
                    else {
                        linkedHashMap.put(currentNode, value);
                    }

                    writer.set(removeBrackets(currentPath), innerList);
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
                JSONObject obj = new JSONObject();
                obj.put(currentNode, value);
                innerList.add(obj);
                writer.set(removeBrackets(currentPath), innerList);
            }
        }
        return null;
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
}
