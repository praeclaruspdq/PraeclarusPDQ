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

var writeHandle;
var readFile;

window.getFile = async function(elemID) {
    let fileHandle;
    [fileHandle] = await window.showOpenFilePicker();
    const file = await fileHandle.getFile();
    const contents = await file.text();
    document.getElementById(elemID).$server.setfile(file.name, contents);
}


window.pickOpenFile = async function(elemID, desc, extn) {
    const opts = {
         types: [{
             description: desc,
             accept: {'text/plain': [extn]},
         }],
     };

    let fileHandle;
    [fileHandle] = await window.showOpenFilePicker(opts);
    readFile = await fileHandle.getFile();
    document.getElementById(elemID).$server.setFileName(readFile.name);
}


window.readFile = async function(elemID) {
    const contents = await readFile.text();
    document.getElementById(elemID).$server.setfile(contents);
}


window.pickSaveFile = async function(elemID, optsStr) {
    // const opts = {
    //     types: [{
    //         description: desc,
    //         accept: {mtype: [extn]},
    //     }],
    // };

    const opts = JSON.parse(optsStr);

    writeHandle = await window.showSaveFilePicker(opts);
    if (writeHandle) {
        const file = await writeHandle.getFile();
        document.getElementById(elemID).$server.setFileName(file.name);
    }
}


window.writeFile = async function(contents) {
    const writable = await writeHandle.createWritable();
    await writable.write(contents);
    await writable.close();
}


