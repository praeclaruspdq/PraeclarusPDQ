package com.processdataquality.praeclarus.support.swiftmend;

import com.processdataquality.praeclarus.pattern.AbstractImperfectLabel;
import com.processdataquality.praeclarus.pattern.SwiftMendDetectRepair;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;

import java.util.*;
import java.time.LocalDateTime;

public class ControlFlowSimilarity extends SwiftMendDetectRepair {
    // Data structures for tracking activities and their indices
    private HashMap<String, HashMap<String, Integer>> activityIndices = new HashMap<>();
    private HashMap<String, Integer> activityToIndex = new HashMap<>();
    private HashMap<String, String> activityTimestamp = new HashMap<>();
    private HashMap<String, Integer> activityCount = new HashMap<>();
    private HashMap<String, String> lastActivityTimestamp = new HashMap<>();
    private HashMap<String, Integer> lastActivityCount = new HashMap<>();
    private int activityIndexCounter = 0;
    private HashMap<String, int[]> caseActivityIndices = new HashMap<>();
    private HashMap<String, Integer>[][] directlyFollows = new HashMap[1][1]; // Initialize with a default size
    private HashMap<ActivityPair, Float> similarityMap = new HashMap<>();
    private int[][] footprintMatrix = new int[1][1];
    private Map<String, List<String>> mergeSet = new HashMap<>();
    private Map<String, List<String>> previousMergeSet;
    private int cThreshold = 0;
    private double eventCounter = 0;
    private int[][] prevFootprint = new int[1][1];

    private double windowSize;
    private int delay;
    private float controlFlowSimThreshold;
    private float upperCausalityThreshold;
    private float upperParallelismThreshold;
    private float lowerCausalityThreshold;
    private float lowerParallelismThreshold;

    private Map<String, List<Pair<String, Float>>> adjacencyList = new HashMap<>();
    private Boolean clusteringHappen = false;
    private IncrementalHierarchicalClustering clustering;

    public ControlFlowSimilarity(double windowSize, int delay, float controlFlowSimThreshold, float upperCausalityThreshold, float upperParallelismThreshold, float lowerCausalityThreshold, float lowerParallelismThreshold) {
        this.windowSize = windowSize;
        this.delay = delay;
        this.controlFlowSimThreshold = controlFlowSimThreshold;
        this.upperCausalityThreshold = upperCausalityThreshold;
        this.upperParallelismThreshold = upperParallelismThreshold;
        this.lowerCausalityThreshold = lowerCausalityThreshold;
        this.lowerParallelismThreshold = lowerParallelismThreshold;
        this.clustering = new IncrementalHierarchicalClustering(adjacencyList, controlFlowSimThreshold, this);
    }

    // Detect method which takes in caseId, activity, and timestamp
    public String detect(String caseId, String activity, String timestamp) {
        clusteringHappen = false;
        eventCounter++;
        activityTimestamp.put(activity, timestamp);
        HashMap<String, Integer> activityData = activityIndices.get(activity);
        if (activityData == null) {
            activityData = new HashMap<>();
            activityData.put(caseId, 1);
            activityIndices.put(activity, activityData);
            activityToIndex.put(activity, activityIndexCounter++);
            activityCount.put(activity, 1);
        } else {
            activityData.put(caseId, activityData.getOrDefault(caseId, 0) + 1);
            activityCount.put(activity, activityCount.get(activity) + 1);
        }
        if (activityIndices.size() > directlyFollows.length) {
            directlyFollows = resizeMatrix(directlyFollows, activityIndices.size());
            footprintMatrix = resizeFootprintMatrix(footprintMatrix, activityIndices.size());
        }

        int[] indices = caseActivityIndices.getOrDefault(caseId, new int[]{-1, -1, cThreshold - 1});
        indices[0] = indices[1];
        indices[1] = activityToIndex.get(activity);
        indices[2]++;

        caseActivityIndices.put(caseId, indices);

        // Update directlyFollows matrix
        if (indices[0] != -1 && indices[1] != -1) {
            if (directlyFollows[indices[0]][indices[1]] == null) {
                directlyFollows[indices[0]][indices[1]] = new HashMap<>();
            }
            directlyFollows[indices[0]][indices[1]].put(caseId, directlyFollows[indices[0]][indices[1]].getOrDefault(caseId, 0) + 1);
        }
        footprintUpdate(indices[0], indices[1]);

        for (HashMap.Entry<String, int[]> entry : caseActivityIndices.entrySet()) {
            String key = entry.getKey();
            int[] value = entry.getValue();
        }
        int floorValue = (int) Math.floor(eventCounter / windowSize);
        if (floorValue != cThreshold) {
            Set<UniquePair> forgetCases = forgetCases();
            if (!forgetCases.isEmpty()) {
                for (UniquePair pair : forgetCases) {
                    footprintUpdate(pair.getFirst(), pair.getSecond());
                }
            }
            cThreshold = floorValue;
        }
        if (!clusteringHappen) {
            // Find the group in the mergeSet that contains the current 'activity'
            String groupWithCurrentActivity = null;
            for (Map.Entry<String, List<String>> entry : mergeSet.entrySet()) {
                if (entry.getKey().equals(activity) || entry.getValue().contains(activity) && entry.getValue().size() > 1) {
                    groupWithCurrentActivity = entry.getKey();
                    break;
                }
            }

            // If a group with the current 'activity' was found
            if (groupWithCurrentActivity != null) {
                // Check if the activityCount or activityTimestamp has changed for the representative of the group
                Integer currentCount = activityCount.get(activity);
                String currentTimestamp = activityTimestamp.get(activity);
                if (!currentCount.equals(lastActivityCount.get(activity)) || !currentTimestamp.equals(lastActivityTimestamp.get(activity))) {
                    // If the activityCount or activityTimestamp has changed, call reassessRepresentatives
                    clustering.reassessRepresentatives(activityCount, activityTimestamp);
                }
            }
        }

        previousMergeSet = mergeSet;
        mergeSet = clustering.getClusters();

        Map<String, String> activityCluster = clustering.getActivityToCluster();
        String outputActivity = activityCluster.get(activity); // Get the output activity from the activityToCluster map
        String outputActivityValue = (outputActivity != null) ? outputActivity : activity;

        lastActivityCount = new HashMap<>(activityCount);
        lastActivityTimestamp = new HashMap<>(activityTimestamp);

        return outputActivityValue;
    }

