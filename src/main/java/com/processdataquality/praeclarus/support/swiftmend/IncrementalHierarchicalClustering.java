package com.processdataquality.praeclarus.support.swiftmend;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import com.processdataquality.praeclarus.pattern.SwiftMendDetectRepair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IncrementalHierarchicalClustering {

    private static final Logger logger = LogManager.getLogger(IncrementalHierarchicalClustering.class);
    private final Map<String, List<Pair<String, Float>>> adjacencyList;
    private final float similarityThreshold;
    Map<String, String> activityToCluster;
    private SwiftMendDetectRepair pluginInstance = null;
    private Map<String, List<String>> clusters;
    private HashMap<String, Integer> activityCount = new HashMap<>();
    private HashMap<String, String> activityTimestamp = new HashMap<>();


    public IncrementalHierarchicalClustering(Map<String, List<Pair<String, Float>>> adjacencyList, float similarityThreshold, SwiftMendDetectRepair swiftMendDetectRepair) {
        this.adjacencyList = adjacencyList;
        this.similarityThreshold = similarityThreshold;
        this.pluginInstance = swiftMendDetectRepair;
        this.activityToCluster = new HashMap<>();
        this.clusters = new HashMap<>();
        initializeClusters();
    }

    public Map<String, List<Pair<String, Float>>> getAdjacencyList() {
        return adjacencyList;
    }

    public Map<String, List<String>> getClusters() {
        return clusters;
    }

    public float getSimilarityThreshold() {
        return similarityThreshold;
    }

    public Map<String, String> getActivityToCluster() {
        return activityToCluster;
    }

    private void initializeClusters() {
        for (String activity : adjacencyList.keySet()) {
            activityToCluster.put(activity, activity); // Initially, each activity is its own cluster
        }
    }

    public void clusterActivities() {
        boolean merged;
        do {
            merged = false;
            for (String activity : adjacencyList.keySet()) {
                for (Pair<String, Float> pair : adjacencyList.get(activity)) {
                    if (pair.second >= similarityThreshold) {
                        String cluster1 = findCluster(activity);
                        String cluster2 = findCluster(pair.first);
                        if (cluster1 != null && cluster2 != null && !cluster1.equals(cluster2)) {
                            if (allMembersSimilar(cluster1, cluster2)) {
                                mergeClusters(cluster1, cluster2);
                                merged = true;
                            }
                        }
                    }
                }
            }
        } while (merged);
    }

    private boolean allMembersSimilar(String cluster1, String cluster2) {
        List<String> members1 = clusters.get(cluster1);
        List<String> members2 = clusters.get(cluster2);
        for (String member1 : members1) {
            for (String member2 : members2) {
                float similarity = getSimilarity(member1, member2);
                if (similarity < similarityThreshold) {
                    return false;
                }
            }
        }
        return true;
    }

    private float getSimilarity(String activity1, String activity2) {
        if (!adjacencyList.containsKey(activity1)) {
            return 0;
        }
        List<Pair<String, Float>> pairs = adjacencyList.get(activity1);
        for (Pair<String, Float> pair : pairs) {
            if (pair.first.equals(activity2)) {
                return pair.second;
            }
        }
        return 0;
    }

    private void mergeClusters(String cluster1, String cluster2) {
        // System.out.println("Merged clusters");
        List<String> members1 = clusters.get(cluster1);
        List<String> members2 = clusters.get(cluster2);
        List<String> newCluster = new ArrayList<>(members1);
        newCluster.addAll(members2);
        for (String member : newCluster) {
            if (!allMembersSimilar(member, newCluster)) {
                newCluster.remove(member);
                clusters.put(member, Collections.singletonList(member));
            } else {
                activityToCluster.put(member, cluster1);
            }
        }
        clusters.put(cluster1, newCluster);
        reassessMembers();
        reassessRepresentatives(this.activityCount, this.activityTimestamp);
    }



    private boolean allMembersSimilar(String member, List<String> cluster) {
        for (String otherMember : cluster) {
            if (!otherMember.equals(member)) {
                float similarity = getSimilarity(member, otherMember);
                if (similarity < similarityThreshold) {
                    return false;
                }
            }
        }
        return true;
    }
    private String findCluster(String activity) {
        return activityToCluster.getOrDefault(activity, null);
    }


    public void addSimilarity(String activity1, String activity2, float similarity, HashMap<String, Integer> activityCount, HashMap<String, String> activityTimestamp) {
        if (!activityToCluster.containsKey(activity1)) {
            activityToCluster.put(activity1, activity1);
        }
        if (!activityToCluster.containsKey(activity2)) {
            activityToCluster.put(activity2, activity2);
        }

        updateAdjacencyList(activity1, activity2, similarity);
        updateAdjacencyList(activity2, activity1, similarity);

        reassessMembers();
        this.activityCount = activityCount;
        this.activityTimestamp = activityTimestamp;
        reassessRepresentatives(this.activityCount, this.activityTimestamp);
    }

    private void removePairIfExists(String activity1, String activity2) {
        List<Pair<String, Float>> pairs = adjacencyList.getOrDefault(activity1, new ArrayList<>());
        pairs.removeIf(pair -> pair.first.equals(activity2));
        if (pairs.isEmpty()) {
            adjacencyList.remove(activity1);
            activityToCluster.remove(activity1);
        }
    }

    private void updateAdjacencyList(String activity1, String activity2, float similarity) {
        adjacencyList.putIfAbsent(activity1, new ArrayList<>());
        List<Pair<String, Float>> pairs = new ArrayList<>(adjacencyList.get(activity1));
//      pairs.removeIf(pair -> pair.first.equals(activity2));
        if (similarity >= similarityThreshold) {
            pairs.add(new Pair<>(activity2, similarity));
            adjacencyList.put(activity1, pairs);
        }
        else{
            removePairIfExists(activity1, activity2);
        }

    }

    public void reassessRepresentatives(Map<String, Integer> activityCount, HashMap<String, String> activityTimestamp) {
        Map<String, String> newRepresentatives = new HashMap<>();
        for (Map.Entry<String, String> entry : activityToCluster.entrySet()) {
            String currentCluster = entry.getValue();
            newRepresentatives.putIfAbsent(currentCluster, findNewRepresentativeForCluster(currentCluster, activityCount, activityTimestamp));
        }

        for (Map.Entry<String, String> entry : activityToCluster.entrySet()) {
            String newRepresentative = newRepresentatives.get(entry.getValue());
            activityToCluster.put(entry.getKey(), newRepresentative);
        }

        Map<String, List<String>> updatedClusters = new HashMap<>();
        // Incrementally update clusters based on the updated activityToCluster.

        for (String activity : activityToCluster.keySet()) {
            String clusterRep = activityToCluster.get(activity);
            updatedClusters.computeIfAbsent(clusterRep, k -> new ArrayList<>()).add(activity);
        }

        this.clusters = updatedClusters;
    }

    private String findNewRepresentativeForCluster(String cluster, Map<String, Integer> activityCount, HashMap<String, String> activityTimestamp) {
        return activityToCluster.entrySet().stream()
                .filter(entry -> entry.getValue().equals(cluster))
                .max((entry1, entry2) -> {
                    int countComparison = Integer.compare(activityCount.getOrDefault(entry1.getKey(), 0), activityCount.getOrDefault(entry2.getKey(), 0));
                    if (countComparison != 0) {
                        return countComparison;
                    } else {
                        LocalDateTime time1 = pluginInstance.textToTime(activityTimestamp.get(entry1.getKey()));
                        LocalDateTime time2 = pluginInstance.textToTime(activityTimestamp.get(entry2.getKey()));
                        if (time2.compareTo(time1) > 0) {
                            return 1;  // Favor the more recent timestamp
                        } else if (time2.compareTo(time1) < 0) {
                            return -1; // Favor the older timestamp
                        } else {
                            return 0;  // Times are the same, no preference given
                        }
                    }
                })
                .map(Map.Entry::getKey)
                .orElse(cluster); // Default to existing cluster if no clear new representative found
    }

    private LocalDateTime parseTimestamp(String timestamp, List<DateTimeFormatter> formatters) {
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDateTime.parse(timestamp, formatter);
            } catch (DateTimeParseException e) {
                // Ignore and try the next formatter
            }
        }
        throw new IllegalArgumentException("Timestamp " + timestamp + " does not match any known patterns");
    }


    private void reassessMembers() {
        for (String cluster : new ArrayList<>(clusters.keySet())) {
            List<String> members = new ArrayList<>(clusters.get(cluster));
            List<String> membersToRemove = new ArrayList<>();
            for (String member : members) {
                if (!allMembersSimilar(member, members)) {
                    membersToRemove.add(member);
                    clusters.put(member, Collections.singletonList(member));
                    activityToCluster.put(member, member);
                    updateAdjacencyListForRemovedMember(member, members);
                }
            }
            members.removeAll(membersToRemove);
            clusters.put(cluster, members);
        }
    }

    private void updateAdjacencyListForRemovedMember(String member, List<String> members) {
        for (String otherMember : members) {
            if (!otherMember.equals(member)) {
                float similarity = getSimilarity(member, otherMember);
                if (similarity < similarityThreshold) {
                    removePairIfExists(member, otherMember);
                    removePairIfExists(otherMember, member);
                }
            }
        }
    }
}