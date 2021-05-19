function fileinput() {
    const fileInput = document.getElementById('file-input');
    fileInput.onchange = function (event) {
        return fileInput.files[0];
    }
    fileInput.click();
}

function inputselect() {
    return document.getElementById('file-input').files[0];
}

window.posn = function() {
    var canvas = document.getElementById("thecanvas");
    var ctx = canvas.getContext("2d");
    ctx.font = "16px Arial";

    // canvas.addEventListener("mousemove", function(e) {
    //     var cRect = canvas.getBoundingClientRect(); // Gets CSS pos, and width/height
    //     var canvasX = Math.round(e.clientX - cRect.left); // Subtract the 'left' of the canvas
    //     var canvasY = Math.round(e.clientY - cRect.top); // from the X/Y positions to make
    //     ctx.clearRect(0, 0, canvas.width, canvas.height); // (0,0) the top left of the canvas
    //     ctx.fillText("X: "+canvasX+", Y: "+canvasY, 10, 20);
    // });
}

window.drawStep = function(name) {
    var canvas = document.getElementById("thecanvas");
    var ctx = canvas.getContext("2d");

    // fix aspect ratio of the inner drawing surface relative to the
    // canvas element size (2w:1h)
    // canvas.width  = window.innerWidth;
    // canvas.height = window.innerHeight;
    canvas.width = canvas.clientWidth;
    canvas.height = canvas.clientHeight;
//    canvas.width = canvas.height * (canvas.clientWidth / canvas.clientHeight);

    var leftX=25; var topY=25; var width=100; var height=80;

    ctx.font = "16px Arial";
    ctx.lineWidth=2;


    ctx.beginPath();
    ctx.rect(leftX, topY, width, height);
    ctx.stroke();

    ctx.beginPath();
    ctx.arc(leftX, topY+40,5, 0, 360);
    ctx.fill();
    ctx.stroke();

    ctx.beginPath();
    ctx.arc(leftX+100, topY+40,5, 0, 360);
    ctx.fill();
    ctx.stroke();

    var words = name.split(" ");
    var y = topY + 20;
    var i;
    for (i=0; i < words.length; i++) {
        ctx.fillText(words[i], leftX + 10, y, 80);
        y = y + 30;
    }

}


