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

var canvas;
var sx = -1, sy, tx, ty;

window.getClientWidth = function() {
    return canvas.clientWidth;
}

window.getClientHeight = function() {
    return canvas.clientHeight;
}

window.setDimensions = function() {
    canvas.width = canvas.clientWidth;
    canvas.height = canvas.clientHeight;
}

window.loadFile = async function() {
    const opts = {
      types: [{
          description: 'Praeclarus Workflows',
          accept: { 'application/json': ['.pwf', '.json']}
        }],
      excludeAcceptAllOption: true,
      multiple: false
    };

    let fileHandle;
    [fileHandle] = await window.showOpenFilePicker(opts);
    const file = await fileHandle.getFile();
    const contents = await file.text();
    canvas.$server.fileloaded(contents);
}


window.saveFile = async function(contents) {
   try {
       const opts = {
           types: [{
               description: 'Praeclarus Workflows',
               accept: {'application/json': ['.pwf', '.json']}
           }],
       };

       const fileHandle = await window.showSaveFilePicker(opts);
       const writable = await fileHandle.createWritable();
       await writable.write(contents);
       await writable.close();
   }
   catch (error) {
       alert(error.message);
   }
}


window.saveThenLoadFile = async function(contents) {
    await window.saveFile(contents);
    await window.loadFile();
}


window.init = function() {
    canvas = document.getElementById("workflowCanvas");
    canvas.addEventListener("mousedown", fireMouseDown, false);
    canvas.addEventListener("mousemove", fireMouseMove, false);
    canvas.addEventListener("mouseup", fireMouseUp, false);
    canvas.addEventListener("click", fireClick, false);
    canvas.addEventListener("dblclick", fireDblClick, false);
}

function fireMouseDown(event) {
    var pos = getMousePos(event);
    canvas.$server.mousedown(pos.x, pos.y);
    sx = pos.x;
    sy = pos.y;
}

function fireMouseMove(event) {
    if (sx > -1) {
        var pos = getMousePos(event);
        tx = pos.x;
        ty = pos.y;
        canvas.$server.mousemove(tx, ty);
    }
}

function fireMouseUp(event) {
    if (sx > -1) {
        var pos = getMousePos(event);
        canvas.$server.mouseup(pos.x, pos.y);
    }
    sx = sy = tx = ty = -1;
}

function fireClick(event) {
    var pos = getMousePos(event);
    canvas.$server.mouseclick(pos.x, pos.y);
}

function fireDblClick(event) {
    var pos = getMousePos(event);
    canvas.$server.mousedblclick(pos.x, pos.y);
}

function getMousePos(event) {
    var rect = canvas.getBoundingClientRect();
    return {
      x: event.clientX - rect.left,
      y: event.clientY - rect.top
    };
}

window.textWidth = function(text) {
    ctx = canvas.getContext('2d');
    var metrics = ctx.measureText(text);
    return metrics.width;
}





