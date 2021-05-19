/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package com.processdataquality.praeclarus.plugin;

import com.processdataquality.praeclarus.annotations.PluginMetaData;
import com.processdataquality.praeclarus.writer.DataWriter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Loads plugins into the PDQF
 * @author Michael Adams
 * @date 25/05/12
 */
public class PluginLoader extends URLClassLoader {

    private final List<String> _pathList;

    /**
     * Constructor
     * @param searchPath the path(s) to search for jar and class files. Multiple paths
     *                   can be specified, separated by semi-colons (;). Any
     *                   sub-directories are also searched
     */
    protected PluginLoader(String searchPath) {
        super(new URL[0], Thread.currentThread().getContextClassLoader());
        _pathList = setPath(searchPath);
    }


    /**
     * Constructor
     * @param pathList the list of path(s) to be searched for jar and class files.
     */
    protected PluginLoader(List<String> pathList) {
        super(new URL[0], Thread.currentThread().getContextClassLoader());
        _pathList = pathList != null ? pathList : Collections.emptyList();
    }


    /**
     * Loads uninstantiated instances of classes found in jar files in a path that
     * implement a specified interface
     * @param iMask The interface of the classes to load
     * @return a set of matching but uninitiated classes implementing the interface
     * @throws IOException if any problems reading the jar files
     */
    protected <T> Set<Class<T>> load(Class<T> iMask) throws IOException {
        Set<Class<T>> plugins = new HashSet<>();
        loadPackageMatches(iMask, plugins);   // load internal package classes

        // next add any external plugins of the same base class
        for (String path : _pathList) {
            if (! path.endsWith(File.separator)) path += File.separator;
            File f = new File(path);
            addURL(f.toURI().toURL());             // add plugin dir to search path
            for (File file : getFileSet(f)) {
                if (file.getName().endsWith(".jar")) {
                    searchJAR(iMask, plugins, file);
                }
                else if (file.getName().endsWith(".class")){
                    String fileName = file.getAbsolutePath().replace(path, "");
                    load(iMask, plugins, fileName);
                }
            }
        }
        return plugins;
    }


    protected <T> Map<String, Class<T>> loadAsMap(Class<T> mask) throws IOException {
        Map<String, Class<T>> map = new HashMap<>();
        for (Class<T> clazz : load(mask)) {
            PluginMetaData pmd = clazz.getAnnotation(PluginMetaData.class);
            map.put(pmd.name(), clazz);
        }
        return map;
    }

    /**
     * Searches a jar for classes that implement a specified interface, an loads an
     * instance of each.
     * @param iMask The interface of the classes to load
     * @param plugins the Set of plugins to add any matches to
     * @param jarFile The jar to search
     * @throws IOException if any problems reading the jar files
     */
    private <T> void searchJAR(Class<T> iMask, Set<Class<T>> plugins, File jarFile) throws IOException {
        addURL(jarFile.toURI().toURL());
        JarFile jar = new JarFile(jarFile);
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (! entry.isDirectory()) {
                load(iMask, plugins, entry.getName());
            }
        }
    }


    private <T> void load(Class<T> mask, Set<Class<T>> plugins, String fileName) {
         if (fileName.endsWith(".class")) {
             Class<T> plugin = load(mask, fileToQualifiedClassName(fileName));
             if (plugin != null) {
                 plugins.add(plugin);
             }
         }
     }


    @SuppressWarnings("unchecked")
    private <T> Class<T> load(Class<T> iMask, String name) {
        try {
            Class<?> c = loadClass(name);

            // filter interfaces & abstract classes before testing assignable
            if (!Modifier.isAbstract(c.getModifiers()) && iMask.isAssignableFrom(c)) {
                return (Class<T>) c;
            }
        }
        catch (Exception e) {
            // fall through to null
        }
        return null;
    }


    // loads the matching classes in the same package as 'mask'
    private <T> void loadPackageMatches(Class<T> mask, Set<Class<T>> plugins)
            throws IOException {
        String pkg = mask.getPackage().getName();
        String pkgPath = pkg.replace('.', '/');
        Enumeration<URL> e = getResources(pkgPath);
        while (e.hasMoreElements()) {
            URL url = e.nextElement();
            URLConnection connection = url.openConnection();
            if (connection instanceof JarURLConnection) {
                processJAR(mask, plugins, (JarURLConnection) connection);
            }
            else {    // single file or directory
                String filePath = URLDecoder.decode(url.getPath(), "UTF-8");
                String[] fileList = new File(filePath).list();
                if (fileList != null) {
                    for (String file : fileList) {
                        String fileName = new File(pkgPath, file).getPath();
                        load(mask, plugins, fileName);
                    }
                }
            }
        }
    }


    private <T> void processJAR(Class<T> mask, Set<Class<T>> plugins,
                                JarURLConnection connection) throws IOException {
        JarFile jar = connection.getJarFile();
        JarEntry baseEntry = connection.getJarEntry();
        String base = baseEntry.getName();
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (! entry.isDirectory() && entry.getName().startsWith(base)) {
                load(mask, plugins, entry.getName());
            }
        }
    }


    // transforms a path string to a qualified class name
    private String fileToQualifiedClassName(String path) {
        return path.replaceAll("[\\\\/]", ".").substring(0, path.lastIndexOf('.'));
    }


    // splits a path string on ';' to a list of paths (also fixes file separators)
    private List<String> setPath(String pathStr) {
        if (pathStr == null || pathStr.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(pathStr.replace('\\', '/').split(";"));
    }


    // walks the tree from 'dir' to build a set of files found (no dirs included)
    private Set<File> getFileSet(File dir) {
        Set<File> fileTree = new HashSet<File>();
        File[] entries = dir.listFiles();
        if (entries != null) {
            for (File entry : entries) {
                if (entry.isFile()) fileTree.add(entry);
                else fileTree.addAll(getFileSet(entry));
            }
        }
        return fileTree;
    }


    public static void main(String[] args) {
        PluginLoader loader = new PluginLoader((String)null);
        try {
            Set<Class<DataWriter>> s = loader.load(DataWriter.class);
            for (Class<DataWriter> d : s) {
                System.out.println(d.getName());
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}
