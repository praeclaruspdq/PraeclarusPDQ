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

import { get, set } from "./index.js";

// window.addUploadListener = function(elem) {
//     elem.onchange = async event => {
//         const file = event.target.files[0];
//         await set(file.name, file);
//         elem.$server.setFileName(file.name);
//     }
// }

window.pickAndSaveFile = async function(elemID, optsStr, contents) {
    try {
        if ('showSaveFilePicker' in window) {
            const opts = JSON.parse(optsStr);
            const writeHandle = await window.showSaveFilePicker(opts);

            if (writeHandle && await verifyPermission(writeHandle, true)) {
                const writable = await writeHandle.createWritable();
                await writable.write(contents);
                await writable.close();
            }
        }
    }
    catch (error) {
        alert(error.message);
    }
}

window.pickSaveFile = async function(elemID, optsStr) {
    try {
        if ('showSaveFilePicker' in window) {
            const opts = JSON.parse(optsStr);
            const writeHandle = await window.showSaveFilePicker(opts);

            if (writeHandle) {
                await set(elemID, writeHandle);
                const file = await writeHandle.getFile();
                document.getElementById(elemID).$server.setSaveFileName(file.name);
            }
 //           return;
        }
        
        // const opts = {
        //     type: 'save-file',
        //     accepts: [{
        //       description: 'Text file',
        //       extensions: ['txt'],
        //       mimeTypes: ['text/plain'],
        //     }],
        //   };
        // const writeHandle = await window.chooseFileSystemEntries(opts);
    }
    catch (error) {
        alert(error.message);
    }
}


window.writeFile = async function(elemID, contents) {
    try {
        const writeHandle = await get(elemID);
        if (writeHandle && await verifyPermission(writeHandle, true)) {
            const writable = await writeHandle.createWritable();
            await writable.write(contents);
            await writable.close();
        }
    }
    catch (error) {
        alert(error.message);
    }
}

// from: https://web.dev/file-system-access/
async function verifyPermission(fileHandle, withWrite) {
  const opts = {};
  if (withWrite) {
    opts.writable = true;
    // For Chrome 86 and later...
    opts.mode = 'readwrite';
  }
  // Check if we already have permission, if so, return true.
  if (await fileHandle.queryPermission(opts) === 'granted') {
    return true;
  }
  // Request permission to the file, if the user grants permission, return true.
  if (await fileHandle.requestPermission(opts) === 'granted') {
    return true;
  }
  // The user did not grant permission, return false.
  return false;
}




// //var writeHandle;
// //var readFile;
//
// // called from FileInput constructor

//
// async function handleUploadSelection(event) {
//     const file = event.target.files[0];
//     await set(file.name, file);
//     event.target.$server.setFileName(file.name);
// }
//
//
// // called from FileInput#upload
// window.upload = async function(elem, filename) {
//     const file = await get(filename);
//     const reader = new FileReader();
//     reader.readAsText(file, 'UTF-8');
//
//     reader.onload = event => {
//         const content = event.target.result;
//         elem.$server.setContent(content);
//     }
// }
//
//
// window.getFile = async function(elemID) {
//     let fileHandle;
//     [fileHandle] = await window.showOpenFilePicker();
//     const file = await fileHandle.getFile();
//     const contents = await file.text();
//     document.getElementById(elemID).$server.setfile(file.name, contents);
// }
//
//
// window.pickOpenFile = async function(elemID, optsStr) {
//     const opts = JSON.parse(optsStr);
//     let fileHandle;
//     [fileHandle] = await window.showOpenFilePicker(opts);
//     readFile = await fileHandle.getFile();
//     document.getElementById(elemID).$server.setFileName(readFile.name);
// }
//
//
// window.readFile = async function(elemID) {
//     const contents = await readFile.text();
//     document.getElementById(elemID).$server.setfile(contents);
// }





