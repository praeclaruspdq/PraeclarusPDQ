/*
 * Copyright (c) 2021-2022 Queensland University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.processdataquality.praeclarus.repo;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.tablesaw.api.Table;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Michael Adams
 * @date 5/11/21
 */
public class Repo {

    private static final Logger LOG = LoggerFactory.getLogger(Repo.class);
    private static final File REPO_DIR = new File("../pdq_repo");

    static {
        checkInitiated();
    }


    private Repo() { }


    public static String commit(Table table, String msg, String user)
            throws IOException, GitAPIException {
        String fileName = write(table);

        try (Git git = Git.open(REPO_DIR)) {
            git.add().addFilepattern(fileName).call();
            RevCommit rev = git.commit().setMessage(msg)
                    .setAuthor(user, "user@example.com")
                    .call();                    
            return rev.getId().name();
        }
    }



    public static Table getTable(String objID, String tableName) throws IOException {
        String content = fetchContent(objID, tableName);
        return Table.read().csv(content, tableName);
    }


    public static List<LogEntry> getFullLog() throws IOException, GitAPIException {
        try (Git git = Git.open(REPO_DIR)) {
            return listLog(git.log().all().call());
        }
    }


    public static List<LogEntry> getLog(String fileName) throws GitAPIException, IOException {
        try (Git git = Git.open(REPO_DIR)) {
            return listLog(git.log().addPath(fileName + ".csv").call());
        }
    }
    

    private static void checkInitiated() {
        if (!REPO_DIR.exists()) {
            if (REPO_DIR.mkdir()) {
                try (Git git = Git.init().setDirectory(REPO_DIR).call()) {
                }
                catch (GitAPIException e) {
                    LOG.error("Failed to initialise repository: ", e);
                }
            }
            else {
                LOG.error("Failed to create repo directory");
            }
        }
    }

   
    private static String write(Table table) throws IOException {
        String fileName = table.name() + ".csv";         // needs extn for write() below
        File file = new File(REPO_DIR, fileName);
        table.write().toFile(file);
        return fileName;
    }


    private static List<LogEntry> listLog(Iterable<RevCommit> logs) {
        List<LogEntry> logList = new ArrayList<>();
        for (RevCommit rev : logs) {
            LogEntry entry = new LogEntry();
            entry.setTime(Instant.ofEpochSecond(rev.getCommitTime()));
            entry.setMessage(rev.getFullMessage());
            entry.setEntryName(rev.getId().getName());
            entry.setCommitter(rev.getAuthorIdent().getName());
            logList.add(entry);
        }
        Collections.reverse(logList);       // RevCommits are provided in reverse order
        return logList;
    }


    // based on: https://stackoverflow.com/questions/1685228/how-to-cat-a-file-in-jgit
    public static String fetchContent(String commitID, String path)
            throws MissingObjectException, IncorrectObjectTypeException,
            IOException {

        ObjectReader reader = null;
        try (Git git = Git.open(REPO_DIR)) {
            ObjectId id = ObjectId.fromString(commitID);
            reader = git.getRepository().newObjectReader();

            // Get the commit object for that revision
            RevWalk walk = new RevWalk(reader);
            RevCommit commit = walk.parseCommit(id);

            // Get the revision's file tree and the single file's path
            RevTree tree = commit.getTree();
            TreeWalk treewalk = TreeWalk.forPath(reader, path + ".csv", tree);

            if (treewalk != null) {
                byte[] data = reader.open(treewalk.getObjectId(0)).getBytes();
                return new String(data, StandardCharsets.UTF_8);
            }
            else {
                return "";
            }
        }
        finally {
            if (reader != null) reader.close();
        }
    }

}
