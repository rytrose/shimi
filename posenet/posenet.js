const tf = require('@tensorflow/tfjs');
require('@tensorflow/tfjs-node');
global.XMLHttpRequest = require("xhr2");
const fetch = require('node-fetch');
const posenet = require('@tensorflow-models/posenet');
const {Image, createCanvas} = require('canvas');
const dgram = require('dgram');
const OSC = require('osc-js');
const cv = require('opencv');

let net;
let socket;

let setup = async function () {
    net = await posenet.load();
    socket = dgram.createSocket('udp4');
};

let send = function (address, arg) {
    let message = new OSC.Message(address, arg);
    let bin = message.pack();
    socket.send(Buffer.from(bin), 0, bin.byteLength, 8000, 'localhost');
};

async function singlePoseOutput(imageElement) {
    const imageScaleFactor = 0.50;
    const flipHorizontal = false;
    const outputStride = 16;

    return await net.estimateSinglePose(imageElement, imageScaleFactor, flipHorizontal, outputStride);
}

let predict = async function () {
    // // Get image
    // let img_path = "https://i.imgur.com/GuAB8OE.jpg";
    // let buffer = await fetch(img_path).then(res => res.buffer());
    // let img = new Image();
    // img.src = buffer;
    //
    // // Draw to canvas
    // const canvas = createCanvas(img.width, img.height);
    // canvas.getContext('2d').drawImage(img, 0, 0);
    //
    // // Get prediction
    // let pose = await singlePoseOutput(canvas);
    // let string = JSON.stringify(pose);
    // send('/posenet', string);

    let camera;
    try {
        camera = new cv.VideoCapture(0);
    } catch (e) {
        console.log("Couldn't start camera:", e);
    }
    read(camera);
};

let read = function (camera) {
    try {
        camera.read(async function (err, im) {
            let img = new Image();
            img.src = im.toBuffer();

            // Draw to canvas
            const canvas = createCanvas(img.width, img.height);
            canvas.getContext('2d').drawImage(img, 0, 0);

            // Get prediction
            let pose = await singlePoseOutput(canvas);
            let string = JSON.stringify(pose);
            send('/posenet', string);
            read(camera);
        });
    } catch (e) {
        console.log("Couldn't start camera:", e)
    }
};

let main = async function () {
    await setup();
    await predict();
};

main();