    private Set<UniquePair> forgetCases() {
        Iterator<Map.Entry<String, int[]>> iterator = caseActivityIndices.entrySet().iterator();
        Set<UniquePair> pairs = new HashSet<>();
        while (iterator.hasNext()) {
            Map.Entry<String, int[]> entry = iterator.next();
            int caseIdCounter = entry.getValue()[2];
            if (caseIdCounter <= (cThreshold - delay)) {
                iterator.remove();
                // Remove from directlyFollows
                for (int i = 0; i < directlyFollows.length; i++) {
                    for (int j = 0; j < directlyFollows[i].length; j++) {
                        if (directlyFollows[i][j] != null && directlyFollows[i][j].containsKey(entry.getKey())) {
                            // save i, j int values as a pair
                            pairs.add(new UniquePair(i, j));
                            directlyFollows[i][j].remove(entry.getKey());
                        }
                    }
                }
                // Remove from activityIndices and decrement activityCount
                for (Map.Entry<String, HashMap<String, Integer>> activityEntry : activityIndices.entrySet()) {
                    Integer removedCount = activityEntry.getValue().remove(entry.getKey());
                    if (removedCount != null) {
                        activityCount.put(activityEntry.getKey(), activityCount.get(activityEntry.getKey()) - removedCount);
                    }
                }
            }
        }
        return pairs;
    }

    private HashMap<String, Integer>[][] resizeMatrix(HashMap<String, Integer>[][] matrix, int newSize) {
        HashMap<String, Integer>[][] newMatrix = new HashMap[newSize][newSize];
        for (int i = 0; i < matrix.length; i++) {
            System.arraycopy(matrix[i], 0, newMatrix[i], 0, matrix[i].length);
        }
        return newMatrix;
    }

    private int[][] resizeFootprintMatrix(int[][] matrix, int newSize) {
        int[][] newMatrix = new int[newSize][newSize];
        for (int i = 0; i < matrix.length; i++) {
            System.arraycopy(matrix[i], 0, newMatrix[i], 0, matrix[i].length);
        }
        return newMatrix;
    }

