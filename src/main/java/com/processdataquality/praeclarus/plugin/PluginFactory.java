package com.processdataquality.praeclarus.plugin;


import com.processdataquality.praeclarus.annotations.PluginMetaData;
import com.processdataquality.praeclarus.config.PluginConfig;
import com.processdataquality.praeclarus.writer.DataWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 14/4/21
 */
public class PluginFactory<T> {

    private final Map<String, Class<T>> _classMap;
    
    public PluginFactory(Class<T> type) {
        _classMap = buildMap(type);
    }


    public List<String> getPluginNames() {
        return new ArrayList<>(_classMap.keySet());
    }


    public List<PluginMetaData> getMetaDataList() {
        List<PluginMetaData> list = new ArrayList<>();
        for (Class<?> clazz : _classMap.values()) {
            PluginMetaData metaData = clazz.getAnnotation(PluginMetaData.class);
            if (metaData != null) {
                list.add(metaData);
            }
        }
        return list;
    }


    public T newInstance(String className) {
        Class<T> clazz = _classMap.get(className);
        return newInstance(clazz);
    }


    private T newInstance(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        }
        catch (Throwable e) {
            return null;
        }
    }


    private Map<String, Class<T>> buildMap(Class<T> type) {
        try {
            PluginLoader loader = new PluginLoader(new PluginConfig().getPathList());
            return loader.loadAsMap(type);
        }
        catch (IOException e) {
            System.out.println("Failed to load plugins.");
            return Collections.emptyMap();
            //e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        PluginFactory<DataWriter> p = new PluginFactory<>(DataWriter.class);
        List<String> list = p.getPluginNames();
        for (String n : list) {
            System.out.println(n);
        }
        DataWriter d = p.newInstance(list.get(0));
        for (Map.Entry<String, Object> e : d.getOptions().entrySet()) {
            System.out.println(e.getKey() + " " + e.getValue());
        }
    }
}
