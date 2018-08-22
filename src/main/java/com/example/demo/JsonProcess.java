package com.example.demo;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.WriteContext;

import java.util.*;

/**
 * Created by enokj on 21/08/2018
 */
public class JsonProcess {

    public String save(Object value, String jsonTarget, String path) {
        WriteContext writer = JsonPath.parse(jsonTarget);

        List<String> pathNodes = new ArrayList<>(Arrays.asList(path.split("\\.")));

        String lastPathNode = "";
        boolean deveParar = false;

        int i = 0;
        Iterator<String> iterator = pathNodes.iterator();
        while (iterator.hasNext()) {
            String node = iterator.next();
            lastPathNode += (i++ > 0 ? "." : "") + node;
            try {
                JsonPath.read(jsonTarget, lastPathNode);
            }
            catch (PathNotFoundException e) {
                deveParar = true;
                lastPathNode = removeUltimoNo(lastPathNode);
            }
            if (deveParar) {
                break;
            }
            iterator.remove();
        }
        if (pathNodes.size() == 0) {
            String pai = buscarPai(lastPathNode);
            Object read = JsonPath.read(jsonTarget, pai);

            if (read instanceof LinkedHashMap) {
                LinkedHashMap node = (LinkedHashMap) read;
                String filho = buscarFilho(lastPathNode);
                node.put(filho, value);
                return writer.set(pai, node).jsonString();
            }
        }
        else {
            for (String node : pathNodes) {
                lastPathNode += "." + node;

                String pai = buscarPai(lastPathNode);
                Object read = JsonPath.read(jsonTarget, pai);

                if (read instanceof LinkedHashMap) {
                    LinkedHashMap node2 = (LinkedHashMap) read;
                    String filho = buscarFilho(lastPathNode);
                    node2.put(filho, value);
                    writer.set(pai, node2).jsonString();
                }
            }
            return writer.jsonString();
        }

        return null;
    }

    private String buscarPai(String path) {
        return path.substring(0, path.lastIndexOf("."));
    }

    private String buscarFilho(String path) {
        return path.substring(path.lastIndexOf(".") + 1);
    }

    private String removeUltimoNo(String path) {
        return path.substring(0, path.lastIndexOf("."));
    }
}
