var nexe = require('nexe');

nexe.compile({
    input: './target/synapse.js', // where the input file is
    output: './target/exe/synapse', // where to output the compiled binary
    nodeVersion: '5.9.1', // node version
    nodeTempDir: './target/tmp/nexe', // where to store node source.
    nodeConfigureArgs: ['--fully-static', '--without-npm'], // for all your configure arg needs.
    nodeMakeArgs: ["-j", "4"], // when you want to control the make process.
    python: '/usr/bin/python', // for non-standard python setups. Or python 3.x forced ones.
    resourceFiles: [  ], // array of files to embed.
    flags: true, // use this for applications that need command line flags.
    jsFlags: "--use_strict", // v8 flags
    framework: "nodejs" // node, nodejs, or iojs
}, function(err) {
    if(err) {
        return console.log(err);
    }

    // do whatever
});
