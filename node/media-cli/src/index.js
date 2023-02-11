const program = require('commander');
const {
    executeDownload,
    executeList,
    executeUpload,
    executeDelete,
} = require('./executor');

program
    .version('0.1.0')
    .option(
        '-l, --list',
        'List all files from the server. Reponse will be a json containing the media informatio. Usage: ./media-cli -l'
    )
    .option(
        '-u,  --upload <file-path>',
        'Upload a media to the remote server. Usage: ./media-cli -u /my-dir/file-to-upload.txt '
    )
    .option(
        '-d, --download <file-name>',
        'Download a media from the server. Usage: ./media-cli -do file-to-dowload.txt'
    )
    .option(
        '-r, --remove <file-name>',
        'Delete a media from the server. Usage: ./media-cli -d file-to-delete.txt'
    )
    .action((options) => {

        if (options.upload) executeUpload(options.upload);

        if (options.download) executeDownload(options.download);

        if (options.list) executeList();

        if (options.remove) executeDelete(options.remove);
    });

if (process.argv.length == 2) program.help();

program.parse();
