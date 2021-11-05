/*
 * Copyright (c) 2021 Queensland University of Technology
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
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.revwalk.RevCommit;
import tech.tablesaw.api.Table;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author Michael Adams
 * @date 5/11/21
 */
public class Repo {

    private static final File REPO_DIR = new File("./repo");

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
            return rev.getId().toString();
        }
    }


    public static Table getTable(String objID, String tableName) throws IOException {
        try (Git git = Git.open(REPO_DIR)) {
            ObjectReader objectReader = git.getRepository().newObjectReader();
            ObjectLoader objectLoader = objectReader.open(ObjectId.fromString(objID));
            byte[] bytes = objectLoader.getBytes();
            String content = new String(bytes, StandardCharsets.UTF_8);
            return Table.read().csv(content, tableName);

//            RevWalk walk = new RevWalk(git.getRepository());
//            RevCommit commit = walk.parseCommit(ObjectId.fromString(objID));
//            commit.getTree().
        }
    }



    private static void checkInitiated() {
        if (!REPO_DIR.exists()) {
            if (REPO_DIR.mkdir()) {
                try (Git git = Git.init().setDirectory(REPO_DIR).call()) {
                }
                catch (GitAPIException e) {
                    System.out.println("Failed to initialise repository: " + e.getMessage());
                }
            }
            else {
                System.out.println("Failed to create repo directory");
            }
        }
    }

   
    private static String write(Table table) throws IOException {
        String fileName = table.name() + ".csv";         // needs extn for write() below
        File file = new File(REPO_DIR, fileName);
        table.write().toFile(file); 
        return table.name();
    }

}
