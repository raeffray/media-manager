const MediaService = require('./MediaServiceImpl');

const mediaService = new MediaService();

const executeUpload = (file) => {
    return mediaService
        .createMedia(file)
        .then((msg) => {
            console.log(msg);
        })
        .catch((err) => {
            console.log(err);
        });
};

const executeDownload = (file) => {
    return mediaService
        .getMedia(file)
        .then((msg) => {
            console.log(msg);
        })
        .catch((err) => {
            console.log(err);
        });
};

const executeList = (file) => {
    return mediaService
        .listMedias(file)
        .then((msg) => {
            console.log(msg);
        })
        .catch((err) => {
            console.log(err);
        });
};

const executeDelete = (file) => {
    return mediaService
        .deleteMedia(file)
        .then((msg) => {
            console.log(msg);
        })
        .catch((err) => {
            console.log(err);
        });
};

module.exports = { executeDownload, executeList, executeUpload, executeDelete };
