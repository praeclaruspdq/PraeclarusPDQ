/*
 * Copyright (c) 2022 Queensland University of Technology
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

package com.processdataquality.praeclarus.writer;

import com.processdataquality.praeclarus.annotation.Plugin;
import com.processdataquality.praeclarus.util.DataCollection;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.FunctionalSyntaxDocumentFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.WriteOptions;

import java.io.IOException;

/**
 * @author Michael Adams
 * @date 27/7/2022
 */
@Plugin(
        name = "OWL Writer",
        author = "Michael Adams",
        version = "1.0",
        synopsis = "Writes an OWL ontology to file. Requires a previous plugin to " +
                "create and store an OWLOntology called 'ontology' in its aux dataset",
        fileDescriptors = "OWL Files;text/owl;.owl"
)
public class OwlWriter extends AbstractDataWriter {


    public OwlWriter() { super(); }

    @Override
    protected WriteOptions getWriteOptions() throws IOException {
        return null;
    }


    @Override
    public void write(Table table, DataCollection auxData) throws IOException {
        Object o = auxData.get("ontology");
        if (o instanceof OWLOntology) {
            OWLOntology ontology = (OWLOntology) o;

            try {
                OWLManager.createOWLOntologyManager().saveOntology(ontology,
                        new FunctionalSyntaxDocumentFormat(),
                        getDestinationAsOutputStream());
            }
            catch (OWLOntologyStorageException e) {
                throw new IOException(e.getMessage(), e.getCause());
            }
        }
        else throw new IOException(
                "Unable to locate ontology data within plugin's auxiliary data sets");
    }
    

}