    private void footprintUpdate(int prevActivityIndex, int currentActivityIndex) {
        prevFootprint = footprintMatrix;
        if (prevActivityIndex != -1) {
            int countXY = directlyFollows[prevActivityIndex][currentActivityIndex] != null ? directlyFollows[prevActivityIndex][currentActivityIndex].values().stream().mapToInt(Integer::intValue).sum() : 0;
            int countYX = directlyFollows[currentActivityIndex][prevActivityIndex] != null ? directlyFollows[currentActivityIndex][prevActivityIndex].values().stream().mapToInt(Integer::intValue).sum() : 0;
            float causalityValue = (float) (countXY - countYX) / (countXY + countYX + 1);
            float parallelismValue = (float) Math.max(countXY, countYX) / Math.min(countXY, countYX);

            //get the value of the footprint matrix before updating it, if it's not available its 0
            int prevValueA = footprintMatrix[currentActivityIndex][prevActivityIndex];
            int prevValueB = footprintMatrix[prevActivityIndex][currentActivityIndex];

            // Check if value is larger than 0.8 or smaller than -0.8
            if (Math.abs(causalityValue) >= upperCausalityThreshold) {
                // Determine if the value is positive or negative

                float sign = Math.signum(causalityValue);
                if (sign > 0) {
                    if (prevValueB != 1) {
                        footprintMatrix[prevActivityIndex][currentActivityIndex] = 1;
                        footprintMatrix[currentActivityIndex][prevActivityIndex] = 2;
                        List<Integer> xA = findAndAddSimilarValues(prevActivityIndex, prevValueA, 2, currentActivityIndex);
                        List<Integer> xB = findAndAddSimilarValues(currentActivityIndex, prevValueB, 1, prevActivityIndex);
                        printAndCalculateSimilarities(prevActivityIndex, currentActivityIndex, xA, xB);
                    }
                } else if (sign < 0) {
                    if (prevValueB != 2) {
                        footprintMatrix[prevActivityIndex][currentActivityIndex] = 2;
                        footprintMatrix[currentActivityIndex][prevActivityIndex] = 1;
                        List<Integer> xA = findAndAddSimilarValues(prevActivityIndex, prevValueA, 1, currentActivityIndex);
                        List<Integer> xB = findAndAddSimilarValues(currentActivityIndex, prevValueB, 2, prevActivityIndex);
                        printAndCalculateSimilarities(prevActivityIndex, currentActivityIndex, xA, xB);
                    }
                }
            } else if (parallelismValue <= upperParallelismThreshold) {
                if (prevValueB != 3) {
                    footprintMatrix[prevActivityIndex][currentActivityIndex] = 3;
                    footprintMatrix[currentActivityIndex][prevActivityIndex] = 3;
                    List<Integer> xA = findAndAddSimilarValues(prevActivityIndex, prevValueA, 3, currentActivityIndex);
                    List<Integer> xB = findAndAddSimilarValues(currentActivityIndex, prevValueB, 3, prevActivityIndex);
                    printAndCalculateSimilarities(prevActivityIndex, currentActivityIndex, xA, xB);
                }
            } else if (Math.abs(causalityValue) < lowerCausalityThreshold || parallelismValue >= lowerParallelismThreshold) {
                if (prevValueB != 0) {
                    footprintMatrix[prevActivityIndex][currentActivityIndex] = 0;
                    footprintMatrix[currentActivityIndex][prevActivityIndex] = 0;
                    List<Integer> xA = findAndAddSimilarValues(prevActivityIndex, prevValueA, 0, currentActivityIndex);
                    List<Integer> xB = findAndAddSimilarValues(currentActivityIndex, prevValueB, 0, prevActivityIndex);
                    printAndCalculateSimilarities(prevActivityIndex, currentActivityIndex, xA, xB);
                }
            }
        }
    }

    private void printAndCalculateSimilarities(int prevActivityIndex, int currentActivityIndex, List<Integer> xA, List<Integer> xB) {
        checkForMergeUnmerge(xA, currentActivityIndex);
        checkForMergeUnmerge(xB, prevActivityIndex);
    }

    private void checkForMergeUnmerge(List<Integer> indices, int i) {
        List<SimilarityResult> simIndex = new ArrayList<>();
        for (int k : indices) {
            if (i != k) {
                Map.Entry<Integer, Float> similarity = calculateSimilarity(i, k);
                ActivityPair pair = new ActivityPair(i, k);
                Float existingSimilarity = similarityMap.get(pair);
                if (existingSimilarity == null || !existingSimilarity.equals(similarity.getValue())) {
                    similarityMap.put(pair, similarity.getValue());
                    clustering.addSimilarity(getActivityFromIndex(i), getActivityFromIndex(k), similarity.getValue(), activityCount, activityTimestamp);
                    // Re-cluster with the updated data
                    clustering.clusterActivities();
                    clustering.reassessRepresentatives(activityCount, activityTimestamp);
                    clusteringHappen = true;
                }
            }
        }
    }

    private List<Integer> findAndAddSimilarValues(int prevIndex, int prevValue, int currentValue, int skipIndex) {
        List<Integer> xPrev = findSimilarValuesInColumn(prevIndex, prevValue, skipIndex);
        List<Integer> xCurrent = findSimilarValuesInColumn(prevIndex, currentValue, skipIndex);
        List<Integer> x = new ArrayList<>();
        x.addAll(xPrev);
        x.addAll(xCurrent);
        return x;
    }

    private Map.Entry<Integer, Float> calculateSimilarity(int i, int k) {
        int similarity = 0;
        int countI = 0;
        int countK = 0;
        for (int j = 0; j < footprintMatrix.length; j++) {
            if (footprintMatrix[i][j] != 0) {
                countI++;
            }
            if (footprintMatrix[k][j] != 0) {
                countK++;
            }
            if (footprintMatrix[i][j] != 0 && footprintMatrix[k][j] != 0 && footprintMatrix[i][j] == footprintMatrix[k][j]) {
                similarity++;
            }
        }
        float similarityValue = (float) similarity / Math.max(countI, countK);
        //if similarityValue is NaN
        if (Float.isNaN(similarityValue)) {
            similarityValue = 0;
        }

        return new AbstractMap.SimpleEntry<>(k, similarityValue);
    }

    public String getActivityFromIndex(int index) {
        for (Map.Entry<String, Integer> entry : activityToIndex.entrySet()) {
            if (entry.getValue().equals(index)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public List<Integer> findSimilarValuesInColumn(int columnIndex, int value, int skipIndex) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < footprintMatrix.length; i++) {
            if (i == skipIndex) {
                continue;
            }
            if (footprintMatrix[i][columnIndex] == value) {
                indices.add(i);
            }
        }
        return indices;
    }

    public LocalDateTime textToTime(String text) {
        return super.textToTime(text);
    }
}
